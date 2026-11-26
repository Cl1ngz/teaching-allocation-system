package pl.pb.tas.teaching_allocation_system.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pl.pb.tas.teaching_allocation_system.models.entities.AcademicDegree;
import pl.pb.tas.teaching_allocation_system.models.entities.AcademicYear;
import pl.pb.tas.teaching_allocation_system.models.entities.AppUser;
import pl.pb.tas.teaching_allocation_system.models.entities.AssignmentChangeRequest;
import pl.pb.tas.teaching_allocation_system.models.entities.AuditLog;
import pl.pb.tas.teaching_allocation_system.models.entities.ClassGroup;
import pl.pb.tas.teaching_allocation_system.models.entities.ClassType;
import pl.pb.tas.teaching_allocation_system.models.entities.Course;
import pl.pb.tas.teaching_allocation_system.models.entities.CourseOffering;
import pl.pb.tas.teaching_allocation_system.models.entities.CourseProgram;
import pl.pb.tas.teaching_allocation_system.models.entities.CourseProgramId;
import pl.pb.tas.teaching_allocation_system.models.entities.OrganizationalUnit;
import pl.pb.tas.teaching_allocation_system.models.entities.Semester;
import pl.pb.tas.teaching_allocation_system.models.entities.Specialization;
import pl.pb.tas.teaching_allocation_system.models.entities.StudyProgram;
import pl.pb.tas.teaching_allocation_system.models.entities.Substitution;
import pl.pb.tas.teaching_allocation_system.models.entities.Teacher;
import pl.pb.tas.teaching_allocation_system.models.entities.TeacherPensum;
import pl.pb.tas.teaching_allocation_system.models.entities.TeacherSpecialization;
import pl.pb.tas.teaching_allocation_system.models.entities.TeacherSpecializationId;
import pl.pb.tas.teaching_allocation_system.models.entities.TeachingAssignment;
import pl.pb.tas.teaching_allocation_system.models.entities.UserRole;
import pl.pb.tas.teaching_allocation_system.models.enums.ApprovalStatus;
import pl.pb.tas.teaching_allocation_system.models.enums.ChangeType;
import pl.pb.tas.teaching_allocation_system.models.enums.RoleType;
import pl.pb.tas.teaching_allocation_system.models.enums.StudyLevel;
import pl.pb.tas.teaching_allocation_system.models.enums.Term;
import pl.pb.tas.teaching_allocation_system.models.enums.UnitType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Mapping tests of the key relations of the remaining model areas (SF-18), on
 * PostgreSQL started by Testcontainers (same approach as TeacherRepositoryTest).
 * The full context also validates every entity against the Flyway schema.
 * Requires Docker; without it the class is skipped. Each test is rolled back.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class ModelMappingTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine");

    @Autowired private AcademicDegreeRepository degreeRepository;
    @Autowired private OrganizationalUnitRepository unitRepository;
    @Autowired private TeacherRepository teacherRepository;
    @Autowired private SpecializationRepository specializationRepository;
    @Autowired private TeacherSpecializationRepository teacherSpecializationRepository;
    @Autowired private AcademicYearRepository yearRepository;
    @Autowired private SemesterRepository semesterRepository;
    @Autowired private TeacherPensumRepository pensumRepository;
    @Autowired private StudyProgramRepository programRepository;
    @Autowired private ClassTypeRepository classTypeRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private CourseProgramRepository courseProgramRepository;
    @Autowired private CourseOfferingRepository offeringRepository;
    @Autowired private ClassGroupRepository groupRepository;
    @Autowired private TeachingAssignmentRepository assignmentRepository;
    @Autowired private AppUserRepository userRepository;
    @Autowired private UserRoleRepository roleRepository;
    @Autowired private AssignmentChangeRequestRepository changeRequestRepository;
    @Autowired private SubstitutionRepository substitutionRepository;
    @Autowired private AuditLogRepository auditLogRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private OrganizationalUnit faculty;
    private OrganizationalUnit department;
    private Teacher kowalski;
    private Teacher nowak;
    private AcademicYear year;
    private Course course;
    private ClassGroup labGroup;
    private TeachingAssignment kowalskiInLab;
    private AppUser admin;

    @BeforeEach
    void setUp() {
        // Dictionaries come from migrations V2 (degrees) and V9 (class types).
        AcademicDegree doctor = degreeRepository.findByCode("DR").orElseThrow();
        ClassType lab = classTypeRepository.findByCode("LAB").orElseThrow();

        faculty = unitRepository.save(new OrganizationalUnit(UnitType.FACULTY, "Test Faculty", null));
        department = unitRepository.save(new OrganizationalUnit(UnitType.DEPARTMENT, "Test Department", faculty));
        kowalski = teacherRepository.save(new Teacher(doctor, "Jan", "Kowalski-Test", "jan.k@test.example"));
        nowak = teacherRepository.save(new Teacher(doctor, "Anna", "Nowak-Test", "anna.n@test.example"));

        year = yearRepository.save(new AcademicYear("2026/2027", LocalDate.of(2026, 10, 1), LocalDate.of(2027, 9, 30)));
        Semester winter = semesterRepository.save(
                new Semester(year, Term.WINTER, LocalDate.of(2026, 10, 1), LocalDate.of(2027, 2, 28)));

        course = courseRepository.save(new Course(department, "TEST-BD", "Bazy danych", new BigDecimal("5.0")));
        CourseOffering offering = offeringRepository.save(new CourseOffering(course, winter));
        labGroup = groupRepository.save(new ClassGroup(offering, lab, "L1", new BigDecimal("30.00")));
        kowalskiInLab = assignmentRepository.save(new TeachingAssignment(labGroup, kowalski, new BigDecimal("60.00")));
        assignmentRepository.save(new TeachingAssignment(labGroup, nowak, new BigDecimal("40.00")));

        admin = userRepository.save(new AppUser("test-admin"));

        flushAndClear();
    }

    @Test
    @DisplayName("Assignment -> group -> offering -> course and semester")
    void assignmentBelongsToGroupOfferingAndSemester() {
        TeachingAssignment loaded = assignmentRepository.findById(kowalskiInLab.getId()).orElseThrow();

        ClassGroup group = loaded.getClassGroup();
        assertThat(group.getGroupCode()).isEqualTo("L1");
        assertThat(group.getClassType().getCode()).isEqualTo("LAB");
        assertThat(group.getOffering().getCourse().getCode()).isEqualTo("TEST-BD");
        assertThat(group.getOffering().getSemester().getTerm()).isEqualTo(Term.WINTER);
        assertThat(group.getOffering().getSemester().getAcademicYear().getLabel()).isEqualTo("2026/2027");
        assertThat(loaded.getTeacher().getLastName()).isEqualTo("Kowalski-Test");
        assertThat(loaded.assignedHours()).isEqualByComparingTo("18.00");

        assertThat(assignmentRepository.findByClassGroupId(labGroup.getId()))
                .extracting(assignment -> assignment.getTeacher().getLastName())
                .containsExactlyInAnyOrder("Kowalski-Test", "Nowak-Test");
    }

    @Test
    @DisplayName("Composite keys: teacher specialization and course in a study program")
    void compositeKeys() {
        Specialization databases = specializationRepository.save(new Specialization("TEST-DB", "Bazy danych"));
        StudyProgram informatics = programRepository.save(
                new StudyProgram(faculty, "TEST-INF", "Informatyka", StudyLevel.BACHELOR));
        flushAndClear();

        Teacher teacher = teacherRepository.findById(kowalski.getId()).orElseThrow();
        Specialization specialization = specializationRepository.findById(databases.getId()).orElseThrow();
        Course loadedCourse = courseRepository.findById(course.getId()).orElseThrow();
        StudyProgram program = programRepository.findById(informatics.getId()).orElseThrow();
        teacherSpecializationRepository.save(new TeacherSpecialization(teacher, specialization, (short) 3));
        courseProgramRepository.save(new CourseProgram(loadedCourse, program, (short) 4, true));
        flushAndClear();

        TeacherSpecialization competence = teacherSpecializationRepository
                .findById(new TeacherSpecializationId(kowalski.getId(), databases.getId())).orElseThrow();
        assertThat(competence.getLevel()).isEqualTo((short) 3);
        assertThat(competence.getSpecialization().getCode()).isEqualTo("TEST-DB");

        CourseProgram inProgram = courseProgramRepository
                .findById(new CourseProgramId(course.getId(), informatics.getId())).orElseThrow();
        assertThat(inProgram.getSemesterNo()).isEqualTo((short) 4);
        assertThat(inProgram.isMandatory()).isTrue();
        assertThat(inProgram.getStudyProgram().getCode()).isEqualTo("TEST-INF");
    }

    @Test
    @DisplayName("Required specializations of a course (M:N via course_specializations)")
    void courseRequiredSpecializations() {
        Specialization databases = specializationRepository.save(new Specialization("TEST-DB", "Bazy danych"));
        Course loadedCourse = courseRepository.findById(course.getId()).orElseThrow();
        loadedCourse.getRequiredSpecializations().add(databases);
        flushAndClear();

        assertThat(courseRepository.findById(course.getId()).orElseThrow().getRequiredSpecializations())
                .extracting(Specialization::getCode)
                .containsExactly("TEST-DB");
    }

    @Test
    @DisplayName("Pensum snapshot: effective_hours is computed by the database on insert and update")
    void effectiveHoursIsGenerated() {
        TeacherPensum pensum = pensumRepository.save(
                new TeacherPensum(kowalski, year, new BigDecimal("120.00"), new BigDecimal("30.00")));
        entityManager.flush();
        assertThat(pensum.getEffectiveHours()).isEqualByComparingTo("90.00");

        pensum.setReductionHours(new BigDecimal("40.00"));
        entityManager.flush();
        assertThat(pensum.getEffectiveHours()).isEqualByComparingTo("80.00");
    }

    @Test
    @DisplayName("Change requests: factory methods satisfy the CHECK and copy the old state")
    void changeRequests() {
        TeachingAssignment assignment = assignmentRepository.findById(kowalskiInLab.getId()).orElseThrow();
        Teacher substitute = teacherRepository.findById(nowak.getId()).orElseThrow();
        AppUser requester = userRepository.findById(admin.getId()).orElseThrow();

        changeRequestRepository.save(AssignmentChangeRequest.add(assignment.getClassGroup(), substitute,
                new BigDecimal("10.00"), requester));
        AssignmentChangeRequest change = changeRequestRepository.save(
                AssignmentChangeRequest.changeShare(assignment, new BigDecimal("50.00"), requester));
        flushAndClear();

        AssignmentChangeRequest loaded = changeRequestRepository.findById(change.getId()).orElseThrow();
        assertThat(loaded.getChangeType()).isEqualTo(ChangeType.CHANGE_SHARE);
        assertThat(loaded.getOldSharePercent()).isEqualByComparingTo("60.00");
        assertThat(loaded.getOldTeacher().getId()).isEqualTo(kowalski.getId());
        assertThat(loaded.getClassGroup().getId()).isEqualTo(labGroup.getId());
        assertThat(changeRequestRepository.findByStatusOrderByRequestedAtAsc(ApprovalStatus.SUBMITTED)).hasSize(2);
    }

    @Test
    @DisplayName("Change requests: the database rejects an assignment from another group")
    void changeRequestWithAssignmentFromAnotherGroupIsRejected() {
        TeachingAssignment assignment = assignmentRepository.findById(kowalskiInLab.getId()).orElseThrow();
        ClassGroup otherGroup = groupRepository.save(new ClassGroup(assignment.getClassGroup().getOffering(),
                assignment.getClassGroup().getClassType(), "L2", new BigDecimal("30.00")));
        AssignmentChangeRequest request = AssignmentChangeRequest.remove(assignment,
                userRepository.findById(admin.getId()).orElseThrow());
        request.setClassGroup(otherGroup);

        assertThatThrownBy(() -> changeRequestRepository.saveAndFlush(request))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Substitution points to an assignment, which determines group and substituted teacher")
    void substitution() {
        Substitution substitution = substitutionRepository.save(new Substitution(
                assignmentRepository.findById(kowalskiInLab.getId()).orElseThrow(),
                teacherRepository.findById(nowak.getId()).orElseThrow(),
                LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 9), new BigDecimal("4.00"),
                userRepository.findById(admin.getId()).orElseThrow()));
        flushAndClear();

        Substitution loaded = substitutionRepository.findById(substitution.getId()).orElseThrow();
        assertThat(loaded.getAssignment().getTeacher().getId()).isEqualTo(kowalski.getId());
        assertThat(loaded.getAssignment().getClassGroup().getGroupCode()).isEqualTo("L1");
        assertThat(loaded.getSubstituteTeacher().getId()).isEqualTo(nowak.getId());
        assertThat(loaded.getStatus()).isEqualTo(ApprovalStatus.SUBMITTED);
    }

    @Test
    @DisplayName("User linked to a teacher, scoped role and audit log with jsonb values")
    void usersRolesAndAudit() {
        AppUser user = new AppUser("jan.kowalski");
        user.setTeacher(teacherRepository.findById(kowalski.getId()).orElseThrow());
        user = userRepository.save(user);
        roleRepository.save(new UserRole(user, RoleType.TEACHER, null));
        roleRepository.save(new UserRole(user, RoleType.DEPARTMENT_HEAD, unitRepository.findById(department.getId()).orElseThrow()));
        AuditLog entry = auditLogRepository.save(new AuditLog("teaching_assignments", kowalskiInLab.getId(), "UPDATE",
                user, "{\"share_percent\": 60}", "{\"share_percent\": 50}"));
        flushAndClear();

        AppUser loaded = userRepository.findByUsername("jan.kowalski").orElseThrow();
        assertThat(loaded.getTeacher().getId()).isEqualTo(kowalski.getId());
        assertThat(roleRepository.findByUserId(loaded.getId()))
                .extracting(UserRole::getRole)
                .containsExactlyInAnyOrder(RoleType.TEACHER, RoleType.DEPARTMENT_HEAD);

        AuditLog loadedEntry = auditLogRepository.findById(entry.getId()).orElseThrow();
        assertThat(loadedEntry.getOldValues()).isEqualTo("{\"share_percent\": 60}");
        assertThat(loadedEntry.getNewValues()).isEqualTo("{\"share_percent\": 50}");
        assertThat(loadedEntry.getChangedBy().getUsername()).isEqualTo("jan.kowalski");
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}

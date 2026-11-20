package pl.pb.tas.teaching_allocation_system.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import pl.pb.tas.teaching_allocation_system.models.TeacherSummary;
import pl.pb.tas.teaching_allocation_system.models.entities.AcademicDegree;
import pl.pb.tas.teaching_allocation_system.models.entities.Employment;
import pl.pb.tas.teaching_allocation_system.models.entities.OrganizationalUnit;
import pl.pb.tas.teaching_allocation_system.models.entities.Position;
import pl.pb.tas.teaching_allocation_system.models.entities.Teacher;
import pl.pb.tas.teaching_allocation_system.models.enums.EmployeeGroup;
import pl.pb.tas.teaching_allocation_system.models.enums.EmploymentType;
import pl.pb.tas.teaching_allocation_system.models.enums.UnitType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests on a real PostgreSQL started by Testcontainers. The full
 * application context runs the Flyway migrations and Hibernate validates the
 * entities against them (ddl-auto=validate). Requires Docker; without it the
 * class is skipped. Each test runs in a transaction that is rolled back.
 */
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class TeacherRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine");

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 20);

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private EmploymentRepository employmentRepository;

    @Autowired
    private OrganizationalUnitRepository unitRepository;

    @Autowired
    private AcademicDegreeRepository degreeRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @PersistenceContext
    private EntityManager entityManager;

    private OrganizationalUnit faculty;
    private OrganizationalUnit department;
    private Teacher withPrimary;
    private Teacher withSecondaryOnly;
    private Teacher withoutEmployment;

    @BeforeEach
    void setUp() {
        // Dictionaries come from migration V2.
        AcademicDegree doctor = degreeRepository.findByCode("DR").orElseThrow();
        Position adjunct = positionRepository.findByCode("ADIUNKT").orElseThrow();

        faculty = unitRepository.save(new OrganizationalUnit(UnitType.FACULTY, "Test Faculty", null));
        department = unitRepository.save(new OrganizationalUnit(UnitType.DEPARTMENT, "Test Department", faculty));

        withPrimary = teacherRepository.save(new Teacher(doctor, "Anna", "Aaa-Test", "anna.aaa@test.example"));
        withSecondaryOnly = teacherRepository.save(new Teacher(doctor, "Bartosz", "Bbb-Test", "bartosz.bbb@test.example"));
        withoutEmployment = teacherRepository.save(new Teacher(doctor, "Celina", "Ccc-Test", "celina.ccc@test.example"));

        employmentRepository.save(new Employment(withPrimary, department, adjunct,
                EmployeeGroup.RESEARCH_TEACHING, EmploymentType.PRIMARY,
                new BigDecimal("1.00"), LocalDate.of(2020, 10, 1), null));
        // Ended primary employment and a current secondary one: no primary employment today.
        employmentRepository.save(new Employment(withSecondaryOnly, department, adjunct,
                EmployeeGroup.TEACHING, EmploymentType.PRIMARY,
                new BigDecimal("1.00"), LocalDate.of(2015, 10, 1), LocalDate.of(2025, 9, 30)));
        employmentRepository.save(new Employment(withSecondaryOnly, department, adjunct,
                EmployeeGroup.TEACHING, EmploymentType.SECONDARY,
                new BigDecimal("0.50"), LocalDate.of(2025, 10, 1), null));

        // Write to the database and forget the loaded objects, so the tests read from PostgreSQL.
        entityManager.flush();
        entityManager.clear();
        statistics().clear();
    }

    @Test
    @DisplayName("Teacher list contains the degree and the primary employment in force")
    void summariesContainDegreeAndCurrentPrimaryEmployment() {
        Map<Long, TeacherSummary> byId = ownSummaries(teacherRepository.findAllSummaries(TODAY));

        assertThat(byId).hasSize(3);

        TeacherSummary primary = byId.get(withPrimary.getId());
        assertThat(primary.degreeCode()).isEqualTo("DR");
        assertThat(primary.unitName()).isEqualTo("Test Department");
        assertThat(primary.positionName()).isEqualTo("adiunkt");

        assertThat(byId.get(withSecondaryOnly.getId()).unitName()).isNull();
        assertThat(byId.get(withoutEmployment.getId()).unitName()).isNull();
        assertThat(byId.get(withoutEmployment.getId()).degreeName()).isEqualTo("doktor");
    }

    @Test
    @DisplayName("Teacher list is loaded with a single SQL query (no N+1)")
    void summariesUseSingleQuery() {
        teacherRepository.findAllSummaries(TODAY);

        assertThat(statistics().getPrepareStatementCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Teacher entities come with the degree loaded in the same query")
    void entityGraphLoadsDegree() {
        List<Teacher> teachers = teacherRepository.findAllByOrderByLastNameAscFirstNameAsc();
        teachers.forEach(teacher -> teacher.getAcademicDegree().getName());

        assertThat(teachers).extracting(Teacher::getId)
                .contains(withPrimary.getId(), withSecondaryOnly.getId(), withoutEmployment.getId());
        assertThat(teachers).allMatch(teacher -> Hibernate.isInitialized(teacher.getAcademicDegree()));
        assertThat(statistics().getPrepareStatementCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Employment history comes with unit and position, newest first")
    void employmentHistoryLoadsUnitAndPosition() {
        List<Employment> history = employmentRepository.findByTeacherIdOrderByValidFromDesc(withSecondaryOnly.getId());

        assertThat(history).extracting(Employment::getEmploymentType)
                .containsExactly(EmploymentType.SECONDARY, EmploymentType.PRIMARY);
        assertThat(history.getFirst().getUnit().getName()).isEqualTo("Test Department");
        assertThat(history.getFirst().getPosition().getCode()).isEqualTo("ADIUNKT");
        assertThat(history.getFirst().isActiveOn(TODAY)).isTrue();
        assertThat(history.getLast().isActiveOn(TODAY)).isFalse();
        assertThat(statistics().getPrepareStatementCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Unit hierarchy: parent and children")
    void unitHierarchy() {
        assertThat(unitRepository.findByParentIdOrderByNameAsc(faculty.getId()))
                .extracting(OrganizationalUnit::getId)
                .containsExactly(department.getId());

        OrganizationalUnit loaded = unitRepository.findById(department.getId()).orElseThrow();
        assertThat(loaded.getParent().getId()).isEqualTo(faculty.getId());
        assertThat(loaded.getUnitType()).isEqualTo(UnitType.DEPARTMENT);
    }

    @Test
    @DisplayName("Technical columns are filled on insert")
    void technicalColumnsAreFilled() {
        Teacher loaded = teacherRepository.findById(withPrimary.getId()).orElseThrow();

        assertThat(loaded.getCreatedAt()).isNotNull();
        assertThat(loaded.getUpdatedAt()).isNotNull();
        assertThat(loaded.getVersion()).isZero();
    }

    private Map<Long, TeacherSummary> ownSummaries(List<TeacherSummary> summaries) {
        Set<Long> ownIds = Set.of(withPrimary.getId(), withSecondaryOnly.getId(), withoutEmployment.getId());
        return summaries.stream()
                .filter(summary -> ownIds.contains(summary.id()))
                .collect(Collectors.toMap(TeacherSummary::id, Function.identity()));
    }

    private Statistics statistics() {
        return entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }
}

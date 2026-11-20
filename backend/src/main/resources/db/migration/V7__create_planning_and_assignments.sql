-- SF-15: course offerings, class groups and teaching assignments.

CREATE TABLE course_offerings (
    id           bigint GENERATED ALWAYS AS IDENTITY,
    course_id    bigint      NOT NULL,
    semester_id  bigint      NOT NULL,
    status       varchar(20) NOT NULL DEFAULT 'PLANNED',
    created_at   timestamptz NOT NULL DEFAULT now(),
    updated_at   timestamptz NOT NULL DEFAULT now(),
    version      bigint      NOT NULL DEFAULT 0,

    CONSTRAINT pk_course_offerings PRIMARY KEY (id),
    CONSTRAINT fk_course_offerings_course
        FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE RESTRICT,
    CONSTRAINT fk_course_offerings_semester
        FOREIGN KEY (semester_id) REFERENCES semesters (id) ON DELETE RESTRICT,
    -- Also serves as the index on course_id.
    CONSTRAINT uq_course_offerings_course_semester UNIQUE (course_id, semester_id),
    CONSTRAINT ck_course_offerings_status CHECK (status IN ('PLANNED', 'OPEN', 'CLOSED'))
);

CREATE INDEX ix_course_offerings_semester_id ON course_offerings (semester_id);

-- One class type in an offered course; the unit teachers are assigned to.
-- hours defaults to course_class_hours in the service but may differ per group.
CREATE TABLE class_groups (
    id             bigint GENERATED ALWAYS AS IDENTITY,
    offering_id    bigint       NOT NULL,
    class_type_id  bigint       NOT NULL,
    group_code     varchar(30)  NOT NULL,
    hours          numeric(6,2) NOT NULL,
    student_count  int,
    usos_id        varchar(50),
    created_at     timestamptz  NOT NULL DEFAULT now(),
    updated_at     timestamptz  NOT NULL DEFAULT now(),
    version        bigint       NOT NULL DEFAULT 0,

    CONSTRAINT pk_class_groups PRIMARY KEY (id),
    CONSTRAINT fk_class_groups_offering
        FOREIGN KEY (offering_id) REFERENCES course_offerings (id) ON DELETE RESTRICT,
    CONSTRAINT fk_class_groups_class_type
        FOREIGN KEY (class_type_id) REFERENCES class_types (id) ON DELETE RESTRICT,
    -- Also serves as the index on offering_id.
    CONSTRAINT uq_class_groups_offering_type_code UNIQUE (offering_id, class_type_id, group_code),
    CONSTRAINT uq_class_groups_usos_id UNIQUE (usos_id),
    CONSTRAINT ck_class_groups_hours CHECK (hours > 0),
    CONSTRAINT ck_class_groups_student_count CHECK (student_count IS NULL OR student_count >= 0)
);

CREATE INDEX ix_class_groups_class_type_id ON class_groups (class_type_id);

-- Teacher assigned to a group with a percentage share. Assigned hours are not
-- stored: hours x share_percent / 100 (D4). The sum of shares of approved
-- assignments in a group (= 100) is validated in the service layer.
CREATE TABLE teaching_assignments (
    id              bigint GENERATED ALWAYS AS IDENTITY,
    class_group_id  bigint       NOT NULL,
    teacher_id      bigint       NOT NULL,
    share_percent   numeric(5,2) NOT NULL,
    status          varchar(20)  NOT NULL DEFAULT 'DRAFT',
    created_at      timestamptz  NOT NULL DEFAULT now(),
    updated_at      timestamptz  NOT NULL DEFAULT now(),
    version         bigint       NOT NULL DEFAULT 0,

    CONSTRAINT pk_teaching_assignments PRIMARY KEY (id),
    CONSTRAINT fk_teaching_assignments_class_group
        FOREIGN KEY (class_group_id) REFERENCES class_groups (id) ON DELETE RESTRICT,
    CONSTRAINT fk_teaching_assignments_teacher
        FOREIGN KEY (teacher_id) REFERENCES teachers (id) ON DELETE RESTRICT,
    -- Target of the composite foreign key in assignment_change_requests (Z4).
    CONSTRAINT uq_teaching_assignments_id_group UNIQUE (id, class_group_id),
    CONSTRAINT ck_teaching_assignments_share CHECK (share_percent > 0 AND share_percent <= 100),
    CONSTRAINT ck_teaching_assignments_status CHECK (status IN ('DRAFT', 'APPROVED', 'CANCELLED'))
);

-- One active assignment per teacher and group; a cancelled one does not block
-- assigning the same teacher again (Z3).
CREATE UNIQUE INDEX ux_teaching_assignments_active
    ON teaching_assignments (class_group_id, teacher_id)
    WHERE status <> 'CANCELLED';
-- The partial index skips cancelled rows, so foreign key lookups need a full one.
CREATE INDEX ix_teaching_assignments_class_group_id ON teaching_assignments (class_group_id);
CREATE INDEX ix_teaching_assignments_teacher_id ON teaching_assignments (teacher_id);

-- SF-15: study programs, course catalogue, class types and planned hours.

CREATE TABLE study_programs (
    id          bigint GENERATED ALWAYS AS IDENTITY,
    unit_id     bigint       NOT NULL,
    code        varchar(50)  NOT NULL,
    name        varchar(255) NOT NULL,
    level       varchar(20)  NOT NULL,
    usos_id     varchar(50),
    active      boolean      NOT NULL DEFAULT true,
    created_at  timestamptz  NOT NULL DEFAULT now(),
    updated_at  timestamptz  NOT NULL DEFAULT now(),
    version     bigint       NOT NULL DEFAULT 0,

    CONSTRAINT pk_study_programs PRIMARY KEY (id),
    CONSTRAINT fk_study_programs_unit
        FOREIGN KEY (unit_id) REFERENCES organizational_units (id) ON DELETE RESTRICT,
    CONSTRAINT uq_study_programs_code UNIQUE (code),
    CONSTRAINT uq_study_programs_usos_id UNIQUE (usos_id),
    CONSTRAINT ck_study_programs_level
        CHECK (level IN ('BACHELOR', 'MASTER', 'LONG_CYCLE', 'POSTGRADUATE'))
);

CREATE INDEX ix_study_programs_unit_id ON study_programs (unit_id);

-- Class types with the weight converting class hours into pensum hours.
-- Weights are constant (SF-13, simplification U1).
CREATE TABLE class_types (
    id           bigint GENERATED ALWAYS AS IDENTITY,
    code         varchar(30)  NOT NULL,
    name         varchar(100) NOT NULL,
    hour_weight  numeric(4,2) NOT NULL DEFAULT 1.00,
    created_at   timestamptz  NOT NULL DEFAULT now(),
    updated_at   timestamptz  NOT NULL DEFAULT now(),
    version      bigint       NOT NULL DEFAULT 0,

    CONSTRAINT pk_class_types PRIMARY KEY (id),
    CONSTRAINT uq_class_types_code UNIQUE (code),
    CONSTRAINT ck_class_types_hour_weight CHECK (hour_weight > 0)
);

-- Course catalogue, independent of semesters. ECTS is assumed to be the same
-- in every study program (SF-13, Q14).
CREATE TABLE courses (
    id             bigint GENERATED ALWAYS AS IDENTITY,
    unit_id        bigint       NOT NULL,
    min_degree_id  bigint,
    code           varchar(50)  NOT NULL,
    name           varchar(255) NOT NULL,
    name_en        varchar(255),
    ects           numeric(4,1) NOT NULL,
    usos_id        varchar(50),
    active         boolean      NOT NULL DEFAULT true,
    created_at     timestamptz  NOT NULL DEFAULT now(),
    updated_at     timestamptz  NOT NULL DEFAULT now(),
    version        bigint       NOT NULL DEFAULT 0,

    CONSTRAINT pk_courses PRIMARY KEY (id),
    CONSTRAINT fk_courses_unit
        FOREIGN KEY (unit_id) REFERENCES organizational_units (id) ON DELETE RESTRICT,
    CONSTRAINT fk_courses_min_degree
        FOREIGN KEY (min_degree_id) REFERENCES academic_degrees (id) ON DELETE RESTRICT,
    CONSTRAINT uq_courses_code UNIQUE (code),
    CONSTRAINT uq_courses_usos_id UNIQUE (usos_id),
    CONSTRAINT ck_courses_ects CHECK (ects >= 0)
);

CREATE INDEX ix_courses_unit_id ON courses (unit_id);
CREATE INDEX ix_courses_min_degree_id ON courses (min_degree_id);

-- M:N courses x study programs.
CREATE TABLE course_programs (
    course_id         bigint   NOT NULL,
    study_program_id  bigint   NOT NULL,
    semester_no       smallint NOT NULL,
    is_mandatory      boolean  NOT NULL DEFAULT true,

    CONSTRAINT pk_course_programs PRIMARY KEY (course_id, study_program_id),
    CONSTRAINT fk_course_programs_course
        FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE RESTRICT,
    CONSTRAINT fk_course_programs_study_program
        FOREIGN KEY (study_program_id) REFERENCES study_programs (id) ON DELETE RESTRICT,
    CONSTRAINT ck_course_programs_semester_no CHECK (semester_no BETWEEN 1 AND 12)
);

CREATE INDEX ix_course_programs_study_program_id ON course_programs (study_program_id);

-- Specializations required from the teacher of a course (M:N).
CREATE TABLE course_specializations (
    course_id          bigint NOT NULL,
    specialization_id  bigint NOT NULL,

    CONSTRAINT pk_course_specializations PRIMARY KEY (course_id, specialization_id),
    CONSTRAINT fk_course_specializations_course
        FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE RESTRICT,
    CONSTRAINT fk_course_specializations_specialization
        FOREIGN KEY (specialization_id) REFERENCES specializations (id) ON DELETE RESTRICT
);

CREATE INDEX ix_course_specializations_specialization_id
    ON course_specializations (specialization_id);

-- Planned hours of a course per class type (one row per type instead of
-- lecture_hours / lab_hours columns).
CREATE TABLE course_class_hours (
    id             bigint GENERATED ALWAYS AS IDENTITY,
    course_id      bigint       NOT NULL,
    class_type_id  bigint       NOT NULL,
    hours          numeric(6,2) NOT NULL,
    created_at     timestamptz  NOT NULL DEFAULT now(),
    updated_at     timestamptz  NOT NULL DEFAULT now(),
    version        bigint       NOT NULL DEFAULT 0,

    CONSTRAINT pk_course_class_hours PRIMARY KEY (id),
    CONSTRAINT fk_course_class_hours_course
        FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE RESTRICT,
    CONSTRAINT fk_course_class_hours_class_type
        FOREIGN KEY (class_type_id) REFERENCES class_types (id) ON DELETE RESTRICT,
    -- Also serves as the index on course_id.
    CONSTRAINT uq_course_class_hours_course_type UNIQUE (course_id, class_type_id),
    CONSTRAINT ck_course_class_hours_hours CHECK (hours > 0)
);

CREATE INDEX ix_course_class_hours_class_type_id ON course_class_hours (class_type_id);

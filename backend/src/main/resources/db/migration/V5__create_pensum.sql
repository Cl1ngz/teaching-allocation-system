-- SF-15: teaching load rules (pensum), additional duties and the yearly
-- pensum snapshot.

-- Annual pensum for a full-time position, depending on the position, the staff
-- group (SF-14, Z8) and the employment type, versioned in time.
-- Overlapping validity periods of the same combination are validated in the
-- service layer (SF-13, simplification U2).
CREATE TABLE pensum_rules (
    id               bigint GENERATED ALWAYS AS IDENTITY,
    position_id      bigint       NOT NULL,
    employee_group   varchar(30)  NOT NULL,
    employment_type  varchar(30)  NOT NULL,
    annual_hours     numeric(6,2) NOT NULL,
    valid_from       date         NOT NULL,
    valid_to         date,
    created_at       timestamptz  NOT NULL DEFAULT now(),
    updated_at       timestamptz  NOT NULL DEFAULT now(),
    version          bigint       NOT NULL DEFAULT 0,

    CONSTRAINT pk_pensum_rules PRIMARY KEY (id),
    CONSTRAINT fk_pensum_rules_position
        FOREIGN KEY (position_id) REFERENCES positions (id) ON DELETE RESTRICT,
    -- Also serves as the index on position_id.
    CONSTRAINT uq_pensum_rules_rule
        UNIQUE (position_id, employee_group, employment_type, valid_from),
    CONSTRAINT ck_pensum_rules_employee_group
        CHECK (employee_group IN ('RESEARCH_TEACHING', 'TEACHING', 'RESEARCH')),
    CONSTRAINT ck_pensum_rules_employment_type
        CHECK (employment_type IN ('PRIMARY', 'SECONDARY')),
    CONSTRAINT ck_pensum_rules_annual_hours CHECK (annual_hours >= 0),
    CONSTRAINT ck_pensum_rules_period CHECK (valid_to IS NULL OR valid_to >= valid_from)
);

CREATE TABLE duty_types (
    id                       bigint GENERATED ALWAYS AS IDENTITY,
    code                     varchar(50)  NOT NULL,
    name                     varchar(150) NOT NULL,
    category                 varchar(30)  NOT NULL,
    default_reduction_hours  numeric(6,2),
    created_at               timestamptz  NOT NULL DEFAULT now(),
    updated_at               timestamptz  NOT NULL DEFAULT now(),
    version                  bigint       NOT NULL DEFAULT 0,

    CONSTRAINT pk_duty_types PRIMARY KEY (id),
    CONSTRAINT uq_duty_types_code UNIQUE (code),
    CONSTRAINT ck_duty_types_category
        CHECK (category IN ('ADMINISTRATIVE', 'RESEARCH_PROJECT', 'OTHER')),
    CONSTRAINT ck_duty_types_default_reduction
        CHECK (default_reduction_hours IS NULL OR default_reduction_hours >= 0)
);

-- reduction_hours is copied from duty_types.default_reduction_hours and may be
-- changed, so it is an independent fact (SF-13 analysis).
CREATE TABLE teacher_duties (
    id                bigint GENERATED ALWAYS AS IDENTITY,
    teacher_id        bigint       NOT NULL,
    academic_year_id  bigint       NOT NULL,
    duty_type_id      bigint       NOT NULL,
    reduction_hours   numeric(6,2) NOT NULL,
    note              text,
    created_at        timestamptz  NOT NULL DEFAULT now(),
    updated_at        timestamptz  NOT NULL DEFAULT now(),
    version           bigint       NOT NULL DEFAULT 0,

    CONSTRAINT pk_teacher_duties PRIMARY KEY (id),
    CONSTRAINT fk_teacher_duties_teacher
        FOREIGN KEY (teacher_id) REFERENCES teachers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_teacher_duties_academic_year
        FOREIGN KEY (academic_year_id) REFERENCES academic_years (id) ON DELETE RESTRICT,
    CONSTRAINT fk_teacher_duties_duty_type
        FOREIGN KEY (duty_type_id) REFERENCES duty_types (id) ON DELETE RESTRICT,
    CONSTRAINT ck_teacher_duties_reduction_hours CHECK (reduction_hours >= 0)
);

-- (teacher_id, academic_year_id) serves the pensum calculation and every lookup
-- by teacher_id alone.
CREATE INDEX ix_teacher_duties_teacher_id_year ON teacher_duties (teacher_id, academic_year_id);
CREATE INDEX ix_teacher_duties_academic_year_id ON teacher_duties (academic_year_id);
CREATE INDEX ix_teacher_duties_duty_type_id ON teacher_duties (duty_type_id);

-- Yearly pensum snapshot (SF-13, deviation O1): base_hours and reduction_hours
-- are written by the service; effective_hours is computed by the database (Z2)
-- and is read-only in JPA.
CREATE TABLE teacher_pensums (
    id                bigint GENERATED ALWAYS AS IDENTITY,
    teacher_id        bigint       NOT NULL,
    academic_year_id  bigint       NOT NULL,
    base_hours        numeric(6,2) NOT NULL,
    reduction_hours   numeric(6,2) NOT NULL,
    effective_hours   numeric(6,2) GENERATED ALWAYS AS (base_hours - reduction_hours) STORED,
    calculated_at     timestamptz  NOT NULL,
    created_at        timestamptz  NOT NULL DEFAULT now(),
    updated_at        timestamptz  NOT NULL DEFAULT now(),
    version           bigint       NOT NULL DEFAULT 0,

    CONSTRAINT pk_teacher_pensums PRIMARY KEY (id),
    CONSTRAINT fk_teacher_pensums_teacher
        FOREIGN KEY (teacher_id) REFERENCES teachers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_teacher_pensums_academic_year
        FOREIGN KEY (academic_year_id) REFERENCES academic_years (id) ON DELETE RESTRICT,
    -- Also serves as the index on teacher_id.
    CONSTRAINT uq_teacher_pensums_teacher_year UNIQUE (teacher_id, academic_year_id),
    CONSTRAINT ck_teacher_pensums_hours CHECK (base_hours >= 0 AND reduction_hours >= 0)
);

CREATE INDEX ix_teacher_pensums_academic_year_id ON teacher_pensums (academic_year_id);

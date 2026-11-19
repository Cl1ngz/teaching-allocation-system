-- SF-14: organizational structure and staff.
-- Tables: organizational_units, academic_degrees, positions, teachers, employments.
-- Source: "Model danych" (Confluence), approved in SF-13.
--
-- Conventions:
--   * surrogate keys: bigint GENERATED ALWAYS AS IDENTITY,
--   * foreign keys: ON DELETE RESTRICT (records are deactivated, not deleted),
--   * fixed dictionaries: varchar + CHECK (Java enums with EnumType.STRING),
--   * technical columns: created_at, updated_at (set by Hibernate; DEFAULT now()
--     covers rows inserted outside the application) and version (@Version).

-- ---------------------------------------------------------------------------
-- Organizational units: faculty -> department -> division (self-referencing tree)
-- ---------------------------------------------------------------------------
CREATE TABLE organizational_units (
    id          bigint GENERATED ALWAYS AS IDENTITY,
    parent_id   bigint,
    unit_type   varchar(20)  NOT NULL,
    name        varchar(255) NOT NULL,
    short_name  varchar(50),
    usos_id     varchar(50),
    active      boolean      NOT NULL DEFAULT true,
    created_at  timestamptz  NOT NULL DEFAULT now(),
    updated_at  timestamptz  NOT NULL DEFAULT now(),
    version     bigint       NOT NULL DEFAULT 0,

    CONSTRAINT pk_organizational_units PRIMARY KEY (id),
    CONSTRAINT fk_organizational_units_parent
        FOREIGN KEY (parent_id) REFERENCES organizational_units (id) ON DELETE RESTRICT,
    CONSTRAINT uq_organizational_units_usos_id UNIQUE (usos_id),
    CONSTRAINT ck_organizational_units_unit_type
        CHECK (unit_type IN ('FACULTY', 'DEPARTMENT', 'DIVISION')),
    -- A faculty is a root; departments and divisions always have a parent.
    -- The parent's type (DEPARTMENT -> FACULTY, DIVISION -> DEPARTMENT) and the
    -- absence of longer cycles are validated in the service layer.
    CONSTRAINT ck_organizational_units_parent
        CHECK ((unit_type = 'FACULTY') = (parent_id IS NULL)),
    CONSTRAINT ck_organizational_units_not_own_parent
        CHECK (parent_id <> id)
);

CREATE INDEX ix_organizational_units_parent_id ON organizational_units (parent_id);

-- ---------------------------------------------------------------------------
-- Academic degrees and titles (dictionary, compared by rank_order)
-- ---------------------------------------------------------------------------
CREATE TABLE academic_degrees (
    id          bigint GENERATED ALWAYS AS IDENTITY,
    code        varchar(30)  NOT NULL,
    name        varchar(100) NOT NULL,
    rank_order  int          NOT NULL,
    created_at  timestamptz  NOT NULL DEFAULT now(),
    updated_at  timestamptz  NOT NULL DEFAULT now(),
    version     bigint       NOT NULL DEFAULT 0,

    CONSTRAINT pk_academic_degrees PRIMARY KEY (id),
    CONSTRAINT uq_academic_degrees_code UNIQUE (code),
    CONSTRAINT uq_academic_degrees_rank_order UNIQUE (rank_order)
);

-- ---------------------------------------------------------------------------
-- Positions (dictionary). The staff group (research-teaching, teaching,
-- research) is not a property of the position but of the employment, see
-- employments.employee_group.
-- ---------------------------------------------------------------------------
CREATE TABLE positions (
    id          bigint GENERATED ALWAYS AS IDENTITY,
    code        varchar(30)  NOT NULL,
    name        varchar(100) NOT NULL,
    created_at  timestamptz  NOT NULL DEFAULT now(),
    updated_at  timestamptz  NOT NULL DEFAULT now(),
    version     bigint       NOT NULL DEFAULT 0,

    CONSTRAINT pk_positions PRIMARY KEY (id),
    CONSTRAINT uq_positions_code UNIQUE (code)
);

-- ---------------------------------------------------------------------------
-- Teachers. Membership in units is stored in employments, not here.
-- ---------------------------------------------------------------------------
CREATE TABLE teachers (
    id                  bigint GENERATED ALWAYS AS IDENTITY,
    academic_degree_id  bigint       NOT NULL,
    first_name          varchar(100) NOT NULL,
    last_name           varchar(100) NOT NULL,
    email               varchar(255) NOT NULL,
    usos_id             varchar(50),
    active              boolean      NOT NULL DEFAULT true,
    created_at          timestamptz  NOT NULL DEFAULT now(),
    updated_at          timestamptz  NOT NULL DEFAULT now(),
    version             bigint       NOT NULL DEFAULT 0,

    CONSTRAINT pk_teachers PRIMARY KEY (id),
    CONSTRAINT fk_teachers_academic_degree
        FOREIGN KEY (academic_degree_id) REFERENCES academic_degrees (id) ON DELETE RESTRICT,
    CONSTRAINT uq_teachers_email UNIQUE (email),
    CONSTRAINT uq_teachers_usos_id UNIQUE (usos_id)
);

CREATE INDEX ix_teachers_academic_degree_id ON teachers (academic_degree_id);

-- ---------------------------------------------------------------------------
-- Employments: teacher x unit x position x staff group x employment type x FTE
-- over a period of time.
-- ---------------------------------------------------------------------------
CREATE TABLE employments (
    id               bigint GENERATED ALWAYS AS IDENTITY,
    teacher_id       bigint       NOT NULL,
    unit_id          bigint       NOT NULL,
    position_id      bigint       NOT NULL,
    employee_group   varchar(30)  NOT NULL,
    employment_type  varchar(30)  NOT NULL,
    fte              numeric(3,2) NOT NULL,
    valid_from       date         NOT NULL,
    valid_to         date,
    created_at       timestamptz  NOT NULL DEFAULT now(),
    updated_at       timestamptz  NOT NULL DEFAULT now(),
    version          bigint       NOT NULL DEFAULT 0,

    CONSTRAINT pk_employments PRIMARY KEY (id),
    CONSTRAINT fk_employments_teacher
        FOREIGN KEY (teacher_id) REFERENCES teachers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_employments_unit
        FOREIGN KEY (unit_id) REFERENCES organizational_units (id) ON DELETE RESTRICT,
    CONSTRAINT fk_employments_position
        FOREIGN KEY (position_id) REFERENCES positions (id) ON DELETE RESTRICT,
    CONSTRAINT ck_employments_employee_group
        CHECK (employee_group IN ('RESEARCH_TEACHING', 'TEACHING', 'RESEARCH')),
    CONSTRAINT ck_employments_employment_type
        CHECK (employment_type IN ('PRIMARY', 'SECONDARY')),
    CONSTRAINT ck_employments_fte
        CHECK (fte > 0 AND fte <= 1),
    CONSTRAINT ck_employments_period
        CHECK (valid_to IS NULL OR valid_to >= valid_from)
    -- At most one PRIMARY employment per teacher in a given period:
    -- validated in the service layer (SF-13, simplification U2).
);

-- (teacher_id, valid_to) serves "current employments of a teacher" and also
-- every lookup by teacher_id alone, so no separate teacher_id index is needed.
CREATE INDEX ix_employments_teacher_id_valid_to ON employments (teacher_id, valid_to);
CREATE INDEX ix_employments_unit_id ON employments (unit_id);
CREATE INDEX ix_employments_position_id ON employments (position_id);

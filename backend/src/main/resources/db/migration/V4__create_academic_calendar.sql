-- SF-15: academic years and semesters.
-- The current year is not stored (SF-13, Z1): it is the year for which
-- CURRENT_DATE BETWEEN start_date AND end_date.

CREATE TABLE academic_years (
    id          bigint GENERATED ALWAYS AS IDENTITY,
    label       varchar(9)  NOT NULL,
    start_date  date        NOT NULL,
    end_date    date        NOT NULL,
    created_at  timestamptz NOT NULL DEFAULT now(),
    updated_at  timestamptz NOT NULL DEFAULT now(),
    version     bigint      NOT NULL DEFAULT 0,

    CONSTRAINT pk_academic_years PRIMARY KEY (id),
    CONSTRAINT uq_academic_years_label UNIQUE (label),
    CONSTRAINT ck_academic_years_period CHECK (end_date > start_date)
);

CREATE TABLE semesters (
    id                bigint GENERATED ALWAYS AS IDENTITY,
    academic_year_id  bigint      NOT NULL,
    term              varchar(10) NOT NULL,
    start_date        date        NOT NULL,
    end_date          date        NOT NULL,
    created_at        timestamptz NOT NULL DEFAULT now(),
    updated_at        timestamptz NOT NULL DEFAULT now(),
    version           bigint      NOT NULL DEFAULT 0,

    CONSTRAINT pk_semesters PRIMARY KEY (id),
    CONSTRAINT fk_semesters_academic_year
        FOREIGN KEY (academic_year_id) REFERENCES academic_years (id) ON DELETE RESTRICT,
    -- Also serves as the index on academic_year_id.
    CONSTRAINT uq_semesters_year_term UNIQUE (academic_year_id, term),
    CONSTRAINT ck_semesters_term CHECK (term IN ('WINTER', 'SUMMER')),
    CONSTRAINT ck_semesters_period CHECK (end_date > start_date)
);

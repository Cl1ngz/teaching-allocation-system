-- SF-15: specializations and teacher competences.
-- Conventions as in V1 (identity keys, ON DELETE RESTRICT, named constraints,
-- technical columns on tables with a surrogate key).

CREATE TABLE specializations (
    id           bigint GENERATED ALWAYS AS IDENTITY,
    code         varchar(50)  NOT NULL,
    name         varchar(150) NOT NULL,
    description  text,
    created_at   timestamptz  NOT NULL DEFAULT now(),
    updated_at   timestamptz  NOT NULL DEFAULT now(),
    version      bigint       NOT NULL DEFAULT 0,

    CONSTRAINT pk_specializations PRIMARY KEY (id),
    CONSTRAINT uq_specializations_code UNIQUE (code)
);

-- M:N teachers x specializations. The primary key index covers lookups by
-- teacher_id, so only specialization_id needs its own index.
CREATE TABLE teacher_specializations (
    teacher_id         bigint   NOT NULL,
    specialization_id  bigint   NOT NULL,
    level              smallint,

    CONSTRAINT pk_teacher_specializations PRIMARY KEY (teacher_id, specialization_id),
    CONSTRAINT fk_teacher_specializations_teacher
        FOREIGN KEY (teacher_id) REFERENCES teachers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_teacher_specializations_specialization
        FOREIGN KEY (specialization_id) REFERENCES specializations (id) ON DELETE RESTRICT,
    CONSTRAINT ck_teacher_specializations_level
        CHECK (level IS NULL OR level BETWEEN 1 AND 3)
);

CREATE INDEX ix_teacher_specializations_specialization_id
    ON teacher_specializations (specialization_id);

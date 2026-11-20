-- SF-15: application users and roles, assignment change requests,
-- substitutions and the audit log.

-- Optionally linked to a teacher (0..1). For linked accounts teachers.email is
-- the authoritative address (SF-13, Q15), so email here is optional.
CREATE TABLE app_users (
    id          bigint GENERATED ALWAYS AS IDENTITY,
    teacher_id  bigint,
    username    varchar(100) NOT NULL,
    email       varchar(255),
    active      boolean      NOT NULL DEFAULT true,
    created_at  timestamptz  NOT NULL DEFAULT now(),
    updated_at  timestamptz  NOT NULL DEFAULT now(),
    version     bigint       NOT NULL DEFAULT 0,

    CONSTRAINT pk_app_users PRIMARY KEY (id),
    CONSTRAINT fk_app_users_teacher
        FOREIGN KEY (teacher_id) REFERENCES teachers (id) ON DELETE RESTRICT,
    -- Also serves as the index on teacher_id.
    CONSTRAINT uq_app_users_teacher_id UNIQUE (teacher_id),
    CONSTRAINT uq_app_users_username UNIQUE (username)
);

-- Role with an optional unit scope; scope_unit_id NULL = global.
-- NULLS NOT DISTINCT (PostgreSQL 15+) makes two global copies of the same role
-- a duplicate.
CREATE TABLE user_roles (
    id             bigint GENERATED ALWAYS AS IDENTITY,
    user_id        bigint      NOT NULL,
    role           varchar(30) NOT NULL,
    scope_unit_id  bigint,
    created_at     timestamptz NOT NULL DEFAULT now(),
    updated_at     timestamptz NOT NULL DEFAULT now(),
    version        bigint      NOT NULL DEFAULT 0,

    CONSTRAINT pk_user_roles PRIMARY KEY (id),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_user_roles_scope_unit
        FOREIGN KEY (scope_unit_id) REFERENCES organizational_units (id) ON DELETE RESTRICT,
    -- Also serves as the index on user_id.
    CONSTRAINT uq_user_roles_user_role_scope
        UNIQUE NULLS NOT DISTINCT (user_id, role, scope_unit_id),
    CONSTRAINT ck_user_roles_role
        CHECK (role IN ('TEACHER', 'DEPARTMENT_HEAD', 'DEAN_OFFICE', 'ADMIN', 'FINANCE'))
);

CREATE INDEX ix_user_roles_scope_unit_id ON user_roles (scope_unit_id);

-- Request to change an assignment, with approval. old_* is a copy of the
-- assignment at request time (SF-13, deviation O2).
CREATE TABLE assignment_change_requests (
    id                 bigint GENERATED ALWAYS AS IDENTITY,
    class_group_id     bigint       NOT NULL,
    assignment_id      bigint,
    change_type        varchar(30)  NOT NULL,
    old_teacher_id     bigint,
    new_teacher_id     bigint,
    old_share_percent  numeric(5,2),
    new_share_percent  numeric(5,2),
    reason             text,
    status             varchar(20)  NOT NULL DEFAULT 'SUBMITTED',
    requested_by       bigint       NOT NULL,
    requested_at       timestamptz  NOT NULL DEFAULT now(),
    decided_by         bigint,
    decided_at         timestamptz,
    decision_comment   text,
    created_at         timestamptz  NOT NULL DEFAULT now(),
    updated_at         timestamptz  NOT NULL DEFAULT now(),
    version            bigint       NOT NULL DEFAULT 0,

    CONSTRAINT pk_assignment_change_requests PRIMARY KEY (id),
    CONSTRAINT fk_acr_class_group
        FOREIGN KEY (class_group_id) REFERENCES class_groups (id) ON DELETE RESTRICT,
    CONSTRAINT fk_acr_assignment
        FOREIGN KEY (assignment_id) REFERENCES teaching_assignments (id) ON DELETE RESTRICT,
    -- The group must be the group of the assignment (Z4). With MATCH SIMPLE it
    -- is not checked when assignment_id is NULL (change type ADD).
    CONSTRAINT fk_acr_assignment_group
        FOREIGN KEY (assignment_id, class_group_id)
        REFERENCES teaching_assignments (id, class_group_id) ON DELETE RESTRICT,
    CONSTRAINT fk_acr_old_teacher
        FOREIGN KEY (old_teacher_id) REFERENCES teachers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_acr_new_teacher
        FOREIGN KEY (new_teacher_id) REFERENCES teachers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_acr_requested_by
        FOREIGN KEY (requested_by) REFERENCES app_users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_acr_decided_by
        FOREIGN KEY (decided_by) REFERENCES app_users (id) ON DELETE RESTRICT,
    CONSTRAINT ck_acr_change_type
        CHECK (change_type IN ('ADD', 'REMOVE', 'CHANGE_SHARE', 'REPLACE_TEACHER')),
    CONSTRAINT ck_acr_status
        CHECK (status IN ('SUBMITTED', 'APPROVED', 'REJECTED')),
    CONSTRAINT ck_acr_new_share
        CHECK (new_share_percent IS NULL OR (new_share_percent > 0 AND new_share_percent <= 100)),
    -- Required fields for each change type (SF-13, Q13).
    CONSTRAINT ck_acr_required_fields CHECK (
           (change_type = 'ADD'             AND assignment_id IS NULL     AND new_teacher_id IS NOT NULL AND new_share_percent IS NOT NULL)
        OR (change_type = 'REMOVE'          AND assignment_id IS NOT NULL)
        OR (change_type = 'CHANGE_SHARE'    AND assignment_id IS NOT NULL AND new_share_percent IS NOT NULL)
        OR (change_type = 'REPLACE_TEACHER' AND assignment_id IS NOT NULL AND new_teacher_id IS NOT NULL)
    )
);

CREATE INDEX ix_acr_class_group_id ON assignment_change_requests (class_group_id);
CREATE INDEX ix_acr_assignment_id ON assignment_change_requests (assignment_id);
CREATE INDEX ix_acr_old_teacher_id ON assignment_change_requests (old_teacher_id);
CREATE INDEX ix_acr_new_teacher_id ON assignment_change_requests (new_teacher_id);
CREATE INDEX ix_acr_requested_by ON assignment_change_requests (requested_by);
CREATE INDEX ix_acr_decided_by ON assignment_change_requests (decided_by);

-- Temporary substitution: hours taken over from an assignment (Z5). The
-- assignment determines the group and the substituted teacher. "Substitute
-- differs from the assignment's teacher" is validated in the service layer.
CREATE TABLE substitutions (
    id                     bigint GENERATED ALWAYS AS IDENTITY,
    assignment_id          bigint       NOT NULL,
    substitute_teacher_id  bigint       NOT NULL,
    date_from              date         NOT NULL,
    date_to                date         NOT NULL,
    hours                  numeric(6,2) NOT NULL,
    reason                 text,
    status                 varchar(20)  NOT NULL DEFAULT 'SUBMITTED',
    requested_by           bigint       NOT NULL,
    requested_at           timestamptz  NOT NULL DEFAULT now(),
    decided_by             bigint,
    decided_at             timestamptz,
    created_at             timestamptz  NOT NULL DEFAULT now(),
    updated_at             timestamptz  NOT NULL DEFAULT now(),
    version                bigint       NOT NULL DEFAULT 0,

    CONSTRAINT pk_substitutions PRIMARY KEY (id),
    CONSTRAINT fk_substitutions_assignment
        FOREIGN KEY (assignment_id) REFERENCES teaching_assignments (id) ON DELETE RESTRICT,
    CONSTRAINT fk_substitutions_substitute_teacher
        FOREIGN KEY (substitute_teacher_id) REFERENCES teachers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_substitutions_requested_by
        FOREIGN KEY (requested_by) REFERENCES app_users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_substitutions_decided_by
        FOREIGN KEY (decided_by) REFERENCES app_users (id) ON DELETE RESTRICT,
    CONSTRAINT ck_substitutions_period CHECK (date_from <= date_to),
    CONSTRAINT ck_substitutions_hours CHECK (hours > 0),
    CONSTRAINT ck_substitutions_status CHECK (status IN ('SUBMITTED', 'APPROVED', 'REJECTED'))
);

CREATE INDEX ix_substitutions_assignment_id ON substitutions (assignment_id);
CREATE INDEX ix_substitutions_substitute_teacher_id ON substitutions (substitute_teacher_id);
CREATE INDEX ix_substitutions_requested_by ON substitutions (requested_by);
CREATE INDEX ix_substitutions_decided_by ON substitutions (decided_by);

-- Append-only change log (SF-13, deviation O3): entity_id has no foreign key
-- and the values are jsonb. No updated_at / version, rows are never modified.
-- Whether this table or Hibernate Envers is used is decided with the JPA
-- entities (Q10).
CREATE TABLE audit_log (
    id           bigint GENERATED ALWAYS AS IDENTITY,
    entity_type  varchar(100) NOT NULL,
    entity_id    bigint       NOT NULL,
    action       varchar(20)  NOT NULL,
    changed_by   bigint,
    changed_at   timestamptz  NOT NULL DEFAULT now(),
    old_values   jsonb,
    new_values   jsonb,

    CONSTRAINT pk_audit_log PRIMARY KEY (id),
    CONSTRAINT fk_audit_log_changed_by
        FOREIGN KEY (changed_by) REFERENCES app_users (id) ON DELETE RESTRICT
);

CREATE INDEX ix_audit_log_entity ON audit_log (entity_type, entity_id);
CREATE INDEX ix_audit_log_changed_by ON audit_log (changed_by);

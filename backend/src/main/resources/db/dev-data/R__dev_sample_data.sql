-- SF-16: sample data for local development ("dev" profile only).
--
-- All people are fictional; e-mails use the reserved .example domain.
-- Every sample unit and teacher has usos_id starting with "DEV-", which is how
-- the cleanup script (backend/docs/dev-sample-data-cleanup.sql) finds them.
--
-- This is a Flyway repeatable migration: it runs after the versioned ones and
-- again only when this file changes. Every insert skips rows that already
-- exist, so a re-run never duplicates data.
--
-- Edge cases for the teacher list:
--   DEV-T10  two current employments (PRIMARY + SECONDARY in another unit)
--   DEV-T11  position change: ended ASYSTENT employment, current ADIUNKT one
--   DEV-T12  no active employment (the only one ended)
--   DEV-T13  inactive teacher (active = false)
--   DEV-T14  only a SECONDARY, part-time employment
--   DEV-T15  no employment at all
--   DEV-T03 / DEV-T04  same position (ADIUNKT) in different staff groups

-- Units: faculties, then departments, then divisions, so each level can find
-- its parent by usos_id.
INSERT INTO organizational_units (unit_type, name, short_name, usos_id, parent_id)
VALUES
    ('FACULTY', 'Wydział Informatyki', 'WI', 'DEV-W1', NULL),
    ('FACULTY', 'Wydział Elektryczny', 'WE', 'DEV-W2', NULL)
ON CONFLICT (usos_id) DO NOTHING;

INSERT INTO organizational_units (unit_type, name, short_name, usos_id, parent_id)
SELECT 'DEPARTMENT', v.name, v.short_name, v.usos_id, p.id
FROM (VALUES
    ('Katedra Oprogramowania',           'KO',  'DEV-K11', 'DEV-W1'),
    ('Katedra Systemów Informatycznych', 'KSI', 'DEV-K12', 'DEV-W1'),
    ('Katedra Automatyki',               'KA',  'DEV-K21', 'DEV-W2')
) AS v (name, short_name, usos_id, parent_usos_id)
JOIN organizational_units p ON p.usos_id = v.parent_usos_id
ON CONFLICT (usos_id) DO NOTHING;

INSERT INTO organizational_units (unit_type, name, short_name, usos_id, parent_id)
SELECT 'DIVISION', v.name, v.short_name, v.usos_id, p.id
FROM (VALUES
    ('Zakład Inżynierii Oprogramowania', 'ZIO', 'DEV-Z111', 'DEV-K11'),
    ('Zakład Baz Danych',                'ZBD', 'DEV-Z112', 'DEV-K11')
) AS v (name, short_name, usos_id, parent_usos_id)
JOIN organizational_units p ON p.usos_id = v.parent_usos_id
ON CONFLICT (usos_id) DO NOTHING;

-- Teachers (fictional).
INSERT INTO teachers (academic_degree_id, first_name, last_name, email, usos_id, active)
SELECT d.id, v.first_name, v.last_name, v.email, v.usos_id, v.active
FROM (VALUES
    ('PROF',   'Anna',       'Przykładowa',     'anna.przykladowa@tas.example',       'DEV-T01', true),
    ('DR_HAB', 'Jan',        'Testowy',         'jan.testowy@tas.example',            'DEV-T02', true),
    ('DR',     'Katarzyna',  'Wzorcowa',        'katarzyna.wzorcowa@tas.example',     'DEV-T03', true),
    ('DR',     'Piotr',      'Próbny',          'piotr.probny@tas.example',           'DEV-T04', true),
    ('MGR',    'Magdalena',  'Modelowa',        'magdalena.modelowa@tas.example',     'DEV-T05', true),
    ('MGR',    'Tomasz',     'Roboczy',         'tomasz.roboczy@tas.example',         'DEV-T06', true),
    ('DR',     'Ewa',        'Demonstracyjna',  'ewa.demonstracyjna@tas.example',     'DEV-T07', true),
    ('MGR',    'Michał',     'Szkicowy',        'michal.szkicowy@tas.example',        'DEV-T08', true),
    ('DR',     'Joanna',     'Fikcyjna',        'joanna.fikcyjna@tas.example',        'DEV-T09', true),
    ('DR_HAB', 'Paweł',      'Umowny',          'pawel.umowny@tas.example',           'DEV-T10', true),
    ('DR',     'Agnieszka',  'Pomocnicza',      'agnieszka.pomocnicza@tas.example',   'DEV-T11', true),
    ('MGR',    'Krzysztof',  'Zastępczy',       'krzysztof.zastepczy@tas.example',    'DEV-T12', true),
    ('DR',     'Barbara',    'Makietowa',       'barbara.makietowa@tas.example',      'DEV-T13', false),
    ('MGR',    'Marek',      'Ćwiczebny',       'marek.cwiczebny@tas.example',        'DEV-T14', true),
    ('MGR',    'Zofia',      'Szkoleniowa',     'zofia.szkoleniowa@tas.example',      'DEV-T15', true)
) AS v (degree_code, first_name, last_name, email, usos_id, active)
JOIN academic_degrees d ON d.code = v.degree_code
ON CONFLICT (usos_id) DO NOTHING;

-- Employments. They have no natural unique key, so a row is skipped when the
-- same teacher already has the same position in the same unit from the same date.
INSERT INTO employments (teacher_id, unit_id, position_id, employee_group, employment_type, fte, valid_from, valid_to)
SELECT t.id, u.id, p.id, v.employee_group, v.employment_type, v.fte, v.valid_from, v.valid_to
FROM (VALUES
    ('DEV-T01', 'DEV-K11',  'PROFESOR',           'RESEARCH_TEACHING', 'PRIMARY',   1.00::numeric(3,2), DATE '2015-10-01', NULL::date),
    ('DEV-T02', 'DEV-Z111', 'PROFESOR_UCZELNI',   'RESEARCH_TEACHING', 'PRIMARY',   1.00, DATE '2018-10-01', NULL),
    ('DEV-T03', 'DEV-Z112', 'ADIUNKT',            'RESEARCH_TEACHING', 'PRIMARY',   1.00, DATE '2019-10-01', NULL),
    ('DEV-T04', 'DEV-Z112', 'ADIUNKT',            'TEACHING',          'PRIMARY',   1.00, DATE '2021-10-01', NULL),
    ('DEV-T05', 'DEV-K12',  'ASYSTENT',           'RESEARCH_TEACHING', 'PRIMARY',   1.00, DATE '2023-10-01', NULL),
    ('DEV-T06', 'DEV-K12',  'WYKLADOWCA',         'TEACHING',          'PRIMARY',   1.00, DATE '2020-10-01', NULL),
    ('DEV-T07', 'DEV-K21',  'STARSZY_WYKLADOWCA', 'TEACHING',          'PRIMARY',   1.00, DATE '2017-10-01', NULL),
    ('DEV-T08', 'DEV-K21',  'LEKTOR',             'TEACHING',          'PRIMARY',   1.00, DATE '2022-10-01', NULL),
    ('DEV-T09', 'DEV-Z111', 'ADIUNKT',            'RESEARCH',          'PRIMARY',   1.00, DATE '2024-10-01', NULL),
    ('DEV-T10', 'DEV-K11',  'PROFESOR_UCZELNI',   'RESEARCH_TEACHING', 'PRIMARY',   1.00, DATE '2016-10-01', NULL),
    ('DEV-T10', 'DEV-K21',  'ADIUNKT',            'TEACHING',          'SECONDARY', 0.50, DATE '2025-10-01', NULL),
    ('DEV-T11', 'DEV-K12',  'ASYSTENT',           'RESEARCH_TEACHING', 'PRIMARY',   1.00, DATE '2016-10-01', DATE '2020-09-30'),
    ('DEV-T11', 'DEV-K12',  'ADIUNKT',            'RESEARCH_TEACHING', 'PRIMARY',   1.00, DATE '2020-10-01', NULL),
    ('DEV-T12', 'DEV-K12',  'ASYSTENT',           'RESEARCH_TEACHING', 'PRIMARY',   1.00, DATE '2021-10-01', DATE '2025-09-30'),
    ('DEV-T13', 'DEV-Z111', 'ADIUNKT',            'RESEARCH_TEACHING', 'PRIMARY',   1.00, DATE '2018-10-01', DATE '2024-09-30'),
    ('DEV-T14', 'DEV-K21',  'INSTRUKTOR',         'TEACHING',          'SECONDARY', 0.25, DATE '2025-10-01', NULL)
) AS v (teacher_usos_id, unit_usos_id, position_code, employee_group, employment_type, fte, valid_from, valid_to)
JOIN teachers t             ON t.usos_id = v.teacher_usos_id
JOIN organizational_units u ON u.usos_id = v.unit_usos_id
JOIN positions p            ON p.code = v.position_code
WHERE NOT EXISTS (
    SELECT 1
    FROM employments e
    WHERE e.teacher_id = t.id
      AND e.unit_id = u.id
      AND e.position_id = p.id
      AND e.valid_from = v.valid_from
);

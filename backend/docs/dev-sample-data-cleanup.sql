-- Removes the SF-16 sample data (usos_id starting with "DEV-") and lets the
-- "dev" profile load it again on the next start. Dictionaries and the schema stay.
-- Run against the development database only, e.g.:
--   Get-Content backend/docs/dev-sample-data-cleanup.sql | docker exec -i tas-db psql -U tas_user -d tas_db   (PowerShell)
--   psql -U tas_user -h localhost -d tas_db -f backend/docs/dev-sample-data-cleanup.sql                (local PostgreSQL)
--
-- If records created in the application point to sample teachers or units
-- (e.g. assignments), the deletes fail on a foreign key; reset the whole
-- database instead (docker compose down -v).

BEGIN;

DELETE FROM employments
WHERE teacher_id IN (SELECT id FROM teachers WHERE usos_id LIKE 'DEV-%')
   OR unit_id IN (SELECT id FROM organizational_units WHERE usos_id LIKE 'DEV-%');

DELETE FROM teachers WHERE usos_id LIKE 'DEV-%';

-- Children before parents.
DELETE FROM organizational_units WHERE usos_id LIKE 'DEV-%' AND unit_type = 'DIVISION';
DELETE FROM organizational_units WHERE usos_id LIKE 'DEV-%' AND unit_type = 'DEPARTMENT';
DELETE FROM organizational_units WHERE usos_id LIKE 'DEV-%' AND unit_type = 'FACULTY';

-- Without this row Flyway treats the sample data as not loaded yet.
DELETE FROM flyway_schema_history WHERE script = 'R__dev_sample_data.sql';

COMMIT;

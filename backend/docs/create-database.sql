-- Teaching Allocation System - local database setup (SF-5)
-- Run as the PostgreSQL superuser (postgres), e.g.:
--   psql -U postgres -f backend/docs/create-database.sql
--
-- IMPORTANT: change the password below BEFORE running the script and use
-- the same value as SPRING_DATASOURCE_PASSWORD in backend/.env.

CREATE USER tas_user WITH PASSWORD 'tas_password';

CREATE DATABASE tas_db
    OWNER tas_user
    ENCODING 'UTF8'
    TEMPLATE template0;

GRANT ALL PRIVILEGES ON DATABASE tas_db TO tas_user;

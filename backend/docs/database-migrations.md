# Migracje schematu bazy danych (Flyway)

Schemat bazy tworzy i aktualizuje Flyway przy starcie backendu. Hibernate nie zmienia schematu (`spring.jpa.hibernate.ddl-auto=none`), więc każda tabela, kolumna i ograniczenie musi pochodzić z migracji.

> Zakres: działanie migracji, zasady ich pisania i dodawania. Przygotowanie bazy i połączenia opisuje [database-setup.md](database-setup.md). Model danych, z którego wynikają tabele, jest w Confluence („Model danych”).

## 1. Jak to działa

1. Przy starcie aplikacji Flyway łączy się z bazą tym samym połączeniem co aplikacja (`SPRING_DATASOURCE_*`).
2. Odczytuje tabelę `flyway_schema_history`. Przy pierwszym uruchomieniu na pustej bazie sam ją tworzy.
3. Porównuje ją z plikami w `src/main/resources/db/migration` i wykonuje tylko migracje, których jeszcze nie ma w historii, w kolejności wersji.
4. Każda migracja wykonuje się w osobnej transakcji. Jeśli się nie powiedzie, jej zmiany są wycofywane, a aplikacja nie startuje.
5. Po wykonaniu migracji startuje reszta aplikacji (JPA, kontrolery).

Ponowne uruchomienie aplikacji niczego nie zmienia, bo wszystkie migracje są już w historii. Tak samo działa to w Dockerze: przy `docker compose up` backend startuje po bazie i sam tworzy schemat.

Podgląd historii:

```sql
SELECT installed_rank, version, description, success, installed_on
FROM flyway_schema_history
ORDER BY installed_rank;
```

## 2. Aktualne migracje

| Wersja | Plik | Zawartość | Zadanie |
|--------|------|-----------|---------|
| 1 | `V1__create_organization_and_staff.sql` | Tabele `organizational_units`, `academic_degrees`, `positions`, `teachers`, `employments` z kluczami, ograniczeniami i indeksami | SF-14 |
| 2 | `V2__seed_dictionaries.sql` | Startowe wartości słowników: stopnie naukowe i stanowiska | SF-14 |

## 3. Jak dodać nową migrację

1. Utwórz plik w `backend/src/main/resources/db/migration` o nazwie `V<numer>__<opis>.sql`:
   - `<numer>` to kolejna liczba całkowita (po `V2` jest `V3`),
   - dwa podkreślniki oddzielają numer od opisu,
   - opis po angielsku, małymi literami, słowa oddzielone `_`, np. `V3__create_pensum_tables.sql`.
2. Napisz w nim SQL dla PostgreSQL według konwencji z sekcji 4.
3. Uruchom backend lokalnie. W logach powinien pojawić się wpis `Migrating schema "public" to version "3 - create pensum tables"`.
4. Dodaj plik do commita razem z kodem, który z niego korzysta.

**Numery przy pracy równoległej.** Jeśli dwie osoby jednocześnie piszą migracje, ustalcie wcześniej, kto bierze który numer, i wpiszcie go w opis zadania w Jirze. Dwa pliki z tym samym numerem zatrzymają start aplikacji (`Found more than one migration with version 3`).

## 4. Zasady

**Nigdy nie edytuj migracji, która trafiła na `dev`.** Flyway zapisuje sumę kontrolną każdego pliku. Zmieniony plik powoduje błąd `Migration checksum mismatch` u każdego, kto już go wykonał. Poprawkę zawsze robi się nową migracją (np. `ALTER TABLE ...`). Migrację, która jest tylko na Twojej gałęzi, możesz zmieniać, ale wtedy wyczyść swoją lokalną bazę (sekcja 5).

Konwencje schematu (zgodne z modelem danych):

- nazwy tabel i kolumn po angielsku, `snake_case`, tabele w liczbie mnogiej,
- klucz główny `id bigint GENERATED ALWAYS AS IDENTITY`,
- klucze obce `<encja>_id`, zawsze `ON DELETE RESTRICT`; rekordów nie usuwamy, tylko dezaktywujemy (`active`, `valid_to`, status),
- **indeks na każdej kolumnie klucza obcego** (PostgreSQL nie tworzy ich automatycznie),
- stałe słowniki jako `varchar` + `CHECK`, a w Javie `@Enumerated(EnumType.STRING)`; słowniki edytowane przez administratora jako osobne tabele,
- godziny jako `numeric(6,2)`, nigdy `float`,
- czas jako `timestamptz`, okresy obowiązywania jako `date`,
- w każdej tabeli z kluczem surogatowym kolumny techniczne:

```sql
created_at  timestamptz NOT NULL DEFAULT now(),
updated_at  timestamptz NOT NULL DEFAULT now(),
version     bigint      NOT NULL DEFAULT 0,
```

  `created_at` i `updated_at` ustawia Hibernate (`@CreationTimestamp`, `@UpdateTimestamp`), a `version` służy do blokady optymistycznej (`@Version`). `DEFAULT` obsługuje wiersze dodane poza aplikacją, np. dane startowe w migracji. W bazie nie ma triggerów.
- nazwy ograniczeń i indeksów nadajemy jawnie: `pk_<tabela>`, `fk_<tabela>_<cel>`, `uq_<tabela>_<kolumny>`, `ck_<tabela>_<opis>`, `ix_<tabela>_<kolumny>`. Wtedy komunikat błędu od razu mówi, która reguła została naruszona.

Dane startowe (np. słowniki) też są migracjami. Ich późniejsza zmiana to kolejna migracja albo zmiana przez aplikację, nigdy edycja starego pliku.

## 5. Czyszczenie lokalnej bazy

Gdy lokalna baza ma stan niezgodny z migracjami (np. zmieniłeś niescalony plik migracji albo utworzyłeś tabele ręcznie), najprościej zacząć od pustego schematu.

Docker (usuwa wszystkie dane bazy):

```powershell
docker compose down -v
docker compose up --build -d
```

Lokalny PostgreSQL (jako `tas_user` w `tas_db`):

```sql
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
```

Przy następnym starcie backend wykona wszystkie migracje od początku.

## 6. Testy

Profil `test` wyłącza bazę danych i Flywaya (`spring.flyway.enabled=false` w `src/test/resources/application-test.properties`), więc `./mvnw test` nie wymaga PostgreSQL. Testy integracyjne na prawdziwej bazie są przedmiotem SF-12.

## 7. Rozwiązywanie problemów

| Objaw | Przyczyna | Rozwiązanie |
|-------|-----------|-------------|
| `Migration checksum mismatch for migration version N` | Zmieniono plik migracji, który był już wykonany | Przywróć plik z `dev` i dodaj nową migrację; jeśli migracja jest tylko na Twojej gałęzi, wyczyść lokalną bazę (sekcja 5) |
| `Found non-empty schema(s) "public" but no schema history table` | W bazie są tabele utworzone poza Flywayem | Wyczyść lokalną bazę (sekcja 5) |
| `Found more than one migration with version N` | Dwa pliki z tym samym numerem (np. po scaleniu gałęzi) | Zmień numer migracji, która nie trafiła jeszcze na `dev` |
| `Detected resolved migration not applied to database` | Na `dev` pojawiła się migracja o numerze niższym niż już wykonane lokalnie | Wyczyść lokalną bazę (sekcja 5) |
| Błąd SQL przy starcie, np. `violates check constraint` w migracji z danymi | Dane w migracji łamią ograniczenie | Popraw migrację (tylko jeśli nie trafiła na `dev`) i wyczyść lokalną bazę |

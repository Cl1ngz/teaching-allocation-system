# Konfiguracja bazy danych PostgreSQL (lokalnie, Windows)

Instrukcja przygotowania lokalnej bazy danych i połączenia z nią dla backendu Teaching Allocation System.

> Zakres: instalacja PostgreSQL, utworzenie bazy i użytkownika oraz konfiguracja połączenia przez zmienne środowiskowe.
> Schemat bazy (tabele, encje, migracje) jest realizowany w osobnych zadaniach. Uruchamianie całości w Dockerze opisuje `compose.yaml`.

## 1. Wymagania

- Java 21
- PostgreSQL 15 lub nowszy (Docker Compose używa wersji 15)
- Backend z katalogu `backend/` (Maven Wrapper jest w repozytorium)

## 2. Instalacja PostgreSQL

1. Pobierz instalator dla Windows ze strony <https://www.postgresql.org/download/windows/> (instalator EDB).
2. Uruchom instalator i zostaw komponenty: PostgreSQL Server, Command Line Tools i opcjonalnie pgAdmin 4. Stack Builder nie jest potrzebny.
3. Ustaw hasło dla superużytkownika `postgres` i **zapamiętaj je** (służy tylko do administracji, nie do aplikacji).
4. Zostaw domyślny port `5432`.
5. Zakończ instalację. Usługa PostgreSQL uruchamia się automatycznie z systemem.

Sprawdzenie, czy usługa działa (PowerShell):

```powershell
Get-Service -Name "postgresql*"
```

Status powinien mieć wartość `Running`. Jeśli polecenie `psql` nie jest rozpoznawane, dodaj do zmiennej `PATH` katalog `C:\Program Files\PostgreSQL\<wersja>\bin` i otwórz nowe okno terminala.

## 3. Utworzenie bazy i użytkownika aplikacji

Aplikacja nie łączy się jako `postgres`, tylko jako osobny użytkownik `tas_user` z dostępem do jednej bazy `tas_db` (te same nazwy co w `.env.example` w katalogu głównym).

1. Otwórz plik `backend/docs/create-database.sql` i zmień `tas_password` na własne hasło (opcjonalnie).
2. Uruchom skrypt (z katalogu głównego repozytorium):

```powershell
psql -U postgres -f backend/docs/create-database.sql
```

Alternatywnie wykonaj polecenia ręcznie w `psql -U postgres`:

```sql
CREATE USER tas_user WITH PASSWORD 'tas_password';
CREATE DATABASE tas_db OWNER tas_user ENCODING 'UTF8' TEMPLATE template0;
```

Sprawdzenie logowania nowym użytkownikiem:

```powershell
psql -U tas_user -h localhost -d tas_db -c "SELECT 1;"
```

## 4. Parametry połączenia

Dane dostępowe nie są przechowywane w kodzie. Konfiguracja (`application.properties`) odczytuje je ze zmiennych środowiskowych o tych samych nazwach, których używa `compose.yaml`:

| Zmienna                      | Opis                   | Wartość domyślna                          | Wymagana |
|------------------------------|------------------------|-------------------------------------------|----------|
| `SPRING_DATASOURCE_URL`      | URL JDBC bazy danych   | `jdbc:postgresql://localhost:5432/tas_db` | nie      |
| `SPRING_DATASOURCE_USERNAME` | Użytkownik bazy danych | brak                                      | **tak**  |
| `SPRING_DATASOURCE_PASSWORD` | Hasło użytkownika      | brak                                      | **tak**  |

Użytkownik i hasło celowo nie mają wartości domyślnych. Bez nich aplikacja nie wystartuje.

**Docker a uruchomienie lokalne.** Plik `.env.example` w katalogu głównym jest przeznaczony dla Docker Compose: URL ma w nim host `db`, czyli nazwę usługi w sieci Dockera. Przy uruchamianiu backendu bezpośrednio na komputerze użyj hosta `localhost` (patrz `backend/.env.example`).

## 5. Ustawienie zmiennych środowiskowych

### Opcja A: plik `.env` (zalecana lokalnie)

```powershell
cd backend
Copy-Item .env.example .env
```

Zawartość `backend/.env`:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/tas_db
SPRING_DATASOURCE_USERNAME=tas_user
SPRING_DATASOURCE_PASSWORD=tas_password
```

Plik `.env` jest w `.gitignore` i **nie może trafić do repozytorium**. Backend wczytuje go automatycznie (`spring.config.import`), ale tylko wtedy, gdy jest uruchamiany z katalogu `backend/`.

### Opcja B: zmienne w sesji PowerShell

```powershell
$env:SPRING_DATASOURCE_USERNAME = "tas_user"
$env:SPRING_DATASOURCE_PASSWORD = "tas_password"
```

Zmienne obowiązują tylko w bieżącym oknie terminala. Zmienne systemowe mają pierwszeństwo nad wartościami z pliku `.env`.

### Opcja C: IDE

W IntelliJ IDEA: *Run → Edit Configurations → Environment variables* i wpisz `SPRING_DATASOURCE_USERNAME=tas_user;SPRING_DATASOURCE_PASSWORD=tas_password`.

## 6. Uruchomienie i weryfikacja połączenia

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Połączenie działa poprawnie, jeśli:

1. Aplikacja startuje bez błędów, a w logach nie ma wyjątków związanych z `HikariPool` ani `PSQLException`.
2. Endpoint health zwraca status `UP`:

```powershell
curl http://localhost:8080/actuator/health
```

Oczekiwana odpowiedź:

```json
{"status":"UP"}
```

Gdy baza przestanie odpowiadać po starcie aplikacji, status zmieni się na `DOWN`. Endpoint `/actuator/health` odzwierciedla realny stan połączenia z bazą, w przeciwieństwie do `/api/v1/health`, który nie sprawdza bazy. Przy błędnych danych dostępowych aplikacja nie uruchomi się w ogóle (Hibernate łączy się z bazą już przy starcie).

## 7. Docker Compose a lokalny PostgreSQL

`compose.yaml` publikuje bazę z kontenera na porcie `5432` hosta. Jeśli lokalna usługa PostgreSQL też nasłuchuje na `5432`, `docker compose up` zgłosi konflikt portów. Przed uruchomieniem Dockera zatrzymaj lokalną usługę (`Stop-Service postgresql*` w PowerShellu uruchomionym jako administrator) albo użyj jednego ze środowisk naraz.

## 8. Testy automatyczne

Test `contextLoads` korzysta z profilu `test`, w którym konfiguracja bazy jest wyłączona, więc `.\mvnw.cmd test` nie wymaga uruchomionego PostgreSQL. Testy integracyjne z bazą są przedmiotem zadania SF-12.

## 9. Rozwiązywanie problemów

| Objaw | Przyczyna | Rozwiązanie |
|-------|-----------|-------------|
| `Could not resolve placeholder 'SPRING_DATASOURCE_USERNAME'` | Nie ustawiono zmiennych | Utwórz `backend/.env` lub ustaw zmienne w sesji/IDE |
| `UnknownHostException: db` | W URL jest host `db` (konfiguracja Dockera) | Użyj `localhost` w `SPRING_DATASOURCE_URL` |
| `Connection to localhost:5432 refused` | Usługa PostgreSQL nie działa lub inny port | `Get-Service postgresql*`, uruchom usługę, sprawdź port w `SPRING_DATASOURCE_URL` |
| `password authentication failed for user "tas_user"` | Złe hasło | Ustaw to samo hasło w bazie i w `SPRING_DATASOURCE_PASSWORD` (`ALTER USER tas_user WITH PASSWORD '...';`) |
| `database "tas_db" does not exist` | Baza nie została utworzona | Wykonaj kroki z sekcji 3 |
| `.env` jest ignorowany | Backend uruchomiony nie z katalogu `backend/` | Uruchom z `backend/` lub ustaw zmienne w sesji/IDE |
| `/actuator/health` zwraca `DOWN` | Brak połączenia z bazą | Sprawdź logi aplikacji i parametry z sekcji 4 |
| `docker compose up`: port 5432 zajęty | Lokalny PostgreSQL działa na tym samym porcie | Zobacz sekcję 7 |
| `psql` nie jest rozpoznawane | Brak w `PATH` | Dodaj `C:\Program Files\PostgreSQL\<wersja>\bin` do `PATH` |

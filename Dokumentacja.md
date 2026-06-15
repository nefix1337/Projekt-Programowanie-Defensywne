# Projekt-Programowanie-Defensywne

Aplikacja webowa do zarządzania projektami i zadaniami z wbudowaną autentykacją, autoryzacją i wsparcem dla dwuwymiarowej autentykacji (2FA).

## Technologia

- **Frontend**: React 19 + Vite + TailwindCSS
- **Backend**: Spring Boot 3.4.4 (Java 21)
- **Baza danych**: PostgreSQL 16
- **Message Broker**: RabbitMQ
- **Konteneryzacja**: Docker & Docker Compose

## Szybki start

### Za pomocą Docker 

w folderze projektu ```..\Projekt-Programowanie-Defensywne>``` wpisujemy:

```bash
cp .env.example .env
docker-compose up -d
```

Aplikacja będzie dostępna pod:
- Frontend: http://localhost:3000/login
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui-custom.html
- RabbitMQ Management: http://localhost:15672 (`guest` / `guest`)

## Architektura rozproszona

Projekt ma osobny backend REST oraz trzy wezly zapisu `node-1`, `node-2`, `node-3`.
Backend wysyla operacje zapisu na zadaniach przez RabbitMQ do aktywnego lidera.

Obslugiwane komendy RabbitMQ:

- `tasks.create` - tworzenie zadania
- `tasks.update` - aktualizacja zadania
- `tasks.delete` - usuwanie zadania
- `tasks.review` - przekazanie zadania do sprawdzenia
- `tasks.comment` - dodanie komentarza

Wybor lidera:

- kazdy wezel cyklicznie zapisuje heartbeat w tabeli `node_leader_candidates`
- liderem zostaje aktywny wezel o najwyzszej wadze
- tylko lider uruchamia listenery RabbitMQ dla operacji zapisu
- po awarii lidera kolejny aktywny wezel przejmuje przetwarzanie po uplywie TTL

Monitoring:

- panel administratora pokazuje status wezlow, aktualnego lidera i ostatni heartbeat
- przyciski `Awaria` i `Przywroc` pozwalaja zasymulowac awarie wezla
- historia zdarzen jest zapisywana w tabeli `distributed_node_events`
- endpointy monitoringu sa dostepne pod `/api/admin/nodes` oraz `/api/admin/nodes/events`
- zakladka "Metryki" w panelu administratora pokazuje zliczenia zdarzen wedlug typu (`/api/admin/nodes/metrics`)
- jesli zaden wezel nie jest aktualnym liderem, panel administratora wyswietla baner ostrzegawczy

### Wstrzykiwanie awarii (fault injection)

System wspiera trzy niezalezne typy wstrzykiwanych awarii, konfigurowane per wezel z panelu administratora
(zakladka "Monitorowanie węzłów" -> sekcja "Symulacja dodatkowych awarii"):

| Typ awarii | Jak wywolac | Efekt |
|---|---|---|
| **Awaria wezla (NODE_DOWN)** | przycisk `Awaria` / `Przywroc` | wezel jest oznaczony jako `forced_down`, przestaje brac udzial w wyborze lidera i zatrzymuje swoje listenery RabbitMQ; kolejny wezel o najwyzszej wadze przejmuje lidera po uplywie TTL |
| **Opoznienie sieciowe (NETWORK_DELAY)** | pole "Opoznienie (ms)" + przycisk `Ustaw opoznienie` (0-30000 ms) | przed przetworzeniem kazdej operacji na zadaniu (create/update/delete/status/comment) wezel-lider wstrzymuje wykonanie o skonfigurowana liczbe milisekund; przy opoznieniu wiekszym niz timeout RPC (`tasks.rabbitmq.reply-timeout-ms`, domyslnie 10000 ms) backend zwraca `503 Service Unavailable` |
| **Uszkodzenie wiadomosci (MESSAGE_CORRUPTION)** | przycisk `Wywolaj korupcje wiadomosci` / `Wylacz korupcje wiadomosci` | wezel-lider odrzuca kazda operacje na zadaniu wyjatkiem (`Simulated message corruption...`), co backend zwraca jako `500 Internal Server Error` |

Kazda zmiana stanu awarii (wlaczenie/wylaczenie) jest zapisywana w historii zdarzen (`NODE_FAILURE_INJECTED`, `NODE_RECOVERED`,
`NETWORK_DELAY_INJECTED`, `NETWORK_DELAY_CLEARED`, `MESSAGE_CORRUPTION_INJECTED`, `MESSAGE_CORRUPTION_CLEARED`), a samo
zadzialanie awarii podczas przetwarzania wiadomosci jest dodatkowo logowane (`log.warn`/`log.error`) i zapisywane jako
`NETWORK_DELAY_APPLIED` / `MESSAGE_CORRUPTION_TRIGGERED`.

Endpointy administracyjne (rola `ADMIN`):

- `POST /api/admin/nodes/{nodeId}/failure` / `/recovery` - awaria/przywrocenie wezla
- `POST /api/admin/nodes/{nodeId}/network-delay` (body: `{ "delayMs": number }`, 0-30000) - ustawienie/wyczyszczenie opoznienia sieciowego
- `POST /api/admin/nodes/{nodeId}/message-corruption` / `/message-corruption/clear` - wlaczenie/wylaczenie korupcji wiadomosci
- `GET /api/admin/nodes/metrics` - zliczenia zdarzen wedlug typu

Przykladowy scenariusz demonstracji:

1. Uruchom `docker-compose up -d --build`.
2. Zaloguj sie jako `admin@example.com`.
3. Wejdz do panelu administratora i sprawdz lidera.
4. Kliknij `Awaria` przy aktualnym liderze.
5. Po kilku sekundach kolejny wezel powinien zostac liderem.
6. Dodaj lub zaktualizuj zadanie i sprawdz wpis w historii zdarzen.
7. Ustaw opoznienie sieciowe (np. 15000 ms) na aktualnym liderze i sprobuj dodac zadanie - operacja powinna zakonczyc sie bledem `503` po przekroczeniu limitu czasu RPC.
8. Wylacz opoznienie, wlacz korupcje wiadomosci na liderze i sprobuj zaktualizowac zadanie - operacja powinna zakonczyc sie bledem `500`, a w historii zdarzen pojawi sie wpis `MESSAGE_CORRUPTION_TRIGGERED`.
9. Sprawdz zakladke "Metryki", aby zobaczyc zsumowane liczby zdarzen wedlug typu.

## 🔐 Domyślne konto administratora

Przy każdym starcie backendu jest automatycznie tworzone konto administratora:

| Pole | Wartość |
|------|---------|
| **Email** | admin@example.com |
| **Hasło** | admin123 |
| **Rola** | ADMIN |
| **2FA** | Wyłączone |

### Logowanie
1. Przejdź do http://localhost:3000/login
2. Wpisz email: `admin@example.com`
3. Wpisz hasło: `admin123`
4. Kliknij "Zaloguj się"


## Pozostałe konta (przykładowe)

### Manager
 
| Pole | Wartość |
|------|---------|
| **Email** | manager@example.com |
| **Hasło** | manager123 |
| **Rola** | MANAGER |
| **2FA** | Wyłączone |
 
### Jan Developer
 
| Pole | Wartość |
|------|---------|
| **Email** | developer@example.com |
| **Hasło** | user123 |
| **Rola** | USER |
| **2FA** | Wyłączone |
 
### Anna Tester
 
| Pole | Wartość |
|------|---------|
| **Email** | tester@example.com |
| **Hasło** | user123 |
| **Rola** | USER |
| **2FA** | Wyłączone |

##

## Struktura projektu

## Funkcjonalności

- Autentykacja i autoryzacja (JWT)
- 2FA (Two-Factor Authentication)
- Zarządzanie projektami
- Zarządzanie zadaniami
- Komentarze do zadań
- Panel administratora
- RabbitMQ dla operacji zapisu zadan
- Wybor lidera wsrod wezlow zapisu
- Heartbeat, wykrywanie awarii i przejecie lidera
- Wstrzykiwanie awarii (wezel offline, opoznienie sieciowe, korupcja wiadomosci)
- Historia zdarzen i metryki systemu rozproszonego
- CORS skonfigurowany dla bezpieczeństwa

## Zmienne środowiskowe

Przed uruchomieniem Dockera skopiuj `.env.example` do `.env` i ustaw wlasne wartosci sekretow:

- `JWT_SECRET` - sekret do podpisywania tokenow JWT, minimum 32 losowe znaki
- `TOTP_ENCRYPTION_SECRET` - sekret do szyfrowania sekretow TOTP w bazie, minimum 32 losowe znaki
- `APP_CORS_ALLOWED_ORIGINS` - dozwolone originy frontendu oddzielone przecinkami

Plik `.env` jest ignorowany przez Git. Nie commituj prawdziwych sekretow.

## Bezpieczeństwo

Aplikacja została poddana audytowi pod kątem podstawowych zagrożeń z listy OWASP Top 10 (SQL Injection, XSS, CSRF, błędna konfiguracja CORS).

### SQL Injection

- Dostęp do bazy danych odbywa się przez Spring Data JPA (metody wyprowadzone z nazwy, zawsze parametryzowane) oraz `JdbcTemplate` w module monitoringu węzłów i w node-ach (`NodeMonitoringService`, `LeaderElectionService`, `DistributedEventService`, `FaultInjectionService`).
- Wszystkie zapytania `JdbcTemplate` używają parametrów pozycyjnych (`?`) - w kodzie nie występuje budowanie zapytań SQL przez konkatenację stringów z danymi wejściowymi użytkownika.
- W projekcie nie ma żadnych natywnych zapytań `@Query(nativeQuery = true)`.

### Cross-Site Scripting (XSS)

- Frontend (React) domyślnie escapuje treści renderowane w JSX - w kodzie nie używamy `dangerouslySetInnerHTML`, `eval`, `document.write` ani `innerHTML`.
- Backend ustawia nagłówek `Content-Security-Policy`:
  `default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; frame-ancestors 'none'`
- Wszystkie pola tekstowe (tytuł/opis zadania, nazwa/opis projektu, komentarze) mają limity długości (`@Size`) zarówno w walidacji backendowej (Bean Validation), jak i w formularzach frontendowych (`maxLength`).

### CSRF

CSRF jest wyłączony dla API (`csrf.disable()`), ponieważ:

- aplikacja jest bezstanowa (`SessionCreationPolicy.STATELESS`) i nie korzysta z ciasteczek sesyjnych,
- token JWT jest przechowywany wyłącznie w pamięci po stronie klienta (stan React) i przesyłany w nagłówku `Authorization: Bearer <token>`, którego przeglądarka nie dołącza automatycznie do żądań cross-site (w przeciwieństwie do ciasteczek),
- `CorsConfiguration.setAllowCredentials(false)` - przeglądarka nie wysyła ciasteczek/danych uwierzytelniających w żądaniach cross-origin.

Te warunki razem eliminują klasyczny wektor ataku CSRF - atakujący nie jest w stanie wymusić uwierzytelnionego żądania z innej domeny, bo nie ma dostępu do tokenu JWT przechowywanego w pamięci aplikacji SPA.

### CORS

- Dozwolone originy są konfigurowane przez zmienną środowiskową `APP_CORS_ALLOWED_ORIGINS` (domyślnie `http://localhost:3000,http://localhost:5173,http://frontend:3000`) - brak wildcard `*`.
- Dozwolone metody: `GET, POST, PUT, PATCH, DELETE, OPTIONS`.
- Dozwolone nagłówki ograniczone do `Authorization, Content-Type, Accept`.
- `allowCredentials = false`.

### Nagłówki bezpieczeństwa HTTP

Skonfigurowane w `SecurityConfig`:

| Nagłówek | Wartość | Cel |
|---|---|---|
| `X-Frame-Options` | `DENY` | ochrona przed clickjackingiem |
| `Content-Security-Policy` | patrz wyżej | ograniczenie źródeł skryptów/stylów/zasobów |
| `Referrer-Policy` | `strict-origin-when-cross-origin` | ogranicza wyciek adresów URL do innych domen |
| `Strict-Transport-Security` | `max-age=31536000; includeSubDomains` | wymusza HTTPS (po wdrożeniu za TLS) |
| `Permissions-Policy` | `geolocation=(), microphone=(), camera=(), payment=()` | wyłącza nieużywane API przeglądarki |
| `X-Content-Type-Options` | `nosniff` | domyślny nagłówek Spring Security, zapobiega sniffowaniu MIME |

### Walidacja danych wejściowych

- Wszystkie DTO żądań (`CreateTaskRequest`, `UpdateTaskRequest`, `CreateProjectRequest`, `UpdateProjectRequest`, `AddProjectMemberRequest`, `ChangeRoleRequest`, `RegisterRequest`, `LoginRequest`, `TotpRequest`, `AddTaskCommentRequest`) są walidowane przez Bean Validation (`@NotBlank`, `@Size`, `@Pattern`, `@Email`, `@Positive`, `@FutureOrPresent`).
- Błędy walidacji oraz niepoprawny JSON / nieznane wartości enum zwracają `400 Bad Request` wraz z listą błędów (`GlobalExceptionHandler`).
- Formularze frontendowe (`AddTask`, `EditTask`, `NewProject`, `EditProject`) odzwierciedlają te same limity (`maxLength`, sprawdzanie dat, pola wymagane) przed wysłaniem żądania.
- Aktualizacja projektu (`PUT /api/projects/{id}`) korzysta z dedykowanego DTO (`UpdateProjectRequest`) zamiast bindowania encji JPA, co eliminuje ryzyko mass assignment.

### Przechowywanie danych

- Hasła są haszowane przy użyciu `BCryptPasswordEncoder`.
- Hasła i sekrety TOTP są oznaczone `@JsonIgnore` i nigdy nie są zwracane w odpowiedziach API.
- Sekrety (`JWT_SECRET`, `TOTP_ENCRYPTION_SECRET`) są przechowywane w pliku `.env` (ignorowanym przez Git), a sekret TOTP użytkownika jest szyfrowany w bazie danych.
- Uwierzytelnianie dwuczynnikowe (2FA, TOTP) jest dostępne dla kont użytkowników.

## Zmiana portów

Jeśli porty są zajęte, edytuj `docker-compose.yml`:

```yaml
services:
  frontend:
    ports:
      - "3001:3000"  # Zmień na inny port
  backend:
    ports:
      - "8081:8080"  # Zmień na inny port
```

## Developerskie polecenia

```bash
# Przebuduj obrazy
docker-compose up -d --build

# Zatrzymaj kontenery
docker-compose down

# Usuń wszystko (w tym dane)
docker-compose down -v

# Wyświetl logi
docker-compose logs -f
```

## Testy

### Testy jednostkowe

```bash
cd backend && ./mvnw test
cd node && ../backend/mvnw test
```

### Testy integracyjne systemu rozproszonego

`backend/src/test/java/pl/projekt/backend/integration/DistributedSystemIT.java` to test koncowo-do-konca
uruchamiany na zywym stosie `docker-compose` (backend + node-1/2/3 + RabbitMQ + Postgres). Sprawdza:

- wybor lidera (dokladnie jeden aktywny lider wsrod 3 wezlow),
- operacje na zadaniach przez RabbitMQ (tworzenie/aktualizacja przez aktualnego lidera),
- opoznienie sieciowe powyzej limitu RPC -> `503 Service Unavailable`,
- korupcje wiadomosci -> `500 Internal Server Error`,
- failover lidera po wstrzyknieciu awarii wezla (`NODE_DOWN`) i kontynuacje operacji przez nowego lidera,
- historie zdarzen i metryki (`/api/admin/nodes/events`, `/api/admin/nodes/metrics`).

Plik nazwany z sufiksem `IT`, dzieki czemu domyslny `mvn test` go nie uruchamia (nie wymaga Dockera
przy zwyklym budowaniu/testach jednostkowych). Wymaga uruchomionego `docker-compose up -d --build`:

```bash
cd backend
./mvnw test -Pintegration-tests
# inny adres backendu:
./mvnw test -Pintegration-tests -Dintegration.baseUrl=http://localhost:8081
```

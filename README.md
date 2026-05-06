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

Przykladowy scenariusz demonstracji:

1. Uruchom `docker-compose up -d --build`.
2. Zaloguj sie jako `admin@example.com`.
3. Wejdz do panelu administratora i sprawdz lidera.
4. Kliknij `Awaria` przy aktualnym liderze.
5. Po kilku sekundach kolejny wezel powinien zostac liderem.
6. Dodaj lub zaktualizuj zadanie i sprawdz wpis w historii zdarzen.

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
- Historia zdarzen systemu rozproszonego
- CORS skonfigurowany dla bezpieczeństwa

## Zmienne środowiskowe

Przed uruchomieniem Dockera skopiuj `.env.example` do `.env` i ustaw wlasne wartosci sekretow:

- `JWT_SECRET` - sekret do podpisywania tokenow JWT, minimum 32 losowe znaki
- `TOTP_ENCRYPTION_SECRET` - sekret do szyfrowania sekretow TOTP w bazie, minimum 32 losowe znaki
- `APP_CORS_ALLOWED_ORIGINS` - dozwolone originy frontendu oddzielone przecinkami

Plik `.env` jest ignorowany przez Git. Nie commituj prawdziwych sekretow.

CSRF jest wylaczony dla API, poniewaz aplikacja jest bezstanowa i uzywa tokenu JWT przekazywanego w naglowku `Authorization`, a nie ciasteczek sesyjnych. CORS jest ograniczony do originow z `APP_CORS_ALLOWED_ORIGINS`.

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

" 

# Projekt-Programowanie-Defensywne

Aplikacja webowa do zarządzania projektami i zadaniami z wbudowaną autentykacją, autoryzacją i wsparcem dla dwuwymiarowej autentykacji (2FA).

## Technologia

- **Frontend**: React 19 + Vite + TailwindCSS
- **Backend**: Spring Boot 3.4.4 (Java 21)
- **Baza danych**: PostgreSQL 16
- **Message Broker**: Apache Kafka (Event Sourcing)
- **Konteneryzacja**: Docker & Docker Compose

## Szybki start

### Za pomocą Docker 

w folderze projektu ```..\Projekt-Programowanie-Defensywne>``` wpisujemy:

```bash
docker-compose up -d
```

Aplikacja będzie dostępna pod:
- Frontend: http://localhost:3000/login
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui-custom.html

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
- CORS skonfigurowany dla bezpieczeństwa

## Zmienne środowiskowe

### Backend
Skopiuj `.env.example` do `.env` i dostosuj:

```bash
cp backend/.env.example backend/.env
```

### Frontend
Skopiuj `.env.example` do `.env` i dostosuj:

```bash
cp frontend/.env.example frontend/.env
```

## Dokumentacja

- [Deployment & Verification Guide](DEPLOYMENT.md) - Wdrażanie, testowanie i monitorowanie
- [Quick Reference Card](QUICK_REFERENCE.md) - Szybkie komendy i API endpoints
- [Database Migrations](MIGRATIONS.md) - Inicjalizacja schematu bazy danych i Flyway
- [Docker Setup Guide](DOCKER.md) - Szczegółowy przewodnik konfiguracji Docker
- [Backend README](backend/README.md) - Dokumentacja backendu
- [Frontend README](frontend/README.md) - Dokumentacja frontendu

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

## 📡 Event Sourcing z Apache Kafka

Projekt implementuje **Event Sourcing** przy użyciu Apache Kafka do pełnego zapisu zmian:

### Cechy
- **Pełna audyt trail**: Każda operacja na zadaniu jest zapisywana jako event
- **Zdarzenia**: TASK_CREATED, TASK_UPDATED, TASK_DELETED, TASK_COMMENTED
- **Replikacja**: wydarzenia są publikowane na temat Kafka i trwale przechowywane w tabeli `event_store`
- **Replay**: Możliwość odtworzenia stanu zadania w dowolnym punkcie czasowym
- **Migrations**: Schemat bazy danych automatycznie tworzony przez Flyway przy starcie

### Monitoring
- **Kafka UI**: http://localhost:8085 - Przeglądaj tematy, wiadomości i grupy konsumentów
- **Event Store**: PostgreSQL tabela `event_store` z indeksami dla szybkich zapytań
- **Metryki**: REST API `/api/events/statistics` dla statystyk zdarzeń

### API dla Event Sourcing
```bash
# Pobierz historię zmian dla zadania
GET /api/events/tasks/{taskId}/history

# Pobierz statystyki zdarzeń
GET /api/events/statistics

# Pobierz zdarzenia użytkownika
GET /api/events/users/{userId}

# Pobierz zdarzenia projektu
GET /api/events/projects/{projectId}
```

### Dokumentacja
- [KAFKA.md](./KAFKA.md) - Szczegółowy przewodnik Event Sourcing i Kafka
- [KAFKA_QUICKSTART.md](./KAFKA_QUICKSTART.md) - Szybki start
- [MIGRATIONS.md](./MIGRATIONS.md) - Inicjalizacja bazy danych i profile deploymentu

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

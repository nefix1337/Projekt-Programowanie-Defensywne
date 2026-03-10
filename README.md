# Projekt-Programowanie-Defensywne

Aplikacja webowa do zarządzania projektami i zadaniami z wbudowaną autentykacją, autoryzacją i wsparcem dla dwuwymiarowej autentykacji (2FA).

## Technologia

- **Frontend**: React 19 + Vite + TailwindCSS
- **Backend**: Spring Boot 3.4.4 (Java 21)
- **Baza danych**: PostgreSQL 16
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

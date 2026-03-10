# Docker Setup Guide

## Przegląd
Ten projekt zawiera konfigurację Docker do uruchamiania aplikacji trójwarstwowej:
- **Frontend**: React z Vite (port 3000)
- **Backend**: Spring Boot Java (port 8080)
- **Baza danych**: PostgreSQL (port 5432)

## Wymagania
- Docker Desktop (https://www.docker.com/products/docker-desktop)
- Docker Compose (normalnie zainstalowany z Docker Desktop)

## Szybki start

### 1. Budowanie i uruchamianie całej aplikacji

```bash
docker-compose up -d
```

Flagi:
- `-d` = uruchomienie w tle
- `--build` = przebudowanie obrazów (gdy zmienisz kod)

```bash
docker-compose up -d --build
```

### 2. Widok statusu kontenerów

```bash
docker-compose ps
```

### 3. Dostęp do aplikacji

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui-custom.html
- **PostgreSQL**: localhost:5432

### 4. Logowanie i debugowanie

Wyświetl logi wszystkich kontenerów:
```bash
docker-compose logs -f
```

Logi konkretnego kontenera:
```bash
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres
```

### 5. Zatrzymanie aplikacji

```bash
docker-compose down
```

Jeśli chcesz usunąć też bazę danych:
```bash
docker-compose down -v
```

## Struktura konfiguracji

### docker-compose.yml
Główny plik konfiguracyjny z trzema usługami:
- **postgres**: Baza danych
- **backend**: Aplikacja Spring Boot
- **frontend**: Aplikacja React

### Dockerfiles
- `backend/Dockerfile`: Multi-stage build dla Java aplikacji
- `frontend/Dockerfile`: Multi-stage build dla React aplikacji

### Zmienne środowiskowe
- `backend/.env.example`: Szablon zmiennych dla backendu
- `frontend/.env.example`: Szablon zmiennych dla frontendu

## Zmiana konfiguracji

### Dane dostępu do bazy danych
Edytuj w `docker-compose.yml`:
```yaml
environment:
  POSTGRES_USER: postgres
  POSTGRES_PASSWORD: postgres
  POSTGRES_DB: projekt
```

### Port aplikacji
Jeśli port jest zajęty, zmień mapowanie w `docker-compose.yml`:
```yaml
ports:
  - "3001:3000"  # Frontend na porcie 3001 zamiast 3000
  - "8081:8080"  # Backend na porcie 8081 zamiast 8080
```

### JWT Secret
Zmień `JWT_SECRET` w `docker-compose.yml` na nowy, bezpieczny klucz.

## Troubleshooting

### Kontener nie startuje
```bash
docker-compose logs <service_name>
```

### Port już w użyciu
```bash
docker-compose down
# lub zmień port w docker-compose.yml
```

### Baza danych nie inicjalizuje się
```bash
docker-compose down -v
docker-compose up -d
```

### Backend nie widzi bazy danych
Upewnij się, że w `docker-compose.yml` backend czeka na PostgreSQL:
```yaml
depends_on:
  postgres:
    condition: service_healthy
```

## Development workflow

### Podczas developmentu
Po zmianach w kodzie, przebuduj obrazy:
```bash
docker-compose up -d --build
```

### Edytowanie kodu
1. Edytuj kod lokalnie (jak zwykle)
2. Przebuduj: `docker-compose up -d --build`
3. Kontenery automatycznie się przeładują

### Czyszczenie
```bash
# Zatrzymaj kontenery
docker-compose down

# Usuń obrazy
docker-compose down --rmi all

# Usuń wszystko włącznie z danymi
docker-compose down -v --rmi all
```

## Zaawansowana konfiguracja

### Network
Wszystkie kontenery komunikują się przez sieć `projekt-network`:
- Backend: `backend:8080`
- PostgreSQL: `postgres:5432`

### Health Checks
Każda usługa ma health check:
- PostgreSQL czeka 10s na sprawdzenie
- Backend czeka 40s na start + sprawdzenie
- Frontend czeka 10s na start + sprawdzenie

### Volumes
Dane PostgreSQL są przechowywane w `postgres_data` volume - przetrwają poza cyklem kontenerów.

## Production considerations

1. **Zmień hasła** w `docker-compose.yml`
2. **Użyj secrets**: Użyj Docker Secrets dla hasła JWT i bazy danych
3. **Ustal limity zasobów**: Dodaj `resources` limit w `docker-compose.yml`
4. **Użyj konkretnych wersji**: Zmień `latest` na konkretne numery wersji
5. **CORS**: Skonfiguruj poprawnie CORS dla backendu
6. **Environment**: Oddziel konfigurację dla dev/prod

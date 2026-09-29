# Wallet Service

Backend REST API для электронного кошелька.

Проект реализован на Java и Spring Boot. Основная цель — практика разработки REST API, работы с базой данных, транзакциями, валидацией и архитектурой backend-приложения.

## Features

- создание пользователей;
- создание кошельков;
- получение информации о кошельке;
- пополнение баланса;
- снятие средств;
- перевод денег между кошельками;
- история транзакций;
- валидация входных данных;
- централизованная обработка ошибок;
- миграции базы данных через Flyway.

## Tech Stack

- Java 21
- Spring Boot
- Spring MVC
- Spring Data JPA
- Hibernate
- PostgreSQL
- Flyway
- Docker / Docker Compose
- Maven
- Jakarta Validation
- Lombok

## Architecture

```text
Controller
   ↓
Service
   ↓
Repository
   ↓
PostgreSQL
```

Основные пакеты проекта:

```text
controller
dto
entity
repository
service
exception
```

## API

### Users

```http
POST /api/users
```

Создание нового пользователя.

### Wallets

```http
POST /api/wallets
GET /api/wallets/{walletId}
GET /api/wallets/user/{userId}
```

Создание и получение кошельков.

### Balance operations

```http
POST /api/wallets/{walletId}/deposit
POST /api/wallets/{walletId}/withdraw
```

Пополнение и снятие средств.

### Transfers

```http
POST /api/transfers
```

Перевод средств между кошельками.

### Transactions

```http
GET /api/wallets/{walletId}/transactions
```

Получение истории операций кошелька.

## Database

В проекте используется PostgreSQL.

Структура базы данных создаётся через Flyway migrations:

```text
V1__create_users_table.sql
V2__create_wallets_table.sql
V3__create_transactions_table.sql
V4__add_transfer_support.sql
```

## Run locally

Клонировать репозиторий:

```bash
git clone https://github.com/Ilya-Turenko/wallet-service.git
cd wallet-service
```

Запустить PostgreSQL:

```bash
docker compose up -d
```

Запустить приложение.

Windows:

```bash
mvnw.cmd spring-boot:run
```

Linux / macOS:

```bash
./mvnw spring-boot:run
```

После запуска API доступен по адресу:

```text
http://localhost:8080
```

## API Testing

Для ручного тестирования запросов используется файл:

```text
requests.http
```

Запросы можно выполнять напрямую из IntelliJ IDEA.

## Project Status

Основная бизнес-логика Wallet Service реализована.

В дальнейшем проект планируется дополнить:

- Spring Security;
- JWT authentication;
- unit tests;
- integration tests;
- Swagger / OpenAPI;
- Dockerfile;
- CI/CD.

## Author

Ilya Turenko

GitHub: https://github.com/Ilya-Turenko

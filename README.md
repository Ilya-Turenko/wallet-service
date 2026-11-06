# WalletService

Учебный REST API электронного кошелька на Java и Spring Boot.

## Возможности

- Регистрация и вход с JWT.
- Создание кошелька, пополнение и снятие денег.
- Переводы между кошельками одной валюты.
- Просмотр баланса и истории операций.
- Проверка владельца кошелька.
- Блокировки при изменении баланса.

## Технологии

Java 21, Spring Boot, Spring Security, PostgreSQL, Spring Data JPA, Flyway, Maven, JUnit 5, Mockito.

## Запуск

1. Установить JDK 21 и Docker.
2. Запустить базу из каталога проекта:

```powershell
docker compose up -d
```

3. Сгенерировать ключ JWT и задать переменную окружения в PowerShell:

```powershell
$keyBytes = New-Object byte[] 32
$random = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$random.GetBytes($keyBytes)
$random.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($keyBytes)
```

4. В том же терминале запустить приложение:

```powershell
.\mvnw.cmd spring-boot:run
```

При запуске через IntelliJ IDEA переменную `JWT_SECRET` нужно задать в настройках конфигурации запуска. Секретный ключ не следует сохранять в репозитории.

Приложение доступно по адресу `http://localhost:8080`. Миграции базы выполняются автоматически.

## API

| Метод | Адрес | Назначение |
|---|---|---|
| POST | `/api/users` | Регистрация |
| POST | `/api/users/login` | Получение токена |
| POST | `/api/wallets` | Создание кошелька |
| GET | `/api/wallets/{id}` | Просмотр кошелька |
| GET | `/api/wallets/user/{userId}` | Кошелёк пользователя |
| POST | `/api/wallets/{id}/deposit` | Пополнение |
| POST | `/api/wallets/{id}/withdraw` | Снятие |
| GET | `/api/wallets/{id}/transactions` | История операций |
| POST | `/api/transfers` | Перевод |

Для работы с кошельками и переводами требуется заголовок:

```http
Authorization: Bearer <accessToken>
```

Токен выдаётся при входе и действует 15 минут. Пользователь может управлять только своим кошельком.

Примеры запросов находятся в `requests.http` и выполняются через IntelliJ HTTP Client.

## Тесты

Тесты сервисов запускаются без базы данных:

```powershell
.\mvnw.cmd "-Dtest=UserServiceTest,WalletServiceTest,TransferServiceTest" test
```

## Ограничения

Проект не подключён к платёжной системе: пополнение является учебной операцией. Пользовательский интерфейс, обновление токенов и интеграционные тесты пока не реализованы. Некоторые некорректные запросы могут возвращать `500`.

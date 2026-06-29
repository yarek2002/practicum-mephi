# Lottery Backend

Бэкенд-система для проведения лотерейных тиражей: создание тиражей, покупка билетов, определение выигрышной комбинации и проверка результатов.

## Технологии

- **Java 21** (без Spring / Spring Boot)
- **Javalin** — REST API
- **PostgreSQL 16** — хранение данных
- **JDBC + HikariCP** — доступ к БД
- **Docker Compose** — развёртывание

## Архитектура

Проект разделён на слои:

```
com.lottery
├── controller/   — HTTP-эндпоинты, маршрутизация, обработка ошибок
├── service/      — бизнес-логика (статусы, валидация, генерация комбинаций)
├── repository/   — работа с PostgreSQL через JDBC
├── model/        — доменные сущности и перечисления статусов
├── dto/          — объекты запросов и ответов API
├── config/       — конфигурация приложения и подключения к БД
└── exception/    — доменные исключения
```

### Модель данных

**Draw (тираж)**

| Поле             | Тип          | Описание                              |
|------------------|--------------|---------------------------------------|
| id               | BIGSERIAL    | Идентификатор                         |
| status           | VARCHAR      | `ACTIVE` или `COMPLETED`              |
| numbers_count    | INTEGER      | Количество чисел в комбинации (по умолчанию 6) |
| max_number       | INTEGER      | Максимальное число (по умолчанию 49) |
| winning_numbers  | INTEGER[]    | Выигрышная комбинация (после завершения) |
| created_at       | TIMESTAMPTZ  | Дата создания                         |
| completed_at     | TIMESTAMPTZ  | Дата завершения                       |

**Ticket (билет)**

| Поле       | Тип         | Описание                          |
|------------|-------------|-----------------------------------|
| id         | BIGSERIAL   | Идентификатор                     |
| draw_id    | BIGINT      | Ссылка на тираж                   |
| numbers    | INTEGER[]   | Комбинация пользователя           |
| status     | VARCHAR     | `PENDING`, `WIN` или `LOSE`       |
| created_at | TIMESTAMPTZ | Дата покупки                      |

### Бизнес-логика

1. Тираж создаётся в статусе `ACTIVE`.
2. Билеты можно купить только для активного тиража; статус билета — `PENDING`.
3. При завершении тиража (`POST /draws/{id}/complete`):
   - генерируется случайная выигрышная комбинация;
   - тираж переходит в статус `COMPLETED`;
   - все билеты тиража получают статус `WIN` (полное совпадение) или `LOSE`.
4. Билет считается выигрышным при **полном совпадении** набора чисел с выигрышной комбинацией (порядок не важен).

## Запуск

### Через Docker Compose (рекомендуется)

```bash
docker compose up --build
```

Сервис будет доступен на `http://localhost:8080`.

### Локально (без Docker)

1. Запустите PostgreSQL и создайте БД `lottery` (пользователь/пароль: `lottery`/`lottery`).
2. Соберите и запустите приложение:

```bash
mvn package -DskipTests
java -jar target/lottery-backend-1.0.0.jar
```

Переменные окружения:

| Переменная   | По умолчанию                              |
|--------------|-------------------------------------------|
| `DB_URL`     | `jdbc:postgresql://localhost:5432/lottery` |
| `DB_USER`    | `lottery`                                 |
| `DB_PASSWORD`| `lottery`                                 |
| `PORT`       | `8080`                                    |

## API

### Создать тираж

```http
POST /draws
Content-Type: application/json

{
  "numbersCount": 6,
  "maxNumber": 49
}
```

Параметры необязательны (по умолчанию 6 из 49).

**Ответ (201):**

```json
{
  "id": 1,
  "status": "ACTIVE",
  "numbersCount": 6,
  "maxNumber": 49,
  "winningNumbers": null,
  "createdAt": "2026-06-29T12:00:00Z",
  "completedAt": null
}
```

### Список активных тиражей

```http
GET /draws
```

### Купить билет

```http
POST /draws/1/tickets
Content-Type: application/json

{
  "numbers": [3, 12, 25, 31, 42, 49]
}
```

**Ответ (201):**

```json
{
  "id": 1,
  "drawId": 1,
  "numbers": [3, 12, 25, 31, 42, 49],
  "status": "PENDING",
  "winningNumbers": null,
  "createdAt": "2026-06-29T12:05:00Z"
}
```

### Завершить тираж

```http
POST /draws/1/complete
```

**Ответ (200):**

```json
{
  "id": 1,
  "status": "COMPLETED",
  "numbersCount": 6,
  "maxNumber": 49,
  "winningNumbers": [3, 12, 25, 31, 42, 49],
  "createdAt": "2026-06-29T12:00:00Z",
  "completedAt": "2026-06-29T12:10:00Z"
}
```

### Проверить результат билета

```http
GET /tickets/1
```

**Ответ (200):**

```json
{
  "id": 1,
  "drawId": 1,
  "numbers": [3, 12, 25, 31, 42, 49],
  "status": "WIN",
  "winningNumbers": [3, 12, 25, 31, 42, 49],
  "createdAt": "2026-06-29T12:05:00Z"
}
```

### Health check

```http
GET /health
```

## Пример полного сценария

```bash
# 1. Создать тираж
curl -X POST http://localhost:8080/draws \
  -H "Content-Type: application/json" \
  -d "{}"

# 2. Посмотреть активные тиражи
curl http://localhost:8080/draws

# 3. Купить билет
curl -X POST http://localhost:8080/draws/1/tickets \
  -H "Content-Type: application/json" \
  -d '{"numbers": [1, 5, 12, 23, 34, 45]}'

# 4. Завершить тираж
curl -X POST http://localhost:8080/draws/1/complete

# 5. Проверить билет
curl http://localhost:8080/tickets/1
```

## Реализованный сценарий

Базовая лотерея «N из M»:

- администратор создаёт тираж;
- пользователь видит список активных тиражей и покупает билет с уникальной комбинацией чисел;
- после завершения тиража система случайно определяет выигрышную комбинацию;
- статусы всех билетов обновляются на `WIN` или `LOSE`;
- пользователь проверяет результат по ID билета.
# practicum-mephi

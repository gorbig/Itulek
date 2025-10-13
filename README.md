# SupWork Backend - Микросервисная архитектура

Multi-module Spring Boot проект для платформы поиска мастеров SupWork.

## Архитектура

```
┌─────────────────┐
│   API Gateway   │  :8080
│   (Routing)     │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
┌───▼────┐ ┌─▼──────────┐
│ Eureka │ │User Service│ :8081
│ Server │ │(Auth/Users)│
│ :8761  │ └────────────┘
└────────┘
```

## Модули

### 1. supwork-eureka-server
Service Discovery сервер для регистрации микросервисов.

- **Port**: 8761
- **UI**: http://localhost:8761

### 2. supwork-api-gateway
Точка входа для всех клиентских запросов с маршрутизацией и JWT фильтрацией.

- **Port**: 8080
- **Routes**:
  - `/user/**` → user-service
  - `/gig/**` → gig-service (future)
  - `/search/**` → search-service (future)

### 3. supwork-user-service
Микросервис аутентификации и управления пользователями.

- **Port**: 8081
- **Features**:
  - JWT аутентификация
  - Регистрация пользователей
  - Role-based access (TECHNICIAN/CLIENT)
  - BCrypt password hashing

## Технологический стек

- **Java**: 17
- **Spring Boot**: 3.5.6
- **Spring Cloud**: 2024.0.0
- **Database**: PostgreSQL
- **Security**: Spring Security + JWT
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway

## Быстрый старт

### Требования

- Java 17+
- Maven 3.8+
- PostgreSQL 13+
- Docker (опционально)

### Сборка проекта

```bash
mvn clean install
```

### Запуск с Maven

```bash
# 1. Запустите Eureka Server
mvn spring-boot:run -pl supwork-eureka-server

# 2. Запустите API Gateway
mvn spring-boot:run -pl supwork-api-gateway

# 3. Создайте БД и запустите User Service
createdb supworkdb
mvn spring-boot:run -pl supwork-user-service
```

### Запуск с Docker Compose

```bash
# Соберите все модули
mvn clean package

# Запустите все сервисы
docker-compose up -d

# Просмотр логов
docker-compose logs -f

# Остановка
docker-compose down
```

## API Примеры

### Регистрация пользователя

```bash
curl -X POST http://localhost:8080/user/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "tech@example.com",
    "password": "password123",
    "role": "TECHNICIAN",
    "skills": ["plumbing", "electrical"]
  }'
```

### Логин

```bash
curl -X POST http://localhost:8080/user/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "tech@example.com",
    "password": "password123"
  }'
```

Ответ:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "userId": 1,
  "email": "tech@example.com",
  "role": "TECHNICIAN"
}
```

### Получение профиля

```bash
curl -X GET http://localhost:8080/user/users/1/profile \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## Конфигурация

### application.yml шаблоны

Каждый модуль имеет свой `application.yml` со следующими основными настройками:

- **Ports**: 8761 (Eureka), 8080 (Gateway), 8081 (User Service)
- **Eureka**: http://localhost:8761/eureka/
- **Database**: PostgreSQL на localhost:5432

### JWT Configuration

Секретный ключ JWT настраивается в `supwork-user-service/src/main/resources/application.yml`:

```yaml
jwt:
  secret: your-production-secret-key-min-256-bits
```

⚠️ **Важно**: Измените секретный ключ в production!

## Структура проекта

```
Itulek/
├── pom.xml                          # Parent POM
├── docker-compose.yml               # Docker orchestration
│
├── supwork-eureka-server/           # Service Discovery
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/...
│
├── supwork-api-gateway/             # API Gateway
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/...
│
└── supwork-user-service/            # User & Auth Service
    ├── pom.xml
    ├── Dockerfile
    ├── README.md
    └── src/
        ├── main/java/com/supwork/user/
        │   ├── entity/              # User, Role
        │   ├── repository/          # JPA Repositories
        │   ├── dto/                 # Data Transfer Objects
        │   ├── service/             # Business Logic
        │   ├── controller/          # REST Controllers
        │   ├── security/            # JWT Filter
        │   └── config/              # Spring Config
        └── test/...                 # Unit Tests
```

## Мониторинг

### Actuator Endpoints

- Eureka: http://localhost:8761/actuator/health
- Gateway: http://localhost:8080/actuator/health
- User Service: http://localhost:8081/actuator/health

### Eureka Dashboard

http://localhost:8761 - просмотр зарегистрированных сервисов

## Тестирование

```bash
# Все тесты
mvn test

# Конкретный модуль
mvn test -pl supwork-user-service
```

## Roadmap

- [ ] Gig Service (управление заказами)
- [ ] Search Service (поиск мастеров)
- [ ] Notification Service (уведомления)
- [ ] Rating Service (отзывы и рейтинги)
- [ ] Payment Service (платежи)

## Разработка

### Добавление нового микросервиса

1. Добавьте `<module>` в root `pom.xml`
2. Создайте директорию модуля
3. Создайте `pom.xml` с parent `../pom.xml`
4. Добавьте `spring-cloud-starter-netflix-eureka-client`
5. Добавьте `@EnableDiscoveryClient` в main класс
6. Настройте `application.yml` с Eureka URL

### Database Migrations

Рекомендуется использовать Flyway или Liquibase для управления миграциями БД в production.

## Лицензия

Private project

## Контакты

SupWork Development Team


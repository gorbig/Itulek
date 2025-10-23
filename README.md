# SupWork Backend

Микросервисная платформа для поиска мастеров на базе Spring Boot + Spring Cloud.

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
  - `/gig/**` → gig-service
  - `/search/**` → search-service (future)

### 3. supwork-user-service
Микросервис аутентификации и управления пользователями.

- **Port**: 8081
- **Features**:
  - JWT аутентификация
  - Регистрация пользователей
  - Role-based access (TECHNICIAN/CLIENT)
  - BCrypt password hashing

### 4. supwork-gig-service
Микросервис управления заказами (gigs).

- **Port**: 8082
- **Features**:
  - CRUD операции для заказов
  - Назначение заказов мастерам
  - Интеграция с user-service через Feign
  - JWT аутентификация

## Технологический стек

- **Java**: 17
- **Spring Boot**: 3.5.6
- **Spring Cloud**: 2024.0.0
- **Database**: PostgreSQL
- **Security**: Spring Security + JWT
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway

## 🚀 Быстрый старт (Docker Compose)

### Требования
- Docker и Docker Compose
- Maven 3.8+ (для сборки)

### Запуск ВСЕХ сервисов одной командой

```bash
# 1. Соберите проект
mvn clean package -DskipTests

# 2. Запустите все сервисы
docker-compose up -d

# 3. Просмотр логов
docker-compose logs -f

# 4. Остановка всех сервисов
docker-compose down
```

### Запуск для разработки (локально)

```bash
# 1. Запустите только PostgreSQL
docker-compose up -d postgres

# 2. В отдельных терминалах:
mvn spring-boot:run -pl supwork-eureka-server
mvn spring-boot:run -pl supwork-api-gateway  
mvn spring-boot:run -pl supwork-user-service
mvn spring-boot:run -pl supwork-gig-service
```

## 🧪 Тестирование API

### Swagger UI (рекомендуется)

Откройте в браузере: **http://localhost:8081/swagger-ui/index.html**

1. Зарегистрируйте пользователя через `POST /users/register`
2. Залогиньтесь через `POST /auth/login` и скопируйте токен
3. Нажмите кнопку **🔓 Authorize** и вставьте: `Bearer ВАШ_ТОКЕН`
4. Тестируйте защищенные endpoints

### Быстрое тестирование через скрипт

```bash
./test-api.sh
```

### Примеры curl запросов

**Регистрация:**
```bash
curl -X POST http://localhost:8081/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "tech@example.com",
    "password": "pass123",
    "role": "TECHNICIAN",
    "skills": ["plumbing", "electrical"]
  }'
```

**Логин:**
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "tech@example.com",
    "password": "pass123"
  }'
```

**Профиль (с токеном):**
```bash
curl -X GET http://localhost:8081/users/1/profile \
  -H "Authorization: Bearer ВАШ_ТОКЕН"
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

### Полезные ссылки

| Сервис | URL | Описание |
|--------|-----|----------|
| 🔍 Eureka Dashboard | http://localhost:8761 | Зарегистрированные сервисы |
| 📘 Swagger UI (User) | http://localhost:8081/swagger-ui/index.html | User Service API |
| 📘 Swagger UI (Gig) | http://localhost:8082/swagger-ui.html | Gig Service API |
| 🌐 API Gateway | http://localhost:8080 | Точка входа |
| 💓 Health Check (User) | http://localhost:8081/actuator/health | Проверка здоровья User Service |
| 💓 Health Check (Gig) | http://localhost:8082/actuator/health | Проверка здоровья Gig Service |
| 🗄️ PostgreSQL | localhost:5433 | База данных |

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


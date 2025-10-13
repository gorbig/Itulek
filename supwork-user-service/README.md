# SupWork User Service

Микросервис для аутентификации и управления пользователями в платформе SupWork.

## Стек технологий

- **Spring Boot 3.5.6**
- **Spring Security** + JWT
- **Spring Data JPA**
- **PostgreSQL**
- **Eureka Client**
- **Java 17**

## Структура

```
com.supwork.user/
├── entity/          # User, Role
├── repository/      # UserRepository
├── dto/            # LoginRequest, JwtResponse, UserDTO, etc.
├── service/        # UserService, JwtUtil
├── controller/     # AuthController, UserController
├── security/       # JwtAuthenticationFilter
└── config/         # SecurityConfig, GlobalExceptionHandler
```

## API Endpoints

### Аутентификация (Public)

**POST** `/auth/login`
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**POST** `/auth/refresh`
```json
{
  "refreshToken": "your-refresh-token"
}
```

### Регистрация (Public)

**POST** `/users/register`
```json
{
  "email": "user@example.com",
  "password": "password123",
  "role": "CLIENT",
  "skills": ["plumbing", "electrical"]
}
```

### Профиль (Protected)

**GET** `/users/{id}/profile`

Требует JWT токен в заголовке:
```
Authorization: Bearer <access-token>
```

## Настройка

### PostgreSQL

Создайте базу данных:
```sql
CREATE DATABASE supworkdb;
```

Обновите `application.yml` при необходимости:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/supworkdb
    username: postgres
    password: postgres
```

### JWT Secret

Измените секретный ключ в production:
```yaml
jwt:
  secret: your-secret-key-min-256-bits
```

## Запуск

### С Maven
```bash
mvn spring-boot:run -pl supwork-user-service
```

### С Docker
```bash
cd supwork-user-service
mvn clean package
docker build -t supwork-user-service .
docker run -p 8081:8081 supwork-user-service
```

## Тестирование

```bash
mvn test -pl supwork-user-service
```

## Интеграция с другими сервисами

- **Eureka Server**: http://localhost:8761
- **API Gateway**: http://localhost:8080
- **User Service**: http://localhost:8081

Сервис автоматически регистрируется в Eureka и доступен через API Gateway по маршруту `/user/**`.


#!/bin/bash

# Цвета
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  SupWork Backend API Test Script      ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
echo ""

# Проверка доступности сервисов
check_service() {
    local name=$1
    local url=$2
    
    if curl -s -f "$url" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ $name доступен${NC}"
        return 0
    else
        echo -e "${RED}❌ $name недоступен ($url)${NC}"
        return 1
    fi
}

echo -e "${YELLOW}🔍 Проверка сервисов...${NC}"
check_service "PostgreSQL" "http://localhost:5433" || echo -e "${YELLOW}  (Запустите: docker-compose -f docker-compose-dev.yml up -d)${NC}"
check_service "User Service" "http://localhost:8081/actuator/health" || echo -e "${YELLOW}  (Запустите: mvn spring-boot:run -pl supwork-user-service)${NC}"

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}📝 Тест 1: Регистрация пользователя${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"

REGISTER_RESPONSE=$(curl -s -X POST http://localhost:8081/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "technician@supwork.com",
    "password": "test123",
    "role": "TECHNICIAN",
    "skills": ["plumbing", "electrical", "carpentry"]
  }')

echo "$REGISTER_RESPONSE" | jq '.' 2>/dev/null || echo "$REGISTER_RESPONSE"

if echo "$REGISTER_RESPONSE" | grep -q "id"; then
    echo -e "${GREEN}✅ Регистрация успешна${NC}"
else
    echo -e "${YELLOW}⚠️  Пользователь уже существует или ошибка${NC}"
fi

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}🔐 Тест 2: Логин (получение JWT токена)${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"

LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "technician@supwork.com",
    "password": "test123"
  }')

echo "$LOGIN_RESPONSE" | jq '.' 2>/dev/null || echo "$LOGIN_RESPONSE"

# Извлечение токена
ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken' 2>/dev/null)
USER_ID=$(echo "$LOGIN_RESPONSE" | jq -r '.userId' 2>/dev/null)

if [ "$ACCESS_TOKEN" != "null" ] && [ ! -z "$ACCESS_TOKEN" ]; then
    echo -e "${GREEN}✅ Логин успешен${NC}"
    echo -e "${BLUE}📋 Access Token: ${NC}${ACCESS_TOKEN:0:50}..."
    echo -e "${BLUE}👤 User ID: ${NC}$USER_ID"
else
    echo -e "${RED}❌ Ошибка логина${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}👤 Тест 3: Получение профиля (Protected Endpoint)${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"

PROFILE_RESPONSE=$(curl -s -X GET "http://localhost:8081/users/$USER_ID/profile" \
  -H "Authorization: Bearer $ACCESS_TOKEN")

echo "$PROFILE_RESPONSE" | jq '.' 2>/dev/null || echo "$PROFILE_RESPONSE"

if echo "$PROFILE_RESPONSE" | grep -q "email"; then
    echo -e "${GREEN}✅ Профиль получен успешно${NC}"
else
    echo -e "${RED}❌ Ошибка получения профиля${NC}"
fi

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}🔄 Тест 4: Refresh Token${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"

REFRESH_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.refreshToken' 2>/dev/null)

REFRESH_RESPONSE=$(curl -s -X POST http://localhost:8081/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\": \"$REFRESH_TOKEN\"}")

echo "$REFRESH_RESPONSE" | jq '.' 2>/dev/null || echo "$REFRESH_RESPONSE"

if echo "$REFRESH_RESPONSE" | grep -q "accessToken"; then
    echo -e "${GREEN}✅ Token обновлен успешно${NC}"
else
    echo -e "${RED}❌ Ошибка обновления токена${NC}"
fi

echo ""
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}🌐 Тест 5: Через API Gateway${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"

GATEWAY_LOGIN=$(curl -s -X POST http://localhost:8080/user/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "technician@supwork.com",
    "password": "test123"
  }')

echo "$GATEWAY_LOGIN" | jq '.' 2>/dev/null || echo "$GATEWAY_LOGIN"

if echo "$GATEWAY_LOGIN" | grep -q "accessToken"; then
    echo -e "${GREEN}✅ API Gateway работает${NC}"
else
    echo -e "${YELLOW}⚠️  API Gateway недоступен (запустите: mvn spring-boot:run -pl supwork-api-gateway)${NC}"
fi

echo ""
echo -e "${GREEN}╔════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║      Все тесты завершены! 🎉           ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════╝${NC}"
echo ""
echo -e "${BLUE}📘 Swagger UI: ${NC}http://localhost:8081/swagger-ui.html"
echo -e "${BLUE}🔍 Eureka Dashboard: ${NC}http://localhost:8761"
echo ""


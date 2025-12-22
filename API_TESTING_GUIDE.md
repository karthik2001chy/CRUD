# API Testing Guide - All Authentication Methods

This guide provides ready-to-use examples for testing all authentication methods supported by the application.

---

## Table of Contents
1. [JWT Authentication](#jwt-authentication)
2. [Basic Authentication](#basic-authentication)
3. [API Key Authentication](#api-key-authentication)
4. [Multi-Factor Authentication (MFA)](#multi-factor-authentication-mfa)
5. [Testing with cURL](#testing-with-curl)
6. [Testing with Postman](#testing-with-postman)

---

## JWT Authentication

### Overview
- **Method**: Token-based authentication
- **Header**: `Authorization: Bearer <token>`
- **Use Case**: Mobile apps, Single Page Applications (SPAs), Microservices
- **Stateless**: Yes (no server-side session needed)

### Step 1: Login and Get Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "registrationNo": "S001",
    "password": "S001"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJTMDAxIiwicm9sZSI6IlNFTklPUiIsImlhdCI6MTcwMzMzOTIwMCwiZXhwIjoxNzAzNDI1NjAwfQ.abc123xyz789",
  "message": "Authentication successful",
  "role": "SENIOR"
}
```

### Step 2: Use Token in Requests

**Get All Students (SENIOR role required):**
```bash
curl -X GET http://localhost:8080/api/students \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJTMDAxIiwicm9sZSI6IlNFTklPUiIsImlhdCI6MTcwMzMzOTIwMCwiZXhwIjoxNzAzNDI1NjAwfQ.abc123xyz789"
```

**Get Simple Students (JUNIOR role required):**
```bash
curl -X GET http://localhost:8080/api/students/simple \
  -H "Authorization: Bearer <your_token_here>"
```

---

## Basic Authentication

### Overview
- **Method**: Credentials encoded in Base64
- **Header**: `Authorization: Basic <base64(username:password)>`
- **Use Case**: Server-to-server communication, Legacy systems
- **Security Note**: Must use HTTPS in production

### Encoding Credentials

```bash
# For credentials S001:S001
echo -n "S001:S001" | base64
# Output: UzAwMTpTMDAx
```

### Making Request with Basic Auth

```bash
# Method 1: Using pre-encoded Base64
curl -X GET http://localhost:8080/api/students \
  -H "Authorization: Basic UzAwMTpTMDAx"

# Method 2: Let curl do the encoding (recommended)
curl -X GET http://localhost:8080/api/students \
  -u S001:S001

# Method 3: Using bash to encode
curl -X GET http://localhost:8080/api/students \
  -H "Authorization: Basic $(echo -n 'S001:S001' | base64)"
```

### Testing Different Roles

```bash
# SENIOR role (registrationNo > 2002 birth year)
curl -X GET http://localhost:8080/api/students \
  -u S001:S001

# JUNIOR role (registrationNo <= 2002 birth year)
curl -X GET http://localhost:8080/api/students/simple \
  -u S002:S002
```

### Advantages & Disadvantages

✅ **Advantages:**
- Simple to implement
- No token generation needed
- Built-in HTTP standard
- Good for server-to-server communication

❌ **Disadvantages:**
- Credentials sent with every request
- Not secure over HTTP (must use HTTPS)
- Cannot revoke access without password change
- Not suitable for public APIs

---

## API Key Authentication

### Overview
- **Method**: Unique key-based authentication
- **Headers/Params**: `X-API-Key` header or `api_key` query parameter
- **Use Case**: Public APIs, Third-party integrations
- **Security Note**: Keep API keys secret, rotate regularly

### Test API Keys

```
key_12345_abc  (Student S001, SENIOR role)
key_67890_xyz  (Student S002, JUNIOR role)
```

### Using API Key in Header

```bash
# SENIOR access
curl -X GET http://localhost:8080/api/students \
  -H "X-API-Key: key_12345_abc"

# JUNIOR access
curl -X GET http://localhost:8080/api/students/simple \
  -H "X-API-Key: key_67890_xyz"
```

### Using API Key in Query Parameter

```bash
# Method 1: SENIOR access
curl -X GET "http://localhost:8080/api/students?api_key=key_12345_abc"

# Method 2: JUNIOR access
curl -X GET "http://localhost:8080/api/students/simple?api_key=key_67890_xyz"
```

### Testing Invalid API Key

```bash
# This should fail with 403 Forbidden
curl -X GET http://localhost:8080/api/students \
  -H "X-API-Key: invalid_key_xyz"
```

### Advantages & Disadvantages

✅ **Advantages:**
- Very simple to implement
- Good for public APIs
- Easy to track API usage
- Can be revoked per key

❌ **Disadvantages:**
- Less secure than JWT (no expiration by default)
- Must be kept secret
- All requests with same key have same permissions
- Cannot encode user info like JWT

---

## Multi-Factor Authentication (MFA)

### Overview
- **Method**: Two-step authentication (password + OTP)
- **Secure**: Yes (even if password is compromised)
- **Use Case**: High-security operations, Sensitive data
- **OTP Validity**: 5 minutes, 6-digit code

### Step 1: Initiate MFA Login

Send credentials to initiate authentication. System generates OTP and sends to registered device.

```bash
curl -X POST http://localhost:8080/api/auth/mfa/login \
  -H "Content-Type: application/json" \
  -d '{
    "registrationNo": "S001",
    "password": "S001"
  }'
```

**Response:**
```json
{
  "sessionToken": "mfa_session_1703339200000_123456",
  "message": "OTP has been sent to your registered email/phone. Please verify to complete authentication.",
  "expiryInSeconds": 300
}
```

**Console Output (for testing):**
```
═══════════════════════════════════════════════════════
📧 OTP SENT TO USER: S001
OTP: 123456
Valid for: 5 minutes
═══════════════════════════════════════════════════════
```

### Step 2: Verify OTP

Submit the OTP received on your device along with session token.

```bash
curl -X POST http://localhost:8080/api/auth/mfa/verify \
  -H "Content-Type: application/json" \
  -d '{
    "sessionToken": "mfa_session_1703339200000_123456",
    "otp": "123456"
  }'
```

**Response (Success):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJTMDAxIiwicm9sZSI6IlNFTklPUiIsImlhdCI6MTcwMzMzOTIwMCwiZXhwIjoxNzAzNDI1NjAwfQ.abc123xyz789",
  "message": "MFA authentication successful",
  "role": "SENIOR"
}
```

### Using MFA Token

Once you have the token, use it like JWT authentication:

```bash
curl -X GET http://localhost:8080/api/students \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJTMDAxIiwicm9sZSI6IlNFTklPUiIsImlhdCI6MTcwMzMzOTIwMCwiZXhwIjoxNzAzNDI1NjAwfQ.abc123xyz789"
```

### Error Scenarios

**Invalid OTP:**
```bash
curl -X POST http://localhost:8080/api/auth/mfa/verify \
  -H "Content-Type: application/json" \
  -d '{
    "sessionToken": "mfa_session_1703339200000_123456",
    "otp": "000000"
  }'
```

**Response (Error):**
```json
{
  "token": null,
  "message": "MFA verification failed: Invalid OTP. Attempts remaining: 2",
  "role": null
}
```

**Expired OTP:**
```bash
# Wait 5+ minutes and try to verify
curl -X POST http://localhost:8080/api/auth/mfa/verify \
  -H "Content-Type: application/json" \
  -d '{
    "sessionToken": "mfa_session_1703339200000_123456",
    "otp": "123456"
  }'
```

**Response (Error):**
```json
{
  "token": null,
  "message": "MFA verification failed: OTP has expired",
  "role": null
}
```

### Advantages & Disadvantages

✅ **Advantages:**
- Very secure (even if password stolen)
- Prevents unauthorized access
- Meets compliance requirements
- Best for sensitive data

❌ **Disadvantages:**
- More complex setup
- Requires additional infrastructure (SMS/email)
- User friction (slower login)
- Support overhead

---

## Testing with cURL

### Basic Setup

```bash
# Test server availability
curl http://localhost:8080/api/auth/health

# Get authentication methods info
curl http://localhost:8080/api/auth/methods
```

### Creating a Test Script

Save as `test_auth.sh`:

```bash
#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080/api"

echo -e "${YELLOW}═══ Testing JWT Authentication ═══${NC}"
TOKEN=$(curl -s -X POST $BASE_URL/auth/login \
  -H "Content-Type: application/json" \
  -d '{"registrationNo":"S001","password":"S001"}' | jq -r '.token')

if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
  echo -e "${GREEN}✓ JWT Token obtained: ${TOKEN:0:30}...${NC}"
else
  echo -e "${RED}✗ Failed to obtain JWT token${NC}"
fi

echo -e "\n${YELLOW}═══ Testing API Key Authentication ═══${NC}"
RESPONSE=$(curl -s -X GET $BASE_URL/students \
  -H "X-API-Key: key_12345_abc")

if echo $RESPONSE | jq . > /dev/null 2>&1; then
  echo -e "${GREEN}✓ API Key authentication successful${NC}"
else
  echo -e "${RED}✗ API Key authentication failed${NC}"
fi

echo -e "\n${YELLOW}═══ Testing Basic Authentication ═══${NC}"
RESPONSE=$(curl -s -X GET $BASE_URL/students \
  -u S001:S001)

if echo $RESPONSE | jq . > /dev/null 2>&1; then
  echo -e "${GREEN}✓ Basic authentication successful${NC}"
else
  echo -e "${RED}✗ Basic authentication failed${NC}"
fi

echo -e "\n${YELLOW}═══ Testing MFA Authentication ═══${NC}"
SESSION=$(curl -s -X POST $BASE_URL/auth/mfa/login \
  -H "Content-Type: application/json" \
  -d '{"registrationNo":"S001","password":"S001"}' | jq -r '.sessionToken')

if [ -n "$SESSION" ] && [ "$SESSION" != "null" ]; then
  echo -e "${GREEN}✓ MFA session created: ${SESSION:0:30}...${NC}"
  echo -e "${YELLOW}Check console for OTP, then verify with:${NC}"
  echo "curl -X POST $BASE_URL/auth/mfa/verify \\"
  echo "  -H 'Content-Type: application/json' \\"
  echo "  -d '{\"sessionToken\":\"$SESSION\",\"otp\":\"<OTP>\"}'"
else
  echo -e "${RED}✗ Failed to initiate MFA${NC}"
fi
```

Run the script:
```bash
chmod +x test_auth.sh
./test_auth.sh
```

---

## Testing with Postman

### Import Collection

Create a Postman collection with these requests:

### 1. JWT Login

```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "registrationNo": "S001",
  "password": "S001"
}
```

**Save response token as variable:**
- In Tests tab, add:
```javascript
var jsonData = pm.response.json();
pm.environment.set("jwt_token", jsonData.token);
```

### 2. JWT Protected Request

```
GET http://localhost:8080/api/students
Authorization: Bearer {{jwt_token}}
```

### 3. Basic Auth

```
GET http://localhost:8080/api/students
Authorization: Basic UzAwMTpTMDAx
```

Or use Postman's Auth tab:
- Type: Basic Auth
- Username: S001
- Password: S001

### 4. API Key Auth

```
GET http://localhost:8080/api/students
X-API-Key: key_12345_abc
```

### 5. MFA Login

```
POST http://localhost:8080/api/auth/mfa/login
Content-Type: application/json

{
  "registrationNo": "S001",
  "password": "S001"
}
```

Save session token:
```javascript
var jsonData = pm.response.json();
pm.environment.set("mfa_session", jsonData.sessionToken);
```

### 6. MFA Verify

```
POST http://localhost:8080/api/auth/mfa/verify
Content-Type: application/json

{
  "sessionToken": "{{mfa_session}}",
  "otp": "123456"
}
```

---

## Troubleshooting

### Issue: "Invalid credentials"
- Check if registrationNo and password are correct
- Verify student exists in database
- Current implementation uses registrationNo as password

### Issue: "Token expired"
- JWT tokens expire after 24 hours (configurable)
- Login again to get a new token

### Issue: "Invalid role"
- Ensure student has correct birth year for role determination
- JUNIOR: born after 2002
- SENIOR: born in 2002 or before

### Issue: "OTP expired"
- OTP is valid for 5 minutes only
- Request a new OTP by calling MFA login again

### Issue: "Maximum OTP attempts exceeded"
- You made 3 incorrect OTP attempts
- Request a new OTP by calling MFA login again

---

## Security Best Practices

1. **Always use HTTPS in production**
   - All authentication methods require HTTPS
   - Never transmit credentials over HTTP

2. **Secure token storage**
   - Store JWT tokens securely on client
   - Use secure cookies for session tokens
   - Never store in local storage if possible

3. **API Key management**
   - Rotate API keys regularly
   - Use different keys per application
   - Store keys in environment variables, never in code

4. **Password security**
   - Use strong passwords (8+ characters)
   - Use password hashing (BCrypt)
   - Implement password reset functionality

5. **Rate limiting**
   - Limit login attempts (3-5 attempts)
   - Implement exponential backoff
   - Block suspicious IP addresses

6. **Audit logging**
   - Log all authentication attempts
   - Log successful and failed logins
   - Monitor for suspicious patterns

---

## Configuration

Update `application.properties`:

```properties
# JWT Configuration
app.jwt.secret=your_super_secret_key_at_least_32_characters_long_should_not_be_in_production
app.jwt.expiration=86400000  # 24 hours in milliseconds

# Session Configuration
server.servlet.session.timeout=30m

# Server Configuration
server.port=8080
server.servlet.context-path=/

# Database Configuration
spring.jpa.hibernate.ddl-auto=update
spring.datasource.url=jdbc:mysql://localhost:3306/student_db
spring.datasource.username=root
spring.datasource.password=password
```



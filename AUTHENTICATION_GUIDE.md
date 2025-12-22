# Spring Security Authentication Types - Complete Guide

## Overview
This guide explains all authentication methods supported in this Spring Security application.

---

## 1. **JWT (JSON Web Token) Authentication** ✅ IMPLEMENTED
### How it Works:
1. Client sends credentials to `/api/auth/login` endpoint
2. Server validates credentials and generates a JWT token
3. Client includes token in `Authorization: Bearer <token>` header for subsequent requests
4. Server validates token in JwtAuthenticationFilter before processing request
5. Token contains embedded claims (registrationNo, role) that don't require database lookup

### Workflow:
```
Client Credentials → Server Validation → JWT Token Generated → Stored in Client
→ Sent in Every Request → Token Validated → Access Granted/Denied
```

### Example Usage:
```bash
# Step 1: Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"registrationNo":"S001","password":"S001"}'

# Response:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "message": "Authentication successful",
  "role": "SENIOR"
}

# Step 2: Use token in subsequent requests
curl -X GET http://localhost:8080/api/students \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Advantages:
- ✅ Stateless (no server-side session storage needed)
- ✅ Scalable for microservices
- ✅ Works well with mobile apps and SPAs
- ✅ CORS friendly

### Disadvantages:
- ❌ Cannot revoke tokens immediately (until expiration)
- ❌ Larger payload size than simple tokens
- ❌ If secret is compromised, all tokens are compromised

### Implementation Files:
- `JwtUtil.java` - Token generation and validation
- `JwtAuthenticationFilter.java` - Filter to extract and validate tokens
- `AuthenticationService.java` - Business logic for authentication
- `AuthenticationController.java` - API endpoints

---

## 2. **Basic Authentication**
### How it Works:
1. Client sends username:password encoded in Base64
2. Included in `Authorization: Basic <base64(username:password)>` header
3. Server decodes and validates credentials
4. Creates authentication object if valid

### Workflow:
```
Username:Password → Base64 Encode → Sent in Header
→ Server Decodes → Validates → Authentication Created → Access Granted
```

### Example Usage:
```bash
curl -X GET http://localhost:8080/api/students \
  -H "Authorization: Basic UzAwMTpTMDAx"
  # Base64("S001:S001") = "UzAwMTpTMDAx"
```

### Advantages:
- ✅ Simple to implement
- ✅ No token generation needed
- ✅ Built-in HTTP standard
- ✅ Good for server-to-server communication

### Disadvantages:
- ❌ Credentials sent with every request
- ❌ Not secure over HTTP (must use HTTPS)
- ❌ Cannot revoke access without password change
- ❌ Not suitable for public APIs

---

## 3. **API Key Authentication**
### How it Works:
1. Client is issued a unique API key
2. Client sends key in header or query parameter
3. Server validates key and grants access
4. Simple but less secure than JWT

### Workflow:
```
Generate API Key → Store in Client → Send in Every Request
→ Server Validates → Access Granted/Denied
```

### Example Usage:
```bash
curl -X GET http://localhost:8080/api/students \
  -H "X-API-Key: abc123xyz789"
```

### Advantages:
- ✅ Very simple to implement
- ✅ Good for public APIs
- ✅ Easy to track API usage
- ✅ Can be revoked per key

### Disadvantages:
- ❌ Less secure than JWT (no expiration by default)
- ❌ Must be kept secret
- ❌ All requests with same key have same permissions
- ❌ Cannot encode user info like JWT

---

## 4. **Session-Based Authentication (Form Login)**
### How it Works:
1. Client submits login form with credentials
2. Server validates and creates a session
3. Session ID stored in cookie (JSESSIONID)
4. Cookie automatically sent with every request
5. Server validates session before allowing access

### Workflow:
```
Form Submission → Server Validates → Session Created
→ Cookie Set in Response → Cookie Sent Automatically
→ Server Validates Session → Access Granted
```

### Example Usage:
```bash
# Step 1: Login (server creates session)
curl -c cookies.txt -X POST http://localhost:8080/login \
  -d "username=S001&password=S001"

# Step 2: Use cookie automatically
curl -b cookies.txt -X GET http://localhost:8080/api/students
```

### Advantages:
- ✅ Traditional and well-understood
- ✅ Automatic cookie handling
- ✅ Server-side revocation possible
- ✅ CSRF protection easier to implement

### Disadvantages:
- ❌ Requires server-side session storage
- ❌ Not scalable for distributed systems
- ❌ Cookie limitations (same-domain)
- ❌ Not suitable for mobile apps

---

## 5. **OAuth2 Authentication (Google/GitHub)**
### How it Works:
1. User clicks "Login with Google/GitHub" button
2. Redirected to OAuth2 provider's login page
3. After successful login, provider redirects back with authorization code
4. Server exchanges code for access token
5. Server uses token to get user info from provider
6. Create/update local user and establish session

### Workflow:
```
User → OAuth Provider Login → Authorization Code
→ Exchange for Access Token → Get User Info
→ Create Local Session → Redirect to App
```

### Example Usage:
```bash
# User clicks this link:
http://localhost:8080/oauth2/authorization/google

# After authorization:
# Server automatically creates session and redirects to dashboard
```

### Advantages:
- ✅ No password management needed
- ✅ Users already have accounts with providers
- ✅ Integrates with existing services
- ✅ Better security (delegation)

### Disadvantages:
- ❌ Requires external service availability
- ❌ More complex setup
- ❌ Depends on provider's terms
- ❌ User data controlled by third party

---

## 6. **Multi-Factor Authentication (MFA)**
### How it Works:
1. User provides first factor (password/JWT)
2. Server validates and sends OTP/code to secondary factor (SMS/email/authenticator)
3. User provides second factor
4. Server validates both factors
5. Session/token created only after both factors verified

### Workflow:
```
Password → Server Sends OTP → User Enters OTP
→ Server Validates Both → Authentication Successful
```

### Example Usage:
```bash
# Step 1: Submit password
curl -X POST http://localhost:8080/api/auth/mfa/login \
  -H "Content-Type: application/json" \
  -d '{"registrationNo":"S001","password":"S001"}'

# Response: OTP sent to registered email/phone
{
  "message": "OTP sent to your registered device",
  "sessionToken": "temp_token_123"
}

# Step 2: Verify OTP
curl -X POST http://localhost:8080/api/auth/mfa/verify \
  -H "Content-Type: application/json" \
  -d '{"sessionToken":"temp_token_123","otp":"123456"}'

# Response: Final JWT token
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "message": "Authentication successful"
}
```

### Advantages:
- ✅ Very secure (even if password stolen)
- ✅ Prevents unauthorized access
- ✅ Meets compliance requirements
- ✅ Best for sensitive data

### Disadvantages:
- ❌ More complex setup
- ❌ Requires additional infrastructure (SMS/email)
- ❌ User friction (slower login)
- ❌ Support overhead

---

## Comparison Table

| Feature | JWT | Basic | API Key | Session | OAuth2 | MFA |
|---------|-----|-------|---------|---------|--------|-----|
| Stateless | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ |
| Revocable | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Mobile Friendly | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ |
| Scalable | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ |
| Simple | ✅ | ✅✅ | ✅✅ | ✅ | ❌ | ❌ |
| Secure | ✅ | ❌ | ❌ | ✅ | ✅ | ✅✅ |
| Best Use Case | Mobile/SPA | Server-to-Server | Public API | Web Apps | Modern Apps | Sensitive Data |

---

## Implementation Status in Your Project

| Authentication Type | Status | Implementation |
|-------------------|--------|-----------------|
| JWT | ✅ COMPLETE | JwtUtil, JwtAuthenticationFilter, AuthenticationService |
| Basic Auth | 📋 TODO | BasicAuthenticationFilter (to be added) |
| API Key | 📋 TODO | ApiKeyAuthenticationFilter (to be added) |
| Session-Based | 📋 TODO | Form login configuration (to be added) |
| OAuth2 | 📋 TODO | OAuth2 configuration (to be added) |
| MFA | 📋 TODO | MfaAuthenticationFilter (to be added) |

---

## Security Best Practices

1. **Always use HTTPS** - Never transmit credentials over HTTP
2. **JWT Secret Management** - Store in environment variables, not code
3. **Token Expiration** - Set appropriate expiration times
4. **Password Hashing** - Use BCrypt, not plain text (current code needs fix)
5. **CORS Configuration** - Restrict to trusted domains
6. **Rate Limiting** - Prevent brute force attacks
7. **Audit Logging** - Log authentication attempts
8. **Regular Updates** - Keep dependencies updated for security patches

---

## Configuration

### application.properties
```properties
# JWT Configuration
app.jwt.secret=your_super_secret_key_at_least_32_characters_long
app.jwt.expiration=86400000  # 24 hours in milliseconds

# Session Configuration (if using session-based auth)
server.servlet.session.timeout=30m

# CORS Configuration
cors.allowedOrigins=http://localhost:3000,http://localhost:8080
cors.allowedMethods=GET,POST,PUT,DELETE,OPTIONS
cors.allowedHeaders=*

# OAuth2 Configuration (if using OAuth2)
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
```

---

## Next Steps

1. ✅ Review current JWT implementation
2. Add Basic Authentication support
3. Add API Key authentication
4. Implement MFA for sensitive operations
5. Add rate limiting and audit logging
6. Implement OAuth2 with Google/GitHub
7. Set up proper password hashing with BCrypt
8. Configure CORS properly



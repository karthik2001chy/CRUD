# Quick Reference Card - All Authentication Methods

## 🚀 Quick Start (Copy-Paste Ready)

### 1. Start Server
```bash
cd /Users/karthik/IdeaProjects/NewSpringSecurity
mvn spring-boot:run
```

### 2. Test Each Method

#### **JWT Authentication**
```bash
# Get token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"registrationNo":"S001","password":"S001"}' | jq -r '.token')

# Use token
curl -X GET http://localhost:8080/api/students \
  -H "Authorization: Bearer $TOKEN"
```

#### **Basic Authentication**
```bash
curl -X GET http://localhost:8080/api/students \
  -u S001:S001
```

#### **API Key Authentication**
```bash
curl -X GET http://localhost:8080/api/students \
  -H "X-API-Key: key_12345_abc"
```

#### **Multi-Factor Authentication (MFA)**
```bash
# Step 1: Get OTP
SESSION=$(curl -s -X POST http://localhost:8080/api/auth/mfa/login \
  -H "Content-Type: application/json" \
  -d '{"registrationNo":"S001","password":"S001"}' | jq -r '.sessionToken')

# Check console for OTP (will be printed there)

# Step 2: Verify OTP
MFA_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/mfa/verify \
  -H "Content-Type: application/json" \
  -d "{\"sessionToken\":\"$SESSION\",\"otp\":\"123456\"}" | jq -r '.token')

# Use token
curl -X GET http://localhost:8080/api/students \
  -H "Authorization: Bearer $MFA_TOKEN"
```

---

## 📚 Method Comparison

| Method | Header | Speed | Security | Use Case |
|--------|--------|-------|----------|----------|
| **JWT** | `Bearer <token>` | Medium | Good | 📱 Mobile Apps, SPAs |
| **Basic** | `Basic <base64>` | Fast | Fair | 🖥️ Server-to-Server |
| **API Key** | `X-API-Key` | Fast | Fair | 🌐 Public APIs |
| **MFA** | `Bearer <token>` | Slow | Excellent | 🔐 High Security |

---

## 🔑 Test Credentials & Keys

```
Username/RegNo: S001    Password: S001    Role: SENIOR
Username/RegNo: S002    Password: S002    Role: JUNIOR

API Keys:
key_12345_abc (S001, SENIOR)
key_67890_xyz (S002, JUNIOR)

MFA:
OTP prints to console (for testing)
Valid for 5 minutes
```

---

## 📍 API Endpoints

### Public (No Auth Required)
```
GET  /api/auth/health          - Server health check
GET  /api/auth/methods         - List auth methods
POST /api/auth/login           - JWT login
POST /api/auth/mfa/login       - MFA step 1
POST /api/auth/mfa/verify      - MFA step 2
```

### Protected (SENIOR Role)
```
GET  /api/students             - Get all students
GET  /api/students/{id}        - Get student by ID
POST /api/students             - Create student
PUT  /api/students/{id}        - Update student
DELETE /api/students/{id}      - Delete student
```

### Protected (JUNIOR Role)
```
GET  /api/students/simple      - Get simple student list
GET  /api/students/simple/{id} - Get simple student info
```

---

## 🔐 Response Codes

```
✅ 200 OK              - Request successful
✅ 201 Created         - Resource created
❌ 400 Bad Request     - Invalid input
❌ 401 Unauthorized    - Missing/invalid auth
❌ 403 Forbidden       - Auth valid but no permission
❌ 404 Not Found       - Resource doesn't exist
❌ 500 Server Error    - Server error
```

---

## 🛠️ File Structure

```
src/main/java/com/students/crud/
│
├── config/
│   └── SecurityConfig.java          ← All filters configured here
│
├── controllers/
│   ├── AuthenticationController.java ← JWT endpoints
│   ├── EnhancedAuthenticationController.java ← All auth endpoints
│   └── StudentController.java
│
├── security/
│   ├── JwtUtil.java                 ← Token generation/validation
│   ├── JwtAuthenticationFilter.java  ← JWT filter
│   ├── BasicAuthenticationFilter.java ← Basic auth filter
│   ├── ApiKeyAuthenticationFilter.java ← API key filter
│   └── MfaManager.java              ← MFA logic
│
├── Services/
│   ├── AuthenticationService.java    ← Original auth service
│   └── EnhancedAuthenticationService.java ← All auth methods
│
├── DTO/
│   ├── AuthRequest.java
│   ├── AuthResponse.java
│   ├── MfaLoginRequest.java
│   ├── MfaOtpVerificationRequest.java
│   └── MfaOtpResponse.java
│
└── DAO/
    └── Student.java
```

---

## 📖 Documentation Files

- **AUTHENTICATION_GUIDE.md** - Detailed auth type explanations
- **API_TESTING_GUIDE.md** - Complete testing examples
- **ARCHITECTURE_DIAGRAMS.md** - Visual flow diagrams
- **IMPLEMENTATION_SUMMARY.md** - Overview and next steps
- **QUICK_REFERENCE.md** - This file

---

## ⚡ Common Issues & Solutions

### Issue: "Invalid credentials"
**Solution**: Check if student exists and credentials are S001:S001 format

### Issue: "Token expired"
**Solution**: JWT tokens expire after 24 hours. Call login again.

### Issue: "Access Denied (403)"
**Solution**: User role doesn't match endpoint requirement
- SENIOR needed for `/api/students/**`
- JUNIOR needed for `/api/students/simple/**`

### Issue: "Unauthorized (401)"
**Solution**: Missing or invalid authentication
- Add token/credentials to request header
- Ensure token is not expired
- Verify API key is active

### Issue: "OTP expired"
**Solution**: OTP is valid for 5 minutes only. Request new OTP.

### Issue: "Maximum OTP attempts exceeded"
**Solution**: You made 3 wrong attempts. Request new OTP.

---

## 🔄 Filter Chain Order (Why It Matters)

```
Request → API Key → Basic Auth → JWT → Form Login → Controller

1. API Key checked first (fastest, O(1))
2. Basic Auth second (still fast, O(1) with caching)
3. JWT third (parses and validates)
4. Form Login last (default Spring handler)

Early return if any filter authenticates successfully!
```

---

## 🎯 Authentication Decision Tree

```
Request arrives
    │
    ├─ Has X-API-Key header or api_key param?
    │  ├─ YES → ApiKeyAuthFilter processes
    │  └─ NO  → Continue
    │
    ├─ Has Authorization: Basic header?
    │  ├─ YES → BasicAuthFilter processes
    │  └─ NO  → Continue
    │
    ├─ Has Authorization: Bearer header?
    │  ├─ YES → JwtAuthFilter processes
    │  └─ NO  → Continue
    │
    ├─ Is POST /login with form data?
    │  ├─ YES → FormLoginFilter processes
    │  └─ NO  → User is ANONYMOUS
    │
    └─ Check authorization for endpoint
       ├─ ROLE required?
       │  ├─ User has role? → Allow (200)
       │  └─ User missing role? → Deny (403)
       └─ Public endpoint? → Allow (200)
```

---

## 🚨 Security Checklist

- [ ] Change `app.jwt.secret` to strong value
- [ ] Store JWT secret in environment variable, not code
- [ ] Enable HTTPS in production
- [ ] Implement BCrypt for password hashing
- [ ] Add rate limiting to auth endpoints
- [ ] Add audit logging for auth attempts
- [ ] Rotate API keys regularly
- [ ] Set JWT expiration to appropriate time
- [ ] Implement refresh token mechanism
- [ ] Test CORS configuration
- [ ] Set secure session cookie flags
- [ ] Implement account lockout after failed attempts
- [ ] Add monitoring/alerting for auth failures

---

## 📞 Need Help?

1. **For detailed explanations**: See `AUTHENTICATION_GUIDE.md`
2. **For testing examples**: See `API_TESTING_GUIDE.md`
3. **For architecture**: See `ARCHITECTURE_DIAGRAMS.md`
4. **For implementation details**: See `IMPLEMENTATION_SUMMARY.md`

---

## 🎓 Learning Path

1. ✅ Understand JWT Authentication (original implementation)
2. ✅ Learn Basic Authentication flow
3. ✅ Understand API Key authentication
4. ✅ Understand MFA (Multi-Factor Auth)
5. 📋 Implement Session-based auth (if needed)
6. 📋 Implement OAuth2 with Google/GitHub (if needed)
7. 🔐 Add password hashing (BCrypt) - CRITICAL
8. 🔐 Add rate limiting - IMPORTANT
9. 🔐 Add audit logging - IMPORTANT
10. ✨ Add refresh tokens - NICE TO HAVE

---

## 🌟 Key Concepts Explained Simply

### Token (JWT)
- **What**: A signed message containing user info
- **Why**: No server database lookup needed per request
- **How**: Cryptographic signature prevents tampering
- **When**: After user logs in successfully

### API Key
- **What**: A secret code given to a user/app
- **Why**: Simple authentication without login
- **How**: Server validates against stored keys
- **When**: For public APIs, automations

### MFA
- **What**: Two-step verification (password + OTP)
- **Why**: Much more secure than password alone
- **How**: OTP sent to user's device (SMS/Email/App)
- **When**: For sensitive operations, banking, healthcare

### Roles
- **What**: Permission levels (JUNIOR, SENIOR, ADMIN)
- **Why**: Different users can access different data
- **How**: Checked after authentication succeeds
- **When**: For every protected endpoint

### Filter Chain
- **What**: Multiple security checks in sequence
- **Why**: Try different auth methods automatically
- **How**: Each filter checks if it can authenticate
- **When**: For every incoming request

---

**Version**: 1.0  
**Last Updated**: December 23, 2025  
**Status**: ✅ Production Ready (after security fixes)

**Next Action**: Read AUTHENTICATION_GUIDE.md for detailed information!



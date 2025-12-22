# Authentication Architecture & Workflow Diagrams

## 1. Overall System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          Spring Security Application                         │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                    ┌─────────────────┼─────────────────┐
                    │                 │                 │
                    ▼                 ▼                 ▼
            ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
            │   Request    │  │   Request    │  │   Request    │
            │  with JWT    │  │  with Basic  │  │  with APIKey │
            │   Token      │  │   Auth       │  │              │
            └──────────────┘  └──────────────┘  └──────────────┘
                    │                 │                 │
                    └─────────────────┼─────────────────┘
                                      │
                            ┌─────────▼────────┐
                            │  Security Filter  │
                            │     Chain         │
                            └─────────┬────────┘
                                      │
            ┌─────────────────────────┼─────────────────────────┐
            │                         │                         │
            ▼                         ▼                         ▼
    ┌──────────────────┐   ┌──────────────────┐   ┌──────────────────┐
    │ ApiKeyAuthFilter │   │ BasicAuthFilter  │   │ JwtAuthFilter    │
    │ Checks X-API-Key │   │ Checks Basic     │   │ Checks Bearer    │
    │ Query Param      │   │ Authorization    │   │ Token            │
    └────────┬─────────┘   └────────┬─────────┘   └────────┬─────────┘
             │                      │                      │
             └──────────────────────┼──────────────────────┘
                                    │
                    ┌───────────────▼───────────────┐
                    │ Authentication Successful?    │
                    └───────────────┬───────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    │                               │
                    ▼ YES                           ▼ NO
        ┌─────────────────────┐          ┌─────────────────────┐
        │ Load User Roles     │          │ Set SecurityContext │
        │ from Claims/DB      │          │ with Anonymous      │
        └──────────┬──────────┘          └────────┬────────────┘
                   │                               │
                   └───────────────┬───────────────┘
                                   │
                       ┌───────────▼───────────┐
                       │ Check Authorization   │
                       │ (Role Requirements)   │
                       └───────────┬───────────┘
                                   │
                    ┌──────────────┴──────────────┐
                    │                             │
                    ▼ ALLOWED                     ▼ DENIED
        ┌─────────────────────────┐    ┌─────────────────────────┐
        │ Request Proceeds to     │    │ CustomAccessDenied      │
        │ Controller              │    │ Handler invoked         │
        │ (Return 200/201/etc)    │    │ (Return 403)            │
        └─────────────────────────┘    └─────────────────────────┘
```

---

## 2. JWT Authentication Flow

```
┌────────────────────────────────────────────────────────────────────────┐
│                        JWT Authentication Flow                          │
└────────────────────────────────────────────────────────────────────────┘

CLIENT SIDE                           SERVER SIDE

Step 1: Login
┌──────────────────┐
│ User clicks      │
│ "Login" button   │
│ Enters:          │
│ - Username       │
│ - Password       │
└────────┬─────────┘
         │
         │ POST /api/auth/login
         │ {registrationNo, password}
         │
         ├──────────────────────────────────────────────▶│
                                                         │
                                                    ┌────▼────┐
                                                    │ Validate│
                                                    │Creds    │
                                                    └────┬────┘
                                                         │
                                          ┌──────────────┴──────────────┐
                                          │                             │
                                     VALID │                             │ INVALID
                                          │                             │
                                          ▼                             ▼
                                    ┌───────────┐              ┌──────────────┐
                                    │ Determine │              │ Return 401   │
                                    │ Role from │              │ Unauthorized │
                                    │ DOB       │              └──────────────┘
                                    └─────┬─────┘
                                          │
                                    ┌─────▼──────┐
                                    │ Generate   │
                                    │ JWT Token  │
                                    │ Contains:  │
                                    │ - Subject  │
                                    │ - Role     │
                                    │ - Exp Time │
                                    └─────┬──────┘
                                          │
         ┌────────────────────────────────┤
         │                                 │
         │ Return 200                      │
         │ {token, role, message}          │
         │                                 │
◀────────┼─────────────────────────────────┤
         │
         │ Response received
         │ Token stored in client
         │ (localStorage/sessionStorage)
         │

Step 2: Subsequent Requests
┌────────────────────┐
│ User requests      │
│ protected resource │
│ e.g., /api/students
└────────┬───────────┘
         │
         │ GET /api/students
         │ Headers: {Authorization: Bearer <token>}
         │
         ├──────────────────────────────────────────────▶│
                                                         │
                                                    ┌────▼──────────┐
                                                    │ JwtAuth       │
                                                    │ Filter        │
                                                    │ Extracts token│
                                                    └────┬──────────┘
                                                         │
                                                    ┌────▼──────────┐
                                                    │ Validate      │
                                                    │ Token         │
                                                    │ (signature,   │
                                                    │  expiry)      │
                                                    └────┬──────────┘
                                                         │
                                        ┌────────────────┴────────────────┐
                                        │                                 │
                                   VALID│                                 │INVALID
                                        │                                 │
                                        ▼                                 ▼
                                   ┌─────────┐                    ┌──────────────┐
                                   │ Extract │                    │ Return 401   │
                                   │ Claims  │                    │ Unauthorized │
                                   │ (user,  │                    └──────────────┘
                                   │  role)  │
                                   └────┬────┘
                                        │
                                   ┌────▼──────────┐
                                   │ Create        │
                                   │ Authentication
                                   │ Object        │
                                   └────┬──────────┘
                                        │
                                   ┌────▼──────────┐
                                   │ Check Role    │
                                   │ Authorization │
                                   └────┬──────────┘
                                        │
                            ┌───────────┴───────────┐
                            │                       │
                       ALLOWED│                       │DENIED
                            │                       │
                            ▼                       ▼
                    ┌──────────────┐        ┌──────────────┐
                    │ Return 200   │        │ Return 403   │
                    │ Resource Data│        │ Forbidden    │
                    └──────────────┘        └──────────────┘
         ◀─────────────────────────────────────────────────────

Token Lifetime:
┌─────────────────────────────────────────┐
│ Issued at: 2025-12-23 10:00:00          │
│ Expires at: 2025-12-24 10:00:00         │
│ Duration: 24 hours                      │
│                                         │
│ After expiration:                       │
│ - Token becomes invalid                 │
│ - User must login again                 │
│ - New token is issued                   │
└─────────────────────────────────────────┘
```

---

## 3. Basic Authentication Flow

```
┌────────────────────────────────────────────────────────────────────────┐
│                    Basic Authentication Flow                            │
└────────────────────────────────────────────────────────────────────────┘

CLIENT SIDE                           SERVER SIDE

Step 1: Prepare Credentials
┌──────────────────────┐
│ Credentials:         │
│ - Username: S001     │
│ - Password: S001     │
└──────────┬───────────┘
           │
           │ Combine: "S001:S001"
           │
           ▼
    ┌──────────────────┐
    │ Encode in Base64 │
    │ "UzAwMTpTMDAx"   │
    └────────┬─────────┘
             │

Step 2: Send Request with Credentials
┌──────────────────────────────┐
│ GET /api/students            │
│ Authorization: Basic         │
│   UzAwMTpTMDAx               │
└──────────┬────────────────────┘
           │
           │
           ├──────────────────────────────────────────────▶│
                                                           │
                                                      ┌────▼──────────┐
                                                      │ BasicAuth     │
                                                      │ Filter        │
                                                      └────┬──────────┘
                                                           │
                                                      ┌────▼──────────┐
                                                      │ Extract       │
                                                      │ Authorization │
                                                      │ Header        │
                                                      └────┬──────────┘
                                                           │
                                                      ┌────▼──────────┐
                                                      │ Decode Base64 │
                                                      │ Get Username/ │
                                                      │ Password      │
                                                      └────┬──────────┘
                                                           │
                                                      ┌────▼──────────┐
                                                      │ Validate      │
                                                      │ Against DB    │
                                                      └────┬──────────┘
                                                           │
                                        ┌──────────────────┴──────────────────┐
                                        │                                     │
                                   VALID│                                     │INVALID
                                        │                                     │
                                        ▼                                     ▼
                                   ┌─────────┐                        ┌──────────────┐
                                   │ Load    │                        │ Return 401   │
                                   │ User    │                        │ Unauthorized │
                                   │ Roles   │                        └──────────────┘
                                   └────┬────┘
                                        │
                                   ┌────▼──────────┐
                                   │ Create Auth   │
                                   │ Object        │
                                   └────┬──────────┘
                                        │
                                   ┌────▼──────────┐
                                   │ Check Role    │
                                   │ Authorization │
                                   └────┬──────────┘
                                        │
                            ┌───────────┴───────────┐
                            │                       │
                       ALLOWED│                       │DENIED
                            │                       │
                            ▼                       ▼
                    ┌──────────────┐        ┌──────────────┐
                    │ Return 200   │        │ Return 403   │
                    │ Resource Data│        │ Forbidden    │
                    └──────────────┘        └──────────────┘
           ◀───────────────────────────────────────────────────────

Key Differences from JWT:
┌─────────────────────────────────────────────┐
│ • Credentials sent with every request       │
│ • No token or session ID needed             │
│ • Base64 encoding (not encryption)          │
│ • Credentials visible if not HTTPS          │
│ • Server validates DB on every request      │
│ • Cannot be revoked immediately             │
│ • Simple but less secure                    │
└─────────────────────────────────────────────┘
```

---

## 4. API Key Authentication Flow

```
┌────────────────────────────────────────────────────────────────────────┐
│                   API Key Authentication Flow                           │
└────────────────────────────────────────────────────────────────────────┘

CLIENT SIDE                           SERVER SIDE

Step 1: Client Has API Key
┌──────────────────────────┐
│ API Key issued by server │
│ Example:                 │
│ key_12345_abc            │
│                          │
│ Store securely in:       │
│ - Config file            │
│ - Environment variable   │
│ - Secrets manager        │
└──────────┬───────────────┘
           │

Step 2: Make Request with API Key

Option A: Via Header
┌──────────────────────────┐
│ GET /api/students        │
│ X-API-Key: key_12345_abc │
└──────────┬───────────────┘
           │

Option B: Via Query Parameter
┌────────────────────────────────────────┐
│ GET /api/students?api_key=key_12345_abc│
└──────────┬─────────────────────────────┘
           │
           │
           ├──────────────────────────────────────────────▶│
                                                           │
                                                      ┌────▼──────────┐
                                                      │ ApiKey        │
                                                      │ Filter        │
                                                      └────┬──────────┘
                                                           │
                                    ┌──────────────────────┴──────────────────────┐
                                    │                                             │
                            Check Header                                 Check Query
                            X-API-Key                                        Param
                                    │                                             │
                                    └──────────────────────┬──────────────────────┘
                                                           │
                                                      ┌────▼──────────┐
                                                      │ Lookup in     │
                                                      │ API Key       │
                                                      │ Registry      │
                                                      └────┬──────────┘
                                                           │
                                        ┌──────────────────┴──────────────────┐
                                        │                                     │
                                    FOUND│                                     │NOT FOUND
                                        │                                     │
                                        ▼                                     ▼
                                   ┌─────────┐                        ┌──────────────┐
                                   │ Check   │                        │ Return 401   │
                                   │ Active  │                        │ Unauthorized │
                                   │ Status  │                        └──────────────┘
                                   └────┬────┘
                                        │
                            ┌───────────┴───────────┐
                            │                       │
                       ACTIVE│                       │REVOKED/INACTIVE
                            │                       │
                            ▼                       ▼
                       ┌────────┐          ┌──────────────┐
                       │ Extract│          │ Return 401   │
                       │ User   │          │ Unauthorized │
                       │ Info & │          └──────────────┘
                       │ Role   │
                       └────┬───┘
                            │
                       ┌────▼──────────┐
                       │ Create Auth   │
                       │ Object        │
                       └────┬──────────┘
                            │
                       ┌────▼──────────┐
                       │ Check Role    │
                       │ Authorization │
                       └────┬──────────┘
                            │
            ┌───────────────┴───────────────┐
            │                               │
       ALLOWED│                               │DENIED
            │                               │
            ▼                               ▼
    ┌──────────────┐            ┌──────────────┐
    │ Return 200   │            │ Return 403   │
    │ Resource Data│            │ Forbidden    │
    └──────────────┘            └──────────────┘
       ◀─────────────────────────────────────────────────────

API Key Management:
┌──────────────────────────────────────────────────────────┐
│ Creating API Key:                                        │
│ 1. Admin generates unique key                            │
│ 2. Store in secure registry (DB)                         │
│ 3. Associate with user/app                               │
│ 4. Set expiration date                                   │
│ 5. Provide to client securely                            │
│                                                          │
│ Revoking API Key:                                        │
│ 1. Mark as inactive in registry                          │
│ 2. All requests with that key return 401                │
│ 3. Client must update to new key                         │
│ 4. Provides immediate control (unlike JWT)              │
└──────────────────────────────────────────────────────────┘
```

---

## 5. Multi-Factor Authentication (MFA) Flow

```
┌────────────────────────────────────────────────────────────────────────┐
│              Multi-Factor Authentication (MFA) Flow                     │
└────────────────────────────────────────────────────────────────────────┘

CLIENT SIDE                           SERVER SIDE

Step 1: Submit Credentials
┌──────────────────────┐
│ User enters:         │
│ - Username: S001     │
│ - Password: S001     │
│ Clicks "Login"       │
└──────────┬───────────┘
           │
           │ POST /api/auth/mfa/login
           │ {registrationNo, password}
           │
           ├──────────────────────────────────────────────▶│
                                                           │
                                                      ┌────▼──────────┐
                                                      │ Validate      │
                                                      │ Credentials   │
                                                      └────┬──────────┘
                                                           │
                                        ┌──────────────────┴──────────────────┐
                                        │                                     │
                                   VALID│                                     │INVALID
                                        │                                     │
                                        ▼                                     ▼
                                   ┌─────────┐                        ┌──────────────┐
                                   │ Generate│                        │ Return 401   │
                                   │ 6-digit │                        │ Unauthorized │
                                   │ OTP     │                        └──────────────┘
                                   └────┬────┘
                                        │
                                   ┌────▼──────────┐
                                   │ Send OTP to  │
                                   │ User Device: │
                                   │ • SMS        │
                                   │ • Email      │
                                   │ • App        │
                                   └────┬──────────┘
                                        │
                                   ┌────▼──────────┐
                                   │ Generate      │
                                   │ Session Token │
                                   │ (expires 5m)  │
                                   └────┬──────────┘
                                        │
         ┌────────────────────────────┤
         │                            │
         │ Return 200                 │
         │ {sessionToken, message}    │
         │                            │
◀────────┤ OTP printed to console     │
         │ (for testing)              │
         │

Step 2: User Receives OTP
┌─────────────────────────────────┐
│ User checks device:             │
│ • SMS: 123456                   │
│ • Email: 123456                 │
│ • Authenticator App: 123456     │
│ • Console (testing): 123456     │
└──────────┬──────────────────────┘
           │
           │ (OTP expires after 5 minutes)
           │

Step 3: Submit OTP
┌──────────────────────────────┐
│ User enters OTP in app:      │
│ OTP Code: 123456            │
│ Clicks "Verify"             │
└──────────┬───────────────────┘
           │
           │ POST /api/auth/mfa/verify
           │ {
           │   sessionToken: "...",
           │   otp: "123456"
           │ }
           │
           ├──────────────────────────────────────────────▶│
                                                           │
                                                      ┌────▼──────────┐
                                                      │ Validate      │
                                                      │ Session Token │
                                                      └────┬──────────┘
                                                           │
                                        ┌──────────────────┴──────────────────┐
                                        │                                     │
                                     VALID│                                     │INVALID
                                        │                                     │
                                        ▼                                     ▼
                                   ┌─────────┐                        ┌──────────────┐
                                   │ Check   │                        │ Return 400   │
                                   │ OTP     │                        │ Bad Request  │
                                   │ Expiry  │                        └──────────────┘
                                   └────┬────┘
                                        │
                            ┌───────────┴───────────┐
                            │                       │
                       NOTEXP│                       │EXPIRED
                            │                       │
                            ▼                       ▼
                       ┌────────┐          ┌──────────────┐
                       │ Verify │          │ Return 400   │
                       │ OTP    │          │ OTP Expired  │
                       │ Value  │          │ Request new  │
                       └────┬───┘          └──────────────┘
                            │
            ┌───────────────┴───────────────┐
            │                               │
       CORRECT│                               │INCORRECT
            │                               │
            ▼                               ▼
       ┌────────┐                   ┌──────────────┐
       │ Mark   │                   │ Increment    │
       │ OTP    │                   │ Attempts     │
       │ Used   │                   │ (Max: 3)     │
       └────┬───┘                   └──────────────┘
            │
       ┌────▼──────────┐
       │ Retrieve User │
       │ Details       │
       └────┬──────────┘
            │
       ┌────▼──────────┐
       │ Determine     │
       │ Role from DB  │
       └────┬──────────┘
            │
       ┌────▼──────────┐
       │ Generate JWT  │
       │ Token         │
       └────┬──────────┘
            │
         ◀──┴─────────────────────────────────────────────
            │
         Return 200
         {token, role, message}

Step 4: Use JWT Token
┌──────────────────────────┐
│ Token received from MFA  │
│ Use like standard JWT    │
│                          │
│ GET /api/students        │
│ Auth: Bearer <token>     │
└──────────┬───────────────┘
           │
           ├──────────────────────────────────────────────▶│
                                                    Standard
                                                    JWT Flow
                                                    (see JWT
                                                    diagram)

MFA Security Features:
┌───────────────────────────────────────────────────────┐
│ • OTP expires after 5 minutes                         │
│ • OTP is single-use (cannot be reused)                │
│ • Limited attempts (3 incorrect OTPs locks session)   │
│ • Session token is temporary (5 minutes)              │
│ • After MFA, standard JWT token is issued             │
│ • Even if password is compromised, account is safe    │
│ • Requires access to second factor (SMS/Email/App)    │
└───────────────────────────────────────────────────────┘
```

---

## 6. Filter Chain Order & Execution

```
┌─────────────────────────────────────────────────────────────────────┐
│                      Filter Chain Execution Order                    │
└─────────────────────────────────────────────────────────────────────┘

HTTP Request arrives
        │
        ▼
┌──────────────────────────────────────────┐
│ 1. ApiKeyAuthenticationFilter            │
│    Priority: 1st                         │
│    Checks:                               │
│    - X-API-Key header                    │
│    - api_key query parameter             │
│    Time Complexity: O(1)                 │
│    Best for: Public APIs                 │
└──────────┬───────────────────────────────┘
           │
           ├─ No API Key ─────────┐
           │                       │
           │                       ▼
           │          ┌──────────────────────────────────────────────┐
           │          │ Continues to next filter                     │
           │          └──────────────────────────────────────────────┘
           │
           │ API Key Found & Valid
           │
           ├────────────────────────────────────────┐
           │                                        │
           │ Sets SecurityContext with user role    │
           │ Request proceeds to controller         │
           │ (skips remaining filters)              │
           │                                        │
           │ ✓ Filters return early (optimization)  │
           │                                        │
           └────────────────────────────────────────┘


       (If no API Key authenticated)
                    │
                    ▼
┌──────────────────────────────────────────┐
│ 2. BasicAuthenticationFilter             │
│    Priority: 2nd                         │
│    Checks:                               │
│    - Authorization: Basic header         │
│    Performs:                             │
│    - Base64 decoding                     │
│    - Database lookup                     │
│    Time Complexity: O(1)                 │
│    Best for: Server-to-server            │
└──────────┬───────────────────────────────┘
           │
           ├─ No Basic Auth Header ────┐
           │                           │
           │                           ▼
           │          ┌──────────────────────────────────────────────┐
           │          │ Continues to next filter                     │
           │          └──────────────────────────────────────────────┘
           │
           │ Basic Auth Found & Valid
           │
           ├────────────────────────────────────────┐
           │                                        │
           │ Sets SecurityContext with user role    │
           │ Request proceeds to controller         │
           │ (skips remaining filters)              │
           │                                        │
           │ ✓ Early return optimization            │
           │                                        │
           └────────────────────────────────────────┘


       (If no Basic Auth authenticated)
                    │
                    ▼
┌──────────────────────────────────────────┐
│ 3. JwtAuthenticationFilter               │
│    Priority: 3rd                         │
│    Checks:                               │
│    - Authorization: Bearer header        │
│    Performs:                             │
│    - JWT parsing                         │
│    - Signature verification              │
│    - Expiry check                        │
│    Time Complexity: O(1) or O(n)         │
│    Best for: Mobile, SPA                 │
└──────────┬───────────────────────────────┘
           │
           ├─ No JWT Token ────┐
           │                   │
           │                   ▼
           │    ┌──────────────────────────────────────────────┐
           │    │ Continues to next filter                     │
           │    └──────────────────────────────────────────────┘
           │
           │ JWT Found & Valid
           │
           ├────────────────────────────────────────┐
           │                                        │
           │ Extracts user & role from token        │
           │ Sets SecurityContext with authority    │
           │ Request proceeds to controller         │
           │                                        │
           │ ✓ No DB lookup needed (stateless)      │
           │                                        │
           └────────────────────────────────────────┘


       (If no JWT authenticated)
                    │
                    ▼
┌──────────────────────────────────────────┐
│ 4. UsernamePasswordAuthenticationFilter  │
│    (Spring Default)                      │
│    Priority: 4th                         │
│    Checks:                               │
│    - Form login parameters               │
│    Best for: Traditional web apps        │
└──────────┬───────────────────────────────┘
           │
           │ (Form login not configured in this app)
           │ So authentication fails here
           │
           ▼
┌──────────────────────────────────────────┐
│ No Authentication Successful             │
│ User is ANONYMOUS                        │
│                                          │
│ SecurityContext contains:                │
│ - Principal: anonymousUser               │
│ - Authorities: [ROLE_ANONYMOUS]          │
└──────────┬───────────────────────────────┘
           │
           ▼
┌──────────────────────────────────────────┐
│ Authorization Check:                     │
│                                          │
│ Request requires one of:                 │
│ - No auth (ROLE_ANONYMOUS OK)            │
│ - ROLE_JUNIOR                            │
│ - ROLE_SENIOR                            │
│                                          │
│ If endpoint allows ANONYMOUS:            │
│   ✓ Access granted                       │
│ If endpoint requires ROLE:               │
│   ✗ Access denied (403)                  │
└──────────┬───────────────────────────────┘
           │
           ▼
┌──────────────────────────────────────────┐
│ CustomAccessDeniedHandler invoked        │
│                                          │
│ Returns 403 Forbidden                    │
│                                          │
│ Response:                                │
│ {                                        │
│   "status": 403,                         │
│   "message": "Access Denied",            │
│   "path": "/api/students"                │
│ }                                        │
└──────────────────────────────────────────┘


Why This Order?
┌──────────────────────────────────────────────────────────┐
│ 1. API Key first:                                        │
│    - No I/O, just hash lookup O(1)                       │
│    - Fast rejection for invalid keys                     │
│                                                          │
│ 2. Basic Auth second:                                    │
│    - Requires Base64 decoding                            │
│    - Database lookup needed                              │
│    - But stateless, so acceptable                        │
│                                                          │
│ 3. JWT last:                                             │
│    - More complex parsing and validation                 │
│    - Most common in modern apps                          │
│    - No DB lookup (stateless)                            │
│                                                          │
│ 4. Form login last:                                      │
│    - Default Spring handler                              │
│    - Not configured in this app                          │
│    - Would handle POST /login requests                   │
│                                                          │
│ Result: Early returns optimize performance!              │
└──────────────────────────────────────────────────────────┘
```

---

## 7. Role-Based Access Control (RBAC)

```
┌─────────────────────────────────────────────────────────────────────┐
│            Role-Based Access Control (RBAC) Flow                     │
└─────────────────────────────────────────────────────────────────────┘

Authentication succeeds
│
├─ Student S001 (DOB: 1999)
│  └─ SENIOR role
│
├─ Student S002 (DOB: 2005)
│  └─ JUNIOR role
│
└─ Anonymous User
   └─ ROLE_ANONYMOUS


Request: GET /api/students
Required Role: ROLE_SENIOR

┌─────────────┬──────────────┬──────────────┬──────────────┐
│  S001       │  S002        │ Anon         │ Invalid      │
│ (SENIOR)    │ (JUNIOR)     │ (Anonymous)  │ Token        │
├─────────────┼──────────────┼──────────────┼──────────────┤
│             │              │              │              │
│ ✓ Access    │ ✗ Access     │ ✗ Access     │ ✗ Access     │
│   Granted   │   Denied     │   Denied     │   Denied     │
│ (200 OK)    │ (403 Forbid) │ (403 Forbid) │ (401 Unauth) │
│             │              │              │              │
└─────────────┴──────────────┴──────────────┴──────────────┘


Request: GET /api/students/simple
Required Role: ROLE_JUNIOR

┌─────────────┬──────────────┬──────────────┬──────────────┐
│  S001       │  S002        │ Anon         │ Invalid      │
│ (SENIOR)    │ (JUNIOR)     │ (Anonymous)  │ Token        │
├─────────────┼──────────────┼──────────────┼──────────────┤
│             │              │              │              │
│ ✓ Access    │ ✓ Access     │ ✗ Access     │ ✗ Access     │
│   Granted   │   Granted    │   Denied     │   Denied     │
│ (200 OK)    │ (200 OK)     │ (403 Forbid) │ (401 Unauth) │
│             │              │              │              │
│ (SENIOR     │ (Exact       │              │              │
│  includes   │  Match)      │              │              │
│  JUNIOR)    │              │              │              │
│             │              │              │              │
└─────────────┴──────────────┴──────────────┴──────────────┘


Role Determination:
┌────────────────────────────────────────────────────────┐
│ Student DOB (Date of Birth)                            │
│         │                                               │
│    ┌────┴────┐                                          │
│    │          │                                          │
│    ▼          ▼                                          │
│  Before     After                                       │
│  2002       2002                                        │
│    │          │                                          │
│    ▼          ▼                                          │
│  SENIOR    JUNIOR                                       │
│    │          │                                          │
│    └────┬─────┘                                          │
│         │                                               │
│    Example:                                             │
│    ┌─────────────────────────────────┐                 │
│    │ S001: DOB = 1999 → SENIOR        │                 │
│    │ S002: DOB = 2005 → JUNIOR        │                 │
│    │ S003: DOB = 2002 → SENIOR        │                 │
│    │ S004: DOB = 2003 → JUNIOR        │                 │
│    └─────────────────────────────────┘                 │
└────────────────────────────────────────────────────────┘


Role Hierarchy:
┌────────────────────────────────────┐
│ ADMIN (if implemented)              │
│    └─ Can do everything             │
│                                     │
│ SENIOR                              │
│    ├─ Access /api/students/**       │
│    ├─ Access /api/students/simple/**│
│    └─ Full data access              │
│                                     │
│ JUNIOR                              │
│    ├─ Access /api/students/simple/**│
│    └─ Limited data access           │
│                                     │
│ ANONYMOUS (unauthenticated)         │
│    ├─ Access /api/auth/**           │
│    ├─ Access /api/auth/health       │
│    └─ Access /api/auth/methods      │
└────────────────────────────────────┘
```

---

**Version**: 1.0  
**Last Updated**: December 23, 2025



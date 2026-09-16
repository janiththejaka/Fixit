# FixIt Backend — Security Architecture

A complete reference for every security mechanism used in this project:
HTTP headers, cookies, JWT authentication, CORS, and CSRF protection — the theory and the exact code that implements it.

---

## Table of Contents

1. [HTTP Headers — The Foundation](#1-http-headers--the-foundation)
2. [Cookies — Persistent State Over HTTP](#2-cookies--persistent-state-over-http)
3. [JWT — Stateless Authentication Tokens](#3-jwt--stateless-authentication-tokens)
4. [Authentication Flow in FixIt](#4-authentication-flow-in-fixit)
5. [CORS — Cross-Origin Resource Sharing](#5-cors--cross-origin-resource-sharing)
6. [CSRF — Cross-Site Request Forgery](#6-csrf--cross-site-request-forgery)
7. [The Complete Security Stack](#7-the-complete-security-stack)
8. [Postman Testing Guide](#8-postman-testing-guide)
9. [Frontend Integration Guide](#9-frontend-integration-guide)

---

## 1. HTTP Headers — The Foundation

Every HTTP request and response carries **headers** — key-value pairs that carry metadata about the message. Security in web applications is largely implemented through specific headers.

### Request Headers (Client → Server)

| Header | Purpose | Example |
|---|---|---|
| `Authorization` | Carries credentials / tokens | `Bearer eyJhbGci...` |
| `Cookie` | Sends stored cookies back to server | `fixit_access_token=eyJ...` |
| `X-XSRF-TOKEN` | Carries the CSRF protection token | `abc123rawvalue` |
| `Content-Type` | Describes the request body format | `application/json` |
| `Origin` | The origin that made the request | `http://localhost:5173` |

### Response Headers (Server → Client)

| Header | Purpose | Example |
|---|---|---|
| `Set-Cookie` | Instructs the browser to store a cookie | `fixit_access_token=eyJ...; HttpOnly` |
| `Access-Control-Allow-Origin` | Tells the browser which origins are allowed | `http://localhost:5173` |
| `Access-Control-Allow-Credentials` | Allows cookies in cross-origin requests | `true` |

### How FixIt Uses Headers

After a successful login, the server sends:

```
HTTP/1.1 200 OK
Set-Cookie: fixit_access_token=eyJhbGciOiJIUzI1NiJ9...; Path=/; HttpOnly; SameSite=Strict
Content-Type: application/json

{"success": true, "message": "Login successful"}
```

The browser stores that cookie. On every subsequent request, the browser automatically attaches it:

```
POST /api/gigs HTTP/1.1
Cookie: fixit_access_token=eyJhbGciOiJIUzI1NiJ9...
X-XSRF-TOKEN: abc123rawvalue
Content-Type: application/json
```

---

## 2. Cookies — Persistent State Over HTTP

HTTP is **stateless** — every request is independent. Cookies solve this by letting the server store small pieces of data in the browser that are automatically sent back on every subsequent request to the same domain.

### Cookie Anatomy

```
Set-Cookie: name=value; Path=/; HttpOnly; Secure; SameSite=Strict; Max-Age=3600
```

| Attribute | Meaning |
|---|---|
| `name=value` | The cookie's identifier and data |
| `Path=/` | Send this cookie for all paths on this domain |
| `HttpOnly` | JavaScript **cannot** read this cookie (protects against XSS theft) |
| `Secure` | Only send over HTTPS (never plain HTTP) |
| `SameSite=Strict` | Browser refuses to send this cookie on **any** cross-origin request |
| `SameSite=Lax` | Browser allows cookie on top-level navigation but not sub-requests |
| `Max-Age=3600` | Cookie expires after 3600 seconds (1 hour) |

### The Two Cookies in FixIt

FixIt uses **two distinct cookies**, each with a different role and different attributes:

#### Cookie 1 — `fixit_access_token` (JWT carrier)

```
Set-Cookie: fixit_access_token=eyJhbGci...; HttpOnly; SameSite=Strict; Path=/; Max-Age=3600
```

- **HttpOnly = true** → JavaScript cannot touch it. Only the browser sends it automatically.
- **SameSite = Strict** → Sent only on same-origin requests. Cross-origin sites cannot trigger requests that include this cookie.
- **Purpose**: Carry the user's JWT so the server can identify who they are.

#### Cookie 2 — `XSRF-TOKEN` (CSRF protection)

```
Set-Cookie: XSRF-TOKEN=abc123rawvalue; Path=/
```

- **HttpOnly = false** → JavaScript **can** read it. This is intentional — the SPA must read this value and echo it back as a header.
- **SameSite = not set** (defaults to browser's default, usually `Lax`) → Accessible to same-origin JS.
- **Purpose**: Provide a verifiable secret that only JavaScript running on the legitimate origin can read and send back.

### Where These Are Set in Code

**`fixit_access_token`** is created in [`AuthCookieService.java`](platform/src/main/java/com/fixit/platform/modules/auth/service/AuthCookieService.java):

```java
public ResponseCookie createAccessTokenCookie(String token) {
    return ResponseCookie.from(cookieName, token)
            .httpOnly(true)       // blocks JS access → XSS-safe
            .secure(secure)       // HTTPS-only in production
            .sameSite(sameSite)   // "Strict" → no cross-origin sending
            .path("/")
            .maxAge(Duration.ofSeconds(maxAge))
            .build();
}
```

**`XSRF-TOKEN`** is set automatically by Spring Security's `CookieCsrfTokenRepository` (configured in [`SecurityConfig.java`](platform/src/main/java/com/fixit/platform/config/SecurityConfig.java)) when the `/api/auth/csrf` endpoint is called.

---

## 3. JWT — Stateless Authentication Tokens

### What Is a JWT?

A **JSON Web Token (JWT)** is a compact, self-contained token that encodes information (claims) and is cryptographically signed so the server can verify it without a database lookup.

### Structure

A JWT has three Base64URL-encoded parts separated by dots:

```
eyJhbGciOiJIUzI1NiJ9   .   eyJ1c2VySWQiOiIxMjMi...   .   SflKxwRJSMeKKF2QT4fw...
      HEADER                       PAYLOAD                        SIGNATURE
```

**Header** — algorithm used to sign:
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload** — the claims (data stored inside the token):
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "roles": ["ROLE_CLIENT"],
  "iat": 1726464000,
  "exp": 1726467600
}
```

**Signature** — proof the token hasn't been tampered with:
```
HMAC-SHA256(base64(header) + "." + base64(payload), SECRET_KEY)
```

### Why JWTs Are Stateless

The server does **not** store sessions. It only holds the `SECRET_KEY`. On each request:
1. Server receives the JWT.
2. Re-computes the signature using the secret.
3. If it matches → token is genuine and unmodified.
4. Reads `exp` field → if not expired, the user is authenticated.

No database query needed. No session table. This scales horizontally.

### JWT Verification in FixIt — `JwtFilter`

[`JwtFilter.java`](platform/src/main/java/com/fixit/platform/modules/auth/security/JwtFilter.java) runs on **every request** before Spring Security's own filters:

```java
@Override
protected void doFilterInternal(HttpServletRequest request, ...) {

    // 1. Extract token from Authorization header OR fixit_access_token cookie
    String token = resolveToken(request);

    // 2. Verify signature + expiry
    if (token != null && jwtService.isTokenValid(token)) {

        // 3. Extract claims from payload
        UUID userId    = jwtService.extractUserId(token);
        String email   = jwtService.extractEmail(token);
        List<String> roles = jwtService.extractRoles(token);

        // 4. Build Spring Security Authentication object
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

        // 5. Store in SecurityContext — request is now "authenticated"
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    filterChain.doFilter(request, response); // continue
}
```

The filter supports two ways to send the JWT:

| Method | Header / Cookie | Use Case |
|---|---|---|
| **Bearer token** | `Authorization: Bearer <jwt>` | Postman / mobile apps |
| **HttpOnly cookie** | `Cookie: fixit_access_token=<jwt>` | Browser SPA |

---

## 4. Authentication Flow in FixIt

### Login

```
Browser                          Spring Boot
   |                                  |
   |-- POST /api/auth/login --------->|
   |   Body: {email, password}        |
   |                                  |-- Verify password with BCrypt
   |                                  |-- Generate JWT (signed with secret)
   |<-- 200 OK ----------------------|
   |   Set-Cookie: fixit_access_token=eyJ...; HttpOnly; SameSite=Strict
   |   Body: {"success": true}        |
```

After this, the browser stores the `fixit_access_token` cookie. Every subsequent request to `localhost:8080` automatically includes it.

### Authenticated Request

```
Browser                          Spring Boot
   |                                  |
   |-- GET /api/profile/me ---------->|
   |   Cookie: fixit_access_token=eyJ|
   |                                  |-- JwtFilter runs
   |                                  |-- Validates JWT signature
   |                                  |-- Extracts userId, roles
   |                                  |-- Sets SecurityContext
   |                                  |-- Controller sees authenticated user
   |<-- 200 OK ----------------------|
   |   Body: {profile data}           |
```

### Logout

```java
// AuthCookieService.java
public ResponseCookie clearAccessTokenCookie() {
    return ResponseCookie.from(cookieName, "")
            .maxAge(Duration.ZERO)   // Immediately expire the cookie
            .build();
}
```

The server sends `Set-Cookie: fixit_access_token=; Max-Age=0` which instructs the browser to delete the cookie.

### Error Responses

Configured in [`SecurityConfig.java`](platform/src/main/java/com/fixit/platform/config/SecurityConfig.java):

| Situation | HTTP Status | Response |
|---|---|---|
| No JWT / invalid JWT | `401 Unauthorized` | `{"error": "Unauthorized", "message": "Valid authentication token required"}` |
| Valid JWT but wrong role | `403 Forbidden` | `{"error": "Forbidden", "message": "You do not have permission to access this resource"}` |

---

## 5. CORS — Cross-Origin Resource Sharing

### The Problem CORS Solves

Browsers enforce the **Same-Origin Policy**: a page at `http://localhost:5173` cannot make JavaScript fetch requests to `http://localhost:8080` by default. They are different origins (different port = different origin).

**Origin** = scheme + hostname + port. All three must match for same-origin.

```
http://localhost:5173  ←→  http://localhost:8080
                                              ^^^^
                                         Different port → Cross-Origin
```

### How CORS Works

For cross-origin requests that might have side effects (POST, PUT, DELETE), browsers first send a **preflight request** using the `OPTIONS` method to ask for permission:

```
Browser                                     Spring Boot
   |                                             |
   |-- OPTIONS /api/gigs ----------------------->|
   |   Origin: http://localhost:5173             |
   |   Access-Control-Request-Method: POST       |
   |   Access-Control-Request-Headers: X-XSRF-TOKEN |
   |                                             |
   |<-- 200 OK ----------------------------------|
   |   Access-Control-Allow-Origin: http://localhost:5173
   |   Access-Control-Allow-Methods: GET, POST, PUT, ...
   |   Access-Control-Allow-Headers: *
   |   Access-Control-Allow-Credentials: true
   |                                             |
   |-- POST /api/gigs (actual request) --------->|
   |   Cookie: fixit_access_token=...            |
```

If the preflight is rejected, the browser blocks the actual request entirely — the code never runs on the server.

### CORS Configuration in FixIt

[`CorsConfig.java`](platform/src/main/java/com/fixit/platform/config/CorsConfig.java):

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // Only our frontend is allowed — all others are blocked
    configuration.setAllowedOrigins(List.of(frontendOrigin)); // "http://localhost:5173"

    // These are the HTTP methods the frontend is permitted to use
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

    // Allow all request headers (including X-XSRF-TOKEN)
    configuration.setAllowedHeaders(List.of("*"));

    // Critical: allows cookies to be sent with cross-origin requests.
    // Without this, the browser strips cookies from cross-origin requests
    // even if the origin is whitelisted.
    configuration.setAllowCredentials(true);

    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

**Why `setAllowCredentials(true)` is required:**
When the frontend does `fetch(..., { credentials: "include" })`, the browser will only actually include cookies if the server explicitly permits it with `Access-Control-Allow-Credentials: true`. Without this, the JWT cookie is silently stripped.

**Why `setAllowedOrigins("*")` cannot be used with credentials:**
This is a browser security rule. You cannot allow all origins AND allow credentials. You must specify an exact origin list.

---

## 6. CSRF — Cross-Site Request Forgery

### The Attack

CSRF exploits the fact that browsers automatically send cookies with every request to a domain — including requests **triggered by a different site**.

**Attack scenario without CSRF protection:**

```
1. User logs in to fixit.com → browser stores fixit_access_token cookie
2. User visits evil.com in another tab
3. evil.com's HTML silently submits a hidden form to fixit.com:
   <form action="http://localhost:8080/api/gigs" method="POST">
4. Browser sends the request WITH the fixit_access_token cookie automatically
5. Server sees a valid JWT → thinks it's the real user → processes the request
6. Attacker has performed an action on the user's behalf ← CSRF attack
```

### Why SameSite=Strict Blocks It (Layer 1)

```java
// AuthCookieService.java
.sameSite("Strict")  // from application.yaml: cookie-same-site: Strict
```

With `SameSite=Strict`, the browser **refuses to send** the `fixit_access_token` cookie when the request originates from another site. The attacker's form submission arrives at the server with no cookie → server returns 401 → attack fails.

| SameSite Value | Cookie sent on cross-origin? |
|---|---|
| `Strict` | Never — not even clicking a link from another site |
| `Lax` | Only for top-level GET navigations (clicking links), not POST/PUT |
| `None` | Always (requires `Secure`) |

### The Double Submit Cookie Pattern (Layer 2)

Even with `SameSite=Strict`, we implement CSRF tokens as a defense-in-depth layer for older browsers and additional assurance.

**The mechanism:**

```
1. Client calls GET /api/auth/csrf
2. Server sets:
   - XSRF-TOKEN cookie (readable by JS, NOT HttpOnly)
   - Returns { "token": "abc123", "headerName": "X-XSRF-TOKEN" }

3. For every state-changing request (POST/PUT/PATCH/DELETE), client must send:
   - Cookie:       XSRF-TOKEN=abc123    ← sent automatically by browser
   - Header:       X-XSRF-TOKEN: abc123  ← explicitly set by JS

4. Server compares: cookie value == header value?
   → Match   → Request allowed ✅
   → Mismatch → 403 Forbidden  ❌
```

**Why an attacker cannot forge this:**

```
Attacker on evil.com tries to forge a request:
  ├── Can they read the XSRF-TOKEN cookie?  NO  → Same-Origin Policy blocks it
  └── Can they set the X-XSRF-TOKEN header? NO  → Cross-origin JS cannot set custom headers

Therefore: attacker cannot produce a matching header → server rejects it
```

### CSRF Configuration in FixIt

[`SecurityConfig.java`](platform/src/main/java/com/fixit/platform/config/SecurityConfig.java):

```java
.csrf(csrf -> csrf
    // Stores the CSRF token in a cookie named XSRF-TOKEN
    // withHttpOnlyFalse() → JS can read the cookie to echo it as a header
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())

    // Plain handler: cookie value == JSON body token == header value
    // No XOR masking needed (that's only for server-rendered HTML + BREACH attack mitigation)
    .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())

    // Login and register don't need CSRF — user is unauthenticated, no state to protect
    .ignoringRequestMatchers("/api/auth/login", "/api/auth/register/**")
)
```

[`AuthController.java`](platform/src/main/java/com/fixit/platform/modules/auth/controller/AuthController.java):

```java
@GetMapping("/csrf")
public CsrfToken csrf(CsrfToken token) {
    // token.getToken() forces the deferred proxy to resolve eagerly.
    // Without this call, Spring's lazy loading never writes the Set-Cookie
    // header for XSRF-TOKEN to the response.
    token.getToken();
    return token;
}
```

### Why Not XOR Masking (`SpaCsrfTokenRequestHandler`)?

Spring provides `SpaCsrfTokenRequestHandler` which applies XOR masking to the CSRF token in the JSON response body. This is designed to mitigate the **BREACH attack** — a compression-based side-channel attack.

**BREACH requires all three:**
1. Response is gzip-compressed ✅
2. A secret is **reflected in every response body** ← key condition
3. Attacker can vary request content and measure response sizes

In a **server-rendered HTML app** (e.g., Thymeleaf), the CSRF token is embedded in every rendered page:
```html
<input type="hidden" name="_csrf" value="TOKEN_HERE">
```
The same token appears in every compressed response → BREACH can extract it byte by byte. XOR masking randomizes the token on every response to defeat this.

In FixIt, the CSRF token is only returned **once**, from `/api/auth/csrf`, and is **never** embedded in other API response bodies. BREACH has nothing to measure — the vulnerability doesn't exist. The plain handler is correct for this REST API architecture.

**`SpaCsrfTokenRequestHandler` also caused a bug in this project:**

```
Cookie XSRF-TOKEN  = raw_token          (e.g. abc123)
JSON body "token"  = XOR(raw_token)     (longer, different value)

Postman sends: X-XSRF-TOKEN: XOR(raw_token)   ← from JSON body
Spring compares: XOR(raw_token) ≠ raw_token    → 403 Forbidden ❌
```

### Endpoints That Bypass CSRF

```java
.ignoringRequestMatchers(
    "/api/auth/login",      // unauthenticated users, no session to protect
    "/api/auth/register/**" // unauthenticated users, no session to protect
)
```

GET requests are always exempt from CSRF checks — they should be read-only and have no side effects.

---

## 7. The Complete Security Stack

```
Incoming Request
       │
       ▼
┌─────────────────────────────────────────────────────────┐
│  CORS Filter (CorsConfig.java)                          │
│  ─ Is Origin: header in allowedOrigins list?            │
│  ─ If no  → Browser blocks request (preflight rejected) │
│  ─ If yes → Add Access-Control-* response headers       │
└─────────────────────────────┬───────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────┐
│  CSRF Filter (SecurityConfig.java)                      │
│  ─ Is this GET/HEAD/OPTIONS/TRACE? → skip check         │
│  ─ Is this path in ignoringRequestMatchers? → skip      │
│  ─ Read XSRF-TOKEN cookie value                         │
│  ─ Read X-XSRF-TOKEN header value                       │
│  ─ Match? → proceed    No match? → 403 Forbidden        │
└─────────────────────────────┬───────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────┐
│  JwtFilter (JwtFilter.java)                             │
│  ─ Find token in Authorization header or cookie         │
│  ─ Validate signature + expiry                          │
│  ─ Extract userId, email, roles from payload            │
│  ─ Set SecurityContext (marks request as authenticated) │
└─────────────────────────────┬───────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────┐
│  Authorization (SecurityConfig.java)                    │
│  ─ Is this a permitAll endpoint? → allow                │
│  ─ Is SecurityContext authenticated? → allow            │
│  ─ Not authenticated → 401 Unauthorized                 │
│  ─ Wrong role (@PreAuthorize) → 403 Forbidden           │
└─────────────────────────────┬───────────────────────────┘
                              │
                              ▼
                       Controller Method
```

### Public vs Protected Endpoints

| Endpoint | Auth Required | CSRF Required |
|---|---|---|
| `POST /api/auth/login` | ❌ | ❌ (ignored) |
| `POST /api/auth/register/**` | ❌ | ❌ (ignored) |
| `GET /api/auth/csrf` | ❌ | ❌ (GET) |
| `GET /api/auth/**` | ❌ | ❌ (GET) |
| `GET /api/skills/**` | ❌ | ❌ (GET) |
| `GET /api/profile/providers` | ❌ | ❌ (GET) |
| `GET /api/gigs/**` | ❌ | ❌ (GET) |
| `POST /api/gigs` | ✅ | ✅ |
| `PUT /api/gigs/{id}` | ✅ | ✅ |
| `DELETE /api/gigs/{id}` | ✅ | ✅ |
| All other endpoints | ✅ | ✅ |

---

## 8. Postman Testing Guide

### Step 1 — Enable Cookie Jar

In Postman, click **Cookies** (top-right of the request panel). Ensure `localhost` appears in the domain list. Postman automatically stores cookies from responses and sends them on subsequent requests to the same domain.

### Step 2 — Fetch the CSRF Token

```
GET  http://localhost:8080/api/auth/csrf
```

**Response:**
```json
{
  "token": "abc123rawvalue",
  "headerName": "X-XSRF-TOKEN",
  "parameterName": "_csrf"
}
```

Spring simultaneously writes `Set-Cookie: XSRF-TOKEN=abc123rawvalue` to the response. Postman stores it in the cookie jar.

Copy the `"token"` value from the JSON body.

### Step 3 — Login

```
POST  http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "yourpassword"
}
```

No CSRF token header needed — this endpoint is in `ignoringRequestMatchers`. After login, Postman stores the `fixit_access_token` cookie automatically.

### Step 4 — Make Authenticated State-Changing Requests

For every **POST / PUT / PATCH / DELETE**:

| What | Where | Value |
|---|---|---|
| JWT | Sent automatically | From `fixit_access_token` cookie in jar |
| CSRF cookie | Sent automatically | From `XSRF-TOKEN` cookie in jar |
| CSRF header | **You must add this manually** | `X-XSRF-TOKEN: abc123rawvalue` |

**Example:**
```
POST  http://localhost:8080/api/gigs
X-XSRF-TOKEN: abc123rawvalue
Content-Type: application/json

{
  "title": "Fix my sink",
  "description": "..."
}
```

### Why Both Must Be Present

Spring's CSRF filter reads two things:
1. `XSRF-TOKEN` cookie → the stored expected value
2. `X-XSRF-TOKEN` header → the value the client claims to have

It compares them. Both must be present and equal.

An attacker on `evil.com` cannot set the `X-XSRF-TOKEN` header value — they cannot read the cookie due to Same-Origin Policy. So even if they can trigger a request, they can't pass the CSRF check.

### Token Lifetime

CSRF tokens in this setup are tied to the server-side session/state only in that the `XSRF-TOKEN` cookie is set once and reused. If the server restarts or you clear cookies, re-run Steps 2 and 3.

---

## 9. Frontend Integration Guide

### Required: `credentials: "include"` on Every Request

All fetch calls must include credentials so the browser sends cookies cross-origin:

```javascript
fetch("http://localhost:8080/api/gigs", {
    method: "POST",
    credentials: "include",   // ← sends fixit_access_token + XSRF-TOKEN cookies
    headers: {
        "Content-Type": "application/json",
        "X-XSRF-TOKEN": csrfToken,
    },
    body: JSON.stringify(data),
});
```

### CSRF Token Utility

```javascript
// api.js

const BASE_URL = "http://localhost:8080";

// Reads the XSRF-TOKEN cookie that Spring writes.
// This works because the cookie is NOT HttpOnly — JS can read it.
function getCsrfTokenFromCookie() {
    return document.cookie
        .split("; ")
        .find((row) => row.startsWith("XSRF-TOKEN="))
        ?.split("=")[1];
}

// Call this once at app startup to trigger Spring to set the XSRF-TOKEN cookie.
// After this, getCsrfTokenFromCookie() will return the value.
export async function initCsrf() {
    await fetch(`${BASE_URL}/api/auth/csrf`, {
        method: "GET",
        credentials: "include",
    });
}

// Central API wrapper — auto-attaches CSRF header for mutating requests
export async function apiFetch(path, options = {}) {
    const method = (options.method || "GET").toUpperCase();
    const headers = { "Content-Type": "application/json", ...options.headers };

    if (["POST", "PUT", "PATCH", "DELETE"].includes(method)) {
        const token = getCsrfTokenFromCookie();
        if (token) {
            headers["X-XSRF-TOKEN"] = token;
        }
    }

    const response = await fetch(`${BASE_URL}${path}`, {
        ...options,
        headers,
        credentials: "include",  // always include cookies
    });

    if (!response.ok) {
        const error = await response.json().catch(() => ({}));
        throw error;
    }

    return response.json();
}
```

### App Startup

```javascript
// main.js
import { initCsrf, apiFetch } from "./api.js";

// Trigger XSRF-TOKEN cookie to be written before any requests
await initCsrf();

// Now all requests work:
await apiFetch("/api/gigs", {
    method: "POST",
    body: JSON.stringify({ title: "Fix my sink" }),
});
```

### Why the Browser Can Read `XSRF-TOKEN` but Not `fixit_access_token`

```
fixit_access_token cookie:
  HttpOnly = true  → JavaScript: document.cookie does NOT contain this
                   → Browser sends it automatically, invisible to JS
                   → If XSS steals JS control, they still can't read the JWT ✅

XSRF-TOKEN cookie:
  HttpOnly = false → JavaScript: document.cookie CONTAINS this
                   → JS reads it and echoes it as X-XSRF-TOKEN header
                   → Attacker on evil.com cannot read it (Same-Origin Policy)
                   → XSS on fixit.com could read it, but XSS already has full
                     access to the page so CSRF is the least of your concerns
```

---

## Configuration Reference

`application.yaml`:

```yaml
app:
  auth:
    cookie-name: fixit_access_token   # name of the JWT cookie
    cookie-max-age: 3600              # 1 hour in seconds
    cookie-secure: false              # set to true in production (HTTPS required)
    cookie-same-site: Strict          # no cross-origin sending

frontend:
  origin: http://localhost:5173       # only this origin passes CORS checks
```

Change for production:
```yaml
app:
  auth:
    cookie-secure: true               # HTTPS only
frontend:
  origin: https://yourdomain.com      # your deployed frontend URL
```

---

## Security Layer Summary

| Layer | Mechanism | Protects Against |
|---|---|---|
| **1** | `SameSite=Strict` on JWT cookie | CSRF — browser never sends cookie cross-origin |
| **2** | Double Submit Cookie (XSRF-TOKEN) | CSRF — backup for older browsers |
| **3** | CORS allowlist | Unauthorized origins cannot make credentialed requests |
| **4** | HttpOnly on JWT cookie | XSS cannot steal the JWT via `document.cookie` |
| **5** | JWT signature verification | Token tampering / forgery |
| **6** | JWT expiry (`exp` claim) | Stolen tokens auto-expire after 1 hour |
| **7** | BCrypt password hashing | Database breach → passwords remain unreadable |
| **8** | `@PreAuthorize` on methods | Privilege escalation — role checks per endpoint |

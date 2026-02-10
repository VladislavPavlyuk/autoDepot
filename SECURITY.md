# Security and vulnerabilities

## Summary of identified issues

### 1. **API without authentication (High)**

- **Location:** `SecurityConfig` — `anyRequest().permitAll()` allows unauthenticated access to all endpoints, including `/api/**`.
- **Impact:** Anyone can create/delete data: create orders, drivers, assign trips, complete trips, trigger breakdowns, request repairs, generate orders.
- **Recommendation:** Require authentication for state-changing API endpoints, e.g.:
  - `.requestMatchers("/api/**").authenticated()` and use session or token (e.g. JWT) for SPA.

### 2. **CSRF disabled for API (Medium)**

- **Location:** `SecurityConfig` — `.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))`.
- **Impact:** If the API is later protected by session cookies, cross-site requests can perform actions as the logged-in user without CSRF token.
- **Recommendation:** When adding auth, either use SameSite cookies + CSRF token for browser clients, or use a non-cookie auth (e.g. Bearer token in header) so CSRF is not applicable.

### 3. **POST /api/orders validation (Mitigated)**

- **Location:** `FleetApiController.createOrder(OrderDTO)`.
- **Mitigation in place:** Controller now validates: destination and cargoType required and max 255 chars; weight in (0, 100_000] kg. Returns 400 with message on failure.
- **Optional:** Add `@Valid` and Bean Validation on DTO for consistency.

### 4. **Driver name length (Mitigated)**

- **Location:** `FleetApiController.createDriver` — driver name limited to 255 characters; returns 400 if exceeded.

### 5. **IDOR on trip operations (when auth is added) (Medium when auth is added)**

- **Location:** `completeTrip`, `reportBreakdown`, `requestRepair` use `@PathVariable Long tripId` with no ownership check.
- **Impact:** With authentication, any user could complete/break/repair another user’s trip by guessing or enumerating IDs.
- **Recommendation:** When adding auth, enforce that the current user is allowed to act on the given trip (e.g. by role or resource ownership).

### 6. **Error messages may leak internals (Low)**

- **Location:** `ApiExceptionHandler.handleAny` and `FleetApiController.generateRandomOrder` catch — return `exception.getMessage()` in JSON.
- **Impact:** Stack traces or DB/implementation details can be exposed to clients.
- **Recommendation:** Log full exception server-side; return generic message to client (e.g. "An error occurred") or sanitized message.

### 7. **Default admin credentials (High if used in production)**

- **Location:** `DataInitializer` — creates user `admin` / `admin` if missing.
- **Impact:** Default credentials are easily guessed.
- **Recommendation:** Do not use default passwords in production; set via env/secret or remove initializer in prod profile.

---

## What is in place

- **Password encoding:** BCrypt is used for user passwords.
- **SQL injection:** JPA/ORM and parameterized queries are used; no raw SQL with user input.
- **File path:** `readActivity()` uses a fixed path (`trips.log`), not user input — no path traversal.
- **Driver create validation:** Name and license categories/year are validated; categories restricted to A–E.

---

## Tests added

- **FleetApiControllerTest:** Unit tests for dashboard, createDriver (empty, invalid JSON, missing name, invalid categories, valid), generateRandomOrder (success and exception → 500 with message).
- **OrderApplicationServiceTest:** createOrder delegates to mapper and orderService; no validation on DTO (documents current behaviour).

Run tests: `./mvnw test`

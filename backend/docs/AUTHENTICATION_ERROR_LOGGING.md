# Authentication Error Logging System

## Overview
Comprehensive authentication and authorization error logging system that tracks all 401, 403, and 404 errors with detailed user information, IP addresses, and request context.

## Architecture

### 1. Entity Layer
**`AuthenticationErrorLog.java`**
- Stores authentication error details in database
- Fields:
  - `errorType`: Enum (UNAUTHORIZED_401, FORBIDDEN_403, NOT_FOUND_404, INVALID_TOKEN, ACCESS_DENIED)
  - `userId`: User ID if identified from token (nullable)
  - `username`: Username if available (nullable)
  - `ipAddress`: Client IP address
  - `userAgent`: Browser/client information
  - `endpoint`: Request URI
  - `httpMethod`: HTTP method (GET, POST, etc.)
  - `errorMessage`: Detailed error message
  - `attemptedAction`: Description of what user tried to do
  - `createdAt`: Timestamp

### 2. Repository Layer
**`AuthenticationErrorLogRepository.java`**
- JPA repository with advanced query methods
- Key queries:
  - `findByErrorType()`: Filter by error type
  - `findByUserId()`: Track specific user's errors
  - `findByIpAddress()`: Monitor suspicious IPs
  - `findByDateRange()`: Time-based analysis
  - `countByIpAddressSince()`: Rate limiting detection
  - `getStatisticsByErrorType()`: Error distribution
  - `getDailyStatistics()`: Daily error trends

### 3. Service Layer
**`AuthErrorLogService.java`**
- Async logging with `@Async` and `REQUIRES_NEW` propagation
- Fail-safe error handling (logging errors don't affect main flow)
- Formatted console output with box drawing
- Specialized methods:
  - `log401()`: Unauthorized errors (no auth)
  - `log403()`: Forbidden errors (insufficient permissions)
  - `log404()`: Not found errors
  - `logInvalidToken()`: Token validation failures
  - `logAccessDenied()`: Access denied events

### 4. Exception Handling Layer
**`GlobalExceptionHandler.java` (Enhanced)**
- `@ControllerAdvice` catches all exceptions globally
- Integrated auth error logging
- Extracts user info from:
  - Spring Security context
  - JWT token in Authorization header
- Handles:
  - `ResourceNotFoundException` → logs as 404
  - `UnauthorizedException` → logs as 403
  - `AccessDeniedException` → logs as 403 (Spring Security)
  - `AuthenticationException` → logs as 401 (Spring Security)

## Error Types

| Error Type | HTTP Code | When Logged | User Info Required |
|-----------|-----------|-------------|-------------------|
| UNAUTHORIZED_401 | 401 | Authentication required | No |
| FORBIDDEN_403 | 403 | Insufficient permissions | Yes (if available) |
| NOT_FOUND_404 | 404 | Resource not found | Yes (if available) |
| INVALID_TOKEN | 401 | Invalid/expired JWT | No |
| ACCESS_DENIED | 403 | Generic access denial | Yes (if available) |

## Data Flow

```
1. Request → Spring Security Filter
2. Authentication/Authorization Check Fails
3. Exception Thrown
4. GlobalExceptionHandler Catches Exception
5. Extract User Info:
   - Try Security Context (authentication.getDetails())
   - Try JWT Token (Authorization header)
   - Extract IP (X-Forwarded-For or RemoteAddr)
   - Extract User Agent
6. Call AuthErrorLogService.logXXX()
7. @Async Execution in Separate Thread
8. REQUIRES_NEW Transaction (independent)
9. Save to authentication_error_logs table
10. Log to Console (formatted box)
11. Return Error Response to Client
```

## Configuration

**application.properties**
```properties
# Enable/disable authentication error logging
app.security.log-auth-errors=true
```

## Database Schema

```sql
CREATE TABLE authentication_error_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    error_type VARCHAR(30) NOT NULL,
    user_id BIGINT NULL,
    username VARCHAR(100) NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    endpoint VARCHAR(500) NOT NULL,
    http_method VARCHAR(10),
    error_message TEXT,
    attempted_action VARCHAR(200),
    created_at TIMESTAMP NOT NULL,
    INDEX idx_error_type (error_type),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    INDEX idx_ip_address (ip_address),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## Console Output Example

```
╔════════════════════════════════════════════════════════════════════════════════╗
║ Authentication Error Detected                                                  ║
╠════════════════════════════════════════════════════════════════════════════════╣
║ Error Type: FORBIDDEN_403 - Forbidden - Insufficient Permissions              ║
║ User ID: 42                                                                    ║
║ Username: john.doe                                                             ║
║ IP Address: 192.168.1.100                                                      ║
║ Endpoint: /api/v1/admin/users                                                  ║
║ Message: Access denied to admin resource                                       ║
╚════════════════════════════════════════════════════════════════════════════════╝
```

## Usage Examples

### Automatic Logging (via GlobalExceptionHandler)

**401 - Unauthorized (No Token)**
```java
// Automatically logged when user tries to access protected endpoint without token
GET /api/v1/sessions
Authorization: (missing)
→ Logs as UNAUTHORIZED_401 with IP and User-Agent
```

**403 - Forbidden (Insufficient Role)**
```java
// Automatically logged when user role doesn't match @PreAuthorize
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser() { ... }
→ User with ROLE_USER tries to access
→ Logs as FORBIDDEN_403 with userId, username, IP
```

**404 - Not Found**
```java
// Automatically logged when ResourceNotFoundException thrown
chatSessionService.findById(999); // Non-existent ID
→ Throws ResourceNotFoundException
→ Logs as NOT_FOUND_404 with userId (if authenticated)
```

### Manual Logging (Direct Service Call)

```java
@Autowired
private AuthErrorLogService authErrorLogService;

// Log custom auth error
authErrorLogService.logAccessDenied(
    userId,
    username,
    ipAddress,
    userAgent,
    "/api/v1/custom/endpoint",
    "POST",
    "Business rule violation: User not verified"
);
```

## Monitoring & Analysis

### 1. Track Brute Force Attempts
```java
// Check if IP has too many 401 errors
Long count = authErrorLogRepository.countByIpAddressSince(
    ipAddress,
    LocalDateTime.now().minusMinutes(15)
);
if (count > 10) {
    // Block IP or trigger alert
}
```

### 2. Analyze Error Distribution
```java
List<Object[]> stats = authErrorLogRepository.getStatisticsByErrorType();
// Returns: [(UNAUTHORIZED_401, 150), (FORBIDDEN_403, 45), ...]
```

### 3. Daily Trends
```java
List<Object[]> daily = authErrorLogRepository.getDailyStatistics(
    LocalDateTime.now().minusDays(30)
);
// Returns: [(2024-01-15, 23), (2024-01-16, 31), ...]
```

### 4. User Behavior Analysis
```java
Page<AuthenticationErrorLog> userErrors = 
    authErrorLogRepository.findByUserId(userId, pageable);
// Track if specific user is probing for vulnerabilities
```

## Security Benefits

1. **Attack Detection**: Identify brute force, credential stuffing, privilege escalation
2. **Forensics**: Full audit trail with IP, timestamp, attempted action
3. **Rate Limiting**: Data for implementing IP-based rate limiting
4. **Anomaly Detection**: Unusual patterns (many 403s, geographic anomalies)
5. **Compliance**: Audit logs for security compliance (SOC 2, PCI DSS)

## Performance Considerations

- **Async Processing**: No impact on request latency
- **Separate Transaction**: Logging failures don't affect main request
- **Indexed Queries**: Fast lookups on error_type, user_id, ip_address, created_at
- **Fail-Safe**: Logging exceptions caught and logged, never crash main flow

## Integration with Existing Systems

### Admin Panel Integration (Future)
```java
// Potential admin endpoints
GET /api/v1/admin/auth-errors?errorType=FORBIDDEN_403
GET /api/v1/admin/auth-errors/user/{userId}
GET /api/v1/admin/auth-errors/ip/{ipAddress}
GET /api/v1/admin/auth-errors/statistics
DELETE /api/v1/admin/auth-errors/{id}  // Level 0 only
```

### Email Alerts (Future Enhancement)
```java
// Similar to prompt injection system
if (count403ForUser > threshold) {
    emailService.sendSecurityAlert(
        "Multiple access denied attempts for user: " + username
    );
}
```

## Testing

### Test 401 Error
```bash
# Request without token
curl -X GET http://localhost:8080/api/v1/sessions
# Check logs for UNAUTHORIZED_401 entry
```

### Test 403 Error
```bash
# User trying to access admin endpoint
curl -X GET http://localhost:8080/api/v1/admin/users \
  -H "Authorization: Bearer <user_token>"
# Check logs for FORBIDDEN_403 entry with userId
```

### Test 404 Error
```bash
# Request non-existent resource
curl -X GET http://localhost:8080/api/v1/sessions/999999 \
  -H "Authorization: Bearer <valid_token>"
# Check logs for NOT_FOUND_404 entry with userId
```

## Comparison with Prompt Injection Logging

| Feature | Auth Error Logging | Prompt Injection Logging |
|---------|-------------------|-------------------------|
| **Entity** | `AuthenticationErrorLog` | `PromptInjectionLog` |
| **Trigger** | Exception thrown | Pattern detected |
| **User Info** | From token/context | Required (user_id) |
| **Email Alerts** | Not yet (future) | Yes (threshold-based) |
| **Severity Levels** | Error type enum | LOW/MEDIUM/HIGH/CRITICAL |
| **Async** | Yes | Yes |
| **Transaction** | REQUIRES_NEW | REQUIRES_NEW |
| **Console Output** | Box format | Box format |

## Future Enhancements

1. **Email Alerts**: Notify admins of suspicious activity
2. **Auto-Blocking**: Automatically block IPs with too many 401s
3. **Admin Panel**: UI for viewing/analyzing auth errors
4. **Retention Policy**: Auto-delete old logs after X days
5. **Metrics Dashboard**: Real-time graphs of auth errors
6. **IP Geolocation**: Track geographic origin of attacks
7. **Machine Learning**: Detect anomalous patterns
8. **Integration with SIEM**: Export to security information systems

## Related Files

- **Entity**: `models/AuthenticationErrorLog.java`
- **Repository**: `repos/AuthenticationErrorLogRepository.java`
- **Service**: `services/AuthErrorLogService.java`
- **Exception Handler**: `exception/GlobalExceptionHandler.java`
- **Configuration**: `application.properties`
- **Documentation**: `docs/AUTHENTICATION_ERROR_LOGGING.md`

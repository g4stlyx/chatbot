# Email Verification Rate Limiting Implementation

## Overview
Added rate limiting to the `resendVerificationEmail` endpoint to prevent abuse and email spam.

## Problem
The `resendVerificationEmail` method did not have any rate limiting, allowing potential attackers to:
- Spam verification emails to any email address
- Overload the email service
- Harass users with repeated verification emails
- Use the service for email enumeration attacks

## Solution
Implemented rate limiting using the existing `RateLimitService` with Redis-based sliding window counter.

## Changes Made

### 1. Configuration (`application.properties`)
Added new rate limit configuration for email verification:
```properties
app.rate-limit.email-verification.attempts=${RATE_LIMIT_EMAIL_VERIFICATION_ATTEMPTS:3}
app.rate-limit.email-verification.window=${RATE_LIMIT_EMAIL_VERIFICATION_WINDOW:3600000}
```

**Default Settings:**
- **Attempts:** 3 resend requests
- **Window:** 3600000ms (1 hour)

This means a user can request verification email resend maximum 3 times per hour per email address.

### 2. RateLimitConfig
Added configuration properties and getters:
- `emailVerificationAttempts` - Maximum number of resend attempts
- `emailVerificationWindow` - Time window in milliseconds
- Corresponding getter methods

### 3. RateLimitService
Added new method:
```java
public boolean isEmailVerificationRateLimitExceeded(String email)
```
- Creates rate limit key: `email_verification_rate_limit:{email}`
- Uses Redis sliding window counter
- Returns `true` if rate limit is exceeded

### 4. AuthService
Updated `resendVerificationEmail` method:
- Injected `RateLimitService` dependency
- Added rate limit check at the beginning of the method
- Returns `true` (success) even when rate limited for security reasons
- Logs warning when rate limit is exceeded

## Security Considerations

### Why Return `true` on Rate Limit?
The method returns `true` even when rate limited to prevent information disclosure:
- Attackers can't determine if rate limiting is active
- Maintains consistent response for enumeration attacks
- Users still get a "success" response (though email may not be sent)

### Rate Limit Key
Uses email address as the rate limit key:
- Prevents spam to specific email addresses
- Works for both existing and non-existing emails
- Protects the email service from overload

### Logging
Proper logging is maintained:
- Rate limit exceeded events are logged with `WARN` level
- Includes email address for audit purposes
- Helps identify potential abuse patterns

## Environment Variables
You can customize rate limiting via environment variables:

```bash
RATE_LIMIT_EMAIL_VERIFICATION_ATTEMPTS=3    # Number of allowed attempts
RATE_LIMIT_EMAIL_VERIFICATION_WINDOW=3600000  # Time window in ms (1 hour)
```

## Testing

### Manual Testing
1. Request verification email resend for an email
2. Repeat the request 3 times within an hour
3. The 4th request should be rate limited (but will return success)
4. Check Redis for the rate limit key:
   ```
   GET email_verification_rate_limit:{email}
   ```
5. Wait for the window to expire or manually delete the key
6. Verify requests work again

### Expected Behavior
- First 3 requests: Email sent, returns success
- 4th+ requests within hour: No email sent, returns success, logs warning
- After 1 hour: Counter resets, requests work again

## Redis Integration
The rate limiting uses Redis for distributed rate limiting:
- Sliding window counter algorithm
- Automatic expiration after the time window
- Works across multiple application instances
- Fails open (allows requests if Redis is unavailable)

## Monitoring
Monitor these logs for potential abuse:
```
WARN - Rate limit exceeded for verification email resend: {email}
```

Multiple rate limit warnings for the same email or from the same IP could indicate:
- Legitimate user repeatedly requesting verification
- Automated attack attempt
- Bug in the client application

## Future Enhancements
Potential improvements:
1. Add IP-based rate limiting in addition to email-based
2. Implement exponential backoff for repeated violations
3. Add admin endpoint to manually reset rate limits
4. Create metrics/dashboard for rate limit violations
5. Send notification to admins on excessive rate limit violations

# Email Verification Resend Feature

## Overview
This feature allows users to request a new verification email if their original verification token has expired.

## Token Expiry
- Verification tokens expire after **24 hours** from creation
- Users can request a new verification email at any time before verification is complete

## API Endpoint

### POST `/api/v1/auth/resend-verification`

Resends a verification email to the specified email address.

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Response (Success):**
```json
{
  "success": true,
  "message": "If the email exists and is not verified, a new verification link has been sent."
}
```

**Response Codes:**
- `200 OK` - Request processed (always returns 200 for security)

## Security Features

1. **Email Privacy**: The endpoint always returns success, even if the email doesn't exist, to prevent email enumeration attacks
2. **Already Verified**: If the email is already verified, the endpoint returns success but doesn't send an email
3. **Token Replacement**: When a new verification email is requested, all previous tokens for that user are deleted
4. **User-Only**: Only regular users can request email verification resends (admins are auto-verified)

## Workflow

1. User registers → Verification email sent with 24-hour token
2. Token expires → User requests new verification email
3. Old tokens deleted → New token created and sent
4. User clicks link → Email verified
5. User can now login

## Implementation Details

### Files Modified

1. **AuthService.java**
   - Added `resendVerificationEmail(String email)` method
   - Handles token deletion and recreation
   - Sends new verification email

2. **AuthController.java**
   - Added `POST /resend-verification` endpoint
   - Added import for `ResendVerificationRequest`

3. **VerificationTokenRepository.java**
   - Added `deleteByUserIdAndUserType()` method
   - Allows cleanup of old tokens

4. **ResendVerificationRequest.java** (New)
   - DTO for resend verification requests
   - Contains only email field

## Usage Example

### Using cURL
```bash
curl -X POST http://localhost:8080/api/v1/auth/resend-verification \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com"}'
```

### Using JavaScript/Fetch
```javascript
const response = await fetch('/api/v1/auth/resend-verification', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    email: 'user@example.com'
  })
});

const data = await response.json();
console.log(data.message);
```

## Frontend Integration

Add a "Resend Verification Email" link on:
1. The verification pending page
2. The login page (when user tries to login with unverified email)

Example UI flow:
```
┌─────────────────────────────────────┐
│  Email Verification Required        │
│                                     │
│  Please check your email to verify  │
│  your account.                      │
│                                     │
│  Didn't receive the email?          │
│  [Resend Verification Email]        │
└─────────────────────────────────────┘
```

## Error Handling

The service logs the following scenarios:
- Non-existent email: Warning logged but success returned
- Already verified: Info logged, success returned without sending email
- Service errors: Error logged, returns false

## Testing

Test scenarios:
1. ✅ Resend verification for unverified user
2. ✅ Resend verification for already verified user (should succeed without sending)
3. ✅ Resend verification for non-existent email (should succeed without sending)
4. ✅ Verify old token is deleted when new one is created
5. ✅ Verify new token works for email verification

## Rate Limiting Considerations

**Recommendation**: Add rate limiting to this endpoint to prevent abuse:
- Max 3 requests per email per 15 minutes
- Max 10 requests per IP per hour

This can be implemented using Spring's rate limiting or a custom filter.

## Related Endpoints

- `POST /api/v1/auth/register` - Initial registration and verification email
- `GET /api/v1/auth/verify-email?token=xxx` - Verify email with token
- `POST /api/v1/auth/login` - Login (requires verified email)

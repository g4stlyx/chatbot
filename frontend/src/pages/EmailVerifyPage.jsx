import { useEffect, useState } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';

const EmailVerifyPage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState('verifying'); // verifying, success, error
  const [message, setMessage] = useState('');
  const token = searchParams.get('token');

  useEffect(() => {
    const verifyEmail = async () => {
      if (!token) {
        setStatus('error');
        setMessage('No verification token provided');
        return;
      }

      try {
        const response = await fetch(
          `http://localhost:8080/api/v1/auth/verify-email?token=${token}`,
          {
            method: 'GET',
          }
        );

        const data = await response.json();

        if (response.ok && data.success) {
          setStatus('success');
          setMessage(data.message || 'Email verified successfully!');
        } else {
          setStatus('error');
          setMessage(data.message || 'Verification failed. Token may be invalid or expired.');
        }
      } catch (error) {
        setStatus('error');
        setMessage('Failed to verify email. Please try again later.');
      }
    };

    verifyEmail();
  }, [token]);

  return (
    <div className="auth-container">
      <div className="auth-card" style={{ textAlign: 'center', maxWidth: '500px' }}>
        {status === 'verifying' && (
          <>
            <div style={{ fontSize: '48px', marginBottom: '20px' }}>⏳</div>
            <h1 className="auth-title">Verifying Your Email</h1>
            <p style={{ color: '#6b7280' }}>Please wait while we verify your email address...</p>
          </>
        )}

        {status === 'success' && (
          <>
            <div style={{ fontSize: '64px', marginBottom: '20px' }}>✅</div>
            <h1 className="auth-title" style={{ color: '#10b981' }}>Email Verified!</h1>
            <p style={{ color: '#6b7280', marginBottom: '30px', lineHeight: '1.6' }}>
              {message}
            </p>
            <button
              onClick={() => navigate('/login')}
              className="btn-primary"
              style={{ width: '100%' }}
            >
              Go to Login
            </button>
          </>
        )}

        {status === 'error' && (
          <>
            <div style={{ fontSize: '64px', marginBottom: '20px' }}>❌</div>
            <h1 className="auth-title" style={{ color: '#ef4444' }}>Verification Failed</h1>
            <p style={{ color: '#6b7280', marginBottom: '30px', lineHeight: '1.6' }}>
              {message}
            </p>
            <div style={{ display: 'flex', gap: '10px' }}>
              <button
                onClick={() => navigate('/register')}
                className="btn-primary"
                style={{ flex: 1 }}
              >
                Sign Up Again
              </button>
              <button
                onClick={() => navigate('/login')}
                style={{
                  flex: 1,
                  padding: '12px',
                  border: '2px solid var(--primary-color)',
                  backgroundColor: 'white',
                  color: 'var(--primary-color)',
                  borderRadius: '8px',
                  fontWeight: '600',
                  cursor: 'pointer',
                }}
              >
                Try Login
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default EmailVerifyPage;

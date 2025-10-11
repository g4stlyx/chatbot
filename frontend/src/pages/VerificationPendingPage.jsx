import { useLocation, useNavigate } from 'react-router-dom';
import { useEffect } from 'react';

const VerificationPendingPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const email = location.state?.email;

  useEffect(() => {
    // Redirect to register if no email provided
    if (!email) {
      navigate('/register');
    }
  }, [email, navigate]);

  return (
    <div className="auth-container">
      <div className="auth-card" style={{ textAlign: 'center', maxWidth: '500px' }}>
        <div style={{ fontSize: '64px', marginBottom: '20px' }}>📧</div>
        
        <h1 className="auth-title" style={{ marginBottom: '15px' }}>
          Check Your Mailbox
        </h1>
        
        <p style={{ 
          color: '#6b7280', 
          fontSize: '16px', 
          lineHeight: '1.6',
          marginBottom: '25px' 
        }}>
          We've sent a verification email to
          <br />
          <strong style={{ color: '#1f2937' }}>{email}</strong>
        </p>

        <div style={{
          backgroundColor: '#f3f4f6',
          padding: '20px',
          borderRadius: '8px',
          marginBottom: '25px',
          textAlign: 'left'
        }}>
          <p style={{ 
            color: '#4b5563', 
            fontSize: '14px',
            marginBottom: '10px',
            fontWeight: '600'
          }}>
            Next steps:
          </p>
          <ol style={{ 
            color: '#6b7280', 
            fontSize: '14px',
            paddingLeft: '20px',
            margin: 0,
            lineHeight: '1.8'
          }}>
            <li>Check your inbox (and spam folder)</li>
            <li>Click the verification link in the email</li>
            <li>Return to login with your credentials</li>
          </ol>
        </div>

        <div style={{
          backgroundColor: '#fef3c7',
          border: '1px solid #fbbf24',
          borderRadius: '8px',
          padding: '15px',
          marginBottom: '25px'
        }}>
          <p style={{ 
            color: '#92400e', 
            fontSize: '14px',
            margin: 0,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}>
            <span style={{ marginRight: '8px', fontSize: '18px' }}>⚠️</span>
            You must verify your email before logging in
          </p>
        </div>

        <button
          onClick={() => navigate('/login')}
          className="btn-primary"
          style={{ width: '100%', marginBottom: '15px' }}
        >
          Go to Login
        </button>

        <p style={{ color: '#9ca3af', fontSize: '14px', margin: 0 }}>
          Didn't receive the email?{' '}
          <button
            onClick={() => navigate('/register')}
            style={{
              background: 'none',
              border: 'none',
              color: 'var(--primary-color)',
              cursor: 'pointer',
              textDecoration: 'underline',
              padding: 0,
              font: 'inherit'
            }}
          >
            Try signing up again
          </button>
        </p>
      </div>
    </div>
  );
};

export default VerificationPendingPage;

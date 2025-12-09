import { Navigate, useLocation } from "react-router-dom";
import { useAdmin } from "../../context/AdminContext";

const AdminProtectedRoute = ({ children, requiredLevel = 2 }) => {
  const { isAuthenticated, loading, hasLevel } = useAdmin();
  const location = useLocation();

  if (loading) {
    return (
      <div className="admin-loading">
        <div className="admin-loading-spinner"></div>
        <p>Yükleniyor...</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    // Redirect to admin login, save the attempted URL
    return <Navigate to="/admin/login" state={{ from: location }} replace />;
  }

  // Check if admin has required level
  if (!hasLevel(requiredLevel)) {
    return (
      <div className="admin-unauthorized">
        <h2>Yetkisiz Erişim</h2>
        <p>Bu sayfaya erişim yetkiniz bulunmamaktadır.</p>
        <button onClick={() => window.history.back()}>Geri Dön</button>
      </div>
    );
  }

  return children;
};

export default AdminProtectedRoute;

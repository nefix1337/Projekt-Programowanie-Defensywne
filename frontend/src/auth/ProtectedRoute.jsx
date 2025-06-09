import { useContext } from "react";
import { Navigate } from "react-router-dom";
import { AuthContext } from "./AuthProvider";

const ProtectedRoute = ({ children, allowedRoles }) => {
  const { user } = useContext(AuthContext);

  console.log('Current user role:', user?.role);
  console.log('Allowed roles:', allowedRoles);

  if (!user) {
    return <Navigate to="/login" />;
  }

  if (!allowedRoles.includes(user.role)) {
    console.log('Access denied: user role', user.role, 'not in allowed roles:', allowedRoles);
    return <Navigate to="/unauthorized" />;
  }

  return children;
};

export default ProtectedRoute;
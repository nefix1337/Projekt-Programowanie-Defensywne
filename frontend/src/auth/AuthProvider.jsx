import { createContext, useState, useContext, useEffect } from 'react';
import api from '../api/axiosInstance';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [accessToken, setAccessToken] = useState(null);
  const [userData, setUserData] = useState(null);

  useEffect(() => {
    const fetchUserData = async () => {
      if (!accessToken) return;

      const parsedUser = parseJwt(accessToken);
      setUser(parsedUser);

      try {
        const userResponse = await api.get('/users/me', {
          headers: { Authorization: `Bearer ${accessToken}` },
        });
        setUserData(userResponse.data);
      } catch (error) {
        console.error('Error fetching user data:', error);
        setUserData(null);
      }
    };

    fetchUserData();
  }, [accessToken]);

  const login = async (email, password) => {
    const res = await api.post('/auth/login', { email, password });

    if (res.data.requires2FA) {
      return { requires2FA: true };
    }

    console.log(parseJwt(res.data.token).role)
    setAccessToken(res.data.token);
    return { requires2FA: false, role: parseJwt(res.data.token).role };
  };

  const register = async (firstName, lastName, email, password) => {
    try {
      const res = await api.post('/auth/register', {
        firstName,
        lastName,
        email,
        password,
      });
      return res.data;
    } catch (error) {
      console.error('Error during registration:', error);
      throw new Error('Registration failed. Please try again.');
    }
  };

  const verify2FA = async (totpCode, email) => {
    try {
      const res = await api.post('/auth/2fa/verify', {
        totpCode,
        email,
      });
      setAccessToken(res.data.token);
    } catch (error) {
      console.error('Error during 2FA verification:', error);
      throw new Error('2FA verification failed. Please try again.');
    }
  };

  const logout = () => {
    setAccessToken(null);
    setUser(null);
    setUserData(null);
  };

  const getToken = () => {
    return accessToken;
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        userData,
        accessToken,
        login,
        register,
        verify2FA,
        logout,
        getToken,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);

function parseJwt(token) {
  if (!token) return null;
  const base64Url = token.split('.')[1];
  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
  const jsonPayload = decodeURIComponent(
    atob(base64)
      .split('')
      .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
      .join('')
  );

  return JSON.parse(jsonPayload);
}

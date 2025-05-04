import { createContext, useState, useContext } from 'react';
import api from '../api/axiosInstance';


export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [accessToken, setAccessToken] = useState(null);

  const login = async (email, password) => {
    const res = await api.post('/auth/login', { email, password });
    
    if (res.data.requires2FA) {
      return { requires2FA: true };
    }

    console.log(res)
  

    setAccessToken(res.data.token);
    setUser(parseJwt(res.data.token));
    return { requires2FA: false };
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
      setUser(parseJwt(res.data.token));
    } catch (error) {
      console.error('Error during 2FA verification:', error);
      throw new Error('2FA verification failed. Please try again.');
    }
  };

  const logout = () => {
    setAccessToken(null);
    setUser(null);
  };

  const getToken = () => {
    return accessToken;
  };

  return (
    <AuthContext.Provider value={{ user, accessToken, login, register, verify2FA, logout, getToken }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);

function parseJwt(token) {
  if (!token) return null;
  const base64Url = token.split('.')[1];
  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
  const jsonPayload = decodeURIComponent(atob(base64).split('').map(c =>
    '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
  ).join(''));

  return JSON.parse(jsonPayload);
}

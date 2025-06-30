import { useState, useContext } from "react";
import { AuthContext } from "../../auth/AuthProvider";
import { useNavigate, Link } from "react-router-dom";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

const Login = () => {
  const { login, verify2FA } = useContext(AuthContext); 
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [code, setCode] = useState(""); 
  const [error, setError] = useState("");
  const [requires2FA, setRequires2FA] = useState(false); 
  const navigate = useNavigate();

  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    try {
      const result = await login(email, password);
      if (result.requires2FA) {
        setRequires2FA(true);
      } else {
      
        if (result.role === "ROLE_ADMIN") {
          navigate("/admin");
        } else {
          navigate("/dashboard");
        }
      }
    } catch (err) {
      setError("Nieprawidłowe dane logowania");
    }
  };

  const handle2FASubmit = async (e) => {
    e.preventDefault();
    try {
      await verify2FA(code,email);
      navigate("/dashboard");
    } catch (err) {
      setError("Błędny kod 2FA");
    }
  };

  return (
    <div className="flex justify-center items-center w-screen h-screen">
      <Card className="w-full max-w-md p-6">
        <CardContent>
          <h2 className="text-2xl font-bold mb-4 text-center">
            {requires2FA ? "Weryfikacja 2FA" : "Logowanie"}
          </h2>
          {requires2FA ? (
            // Formularz 2FA
            <form onSubmit={handle2FASubmit} className="flex flex-col gap-4">
              <Input
                value={code}
                onChange={(e) => setCode(e.target.value)}
                placeholder="Kod 2FA"
                required
              />
              {error && <p className="text-red-500 text-sm">{error}</p>}
              <Button type="submit">Zweryfikuj</Button>
            </form>
          ) : (
            // Formularz logowania
            <>
              <form onSubmit={handleLoginSubmit} className="flex flex-col gap-4">
                <Input
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="Email"
                  type="email"
                  required
                />
                <Input
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Hasło"
                  type="password"
                  required
                />
                {error && <p className="text-red-500 text-sm">{error}</p>}
                <Button type="submit">Zaloguj się</Button>
              </form>
              <div className="text-center mt-2">
                <span className="text-sm text-gray-600">Nie masz konta? </span>
                <Link to="/register" className="text-blue-600 hover:underline text-sm">
                  Zarejestruj się
                </Link>
              </div>
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default Login;
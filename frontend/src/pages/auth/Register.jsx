import { useState } from "react";
import { useAuth } from "@/auth/AuthProvider";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Link } from "react-router-dom";

const Register = () => {
  const { register } = useAuth();
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    confirmPassword: "",
  });
  const [error, setError] = useState("");

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const validate = (data) => {
    if (!data.firstName || data.firstName.length < 2 || data.firstName.length > 20) {
      return "Imię musi mieć od 2 do 20 znaków";
    }
    if (!data.lastName || data.lastName.length < 2 || data.lastName.length > 20) {
      return "Nazwisko musi mieć od 2 do 20 znaków";
    }
    if (!data.email || data.email.length > 50) {
      return "Email jest wymagany i nie może mieć więcej niż 50 znaków";
    }
    // Prosta walidacja emaila
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email)) {
      return "Podaj poprawny adres email";
    }
    if (!data.password || data.password.length < 6 || data.password.length > 40) {
      return "Hasło musi mieć od 6 do 40 znaków";
    }
    if (data.password !== data.confirmPassword) {
      return "Hasła nie są zgodne";
    }
    return "";
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const validationError = validate(formData);
    if (validationError) {
      setError(validationError);
      return;
    }
    setError("");
    try {
      const response = await register(
        formData.firstName,
        formData.lastName,
        formData.email,
        formData.password
      );
      console.log("Rejestracja zakończona sukcesem:", response);
    } catch (err) {
      console.error("Rejestracja nie powiodła się:", err);
      setError("Rejestracja nie powiodła się. Spróbuj ponownie.");
    }
  };

  return (
    <div className="flex justify-center items-center w-screen h-screen">
      <Card className="w-full max-w-md p-6">
        <CardContent>
          <h2 className="text-2xl font-bold mb-4 text-center">Rejestracja</h2>
          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <Input
              type="text"
              id="firstName"
              name="firstName"
              value={formData.firstName}
              onChange={handleChange}
              placeholder="Imię"
              required
            />
            <Input
              type="text"
              id="lastName"
              name="lastName"
              value={formData.lastName}
              onChange={handleChange}
              placeholder="Nazwisko"
              required
            />
            <Input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="Email"
              required
            />
            <Input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="Hasło"
              required
            />
            <Input
              type="password"
              id="confirmPassword"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleChange}
              placeholder="Potwierdź hasło"
              required
            />
            {error && (
              <div className="text-red-600 text-sm mb-2">{error}</div>
            )}
            <Button type="submit">Zarejestruj się</Button>
          </form>
          <div className="text-center mt-2">
            <span className="text-sm text-gray-600">Masz już konto? </span>
            <Link to="/login" className="text-blue-600 hover:underline text-sm">
              Zaloguj się
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default Register;
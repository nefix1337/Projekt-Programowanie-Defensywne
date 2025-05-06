import { useState } from "react";
import { useAuth } from "@/auth/AuthProvider";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

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

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (formData.password !== formData.confirmPassword) {
      setError("Hasła nie są zgodne");
      return;
    }

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
            {error && <p className="text-red-500 text-sm">{error}</p>}
            <Button type="submit">Zarejestruj się</Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
};

export default Register;
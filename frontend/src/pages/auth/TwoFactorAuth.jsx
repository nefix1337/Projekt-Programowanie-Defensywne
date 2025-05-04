import { useState, useContext } from "react";
import { AuthContext } from "../../auth/AuthProvider";
import { useNavigate } from "react-router-dom";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

const TwoFactorAuth = () => {
  const { verify2FA } = useContext(AuthContext);
  const [code, setCode] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await verify2FA(code);
      navigate("/dashboard");
    } catch (err) {
      console.error("Błędny kod 2FA");
    }
  };

  return (
    <div className="flex justify-center items-center w-screen h-screen">
      <Card className="w-full max-w-md p-6">
        <CardContent>
          <h2 className="text-2xl font-bold mb-4 text-center">Weryfikacja 2FA</h2>
          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <Input
              value={code}
              onChange={(e) => setCode(e.target.value)}
              placeholder="Kod 2FA"
              required
            />
            <Button type="submit">Zweryfikuj</Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
};

export default TwoFactorAuth;

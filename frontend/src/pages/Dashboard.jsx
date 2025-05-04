"use client";
import { useContext } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { AuthContext } from "../auth/AuthProvider";
import api from "../api/axiosInstance";
import { useState } from "react";

export default function Dashboard() {

  const [qrCodeImage, setQrCodeImage] = useState("");
  const [loading, setLoading] = useState(false);
  const { getToken} = useContext(AuthContext);

  const handleEnable2FA = async () => {
    try {
      setLoading(true);
      const token = getToken(); 
      const response = await api.post(
        "/auth/2fa/enable",
        {}, 
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      setQrCodeImage(response.data.qrCodeImage);
    } catch (error) {
      console.error("Błąd przy włączaniu 2FA:", error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col items-center p-6 space-y-6">
      <Card className="w-full max-w-2xl">
        <CardHeader>
          <CardTitle>Dashboard</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
        

          <Button onClick={handleEnable2FA} disabled={loading}>
            {loading ? "Ładowanie..." : "Aktywuj 2FA"}
          </Button>

          {qrCodeImage && (
            <div className="flex flex-col items-center mt-4 space-y-2">
              <p>Zeskanuj kod w aplikacji Google Authenticator:</p>
              <img src={qrCodeImage} alt="Kod QR" className="w-60 h-60" />
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

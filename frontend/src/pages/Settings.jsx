import { useContext, useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { AuthContext } from "../auth/AuthProvider";
import api from "../api/axiosInstance";

export default function Settings() {
  const [qrCodeImage, setQrCodeImage] = useState("");
  const [loading, setLoading] = useState(false);
  const { getToken } = useContext(AuthContext);

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
    <div className="w-full flex flex-col items-center ">
      
        
        
          <Tabs defaultValue="general" className="w-full">
            <TabsList>
              <TabsTrigger  value="general">Ogólne informacje</TabsTrigger>
              <TabsTrigger  value="2fa">2FA</TabsTrigger>
            </TabsList>

            {/* Zakładka Ogólne informacje */}
            <TabsContent value="general">
              <div className="p-4">
                <p>Tu będą ogólne informacje o użytkowniku.</p>
              </div>
            </TabsContent>

            {/* Zakładka 2FA */}
            <TabsContent value="2fa">
              <div className="p-4">
                <Button onClick={handleEnable2FA} disabled={loading}>
                  {loading ? "Ładowanie..." : "Aktywuj 2FA"}
                </Button>

                {qrCodeImage && (
                  <div className="flex flex-col items-center mt-4 space-y-2">
                    <p>Zeskanuj kod w aplikacji Google Authenticator:</p>
                    <img src={qrCodeImage} alt="Kod QR" className="w-60 h-60" />
                  </div>
                )}
              </div>
            </TabsContent>
          </Tabs>
        
      
    </div>
  );
}

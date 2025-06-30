import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { toast } from "sonner";
import api from "@/api/axiosInstance";
import { useAuth } from "@/auth/AuthProvider";

const EditProject = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { getToken } = useAuth();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({
    name: "",
    description: "",
    icon: "",
    status: "TODO",
  });

  useEffect(() => {
    const fetchProject = async () => {
      try {
        const response = await api.get(`/projects/${id}`, {
          headers: { Authorization: `Bearer ${getToken()}` },
        });
        setForm({
          name: response.data.name || "",
          description: response.data.description || "",
          icon: response.data.icon || "",
          status: response.data.status || "TODO",
        });
      } catch (error) {
        toast.error("Nie udało się pobrać projektu");
      } finally {
        setLoading(false);
      }
    };
    fetchProject();
  }, [id, getToken]);

  const handleChange = (e) => {
    setForm((prev) => ({
      ...prev,
      [e.target.name]: e.target.value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await api.put(
        `/projects/${id}`,
        form,
        { headers: { Authorization: `Bearer ${getToken()}` } }
      );
      toast.success("Projekt zaktualizowany");
      navigate(`/dashboard/projects/${id}`);
    } catch (error) {
      toast.error("Nie udało się zaktualizować projektu");
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div>Ładowanie...</div>;

  return (
    <div className="p-8 flex justify-center">
      <Card className="w-full max-w-xl">
        <CardHeader>
          <CardTitle>Edytuj projekt</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs mb-1">Nazwa projektu</label>
              <Input
                name="name"
                value={form.name}
                onChange={handleChange}
                required
              />
            </div>
            <div>
              <label className="block text-xs mb-1">Opis</label>
              <Input
                name="description"
                value={form.description}
                onChange={handleChange}
              />
            </div>
            <div>
              <label className="block text-xs mb-1">Ikona (emoji)</label>
              <Input
                name="icon"
                value={form.icon}
                onChange={handleChange}
                maxLength={2}
              />
            </div>
            <div>
              <label className="block text-xs mb-1">Status</label>
              <select
                name="status"
                value={form.status}
                onChange={handleChange}
                className="border rounded px-2 py-1 w-full"
              >
                <option value="TODO">TODO</option>
                <option value="IN_PROGRESS">IN_PROGRESS</option>
                <option value="DONE">DONE</option>
                <option value="TO_REVIEW">TO_REVIEW</option>
                <option value="VERIFIED">VERIFIED</option>
                <option value="ARCHIVED">ARCHIVED</option>
              </select>
            </div>
            <div className="flex gap-2 mt-4">
              <Button type="submit" disabled={saving}>
                {saving ? "Zapisywanie..." : "Zapisz zmiany"}
              </Button>
              <Button
                type="button"
                variant="outline"
                onClick={() => navigate(-1)}
              >
                Anuluj
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
};

export default EditProject;
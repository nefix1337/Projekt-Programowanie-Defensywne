import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select, SelectTrigger, SelectValue, SelectContent, SelectItem } from "@/components/ui/select";
import { Popover, PopoverTrigger, PopoverContent } from "@/components/ui/popover";
import { toast } from "sonner";
import api from "@/api/axiosInstance";
import { useAuth } from "@/auth/AuthProvider";
import EmojiPicker from "emoji-picker-react";

const STATUS_OPTIONS = [
  { value: "TODO", label: "Do zrobienia" },
  { value: "IN_PROGRESS", label: "W trakcie" },
  { value: "DONE", label: "Zrobione" },
  { value: "TO_REVIEW", label: "Do sprawdzenia" },
  { value: "VERIFIED", label: "Zweryfikowane" },
  { value: "ARCHIVED", label: "Zarchiwizowane" },
];

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
  const [showEmojiPicker, setShowEmojiPicker] = useState(false);

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

  const handleEmojiSelect = (emojiData) => {
    setForm((prev) => ({
      ...prev,
      icon: emojiData.emoji || emojiData.native || "",
    }));
    setShowEmojiPicker(false);
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
              <Popover open={showEmojiPicker} onOpenChange={setShowEmojiPicker}>
                <PopoverTrigger asChild>
                  <Button
                    type="button"
                    variant="outline"
                    className="w-16 h-10 text-2xl"
                  >
                    {form.icon || "🙂"}
                  </Button>
                </PopoverTrigger>
                <PopoverContent className="w-auto p-0">
                  <EmojiPicker
                    onEmojiClick={handleEmojiSelect}
                    width={300}
                    height={400}
                  />
                </PopoverContent>
              </Popover>
            </div>
            <div>
              <label className="block text-xs mb-1">Status</label>
              <Select value={form.status} onValueChange={val => setForm(prev => ({ ...prev, status: val }))}>
                <SelectTrigger className="w-full">
                  <SelectValue placeholder="Wybierz status" />
                </SelectTrigger>
                <SelectContent>
                  {STATUS_OPTIONS.map((status) => (
                    <SelectItem key={status.value} value={status.value}>
                      {status.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
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
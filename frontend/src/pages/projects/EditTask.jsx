import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
} from "@/components/ui/select";
import { toast } from "sonner";
import api from "@/api/axiosInstance";
import { useAuth } from "@/auth/AuthProvider";

const STATUS_OPTIONS = [
  { value: "TODO", label: "Do zrobienia" },
  { value: "IN_PROGRESS", label: "W trakcie" },
  { value: "DONE", label: "Zrobione" },
  { value: "TO_REVIEW", label: "Do sprawdzenia" },
  { value: "VERIFIED", label: "Zweryfikowane" },
  { value: "ARCHIVED", label: "Zarchiwizowane" },
];
const PRIORITY_OPTIONS = [
  { value: "LOW", label: "Niski" },
  { value: "MEDIUM", label: "Średni" },
  { value: "HIGH", label: "Wysoki" },
];

const EditTask = () => {
  const { id, taskId } = useParams();
  const navigate = useNavigate();
  const { getToken } = useAuth();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({
    title: "",
    description: "",
    status: "TODO",
    priority: "LOW",
    dueDate: "",
  });

  useEffect(() => {
    const fetchTask = async () => {
      try {
        const response = await api.get(`/tasks/${taskId}/details`, {
          headers: { Authorization: `Bearer ${getToken()}` },
        });
        const t = response.data;
        setForm({
          title: t.title || "",
          description: t.description || "",
          status: t.status || "TODO",
          priority: t.priority || "LOW",
          dueDate: t.dueDate ? t.dueDate.slice(0, 16) : "",
        });
      } catch (error) {
        toast.error("Nie udało się pobrać zadania");
      } finally {
        setLoading(false);
      }
    };
    fetchTask();
  }, [taskId, getToken]);

  const handleChange = (e) => {
    setForm((prev) => ({
      ...prev,
      [e.target.name]: e.target.value,
    }));
  };

  const handleStatusChange = (value) => {
    setForm((prev) => ({
      ...prev,
      status: value,
    }));
  };

  const handlePriorityChange = (value) => {
    setForm((prev) => ({
      ...prev,
      priority: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await api.put(
        `/tasks/${taskId}`,
        {
          ...form,
          dueDate: form.dueDate ? form.dueDate : null,
        },
        {
          headers: { Authorization: `Bearer ${getToken()}` },
        }
      );
      toast.success("Zadanie zaktualizowane");
      navigate(`/dashboard/projects/${id}/tasks/${taskId}`);
    } catch (error) {
      toast.error("Nie udało się zaktualizować zadania");
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div>Ładowanie...</div>;

  return (
    <div className="p-8">
      <Card>
        <CardHeader>
          <CardTitle>Edytuj zadanie</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs mb-1">Tytuł</label>
              <Input
                type="text"
                name="title"
                value={form.title}
                onChange={handleChange}
                required
              />
            </div>
            <div>
              <label className="block text-xs mb-1">Opis</label>
              <Textarea
                name="description"
                value={form.description}
                onChange={handleChange}
                rows={3}
              />
            </div>
            <div>
              <label className="block text-xs mb-1">Status</label>
              <Select
                value={form.status}
                onValueChange={handleStatusChange}
                defaultValue={form.status}
              >
                <SelectTrigger>
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
            <div>
              <label className="block text-xs mb-1">Priorytet</label>
              <Select
                value={form.priority}
                onValueChange={handlePriorityChange}
                defaultValue={form.priority}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Wybierz priorytet" />
                </SelectTrigger>
                <SelectContent>
                  {PRIORITY_OPTIONS.map((priority) => (
                    <SelectItem key={priority.value} value={priority.value}>
                      {priority.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <label className="block text-xs mb-1">Termin</label>
              <Input
                type="datetime-local"
                name="dueDate"
                value={form.dueDate}
                onChange={handleChange}
              />
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

export default EditTask;
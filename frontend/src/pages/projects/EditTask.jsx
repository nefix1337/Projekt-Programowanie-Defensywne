import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { toast } from "sonner";
import api from "@/api/axiosInstance";
import { useAuth } from "@/auth/AuthProvider";

const STATUS_OPTIONS = [
  "TODO",
  "IN_PROGRESS",
  "DONE",
  "TO_REVIEW",
  "VERIFIED",
  "ARCHIVED",
];
const PRIORITY_OPTIONS = ["LOW", "MEDIUM", "HIGH"];

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
              <input
                type="text"
                name="title"
                value={form.title}
                onChange={handleChange}
                className="border rounded px-2 py-1 w-full"
                required
              />
            </div>
            <div>
              <label className="block text-xs mb-1">Opis</label>
              <textarea
                name="description"
                value={form.description}
                onChange={handleChange}
                className="border rounded px-2 py-1 w-full"
                rows={3}
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
                {STATUS_OPTIONS.map((status) => (
                  <option key={status} value={status}>
                    {status}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-xs mb-1">Priorytet</label>
              <select
                name="priority"
                value={form.priority}
                onChange={handleChange}
                className="border rounded px-2 py-1 w-full"
              >
                {PRIORITY_OPTIONS.map((priority) => (
                  <option key={priority} value={priority}>
                    {priority}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-xs mb-1">Termin</label>
              <input
                type="datetime-local"
                name="dueDate"
                value={form.dueDate}
                onChange={handleChange}
                className="border rounded px-2 py-1 w-full"
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
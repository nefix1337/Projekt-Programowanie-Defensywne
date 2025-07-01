import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useAuth } from "@/auth/AuthProvider";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { toast } from "sonner";
import api from "@/api/axiosInstance";

const TASK_STATUSES = [
  { value: "TODO", label: "Do zrobienia" },
  { value: "IN_PROGRESS", label: "W trakcie" },
  { value: "DONE", label: "Zrobione" },
  { value: "VERIFIED", label: "Zweryfikowane" },
  { value: "ARCHIVED", label: "Zarchiwizowane" },
];

const TASK_PRIORITIES = [
  { value: "LOW", label: "Niski" },
  { value: "MEDIUM", label: "Średni" },
  { value: "HIGH", label: "Wysoki" },
];

const AddTask = () => {
  const { id: projectId } = useParams();
  const navigate = useNavigate();
  const { getToken } = useAuth();
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({
    title: "",
    description: "",
    status: "TODO",
    priority: "MEDIUM",
    dueDate: "",
    assignedToId: "",
  });

  useEffect(() => {
    const fetchMembers = async () => {
      try {
        const response = await api.get(`/projects/${projectId}/members`, {
          headers: { Authorization: `Bearer ${getToken()}` }
        });
        setMembers(Object.values(response.data));
      } catch (error) {
        toast.error("Nie udało się pobrać członków projektu");
      } finally {
        setLoading(false);
      }
    };
    fetchMembers();
  }, [projectId, getToken]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSelectChange = (name, value) => {
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.title || !form.assignedToId) {
      toast.error("Tytuł i przypisany użytkownik są wymagane");
      return;
    }

    
    let dueDateFormatted = form.dueDate ? form.dueDate.replace("T", "T").slice(0, 19) : null;

    try {
      await api.post(
        "/tasks",
        {
          projectId,
          ...form,
          dueDate: dueDateFormatted,
        },
        { headers: { Authorization: `Bearer ${getToken()}` } }
      );
      toast.success("Zadanie zostało dodane");
      navigate(`/dashboard/projects/${projectId}`);
    } catch (error) {
      toast.error("Nie udało się dodać zadania");
    }
  };

  if (loading) return <div>Ładowanie...</div>;

  return (
    <div className="p-8">
      <Card>
        <CardHeader>
          <CardTitle>Dodaj zadanie</CardTitle>
        </CardHeader>
        <CardContent>
          <form className="space-y-4" onSubmit={handleSubmit}>
            <div>
              <label className="text-sm font-medium">Tytuł</label>
              <Input
                name="title"
                value={form.title}
                onChange={handleChange}
                required
              />
            </div>
            <div>
              <label className="text-sm font-medium">Opis</label>
              <Textarea
                name="description"
                value={form.description}
                onChange={handleChange}
              />
            </div>
            <div className="flex gap-4">
              <div className="flex-1">
                <label className="text-sm font-medium">Status</label>
                <Select
                  value={form.status}
                  onValueChange={(val) => handleSelectChange("status", val)}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Wybierz status" />
                  </SelectTrigger>
                  <SelectContent>
                    {TASK_STATUSES.map((status) => (
                      <SelectItem key={status.value} value={status.value}>
                        {status.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="flex-1">
                <label className="text-sm font-medium">Priorytet</label>
                <Select
                  value={form.priority}
                  onValueChange={(val) => handleSelectChange("priority", val)}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Wybierz priorytet" />
                  </SelectTrigger>
                  <SelectContent>
                    {TASK_PRIORITIES.map((priority) => (
                      <SelectItem key={priority.value} value={priority.value}>
                        {priority.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
            <div>
              <label className="text-sm font-medium">Termin</label>
              <Input
                type="datetime-local"
                name="dueDate"
                value={form.dueDate}
                onChange={handleChange}
              />
            </div>
            <div>
              <label className="text-sm font-medium">Przypisz do członka projektu</label>
              <Select
                value={form.assignedToId}
                onValueChange={(val) => handleSelectChange("assignedToId", val)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Wybierz użytkownika" />
                </SelectTrigger>
                <SelectContent>
                  {members.map((member) => (
                    <SelectItem key={member.userId} value={member.userId.toString()}>
                      {member.firstName} {member.lastName} ({member.userEmail})
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="flex justify-end gap-2">
              <Button type="button" variant="outline" onClick={() => navigate(-1)}>
                Anuluj
              </Button>
              <Button type="submit">Dodaj zadanie</Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
};

export default AddTask;
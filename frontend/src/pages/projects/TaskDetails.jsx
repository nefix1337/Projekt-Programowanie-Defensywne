import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { toast } from "sonner";
import api from "@/api/axiosInstance";
import { useAuth } from "@/auth/AuthProvider";
import { Trash2 } from "lucide-react";

const TaskDetails = () => {
  const { id, taskId } = useParams();
  const navigate = useNavigate();
  const { getToken, user } = useAuth();
  const [task, setTask] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchTask = async () => {
      try {
        const response = await api.get(`/tasks/${taskId}/details`, {
          headers: { Authorization: `Bearer ${getToken()}` },
        });
        setTask(response.data);
      } catch (error) {
        toast.error("Nie udało się pobrać szczegółów zadania");
      } finally {
        setLoading(false);
      }
    };
    fetchTask();
  }, [taskId, getToken]);

  const handleDelete = async () => {
    if (!window.confirm("Czy na pewno chcesz usunąć to zadanie?")) return;
    try {
      await api.delete(`/tasks/${taskId}`, {
        headers: { Authorization: `Bearer ${getToken()}` },
      });
      toast.success("Zadanie zostało usunięte");
      navigate(`/dashboard/projects/${id}`);
    } catch (error) {
      toast.error("Nie udało się usunąć zadania");
    }
  };

  if (loading) return <div>Ładowanie...</div>;
  if (!task) return <div>Nie znaleziono zadania</div>;


  const canDelete =
    user?.role === "ROLE_MANAGER" ||
    user?.email === task.creatorEmail;

  return (
    <div className="p-8">
      <Card>
        <CardHeader>
          <CardTitle>{task.title}</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="mb-2">
            <span className="font-medium">Opis:</span>
            <div>{task.description || "Brak opisu"}</div>
          </div>
          <div className="mb-2">
            <span className="font-medium">Status:</span> {task.status}
          </div>
          <div className="mb-2">
            <span className="font-medium">Priorytet:</span> {task.priority}
          </div>
          <div className="mb-2">
            <span className="font-medium">Termin:</span>{" "}
            {task.dueDate && new Date(task.dueDate).toLocaleString("pl-PL")}
          </div>
          <div className="mb-2">
            <span className="font-medium">Utworzył:</span>{" "}
            {task.creatorFirstName} {task.creatorLastName}
            {task.creatorEmail && (
              <span className="ml-2 text-gray-500">({task.creatorEmail})</span>
            )}
          </div>
          <div className="flex gap-2 mt-4">
            <Button variant="outline" onClick={() => navigate(-1)}>
              Powrót
            </Button>
            {canDelete && (
              <Button
                variant="destructive"
                onClick={handleDelete}
                className="flex items-center gap-2"
              >
                <Trash2 className="h-4 w-4" />
                Usuń zadanie
              </Button>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default TaskDetails;
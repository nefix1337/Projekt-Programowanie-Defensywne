import { useState, useEffect } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { useAuth } from "@/auth/AuthProvider";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { UserPlus, Edit, PlusCircle, Trash2 } from "lucide-react";
import { toast } from "sonner";
import api from "@/api/axiosInstance";

const ProjectDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { getToken, user } = useAuth();
  const [project, setProject] = useState(null);
  const [loading, setLoading] = useState(true);
  const [tasks, setTasks] = useState([]);
  const [tasksLoading, setTasksLoading] = useState(true);

  useEffect(() => {
    const fetchProjectDetails = async () => {
      try {
        const response = await api.get(`/projects/${id}`, {
          headers: { Authorization: `Bearer ${getToken()}` }
        });
        setProject(response.data);
      } catch (error) {
        console.error("Error fetching project details:", error);
        toast.error("Nie udało się pobrać szczegółów projektu");
      } finally {
        setLoading(false);
      }
    };

    fetchProjectDetails();
  }, [id, getToken]);

  useEffect(() => {
    const fetchTasks = async () => {
      setTasksLoading(true);
      try {
        let response;
        if (user.role === "ROLE_MANAGER") {
          response = await api.get(`/tasks/project/${id}/all`, {
            headers: { Authorization: `Bearer ${getToken()}` }
          });
          setTasks(response.data);
        } else {
          response = await api.get("/tasks/my", {
            headers: { Authorization: `Bearer ${getToken()}` }
          });
          setTasks(response.data); // <-- nie filtruj!
        }
      } catch (error) {
        toast.error("Nie udało się pobrać zadań");
      } finally {
        setTasksLoading(false);
      }
    };
    fetchTasks();
  }, [id, user.role, getToken]);

  const handleDeleteTask = async (taskId) => {
    if (!window.confirm("Czy na pewno chcesz usunąć to zadanie?")) return;
    try {
      await api.delete(`/tasks/${taskId}`, {
        headers: { Authorization: `Bearer ${getToken()}` }
      });
      setTasks(tasks.filter(task => task.id !== taskId));
      toast.success("Zadanie zostało usunięte");
    } catch (error) {
      toast.error("Nie udało się usunąć zadania");
    }
  };

  if (loading) {
    return <div>Ładowanie...</div>;
  }

  if (!project) {
    return <div>Nie znaleziono projektu</div>;
  }

  return (
    <div className="p-8">
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <span className="text-3xl">{project.icon}</span>
              <CardTitle>{project.name}</CardTitle>
            </div>
            
            {user.role === "ROLE_MANAGER" && (
              <div className="flex gap-2">
                <Button 
                  variant="outline" 
                  size="icon" 
                  title="Zarządzaj członkami"
                  onClick={() => navigate(`/dashboard/projects/${id}/members`)}
                >
                  <UserPlus className="h-4 w-4" />
                </Button>
                <Button
                  variant="outline"
                  size="icon"
                  title="Dodaj zadanie"
                  onClick={() => navigate(`/dashboard/projects/${id}/tasks/new`)}
                >
                  <PlusCircle className="h-4 w-4" />
                </Button>
                <Button variant="outline" size="icon" title="Edytuj projekt">
                  <Edit className="h-4 w-4" />
                </Button>
                <Button variant="outline" size="icon" title="Usuń projekt">
                  <Trash2 className="h-4 w-4" />
                </Button>
              </div>
            )}
          </div>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 gap-6">
            <div className="space-y-2">
              <h3 className="text-sm font-medium text-gray-500">Opis</h3>
              <p>{project.description || "Brak opisu"}</p>
            </div>

            <div className="space-y-2">
              <h3 className="text-sm font-medium text-gray-500">Status</h3>
              <p>{project.status}</p>
            </div>

            <div className="space-y-2">
              <h3 className="text-sm font-medium text-gray-500">Data utworzenia</h3>
              <p>{new Date(project.createdAt).toLocaleDateString('pl-PL')}</p>
            </div>

            <div className="space-y-2">
              <h3 className="text-sm font-medium text-gray-500">Utworzony przez</h3>
              <p>
                {project.createdBy.firstName} {project.createdBy.lastName}
              </p>
            </div>
          </div>

          {project.members && project.members.length > 0 && (
            <div className="mt-6">
              <h3 className="text-sm font-medium text-gray-500 mb-4">
                Członkowie projektu
              </h3>
              <div className="grid grid-cols-3 gap-4">
                {project.members.map((member) => (
                  <div 
                    key={member.id} 
                    className="p-3 border rounded-lg flex items-center gap-2"
                  >
                    <span>
                      {member.user.firstName} {member.user.lastName}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Zadania sekcja */}
          <div className="mt-8">
            <h3 className="text-sm font-medium text-gray-500 mb-4">
              Zadania w projekcie
            </h3>
            {tasksLoading ? (
              <div>Ładowanie zadań...</div>
            ) : tasks.length === 0 ? (
              <div className="text-gray-400">Brak zadań</div>
            ) : (
              <div className="overflow-x-auto">
                <table className="min-w-full border text-sm">
                  <thead>
                    <tr className="bg-gray-100">
                      <th className="p-2 border">Tytuł</th>
                      <th className="p-2 border">Status</th>
                      <th className="p-2 border">Priorytet</th>
                      <th className="p-2 border">Termin</th>
                      <th className="p-2 border">Przypisany do</th>
                      <th className="p-2 border"></th>
                      {user.role === "ROLE_MANAGER" && <th className="p-2 border"></th>}
                    </tr>
                  </thead>
                  <tbody>
                    {tasks.map((task) => (
                      <tr key={task.id} className="hover:bg-gray-50">
                        <td className="p-2 border font-semibold">
                          <Link
                            to={`/dashboard/projects/${id}/tasks/${task.id}`}
                            className="hover:underline"
                          >
                            {task.title}
                          </Link>
                        </td>
                        <td className="p-2 border">{task.status}</td>
                        <td className="p-2 border">{task.priority}</td>
                        <td className="p-2 border">
                          {task.dueDate && new Date(task.dueDate).toLocaleDateString("pl-PL")}
                        </td>
                        <td className="p-2 border">
                          {task.assignedFirstName && task.assignedLastName
                            ? `${task.assignedFirstName} ${task.assignedLastName}`
                            : task.assignedTo && typeof task.assignedTo === "object"
                            ? `${task.assignedTo.firstName} ${task.assignedTo.lastName}`
                            : "-"}
                        </td>
                        <td className="p-2 border text-right">
                          <Link
                            to={`/dashboard/projects/${id}/tasks/${task.id}`}
                            className="text-blue-600 hover:underline"
                          >
                            Szczegóły
                          </Link>
                        </td>
                        {user.role === "ROLE_MANAGER" && (
                          <td className="p-2 border text-right">
                            <Button
                              variant="ghost"
                              size="icon"
                              title="Usuń zadanie"
                              onClick={() => handleDeleteTask(task.id)}
                            >
                              <Trash2 className="h-4 w-4 text-red-500" />
                            </Button>
                          </td>
                        )}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default ProjectDetails;
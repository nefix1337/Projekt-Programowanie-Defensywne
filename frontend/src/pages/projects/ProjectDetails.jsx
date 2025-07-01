import { useState, useEffect } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { useAuth } from "@/auth/AuthProvider";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { UserPlus, Edit, PlusCircle, Trash2 } from "lucide-react";
import { toast } from "sonner";
import api from "@/api/axiosInstance";
import {
  Table,
  TableHeader,
  TableBody,
  TableRow,
  TableHead,
  TableCell,
} from "@/components/ui/table";
import { Select, SelectTrigger, SelectValue, SelectContent, SelectItem } from "@/components/ui/select";

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

const ProjectDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { getToken, user } = useAuth();
  const [project, setProject] = useState(null);
  const [loading, setLoading] = useState(true);
  const [tasks, setTasks] = useState([]);
  const [tasksLoading, setTasksLoading] = useState(true);

  // Stan filtrów
  const [statusFilter, setStatusFilter] = useState("__ALL__");
  const [priorityFilter, setPriorityFilter] = useState("__ALL__");
  const [assigneeFilter, setAssigneeFilter] = useState("__ALL__");

  // Stan sortowania
  const [sortBy, setSortBy] = useState("");
  const [sortOrder, setSortOrder] = useState("asc");

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
          setTasks(Array.isArray(response.data) ? response.data : []);
        } else {
          response = await api.get("/tasks/my", {
            headers: { Authorization: `Bearer ${getToken()}` }
          });
          setTasks(Array.isArray(response.data) ? response.data : []);
        }
      } catch (error) {
        toast.error("Nie udało się pobrać zadań");
        setTasks([]); // <- ważne!
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

  const handleDeleteProject = async () => {
    if (!window.confirm("Czy na pewno chcesz usunąć ten projekt?")) return;
    try {
      await api.delete(`/projects/${id}`, {
        headers: { Authorization: `Bearer ${getToken()}` }
      });
      toast.success("Projekt został usunięty");
      navigate("/dashboard");
    } catch (error) {
      toast.error("Nie udało się usunąć projektu");
    }
  };

  // Filtrowanie zadań
  const filteredTasks = tasks.filter((task) => {
    const statusOk = statusFilter === "__ALL__" ? true : task.status === statusFilter;
    const priorityOk = priorityFilter === "__ALL__" ? true : task.priority === priorityFilter;
    const assigneeOk = assigneeFilter === "__ALL__"
      ? true
      : (
          (task.assignedFirstName && task.assignedLastName
            ? `${task.assignedFirstName} ${task.assignedLastName}`
            : task.assignedTo && typeof task.assignedTo === "object"
            ? `${task.assignedTo.firstName} ${task.assignedTo.lastName}`
            : "-"
          ) === assigneeFilter
        );
    return statusOk && priorityOk && assigneeOk;
  });

  // Lista unikalnych przypisanych użytkowników do selecta
  const assignees = Array.from(
    new Set(
      tasks.map((task) =>
        task.assignedFirstName && task.assignedLastName
          ? `${task.assignedFirstName} ${task.assignedLastName}`
          : task.assignedTo && typeof task.assignedTo === "object"
          ? `${task.assignedTo.firstName} ${task.assignedTo.lastName}`
          : "-"
      )
    )
  ).filter((name) => name && name !== "-");

  const handleSort = (column) => {
    if (sortBy === column) {
      setSortOrder(sortOrder === "asc" ? "desc" : "asc");
    } else {
      setSortBy(column);
      setSortOrder("asc");
    }
  };

  // Funkcja sortująca
  const sortedTasks = [...filteredTasks].sort((a, b) => {
    if (!sortBy) return 0;
    let aValue = a[sortBy];
    let bValue = b[sortBy];

    // Obsługa daty
    if (sortBy === "dueDate") {
      aValue = aValue ? new Date(aValue) : new Date(0);
      bValue = bValue ? new Date(bValue) : new Date(0);
    }
    // Obsługa przypisanego do
    if (sortBy === "assignedTo") {
      aValue = a.assignedFirstName && a.assignedLastName
        ? `${a.assignedFirstName} ${a.assignedLastName}`
        : a.assignedTo && typeof a.assignedTo === "object"
        ? `${a.assignedTo.firstName} ${a.assignedTo.lastName}`
        : "";
      bValue = b.assignedFirstName && b.assignedLastName
        ? `${b.assignedFirstName} ${b.assignedLastName}`
        : b.assignedTo && typeof b.assignedTo === "object"
        ? `${b.assignedTo.firstName} ${b.assignedTo.lastName}`
        : "";
    }

    if (aValue < bValue) return sortOrder === "asc" ? -1 : 1;
    if (aValue > bValue) return sortOrder === "asc" ? 1 : -1;
    return 0;
  });

  if (loading) {
    return <div>Ładowanie...</div>;
  }

  if (!project) {
    return <div>Nie znaleziono projektu</div>;
  }

  // Badge do statusu
  const StatusBadge = ({ status }) => {
    const colors = {
      TODO: "bg-gray-200 text-gray-800",
      IN_PROGRESS: "bg-blue-200 text-blue-800",
      DONE: "bg-green-200 text-green-800",
      TO_REVIEW: "bg-yellow-200 text-yellow-800",
      VERIFIED: "bg-purple-200 text-purple-800",
      ARCHIVED: "bg-gray-400 text-white",
    };
    const labels = {
      TODO: "Do zrobienia",
      IN_PROGRESS: "W trakcie",
      DONE: "Zrobione",
      TO_REVIEW: "Do sprawdzenia",
      VERIFIED: "Zweryfikowane",
      ARCHIVED: "Zarchiwizowane",
    };
    return (
      <span className={`px-2 py-1 rounded text-xs font-semibold ${colors[status] || "bg-gray-100 text-gray-700"}`}>
        {labels[status] || status}
      </span>
    );
  };

  // Badge do priorytetu
  const PriorityBadge = ({ priority }) => {
    const colors = {
      LOW: "bg-green-100 text-green-700",
      MEDIUM: "bg-yellow-100 text-yellow-700",
      HIGH: "bg-red-100 text-red-700",
    };
    const labels = {
      LOW: "Niski",
      MEDIUM: "Średni",
      HIGH: "Wysoki",
    };
    return (
      <span className={`px-2 py-1 rounded text-xs font-semibold ${colors[priority] || "bg-gray-100 text-gray-700"}`}>
        {labels[priority] || priority}
      </span>
    );
  };

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
                <Button
                  variant="outline"
                  size="icon"
                  title="Edytuj projekt"
                  onClick={() => navigate(`/dashboard/projects/${id}/edit`)}
                >
                  <Edit className="h-4 w-4" />
                </Button>
                <Button
                  variant="outline"
                  size="icon"
                  title="Usuń projekt"
                  onClick={handleDeleteProject}
                >
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
              <p><StatusBadge status={project.status} /></p>
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
            {/* FILTRY */}
            <div className="flex flex-wrap gap-4 mb-4">
              <div>
                <label className="block text-xs text-gray-500 mb-1">Status</label>
                <Select value={statusFilter} onValueChange={setStatusFilter}>
                  <SelectTrigger className="w-40">
                    <SelectValue placeholder="Wszystkie" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="__ALL__">Wszystkie</SelectItem>
                    {STATUS_OPTIONS.map((status) => (
                      <SelectItem key={status.value} value={status.value}>
                        {status.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div>
                <label className="block text-xs text-gray-500 mb-1">Priorytet</label>
                <Select value={priorityFilter} onValueChange={setPriorityFilter}>
                  <SelectTrigger className="w-40">
                    <SelectValue placeholder="Wszystkie" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="__ALL__">Wszystkie</SelectItem>
                    {PRIORITY_OPTIONS.map((priority) => (
                      <SelectItem key={priority.value} value={priority.value}>
                        {priority.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div>
                <label className="block text-xs text-gray-500 mb-1">Przypisany do</label>
                <Select value={assigneeFilter} onValueChange={setAssigneeFilter}>
                  <SelectTrigger className="w-40">
                    <SelectValue placeholder="Wszyscy" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="__ALL__">Wszyscy</SelectItem>
                    {assignees.map((name) => (
                      <SelectItem key={name} value={name}>
                        {name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
            {/* KONIEC FILTRÓW */}

            {tasksLoading ? (
              <div>Ładowanie zadań...</div>
            ) : sortedTasks.length === 0 ? (
              <div className="text-gray-400">Brak zadań</div>
            ) : (
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead
                        className="cursor-pointer select-none"
                        onClick={() => handleSort("title")}
                      >
                        Tytuł {sortBy === "title" && (sortOrder === "asc" ? "▲" : "▼")}
                      </TableHead>
                      <TableHead
                        className="cursor-pointer select-none"
                        onClick={() => handleSort("status")}
                      >
                        Status {sortBy === "status" && (sortOrder === "asc" ? "▲" : "▼")}
                      </TableHead>
                      <TableHead
                        className="cursor-pointer select-none"
                        onClick={() => handleSort("priority")}
                      >
                        Priorytet {sortBy === "priority" && (sortOrder === "asc" ? "▲" : "▼")}
                      </TableHead>
                      <TableHead
                        className="cursor-pointer select-none"
                        onClick={() => handleSort("dueDate")}
                      >
                        Termin {sortBy === "dueDate" && (sortOrder === "asc" ? "▲" : "▼")}
                      </TableHead>
                      <TableHead
                        className="cursor-pointer select-none"
                        onClick={() => handleSort("assignedTo")}
                      >
                        Przypisany do {sortBy === "assignedTo" && (sortOrder === "asc" ? "▲" : "▼")}
                      </TableHead>
                      <TableHead></TableHead>
                      {user.role === "ROLE_MANAGER" && <TableHead></TableHead>}
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {sortedTasks.map((task) => (
                      <TableRow key={task.id} className="hover:bg-gray-50">
                        <TableCell className="font-semibold">
                          <Link
                            to={`/dashboard/projects/${id}/tasks/${task.id}`}
                            className="hover:underline"
                          >
                            {task.title}
                          </Link>
                        </TableCell>
                        <TableCell><StatusBadge status={task.status} /></TableCell>
                        <TableCell><PriorityBadge priority={task.priority} /></TableCell>
                        <TableCell>
                          {task.dueDate && new Date(task.dueDate).toLocaleDateString("pl-PL")}
                        </TableCell>
                        <TableCell>
                          {task.assignedFirstName && task.assignedLastName
                            ? `${task.assignedFirstName} ${task.assignedLastName}`
                            : task.assignedTo && typeof task.assignedTo === "object"
                            ? `${task.assignedTo.firstName} ${task.assignedTo.lastName}`
                            : "-"}
                        </TableCell>
                        <TableCell className="text-right">
                          <Link
                            to={`/dashboard/projects/${id}/tasks/${task.id}`}
                            className="text-blue-600 hover:underline"
                          >
                            Szczegóły
                          </Link>
                        </TableCell>
                        {user.role === "ROLE_MANAGER" && (
                          <TableCell className="text-right">
                            <Button
                              variant="ghost"
                              size="icon"
                              title="Usuń zadanie"
                              onClick={() => handleDeleteTask(task.id)}
                            >
                              <Trash2 className="h-4 w-4 text-red-500" />
                            </Button>
                          </TableCell>
                        )}
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default ProjectDetails;
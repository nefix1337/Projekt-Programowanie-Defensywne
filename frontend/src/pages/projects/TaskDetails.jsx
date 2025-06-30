import { useState, useEffect, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { toast } from "sonner";
import api from "@/api/axiosInstance";
import { useAuth } from "@/auth/AuthProvider";
import { Trash2 } from "lucide-react";

const StatusBadge = ({ status }) => {
  const colors = {
    TODO: "bg-gray-200 text-gray-800",
    IN_PROGRESS: "bg-blue-200 text-blue-800",
    DONE: "bg-green-200 text-green-800",
    TO_REVIEW: "bg-yellow-200 text-yellow-800",
    VERIFIED: "bg-purple-200 text-purple-800",
    ARCHIVED: "bg-gray-400 text-white",
  };
  return (
    <span className={`px-2 py-1 rounded text-xs font-semibold ${colors[status] || "bg-gray-100 text-gray-700"}`}>
      {status.replace("_", " ")}
    </span>
  );
};

const TaskDetails = () => {
  const { id, taskId } = useParams();
  const navigate = useNavigate();
  const { getToken, user } = useAuth();
  const [task, setTask] = useState(null);
  const [loading, setLoading] = useState(true);
  const [changingStatus, setChangingStatus] = useState(false);

  // Komentarze
  const [comments, setComments] = useState([]);
  const [commentsLoading, setCommentsLoading] = useState(true);
  const [commentText, setCommentText] = useState("");
  const [addingComment, setAddingComment] = useState(false);
  const commentInputRef = useRef(null);

  // Pobierz szczegóły zadania
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

  // Pobierz komentarze
  useEffect(() => {
    const fetchComments = async () => {
      setCommentsLoading(true);
      try {
        const response = await api.get(`/tasks/${taskId}/comments`, {
          headers: { Authorization: `Bearer ${getToken()}` },
        });
        setComments(response.data);
      } catch (error) {
        toast.error("Nie udało się pobrać komentarzy");
      } finally {
        setCommentsLoading(false);
      }
    };
    fetchComments();
  }, [taskId, getToken]);

  // Dodawanie komentarza
  const handleAddComment = async (e) => {
    e.preventDefault();
    if (!commentText.trim()) return;
    setAddingComment(true);
    try {
      await api.post(
        `/tasks/${taskId}/comments`,
        { comment: commentText },
        { headers: { Authorization: `Bearer ${getToken()}` } }
      );
      setCommentText("");
      // Odśwież komentarze po dodaniu
      const response = await api.get(`/tasks/${taskId}/comments`, {
        headers: { Authorization: `Bearer ${getToken()}` },
      });
      setComments(response.data);
      toast.success("Komentarz dodany");
      commentInputRef.current?.focus();
    } catch (error) {
      toast.error("Nie udało się dodać komentarza");
    } finally {
      setAddingComment(false);
    }
  };

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

  const handleSetToReview = async () => {
    setChangingStatus(true);
    try {
      const response = await api.patch(`/tasks/${taskId}/to-review`, null, {
        headers: { Authorization: `Bearer ${getToken()}` },
      });
      setTask(response.data);
      toast.success("Zadanie przekazane do sprawdzenia");
    } catch (error) {
      toast.error("Nie udało się przekazać zadania do sprawdzenia");
    } finally {
      setChangingStatus(false);
    }
  };

  if (loading) return <div>Ładowanie...</div>;
  if (!task) return <div>Nie znaleziono zadania</div>;

  const canDelete =
    user?.role === "ROLE_MANAGER" ||
    user?.email === task.creatorEmail;

  // Użytkownik może przekazać do sprawdzenia, jeśli jest przypisany do zadania i status to np. "DONE" lub "IN_PROGRESS"
  const canSetToReview =
    user?.role === "ROLE_USER" &&
    user?.email === task.assignedEmail &&
    !["TO_REVIEW", "VERIFIED", "ARCHIVED"].includes(task.status);

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
            <span className="font-medium">Status:</span> <StatusBadge status={task.status} />
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
          {/* PRZYCISKI AKCJI */}
          <div className="flex gap-2 mt-4 mb-8">
            <Button variant="outline" onClick={() => navigate(-1)}>
              Powrót
            </Button>
            <Button
              variant="outline"
              onClick={() => navigate(`/dashboard/projects/${id}/tasks/${taskId}/edit`)}
            >
              Edytuj
            </Button>
            {canSetToReview && (
              <Button
                variant="secondary"
                onClick={handleSetToReview}
                disabled={changingStatus}
              >
                {changingStatus ? "Przekazywanie..." : "Przekaż do sprawdzenia"}
              </Button>
            )}
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
          {/* KOMENTARZE */}
          <div>
            <h3 className="text-sm font-medium text-gray-500 mb-2">Komentarze</h3>
            <form onSubmit={handleAddComment} className="flex gap-2 mb-4">
              <Input
                ref={commentInputRef}
                value={commentText}
                onChange={(e) => setCommentText(e.target.value)}
                placeholder="Dodaj komentarz..."
                disabled={addingComment}
                maxLength={500}
              />
              <Button type="submit" disabled={addingComment || !commentText.trim()}>
                Dodaj
              </Button>
            </form>
            {commentsLoading ? (
              <div className="text-gray-400">Ładowanie komentarzy...</div>
            ) : comments.length === 0 ? (
              <div className="text-gray-400">Brak komentarzy</div>
            ) : (
              <div className="space-y-4">
                {comments.map((c) => (
                  <div key={c.id} className="border rounded p-3 bg-gray-50">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="font-semibold">{c.authorFirstName} {c.authorLastName}</span>
                      <span className="text-xs text-gray-500">({c.authorEmail})</span>
                      <span className="text-xs text-gray-400 ml-2">
                        {new Date(c.createdAt).toLocaleString("pl-PL")}
                      </span>
                    </div>
                    <div className="text-sm">{c.comment}</div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default TaskDetails;
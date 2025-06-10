import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useAuth } from "@/auth/AuthProvider";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { UserPlus, Edit, PlusCircle } from "lucide-react";
import { toast } from "sonner";
import api from "@/api/axiosInstance";

const ProjectDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { getToken, user } = useAuth();
  const [project, setProject] = useState(null);
  const [loading, setLoading] = useState(true);

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
        </CardContent>
      </Card>
    </div>
  );
};

export default ProjectDetails;
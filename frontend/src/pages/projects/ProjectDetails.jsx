import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { useAuth } from "@/auth/AuthProvider";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { toast } from "sonner";
import api from "@/api/axiosInstance";

const ProjectDetails = () => {
  const { id } = useParams();
  const { getToken } = useAuth();
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
          <div className="flex items-center gap-4">
            <span className="text-3xl">{project.icon}</span>
            <CardTitle>{project.name}</CardTitle>
          </div>
        </CardHeader>
        <CardContent className="space-y-6">
          <div>
            <h3 className="text-sm font-medium text-gray-500">Opis</h3>
            <p className="mt-1">{project.description || "Brak opisu"}</p>
          </div>

          <div>
            <h3 className="text-sm font-medium text-gray-500">Status</h3>
            <p className="mt-1">{project.status}</p>
          </div>

          <div>
            <h3 className="text-sm font-medium text-gray-500">Data utworzenia</h3>
            <p className="mt-1">
              {new Date(project.createdAt).toLocaleDateString('pl-PL')}
            </p>
          </div>

          <div>
            <h3 className="text-sm font-medium text-gray-500">Utworzony przez</h3>
            <p className="mt-1">
              {project.createdBy.firstName} {project.createdBy.lastName}
            </p>
          </div>

          {project.members && project.members.length > 0 && (
            <div>
              <h3 className="text-sm font-medium text-gray-500">Członkowie projektu</h3>
              <ul className="mt-2 space-y-2">
                {project.members.map((member) => (
                  <li key={member.id} className="flex items-center gap-2">
                    <span>{member.user.firstName} {member.user.lastName}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default ProjectDetails;
import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useAuth } from "@/auth/AuthProvider";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { UserMinus, ArrowLeft } from "lucide-react";
import { toast } from "sonner";
import api from "@/api/axiosInstance";

const PROJECT_ROLES = [
  "PROJECT_MANAGER",
  "TECH_LEAD",
  "DEVELOPER",
  "TESTER",
  "ANALYST",
  "DESIGNER",
  "SCRUM_MASTER",
  "DEVOPS_ENGINEER"
];

const formatRole = (role) => {
  return role
    .split('_')
    .map(word => word.charAt(0) + word.slice(1).toLowerCase())
    .join(' ');
};

const ProjectMembers = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { getToken } = useAuth();
  const [members, setMembers] = useState([]);
  const [availableUsers, setAvailableUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [project, setProject] = useState(null);
  const [selectedEmail, setSelectedEmail] = useState("");
  const [selectedRole, setSelectedRole] = useState("DEVELOPER"); // Default role

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [projectResponse, membersResponse, usersResponse] = await Promise.all([
          api.get(`/projects/${id}`, {
            headers: { Authorization: `Bearer ${getToken()}` }
          }),
          api.get(`/projects/${id}/members`, {
            headers: { Authorization: `Bearer ${getToken()}` }
          }),
          api.get('/users', {
            headers: { Authorization: `Bearer ${getToken()}` }
          })
        ]);

        setProject(projectResponse.data);
        setMembers(Object.values(membersResponse.data)); // <-- ważne!
        setAvailableUsers(usersResponse.data);
      } catch (error) {
        console.error("Error fetching data:", error);
        toast.error("Nie udało się pobrać danych");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [id, getToken]);

  const handleAddMember = async () => {
    if (!selectedEmail || !selectedRole) return;

    try {
      await api.post(
        `/projects/${id}/members`,
        {
          userEmail: selectedEmail,
          projectRole: selectedRole
        },
        { headers: { Authorization: `Bearer ${getToken()}` }}
      );
      
      const response = await api.get(`/projects/${id}/members`, {
        headers: { Authorization: `Bearer ${getToken()}` }
      });
      setMembers(response.data);
      setSelectedEmail("");
      setSelectedRole("DEVELOPER");
      toast.success("Użytkownik został dodany do projektu");
    } catch (error) {
      console.error("Error adding member:", error);
      toast.error("Nie udało się dodać użytkownika do projektu");
    }
  };

  const handleRemoveMember = async (userId) => {
    try {
      await api.delete(`/projects/${id}/members/${userId}`, {
        headers: { Authorization: `Bearer ${getToken()}` }
      });
      setMembers(members.filter(member => member.userId !== userId));
      toast.success("Użytkownik został usunięty z projektu");
    } catch (error) {
      console.error("Error removing member:", error);
      toast.error("Nie udało się usunąć użytkownika z projektu");
    }
  };

  if (loading) {
    return <div>Ładowanie...</div>;
  }

  // Filter out users that are already members
  const filteredUsers = availableUsers.filter(
    user => !members.some(member => member.email === user.email)
  );

  return (
    <div className="p-8">
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <Button
                variant="ghost"
                size="icon"
                onClick={() => navigate(`/dashboard/projects/${id}`)}
              >
                <ArrowLeft className="h-4 w-4" />
              </Button>
              <CardTitle>Zarządzanie członkami projektu: {project.name}</CardTitle>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <div className="flex gap-2 mb-6">
            <Select
              value={selectedEmail}
              onValueChange={setSelectedEmail}
              className="flex-1"
            >
              <SelectTrigger>
                <SelectValue placeholder="Wybierz użytkownika" />
              </SelectTrigger>
              <SelectContent>
                {filteredUsers.map(user => (
                  <SelectItem key={user.email} value={user.email}>
                    {user.firstName} {user.lastName} ({user.email})
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Select
              value={selectedRole}
              onValueChange={setSelectedRole}
              className="w-[200px]"
            >
              <SelectTrigger>
                <SelectValue placeholder="Wybierz rolę" />
              </SelectTrigger>
              <SelectContent>
                {PROJECT_ROLES.map(role => (
                  <SelectItem key={role} value={role}>
                    {formatRole(role)}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Button onClick={handleAddMember}>Dodaj</Button>
          </div>

          <div className="space-y-4">
            {members.map((member) => (
              <div
                key={member.id}
                className="flex items-center justify-between p-4 border rounded-lg"
              >
                <div>
                  <span className="font-medium">
                    {member.firstName} {member.lastName}
                  </span>
                  <span className="ml-2 text-sm text-gray-500">
                    {member.userEmail}
                  </span>
                  <span className="ml-2 text-sm text-gray-500">
                    {member.projectRole ? `(${formatRole(member.projectRole)})` : "(brak roli)"}
                  </span>
                </div>
                <Button
                  variant="ghost"
                  size="icon"
                  onClick={() => handleRemoveMember(member.userId)}
                >
                  <UserMinus className="h-4 w-4" />
                </Button>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default ProjectMembers;
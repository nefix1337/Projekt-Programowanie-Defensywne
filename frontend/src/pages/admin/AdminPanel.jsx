import { useEffect, useState } from "react";
import { useAuth } from "@/auth/AuthProvider";
import api from "@/api/axiosInstance";
import { toast } from "sonner";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

const AdminPanel = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const { getToken } = useAuth();

  const roles = ["USER", "MANAGER"];

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      const response = await api.get("/admin/users", {
        headers: { Authorization: `Bearer ${getToken()}` },
      });
      const filteredUsers = response.data.filter((user) => user.role !== "ADMIN");
      setUsers(filteredUsers);
    } catch (error) {
      console.error("Error fetching users:", error);
      toast.error("Nie udało się pobrać listy użytkowników");
    } finally {
      setLoading(false);
    }
  };

  const handleRoleChange = async (userEmail, newRole) => {
    if (newRole === "ADMIN") {
      toast.error("Nie można ustawić roli ADMIN");
      return;
    }

    try {
      await api.post(
        "/admin/change-role",
        {
          email: userEmail,
          newRole: newRole,
        },
        {
          headers: { Authorization: `Bearer ${getToken()}` },
        }
      );

      setUsers(
        users.map((user) =>
          user.email === userEmail ? { ...user, role: newRole } : user
        )
      );

      toast.success("Rola użytkownika została zaktualizowana");
    } catch (error) {
      console.error("Error updating user role:", error);
      toast.error("Nie udało się zaktualizować roli użytkownika");
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-screen">
        Ładowanie...
      </div>
    );
  }

  return (
    <div className="p-8">
      <Card>
        <CardHeader>
          <CardTitle>Panel administratora</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>ID</TableHead>
                <TableHead>Imię</TableHead>
                <TableHead>Nazwisko</TableHead>
                <TableHead>Email</TableHead>
                <TableHead>Rola</TableHead>
                <TableHead className="text-right">Akcje</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {users.map((user) => (
                <TableRow key={user.id}>
                  <TableCell>{user.id}</TableCell>
                  <TableCell>{user.firstName}</TableCell>
                  <TableCell>{user.lastName}</TableCell>
                  <TableCell>{user.email}</TableCell>
                  <TableCell>{user.role}</TableCell>
                  <TableCell className="text-right">
                    <Select
                      value={user.role}
                      onValueChange={(newRole) =>
                        handleRoleChange(user.email, newRole)
                      }
                    >
                      <SelectTrigger className="w-[180px]">
                        <SelectValue placeholder="Wybierz rolę" />
                      </SelectTrigger>
                      <SelectContent>
                        {roles.map((role) => (
                          <SelectItem key={role} value={role}>
                            {role}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
};

export default AdminPanel;
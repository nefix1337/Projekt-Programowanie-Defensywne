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
import { Button } from "@/components/ui/button";

const AdminPanel = () => {
  const [users, setUsers] = useState([]);
  const [nodes, setNodes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [nodesLoading, setNodesLoading] = useState(true);
  const { getToken } = useAuth();

  const roles = ["USER", "MANAGER"];

  useEffect(() => {
    fetchUsers();
    fetchNodes();
    const interval = setInterval(fetchNodes, 5000);
    return () => clearInterval(interval);
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

  const fetchNodes = async () => {
    try {
      const response = await api.get("/admin/nodes", {
        headers: { Authorization: `Bearer ${getToken()}` },
      });
      setNodes(response.data);
    } catch (error) {
      console.error("Error fetching nodes:", error);
      toast.error("Nie udalo sie pobrac statusu wezlow");
    } finally {
      setNodesLoading(false);
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

  const handleNodeFailure = async (nodeId) => {
    try {
      await api.post(`/admin/nodes/${nodeId}/failure`, null, {
        headers: { Authorization: `Bearer ${getToken()}` },
      });
      toast.success(`Awaria wezla ${nodeId} zostala wlaczona`);
      fetchNodes();
    } catch (error) {
      console.error("Error injecting node failure:", error);
      toast.error("Nie udalo sie wprowadzic awarii wezla");
    }
  };

  const handleNodeRecovery = async (nodeId) => {
    try {
      await api.post(`/admin/nodes/${nodeId}/recovery`, null, {
        headers: { Authorization: `Bearer ${getToken()}` },
      });
      toast.success(`Wezel ${nodeId} zostal przywrocony`);
      fetchNodes();
    } catch (error) {
      console.error("Error recovering node:", error);
      toast.error("Nie udalo sie przywrocic wezla");
    }
  };

  const onlineCount = nodes.filter((node) => node.online).length;
  const leader = nodes.find((node) => node.leader);

  if (loading) {
    return (
      <div className="flex justify-center items-center h-screen">
        Ładowanie...
      </div>
    );
  }

  return (
    <div className="space-y-6 p-8">
      <Card>
        <CardHeader>
          <CardTitle>Monitoring wezlow</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="mb-4 grid gap-3 sm:grid-cols-3">
            <div className="rounded-md border p-3">
              <div className="text-sm text-muted-foreground">Online</div>
              <div className="text-2xl font-semibold">
                {onlineCount}/{nodes.length || 3}
              </div>
            </div>
            <div className="rounded-md border p-3">
              <div className="text-sm text-muted-foreground">Lider</div>
              <div className="text-2xl font-semibold">
                {leader ? leader.nodeId : "brak"}
              </div>
            </div>
            <div className="rounded-md border p-3">
              <div className="text-sm text-muted-foreground">Odswiezanie</div>
              <div className="text-2xl font-semibold">5s</div>
            </div>
          </div>

          {nodesLoading ? (
            <div>Ladowanie statusu wezlow...</div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Node</TableHead>
                  <TableHead>Waga</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Lider</TableHead>
                  <TableHead>Ostatni heartbeat</TableHead>
                  <TableHead className="text-right">Awaria</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {nodes.map((node) => (
                  <TableRow key={node.nodeId}>
                    <TableCell className="font-medium">{node.nodeId}</TableCell>
                    <TableCell>{node.weight}</TableCell>
                    <TableCell>
                      {node.forcedDown
                        ? "awaria wymuszona"
                        : node.online
                          ? "online"
                          : "offline"}
                    </TableCell>
                    <TableCell>{node.leader ? "tak" : "nie"}</TableCell>
                    <TableCell>
                      {node.secondsSinceLastSeen === null
                        ? "brak"
                        : `${node.secondsSinceLastSeen}s temu`}
                    </TableCell>
                    <TableCell className="text-right">
                      {node.forcedDown ? (
                        <Button
                          type="button"
                          variant="outline"
                          onClick={() => handleNodeRecovery(node.nodeId)}
                        >
                          Przywroc
                        </Button>
                      ) : (
                        <Button
                          type="button"
                          variant="destructive"
                          onClick={() => handleNodeFailure(node.nodeId)}
                        >
                          Awaria
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

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

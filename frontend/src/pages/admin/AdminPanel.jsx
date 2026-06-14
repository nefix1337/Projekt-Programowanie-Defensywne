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
import { Input } from "@/components/ui/input";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";

const AdminPanel = () => {
  const [users, setUsers] = useState([]);
  const [nodes, setNodes] = useState([]);
  const [events, setEvents] = useState([]);
  const [metrics, setMetrics] = useState([]);
  const [loading, setLoading] = useState(true);
  const [nodesLoading, setNodesLoading] = useState(true);
  const [metricsLoading, setMetricsLoading] = useState(true);
  const [delayInputs, setDelayInputs] = useState({});
  const { getToken } = useAuth();

  const roles = ["USER", "MANAGER"];

  useEffect(() => {
    fetchUsers();
    fetchNodes();
    fetchNodeEvents();
    fetchMetrics();
    const interval = setInterval(() => {
      fetchNodes();
      fetchNodeEvents();
      fetchMetrics();
    }, 5000);
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

  const fetchNodeEvents = async () => {
    try {
      const response = await api.get("/admin/nodes/events", {
        headers: { Authorization: `Bearer ${getToken()}` },
      });
      setEvents(response.data);
    } catch (error) {
      console.error("Error fetching node events:", error);
    }
  };

  const fetchMetrics = async () => {
    try {
      const response = await api.get("/admin/nodes/metrics", {
        headers: { Authorization: `Bearer ${getToken()}` },
      });
      setMetrics(response.data);
    } catch (error) {
      console.error("Error fetching node metrics:", error);
    } finally {
      setMetricsLoading(false);
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

  const handleSetNetworkDelay = async (nodeId, delayMs) => {
    try {
      await api.post(
        `/admin/nodes/${nodeId}/network-delay`,
        { delayMs },
        { headers: { Authorization: `Bearer ${getToken()}` } }
      );
      toast.success(
        delayMs > 0
          ? `Opoznienie sieciowe ${delayMs} ms ustawione dla ${nodeId}`
          : `Opoznienie sieciowe wyczyszczone dla ${nodeId}`
      );
      fetchNodes();
    } catch (error) {
      console.error("Error setting network delay:", error);
      toast.error(
        error.response?.data?.message || "Nie udalo sie ustawic opoznienia sieciowego"
      );
    }
  };

  const handleApplyNetworkDelay = (nodeId) => {
    const rawValue = delayInputs[nodeId];
    const delayMs = Number(rawValue);
    if (rawValue === undefined || rawValue === "" || !Number.isFinite(delayMs) || delayMs < 0 || delayMs > 30000) {
      toast.error("Opoznienie musi byc liczba od 0 do 30000 ms");
      return;
    }
    handleSetNetworkDelay(nodeId, delayMs);
  };

  const handleToggleMessageCorruption = async (nodeId, enable) => {
    try {
      const path = enable
        ? `/admin/nodes/${nodeId}/message-corruption`
        : `/admin/nodes/${nodeId}/message-corruption/clear`;
      await api.post(path, null, {
        headers: { Authorization: `Bearer ${getToken()}` },
      });
      toast.success(
        enable
          ? `Uszkodzenie wiadomosci wlaczone dla ${nodeId}`
          : `Uszkodzenie wiadomosci wylaczone dla ${nodeId}`
      );
      fetchNodes();
    } catch (error) {
      console.error("Error toggling message corruption:", error);
      toast.error("Nie udalo sie zmienic stanu uszkodzenia wiadomosci");
    }
  };

  const onlineCount = nodes.filter((node) => node.online).length;
  const leader = nodes.find((node) => node.leader);
  const lastEvent = events[0];

  const getNodeState = (node) => {
    if (node.forcedDown) {
      return {
        label: "awaria wymuszona",
        className: "border-red-200 bg-red-50 text-red-700",
      };
    }
    if (!node.online) {
      return {
        label: "offline",
        className: "border-zinc-200 bg-zinc-100 text-zinc-700",
      };
    }
    if (node.leader) {
      return {
        label: "lider aktywny",
        className: "border-emerald-200 bg-emerald-50 text-emerald-700",
      };
    }
    return {
      label: "online",
      className: "border-sky-200 bg-sky-50 text-sky-700",
    };
  };

  const getEventClassName = (eventType) => {
    if (eventType?.includes("FAILURE") || eventType?.includes("CORRUPTION")) {
      return "border-red-200 bg-red-50 text-red-700";
    }
    if (eventType?.includes("RECOVERED") || eventType?.includes("CLEARED")) {
      return "border-emerald-200 bg-emerald-50 text-emerald-700";
    }
    if (eventType?.includes("DELAY")) {
      return "border-amber-200 bg-amber-50 text-amber-700";
    }
    return "border-zinc-200 bg-zinc-50 text-zinc-700";
  };

  const getMetricBarColor = (eventType) => {
    if (eventType?.includes("FAILURE") || eventType?.includes("CORRUPTION")) {
      return "bg-red-500";
    }
    if (eventType?.includes("RECOVERED") || eventType?.includes("CLEARED")) {
      return "bg-emerald-500";
    }
    if (eventType?.includes("DELAY")) {
      return "bg-amber-500";
    }
    return "bg-sky-500";
  };

  const maxMetricCount = Math.max(1, ...metrics.map((metric) => metric.count));

  if (loading) {
    return (
      <div className="flex justify-center items-center h-screen">
        Ładowanie...
      </div>
    );
  }

  return (
    <div className="p-8">
      <Tabs defaultValue="nodes" className="w-full">
        <TabsList className="mb-6">
          <TabsTrigger value="nodes">Monitorowanie węzłów</TabsTrigger>
          <TabsTrigger value="events">Historia zdarzeń</TabsTrigger>
          <TabsTrigger value="metrics">Metryki</TabsTrigger>
          <TabsTrigger value="users">Panel administratora</TabsTrigger>
        </TabsList>

        {/* Nodes Monitoring Tab */}
        <TabsContent value="nodes" className="space-y-4">
          {!nodesLoading && nodes.length > 0 && !leader && (
            <div className="rounded-md border border-red-200 bg-red-50 p-3 text-sm font-medium text-red-700">
              Brak aktywnego lidera - operacje zapisu zadan (RabbitMQ) sa tymczasowo niedostepne.
            </div>
          )}
          <Card>
            <CardHeader>
              <CardTitle>Monitoring węzłów</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="mb-4 grid gap-3 sm:grid-cols-4">
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
                  <div className="text-sm text-muted-foreground">Odświeżanie</div>
                  <div className="text-2xl font-semibold">5s</div>
                </div>
                <div className="rounded-md border p-3">
                  <div className="text-sm text-muted-foreground">Ostatnia reakcja</div>
                  <div className="truncate text-sm font-medium">
                    {lastEvent ? `${lastEvent.eventType} / ${lastEvent.nodeId}` : "brak"}
                  </div>
                </div>
              </div>

              {nodesLoading ? (
                <div>Ładowanie statusu węzłów...</div>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Node</TableHead>
                      <TableHead>Waga</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead>Lider</TableHead>
                      <TableHead>Opóźnienie sieci</TableHead>
                      <TableHead>Korupcja wiad.</TableHead>
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
                          <span
                            className={`inline-flex rounded-md border px-2 py-1 text-xs font-medium ${getNodeState(node).className}`}
                          >
                            {getNodeState(node).label}
                          </span>
                        </TableCell>
                        <TableCell>{node.leader ? "tak" : "nie"}</TableCell>
                        <TableCell>
                          {node.networkDelayMs > 0 ? (
                            <span className="inline-flex rounded-md border px-2 py-1 text-xs font-medium border-amber-200 bg-amber-50 text-amber-700">
                              {node.networkDelayMs} ms
                            </span>
                          ) : (
                            "brak"
                          )}
                        </TableCell>
                        <TableCell>
                          {node.messageCorruption ? (
                            <span className="inline-flex rounded-md border px-2 py-1 text-xs font-medium border-red-200 bg-red-50 text-red-700">
                              aktywna
                            </span>
                          ) : (
                            "brak"
                          )}
                        </TableCell>
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
                              Przywróć
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
              <CardTitle>Symulacja dodatkowych awarii</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <p className="text-sm text-muted-foreground">
                Oprócz wyłączenia węzła (powyżej) można zasymulować opóźnienie sieciowe lub
                uszkodzenie przetwarzanej wiadomości na wybranym węźle, aby zobaczyć reakcję systemu.
              </p>
              {nodesLoading ? (
                <div>Ładowanie statusu węzłów...</div>
              ) : (
                nodes.map((node) => (
                  <div key={node.nodeId} className="rounded-md border p-3 space-y-3">
                    <div className="flex items-center justify-between">
                      <span className="font-medium">{node.nodeId}</span>
                      <div className="flex gap-2">
                        {node.networkDelayMs > 0 && (
                          <span className="inline-flex rounded-md border px-2 py-1 text-xs font-medium border-amber-200 bg-amber-50 text-amber-700">
                            opóźnienie {node.networkDelayMs} ms
                          </span>
                        )}
                        {node.messageCorruption && (
                          <span className="inline-flex rounded-md border px-2 py-1 text-xs font-medium border-red-200 bg-red-50 text-red-700">
                            korupcja wiadomości
                          </span>
                        )}
                      </div>
                    </div>
                    <div className="flex flex-wrap items-center gap-2">
                      <Input
                        type="number"
                        min={0}
                        max={30000}
                        step={100}
                        placeholder="Opóźnienie (ms)"
                        className="h-9 w-40"
                        value={delayInputs[node.nodeId] ?? ""}
                        onChange={(e) =>
                          setDelayInputs((prev) => ({ ...prev, [node.nodeId]: e.target.value }))
                        }
                      />
                      <Button type="button" variant="outline" onClick={() => handleApplyNetworkDelay(node.nodeId)}>
                        Ustaw opóźnienie
                      </Button>
                      <Button
                        type="button"
                        variant="outline"
                        disabled={node.networkDelayMs === 0}
                        onClick={() => handleSetNetworkDelay(node.nodeId, 0)}
                      >
                        Wyczyść opóźnienie
                      </Button>
                      {node.messageCorruption ? (
                        <Button
                          type="button"
                          variant="outline"
                          onClick={() => handleToggleMessageCorruption(node.nodeId, false)}
                        >
                          Wyłącz korupcję wiadomości
                        </Button>
                      ) : (
                        <Button
                          type="button"
                          variant="destructive"
                          onClick={() => handleToggleMessageCorruption(node.nodeId, true)}
                        >
                          Wywołaj korupcję wiadomości
                        </Button>
                      )}
                    </div>
                  </div>
                ))
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* Events History Tab */}
        <TabsContent value="events" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Historia zdarzeń systemu rozproszonego</CardTitle>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Czas</TableHead>
                    <TableHead>Node</TableHead>
                    <TableHead>Zdarzenie</TableHead>
                    <TableHead>Szczegóły</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {events.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={4}>Brak zdarzeń</TableCell>
                    </TableRow>
                  ) : (
                    events.map((event) => (
                      <TableRow key={event.id}>
                        <TableCell>
                          {event.eventTime
                            ? new Date(event.eventTime).toLocaleString()
                            : "brak"}
                        </TableCell>
                        <TableCell>{event.nodeId}</TableCell>
                        <TableCell>
                          <span
                            className={`inline-flex rounded-md border px-2 py-1 text-xs font-medium ${getEventClassName(event.eventType)}`}
                          >
                            {event.eventType}
                          </span>
                        </TableCell>
                        <TableCell>{event.details}</TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Metrics Tab */}
        <TabsContent value="metrics" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Metryki zdarzeń systemu rozproszonego</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              {metricsLoading ? (
                <div>Ładowanie metryk...</div>
              ) : metrics.length === 0 ? (
                <div>Brak danych</div>
              ) : (
                metrics.map((metric) => (
                  <div key={metric.eventType} className="space-y-1">
                    <div className="flex justify-between text-sm">
                      <span className="font-medium">{metric.eventType}</span>
                      <span className="text-muted-foreground">{metric.count}</span>
                    </div>
                    <div className="h-2 w-full rounded-full bg-zinc-100">
                      <div
                        className={`h-2 rounded-full ${getMetricBarColor(metric.eventType)}`}
                        style={{ width: `${Math.max(4, (metric.count / maxMetricCount) * 100)}%` }}
                      />
                    </div>
                  </div>
                ))
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* Users Management Tab */}
        <TabsContent value="users" className="space-y-4">
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
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default AdminPanel;

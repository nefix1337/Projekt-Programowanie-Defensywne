import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Settings, FolderPlus, LayoutDashboard, ClipboardList } from "lucide-react";
import api from "@/api/axiosInstance";
import {
  useSidebar,
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from "@/components/ui/sidebar";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip";
import { useAuth } from "@/auth/AuthProvider"; 

const SidebarRender = () => {
  const { state } = useSidebar();
  const { user, userData, getToken } = useAuth();
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchProjects = async () => {
      try {
        let response;
        if (user.role === "ROLE_USER") {
          response = await api.get("/projects/my-member-projects", {
            headers: { Authorization: `Bearer ${getToken()}` }
          });
        } else {
          response = await api.get("/projects", {
            headers: { Authorization: `Bearer ${getToken()}` }
          });
        }
        setProjects(response.data);
      } catch (error) {
        console.error("Error fetching projects:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchProjects();
  }, [getToken, user.role]);

  if (!user || loading) {
    return <div>Loading...</div>;
  }

  return (
    <TooltipProvider>
      <Sidebar collapsible="icon" className="flex flex-col h-full">
        <SidebarHeader className="flex flex-col gap-2 px-4">
          {/* App title */}
          <div className="flex items-center justify-between">
            {state === "expanded" && (
              <span className="flex items-center gap-2 text-lg font-semibold mx-auto truncate">
                <ClipboardList className="h-6 w-6" />
                TASK MANAGER
              </span>
            )}
          </div>
        </SidebarHeader>

        <SidebarContent className="flex-1">
          <SidebarGroup>
            <SidebarGroupContent>
              <SidebarMenu>
                <SidebarMenuItem className="mt-2 h-5">
                  <SidebarMenuButton asChild>
                    <Link to="/dashboard">
                      <div className="flex items-center gap-2">
                        <LayoutDashboard className="h-4 w-4" />
                        <span>Dashboard</span>
                      </div>
                    </Link>
                  </SidebarMenuButton>
                </SidebarMenuItem>

                {/* Add Project button for MANAGER role - moved above projects list */}
                {user.role === "ROLE_MANAGER" && (
                  <SidebarMenuItem className="mt-4 h-10">
                    <SidebarMenuButton asChild>
                      <Link to="/dashboard/projects/new">
                        {state === "expanded" ? (
                          <div className="flex items-center gap-2">
                            <FolderPlus className="h-4 w-4" />
                            <span>Dodaj projekt</span>
                          </div>
                        ) : (
                          <Tooltip>
                            <TooltipTrigger>
                              <FolderPlus className="h-4 w-4" />
                            </TooltipTrigger>
                            <TooltipContent side="right">
                              Dodaj projekt
                            </TooltipContent>
                          </Tooltip>
                        )}
                      </Link>
                    </SidebarMenuButton>
                  </SidebarMenuItem>
                )}

                {/* Projects List */}
                <div className="mt-4">
                  <div className="px-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                    Projekty
                  </div>
                  {projects.map((project) => (
                    <SidebarMenuItem key={project.id} className="mt-1 h-10">
                      <SidebarMenuButton asChild>
                        <Link to={`/dashboard/projects/${project.id}`}>
                          {state === "expanded" ? (
                            <div className="flex items-center gap-2">
                              <span className="text-xl">{project.icon}</span>
                              <span className="truncate">{project.name}</span>
                            </div>
                          ) : (
                            <Tooltip>
                              <TooltipTrigger>
                                <span className="text-xl">{project.icon}</span>
                              </TooltipTrigger>
                              <TooltipContent side="right">
                                {project.name}
                              </TooltipContent>
                            </Tooltip>
                          )}
                        </Link>
                      </SidebarMenuButton>
                    </SidebarMenuItem>
                  ))}
                </div>
              </SidebarMenu>
            </SidebarGroupContent>
          </SidebarGroup>
        </SidebarContent>

        {/* User profile section at the bottom */}
        <div className="flex items-center gap-2 p-4 border-t border-gray-200 mt-auto">
          <Avatar className="h-8 w-8">
            <AvatarImage src={userData.avatarUrl || "/placeholder.svg"} alt={userData.firstName} />
            <AvatarFallback>
              {userData.firstName?.[0]}
              {userData.lastName?.[0]}
            </AvatarFallback>
          </Avatar>

          {state === "expanded" && (
            <div className="flex flex-1 items-center justify-between">
              <span className="text-sm font-medium truncate">
                {userData.firstName} {userData.lastName}
              </span>
              <Tooltip>
                <TooltipTrigger asChild>
                  <Button variant="ghost" size="icon" className="h-7 w-7" asChild>
                    <Link to="/dashboard/settings">
                      <Settings className="h-4 w-4" />
                      <span className="sr-only">Settings</span>
                    </Link>
                  </Button>
                </TooltipTrigger>
                <TooltipContent side="right">Settings</TooltipContent>
              </Tooltip>
            </div>
          )}

          {state === "collapsed" && (
            <Tooltip>
              <TooltipTrigger asChild>
                <Button variant="ghost" size="icon" className="h-7 w-7" asChild>
                  <Link to="/dashboard/settings">
                    <Settings className="h-4 w-4" />
                    <span className="sr-only">Settings</span>
                  </Link>
                </Button>
              </TooltipTrigger>
              <TooltipContent side="right">Settings</TooltipContent>
            </Tooltip>
          )}
        </div>
      </Sidebar>
    </TooltipProvider>
  );
};

export default SidebarRender;

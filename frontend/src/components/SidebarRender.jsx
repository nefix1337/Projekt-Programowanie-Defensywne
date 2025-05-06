import { Link } from "react-router-dom";
import { Settings } from "lucide-react";
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
  const { userData } = useAuth(); 

  
  if (!userData) {
    return <div>Loading...</div>;
  }

  return (
    <TooltipProvider>
      <Sidebar collapsible="icon" className="flex flex-col h-full">
        <SidebarHeader className="flex flex-col gap-2 px-4">
          {/* App title */}
          <div className="flex items-center justify-between">
            {state === "expanded" && <span className="text-lg font-semibold truncate">TASKMANAGER</span>}
          </div>
        </SidebarHeader>

        <SidebarContent className="flex-1">
          <SidebarGroup>
            <SidebarGroupContent>
              <SidebarMenu>
                <SidebarMenuItem className="mt-2 h-10">
                  <SidebarMenuButton asChild>
                    <a>
                      <span>Dashboard</span>
                    </a>
                  </SidebarMenuButton>
                </SidebarMenuItem>
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

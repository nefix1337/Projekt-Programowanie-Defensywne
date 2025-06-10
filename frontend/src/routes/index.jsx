import {
  RouterProvider,
  createBrowserRouter,
} from "react-router-dom";
import ProtectedRoute from "../auth/ProtectedRoute";
import Login from "../pages/auth/Login";
import Register from "../pages/auth/Register";
import Dashboard from "../pages/Dashboard";
import AdminPanel from "../pages/admin/AdminPanel";
import Unauthorized from "../pages/Unauthorized"; 
import Settings from "../pages/Settings";
import NewProject from "../pages/projects/NewProject";
import ProjectDetails from "../pages/projects/ProjectDetails";
import ProjectMembers from "../pages/projects/ProjectMembers";
import AddTask from "../pages/projects/AddTask";

const Routes = () => {
  const publicRoutes = [
    {
      path: "/login",
      element: <Login />,
    },
    {
      path: "/register",
      element: <Register />,
    },
    {
      path: "/unauthorized",
      element: <Unauthorized />, 
    },
  ];

  const privateRoutes = [
    {
      path: "/dashboard",
      element: (
        <ProtectedRoute allowedRoles={["ROLE_USER", "ROLE_MANAGER"]}>
          <Dashboard />
        </ProtectedRoute>
      ),
      children: [
        {
          path: "settings", 
          element: <Settings />,
        },
        {
          path: "projects/new",
          element: (
            <ProtectedRoute allowedRoles={["ROLE_MANAGER"]}>
              <NewProject />
            </ProtectedRoute>
          ),
        },
        {
          path: "projects/:id",
          element: <ProjectDetails />,
        },
        {
          path: "projects/:id/members",
          element: (
            <ProtectedRoute allowedRoles={["ROLE_MANAGER"]}>
              <ProjectMembers />
            </ProtectedRoute>
          ),
        },
        {
          path: "projects/:id/tasks/new",
          element: (
            <ProtectedRoute allowedRoles={["ROLE_MANAGER"]}>
              <AddTask />
            </ProtectedRoute>
          ),
        },
      ],
    },
    {
      path: "/admin",
      element: (
        <ProtectedRoute allowedRoles={["ROLE_ADMIN"]}>
          <AdminPanel />
        </ProtectedRoute>
      ),
    },
  ];

  const router = createBrowserRouter([...publicRoutes, ...privateRoutes]);

  return <RouterProvider router={router} />;
};

export default Routes;

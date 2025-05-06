"use client";
import { useContext } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { AuthContext } from "../auth/AuthProvider";
import api from "../api/axiosInstance";
import { useState } from "react";
import SidebarRender from "@/components/SidebarRender";
import { Outlet } from "react-router-dom";

import { SidebarProvider } from "@/components/ui/sidebar"
export default function Dashboard() {

  

  return (
    <div className="min-h-screen flex overflow-hidden">
    <SidebarProvider>
    <SidebarRender/>
    <div className="flex w-full h-full flex-col">
      <div className="h-full w-full  bg-background">
        <Outlet />
      </div>
    </div>
    </SidebarProvider>
  </div>
  );
}

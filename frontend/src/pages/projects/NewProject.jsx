import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "@/auth/AuthProvider";
import { toast } from "sonner";
import EmojiPicker from 'emoji-picker-react';
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import api from "@/api/axiosInstance";

const projectStatuses = [
  { value: "NEW", label: "Nowy" },
  { value: "IN_PROGRESS", label: "W trakcie" },
  { value: "COMPLETED", label: "Zakończony" },
  { value: "ON_HOLD", label: "Wstrzymany" }
];

const NewProject = () => {
  const navigate = useNavigate();
  const { getToken } = useAuth();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    status: "NEW",
    icon: "💼" 
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleStatusChange = (status) => {
    setFormData(prev => ({
      ...prev,
      status
    }));
  };

  const handleEmojiSelect = (emojiData) => {
    setFormData(prev => ({
      ...prev,
      icon: emojiData.emoji
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      await api.post("/projects", formData, {
        headers: {
          Authorization: `Bearer ${getToken()}`
        }
      });

      toast.success("Projekt został utworzony");
      navigate("/dashboard");
    } catch (error) {
      console.error("Error creating project:", error);
      toast.error(
        error.response?.data?.message || "Wystąpił błąd podczas tworzenia projektu"
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-8">
      <Card>
        <CardHeader>
          <CardTitle>Nowy projekt</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="flex flex-col md:flex-row gap-4">
              <div className="flex-1 space-y-2">
                <label htmlFor="name" className="text-sm font-medium">
                  Nazwa projektu
                </label>
                <Input
                  id="name"
                  name="name"
                  value={formData.name}
                  onChange={handleChange}
                  placeholder="Wprowadź nazwę projektu"
                  required
                />
              </div>
              <div className="flex flex-col justify-end items-start md:items-end min-w-[160px]">
                <label className="text-sm font-medium mb-1 md:mb-2 whitespace-nowrap">
                  Ikona projektu
                </label>
                <Popover>
                  <PopoverTrigger asChild>
                    <Button variant="outline" className="w-16 h-10 text-2xl">
                      {formData.icon}
                    </Button>
                  </PopoverTrigger>
                  <PopoverContent className="w-full p-0">
                    <EmojiPicker
                      onEmojiClick={handleEmojiSelect}
                      width="100%"
                      height={400}
                    />
                  </PopoverContent>
                </Popover>
              </div>
            </div>

            <div className="space-y-2">
              <label htmlFor="description" className="text-sm font-medium">
                Opis projektu
              </label>
              <Textarea
                id="description"
                name="description"
                value={formData.description}
                onChange={handleChange}
                placeholder="Wprowadź opis projektu"
              />
            </div>

            <div className="flex flex-col md:flex-row md:items-center gap-4">
              <div className="flex-1 space-y-2">
                <label htmlFor="status" className="text-sm font-medium">
                  Status projektu
                </label>
                <Select
                  value={formData.status}
                  onValueChange={handleStatusChange}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Wybierz status" />
                  </SelectTrigger>
                  <SelectContent>
                    {projectStatuses.map(status => (
                      <SelectItem key={status.value} value={status.value}>
                        {status.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="flex-1" />
            </div>

            <div className="flex justify-end space-x-4">
              <Button
                type="button"
                variant="outline"
                onClick={() => navigate("/dashboard/projects")}
              >
                Anuluj
              </Button>
              <Button type="submit" disabled={loading}>
                {loading ? "Tworzenie..." : "Utwórz projekt"}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
};

export default NewProject;
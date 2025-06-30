package pl.projekt.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import pl.projekt.backend.dto.AddProjectMemberRequest;
import pl.projekt.backend.dto.CreateProjectRequest;
import pl.projekt.backend.dto.ProjectMemberResponse;
import pl.projekt.backend.model.Project;
import pl.projekt.backend.service.ProjectService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testy jednostkowe kontrolera ProjectController.
 * Sprawdzają obsługę endpointów związanych z projektami.
 */
@DisplayName("Testy kontrolera ProjectController")
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    private Project project;
    private CreateProjectRequest createProjectRequest;
    private ProjectMemberResponse memberResponse;
    private AddProjectMemberRequest addMemberRequest;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        project = new Project();
        project.setId(projectId);
        project.setName("Testowy projekt");

        createProjectRequest = new CreateProjectRequest();
        createProjectRequest.setName("Nowy projekt");

        memberResponse = new ProjectMemberResponse();
        addMemberRequest = new AddProjectMemberRequest();
    }

    /**
     * Powinien zwrócić listę projektów aktualnego użytkownika.
     */
    @Test
    @DisplayName("Pobieranie wszystkich projektów użytkownika")
    void getAllProjectsForCurrentUser_ShouldReturnProjects() {
        List<Project> projects = List.of(project);
        when(projectService.getAllProjectsForCurrentUser()).thenReturn(projects);

        ResponseEntity<List<Project>> response = projectController.getAllProjectsForCurrentUser();

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(projects, response.getBody());
        verify(projectService).getAllProjectsForCurrentUser();
    }

    /**
     * Powinien zwrócić projekt po ID.
     */
    @Test
    @DisplayName("Pobieranie projektu po ID")
    void getProjectById_ShouldReturnProject() {
        when(projectService.getProjectById(projectId)).thenReturn(Optional.of(project));

        ResponseEntity<Project> response = projectController.getProjectById(projectId);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(project, response.getBody());
        verify(projectService).getProjectById(projectId);
    }

    /**
     * Powinien zwrócić 404 jeśli projekt nie istnieje.
     */
    @Test
    @DisplayName("Pobieranie projektu po ID - brak projektu")
    void getProjectById_ShouldReturnNotFound() {
        when(projectService.getProjectById(projectId)).thenReturn(Optional.empty());

        ResponseEntity<Project> response = projectController.getProjectById(projectId);

        assertNotNull(response);
        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(projectService).getProjectById(projectId);
    }

    /**
     * Powinien utworzyć nowy projekt.
     */
    @Test
    @DisplayName("Tworzenie nowego projektu")
    void createProject_ShouldReturnCreatedProject() {
        when(projectService.createProject(createProjectRequest)).thenReturn(project);

        ResponseEntity<Project> response = projectController.createProject(createProjectRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(project, response.getBody());
        verify(projectService).createProject(createProjectRequest);
    }

    /**
     * Powinien zaktualizować projekt.
     */
    @Test
    @DisplayName("Aktualizacja projektu")
    void updateProject_ShouldReturnUpdatedProject() {
        when(projectService.updateProject(projectId, project)).thenReturn(project);

        ResponseEntity<Project> response = projectController.updateProject(projectId, project);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(project, response.getBody());
        verify(projectService).updateProject(projectId, project);
    }

    /**
     * Powinien usunąć projekt i zwrócić 204 No Content.
     */
    @Test
    @DisplayName("Usuwanie projektu")
    void deleteProject_ShouldReturnNoContent() {
        ResponseEntity<Void> response = projectController.deleteProject(projectId);

        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());
        verify(projectService).deleteProject(projectId);
    }

    /**
     * Powinien dodać członka do projektu.
     */
    @Test
    @DisplayName("Dodawanie użytkownika do projektu")
    void addProjectMember_ShouldReturnMemberResponse() {
        when(projectService.addProjectMember(projectId, addMemberRequest)).thenReturn(memberResponse);

        ResponseEntity<ProjectMemberResponse> response = projectController.addProjectMember(projectId, addMemberRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(memberResponse, response.getBody());
        verify(projectService).addProjectMember(projectId, addMemberRequest);
    }

    /**
     * Powinien zwrócić listę członków projektu.
     */
    @Test
    @DisplayName("Pobieranie członków projektu")
    void getProjectMembers_ShouldReturnMembers() {
        List<ProjectMemberResponse> members = List.of(memberResponse);
        when(projectService.getProjectMembers(projectId)).thenReturn(members);

        ResponseEntity<List<ProjectMemberResponse>> response = projectController.getProjectMembers(projectId);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(members, response.getBody());
        verify(projectService).getProjectMembers(projectId);
    }

    /**
     * Powinien usunąć członka z projektu i zwrócić 204 No Content.
     */
    @Test
    @DisplayName("Usuwanie użytkownika z projektu")
    void removeProjectMember_ShouldReturnNoContent() {
        Long userId = 2L;

        ResponseEntity<Void> response = projectController.removeProjectMember(projectId, userId);

        assertNotNull(response);
        assertEquals(204, response.getStatusCodeValue());
        verify(projectService).removeProjectMember(projectId, userId);
    }

    /**
     * Powinien zwrócić projekty, w których użytkownik jest członkiem.
     */
    @Test
    @DisplayName("Pobieranie projektów, w których jestem członkiem")
    void getProjectsWhereCurrentUserIsMember_ShouldReturnProjects() {
        List<Project> projects = List.of(project);
        when(projectService.getProjectsWhereCurrentUserIsMember()).thenReturn(projects);

        ResponseEntity<List<Project>> response = projectController.getProjectsWhereCurrentUserIsMember();

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(projects, response.getBody());
        verify(projectService).getProjectsWhereCurrentUserIsMember();
    }
}

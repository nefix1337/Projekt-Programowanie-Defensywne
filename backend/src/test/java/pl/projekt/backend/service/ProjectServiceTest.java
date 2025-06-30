package pl.projekt.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.projekt.backend.dto.AddProjectMemberRequest;
import pl.projekt.backend.dto.CreateProjectRequest;
import pl.projekt.backend.dto.ProjectMemberResponse;
import pl.projekt.backend.model.*;
import pl.projekt.backend.repository.ProjectMemberRepository;
import pl.projekt.backend.repository.ProjectRepository;
import pl.projekt.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testy jednostkowe serwisu ProjectService.
 * Sprawdzają operacje na projektach i członkach projektów.
 */
@DisplayName("Testy serwisu ProjectService")
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class ProjectServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProjectMemberRepository projectMemberRepository;

    @InjectMocks private ProjectService projectService;

    private User user;
    private Project project;
    private ProjectMember member;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFirstName("Jan");
        user.setLastName("Kowalski");
        user.setEmail("jan.kowalski@example.com");

        project = new Project();
        project.setId(UUID.randomUUID());
        project.setName("Test Project");
        project.setCreatedBy(user);
        project.setStatus(ProjectStatus.IN_PROGRESS);

        member = new ProjectMember();
        member.setId(1L);
        member.setUser(user);
        member.setProject(project);
        member.setProjectRole(ProjectRole.DEVELOPER);
        member.setJoinedAt(LocalDateTime.now());
    }

    /**
     * Powinien zwrócić projekty utworzone przez aktualnego użytkownika.
     */
    @Test
    @DisplayName("Pobieranie projektów utworzonych przez aktualnego użytkownika")
    void getAllProjectsForCurrentUser_ShouldReturnProjects() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(user.getEmail());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(projectRepository.findByCreatedBy(user)).thenReturn(List.of(project));

        List<Project> result = projectService.getAllProjectsForCurrentUser();

        assertEquals(1, result.size());
        assertEquals(project, result.get(0));
    }

    /**
     * Powinien utworzyć nowy projekt.
     */
    @Test
    @DisplayName("Tworzenie nowego projektu")
    void createProject_ShouldCreateProject() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("Nowy projekt");
        request.setDescription("Opis");
        request.setStatus("IN_PROGRESS");
        request.setIcon("icon");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(user.getEmail());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        Project result = projectService.createProject(request);

        assertNotNull(result);
        assertEquals("Test Project", result.getName());
        verify(projectRepository).save(any(Project.class));
    }

    /**
     * Powinien zaktualizować istniejący projekt.
     */
    @Test
    @DisplayName("Aktualizacja projektu")
    void updateProject_ShouldUpdateProject() {
        Project updated = new Project();
        updated.setName("Zmieniony");
        updated.setDescription("Nowy opis");
        updated.setStatus(ProjectStatus.DONE);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(updated);

        Project result = projectService.updateProject(project.getId(), updated);

        assertNotNull(result);
        assertEquals("Zmieniony", result.getName());
        assertEquals(ProjectStatus.DONE, result.getStatus());
    }

    /**
     * Powinien usunąć projekt.
     */
    @Test
    @DisplayName("Usuwanie projektu")
    void deleteProject_ShouldDeleteProject() {
        projectService.deleteProject(project.getId());
        verify(projectRepository).deleteById(project.getId());
    }

    /**
     * Powinien dodać członka do projektu.
     */
    @Test
    @DisplayName("Dodawanie członka do projektu")
    void addProjectMember_ShouldAddMember() {
        AddProjectMemberRequest request = new AddProjectMemberRequest();
        request.setUserEmail(user.getEmail());
        request.setProjectRole(ProjectRole.DEVELOPER);

        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(projectMemberRepository.findByProjectAndUser(project, user)).thenReturn(Optional.empty());
        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(member);

        ProjectMemberResponse response = projectService.addProjectMember(project.getId(), request);

        assertNotNull(response);
        assertEquals(user.getEmail(), response.getUserEmail());
        assertEquals(ProjectRole.DEVELOPER, response.getProjectRole());
    }

    /**
     * Powinien zwrócić listę członków projektu.
     */
    @Test
    @DisplayName("Pobieranie członków projektu")
    void getProjectMembers_ShouldReturnMembers() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(projectMemberRepository.findByProject(project)).thenReturn(List.of(member));

        List<ProjectMemberResponse> result = projectService.getProjectMembers(project.getId());

        assertEquals(1, result.size());
        assertEquals(user.getEmail(), result.get(0).getUserEmail());
    }

    /**
     * Powinien usunąć członka z projektu.
     */
    @Test
    @DisplayName("Usuwanie członka z projektu")
    void removeProjectMember_ShouldRemoveMember() {
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(projectMemberRepository.findByProjectAndUser(project, user)).thenReturn(Optional.of(member));

        projectService.removeProjectMember(project.getId(), user.getId());

        verify(projectMemberRepository).delete(member);
    }

    /**
     * Powinien zwrócić projekty, w których użytkownik jest członkiem.
     */
    @Test
    @DisplayName("Pobieranie projektów, w których użytkownik jest członkiem")
    void getProjectsWhereCurrentUserIsMember_ShouldReturnProjects() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(user.getEmail());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(projectMemberRepository.findByUser(user)).thenReturn(List.of(member));

        List<Project> result = projectService.getProjectsWhereCurrentUserIsMember();

        assertEquals(1, result.size());
        assertEquals(project, result.get(0));
    }
}

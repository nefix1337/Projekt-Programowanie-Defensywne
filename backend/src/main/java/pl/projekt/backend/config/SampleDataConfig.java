package pl.projekt.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.projekt.backend.model.Project;
import pl.projekt.backend.model.ProjectMember;
import pl.projekt.backend.model.ProjectRole;
import pl.projekt.backend.model.ProjectStatus;
import pl.projekt.backend.model.Role;
import pl.projekt.backend.model.Task;
import pl.projekt.backend.model.TaskPriority;
import pl.projekt.backend.model.TaskStatus;
import pl.projekt.backend.model.User;
import pl.projekt.backend.repository.ProjectMemberRepository;
import pl.projekt.backend.repository.ProjectRepository;
import pl.projekt.backend.repository.TaskRepository;
import pl.projekt.backend.repository.UserRepository;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class SampleDataConfig {
    private static final String SAMPLE_PROJECT_NAME = "Przykladowy projekt CRM";

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initSampleData() {
        return args -> {
            User manager = findOrCreateUser("Manager", "Projektu", "manager@example.com", "manager123", Role.MANAGER);
            User developer = findOrCreateUser("Jan", "Developer", "developer@example.com", "user123", Role.USER);
            User tester = findOrCreateUser("Anna", "Tester", "tester@example.com", "user123", Role.USER);

            Project project = findOrCreateProject(manager);

            findOrCreateMember(project, manager, ProjectRole.PROJECT_MANAGER);
            findOrCreateMember(project, developer, ProjectRole.DEVELOPER);
            findOrCreateMember(project, tester, ProjectRole.TESTER);

            findOrCreateTask(project, manager, developer, "Przygotowac widok listy zadan",
                    "Dodac podstawowy widok zadan projektu w aplikacji React.",
                    TaskStatus.IN_PROGRESS, TaskPriority.HIGH, LocalDateTime.now().plusDays(3));
            findOrCreateTask(project, manager, tester, "Przetestowac logowanie 2FA",
                    "Sprawdzic scenariusze logowania z wlaczona i wylaczona dwuskladnikowa autentykacja.",
                    TaskStatus.TODO, TaskPriority.MEDIUM, LocalDateTime.now().plusDays(5));
            findOrCreateTask(project, manager, developer, "Podlaczyc RabbitMQ dla tworzenia zadan",
                    "Zweryfikowac zapis zadania przez wezel node i kolejke RabbitMQ.",
                    TaskStatus.TO_REVIEW, TaskPriority.HIGH, LocalDateTime.now().plusDays(1));
        };
    }

    private User findOrCreateUser(String firstName, String lastName, String email, String password, Role role) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User user = new User();
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setEmail(email);
                    user.setPassword(passwordEncoder.encode(password));
                    user.setRole(role);
                    user.setTwoFactorEnabled(false);
                    return userRepository.save(user);
                });
    }

    private Project findOrCreateProject(User manager) {
        return projectRepository.findByCreatedBy(manager).stream()
                .filter(project -> SAMPLE_PROJECT_NAME.equals(project.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Project project = new Project();
                    project.setName(SAMPLE_PROJECT_NAME);
                    project.setDescription("Projekt demonstracyjny z przykladowymi czlonkami i zadaniami.");
                    project.setCreatedBy(manager);
                    project.setStatus(ProjectStatus.IN_PROGRESS);
                    project.setIcon("CRM");
                    return projectRepository.save(project);
                });
    }

    private void findOrCreateMember(Project project, User user, ProjectRole role) {
        projectMemberRepository.findByProjectAndUser(project, user)
                .orElseGet(() -> {
                    ProjectMember member = new ProjectMember();
                    member.setProject(project);
                    member.setUser(user);
                    member.setProjectRole(role);
                    member.setJoinedAt(LocalDateTime.now());
                    return projectMemberRepository.save(member);
                });
    }

    private void findOrCreateTask(
            Project project,
            User createdBy,
            User assignedTo,
            String title,
            String description,
            TaskStatus status,
            TaskPriority priority,
            LocalDateTime dueDate) {
        boolean exists = taskRepository.findByProject(project).stream()
                .anyMatch(task -> title.equals(task.getTitle()));

        if (exists) {
            return;
        }

        Task task = new Task();
        task.setProject(project);
        task.setCreatedBy(createdBy);
        task.setAssignedTo(assignedTo);
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setPriority(priority);
        task.setDueDate(dueDate);
        taskRepository.save(task);
    }
}

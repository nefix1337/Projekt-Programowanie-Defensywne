package pl.projekt.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.projekt.backend.model.Project;
import pl.projekt.backend.model.ProjectMember;
import pl.projekt.backend.model.User;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    List<ProjectMember> findByProject(Project project);
    Optional<ProjectMember> findByProjectAndUser(Project project, User user);
    List<ProjectMember> findByUser(User user);
}
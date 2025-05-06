package pl.projekt.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.projekt.backend.model.Project;
import pl.projekt.backend.model.User;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByCreatedBy(User user);
}
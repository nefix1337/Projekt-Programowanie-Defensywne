package pl.projekt.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pl.projekt.backend.service.AuthService;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner initAdmin(AuthService authService) {
        return args -> {
            authService.registerAdmin(
                "Admin",             
                "User",             
                "admin@example.com", 
                "admin123"
            );
        };
    }
}

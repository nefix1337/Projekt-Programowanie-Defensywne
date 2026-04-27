package com.bsr.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "projects")
public class Project {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
}

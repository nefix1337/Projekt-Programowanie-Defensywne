package pl.projekt.backend.model;

public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    TO_REVIEW,   // <-- Nowy status: przekazane do sprawdzenia
    VERIFIED,
    DONE,
    ARCHIVED
}
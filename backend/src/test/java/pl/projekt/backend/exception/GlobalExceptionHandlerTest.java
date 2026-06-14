package pl.projekt.backend.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import pl.projekt.backend.dto.ErrorResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testy jednostkowe GlobalExceptionHandler.
 * Sprawdzają mapowanie wyjątków na odpowiednie kody i treści odpowiedzi HTTP.
 */
@DisplayName("Testy GlobalExceptionHandler")
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        lenient().when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    @DisplayName("Błędy walidacji DTO mapowane na 400 z mapą błędów pól")
    void handleValidation_ShouldReturnBadRequestWithFieldErrors() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("request", "title", "must not be blank");
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = handler.handleValidation(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertEquals("must not be blank", response.getBody().getValidationErrors().get("title"));
    }

    @Test
    @DisplayName("Niepoprawny JSON / nieznana wartość enum mapowane na 400")
    void handleMalformedRequest_ShouldReturnBadRequest() {
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);

        ResponseEntity<ErrorResponse> response = handler.handleMalformedRequest(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Malformed JSON request body", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    @DisplayName("Niepoprawny typ parametru ścieżki/zapytania mapowany na 400")
    void handleTypeMismatch_ShouldReturnBadRequest() {
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "abc", Long.class, "id", mock(MethodParameter.class), new IllegalArgumentException());

        ResponseEntity<ErrorResponse> response = handler.handleTypeMismatch(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid value for parameter 'id'", response.getBody().getMessage());
    }

    @Test
    @DisplayName("EntityNotFoundException mapowany na 404")
    void handleNotFound_ShouldReturnNotFound_ForEntityNotFound() {
        EntityNotFoundException exception = new EntityNotFoundException("Task not found");

        ResponseEntity<ErrorResponse> response = handler.handleNotFound(exception, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Task not found", response.getBody().getMessage());
    }

    @Test
    @DisplayName("UsernameNotFoundException mapowany na 404")
    void handleNotFound_ShouldReturnNotFound_ForUsernameNotFound() {
        UsernameNotFoundException exception = new UsernameNotFoundException("User not found");

        ResponseEntity<ErrorResponse> response = handler.handleNotFound(exception, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody().getMessage());
    }

    @Test
    @DisplayName("AccessDeniedException mapowany na 403")
    void handleAccessDenied_ShouldReturnForbidden() {
        AccessDeniedException exception = new AccessDeniedException("denied");

        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(exception, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied", response.getBody().getMessage());
    }

    @Test
    @DisplayName("IllegalArgumentException mapowany na 400")
    void handleBadRequest_ShouldReturnBadRequest() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid input");

        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid input", response.getBody().getMessage());
    }

    @Test
    @DisplayName("ResponseStatusException zachowuje przekazany status")
    void handleResponseStatus_ShouldUseExceptionStatus() {
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.CONFLICT, "Conflict occurred");

        ResponseEntity<ErrorResponse> response = handler.handleResponseStatus(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Conflict occurred", response.getBody().getMessage());
    }

    @Test
    @DisplayName("RuntimeException z komunikatem o RabbitMQ mapowany na 503")
    void handleRuntime_ShouldReturnServiceUnavailable_ForDistributedSystemError() {
        RuntimeException exception = new RuntimeException("Task creation timed out");

        ResponseEntity<ErrorResponse> response = handler.handleRuntime(exception, request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    @DisplayName("RuntimeException rozpoznany jako błąd klienta mapowany na 400")
    void handleRuntime_ShouldReturnBadRequest_ForClientError() {
        RuntimeException exception = new RuntimeException("Project not found");

        ResponseEntity<ErrorResponse> response = handler.handleRuntime(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Nierozpoznany RuntimeException mapowany na 500")
    void handleRuntime_ShouldReturnInternalServerError_ForUnknownError() {
        RuntimeException exception = new RuntimeException("boom");

        ResponseEntity<ErrorResponse> response = handler.handleRuntime(exception, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("Niespodziewany wyjątek mapowany na 500 z ogólnym komunikatem")
    void handleUnexpected_ShouldReturnInternalServerError() {
        Exception exception = new Exception("something went very wrong");

        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(exception, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected server error", response.getBody().getMessage());
    }
}

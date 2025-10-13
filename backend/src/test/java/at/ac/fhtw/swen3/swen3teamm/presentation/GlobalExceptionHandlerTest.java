package at.ac.fhtw.swen3.swen3teamm.presentation;

import at.ac.fhtw.swen3.swen3teamm.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    //einfach alle Exceptions durchgetestet

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/test/uri");
    }

    @Test
    void onValidation_shouldReturnBadRequest() {
        ValidationException ex = new ValidationException("Invalid input");

        ResponseEntity<?> response = handler.onValidation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(((org.springframework.http.ProblemDetail)response.getBody()).getTitle()).isEqualTo("ValidationException");
    }

    @Test
    void onOther_shouldReturnInternalServerError() {
        Exception ex = new Exception("Something bad");

        ResponseEntity<?> response = handler.onOther(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(((org.springframework.http.ProblemDetail)response.getBody()).getTitle()).isEqualTo("InternalServerError");
    }

    @Test
    void onMissingFile_shouldReturnBadRequest() {
        MissingServletRequestPartException ex = mock(MissingServletRequestPartException.class);

        ResponseEntity<?> response = handler.onMissingFile(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(((org.springframework.http.ProblemDetail)response.getBody()).getTitle()).isEqualTo("ValidationException");
    }

    @Test
    void onUploadTooLarge_shouldReturnPayloadTooLarge() {
        MaxUploadSizeExceededException ex = mock(MaxUploadSizeExceededException.class);

        ResponseEntity<?> response = handler.onUploadTooLarge(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(((org.springframework.http.ProblemDetail)response.getBody()).getTitle()).isEqualTo("UploadTooLarge");
    }

    @Test
    void onNotFound_shouldReturnNotFound() {
        DocumentNotFoundException ex = new DocumentNotFoundException("Doc not found");

        ResponseEntity<?> response = handler.onNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(((org.springframework.http.ProblemDetail)response.getBody()).getTitle()).isEqualTo("DocumentNotFound");
    }

    @Test
    void onTypeMismatch_shouldReturnBadRequestForUUID() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "invalid", UUID.class, "id", null, null
        );

        ResponseEntity<?> response = handler.onTypeMismatch(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(((org.springframework.http.ProblemDetail)response.getBody()).getTitle()).isEqualTo("ValidationException");
        assertThat(((org.springframework.http.ProblemDetail)response.getBody()).getDetail()).isEqualTo("Invalid UUID in path");
    }
}

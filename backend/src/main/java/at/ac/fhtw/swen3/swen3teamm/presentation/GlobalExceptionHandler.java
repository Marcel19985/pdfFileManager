package at.ac.fhtw.swen3.swen3teamm.presentation;

import at.ac.fhtw.swen3.swen3teamm.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ProblemDetail> onValidation(ValidationException ex, HttpServletRequest req) {
        return problem(HttpStatus.BAD_REQUEST, "ValidationException", ex.getMessage(), req);
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ProblemDetail> onMessaging(MessagingException ex, HttpServletRequest req) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "MessagingException", ex.getMessage(), req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> onOther(Exception ex, HttpServletRequest req) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "InternalServerError", "Unexpected error", req);
    }

    @ExceptionHandler({MissingServletRequestPartException.class, MultipartException.class})
    public ResponseEntity<ProblemDetail> onMissingFile(Exception ex, HttpServletRequest req) {
        // tritt auf, wenn das 'file'-Form-Field fehlt oder Multipart kaputt ist
        return problem(HttpStatus.BAD_REQUEST, "ValidationException", "Required part 'file' is missing or invalid", req);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> onUploadTooLarge(MaxUploadSizeExceededException ex, HttpServletRequest req) {
        return problem(HttpStatus.PAYLOAD_TOO_LARGE, "UploadTooLarge", "File too large", req);
    }

    // 404 für Dokument nicht gefunden
    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<ProblemDetail> onNotFound(DocumentNotFoundException ex, HttpServletRequest req) {
        return problem(HttpStatus.NOT_FOUND, "DocumentNotFound", ex.getMessage(), req);
    }

    // 400 wenn die Pfad-UUID ungültig ist (z. B. Tippfehler)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> onTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        if (ex.getRequiredType() == java.util.UUID.class) {
            return problem(HttpStatus.BAD_REQUEST, "ValidationException", "Invalid UUID in path", req);
        }
        return problem(HttpStatus.BAD_REQUEST, "ValidationException", "Invalid parameter", req);
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String title, String detail, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setProperty("path", req.getRequestURI());
        return ResponseEntity.status(status)
                .header("Content-Type","application/problem+json")
                .body(pd);
    }
}
package com.datacenterflow.infrastructure.exception;

import com.datacenterflow.auth.domain.exception.UserNotFoundException;
import com.datacenterflow.simulation.domain.exception.SimulationRunNotFoundException;
import com.datacenterflow.cad.domain.exception.CadFileNotFoundException;
import com.datacenterflow.cad.domain.exception.CadFileStorageException;
import com.datacenterflow.cad.domain.exception.InvalidCadFileException;
import com.datacenterflow.project.domain.exception.ProjectAccessDeniedException;
import com.datacenterflow.project.domain.exception.ProjectNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    ProblemDetail handleProjectNotFound(ProjectNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ProjectAccessDeniedException.class)
    ProblemDetail handleProjectAccessDenied(ProjectAccessDeniedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining("; "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    }

    @ExceptionHandler(SimulationRunNotFoundException.class)
    ProblemDetail handleSimulationRunNotFound(SimulationRunNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CadFileNotFoundException.class)
    ProblemDetail handleCadFileNotFound(CadFileNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidCadFileException.class)
    ProblemDetail handleInvalidCadFile(InvalidCadFileException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(CadFileStorageException.class)
    ProblemDetail handleStorageError(CadFileStorageException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, "Storage operation failed: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }
}

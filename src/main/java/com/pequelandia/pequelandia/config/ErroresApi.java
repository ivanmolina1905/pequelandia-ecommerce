package com.pequelandia.pequelandia.config;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ErroresApi {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> acceso(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("ok", false,
                "error", ex.getReason() == null ? "No autorizado" : ex.getReason(),
                "redirect", ex.getStatusCode().value() == 401 ? "/auth/login" : ""));
    }
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> inexistente(NoSuchElementException ex) {
        return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Registro no encontrado"));
    }
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<?> invalido(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of("ok", false, "error",
                ex.getMessage() == null ? "Operación inválida" : ex.getMessage()));
    }
}

package com.pequelandia.pequelandia.config;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
import java.util.NoSuchElementException;

/** [Doc]
 * Traduce excepciones de acceso, búsqueda y validación a respuestas HTTP.
 */
@RestControllerAdvice
public class ErroresApi {
    /** [Doc]
     * Convierte una excepción de estado HTTP en una respuesta de error con el estado correspondiente.
     * @param ex excepción capturada para tratar el fallo.
     * @return ResponseEntity&lt;?&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> acceso(ResponseStatusException ex) {
        // [Doc] Termina el método o la función y devuelve ResponseEntity.status(ex.getStatusCode()).body(Map.of("ok", false, "error", ex.getReason() == null ? "No autorizado" : ex.getReason(), "redirect", ex.getStat....
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("ok", false,
                "error", ex.getReason() == null ? "No autorizado" : ex.getReason(),
                "redirect", ex.getStatusCode().value() == 401 ? "/auth/login" : ""));
    }
    /** [Doc]
     * Convierte la ausencia de una entidad en una respuesta HTTP de recurso no encontrado.
     * @param ex excepción capturada para tratar el fallo.
     * @return ResponseEntity&lt;?&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> inexistente(NoSuchElementException ex) {
        // [Doc] Termina el método o la función y devuelve ResponseEntity.status(404).body(Map.of("ok", false, "error", "Registro no encontrado")).
        return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Registro no encontrado"));
    }
    /** [Doc]
     * Convierte una operación inválida en una respuesta de error de validación.
     * @param ex excepción capturada para tratar el fallo.
     * @return ResponseEntity&lt;?&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<?> invalido(RuntimeException ex) {
        // [Doc] Termina el método o la función y devuelve ResponseEntity.badRequest().body(Map.of("ok", false, "error", ex.getMessage() == null ? "Operación inválida" : ex.getMessage())).
        return ResponseEntity.badRequest().body(Map.of("ok", false, "error",
                ex.getMessage() == null ? "Operación inválida" : ex.getMessage()));
    }
}

package com.pequelandia.pequelandia.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.service.INotificacionService;

import jakarta.servlet.http.HttpSession;

/** [Doc]
 * Expone el listado y las operaciones de lectura de avisos para el destinatario de la sesión.
 */
@RestController
@RequestMapping("/notificaciones")
public class NotificacionController {

    // [Doc] Atributo identidad (com.pequelandia.pequelandia.service.IdentidadService): colaborador que concentra operaciones de com.pequelandia.pequelandia.service.identidad.
    @org.springframework.beans.factory.annotation.Autowired
    private com.pequelandia.pequelandia.service.IdentidadService identidad;
    // [Doc] Atributo notificacionService (INotificacionService): colaborador que concentra operaciones de notificacion. Su referencia no se reasigna después de inicializarse.
    private final INotificacionService notificacionService;

    /** [Doc]
     * Construye NotificacionController y conserva sus dependencias o valores iniciales.
     * @param notificacionService colaborador que concentra operaciones de notificacion.
     */
    public NotificacionController(
            INotificacionService notificacionService
    ) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.notificacionService = notificacionService;
        this.notificacionService = notificacionService;
    }

    // ======================================================
    // LISTAR NOTIFICACIONES DEL USUARIO LOGUEADO
    // ======================================================

    /** [Doc]
     * Consulta y devuelve los datos de la operación en el ámbito de NotificacionController.
     * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
     * @return Map&lt;String, Object&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping("/mias")
    public Map<String, Object> listar(HttpSession session) {

        // [Doc] Variable local destinatario (String): identidad o dirección que recibirá el aviso o correo. Se inicializa con obtenerDestinatario(session).
        String destinatario = obtenerDestinatario(session);

        // [Doc] Evalúa destinatario == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (destinatario == null) {
            // [Doc] Termina el método o la función y devuelve respuestaSesionVencida().
            return respuestaSesionVencida();
        }

        // [Doc] Ejecuta un bloque con tratamiento de excepciones; los catch asociados atienden los fallos declarados y finally se ejecuta al salir si existe.
        try {
            // [Doc] Termina el método o la función y devuelve Map.of( "ok", true, "noLeidas", notificacionService.contarNoLeidas( destinatario ), "notificaciones", notificacionService.listarUltimas( destinatario ) ).
            return Map.of(
                    "ok", true,
                    "noLeidas",
                    notificacionService.contarNoLeidas(
                            destinatario
                    ),
                    "notificaciones",
                    notificacionService.listarUltimas(
                            destinatario
                    )
            );

        } catch (Exception e) {
            // [Doc] Termina el método o la función y devuelve Map.of( "ok", false, "error", "No se pudieron cargar las notificaciones." ).
            return Map.of(
                    "ok", false,
                    "error",
                    "No se pudieron cargar las notificaciones."
            );
        }
    }

    // ======================================================
    // MARCAR UNA NOTIFICACIÓN COMO LEÍDA
    // ======================================================

    /** [Doc]
     * Registra como leida en el ámbito de NotificacionController.
     * @param id identificador del registro.
     * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
     * @return Map&lt;String, Object&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @PostMapping("/{id}/leer")
    public Map<String, Object> marcarComoLeida(
            @PathVariable Long id,
            HttpSession session
    ) {
        // [Doc] Variable local destinatario (String): identidad o dirección que recibirá el aviso o correo. Se inicializa con obtenerDestinatario(session).
        String destinatario = obtenerDestinatario(session);

        // [Doc] Evalúa destinatario == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (destinatario == null) {
            // [Doc] Termina el método o la función y devuelve respuestaSesionVencida().
            return respuestaSesionVencida();
        }

        // [Doc] Ejecuta un bloque con tratamiento de excepciones; los catch asociados atienden los fallos declarados y finally se ejecuta al salir si existe.
        try {
            // [Doc] Ejecuta la llamada o modificación notificacionService.marcarComoLeida( id, destinatario ); Sus efectos dependen del método invocado y del objeto receptor.
            notificacionService.marcarComoLeida(
                    id,
                    destinatario
            );

            // [Doc] Termina el método o la función y devuelve Map.of("ok", true).
            return Map.of("ok", true);

        } catch (IllegalArgumentException e) {
            // [Doc] Termina el método o la función y devuelve Map.of( "ok", false, "error", e.getMessage() ).
            return Map.of(
                    "ok", false,
                    "error", e.getMessage()
            );

        } catch (Exception e) {
            // [Doc] Termina el método o la función y devuelve Map.of( "ok", false, "error", "No se pudo marcar la notificación como leída." ).
            return Map.of(
                    "ok", false,
                    "error",
                    "No se pudo marcar la notificación como leída."
            );
        }
    }

    // ======================================================
    // MARCAR TODAS COMO LEÍDAS
    // ======================================================

    /** [Doc]
     * Registra todas en el ámbito de NotificacionController.
     * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
     * @return Map&lt;String, Object&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @PostMapping("/leer-todas")
    public Map<String, Object> marcarTodas(
            HttpSession session
    ) {
        // [Doc] Variable local destinatario (String): identidad o dirección que recibirá el aviso o correo. Se inicializa con obtenerDestinatario(session).
        String destinatario = obtenerDestinatario(session);

        // [Doc] Evalúa destinatario == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (destinatario == null) {
            // [Doc] Termina el método o la función y devuelve respuestaSesionVencida().
            return respuestaSesionVencida();
        }

        // [Doc] Ejecuta un bloque con tratamiento de excepciones; los catch asociados atienden los fallos declarados y finally se ejecuta al salir si existe.
        try {
            // [Doc] Ejecuta la llamada o modificación notificacionService.marcarTodasComoLeidas( destinatario ); Sus efectos dependen del método invocado y del objeto receptor.
            notificacionService.marcarTodasComoLeidas(
                    destinatario
            );

            // [Doc] Termina el método o la función y devuelve Map.of("ok", true).
            return Map.of("ok", true);

        } catch (Exception e) {
            // [Doc] Termina el método o la función y devuelve Map.of( "ok", false, "error", "No se pudieron marcar las notificaciones como leídas." ).
            return Map.of(
                    "ok", false,
                    "error",
                    "No se pudieron marcar las notificaciones como leídas."
            );
        }
    }

    // ======================================================
    // DESTINATARIO SEGÚN SESIÓN
    // ======================================================

    /** [Doc]
     * Obtiene destinatario en el ámbito de NotificacionController.
     * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private String obtenerDestinatario(
            HttpSession session
    ) {
        // [Doc] Variable local usuario (Usuario): cuenta de usuario utilizada por la operación. Se inicializa con (Usuario) session.getAttribute("usuario").
        Usuario usuario =
                (Usuario) session.getAttribute("usuario");

        // [Doc] Evalúa usuario == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (usuario == null) {
            // [Doc] Termina el método o la función y devuelve null.
            return null;
        }

        // [Doc] Evalúa identidad.esAdmin(usuario). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (identidad.esAdmin(usuario)) {
            // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
            return "ADMIN";
        }

        // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
        return "USUARIO_" + usuario.getId();
    }

    // ======================================================
    // RESPUESTA CUANDO NO EXISTE SESIÓN
    // ======================================================

    /** [Doc]
     * Construye la respuesta de error y redirección para una sesión que ya no permite consultar avisos.
     * @return Map&lt;String, Object&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private Map<String, Object> respuestaSesionVencida() {
        // [Doc] Termina el método o la función y devuelve Map.of( "ok", false, "redirect", "/auth/login", "error", "La sesión venció. Iniciá sesión nuevamente." ).
        return Map.of(
                "ok", false,
                "redirect", "/auth/login",
                "error", "La sesión venció. Iniciá sesión nuevamente."
        );
    }
}
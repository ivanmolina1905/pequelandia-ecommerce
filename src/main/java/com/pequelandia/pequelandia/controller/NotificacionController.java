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

@RestController
@RequestMapping("/notificaciones")
public class NotificacionController {

    private final INotificacionService notificacionService;

    public NotificacionController(
            INotificacionService notificacionService
    ) {
        this.notificacionService = notificacionService;
    }

    // ======================================================
    // LISTAR NOTIFICACIONES DEL USUARIO LOGUEADO
    // ======================================================

    @GetMapping("/mias")
    public Map<String, Object> listar(HttpSession session) {

        String destinatario = obtenerDestinatario(session);

        if (destinatario == null) {
            return respuestaSesionVencida();
        }

        try {
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

    @PostMapping("/{id}/leer")
    public Map<String, Object> marcarComoLeida(
            @PathVariable Long id,
            HttpSession session
    ) {
        String destinatario = obtenerDestinatario(session);

        if (destinatario == null) {
            return respuestaSesionVencida();
        }

        try {
            notificacionService.marcarComoLeida(
                    id,
                    destinatario
            );

            return Map.of("ok", true);

        } catch (IllegalArgumentException e) {
            return Map.of(
                    "ok", false,
                    "error", e.getMessage()
            );

        } catch (Exception e) {
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

    @PostMapping("/leer-todas")
    public Map<String, Object> marcarTodas(
            HttpSession session
    ) {
        String destinatario = obtenerDestinatario(session);

        if (destinatario == null) {
            return respuestaSesionVencida();
        }

        try {
            notificacionService.marcarTodasComoLeidas(
                    destinatario
            );

            return Map.of("ok", true);

        } catch (Exception e) {
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

    private String obtenerDestinatario(
            HttpSession session
    ) {
        Usuario usuario =
                (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return null;
        }

        if ("admin".equalsIgnoreCase(
                usuario.getNombre_usuario()
        )) {
            return "ADMIN";
        }

        return "USUARIO_" + usuario.getId();
    }

    // ======================================================
    // RESPUESTA CUANDO NO EXISTE SESIÓN
    // ======================================================

    private Map<String, Object> respuestaSesionVencida() {
        return Map.of(
                "ok", false,
                "redirect", "/auth/login",
                "error", "La sesión venció. Iniciá sesión nuevamente."
        );
    }
}
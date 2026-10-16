package com.pequelandia.pequelandia.service;

import java.util.List;

import com.pequelandia.pequelandia.dto.NotificacionDTO;
import com.pequelandia.pequelandia.model.Pedido;
import com.pequelandia.pequelandia.model.Usuario;

public interface INotificacionService {

    // ======================================================
    // EVENTOS DEL SISTEMA
    // ======================================================

    /**
     * Guarda una notificación interna para el administrador
     * y envía un correo cuando se registra una cuenta nueva.
     */
    void notificarNuevoUsuario(Usuario usuario);

    /**
     * Guarda notificaciones para el administrador y el usuario.
     * También envía los correos de confirmación del pedido.
     */
    void notificarNuevoPedido(Pedido pedido);

    /**
     * Notifica al administrador cuando un usuario solicita
     * cancelar un pedido.
     */
    void notificarSolicitudCancelacion(Pedido pedido);

    /**
     * Notifica al administrador y al usuario cuando cambia
     * realmente el estado de un pedido.
     */
    void notificarCambioEstado(
            Pedido pedido,
            String estadoAnterior
    );

    // ======================================================
    // CAMPANITA
    // ======================================================

    /**
     * Devuelve las últimas notificaciones del destinatario.
     *
     * Ejemplos:
     * ADMIN
     * USUARIO_15
     */
    List<NotificacionDTO> listarUltimas(
            String destinatario
    );

    /**
     * Cuenta solamente las notificaciones todavía no leídas.
     */
    long contarNoLeidas(
            String destinatario
    );

    /**
     * Marca una notificación individual como leída.
     */
    void marcarComoLeida(
            Long notificacionId,
            String destinatario
    );

    /**
     * Marca todas las notificaciones del destinatario como leídas.
     */
    void marcarTodasComoLeidas(
            String destinatario
    );
}
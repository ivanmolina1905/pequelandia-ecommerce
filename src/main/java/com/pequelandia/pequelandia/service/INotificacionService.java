package com.pequelandia.pequelandia.service;

import java.util.List;

import com.pequelandia.pequelandia.dto.NotificacionDTO;
import com.pequelandia.pequelandia.model.Pedido;
import com.pequelandia.pequelandia.model.Usuario;

/** [Doc]
 * Define el contrato de operaciones de notificacion que implementa el servicio correspondiente.
 */
public interface INotificacionService {

    // ======================================================
    // EVENTOS DEL SISTEMA
    // ======================================================

    /**
     * Guarda una notificación interna para el administrador
     * y envía un correo cuando se registra una cuenta nueva.
     */
    /** [Doc]
     * Genera avisos de nuevo usuario en el ámbito de INotificacionService.
     * @param usuario cuenta de usuario utilizada por la operación.
     */
    void notificarNuevoUsuario(Usuario usuario);

    /**
     * Guarda notificaciones para el administrador y el usuario.
     * También envía los correos de confirmación del pedido.
     */
    /** [Doc]
     * Genera avisos de nuevo pedido en el ámbito de INotificacionService.
     * @param pedido pedido procesado en este punto.
     */
    void notificarNuevoPedido(Pedido pedido);

    /**
     * Notifica al administrador cuando un usuario solicita
     * cancelar un pedido.
     */
    /** [Doc]
     * Genera avisos de solicitud cancelacion en el ámbito de INotificacionService.
     * @param pedido pedido procesado en este punto.
     */
    void notificarSolicitudCancelacion(Pedido pedido);

    /**
     * Notifica al administrador y al usuario cuando cambia
     * realmente el estado de un pedido.
     */
    /** [Doc]
     * Genera avisos de cambio estado en el ámbito de INotificacionService.
     * @param pedido pedido procesado en este punto.
     * @param estadoAnterior estado anterior.
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
    /** [Doc]
     * Consulta hasta doce avisos del destinatario y los transforma a DTO con su estado de lectura.
     * @param destinatario identidad o dirección que recibirá el aviso o correo.
     * @return List&lt;NotificacionDTO&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    List<NotificacionDTO> listarUltimas(
            String destinatario
    );

    /**
     * Cuenta solamente las notificaciones todavía no leídas.
     */
    /** [Doc]
     * Cuenta no leidas en el ámbito de INotificacionService.
     * @param destinatario identidad o dirección que recibirá el aviso o correo.
     * @return long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    long contarNoLeidas(
            String destinatario
    );

    /**
     * Marca una notificación individual como leída.
     */
    /** [Doc]
     * Exige que el destinatario coincida con el aviso e inserta su marca de lectura.
     * @param notificacionId notificacion id.
     * @param destinatario identidad o dirección que recibirá el aviso o correo.
     */
    void marcarComoLeida(
            Long notificacionId,
            String destinatario
    );

    /**
     * Marca todas las notificaciones del destinatario como leídas.
     */
    /** [Doc]
     * Registra todas como leidas en el ámbito de INotificacionService.
     * @param destinatario identidad o dirección que recibirá el aviso o correo.
     */
    void marcarTodasComoLeidas(
            String destinatario
    );
}
package com.pequelandia.pequelandia.service;

import com.pequelandia.pequelandia.model.Pedido;
import com.pequelandia.pequelandia.model.Usuario;

/** [Doc]
 * Define el contrato de operaciones de correo que implementa el servicio correspondiente.
 */
public interface ICorreoService {

    /** [Doc]
     * Compone y encola o delega el envío de los datos de la operación en el ámbito de ICorreoService.
     * @param destinatario identidad o dirección que recibirá el aviso o correo.
     * @param asunto asunto.
     * @param contenido contenido.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    boolean enviar(
            String destinatario,
            String asunto,
            String contenido
    );

    /** [Doc]
     * Compone y encola o delega el envío de al admin en el ámbito de ICorreoService.
     * @param asunto asunto.
     * @param contenido contenido.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    boolean enviarAlAdmin(
            String asunto,
            String contenido
    );

    /** [Doc]
     * Compone y encola o delega el envío de bienvenida en el ámbito de ICorreoService.
     * @param usuario cuenta de usuario utilizada por la operación.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    boolean enviarBienvenida(
            Usuario usuario
    );

    /** [Doc]
     * Compone y encola o delega el envío de nuevo usuario al admin en el ámbito de ICorreoService.
     * @param usuario cuenta de usuario utilizada por la operación.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    boolean enviarNuevoUsuarioAlAdmin(
            Usuario usuario
    );

    /** [Doc]
     * Compone y encola o delega el envío de nuevo pedido al admin en el ámbito de ICorreoService.
     * @param pedido pedido procesado en este punto.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    boolean enviarNuevoPedidoAlAdmin(
            Pedido pedido
    );

    /** [Doc]
     * Compone y encola o delega el envío de confirmacion pedido al usuario en el ámbito de ICorreoService.
     * @param pedido pedido procesado en este punto.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    boolean enviarConfirmacionPedidoAlUsuario(
            Pedido pedido
    );

    /** [Doc]
     * Compone y encola o delega el envío de cambio estado al usuario en el ámbito de ICorreoService.
     * @param pedido pedido procesado en este punto.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    boolean enviarCambioEstadoAlUsuario(
            Pedido pedido
    );

    /** [Doc]
     * Compone y encola o delega el envío de cambio estado al admin en el ámbito de ICorreoService.
     * @param pedido pedido procesado en este punto.
     * @param estadoAnterior estado anterior.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    boolean enviarCambioEstadoAlAdmin(
            Pedido pedido,
            String estadoAnterior
    );

    /** [Doc]
     * Compone y encola o delega el envío de solicitud cancelacion al admin en el ámbito de ICorreoService.
     * @param pedido pedido procesado en este punto.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    boolean enviarSolicitudCancelacionAlAdmin(
            Pedido pedido
    );
}
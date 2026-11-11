package com.pequelandia.pequelandia.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.pequelandia.pequelandia.model.Pedido;
import com.pequelandia.pequelandia.model.Usuario;

/** [Doc]
 * Compone los mensajes de bienvenida, pedidos y cancelaciones y delega su almacenamiento en la cola.
 */
@Service
@org.springframework.transaction.annotation.Transactional
public class CorreoService implements ICorreoService {

    // [Doc] Atributo log (Logger): registrador de eventos de la clase. Se inicializa con LoggerFactory.getLogger(CorreoService.class). Su referencia no se reasigna después de inicializarse.
    private static final Logger log =
            LoggerFactory.getLogger(CorreoService.class);

    // [Doc] Atributo cola (ColaCorreoService): colaborador que concentra operaciones de cola correo. Su referencia no se reasigna después de inicializarse.
    private final ColaCorreoService cola;
    // [Doc] Atributo correoAdmin (String): correo admin.
    @Value("${app.admin.email}") private String correoAdmin;
    /** [Doc]
     * Construye CorreoService y conserva sus dependencias o valores iniciales.
     * @param cola colaborador que concentra operaciones de cola correo.
     */
    public CorreoService(ColaCorreoService cola) { this.cola = cola; }

    // ======================================================
    // ENVÍO BASE
    // ======================================================

    /** [Doc]
     * Compone y encola o delega el envío de los datos de la operación en el ámbito de CorreoService.
     * @param destinatario identidad o dirección que recibirá el aviso o correo.
     * @param asunto asunto.
     * @param contenido contenido.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public boolean enviar(
            String destinatario,
            String asunto,
            String contenido
    ) {
        // [Doc] Evalúa destinatario == null || destinatario.isBlank(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (
                destinatario == null
                        || destinatario.isBlank()
        ) {
            // [Doc] Ejecuta la llamada o modificación log.warn( "No se envió el correo: destinatario vacío" ); Sus efectos dependen del método invocado y del objeto receptor.
            log.warn(
                    "No se envió el correo: destinatario vacío"
            );

            // [Doc] Termina el método o la función y devuelve false.
            return false;
        }

        // [Doc] Ejecuta la llamada o modificación cola.encolar(destinatario, asunto, contenido); Sus efectos dependen del método invocado y del objeto receptor.
        cola.encolar(destinatario, asunto, contenido);
        // [Doc] Termina el método o la función y devuelve true.
        return true; // Aceptado en cola; la entrega SMTP se confirma por separado.
    }

    // ======================================================
    // ENVÍO AL ADMINISTRADOR
    // ======================================================

    /** [Doc]
     * Compone y encola o delega el envío de al admin en el ámbito de CorreoService.
     * @param asunto asunto.
     * @param contenido contenido.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public boolean enviarAlAdmin(
            String asunto,
            String contenido
    ) {
        // [Doc] Termina el método o la función y devuelve enviar( correoAdmin, asunto, contenido ).
        return enviar(
                correoAdmin,
                asunto,
                contenido
        );
    }

    // ======================================================
    // BIENVENIDA AL NUEVO USUARIO
    // ======================================================

    /** [Doc]
     * Compone y encola o delega el envío de bienvenida en el ámbito de CorreoService.
     * @param usuario cuenta de usuario utilizada por la operación.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public boolean enviarBienvenida(
            Usuario usuario
    ) {
        // [Doc] Variable local contenido (String): contenido. Se inicializa con """ Hola %s: ¡Bienvenido a Pequelandia! Tu cuenta fue creada correctamente. Tu nombre de usuario es: %s Por....
        String contenido = """
                Hola %s:

                ¡Bienvenido a Pequelandia!

                Tu cuenta fue creada correctamente.

                Tu nombre de usuario es: %s

                Por seguridad, no incluimos tu contraseña en este correo.
                La contraseña es la que elegiste al registrarte.

                Ya podés iniciar sesión y comenzar a comprar.

                Equipo Pequelandia
                """.formatted(
                        usuario.getNombre_usuario(),
                        usuario.getNombre_usuario()
                );

        // [Doc] Termina el método o la función y devuelve enviar( usuario.getEmail(), "¡Bienvenido a Pequelandia!", contenido ).
        return enviar(
                usuario.getEmail(),
                "¡Bienvenido a Pequelandia!",
                contenido
        );
    }

    // ======================================================
    // NUEVO USUARIO -> ADMIN
    // ======================================================

    /** [Doc]
     * Compone y encola o delega el envío de nuevo usuario al admin en el ámbito de CorreoService.
     * @param usuario cuenta de usuario utilizada por la operación.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public boolean enviarNuevoUsuarioAlAdmin(
            Usuario usuario
    ) {
        // [Doc] Variable local contenido (String): contenido. Se inicializa con """ Se registró un nuevo usuario en Pequelandia. Usuario: %s Email: %s DNI: %s Teléfono: %s Podés revisarlo....
        String contenido = """
                Se registró un nuevo usuario en Pequelandia.

                Usuario: %s
                Email: %s
                DNI: %s
                Teléfono: %s

                Podés revisarlo desde el panel de administración.

                Equipo Pequelandia
                """.formatted(
                        usuario.getNombre_usuario(),
                        usuario.getEmail(),
                        usuario.getDni(),
                        usuario.getTelefono() != null
                                ? usuario.getTelefono()
                                : "No informado"
                );

        // [Doc] Termina el método o la función y devuelve enviarAlAdmin( "Nuevo usuario registrado: " + usuario.getNombre_usuario(), contenido ).
        return enviarAlAdmin(
                "Nuevo usuario registrado: "
                        + usuario.getNombre_usuario(),
                contenido
        );
    }

    // ======================================================
    // NUEVO PEDIDO -> ADMIN
    // ======================================================

    /** [Doc]
     * Compone y encola o delega el envío de nuevo pedido al admin en el ámbito de CorreoService.
     * @param pedido pedido procesado en este punto.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public boolean enviarNuevoPedidoAlAdmin(
            Pedido pedido
    ) {
        // [Doc] Variable local usuario (Usuario): cuenta de usuario utilizada por la operación. Se inicializa con pedido.getUsuario().
        Usuario usuario =
                pedido.getUsuario();

        // [Doc] Variable local contenido (String): contenido. Se inicializa con """ Se registró un nuevo pedido en Pequelandia. Pedido: #%s Usuario: %s Email: %s Estado inicial: %s Revisa....
        String contenido = """
                Se registró un nuevo pedido en Pequelandia.

                Pedido: #%s
                Usuario: %s
                Email: %s
                Estado inicial: %s

                Revisalo desde el panel de administración.

                Equipo Pequelandia
                """.formatted(
                        pedido.getId_pedido(),
                        usuario != null
                                ? usuario.getNombre_usuario()
                                : "Sin usuario",
                        usuario != null
                                ? usuario.getEmail()
                                : "Sin email",
                        pedido.getEstado_pedido()
                );

        // [Doc] Termina el método o la función y devuelve enviarAlAdmin( "Nuevo pedido #" + pedido.getId_pedido(), contenido ).
        return enviarAlAdmin(
                "Nuevo pedido #"
                        + pedido.getId_pedido(),
                contenido
        );
    }

    // ======================================================
    // NUEVO PEDIDO -> USUARIO
    // ======================================================

    /** [Doc]
     * Compone y encola o delega el envío de confirmacion pedido al usuario en el ámbito de CorreoService.
     * @param pedido pedido procesado en este punto.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public boolean enviarConfirmacionPedidoAlUsuario(
            Pedido pedido
    ) {
        // [Doc] Variable local usuario (Usuario): cuenta de usuario utilizada por la operación. Se inicializa con pedido.getUsuario().
        Usuario usuario =
                pedido.getUsuario();

        // [Doc] Evalúa usuario == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (usuario == null) {
            // [Doc] Termina el método o la función y devuelve false.
            return false;
        }

        // [Doc] Variable local contenido (String): contenido. Se inicializa con """ Hola %s: Recibimos correctamente tu pedido #%s. Estado actual: %s Podés consultar los detalles desde tu....
        String contenido = """
                Hola %s:

                Recibimos correctamente tu pedido #%s.

                Estado actual: %s

                Podés consultar los detalles desde tu panel de usuario.

                Equipo Pequelandia
                """.formatted(
                        usuario.getNombre_usuario(),
                        pedido.getId_pedido(),
                        pedido.getEstado_pedido()
                );

        // [Doc] Termina el método o la función y devuelve enviar( usuario.getEmail(), "Recibimos tu pedido #" + pedido.getId_pedido(), contenido ).
        return enviar(
                usuario.getEmail(),
                "Recibimos tu pedido #"
                        + pedido.getId_pedido(),
                contenido
        );
    }

    // ======================================================
    // CAMBIO DE ESTADO -> USUARIO
    // ======================================================

    /** [Doc]
     * Compone y encola o delega el envío de cambio estado al usuario en el ámbito de CorreoService.
     * @param pedido pedido procesado en este punto.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public boolean enviarCambioEstadoAlUsuario(
            Pedido pedido
    ) {
        // [Doc] Variable local usuario (Usuario): cuenta de usuario utilizada por la operación. Se inicializa con pedido.getUsuario().
        Usuario usuario =
                pedido.getUsuario();

        // [Doc] Evalúa usuario == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (usuario == null) {
            // [Doc] Termina el método o la función y devuelve false.
            return false;
        }

        // [Doc] Variable local contenido (String): contenido. Se inicializa con """ Hola %s: El estado de tu pedido #%s fue actualizado. Nuevo estado: %s Podés consultar el detalle desde ....
        String contenido = """
                Hola %s:

                El estado de tu pedido #%s fue actualizado.

                Nuevo estado: %s

                Podés consultar el detalle desde tu panel de usuario.

                Equipo Pequelandia
                """.formatted(
                        usuario.getNombre_usuario(),
                        pedido.getId_pedido(),
                        pedido.getEstado_pedido()
                );

        // [Doc] Termina el método o la función y devuelve enviar( usuario.getEmail(), "Actualización del pedido #" + pedido.getId_pedido(), contenido ).
        return enviar(
                usuario.getEmail(),
                "Actualización del pedido #"
                        + pedido.getId_pedido(),
                contenido
        );
    }

    // ======================================================
    // CAMBIO DE ESTADO -> ADMIN
    // ======================================================

    /** [Doc]
     * Compone y encola o delega el envío de cambio estado al admin en el ámbito de CorreoService.
     * @param pedido pedido procesado en este punto.
     * @param estadoAnterior estado anterior.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public boolean enviarCambioEstadoAlAdmin(
            Pedido pedido,
            String estadoAnterior
    ) {
        // [Doc] Variable local contenido (String): contenido. Se inicializa con """ Se actualizó el estado de un pedido. Pedido: #%s Estado anterior: %s Nuevo estado: %s Equipo Pequelandi....
        String contenido = """
                Se actualizó el estado de un pedido.

                Pedido: #%s
                Estado anterior: %s
                Nuevo estado: %s

                Equipo Pequelandia
                """.formatted(
                        pedido.getId_pedido(),
                        estadoAnterior,
                        pedido.getEstado_pedido()
                );

        // [Doc] Termina el método o la función y devuelve enviarAlAdmin( "Cambio de estado del pedido #" + pedido.getId_pedido(), contenido ).
        return enviarAlAdmin(
                "Cambio de estado del pedido #"
                        + pedido.getId_pedido(),
                contenido
        );
    }

    // ======================================================
    // SOLICITUD DE CANCELACIÓN -> ADMIN
    // ======================================================

    /** [Doc]
     * Compone y encola o delega el envío de solicitud cancelacion al admin en el ámbito de CorreoService.
     * @param pedido pedido procesado en este punto.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public boolean enviarSolicitudCancelacionAlAdmin(
            Pedido pedido
    ) {
        // [Doc] Variable local contenido (String): contenido. Se inicializa con """ Un usuario solicitó cancelar un pedido. Pedido: #%s Estado actual: %s Usuario: %s Revisalo desde Gestió....
        String contenido = """
                Un usuario solicitó cancelar un pedido.

                Pedido: #%s
                Estado actual: %s
                Usuario: %s

                Revisalo desde Gestión de Pedidos.

                Equipo Pequelandia
                """.formatted(
                        pedido.getId_pedido(),
                        pedido.getEstado_pedido(),
                        pedido.getUsuario() != null
                                ? pedido
                                        .getUsuario()
                                        .getNombre_usuario()
                                : "Sin usuario"
                );

        // [Doc] Termina el método o la función y devuelve enviarAlAdmin( "Solicitud de cancelación del pedido #" + pedido.getId_pedido(), contenido ).
        return enviarAlAdmin(
                "Solicitud de cancelación del pedido #"
                        + pedido.getId_pedido(),
                contenido
        );
    }
}
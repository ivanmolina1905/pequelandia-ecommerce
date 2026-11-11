package com.pequelandia.pequelandia.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pequelandia.pequelandia.dto.NotificacionDTO;
import com.pequelandia.pequelandia.model.Notificacion;
import com.pequelandia.pequelandia.model.NotificacionLectura;
import com.pequelandia.pequelandia.model.Pedido;
import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.repository.INotificacionLecturaRepository;
import com.pequelandia.pequelandia.repository.INotificacionRepository;

/** [Doc]
 * Crea avisos por destinatario, encola correos y administra las marcas de lectura.
 */
@Service
@Transactional
public class NotificacionService implements INotificacionService {

    // [Doc] Atributo notificacionRepo (INotificacionRepository): acceso de persistencia a notificacion. Su referencia no se reasigna después de inicializarse.
    private final INotificacionRepository notificacionRepo;
    // [Doc] Atributo lecturaRepo (INotificacionLecturaRepository): acceso de persistencia a notificacion lectura. Su referencia no se reasigna después de inicializarse.
    private final INotificacionLecturaRepository lecturaRepo;
    // [Doc] Atributo correoService (CorreoService): colaborador que concentra operaciones de correo. Su referencia no se reasigna después de inicializarse.
    private final CorreoService correoService;

    /** [Doc]
     * Construye NotificacionService y conserva sus dependencias o valores iniciales.
     * @param notificacionRepo acceso de persistencia a notificacion.
     * @param lecturaRepo acceso de persistencia a notificacion lectura.
     * @param correoService colaborador que concentra operaciones de correo.
     */
    public NotificacionService(
            INotificacionRepository notificacionRepo,
            INotificacionLecturaRepository lecturaRepo,
            CorreoService correoService
    ) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.notificacionRepo = notificacionRepo;
        this.notificacionRepo = notificacionRepo;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.lecturaRepo = lecturaRepo;
        this.lecturaRepo = lecturaRepo;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.correoService = correoService;
        this.correoService = correoService;
    }

    // ======================================================
    // DESTINATARIOS
    // ======================================================

    /** [Doc]
     * Construye la identidad lógica USUARIO_ seguida por el ID del usuario.
     * @param usuarioId identificador del usuario.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private String destinatarioUsuario(Long usuarioId) {
        // [Doc] Termina el método o la función y devuelve "USUARIO_" + usuarioId.
        return "USUARIO_" + usuarioId;
    }

    // ======================================================
    // CREACIÓN INTERNA DE NOTIFICACIONES
    // ======================================================

    /** [Doc]
     * Construye un aviso con fecha actual, destinatario, descripción y pedido opcional y lo guarda.
     * @param destinatario identidad o dirección que recibirá el aviso o correo.
     * @param descripcion texto descriptivo de la entidad u operación.
     * @param pedido pedido procesado en este punto.
     * @return Notificacion con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private Notificacion crear(
            String destinatario,
            String descripcion,
            Pedido pedido
    ) {
        // [Doc] Variable local notificacion (Notificacion): aviso que se construye o consulta. Se inicializa con new Notificacion().
        Notificacion notificacion = new Notificacion();

        // [Doc] Actualiza una propiedad o configuración mediante la llamada: notificacion.setFecha_notificacion( LocalDateTime.now() );
        notificacion.setFecha_notificacion(
                LocalDateTime.now()
        );

        // [Doc] Actualiza una propiedad o configuración mediante la llamada: notificacion.setDestinatario( destinatario );
        notificacion.setDestinatario(
                destinatario
        );

        // [Doc] Actualiza una propiedad o configuración mediante la llamada: notificacion.setDescripcion( descripcion );
        notificacion.setDescripcion(
                descripcion
        );

        /*
         * Puede quedar null cuando el aviso no corresponde
         * a un pedido, por ejemplo al crear una cuenta.
         */
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: notificacion.setPedido( pedido );
        notificacion.setPedido(
                pedido
        );

        // [Doc] Termina el método o la función y devuelve notificacionRepo.save( notificacion ).
        return notificacionRepo.save(
                notificacion
        );
    }

    // ======================================================
    // NUEVO USUARIO
    // ======================================================

    /** [Doc]
     * Genera avisos de nuevo usuario en el ámbito de NotificacionService.
     * @param usuario cuenta de usuario utilizada por la operación.
     */
    @Override
    public void notificarNuevoUsuario(
            Usuario usuario
    ) {
        // [Doc] Ejecuta la llamada o modificación crear( "ADMIN", "Nuevo usuario registrado: " + usuario.getNombre_usuario(), null ); Sus efectos dependen del método invocado y del objeto receptor.
        crear(
                "ADMIN",
                "Nuevo usuario registrado: "
                        + usuario.getNombre_usuario(),
                null
        );

        // [Doc] Ejecuta la llamada o modificación correoService.enviarNuevoUsuarioAlAdmin( usuario ); Sus efectos dependen del método invocado y del objeto receptor.
        correoService.enviarNuevoUsuarioAlAdmin(
                usuario
        );
    }

    // ======================================================
    // NUEVO PEDIDO
    // ======================================================

    /** [Doc]
     * Genera avisos de nuevo pedido en el ámbito de NotificacionService.
     * @param pedido pedido procesado en este punto.
     */
    @Override
    public void notificarNuevoPedido(
            Pedido pedido
    ) {
        // [Doc] Ejecuta la llamada o modificación crear( "ADMIN", "Nuevo pedido #" + pedido.getId_pedido(), pedido ); Sus efectos dependen del método invocado y del objeto receptor.
        crear(
                "ADMIN",
                "Nuevo pedido #"
                        + pedido.getId_pedido(),
                pedido
        );

        // [Doc] Evalúa pedido.getUsuario() != null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (pedido.getUsuario() != null) {
            // [Doc] Ejecuta la llamada o modificación crear( destinatarioUsuario( pedido.getUsuario().getId() ), "Recibimos tu pedido #" + pedido.getId_pedido(), pedido ); Sus efectos dependen del método invocado y del objeto receptor.
            crear(
                    destinatarioUsuario(
                            pedido.getUsuario().getId()
                    ),
                    "Recibimos tu pedido #"
                            + pedido.getId_pedido(),
                    pedido
            );
        }

        // [Doc] Ejecuta la llamada o modificación correoService.enviarNuevoPedidoAlAdmin( pedido ); Sus efectos dependen del método invocado y del objeto receptor.
        correoService.enviarNuevoPedidoAlAdmin(
                pedido
        );

        // [Doc] Ejecuta la llamada o modificación correoService.enviarConfirmacionPedidoAlUsuario( pedido ); Sus efectos dependen del método invocado y del objeto receptor.
        correoService.enviarConfirmacionPedidoAlUsuario(
                pedido
        );
    }

    // ======================================================
    // SOLICITUD DE CANCELACIÓN
    // ======================================================

    /** [Doc]
     * Genera avisos de solicitud cancelacion en el ámbito de NotificacionService.
     * @param pedido pedido procesado en este punto.
     */
    @Override
    public void notificarSolicitudCancelacion(
            Pedido pedido
    ) {
        // [Doc] Ejecuta la llamada o modificación crear( "ADMIN", "Solicitud de cancelación del pedido #" + pedido.getId_pedido(), pedido ); Sus efectos dependen del método invocado y del objeto receptor.
        crear(
                "ADMIN",
                "Solicitud de cancelación del pedido #"
                        + pedido.getId_pedido(),
                pedido
        );

        // [Doc] Ejecuta la llamada o modificación correoService.enviarSolicitudCancelacionAlAdmin( pedido ); Sus efectos dependen del método invocado y del objeto receptor.
        correoService.enviarSolicitudCancelacionAlAdmin(
                pedido
        );
    }

    // ======================================================
    // CAMBIO DE ESTADO
    // ======================================================

    /** [Doc]
     * Genera avisos de cambio estado en el ámbito de NotificacionService.
     * @param pedido pedido procesado en este punto.
     * @param estadoAnterior estado anterior.
     */
    @Override
    public void notificarCambioEstado(
            Pedido pedido,
            String estadoAnterior
    ) {
        // [Doc] Variable local nuevoEstado (String): nuevo estado. Se inicializa con pedido.getEstado_pedido().
        String nuevoEstado =
                pedido.getEstado_pedido();

        /*
         * Evita generar correos y avisos duplicados
         * si el admin guarda el mismo estado.
         */
        // [Doc] Evalúa Objects.equals( normalizarEstado(estadoAnterior), normalizarEstado(nuevoEstado) ). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (Objects.equals(
                normalizarEstado(estadoAnterior),
                normalizarEstado(nuevoEstado)
        )) {
            // [Doc] Termina este método sin devolver un valor.
            return;
        }

        // [Doc] Variable local descripcionAdmin (String): descripcion admin. Se inicializa con "Pedido #" + pedido.getId_pedido() + ": " + estadoAnterior + " → " + nuevoEstado.
        String descripcionAdmin =
                "Pedido #" + pedido.getId_pedido()
                        + ": " + estadoAnterior
                        + " → " + nuevoEstado;

        // [Doc] Ejecuta la llamada o modificación crear( "ADMIN", descripcionAdmin, pedido ); Sus efectos dependen del método invocado y del objeto receptor.
        crear(
                "ADMIN",
                descripcionAdmin,
                pedido
        );

        // [Doc] Evalúa pedido.getUsuario() != null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (pedido.getUsuario() != null) {
            // [Doc] Ejecuta la llamada o modificación crear( destinatarioUsuario( pedido.getUsuario().getId() ), "Tu pedido #" + pedido.getId_pedido() + " cambió a " + nuevoEstado, pedido ); Sus efectos dependen del método invocado y del objeto receptor.
            crear(
                    destinatarioUsuario(
                            pedido.getUsuario().getId()
                    ),
                    "Tu pedido #"
                            + pedido.getId_pedido()
                            + " cambió a "
                            + nuevoEstado,
                    pedido
            );
        }

        // [Doc] Ejecuta la llamada o modificación correoService.enviarCambioEstadoAlAdmin( pedido, estadoAnterior ); Sus efectos dependen del método invocado y del objeto receptor.
        correoService.enviarCambioEstadoAlAdmin(
                pedido,
                estadoAnterior
        );

        // [Doc] Ejecuta la llamada o modificación correoService.enviarCambioEstadoAlUsuario( pedido ); Sus efectos dependen del método invocado y del objeto receptor.
        correoService.enviarCambioEstadoAlUsuario(
                pedido
        );
    }

    /** [Doc]
     * Normaliza estado en el ámbito de NotificacionService.
     * @param estado estado usado por la operación.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private String normalizarEstado(
            String estado
    ) {
        // [Doc] Termina el método o la función y devuelve estado == null ? "" : estado.trim().toUpperCase().
        return estado == null
                ? ""
                : estado.trim().toUpperCase();
    }

    // ======================================================
    // LISTAR NOTIFICACIONES PARA LA CAMPANITA
    // ======================================================

    /** [Doc]
     * Consulta hasta doce avisos del destinatario y los transforma a DTO con su estado de lectura.
     * @param destinatario identidad o dirección que recibirá el aviso o correo.
     * @return List&lt;NotificacionDTO&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Transactional(readOnly = true)
    @Override
    public List<NotificacionDTO> listarUltimas(
            String destinatario
    ) {
        // [Doc] Termina el método o la función y devuelve notificacionRepo .listarUltimas( destinatario, PageRequest.of(0, 12) ) .stream() .map(notificacion -> new NotificacionDTO( notificacion .getId_notificacion()....
        return notificacionRepo
                .listarUltimas(
                        destinatario,
                        PageRequest.of(0, 12)
                )
                .stream()
                .map(notificacion ->
                        new NotificacionDTO(
                                notificacion
                                        .getId_notificacion(),

                                notificacion
                                        .getDescripcion(),

                                notificacion
                                        .getFecha_notificacion(),

                                notificacion.getPedido() != null
                                        ? notificacion
                                                .getPedido()
                                                .getId_pedido()
                                        : null,

                                lecturaRepo
                                        .buscarLectura(
                                                notificacion
                                                        .getId_notificacion(),
                                                destinatario
                                        )
                                        .isPresent()
                        )
                )
                .toList();
    }

    // ======================================================
    // CONTADOR DE NO LEÍDAS
    // ======================================================

    /** [Doc]
     * Cuenta no leidas en el ámbito de NotificacionService.
     * @param destinatario identidad o dirección que recibirá el aviso o correo.
     * @return long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public long contarNoLeidas(
            String destinatario
    ) {
        // [Doc] Termina el método o la función y devuelve notificacionRepo .contarNoLeidas( destinatario ).
        return notificacionRepo
                .contarNoLeidas(
                        destinatario
                );
    }

    // ======================================================
    // MARCAR UNA COMO LEÍDA
    // ======================================================

    /** [Doc]
     * Exige que el destinatario coincida con el aviso e inserta su marca de lectura.
     * @param notificacionId notificacion id.
     * @param destinatario identidad o dirección que recibirá el aviso o correo.
     */
    @Transactional
    @Override
    public void marcarComoLeida(
            Long notificacionId,
            String destinatario
    ) {
        // [Doc] Variable local notificacion (Notificacion): aviso que se construye o consulta. Se inicializa con notificacionRepo .findById(notificacionId) .orElseThrow( () -> new IllegalArgumentException( "Notificación ....
        Notificacion notificacion =
                notificacionRepo
                        .findById(notificacionId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Notificación no encontrada."
                                        )
                        );

        /*
         * Seguridad: un usuario no puede marcar como leída
         * una notificación de otro usuario o del admin.
         */
        // [Doc] Evalúa !destinatario.equals( notificacion.getDestinatario() ). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (!destinatario.equals(
                notificacion.getDestinatario()
        )) {
            // [Doc] Interrumpe el flujo lanzando new IllegalArgumentException( "No autorizado." ). El llamador o el manejador de errores decide la respuesta.
            throw new IllegalArgumentException(
                    "No autorizado."
            );
        }

        // [Doc] Ejecuta la llamada o modificación lecturaRepo.insertarLectura(notificacionId, destinatario); Sus efectos dependen del método invocado y del objeto receptor.
        lecturaRepo.insertarLectura(notificacionId, destinatario);
    }

    // ======================================================
    // MARCAR TODAS COMO LEÍDAS
    // ======================================================

    /** [Doc]
     * Registra todas como leidas en el ámbito de NotificacionService.
     * @param destinatario identidad o dirección que recibirá el aviso o correo.
     */
    @Transactional
    @Override
    public void marcarTodasComoLeidas(
            String destinatario
    ) {
        // [Doc] Ejecuta la llamada o modificación lecturaRepo.marcarTodasComoLeidas( destinatario ); Sus efectos dependen del método invocado y del objeto receptor.
        lecturaRepo.marcarTodasComoLeidas(
                destinatario
        );
    }
}
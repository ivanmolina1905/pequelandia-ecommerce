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

@Service
public class NotificacionService implements INotificacionService {

    private final INotificacionRepository notificacionRepo;
    private final INotificacionLecturaRepository lecturaRepo;
    private final CorreoService correoService;

    public NotificacionService(
            INotificacionRepository notificacionRepo,
            INotificacionLecturaRepository lecturaRepo,
            CorreoService correoService
    ) {
        this.notificacionRepo = notificacionRepo;
        this.lecturaRepo = lecturaRepo;
        this.correoService = correoService;
    }

    // ======================================================
    // DESTINATARIOS
    // ======================================================

    private String destinatarioUsuario(Long usuarioId) {
        return "USUARIO_" + usuarioId;
    }

    // ======================================================
    // CREACIÓN INTERNA DE NOTIFICACIONES
    // ======================================================

    private Notificacion crear(
            String destinatario,
            String descripcion,
            Pedido pedido
    ) {
        Notificacion notificacion = new Notificacion();

        notificacion.setFecha_notificacion(
                LocalDateTime.now()
        );

        notificacion.setDestinatario(
                destinatario
        );

        notificacion.setDescripcion(
                descripcion
        );

        /*
         * Puede quedar null cuando el aviso no corresponde
         * a un pedido, por ejemplo al crear una cuenta.
         */
        notificacion.setPedido(
                pedido
        );

        return notificacionRepo.save(
                notificacion
        );
    }

    // ======================================================
    // NUEVO USUARIO
    // ======================================================

    @Override
    public void notificarNuevoUsuario(
            Usuario usuario
    ) {
        crear(
                "ADMIN",
                "Nuevo usuario registrado: "
                        + usuario.getNombre_usuario(),
                null
        );

        correoService.enviarNuevoUsuarioAlAdmin(
                usuario
        );
    }

    // ======================================================
    // NUEVO PEDIDO
    // ======================================================

    @Override
    public void notificarNuevoPedido(
            Pedido pedido
    ) {
        crear(
                "ADMIN",
                "Nuevo pedido #"
                        + pedido.getId_pedido(),
                pedido
        );

        if (pedido.getUsuario() != null) {
            crear(
                    destinatarioUsuario(
                            pedido.getUsuario().getId()
                    ),
                    "Recibimos tu pedido #"
                            + pedido.getId_pedido(),
                    pedido
            );
        }

        correoService.enviarNuevoPedidoAlAdmin(
                pedido
        );

        correoService.enviarConfirmacionPedidoAlUsuario(
                pedido
        );
    }

    // ======================================================
    // SOLICITUD DE CANCELACIÓN
    // ======================================================

    @Override
    public void notificarSolicitudCancelacion(
            Pedido pedido
    ) {
        crear(
                "ADMIN",
                "Solicitud de cancelación del pedido #"
                        + pedido.getId_pedido(),
                pedido
        );

        correoService.enviarSolicitudCancelacionAlAdmin(
                pedido
        );
    }

    // ======================================================
    // CAMBIO DE ESTADO
    // ======================================================

    @Override
    public void notificarCambioEstado(
            Pedido pedido,
            String estadoAnterior
    ) {
        String nuevoEstado =
                pedido.getEstado_pedido();

        /*
         * Evita generar correos y avisos duplicados
         * si el admin guarda el mismo estado.
         */
        if (Objects.equals(
                normalizarEstado(estadoAnterior),
                normalizarEstado(nuevoEstado)
        )) {
            return;
        }

        String descripcionAdmin =
                "Pedido #" + pedido.getId_pedido()
                        + ": " + estadoAnterior
                        + " → " + nuevoEstado;

        crear(
                "ADMIN",
                descripcionAdmin,
                pedido
        );

        if (pedido.getUsuario() != null) {
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

        correoService.enviarCambioEstadoAlAdmin(
                pedido,
                estadoAnterior
        );

        correoService.enviarCambioEstadoAlUsuario(
                pedido
        );
    }

    private String normalizarEstado(
            String estado
    ) {
        return estado == null
                ? ""
                : estado.trim().toUpperCase();
    }

    // ======================================================
    // LISTAR NOTIFICACIONES PARA LA CAMPANITA
    // ======================================================

    @Transactional(readOnly = true)
    @Override
    public List<NotificacionDTO> listarUltimas(
            String destinatario
    ) {
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

    @Override
    public long contarNoLeidas(
            String destinatario
    ) {
        return notificacionRepo
                .contarNoLeidas(
                        destinatario
                );
    }

    // ======================================================
    // MARCAR UNA COMO LEÍDA
    // ======================================================

    @Transactional
    @Override
    public void marcarComoLeida(
            Long notificacionId,
            String destinatario
    ) {
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
        if (!destinatario.equals(
                notificacion.getDestinatario()
        )) {
            throw new IllegalArgumentException(
                    "No autorizado."
            );
        }

        boolean yaLeida =
                lecturaRepo
                        .buscarLectura(
                                notificacionId,
                                destinatario
                        )
                        .isPresent();

        if (yaLeida) {
            return;
        }

        NotificacionLectura lectura =
                new NotificacionLectura();

        lectura.setNotificacion(
                notificacion
        );

        lectura.setDestinatario(
                destinatario
        );

        lectura.setFechaLectura(
                LocalDateTime.now()
        );

        lecturaRepo.save(
                lectura
        );
    }

    // ======================================================
    // MARCAR TODAS COMO LEÍDAS
    // ======================================================

    @Transactional
    @Override
    public void marcarTodasComoLeidas(
            String destinatario
    ) {
        lecturaRepo.marcarTodasComoLeidas(
                destinatario
        );
    }
}
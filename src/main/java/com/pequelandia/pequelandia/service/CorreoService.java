package com.pequelandia.pequelandia.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.pequelandia.pequelandia.model.Pedido;
import com.pequelandia.pequelandia.model.Usuario;

@Service
public class CorreoService implements ICorreoService {

    private static final Logger log =
            LoggerFactory.getLogger(CorreoService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String correoEmisor;

    @Value("${app.admin.email}")
    private String correoAdmin;

    public CorreoService(
            JavaMailSender mailSender
    ) {
        this.mailSender = mailSender;
    }

    // ======================================================
    // ENVÍO BASE
    // ======================================================

    @Override
    public boolean enviar(
            String destinatario,
            String asunto,
            String contenido
    ) {
        if (
                destinatario == null
                        || destinatario.isBlank()
        ) {
            log.warn(
                    "No se envió el correo: destinatario vacío"
            );

            return false;
        }

        try {
            log.info(
                    "Intentando enviar correo a: {} | asunto: {}",
                    destinatario,
                    asunto
            );

            SimpleMailMessage mensaje =
                    new SimpleMailMessage();

            mensaje.setFrom(correoEmisor);
            mensaje.setTo(destinatario);
            mensaje.setSubject(asunto);
            mensaje.setText(contenido);

            mailSender.send(mensaje);

            log.info(
                    "Correo enviado correctamente a: {}",
                    destinatario
            );

            return true;

        } catch (Exception e) {
            /*
             * Un problema SMTP no debe bloquear
             * el registro ni la compra.
             */
            log.error(
                    "No se pudo enviar correo a {}. Tipo: {}. Mensaje: {}",
                    destinatario,
                    e.getClass().getName(),
                    e.getMessage(),
                    e
            );

            return false;
        }
    }

    // ======================================================
    // ENVÍO AL ADMINISTRADOR
    // ======================================================

    @Override
    public boolean enviarAlAdmin(
            String asunto,
            String contenido
    ) {
        return enviar(
                correoAdmin,
                asunto,
                contenido
        );
    }

    // ======================================================
    // BIENVENIDA AL NUEVO USUARIO
    // ======================================================

    @Override
    public boolean enviarBienvenida(
            Usuario usuario
    ) {
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

        return enviar(
                usuario.getEmail(),
                "¡Bienvenido a Pequelandia!",
                contenido
        );
    }

    // ======================================================
    // NUEVO USUARIO -> ADMIN
    // ======================================================

    @Override
    public boolean enviarNuevoUsuarioAlAdmin(
            Usuario usuario
    ) {
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

        return enviarAlAdmin(
                "Nuevo usuario registrado: "
                        + usuario.getNombre_usuario(),
                contenido
        );
    }

    // ======================================================
    // NUEVO PEDIDO -> ADMIN
    // ======================================================

    @Override
    public boolean enviarNuevoPedidoAlAdmin(
            Pedido pedido
    ) {
        Usuario usuario =
                pedido.getUsuario();

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

        return enviarAlAdmin(
                "Nuevo pedido #"
                        + pedido.getId_pedido(),
                contenido
        );
    }

    // ======================================================
    // NUEVO PEDIDO -> USUARIO
    // ======================================================

    @Override
    public boolean enviarConfirmacionPedidoAlUsuario(
            Pedido pedido
    ) {
        Usuario usuario =
                pedido.getUsuario();

        if (usuario == null) {
            return false;
        }

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

    @Override
    public boolean enviarCambioEstadoAlUsuario(
            Pedido pedido
    ) {
        Usuario usuario =
                pedido.getUsuario();

        if (usuario == null) {
            return false;
        }

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

    @Override
    public boolean enviarCambioEstadoAlAdmin(
            Pedido pedido,
            String estadoAnterior
    ) {
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

        return enviarAlAdmin(
                "Cambio de estado del pedido #"
                        + pedido.getId_pedido(),
                contenido
        );
    }

    // ======================================================
    // SOLICITUD DE CANCELACIÓN -> ADMIN
    // ======================================================

    @Override
    public boolean enviarSolicitudCancelacionAlAdmin(
            Pedido pedido
    ) {
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

        return enviarAlAdmin(
                "Solicitud de cancelación del pedido #"
                        + pedido.getId_pedido(),
                contenido
        );
    }
}
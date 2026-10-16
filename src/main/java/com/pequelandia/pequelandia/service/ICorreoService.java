package com.pequelandia.pequelandia.service;

import com.pequelandia.pequelandia.model.Pedido;
import com.pequelandia.pequelandia.model.Usuario;

public interface ICorreoService {

    boolean enviar(
            String destinatario,
            String asunto,
            String contenido
    );

    boolean enviarAlAdmin(
            String asunto,
            String contenido
    );

    boolean enviarBienvenida(
            Usuario usuario
    );

    boolean enviarNuevoUsuarioAlAdmin(
            Usuario usuario
    );

    boolean enviarNuevoPedidoAlAdmin(
            Pedido pedido
    );

    boolean enviarConfirmacionPedidoAlUsuario(
            Pedido pedido
    );

    boolean enviarCambioEstadoAlUsuario(
            Pedido pedido
    );

    boolean enviarCambioEstadoAlAdmin(
            Pedido pedido,
            String estadoAnterior
    );

    boolean enviarSolicitudCancelacionAlAdmin(
            Pedido pedido
    );
}
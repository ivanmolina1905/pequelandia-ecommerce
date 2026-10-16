package com.pequelandia.pequelandia.config;

import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.service.IPedidoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import java.util.Map;

/** Control de acceso del modelo actual de sesiones; no modifica contraseñas. */
@Component
public class AccessInterceptor implements HandlerInterceptor {
    private final IPedidoService pedidos;
    private final com.pequelandia.pequelandia.service.IdentidadService identidad;
    public AccessInterceptor(IPedidoService pedidos, com.pequelandia.pequelandia.service.IdentidadService identidad) {
        this.pedidos = pedidos;
        this.identidad = identidad;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        var session = request.getSession(false);
        Usuario usuario = session == null ? null : (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            rechazar(response, 401, "Iniciá sesión para continuar");
            return false;
        }
        String path = org.springframework.web.util.UrlPathHelper.defaultInstance.getPathWithinApplication(request);
        usuario = identidad.exigirUsuario(session);
        boolean admin = identidad.esAdmin(usuario);
        boolean soloAdmin = path.startsWith("/admin/") || path.equals("/admin")
                || path.startsWith("/usuarios/") || path.equals("/panel/admin")
                || path.startsWith("/paneladministracion") || path.endsWith("/confirmar-cancelacion");
        if (soloAdmin && !admin) {
            rechazar(response, 403, "Acceso reservado a administración");
            return false;
        }
        if (path.startsWith("/pedido/")) {
            var variables = (Map<?, ?>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            if (variables != null && variables.get("id") != null) {
                var pedido = pedidos.findPedido(Long.valueOf(variables.get("id").toString()));
                if (pedido == null) { rechazar(response, 404, "Pedido no encontrado"); return false; }
                if (!admin && (pedido.getUsuario() == null || !java.util.Objects.equals(pedido.getUsuario().getId(), usuario.getId()))) {
                    rechazar(response, 403, "No autorizado"); return false;
                }
            }
        }
        return true;
    }

    private void rechazar(HttpServletResponse response, int status, String mensaje) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        new com.fasterxml.jackson.databind.ObjectMapper().writeValue(response.getWriter(), Map.of(
                "ok", false, "error", mensaje, "redirect", status == 401 ? "/auth/login" : ""));
    }
}

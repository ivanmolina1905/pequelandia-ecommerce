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
    public AccessInterceptor(IPedidoService pedidos) { this.pedidos = pedidos; }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        var session = request.getSession(false);
        Usuario usuario = session == null ? null : (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            response.sendError(401, "Iniciá sesión para continuar");
            return false;
        }
        String path = request.getServletPath();
        boolean admin = "admin".equalsIgnoreCase(usuario.getNombre_usuario());
        boolean soloAdmin = path.startsWith("/admin/") || path.equals("/admin")
                || path.startsWith("/usuarios/") || path.equals("/panel/admin")
                || path.startsWith("/paneladministracion") || path.endsWith("/confirmar-cancelacion");
        if (soloAdmin && !admin) {
            response.sendError(403, "Acceso reservado a administración");
            return false;
        }
        if (path.startsWith("/pedido/")) {
            var variables = (Map<?, ?>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            if (variables != null && variables.get("id") != null) {
                var pedido = pedidos.findPedido(Long.valueOf(variables.get("id").toString()));
                if (pedido == null) { response.sendError(404); return false; }
                if (!admin && (pedido.getUsuario() == null || pedido.getUsuario().getId() != usuario.getId())) {
                    response.sendError(403); return false;
                }
            }
        }
        return true;
    }
}

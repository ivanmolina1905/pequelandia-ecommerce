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
/** [Doc]
 * Comprueba sesión, permisos administrativos y titularidad del pedido antes de ejecutar la ruta protegida.
 */
@Component
public class AccessInterceptor implements HandlerInterceptor {
    // [Doc] Atributo pedidos (IPedidoService): pedidos o servicio de pedidos según el tipo declarado. Su referencia no se reasigna después de inicializarse.
    private final IPedidoService pedidos;
    // [Doc] Atributo identidad (com.pequelandia.pequelandia.service.IdentidadService): colaborador que concentra operaciones de com.pequelandia.pequelandia.service.identidad. Su referencia no se reasigna después de inicializarse.
    private final com.pequelandia.pequelandia.service.IdentidadService identidad;
    /** [Doc]
     * Construye AccessInterceptor y conserva sus dependencias o valores iniciales.
     * @param pedidos pedidos o servicio de pedidos según el tipo declarado.
     * @param identidad colaborador que concentra operaciones de com.pequelandia.pequelandia.service.identidad.
     */
    public AccessInterceptor(IPedidoService pedidos, com.pequelandia.pequelandia.service.IdentidadService identidad) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.pedidos = pedidos;
        this.pedidos = pedidos;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.identidad = identidad;
        this.identidad = identidad;
    }

    /** [Doc]
     * Rechaza falta de sesión o privilegios y comprueba titularidad en rutas de pedidos antes de continuar.
     * @param request petición HTTP entrante.
     * @param response respuesta HTTP que se envía al navegador.
     * @param handler manejador MVC que atenderá la petición.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // [Doc] Variable local session (var; tipo inferido por Java): sesión HTTP que conserva identidad, carrito y token CSRF. Se inicializa con request.getSession(false).
        var session = request.getSession(false);
        // [Doc] Variable local usuario (Usuario): cuenta de usuario utilizada por la operación. Se inicializa con session == null ? null : (Usuario) session.getAttribute("usuario").
        Usuario usuario = session == null ? null : (Usuario) session.getAttribute("usuario");
        // [Doc] Evalúa usuario == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (usuario == null) {
            String accept = request.getHeader("Accept");
            if (accept != null && accept.contains("text/html")) {
                response.sendRedirect(request.getContextPath() + "/auth/login?sesionVencida=true");
                return false;
            }
            // [Doc] Ejecuta la llamada o modificación rechazar(response, 401, "Iniciá sesión para continuar"); Sus efectos dependen del método invocado y del objeto receptor.
            rechazar(response, 401, "Iniciá sesión para continuar");
            // [Doc] Termina el método o la función y devuelve false.
            return false;
        }
        // [Doc] Variable local path (String): path. Se inicializa con org.springframework.web.util.UrlPathHelper.defaultInstance.getPathWithinApplication(request).
        String path = org.springframework.web.util.UrlPathHelper.defaultInstance.getPathWithinApplication(request);
        // [Doc] Ejecuta la llamada o modificación usuario = identidad.exigirUsuario(session); Sus efectos dependen del método invocado y del objeto receptor.
        usuario = identidad.exigirUsuario(session);
        // [Doc] Variable local admin (boolean): admin. Se inicializa con identidad.esAdmin(usuario).
        boolean admin = identidad.esAdmin(usuario);
        // [Doc] Variable local soloAdmin (boolean): solo admin. Se inicializa con path.startsWith("/admin/") || path.equals("/admin") || path.startsWith("/usuarios/") || path.equals("/panel....
        boolean soloAdmin = path.startsWith("/admin/") || path.equals("/admin")
                || path.startsWith("/usuarios/") || path.equals("/panel/admin")
                || path.startsWith("/paneladministracion") || path.endsWith("/confirmar-cancelacion");
        // [Doc] Evalúa soloAdmin && !admin. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (soloAdmin && !admin) {
            // [Doc] Ejecuta la llamada o modificación rechazar(response, 403, "Acceso reservado a administración"); Sus efectos dependen del método invocado y del objeto receptor.
            rechazar(response, 403, "Acceso reservado a administración");
            // [Doc] Termina el método o la función y devuelve false.
            return false;
        }
        // [Doc] Evalúa path.startsWith("/pedido/"). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (path.startsWith("/pedido/")) {
            // [Doc] Variable local variables (var; tipo inferido por Java): variables. Se inicializa con (Map<?, ?>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE).
            var variables = (Map<?, ?>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            // [Doc] Evalúa variables != null && variables.get("id") != null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
            if (variables != null && variables.get("id") != null) {
                // [Doc] Variable local pedido (var; tipo inferido por Java): pedido. Se inicializa con pedidos.findPedido(Long.valueOf(variables.get("id").toString())).
                var pedido = pedidos.findPedido(Long.valueOf(variables.get("id").toString()));
                // [Doc] Evalúa pedido == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
                if (pedido == null) { rechazar(response, 404, "Pedido no encontrado"); return false; }
                // [Doc] Evalúa !admin && (pedido.getUsuario() == null || !java.util.Objects.equals(pedido.getUsuario().getId(), usuario.getId())). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
                if (!admin && (pedido.getUsuario() == null || !java.util.Objects.equals(pedido.getUsuario().getId(), usuario.getId()))) {
                    // [Doc] Ejecuta la llamada o modificación rechazar(response, 403, "No autorizado"); Sus efectos dependen del método invocado y del objeto receptor.
                    rechazar(response, 403, "No autorizado"); return false;
                }
            }
        }
        // [Doc] Termina el método o la función y devuelve true.
        return true;
    }

    /** [Doc]
     * Escribe el estado HTTP de rechazo y una respuesta JSON con error y redirección al login cuando falta sesión.
     * @param response respuesta HTTP que se envía al navegador.
     * @param status status.
     * @param mensaje mensaje que se prepara para enviar o responder.
     */
    private void rechazar(HttpServletResponse response, int status, String mensaje) throws java.io.IOException {
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: response.setStatus(status);
        response.setStatus(status);
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: response.setContentType("application/json;charset=UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        // [Doc] Ejecuta la llamada o modificación new com.fasterxml.jackson.databind.ObjectMapper().writeValue(response.getWriter(), Map.of( "ok", false, "error", mensaje, "redirect", status == 401 ? "/auth/login" : "... Sus efectos dependen del método invocado y del objeto receptor.
        new com.fasterxml.jackson.databind.ObjectMapper().writeValue(response.getWriter(), Map.of(
                "ok", false, "error", mensaje, "redirect", status == 401 ? "/auth/login" : ""));
    }
}

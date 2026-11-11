package com.pequelandia.pequelandia.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.method.HandlerMethod;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;
import java.util.UUID;

/** [Doc]
 * Genera el token de sesión y valida su envío en las peticiones que modifican datos.
 */
@Component
public class CsrfInterceptor implements HandlerInterceptor {
    // [Doc] Atributo KEY (String): clave de atributo usada para localizar el valor en la sesión. Se inicializa con "CSRF_TOKEN". Su referencia no se reasigna después de inicializarse.
    public static final String KEY = "CSRF_TOKEN";
    // [Doc] Atributo SAFE (Set<String>): métodos HTTP que no requieren token de modificación. Se inicializa con Set.of("GET", "HEAD", "OPTIONS"). Su referencia no se reasigna después de inicializarse.
    private static final Set<String> SAFE = Set.of("GET", "HEAD", "OPTIONS");

    /** [Doc]
     * Crea o reutiliza el token CSRF de sesión, lo expone a la vista y compara el token recibido por cabecera o
     * formulario.
     * @param request petición HTTP entrante.
     * @param response respuesta HTTP que se envía al navegador.
     * @param handler manejador MVC que atenderá la petición.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // [Doc] Evalúa !(handler instanceof HandlerMethod). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (!(handler instanceof HandlerMethod)) return true;
        // [Doc] Variable local session (var; tipo inferido por Java): sesión HTTP que conserva identidad, carrito y token CSRF. Se inicializa con request.getSession().
        var session = request.getSession();
        // [Doc] Variable local token (String): valor del token usado por esta operación.
        String token;
        // [Doc] Exige acceso exclusivo al monitor session mientras se ejecuta el bloque, para serializar operaciones concurrentes.
        synchronized (session) {
            // [Doc] Ejecuta la llamada o modificación token = (String) session.getAttribute(KEY); Sus efectos dependen del método invocado y del objeto receptor.
            token = (String) session.getAttribute(KEY);
            // [Doc] Evalúa token == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
            if (token == null) {
                // [Doc] Ejecuta la llamada o modificación token = UUID.randomUUID().toString(); Sus efectos dependen del método invocado y del objeto receptor.
                token = UUID.randomUUID().toString();
                // [Doc] Almacena un atributo en la sesión o petición: session.setAttribute(KEY, token);
                session.setAttribute(KEY, token);
            }
        }
        // [Doc] Almacena un atributo en la sesión o petición: request.setAttribute("csrfToken", token);
        request.setAttribute("csrfToken", token);
        if (session.getAttribute("usuario") != null) {
            long vence = System.currentTimeMillis() + session.getMaxInactiveInterval() * 1000L;
            request.setAttribute("sesionVence", vence);
            response.setHeader("X-Session-Expires", Long.toString(vence));
            response.setHeader("Cache-Control", "no-store");
        }
        // [Doc] Evalúa SAFE.contains(request.getMethod()). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (SAFE.contains(request.getMethod())) return true;
        // [Doc] Variable local recibido (String): recibido. Se inicializa con request.getHeader("X-CSRF-TOKEN").
        String recibido = request.getHeader("X-CSRF-TOKEN");
        // [Doc] Evalúa recibido == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (recibido == null) recibido = request.getParameter("_csrf");
        // [Doc] Evalúa recibido == null || !MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), recibido.getBytes(StandardCharsets.UTF_8)). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (recibido == null || !MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8),
                recibido.getBytes(StandardCharsets.UTF_8))) {
            // [Doc] Actualiza una propiedad o configuración mediante la llamada: response.setStatus(403);
            response.setStatus(403);
            // [Doc] Actualiza una propiedad o configuración mediante la llamada: response.setContentType("application/json;charset=UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            // [Doc] Ejecuta la llamada o modificación response.getWriter().write("{\"ok\":false,\"error\":\"La sesión del formulario venció. Recargá la página.\"}"); Sus efectos dependen del método invocado y del objeto receptor.
            response.getWriter().write("{\"ok\":false,\"error\":\"La sesión del formulario venció. Recargá la página.\"}");
            // [Doc] Termina el método o la función y devuelve false.
            return false;
        }
        // [Doc] Termina el método o la función y devuelve true.
        return true;
    }
}

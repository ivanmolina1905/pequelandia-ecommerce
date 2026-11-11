package com.pequelandia.pequelandia.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;
import java.io.IOException;
import java.util.Set;

/** Evita perder cambios de otra pestaña mientras se confirma el carrito de sesión. */
/** [Doc]
 * Serializa mediante el mutex de sesión las operaciones POST de las rutas de carrito configuradas.
 */
@Component
public class CarritoSesionFilter extends OncePerRequestFilter {
    // [Doc] Atributo RUTAS (Set<String>): rutas. Se inicializa con Set.of("/carrito/finalizar", "/carrito/cantidad", "/carrito/eliminar", "/productos/agregar", "/productos/ap.... Su referencia no se reasigna después de inicializarse.
    private static final Set<String> RUTAS = Set.of("/carrito/finalizar", "/carrito/cantidad", "/carrito/eliminar",
            "/productos/agregar", "/productos/api/agregar");
    /** [Doc]
     * Si la petición es POST y su ruta está en RUTAS, ejecuta la cadena bajo el mutex de sesión; en los demás casos
     * continúa sin ese bloqueo.
     * @param request petición HTTP entrante.
     * @param response respuesta HTTP que se envía al navegador.
     * @param chain chain.
     */
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        // [Doc] Variable local path (String): path. Se inicializa con org.springframework.web.util.UrlPathHelper.defaultInstance.getPathWithinApplication(request).
        String path = org.springframework.web.util.UrlPathHelper.defaultInstance.getPathWithinApplication(request);
        // [Doc] Evalúa "POST".equals(request.getMethod()) && RUTAS.contains(path). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if ("POST".equals(request.getMethod()) && RUTAS.contains(path)) {
            // [Doc] Exige acceso exclusivo al monitor WebUtils.getSessionMutex(request.getSession()) mientras se ejecuta el bloque, para serializar operaciones concurrentes.
            synchronized (WebUtils.getSessionMutex(request.getSession())) { chain.doFilter(request, response); }
        } else chain.doFilter(request, response);
    }
}

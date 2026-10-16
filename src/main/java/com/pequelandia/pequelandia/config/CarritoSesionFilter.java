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
@Component
public class CarritoSesionFilter extends OncePerRequestFilter {
    private static final Set<String> RUTAS = Set.of("/carrito/finalizar", "/carrito/cantidad", "/carrito/eliminar",
            "/productos/agregar", "/productos/api/agregar");
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String path = org.springframework.web.util.UrlPathHelper.defaultInstance.getPathWithinApplication(request);
        if ("POST".equals(request.getMethod()) && RUTAS.contains(path)) {
            synchronized (WebUtils.getSessionMutex(request.getSession())) { chain.doFilter(request, response); }
        } else chain.doFilter(request, response);
    }
}

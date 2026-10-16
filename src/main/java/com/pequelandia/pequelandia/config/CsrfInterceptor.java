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

@Component
public class CsrfInterceptor implements HandlerInterceptor {
    public static final String KEY = "CSRF_TOKEN";
    private static final Set<String> SAFE = Set.of("GET", "HEAD", "OPTIONS");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod)) return true;
        var session = request.getSession();
        String token;
        synchronized (session) {
            token = (String) session.getAttribute(KEY);
            if (token == null) {
                token = UUID.randomUUID().toString();
                session.setAttribute(KEY, token);
            }
        }
        request.setAttribute("csrfToken", token);
        if (SAFE.contains(request.getMethod())) return true;
        String recibido = request.getHeader("X-CSRF-TOKEN");
        if (recibido == null) recibido = request.getParameter("_csrf");
        if (recibido == null || !MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8),
                recibido.getBytes(StandardCharsets.UTF_8))) {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"ok\":false,\"error\":\"La sesión del formulario venció. Recargá la página.\"}");
            return false;
        }
        return true;
    }
}

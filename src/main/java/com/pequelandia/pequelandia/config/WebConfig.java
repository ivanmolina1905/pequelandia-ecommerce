package com.pequelandia.pequelandia.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AccessInterceptor access;
    private final CsrfInterceptor csrf;
    public WebConfig(AccessInterceptor access, CsrfInterceptor csrf) { this.access = access; this.csrf = csrf; }
    @Override public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(csrf).addPathPatterns("/**");
        registry.addInterceptor(access).addPathPatterns("/admin", "/admin/**", "/usuarios/**",
            "/panel/admin", "/paneladministracion", "/paneladministracion.html",
            "/panelusuario", "/panelusuario.html", "/panel/usuario", "/panelusuarios/**", "/pedido/**", "/notificaciones/**", "/carrito/finalizar");
    }
}

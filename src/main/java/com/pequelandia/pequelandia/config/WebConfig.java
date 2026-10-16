package com.pequelandia.pequelandia.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AccessInterceptor access;
    public WebConfig(AccessInterceptor access) { this.access = access; }
    @Override public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(access).addPathPatterns("/admin", "/admin/**", "/usuarios/**",
            "/panel/admin", "/paneladministracion", "/paneladministracion.html",
            "/panelusuario", "/panelusuario.html", "/panel/usuario", "/panelusuarios/**", "/pedido/**");
    }
}

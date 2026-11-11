package com.pequelandia.pequelandia.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** [Doc]
 * Registra los interceptores CSRF y de acceso y determina las rutas a las que se aplican.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // [Doc] Atributo access (AccessInterceptor): access. Su referencia no se reasigna después de inicializarse.
    private final AccessInterceptor access;
    // [Doc] Atributo csrf (CsrfInterceptor): csrf. Su referencia no se reasigna después de inicializarse.
    private final CsrfInterceptor csrf;
    /** [Doc]
     * Construye WebConfig y conserva sus dependencias o valores iniciales.
     * @param access access.
     * @param csrf csrf.
     */
    public WebConfig(AccessInterceptor access, CsrfInterceptor csrf) { this.access = access; this.csrf = csrf; }
    /** [Doc]
     * Agrega interceptors en el ámbito de WebConfig.
     * @param registry registro de interceptores MVC.
     */
    @Override public void addInterceptors(InterceptorRegistry registry) {
        // [Doc] Ejecuta la llamada o modificación registry.addInterceptor(csrf).addPathPatterns("/**"); Sus efectos dependen del método invocado y del objeto receptor.
        registry.addInterceptor(csrf).addPathPatterns("/**");
        // [Doc] Ejecuta la llamada o modificación registry.addInterceptor(access).addPathPatterns("/admin", "/admin/**", "/usuarios/**", "/panel/admin", "/paneladministracion", "/paneladministracion.html", "/panelusua... Sus efectos dependen del método invocado y del objeto receptor.
        registry.addInterceptor(access).addPathPatterns("/admin", "/admin/**", "/usuarios/**",
            "/panel/admin", "/paneladministracion", "/paneladministracion.html",
            "/panelusuario", "/panelusuario.html", "/panel/usuario", "/panelusuarios/**", "/pedido/**", "/notificaciones/**", "/carrito/finalizar");
    }
}

package com.pequelandia.pequelandia.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import static org.junit.jupiter.api.Assertions.*;

/** [Doc]
 * Agrupa pruebas automatizadas de csrf interceptor.
 */
class CsrfInterceptorTest {
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: endpoint.
     */
    public void endpoint() { }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: handler.
     * @return HandlerMethod con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private HandlerMethod handler() throws Exception { return new HandlerMethod(this, getClass().getMethod("endpoint")); }
    /** [Doc]
     * Comprueba el escenario «rechaza post sin token ycon token ajeno» mediante las aserciones del cuerpo.
     */
    @Test void rechazaPostSinTokenYConTokenAjeno() throws Exception {
        // [Doc] Variable local csrf (var; tipo inferido por Java): csrf. Se inicializa con new CsrfInterceptor().
        var csrf = new CsrfInterceptor(); var req = new MockHttpServletRequest("POST", "/pedido/1/cancelar");
        // [Doc] Variable local res (var; tipo inferido por Java): res. Se inicializa con new MockHttpServletResponse().
        var res = new MockHttpServletResponse();
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertFalse(csrf.preHandle(req, res, handler()));
        assertFalse(csrf.preHandle(req, res, handler())); assertEquals(403, res.getStatus());
        // [Doc] Ejecuta la llamada o modificación req.addHeader("X-CSRF-TOKEN", "otro-token"); Sus efectos dependen del método invocado y del objeto receptor.
        req.addHeader("X-CSRF-TOKEN", "otro-token");
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertFalse(csrf.preHandle(req, new MockHttpServletResponse(), handler()));
        assertFalse(csrf.preHandle(req, new MockHttpServletResponse(), handler()));
    }
    /** [Doc]
     * Comprueba el escenario «acepta token en header oformulario» mediante las aserciones del cuerpo.
     */
    @Test void aceptaTokenEnHeaderOFormulario() throws Exception {
        // [Doc] Variable local csrf (var; tipo inferido por Java): csrf. Se inicializa con new CsrfInterceptor().
        var csrf = new CsrfInterceptor(); var req = new MockHttpServletRequest("GET", "/auth/login");
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(csrf.preHandle(req, new MockHttpServletResponse(), handler()));
        assertTrue(csrf.preHandle(req, new MockHttpServletResponse(), handler()));
        // [Doc] Variable local token (String): valor del token usado por esta operación. Se inicializa con (String) req.getSession().getAttribute(CsrfInterceptor.KEY).
        String token = (String) req.getSession().getAttribute(CsrfInterceptor.KEY);
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertNotNull(token);
        assertNotNull(token); assertEquals(token, req.getAttribute("csrfToken"));
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: req.setMethod("POST");
        req.setMethod("POST"); req.addParameter("_csrf", token);
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(csrf.preHandle(req, new MockHttpServletResponse(), handler()));
        assertTrue(csrf.preHandle(req, new MockHttpServletResponse(), handler()));
        // [Doc] Ejecuta la llamada o modificación req.removeParameter("_csrf"); Sus efectos dependen del método invocado y del objeto receptor.
        req.removeParameter("_csrf"); req.addHeader("X-CSRF-TOKEN", token);
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(csrf.preHandle(req, new MockHttpServletResponse(), handler()));
        assertTrue(csrf.preHandle(req, new MockHttpServletResponse(), handler()));
    }
}

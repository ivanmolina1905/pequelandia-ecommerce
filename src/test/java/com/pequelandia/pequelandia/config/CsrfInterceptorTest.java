package com.pequelandia.pequelandia.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import static org.junit.jupiter.api.Assertions.*;

class CsrfInterceptorTest {
    public void endpoint() { }
    private HandlerMethod handler() throws Exception { return new HandlerMethod(this, getClass().getMethod("endpoint")); }
    @Test void rechazaPostSinTokenYConTokenAjeno() throws Exception {
        var csrf = new CsrfInterceptor(); var req = new MockHttpServletRequest("POST", "/pedido/1/cancelar");
        var res = new MockHttpServletResponse();
        assertFalse(csrf.preHandle(req, res, handler())); assertEquals(403, res.getStatus());
        req.addHeader("X-CSRF-TOKEN", "otro-token");
        assertFalse(csrf.preHandle(req, new MockHttpServletResponse(), handler()));
    }
    @Test void aceptaTokenEnHeaderOFormulario() throws Exception {
        var csrf = new CsrfInterceptor(); var req = new MockHttpServletRequest("GET", "/auth/login");
        assertTrue(csrf.preHandle(req, new MockHttpServletResponse(), handler()));
        String token = (String) req.getSession().getAttribute(CsrfInterceptor.KEY);
        assertNotNull(token); assertEquals(token, req.getAttribute("csrfToken"));
        req.setMethod("POST"); req.addParameter("_csrf", token);
        assertTrue(csrf.preHandle(req, new MockHttpServletResponse(), handler()));
        req.removeParameter("_csrf"); req.addHeader("X-CSRF-TOKEN", token);
        assertTrue(csrf.preHandle(req, new MockHttpServletResponse(), handler()));
    }
}

package com.pequelandia.pequelandia.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pequelandia.pequelandia.dto.PuntoSerieDTO;
import com.pequelandia.pequelandia.model.Pedido;
import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.service.IPedidoService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccessInterceptorTest {
    private final IPedidoService pedidos = mock(IPedidoService.class);
    private final com.pequelandia.pequelandia.repository.IUsuarioRepository usuarios = mock(com.pequelandia.pequelandia.repository.IUsuarioRepository.class);
    private final com.pequelandia.pequelandia.service.IdentidadService identidad = new com.pequelandia.pequelandia.service.IdentidadService(usuarios, 1L);
    private final AccessInterceptor access = new AccessInterceptor(pedidos, identidad);
    { identidad.inicializar(); }
    private Usuario usuario(long id, String nombre) {
        var u = new Usuario(); u.setId(id); u.setNombre_usuario(nombre); when(usuarios.findById(id)).thenReturn(java.util.Optional.of(u)); return u;
    }
    private MockHttpServletRequest request(String path, Usuario u) {
        var req = new MockHttpServletRequest("GET", path); req.setServletPath(path);
        if (u != null) req.getSession().setAttribute("usuario", u);
        return req;
    }
    @Test void requiereSesion() throws Exception {
        var res = new MockHttpServletResponse();
        assertFalse(access.preHandle(request("/admin/usuarios", null), res, new Object()));
        assertEquals(401, res.getStatus());
    }
    @Test void bloqueaUsuarioComunEnAdministracionYExportaciones() throws Exception {
        for (String path : new String[]{"/admin/usuarios", "/admin/export/usuarios", "/usuarios/traer", "/paneladministracion.html", "/pedido/1/confirmar-cancelacion"}) {
            var res = new MockHttpServletResponse();
            assertFalse(access.preHandle(request(path, usuario(2, "demo")), res, new Object()));
            assertEquals(403, res.getStatus());
        }
    }
    @Test void permiteAdministracion() throws Exception {
        assertTrue(access.preHandle(request("/admin/usuarios", usuario(1, "admin")), new MockHttpServletResponse(), new Object()));
    }
    @Test void pedidoExigeTitularidad() throws Exception {
        var p = new Pedido(); p.setUsuario(usuario(2, "titular")); when(pedidos.findPedido(7L)).thenReturn(p);
        var req = request("/pedido/7/pdf", usuario(3, "ajeno"));
        req.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("id", "7"));
        var res = new MockHttpServletResponse();
        assertFalse(access.preHandle(req, res, new Object())); assertEquals(403, res.getStatus());
        req.getSession().setAttribute("usuario", usuario(2, "titular"));
        assertTrue(access.preHandle(req, new MockHttpServletResponse(), new Object()));
    }
    @Test void pedidoInexistenteDevuelve404() throws Exception {
        var req = request("/pedido/9", usuario(2, "demo"));
        req.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("id", "9"));
        var res = new MockHttpServletResponse();
        assertFalse(access.preHandle(req, res, new Object())); assertEquals(404, res.getStatus());
    }
    @Test void jsonNoExponeContrasenaNiModificaSuAlmacenamiento() throws Exception {
        var u = usuario(2, "demo"); u.setContraseña("valor-ficticio-de-prueba");
        assertFalse(new ObjectMapper().writeValueAsString(u).contains("valor-ficticio-de-prueba"));
        assertEquals("valor-ficticio-de-prueba", u.getContraseña());
    }
    @Test void serieConservaDatosParaEstadisticas() {
        var punto = new PuntoSerieDTO("2026-01", 125.0);
        assertEquals("2026-01", punto.getLabel()); assertEquals(125.0, punto.getValor());
    }
}

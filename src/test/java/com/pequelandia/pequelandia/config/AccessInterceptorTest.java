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

/** [Doc]
 * Agrupa pruebas automatizadas de access interceptor.
 */
class AccessInterceptorTest {
    @Test void redirigeNavegacionSinSesionAlLogin() throws Exception {
        var req = request("/panelusuario", null);
        req.addHeader("Accept", "text/html");
        var res = new MockHttpServletResponse();
        assertFalse(access.preHandle(req, res, new Object()));
        assertEquals("/auth/login?sesionVencida=true", res.getRedirectedUrl());
    }
    // [Doc] Atributo pedidos (IPedidoService): pedidos o servicio de pedidos según el tipo declarado. Se inicializa con mock(IPedidoService.class). Su referencia no se reasigna después de inicializarse.
    private final IPedidoService pedidos = mock(IPedidoService.class);
    // [Doc] Atributo usuarios (com.pequelandia.pequelandia.repository.IUsuarioRepository): acceso de persistencia a com.pequelandia.pequelandia.repository.iusuario. Se inicializa con mock(com.pequelandia.pequelandia.repository.IUsuarioRepository.class). Su referencia no se reasigna después de inicializarse.
    private final com.pequelandia.pequelandia.repository.IUsuarioRepository usuarios = mock(com.pequelandia.pequelandia.repository.IUsuarioRepository.class);
    // [Doc] Atributo identidad (com.pequelandia.pequelandia.service.IdentidadService): colaborador que concentra operaciones de com.pequelandia.pequelandia.service.identidad. Se inicializa con new com.pequelandia.pequelandia.service.IdentidadService(usuarios, 1L). Su referencia no se reasigna después de inicializarse.
    private final com.pequelandia.pequelandia.service.IdentidadService identidad = new com.pequelandia.pequelandia.service.IdentidadService(usuarios, 1L);
    // [Doc] Atributo access (AccessInterceptor): access. Se inicializa con new AccessInterceptor(pedidos, identidad). Su referencia no se reasigna después de inicializarse.
    private final AccessInterceptor access = new AccessInterceptor(pedidos, identidad);
    { identidad.inicializar(); }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: usuario.
     * @param id identificador del registro.
     * @param nombre nombre normalizado o visible de la entidad.
     * @return Usuario con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private Usuario usuario(long id, String nombre) {
        // [Doc] Variable local u (var; tipo inferido por Java): cuenta de usuario utilizada por la operación. Se inicializa con new Usuario().
        var u = new Usuario(); u.setId(id); u.setNombre_usuario(nombre); when(usuarios.findById(id)).thenReturn(java.util.Optional.of(u)); return u;
    }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: request.
     * @param path path.
     * @param u cuenta de usuario utilizada por la operación.
     * @return MockHttpServletRequest con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private MockHttpServletRequest request(String path, Usuario u) {
        // [Doc] Variable local req (var; tipo inferido por Java): req. Se inicializa con new MockHttpServletRequest("GET", path).
        var req = new MockHttpServletRequest("GET", path); req.setServletPath(path);
        // [Doc] Evalúa u != null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (u != null) req.getSession().setAttribute("usuario", u);
        // [Doc] Termina el método o la función y devuelve req.
        return req;
    }
    /** [Doc]
     * Comprueba el escenario «requiere sesion» mediante las aserciones del cuerpo.
     */
    @Test void requiereSesion() throws Exception {
        // [Doc] Variable local res (var; tipo inferido por Java): res. Se inicializa con new MockHttpServletResponse().
        var res = new MockHttpServletResponse();
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertFalse(access.preHandle(request("/admin/usuarios", null), res, new Object()));
        assertFalse(access.preHandle(request("/admin/usuarios", null), res, new Object()));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(401, res.getStatus());
        assertEquals(401, res.getStatus());
    }
    /** [Doc]
     * Comprueba el escenario «bloquea usuario comun en administracion yexportaciones» mediante las aserciones del
     * cuerpo.
     */
    @Test void bloqueaUsuarioComunEnAdministracionYExportaciones() throws Exception {
        // [Doc] Recorre los elementos o repite según String path : new String[]{"/admin/usuarios", "/admin/export/usuarios", "/usuarios/traer", "/paneladministracion.html", "/pedido/1/confirmar-cancel.... Cada vuelta ejecuta el cuerpo del bucle.
        for (String path : new String[]{"/admin/usuarios", "/admin/export/usuarios", "/usuarios/traer", "/paneladministracion.html", "/pedido/1/confirmar-cancelacion"}) {
            // [Doc] Variable local res (var; tipo inferido por Java): res. Se inicializa con new MockHttpServletResponse().
            var res = new MockHttpServletResponse();
            // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertFalse(access.preHandle(request(path, usuario(2, "demo")), res, new Object()));
            assertFalse(access.preHandle(request(path, usuario(2, "demo")), res, new Object()));
            // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(403, res.getStatus());
            assertEquals(403, res.getStatus());
        }
    }
    /** [Doc]
     * Comprueba el escenario «permite administracion» mediante las aserciones del cuerpo.
     */
    @Test void permiteAdministracion() throws Exception {
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(access.preHandle(request("/admin/usuarios", usuario(1, "admin")), new MockHttpServletResponse(), new Object()));
        assertTrue(access.preHandle(request("/admin/usuarios", usuario(1, "admin")), new MockHttpServletResponse(), new Object()));
    }
    /** [Doc]
     * Comprueba el escenario «pedido exige titularidad» mediante las aserciones del cuerpo.
     */
    @Test void pedidoExigeTitularidad() throws Exception {
        // [Doc] Variable local p (var; tipo inferido por Java): p. Se inicializa con new Pedido().
        var p = new Pedido(); p.setUsuario(usuario(2, "titular")); when(pedidos.findPedido(7L)).thenReturn(p);
        // [Doc] Variable local req (var; tipo inferido por Java): req. Se inicializa con request("/pedido/7/pdf", usuario(3, "ajeno")).
        var req = request("/pedido/7/pdf", usuario(3, "ajeno"));
        // [Doc] Almacena un atributo en la sesión o petición: req.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("id", "7"));
        req.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("id", "7"));
        // [Doc] Variable local res (var; tipo inferido por Java): res. Se inicializa con new MockHttpServletResponse().
        var res = new MockHttpServletResponse();
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertFalse(access.preHandle(req, res, new Object()));
        assertFalse(access.preHandle(req, res, new Object())); assertEquals(403, res.getStatus());
        // [Doc] Almacena un atributo en la sesión o petición: req.getSession().setAttribute("usuario", usuario(2, "titular"));
        req.getSession().setAttribute("usuario", usuario(2, "titular"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(access.preHandle(req, new MockHttpServletResponse(), new Object()));
        assertTrue(access.preHandle(req, new MockHttpServletResponse(), new Object()));
    }
    /** [Doc]
     * Comprueba el escenario «pedido inexistente devuelve404» mediante las aserciones del cuerpo.
     */
    @Test void pedidoInexistenteDevuelve404() throws Exception {
        // [Doc] Variable local req (var; tipo inferido por Java): req. Se inicializa con request("/pedido/9", usuario(2, "demo")).
        var req = request("/pedido/9", usuario(2, "demo"));
        // [Doc] Almacena un atributo en la sesión o petición: req.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("id", "9"));
        req.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("id", "9"));
        // [Doc] Variable local res (var; tipo inferido por Java): res. Se inicializa con new MockHttpServletResponse().
        var res = new MockHttpServletResponse();
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertFalse(access.preHandle(req, res, new Object()));
        assertFalse(access.preHandle(req, res, new Object())); assertEquals(404, res.getStatus());
    }
    /** [Doc]
     * Comprueba el escenario «json no expone contrasena ni modifica su almacenamiento» mediante las aserciones del
     * cuerpo.
     */
    @Test void jsonNoExponeContrasenaNiModificaSuAlmacenamiento() throws Exception {
        // [Doc] Variable local u (var; tipo inferido por Java): cuenta de usuario utilizada por la operación. Se inicializa con usuario(2, "demo").
        var u = usuario(2, "demo"); u.setContraseña("valor-ficticio-de-prueba");
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertFalse(new ObjectMapper().writeValueAsString(u).contains("valor-ficticio-de-prueba"));
        assertFalse(new ObjectMapper().writeValueAsString(u).contains("valor-ficticio-de-prueba"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals("valor-ficticio-de-prueba", u.getContraseña());
        assertEquals("valor-ficticio-de-prueba", u.getContraseña());
    }
    /** [Doc]
     * Comprueba el escenario «serie conserva datos para estadisticas» mediante las aserciones del cuerpo.
     */
    @Test void serieConservaDatosParaEstadisticas() {
        // [Doc] Variable local punto (var; tipo inferido por Java): punto. Se inicializa con new PuntoSerieDTO("2026-01", 125.0).
        var punto = new PuntoSerieDTO("2026-01", 125.0);
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals("2026-01", punto.getLabel());
        assertEquals("2026-01", punto.getLabel()); assertEquals(125.0, punto.getValor());
    }
}

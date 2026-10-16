package com.pequelandia.pequelandia;

import com.pequelandia.pequelandia.config.CsrfInterceptor;
import com.pequelandia.pequelandia.model.*;
import com.pequelandia.pequelandia.repository.*;
import com.pequelandia.pequelandia.service.*;
import com.pequelandia.pequelandia.util.CartSession;
import com.pequelandia.pequelandia.util.CartSessionUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PedidosIntegrationTest extends IntegrationSupport {
    @Autowired IPedidoService pedidos;
    @Autowired IProductoRepository productos;
    @Autowired IUsuarioRepository usuarios;
    @Autowired IPedidoRepository pedidoRepo;
    @Autowired INotificacionRepository avisos;
    @Autowired INotificacionService notificaciones;
    @Autowired ICarrito_de_compraService carritos;
    @Autowired IDetalle_carritoService detalles;
    @Autowired ICarrito_de_compraRepository carritoRepo;
    @Autowired ILocalidadRepository localidades;
    @Autowired EntregaCorreoService entrega;
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;
    @MockitoBean JavaMailSender smtp;
    @MockitoSpyBean ColaCorreoService cola;
    Usuario admin, titular, ajeno;
    Producto producto;

    @BeforeEach void preparar() {
        jdbc.execute("TRUNCATE usuario, producto, correo_pendiente RESTART IDENTITY CASCADE");
        admin = usuario("admin"); titular = usuario("titular"); ajeno = usuario("ajeno");
        producto = producto(10);
        reset(smtp, cola);
    }
    Usuario usuario(String nombre) {
        Usuario u = new Usuario(); u.setNombre_usuario(nombre); u.setEmail(nombre + "@example.invalid");
        u.setContraseña("ficticia"); return usuarios.save(u);
    }
    Producto producto(int stock) {
        Producto p = new Producto(); p.setNombre_producto("Prueba"); p.setStock(stock);
        p.setPrecio(12.5f); p.setEstado("activo"); return productos.save(p);
    }
    Pedido comprar(int cantidad) { return pedidos.comprar(titular.getId(), Map.of(producto.getIdProducto(), cantidad)); }
    int stock() { return productos.findById(producto.getIdProducto()).orElseThrow().getStock(); }
    int correos() { return jdbc.queryForObject("SELECT count(*) FROM correo_pendiente", Integer.class); }
    MockHttpSession sesion(Usuario u) {
        MockHttpSession s = new MockHttpSession(); s.setAttribute("usuario", u); s.setAttribute(CsrfInterceptor.KEY, "prueba-csrf"); return s;
    }
    List<Object> paralelos(Callable<?> a, Callable<?> b) throws Exception {
        try (var pool = Executors.newVirtualThreadPerTaskExecutor()) {
            CountDownLatch listos = new CountDownLatch(2), inicio = new CountDownLatch(1);
            List<Future<Object>> futuros = new ArrayList<>();
            for (Callable<?> tarea : List.of(a, b)) futuros.add(pool.submit(() -> {
                listos.countDown(); inicio.await();
                try { return tarea.call(); } catch (Exception ex) { return ex; }
            }));
            assertTrue(listos.await(5, TimeUnit.SECONDS)); inicio.countDown();
            return List.of(futuros.get(0).get(20, TimeUnit.SECONDS), futuros.get(1).get(20, TimeUnit.SECONDS));
        }
    }

    @Test void compraAtomicaYReintentoDelMismoCarrito() {
        Pedido p = comprar(2);
        assertEquals(8, stock()); assertEquals(2, avisos.count()); assertEquals(2, correos());
        assertEquals(p.getId_pedido(), pedidos.finalizarCompra(titular.getId(), p.getCarrito().getIdCarrito()).getId_pedido());
        assertEquals(8, stock()); assertEquals(2, avisos.count()); assertEquals(2, correos());
        verifyNoInteractions(smtp);
    }
    @Test void falloDeStockRevierteCarritoDetallesYDescuentos() {
        Producto sinStock = producto(0);
        assertThrows(IllegalStateException.class, () -> pedidos.comprar(titular.getId(),
                Map.of(producto.getIdProducto(), 2, sinStock.getIdProducto(), 1)));
        assertEquals(10, stock()); assertEquals(0, pedidoRepo.count()); assertEquals(0, correos());
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM carrito_de_compra", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM detalle_carrito", Integer.class));
    }
    @Test void falloDeAvisosRevierteLaCompraCompleta() {
        doThrow(new IllegalStateException("Fallo simulado de persistencia")).when(cola).encolar(anyString(), anyString(), anyString());
        assertThrows(IllegalStateException.class, () -> comprar(2));
        assertEquals(10, stock()); assertEquals(0, pedidoRepo.count()); assertEquals(0, avisos.count());
    }
    @Test void solicitudRepetidaYConfirmacionesConcurrentesReponenUnaVez() throws Exception {
        Pedido p = comprar(3);
        pedidos.solicitarCancelacion(p.getId_pedido(), titular.getId());
        pedidos.solicitarCancelacion(p.getId_pedido(), titular.getId());
        assertEquals(7, stock()); assertEquals(3, avisos.count()); assertEquals(3, correos());
        var resultados = paralelos(() -> pedidos.confirmarCancelacion(p.getId_pedido()), () -> pedidos.confirmarCancelacion(p.getId_pedido()));
        assertTrue(resultados.stream().allMatch(Pedido.class::isInstance), resultados.toString());
        assertEquals(10, stock()); assertEquals(5, avisos.count()); assertEquals(5, correos());
        assertThrows(IllegalStateException.class, () -> pedidos.cambiarEstado(p.getId_pedido(), "PREPARANDO"));
        assertEquals(10, stock());
    }
    @Test void entregadoNoPuedeCancelarseNiReponerse() {
        Pedido p = comprar(2); pedidos.marcarEntregado(p.getId_pedido());
        assertThrows(IllegalStateException.class, () -> pedidos.cambiarEstado(p.getId_pedido(), "CANCELADO"));
        assertThrows(IllegalStateException.class, () -> pedidos.solicitarCancelacion(p.getId_pedido(), titular.getId()));
        assertEquals(8, stock());
    }
    @Test void falloAlNotificarCancelacionRevierteStockYEstado() {
        Pedido pedido = comprar(2);
        doThrow(new IllegalStateException("Fallo simulado")).when(cola).encolar(anyString(), anyString(), anyString());
        assertThrows(IllegalStateException.class, () -> pedidos.cambiarEstado(pedido.getId_pedido(), "CANCELADO"));
        assertEquals(8, stock());
        assertEquals("PREPARANDO", pedidos.findPedido(pedido.getId_pedido()).getEstado_pedido());
        assertEquals(2, avisos.count()); assertEquals(2, correos());
    }
    @Test void carritoHistoricoNoSeReutilizaNiSeModifica() {
        Pedido pedido = comprar(2);
        long historico = pedido.getCarrito().getIdCarrito();
        var activo = carritos.getOrCreateCarrito(sesion(titular));
        assertNotEquals(historico, activo.getIdCarrito());
        assertThrows(IllegalStateException.class, () -> carritos.cambiarCantidad(historico, producto.getIdProducto(), 9));
        assertThrows(IllegalStateException.class, () -> detalles.setCantidad(historico, producto.getIdProducto(), 9));
        pedidos.cambiarEstado(pedido.getId_pedido(), "CANCELADO");
        assertEquals(10, stock());
    }
    @Test void fusionDeInvitadoConservaCantidadesYPrecio() {
        var invitado = carritos.getOrCreateCarrito(new MockHttpSession());
        carritos.agregarDetalle(invitado.getIdCarrito(), producto.getIdProducto(), 2);
        var activo = carritos.getOrCreateCarrito(sesion(titular));
        carritos.mergeCarritos(invitado.getIdCarrito(), activo.getIdCarrito());
        assertFalse(carritoRepo.existsById(invitado.getIdCarrito()));
        var lineas = detalles.listarPorCarrito(activo.getIdCarrito());
        assertEquals(1, lineas.size()); assertEquals(2, lineas.get(0).getCantidad());
        assertEquals(12.5f, lineas.get(0).getPrecio());
        pedidos.finalizarCompra(titular.getId(), activo.getIdCarrito());
        assertEquals(8, stock()); assertEquals(2, correos());
    }
    @Test void dosComprasCompitenPorLaUltimaUnidad() throws Exception {
        producto.setStock(1); productos.save(producto);
        var resultados = paralelos(() -> comprar(1), () -> comprar(1));
        assertEquals(1, resultados.stream().filter(Pedido.class::isInstance).count(), resultados.toString());
        assertEquals(0, stock()); assertEquals(1, pedidoRepo.count()); assertEquals(2, correos());
    }
    @Test void lecturaConcurrenteEsIdempotenteYProtegeDestinatario() throws Exception {
        comprar(1);
        long id = avisos.listarUltimas("USUARIO_" + titular.getId(), org.springframework.data.domain.PageRequest.of(0, 1)).get(0).getId_notificacion();
        String destinatario = "USUARIO_" + titular.getId();
        var resultado = paralelos(() -> { notificaciones.marcarComoLeida(id, destinatario); return true; },
                () -> { notificaciones.marcarTodasComoLeidas(destinatario); return true; });
        assertEquals(List.of(true, true), resultado);
        assertEquals(0, notificaciones.contarNoLeidas(destinatario));
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM notificacion_lectura", Integer.class));
        assertThrows(IllegalArgumentException.class, () -> notificaciones.marcarComoLeida(id, "USUARIO_" + ajeno.getId()));
    }
    @Test void smtpFallidoSeReintentaSinDuplicarAvisos() {
        comprar(1);
        doThrow(new MailSendException("Simulado")).when(smtp).send(any(SimpleMailMessage.class));
        assertTrue(entrega.entregarSiguiente());
        assertEquals(1, jdbc.queryForObject("SELECT sum(intentos) FROM correo_pendiente", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM correo_pendiente WHERE enviado_en IS NOT NULL", Integer.class));
        doNothing().when(smtp).send(any(SimpleMailMessage.class));
        jdbc.update("UPDATE correo_pendiente SET proximo_intento=CURRENT_TIMESTAMP");
        assertTrue(entrega.entregarSiguiente()); assertTrue(entrega.entregarSiguiente()); assertFalse(entrega.entregarSiguiente());
        assertEquals(2, avisos.count()); assertEquals(2, correos());
        assertEquals(2, jdbc.queryForObject("SELECT count(*) FROM correo_pendiente WHERE enviado_en IS NOT NULL", Integer.class));
    }
    @Test void seguridadHttpProtegePedidosAdminPerfilYCsrF() throws Exception {
        Pedido p = comprar(1); long id = p.getId_pedido();
        mvc.perform(get("/admin/pedidos/lista")).andExpect(status().isUnauthorized());
        mvc.perform(get("/admin/pedidos/lista").session(sesion(titular))).andExpect(status().isForbidden());
        mvc.perform(get("/pedido/" + id + "/pdf").session(sesion(ajeno))).andExpect(status().isForbidden());
        mvc.perform(get("/pedido/" + id).session(sesion(titular))).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        mvc.perform(post("/pedido/" + id + "/confirmar-cancelacion").session(sesion(titular)).header("X-CSRF-TOKEN", "prueba-csrf"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/pedido/" + id + "/cancelar").session(sesion(titular))).andExpect(status().isForbidden());
        mvc.perform(post("/panelusuarios/perfil/editar").session(sesion(titular)).header("X-CSRF-TOKEN", "prueba-csrf")
                .param("nombre", "admin").param("email", titular.getEmail())).andExpect(status().isBadRequest());
        assertEquals("titular", usuarios.findById(titular.getId()).orElseThrow().getNombre_usuario());
        mvc.perform(post("/pedido/" + id + "/cancelar").session(sesion(titular)).header("X-CSRF-TOKEN", "prueba-csrf"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ok").value(true));
        mvc.perform(post("/panelusuarios/" + id + "/cancelar").session(sesion(titular)).header("X-CSRF-TOKEN", "prueba-csrf"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ok").value(true));
        assertEquals(3, avisos.count());
        mvc.perform(post("/pedido/" + id + "/confirmar-cancelacion").session(sesion(admin)).header("X-CSRF-TOKEN", "prueba-csrf"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ok").value(true));
        assertEquals(10, stock());
    }
    @Test void checkoutConcurrenteDeUnaSesionSoloCreaUnPedido() throws Exception {
        MockHttpSession session = sesion(titular);
        CartSession cart = new CartSession(); cart.add(producto.getIdProducto(), 2);
        session.setAttribute(CartSessionUtil.KEY, cart);
        Callable<Integer> compra = () -> mvc.perform(post("/carrito/finalizar").session(session)
                .header("X-CSRF-TOKEN", "prueba-csrf")).andReturn().getResponse().getStatus();
        var resultados = paralelos(compra, compra);
        assertTrue(resultados.stream().allMatch(Integer.class::isInstance), resultados.toString());
        assertEquals(1, pedidoRepo.count()); assertEquals(8, stock()); assertEquals(2, correos());
    }
    @Test void vistasRenderizanTokenYCuentasNoSeEscalanPorNombreDeSesion() throws Exception {
        var session = sesion(titular);
        mvc.perform(get("/auth/login").session(session)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("content=\"prueba-csrf\"")));
        titular.setNombre_usuario("admin"); session.setAttribute("usuario", titular);
        mvc.perform(get("/admin/pedidos/lista").session(session)).andExpect(status().isForbidden());
        mvc.perform(get("/notificaciones/mias").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.ok").value(true));
    }
    @Test void registroEncolaBienvenidaYAvisoSinEnviarSmtp() throws Exception {
        Localidad localidad = new Localidad(); localidad.setNombre_localidad("Localidad ficticia");
        localidad.setCodigo_postal(3000); localidades.save(localidad);
        mvc.perform(post("/auth/registro").session(sesion(titular)).header("X-CSRF-TOKEN", "prueba-csrf")
                .param("nombre_usuario", "nueva").param("apellido", "Prueba").param("email", "nueva@example.invalid")
                .param("dni", "11111111").param("direccion", "Domicilio ficticio")
                .param("localidadId", String.valueOf(localidad.getId_localidad())).param("contraseña", "ficticia")
                .param("confirmarContraseña", "ficticia").param("aceptarTerminos", "true"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/auth/login?registroExitoso=true"));
        assertTrue(usuarios.existeNombreUsuario("nueva")); assertEquals(1, avisos.count()); assertEquals(2, correos());
        verifyNoInteractions(smtp);
    }
    @Test void panelesYCatalogoRenderizanConCsrfYPermisos() throws Exception {
        comprar(2);
        for (String ruta : List.of("/panelusuarios/panelhistorial", "/carrito/vercarrito", "/productos"))
            mvc.perform(get(ruta).session(sesion(titular))).andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("content=\"prueba-csrf\"")));
        mvc.perform(get("/admin/usuarios").session(sesion(admin))).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("content=\"prueba-csrf\"")));
        mvc.perform(get("/pedido/" + pedidoRepo.findAll().get(0).getId_pedido() + "/pdf").session(sesion(titular)))
                .andExpect(status().isOk()).andExpect(content().contentType("application/pdf"));
    }
}

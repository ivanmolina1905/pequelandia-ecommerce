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

/** [Doc]
 * Agrupa pruebas automatizadas de pedidos.
 */
class PedidosIntegrationTest extends IntegrationSupport {
    // [Doc] Atributo pedidos (IPedidoService): pedidos o servicio de pedidos según el tipo declarado.
    @Autowired IPedidoService pedidos;
    // [Doc] Atributo productos (IProductoRepository): acceso de persistencia a producto.
    @Autowired IProductoRepository productos;
    // [Doc] Atributo usuarios (IUsuarioRepository): acceso de persistencia a usuario.
    @Autowired IUsuarioRepository usuarios;
    // [Doc] Atributo pedidoRepo (IPedidoRepository): acceso de persistencia a pedido.
    @Autowired IPedidoRepository pedidoRepo;
    // [Doc] Atributo avisos (INotificacionRepository): acceso de persistencia a notificacion.
    @Autowired INotificacionRepository avisos;
    // [Doc] Atributo notificaciones (INotificacionService): colaborador que concentra operaciones de notificacion.
    @Autowired INotificacionService notificaciones;
    // [Doc] Atributo carritos (ICarrito_de_compraService): colaborador que concentra operaciones de carrito de compra.
    @Autowired ICarrito_de_compraService carritos;
    // [Doc] Atributo detalles (IDetalle_carritoService): líneas asociadas al carrito.
    @Autowired IDetalle_carritoService detalles;
    // [Doc] Atributo carritoRepo (ICarrito_de_compraRepository): acceso de persistencia a carrito de compra.
    @Autowired ICarrito_de_compraRepository carritoRepo;
    // [Doc] Atributo localidades (ILocalidadRepository): acceso de persistencia a localidad.
    @Autowired ILocalidadRepository localidades;
    // [Doc] Atributo entrega (EntregaCorreoService): colaborador que concentra operaciones de entrega correo.
    @Autowired EntregaCorreoService entrega;
    // [Doc] Atributo jdbc (JdbcTemplate): acceso SQL parametrizado mediante JdbcTemplate.
    @Autowired JdbcTemplate jdbc;
    // [Doc] Atributo mvc (MockMvc): mvc.
    @Autowired MockMvc mvc;
    // [Doc] Atributo smtp (JavaMailSender): smtp.
    @MockitoBean JavaMailSender smtp;
    // [Doc] Atributo cola (ColaCorreoService): colaborador que concentra operaciones de cola correo.
    @MockitoSpyBean ColaCorreoService cola;
    // [Doc] Atributo ajeno (Usuario): ajeno.
    Usuario admin, titular, ajeno;
    // [Doc] Atributo producto (Producto): producto procesado en este punto.
    Producto producto;

    /** [Doc]
     * Prepara o auxilia el escenario de prueba: preparar.
     */
    @BeforeEach void preparar() {
        // [Doc] Ejecuta la llamada o modificación jdbc.execute("TRUNCATE usuario, producto, correo_pendiente, pedido_historial, producto_alerta_stock RESTART IDENTITY CASCADE"); Sus efectos dependen del método invocado y del objeto receptor.
        jdbc.execute("TRUNCATE usuario, producto, correo_pendiente, pedido_historial, producto_alerta_stock RESTART IDENTITY CASCADE");
        // [Doc] Ejecuta la llamada o modificación admin = usuario("admin"); Sus efectos dependen del método invocado y del objeto receptor.
        admin = usuario("admin"); titular = usuario("titular"); ajeno = usuario("ajeno");
        // [Doc] Ejecuta la llamada o modificación producto = producto(10); Sus efectos dependen del método invocado y del objeto receptor.
        producto = producto(10);
        // [Doc] Ejecuta la llamada o modificación reset(smtp, cola); Sus efectos dependen del método invocado y del objeto receptor.
        reset(smtp, cola);
    }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: usuario.
     * @param nombre nombre normalizado o visible de la entidad.
     * @return Usuario con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    Usuario usuario(String nombre) {
        // [Doc] Variable local u (Usuario): cuenta de usuario utilizada por la operación. Se inicializa con new Usuario().
        Usuario u = new Usuario(); u.setNombre_usuario(nombre); u.setEmail(nombre + "@example.invalid");
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: u.setContraseña("ficticia");
        u.setContraseña("ficticia"); return usuarios.save(u);
    }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: producto.
     * @param stock unidades disponibles del producto.
     * @return Producto con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    Producto producto(int stock) {
        // [Doc] Variable local p (Producto): producto consultado para esta operación. Se inicializa con new Producto().
        Producto p = new Producto(); p.setNombre_producto("Prueba"); p.setStock(stock);
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: p.setPrecio(12.5f);
        p.setPrecio(12.5f); p.setEstado("activo"); return productos.save(p);
    }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: comprar.
     * @param cantidad unidades de una línea de carrito.
     * @return Pedido con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    Pedido comprar(int cantidad) { return pedidos.comprar(titular.getId(), Map.of(producto.getIdProducto(), cantidad)); }

    @Test void filtroAdministrativoPorFechasIncluyeLimitesYPagina() throws Exception {
        for (String fecha : List.of("2026-10-31 23:59:59", "2026-11-01 00:00:00",
                "2026-11-02 23:59:59.999", "2026-11-03 00:00:00")) {
            Pedido pedido = comprar(1);
            jdbc.update("UPDATE pedido SET fecha_pedido = ? WHERE id_pedido = ?",
                    java.sql.Timestamp.valueOf(fecha), pedido.getId_pedido());
        }
        var session = sesion(admin);
        mvc.perform(get("/admin/pedidos/lista").session(session)
                        .param("desde", "2026-11-01").param("hasta", "2026-11-02").param("size", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2)).andExpect(jsonPath("$.content.length()").value(1));
        mvc.perform(get("/admin/pedidos/lista").session(session).param("desde", "2026-11-02"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));
        mvc.perform(get("/admin/pedidos/lista").session(session).param("hasta", "2026-11-01"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));
        mvc.perform(get("/admin/pedidos/lista").session(session)
                        .param("desde", "2026-11-02").param("hasta", "2026-11-02"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/admin/pedidos/lista").session(session))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(4));
        mvc.perform(get("/admin/pedidos/lista").session(session)
                        .param("desde", "2026-12-01").param("hasta", "2026-12-02"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/admin/pedidos/lista").session(session)
                        .param("desde", "2026-11-01").param("hasta", "2026-11-02")
                        .param("estado", "PREPARANDO").param("usuario", "titular"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));
        mvc.perform(get("/admin/pedidos/lista").session(session)
                        .param("desde", "2026-11-03").param("hasta", "2026-11-01"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value(
                        "La fecha Desde debe ser anterior o igual a Hasta."));
    }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: stock.
     * @return int con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    int stock() { return productos.findById(producto.getIdProducto()).orElseThrow().getStock(); }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: correos.
     * @return int con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    int correos() { return jdbc.queryForObject("SELECT count(*) FROM correo_pendiente", Integer.class); }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: sesion.
     * @param u cuenta de usuario utilizada por la operación.
     * @return MockHttpSession con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    MockHttpSession sesion(Usuario u) {
        // [Doc] Variable local s (MockHttpSession): s. Se inicializa con new MockHttpSession().
        MockHttpSession s = new MockHttpSession(); s.setAttribute("usuario", u); s.setAttribute(CsrfInterceptor.KEY, "prueba-csrf"); return s;
    }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: paralelos.
     * @param a a.
     * @param b b.
     * @return List&lt;Object&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    List<Object> paralelos(Callable<?> a, Callable<?> b) throws Exception {
        // [Doc] Ejecuta un bloque con tratamiento de excepciones; los catch asociados atienden los fallos declarados y finally se ejecuta al salir si existe.
        try (var pool = Executors.newVirtualThreadPerTaskExecutor()) {
            // [Doc] Variable local inicio (CountDownLatch): límite inicial del intervalo. Se inicializa con new CountDownLatch(1).
            CountDownLatch listos = new CountDownLatch(2), inicio = new CountDownLatch(1);
            // [Doc] Variable local futuros (List<Future<Object>>): futuros. Se inicializa con new ArrayList<>().
            List<Future<Object>> futuros = new ArrayList<>();
            // [Doc] Recorre los elementos o repite según Callable<?> tarea : List.of(a, b). Cada vuelta ejecuta el cuerpo del bucle.
            for (Callable<?> tarea : List.of(a, b)) futuros.add(pool.submit(() -> {
                // [Doc] Ejecuta la llamada o modificación listos.countDown(); Sus efectos dependen del método invocado y del objeto receptor.
                listos.countDown(); inicio.await();
                // [Doc] Ejecuta un bloque con tratamiento de excepciones; los catch asociados atienden los fallos declarados y finally se ejecuta al salir si existe.
                try { return tarea.call(); } catch (Exception ex) { return ex; }
            }));
            // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(listos.await(5, TimeUnit.SECONDS));
            assertTrue(listos.await(5, TimeUnit.SECONDS)); inicio.countDown();
            // [Doc] Termina el método o la función y devuelve List.of(futuros.get(0).get(20, TimeUnit.SECONDS), futuros.get(1).get(20, TimeUnit.SECONDS)).
            return List.of(futuros.get(0).get(20, TimeUnit.SECONDS), futuros.get(1).get(20, TimeUnit.SECONDS));
        }
    }

    /** [Doc]
     * Comprueba el escenario «compra atomica yreintento del mismo carrito» mediante las aserciones del cuerpo.
     */
    @Test void compraAtomicaYReintentoDelMismoCarrito() {
        // [Doc] Variable local p (Pedido): pedido consultado para esta operación. Se inicializa con comprar(2).
        Pedido p = comprar(2);
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(8, stock());
        assertEquals(8, stock()); assertEquals(2, avisos.count()); assertEquals(2, correos());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(p.getId_pedido(), pedidos.finalizarCompra(titular.getId(), p.getCarrito().getIdCarrito()).getId_pedido());
        assertEquals(p.getId_pedido(), pedidos.finalizarCompra(titular.getId(), p.getCarrito().getIdCarrito()).getId_pedido());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(8, stock());
        assertEquals(8, stock()); assertEquals(2, avisos.count()); assertEquals(2, correos());
        // [Doc] Ejecuta la llamada o modificación verifyNoInteractions(smtp); Sus efectos dependen del método invocado y del objeto receptor.
        verifyNoInteractions(smtp);
    }
    /** [Doc]
     * Comprueba el escenario «fallo de stock revierte carrito detalles ydescuentos» mediante las aserciones del
     * cuerpo.
     */
    @Test void falloDeStockRevierteCarritoDetallesYDescuentos() {
        // [Doc] Variable local sinStock (Producto): producto procesado en este punto. Se inicializa con producto(0).
        Producto sinStock = producto(0);
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertThrows(IllegalStateException.class, () -> pedidos.comprar(titular.getId(), Map.of(producto.getIdProducto(), 2, sinStock.getIdProducto(), 1)));
        assertThrows(IllegalStateException.class, () -> pedidos.comprar(titular.getId(),
                Map.of(producto.getIdProducto(), 2, sinStock.getIdProducto(), 1)));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(10, stock());
        assertEquals(10, stock()); assertEquals(0, pedidoRepo.count()); assertEquals(0, correos());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM carrito_de_compra", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM carrito_de_compra", Integer.class));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM detalle_carrito", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM detalle_carrito", Integer.class));
    }
    /** [Doc]
     * Comprueba el escenario «fallo de avisos revierte la compra completa» mediante las aserciones del cuerpo.
     */
    @Test void falloDeAvisosRevierteLaCompraCompleta() {
        // [Doc] Ejecuta la llamada o modificación doThrow(new IllegalStateException("Fallo simulado de persistencia")).when(cola).encolar(anyString(), anyString(), anyString()); Sus efectos dependen del método invocado y del objeto receptor.
        doThrow(new IllegalStateException("Fallo simulado de persistencia")).when(cola).encolar(anyString(), anyString(), anyString());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertThrows(IllegalStateException.class, () -> comprar(2));
        assertThrows(IllegalStateException.class, () -> comprar(2));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(10, stock());
        assertEquals(10, stock()); assertEquals(0, pedidoRepo.count()); assertEquals(0, avisos.count());
    }
    /** [Doc]
     * Comprueba el escenario «solicitud repetida yconfirmaciones concurrentes reponen una vez» mediante las
     * aserciones del cuerpo.
     */
    @Test void solicitudRepetidaYConfirmacionesConcurrentesReponenUnaVez() throws Exception {
        // [Doc] Variable local p (Pedido): pedido consultado para esta operación. Se inicializa con comprar(3).
        Pedido p = comprar(3);
        // [Doc] Ejecuta la llamada o modificación pedidos.solicitarCancelacion(p.getId_pedido(), titular.getId()); Sus efectos dependen del método invocado y del objeto receptor.
        pedidos.solicitarCancelacion(p.getId_pedido(), titular.getId());
        // [Doc] Ejecuta la llamada o modificación pedidos.solicitarCancelacion(p.getId_pedido(), titular.getId()); Sus efectos dependen del método invocado y del objeto receptor.
        pedidos.solicitarCancelacion(p.getId_pedido(), titular.getId());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(7, stock());
        assertEquals(7, stock()); assertEquals(3, avisos.count()); assertEquals(3, correos());
        // [Doc] Variable local resultados (var; tipo inferido por Java): resultados. Se inicializa con paralelos(() -> pedidos.confirmarCancelacion(p.getId_pedido()), () -> pedidos.confirmarCancelacion(p.getId_....
        var resultados = paralelos(() -> pedidos.confirmarCancelacion(p.getId_pedido()), () -> pedidos.confirmarCancelacion(p.getId_pedido()));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(resultados.stream().allMatch(Pedido.class::isInstance), resultados.toString());
        assertTrue(resultados.stream().allMatch(Pedido.class::isInstance), resultados.toString());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(10, stock());
        assertEquals(10, stock()); assertEquals(5, avisos.count()); assertEquals(5, correos());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertThrows(IllegalStateException.class, () -> pedidos.cambiarEstado(p.getId_pedido(), "PREPARANDO"));
        assertThrows(IllegalStateException.class, () -> pedidos.cambiarEstado(p.getId_pedido(), "PREPARANDO"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(10, stock());
        assertEquals(10, stock());
    }
    /** [Doc]
     * Comprueba el escenario «entregado no puede cancelarse ni reponerse» mediante las aserciones del cuerpo.
     */
    @Test void entregadoNoPuedeCancelarseNiReponerse() {
        // [Doc] Variable local p (Pedido): pedido consultado para esta operación. Se inicializa con comprar(2).
        Pedido p = comprar(2); pedidos.marcarEntregado(p.getId_pedido());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertThrows(IllegalStateException.class, () -> pedidos.cambiarEstado(p.getId_pedido(), "CANCELADO"));
        assertThrows(IllegalStateException.class, () -> pedidos.cambiarEstado(p.getId_pedido(), "CANCELADO"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertThrows(IllegalStateException.class, () -> pedidos.solicitarCancelacion(p.getId_pedido(), titular.getId()));
        assertThrows(IllegalStateException.class, () -> pedidos.solicitarCancelacion(p.getId_pedido(), titular.getId()));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(8, stock());
        assertEquals(8, stock());
    }
    /** [Doc]
     * Comprueba el escenario «fallo al notificar cancelacion revierte stock yestado» mediante las aserciones del
     * cuerpo.
     */
    @Test void falloAlNotificarCancelacionRevierteStockYEstado() {
        // [Doc] Variable local pedido (Pedido): pedido procesado en este punto. Se inicializa con comprar(2).
        Pedido pedido = comprar(2);
        // [Doc] Ejecuta la llamada o modificación doThrow(new IllegalStateException("Fallo simulado")).when(cola).encolar(anyString(), anyString(), anyString()); Sus efectos dependen del método invocado y del objeto receptor.
        doThrow(new IllegalStateException("Fallo simulado")).when(cola).encolar(anyString(), anyString(), anyString());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertThrows(IllegalStateException.class, () -> pedidos.cambiarEstado(pedido.getId_pedido(), "CANCELADO"));
        assertThrows(IllegalStateException.class, () -> pedidos.cambiarEstado(pedido.getId_pedido(), "CANCELADO"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(8, stock());
        assertEquals(8, stock());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals("PREPARANDO", pedidos.findPedido(pedido.getId_pedido()).getEstado_pedido());
        assertEquals("PREPARANDO", pedidos.findPedido(pedido.getId_pedido()).getEstado_pedido());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(2, avisos.count());
        assertEquals(2, avisos.count()); assertEquals(2, correos());
    }
    /** [Doc]
     * Comprueba el escenario «carrito historico no se reutiliza ni se modifica» mediante las aserciones del cuerpo.
     */
    @Test void carritoHistoricoNoSeReutilizaNiSeModifica() {
        // [Doc] Variable local pedido (Pedido): pedido procesado en este punto. Se inicializa con comprar(2).
        Pedido pedido = comprar(2);
        // [Doc] Variable local historico (long): historico. Se inicializa con pedido.getCarrito().getIdCarrito().
        long historico = pedido.getCarrito().getIdCarrito();
        // [Doc] Variable local activo (var; tipo inferido por Java): activo. Se inicializa con carritos.getOrCreateCarrito(sesion(titular)).
        var activo = carritos.getOrCreateCarrito(sesion(titular));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertNotEquals(historico, activo.getIdCarrito());
        assertNotEquals(historico, activo.getIdCarrito());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertThrows(IllegalStateException.class, () -> carritos.cambiarCantidad(historico, producto.getIdProducto(), 9));
        assertThrows(IllegalStateException.class, () -> carritos.cambiarCantidad(historico, producto.getIdProducto(), 9));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertThrows(IllegalStateException.class, () -> detalles.setCantidad(historico, producto.getIdProducto(), 9));
        assertThrows(IllegalStateException.class, () -> detalles.setCantidad(historico, producto.getIdProducto(), 9));
        // [Doc] Ejecuta la llamada o modificación pedidos.cambiarEstado(pedido.getId_pedido(), "CANCELADO"); Sus efectos dependen del método invocado y del objeto receptor.
        pedidos.cambiarEstado(pedido.getId_pedido(), "CANCELADO");
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(10, stock());
        assertEquals(10, stock());
    }
    /** [Doc]
     * Comprueba el escenario «fusion de invitado conserva cantidades yprecio» mediante las aserciones del cuerpo.
     */
    @Test void fusionDeInvitadoConservaCantidadesYPrecio() {
        // [Doc] Variable local invitado (var; tipo inferido por Java): invitado. Se inicializa con carritos.getOrCreateCarrito(new MockHttpSession()).
        var invitado = carritos.getOrCreateCarrito(new MockHttpSession());
        // [Doc] Ejecuta la llamada o modificación carritos.agregarDetalle(invitado.getIdCarrito(), producto.getIdProducto(), 2); Sus efectos dependen del método invocado y del objeto receptor.
        carritos.agregarDetalle(invitado.getIdCarrito(), producto.getIdProducto(), 2);
        // [Doc] Variable local activo (var; tipo inferido por Java): activo. Se inicializa con carritos.getOrCreateCarrito(sesion(titular)).
        var activo = carritos.getOrCreateCarrito(sesion(titular));
        // [Doc] Ejecuta la llamada o modificación carritos.mergeCarritos(invitado.getIdCarrito(), activo.getIdCarrito()); Sus efectos dependen del método invocado y del objeto receptor.
        carritos.mergeCarritos(invitado.getIdCarrito(), activo.getIdCarrito());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertFalse(carritoRepo.existsById(invitado.getIdCarrito()));
        assertFalse(carritoRepo.existsById(invitado.getIdCarrito()));
        // [Doc] Variable local lineas (var; tipo inferido por Java): lineas. Se inicializa con detalles.listarPorCarrito(activo.getIdCarrito()).
        var lineas = detalles.listarPorCarrito(activo.getIdCarrito());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(1, lineas.size());
        assertEquals(1, lineas.size()); assertEquals(2, lineas.get(0).getCantidad());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(12.5f, lineas.get(0).getPrecio());
        assertEquals(12.5f, lineas.get(0).getPrecio());
        // [Doc] Ejecuta la llamada o modificación pedidos.finalizarCompra(titular.getId(), activo.getIdCarrito()); Sus efectos dependen del método invocado y del objeto receptor.
        pedidos.finalizarCompra(titular.getId(), activo.getIdCarrito());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(8, stock());
        assertEquals(8, stock()); assertEquals(2, correos());
    }
    /** [Doc]
     * Comprueba el escenario «dos compras compiten por la ultima unidad» mediante las aserciones del cuerpo.
     */
    @Test void dosComprasCompitenPorLaUltimaUnidad() throws Exception {
        // [Doc] Actualiza el stock de la entidad en memoria; la persistencia se realiza en la transacción: producto.setStock(1);
        producto.setStock(1); productos.save(producto);
        // [Doc] Variable local resultados (var; tipo inferido por Java): resultados. Se inicializa con paralelos(() -> comprar(1), () -> comprar(1)).
        var resultados = paralelos(() -> comprar(1), () -> comprar(1));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(1, resultados.stream().filter(Pedido.class::isInstance).count(), resultados.toString());
        assertEquals(1, resultados.stream().filter(Pedido.class::isInstance).count(), resultados.toString());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(0, stock());
        assertEquals(0, stock()); assertEquals(1, pedidoRepo.count()); assertEquals(3, correos());
    }
    /** [Doc]
     * Comprueba el escenario «lectura concurrente es idempotente yprotege destinatario» mediante las aserciones del
     * cuerpo.
     */
    @Test void lecturaConcurrenteEsIdempotenteYProtegeDestinatario() throws Exception {
        // [Doc] Ejecuta la llamada o modificación comprar(1); Sus efectos dependen del método invocado y del objeto receptor.
        comprar(1);
        // [Doc] Variable local id (long): identificador del registro. Se inicializa con avisos.listarUltimas("USUARIO_" + titular.getId(), org.springframework.data.domain.PageRequest.of(0, 1)).ge....
        long id = avisos.listarUltimas("USUARIO_" + titular.getId(), org.springframework.data.domain.PageRequest.of(0, 1)).get(0).getId_notificacion();
        // [Doc] Variable local destinatario (String): identidad o dirección que recibirá el aviso o correo. Se inicializa con "USUARIO_" + titular.getId().
        String destinatario = "USUARIO_" + titular.getId();
        // [Doc] Variable local resultado (var; tipo inferido por Java): colección o valor que acumula el resultado de la operación. Se inicializa con paralelos(() -> { notificaciones.marcarComoLeida(id, destinatario); return true; }, () -> { notificaciones.....
        var resultado = paralelos(() -> { notificaciones.marcarComoLeida(id, destinatario); return true; },
                () -> { notificaciones.marcarTodasComoLeidas(destinatario); return true; });
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(List.of(true, true), resultado);
        assertEquals(List.of(true, true), resultado);
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(0, notificaciones.contarNoLeidas(destinatario));
        assertEquals(0, notificaciones.contarNoLeidas(destinatario));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM notificacion_lectura", Integer.class));
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM notificacion_lectura", Integer.class));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertThrows(IllegalArgumentException.class, () -> notificaciones.marcarComoLeida(id, "USUARIO_" + ajeno.getId()));
        assertThrows(IllegalArgumentException.class, () -> notificaciones.marcarComoLeida(id, "USUARIO_" + ajeno.getId()));
    }
    /** [Doc]
     * Comprueba el escenario «smtp fallido se reintenta sin duplicar avisos» mediante las aserciones del cuerpo.
     */
    @Test void smtpFallidoSeReintentaSinDuplicarAvisos() {
        // [Doc] Ejecuta la llamada o modificación comprar(1); Sus efectos dependen del método invocado y del objeto receptor.
        comprar(1);
        // [Doc] Ejecuta la llamada o modificación doThrow(new MailSendException("Simulado")).when(smtp).send(any(SimpleMailMessage.class)); Sus efectos dependen del método invocado y del objeto receptor.
        doThrow(new MailSendException("Simulado")).when(smtp).send(any(SimpleMailMessage.class));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(entrega.entregarSiguiente());
        assertTrue(entrega.entregarSiguiente());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(1, jdbc.queryForObject("SELECT sum(intentos) FROM correo_pendiente", Integer.class));
        assertEquals(1, jdbc.queryForObject("SELECT sum(intentos) FROM correo_pendiente", Integer.class));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM correo_pendiente WHERE enviado_en IS NOT NULL", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM correo_pendiente WHERE enviado_en IS NOT NULL", Integer.class));
        // [Doc] Ejecuta la llamada o modificación doNothing().when(smtp).send(any(SimpleMailMessage.class)); Sus efectos dependen del método invocado y del objeto receptor.
        doNothing().when(smtp).send(any(SimpleMailMessage.class));
        // [Doc] Ejecuta una modificación SQL con los parámetros indicados; participa en la transacción si existe.
        jdbc.update("UPDATE correo_pendiente SET proximo_intento=CURRENT_TIMESTAMP");
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(entrega.entregarSiguiente());
        assertTrue(entrega.entregarSiguiente()); assertTrue(entrega.entregarSiguiente()); assertFalse(entrega.entregarSiguiente());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(2, avisos.count());
        assertEquals(2, avisos.count()); assertEquals(2, correos());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(2, jdbc.queryForObject("SELECT count(*) FROM correo_pendiente WHERE enviado_en IS NOT NULL", Integer.class));
        assertEquals(2, jdbc.queryForObject("SELECT count(*) FROM correo_pendiente WHERE enviado_en IS NOT NULL", Integer.class));
    }
    /** [Doc]
     * Comprueba el escenario «seguridad http protege pedidos admin perfil ycsr f» mediante las aserciones del
     * cuerpo.
     */
    @Test void seguridadHttpProtegePedidosAdminPerfilYCsrF() throws Exception {
        // [Doc] Variable local p (Pedido): pedido consultado para esta operación. Se inicializa con comprar(1).
        Pedido p = comprar(1); long id = p.getId_pedido();
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/admin/pedidos/lista")).andExpect(status().isUnauthorized()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/admin/pedidos/lista")).andExpect(status().isUnauthorized());
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/admin/pedidos/lista").session(sesion(titular))).andExpect(status().isForbidden()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/admin/pedidos/lista").session(sesion(titular))).andExpect(status().isForbidden());
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/pedido/" + id + "/pdf").session(sesion(ajeno))).andExpect(status().isForbidden()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/pedido/" + id + "/pdf").session(sesion(ajeno))).andExpect(status().isForbidden());
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/pedido/" + id).session(sesion(titular))).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id)); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/pedido/" + id).session(sesion(titular))).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/pedido/" + id + "/confirmar-cancelacion").session(sesion(titular)).header("X-CSRF-TOKEN", "prueba-csrf")) .andExpect(status().isForbidden()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/pedido/" + id + "/confirmar-cancelacion").session(sesion(titular)).header("X-CSRF-TOKEN", "prueba-csrf"))
                .andExpect(status().isForbidden());
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/pedido/" + id + "/cancelar").session(sesion(titular))).andExpect(status().isForbidden()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/pedido/" + id + "/cancelar").session(sesion(titular))).andExpect(status().isForbidden());
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/panelusuarios/perfil/editar").session(sesion(titular)).header("X-CSRF-TOKEN", "prueba-csrf") .param("nombre", "admin").param("email", titular.getEma... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/panelusuarios/perfil/editar").session(sesion(titular)).header("X-CSRF-TOKEN", "prueba-csrf")
                .param("nombre", "admin").param("email", titular.getEmail())).andExpect(status().isBadRequest());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals("titular", usuarios.findById(titular.getId()).orElseThrow().getNombre_usuario());
        assertEquals("titular", usuarios.findById(titular.getId()).orElseThrow().getNombre_usuario());
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/pedido/" + id + "/cancelar").session(sesion(titular)).header("X-CSRF-TOKEN", "prueba-csrf")) .andExpect(status().isOk()).andExpect(jsonPath("$.ok").... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/pedido/" + id + "/cancelar").session(sesion(titular)).header("X-CSRF-TOKEN", "prueba-csrf"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ok").value(true));
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/panelusuarios/" + id + "/cancelar").session(sesion(titular)).header("X-CSRF-TOKEN", "prueba-csrf")) .andExpect(status().isOk()).andExpect(jsonPath("... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/panelusuarios/" + id + "/cancelar").session(sesion(titular)).header("X-CSRF-TOKEN", "prueba-csrf"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ok").value(true));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(3, avisos.count());
        assertEquals(3, avisos.count());
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/pedido/" + id + "/confirmar-cancelacion").session(sesion(admin)).header("X-CSRF-TOKEN", "prueba-csrf")) .andExpect(status().isOk()).andExpect(jsonPa... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/pedido/" + id + "/confirmar-cancelacion").session(sesion(admin)).header("X-CSRF-TOKEN", "prueba-csrf"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ok").value(true));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(10, stock());
        assertEquals(10, stock());
    }
    /** [Doc]
     * Comprueba el escenario «checkout concurrente de una sesion solo crea un pedido» mediante las aserciones del
     * cuerpo.
     */
    @Test void checkoutConcurrenteDeUnaSesionSoloCreaUnPedido() throws Exception {
        // [Doc] Variable local session (MockHttpSession): sesión HTTP que conserva identidad, carrito y token CSRF. Se inicializa con sesion(titular).
        MockHttpSession session = sesion(titular);
        // [Doc] Variable local cart (CartSession): carrito en memoria de la sesión. Se inicializa con new CartSession().
        CartSession cart = new CartSession(); cart.add(producto.getIdProducto(), 2);
        // [Doc] Almacena un atributo en la sesión o petición: session.setAttribute(CartSessionUtil.KEY, cart);
        session.setAttribute(CartSessionUtil.KEY, cart);
        // [Doc] Variable local compra (Callable<Integer>): compra. Se inicializa con () -> mvc.perform(post("/carrito/finalizar").session(session) .header("X-CSRF-TOKEN", "prueba-csrf")).andRe....
        Callable<Integer> compra = () -> mvc.perform(post("/carrito/finalizar").session(session)
                .header("X-CSRF-TOKEN", "prueba-csrf")).andReturn().getResponse().getStatus();
        // [Doc] Variable local resultados (var; tipo inferido por Java): resultados. Se inicializa con paralelos(compra, compra).
        var resultados = paralelos(compra, compra);
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(resultados.stream().allMatch(Integer.class::isInstance), resultados.toString());
        assertTrue(resultados.stream().allMatch(Integer.class::isInstance), resultados.toString());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(1, pedidoRepo.count());
        assertEquals(1, pedidoRepo.count()); assertEquals(8, stock()); assertEquals(2, correos());
    }
    /** [Doc]
     * Comprueba el escenario «vistas renderizan token ycuentas no se escalan por nombre de sesion» mediante las
     * aserciones del cuerpo.
     */
    @Test void vistasRenderizanTokenYCuentasNoSeEscalanPorNombreDeSesion() throws Exception {
        // [Doc] Variable local session (var; tipo inferido por Java): sesión HTTP que conserva identidad, carrito y token CSRF. Se inicializa con sesion(titular).
        var session = sesion(titular);
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/auth/login").session(session)).andExpect(status().isOk()) .andExpect(content().string(org.hamcrest.Matchers.containsString("content=\"prueba-csrf\""))); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/auth/login").session(session)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("content=\"prueba-csrf\"")));
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: titular.setNombre_usuario("admin");
        titular.setNombre_usuario("admin"); session.setAttribute("usuario", titular);
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/admin/pedidos/lista").session(session)).andExpect(status().isForbidden()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/admin/pedidos/lista").session(session)).andExpect(status().isForbidden());
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/notificaciones/mias").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.ok").value(true)); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/notificaciones/mias").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.ok").value(true));
    }
    /** [Doc]
     * Comprueba el escenario «registro encola bienvenida yaviso sin enviar smtp» mediante las aserciones del cuerpo.
     */
    @Test void registroEncolaBienvenidaYAvisoSinEnviarSmtp() throws Exception {
        // [Doc] Variable local localidad (Localidad): localidad asociada a la cuenta. Se inicializa con new Localidad().
        Localidad localidad = new Localidad(); localidad.setNombre_localidad("Localidad ficticia");
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: localidad.setCodigo_postal(3000);
        localidad.setCodigo_postal(3000); localidades.save(localidad);
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/auth/registro").session(sesion(titular)).header("X-CSRF-TOKEN", "prueba-csrf") .param("nombre_usuario", "nueva").param("apellido", "Prueba").param("... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/auth/registro").session(sesion(titular)).header("X-CSRF-TOKEN", "prueba-csrf")
                .param("nombre_usuario", "nueva").param("apellido", "Prueba").param("email", "nueva@example.invalid")
                .param("dni", "11111111").param("direccion", "Domicilio ficticio")
                .param("localidadId", String.valueOf(localidad.getId_localidad())).param("contraseña", "ficticia")
                .param("confirmarContraseña", "ficticia").param("aceptarTerminos", "true"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/auth/login?registroExitoso=true"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(usuarios.existeNombreUsuario("nueva"));
        assertTrue(usuarios.existeNombreUsuario("nueva")); assertEquals(1, avisos.count()); assertEquals(2, correos());
        // [Doc] Ejecuta la llamada o modificación verifyNoInteractions(smtp); Sus efectos dependen del método invocado y del objeto receptor.
        verifyNoInteractions(smtp);
    }
    /** [Doc]
     * Comprueba el escenario «paneles ycatalogo renderizan con csrf ypermisos» mediante las aserciones del cuerpo.
     */
    @Test void panelesYCatalogoRenderizanConCsrfYPermisos() throws Exception {
        // [Doc] Ejecuta la llamada o modificación comprar(2); Sus efectos dependen del método invocado y del objeto receptor.
        comprar(2);
        // [Doc] Recorre los elementos o repite según String ruta : List.of("/panelusuarios/panelhistorial", "/carrito/vercarrito", "/productos"). Cada vuelta ejecuta el cuerpo del bucle.
        for (String ruta : List.of("/panelusuarios/panelhistorial", "/carrito/vercarrito", "/productos"))
            // [Doc] Ejecuta la llamada o modificación mvc.perform(get(ruta).session(sesion(titular))).andExpect(status().isOk()) .andExpect(content().string(org.hamcrest.Matchers.containsString("content=\"prueba-csrf\""))); Sus efectos dependen del método invocado y del objeto receptor.
            mvc.perform(get(ruta).session(sesion(titular))).andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("content=\"prueba-csrf\"")));
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/admin/usuarios").session(sesion(admin))).andExpect(status().isOk()) .andExpect(content().string(org.hamcrest.Matchers.containsString("content=\"prueb... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/admin/usuarios").session(sesion(admin))).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("content=\"prueba-csrf\"")));
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/pedido/" + pedidoRepo.findAll().get(0).getId_pedido() + "/pdf").session(sesion(titular))) .andExpect(status().isOk()).andExpect(content().contentType... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/pedido/" + pedidoRepo.findAll().get(0).getId_pedido() + "/pdf").session(sesion(titular)))
                .andExpect(status().isOk()).andExpect(content().contentType("application/pdf"));
    }

    // [Doc] Atributo centro (CentroAvisosService): colaborador que concentra operaciones de centro avisos.
    @Autowired CentroAvisosService centro;
    // [Doc] Atributo txManager (org.springframework.transaction.PlatformTransactionManager): tx manager.
    @Autowired org.springframework.transaction.PlatformTransactionManager txManager;

    /** [Doc]
     * Comprueba el escenario «historial pagina filtra yaisla destinatarios» mediante las aserciones del cuerpo.
     */
    @Test void historialPaginaFiltraYAislaDestinatarios() throws Exception {
        // [Doc] Recorre los elementos o repite según int i=0;i<25;i++. Cada vuelta ejecuta el cuerpo del bucle.
        for(int i=0;i<25;i++) jdbc.update("INSERT INTO notificacion(destinatario,descripcion,fecha_notificacion) VALUES (?,?,CURRENT_TIMESTAMP)", "USUARIO_"+titular.getId(), "Aviso propio "+i);
        // [Doc] Ejecuta una modificación SQL con los parámetros indicados; participa en la transacción si existe.
        jdbc.update("INSERT INTO notificacion(destinatario,descripcion,fecha_notificacion) VALUES ('ADMIN','Solo admin',CURRENT_TIMESTAMP)");
        // [Doc] Ejecuta una modificación SQL con los parámetros indicados; participa en la transacción si existe.
        jdbc.update("INSERT INTO notificacion(destinatario,descripcion,fecha_notificacion) VALUES (?, 'Solo ajeno',CURRENT_TIMESTAMP)", "USUARIO_"+ajeno.getId());
        // [Doc] Variable local pagina (var; tipo inferido por Java): índice de página solicitado, comenzando en cero. Se inicializa con centro.notificaciones("USUARIO_"+titular.getId(),"TODAS",0).
        var pagina=centro.notificaciones("USUARIO_"+titular.getId(),"TODAS",0);
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(25,pagina.getTotalElements());
        assertEquals(25,pagina.getTotalElements()); assertEquals(20,pagina.getNumberOfElements());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(5,centro.notificaciones("USUARIO_"+titular.getId(),"TODAS",1).getNumberOfElements());
        assertEquals(5,centro.notificaciones("USUARIO_"+titular.getId(),"TODAS",1).getNumberOfElements());
        // [Doc] Variable local id (long): identificador del registro. Se inicializa con ((Number)pagina.getContent().get(0).get("id")).longValue().
        long id=((Number)pagina.getContent().get(0).get("id")).longValue();
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/notificaciones/historial/"+id+"/leer").session(sesion(ajeno)).param("_csrf","prueba-csrf")).andExpect(status().isNotFound()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/notificaciones/historial/"+id+"/leer").session(sesion(ajeno)).param("_csrf","prueba-csrf")).andExpect(status().isNotFound());
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/notificaciones/historial/"+id+"/leer").session(sesion(titular))).andExpect(status().isForbidden()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/notificaciones/historial/"+id+"/leer").session(sesion(titular))).andExpect(status().isForbidden());
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/notificaciones/historial/"+id+"/leer").session(sesion(titular)).param("_csrf","prueba-csrf")).andExpect(status().is3xxRedirection()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/notificaciones/historial/"+id+"/leer").session(sesion(titular)).param("_csrf","prueba-csrf")).andExpect(status().is3xxRedirection());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(1,centro.notificaciones("USUARIO_"+titular.getId(),"LEIDAS",0).getTotalElements());
        assertEquals(1,centro.notificaciones("USUARIO_"+titular.getId(),"LEIDAS",0).getTotalElements());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(24,centro.notificaciones("USUARIO_"+titular.getId(),"NO_LEIDAS",0).getTotalElements());
        assertEquals(24,centro.notificaciones("USUARIO_"+titular.getId(),"NO_LEIDAS",0).getTotalElements());
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/notificaciones/historial").session(sesion(titular))) .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Ma... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/notificaciones/historial").session(sesion(titular)))
            .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Solo admin"))))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Siguiente")));
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/notificaciones/historial").session(sesion(admin))).andExpect(status().isOk()) .andExpect(content().string(org.hamcrest.Matchers.containsString("Solo ... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/notificaciones/historial").session(sesion(admin))).andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Solo admin")));
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/notificaciones/historial/leer-todas").session(sesion(titular)).param("_csrf","prueba-csrf")).andExpect(status().is3xxRedirection()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/notificaciones/historial/leer-todas").session(sesion(titular)).param("_csrf","prueba-csrf")).andExpect(status().is3xxRedirection());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(0,notificaciones.contarNoLeidas("USUARIO_"+titular.getId()));
        assertEquals(0,notificaciones.contarNoLeidas("USUARIO_"+titular.getId()));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(1,notificaciones.contarNoLeidas("ADMIN"));
        assertEquals(1,notificaciones.contarNoLeidas("ADMIN"));
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/notificaciones/historial")).andExpect(status().isUnauthorized()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/notificaciones/historial")).andExpect(status().isUnauthorized());
    }

    /** [Doc]
     * Comprueba el escenario «panel correos protege datos yreintentos» mediante las aserciones del cuerpo.
     */
    @Test void panelCorreosProtegeDatosYReintentos() throws Exception {
        // [Doc] Ejecuta la llamada o modificación cola.encolar("destino@example.invalid","Asunto prueba","Texto"); Sus efectos dependen del método invocado y del objeto receptor.
        cola.encolar("destino@example.invalid","Asunto prueba","Texto");
        // [Doc] Variable local id (long): identificador del registro. Se inicializa con jdbc.queryForObject("SELECT id FROM correo_pendiente",Long.class).
        long id=jdbc.queryForObject("SELECT id FROM correo_pendiente",Long.class);
        // [Doc] Ejecuta una modificación SQL con los parámetros indicados; participa en la transacción si existe.
        jdbc.update("UPDATE correo_pendiente SET intentos=2,proximo_intento=CURRENT_TIMESTAMP+INTERVAL '1 hour' WHERE id=?",id);
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/admin/correos").session(sesion(titular))).andExpect(status().isForbidden()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/admin/correos").session(sesion(titular))).andExpect(status().isForbidden());
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/admin/correos/"+id+"/reintentar").session(sesion(titular)).param("_csrf","prueba-csrf")).andExpect(status().isForbidden()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/admin/correos/"+id+"/reintentar").session(sesion(titular)).param("_csrf","prueba-csrf")).andExpect(status().isForbidden());
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/admin/correos/"+id+"/reintentar").session(sesion(admin))).andExpect(status().isForbidden()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/admin/correos/"+id+"/reintentar").session(sesion(admin))).andExpect(status().isForbidden());
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/admin/correos").session(sesion(admin)).param("estado","REINTENTO").param("q","ASUNTO")) .andExpect(status().isOk()).andExpect(content().string(org.ha... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/admin/correos").session(sesion(admin)).param("estado","REINTENTO").param("q","ASUNTO"))
            .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Reintentar ahora")));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(0,centro.correos("TODOS","%' OR 1=1 --",0).getTotalElements());
        assertEquals(0,centro.correos("TODOS","%' OR 1=1 --",0).getTotalElements());
        // [Doc] Variable local resultados (var; tipo inferido por Java): resultados. Se inicializa con paralelos(()->centro.reintentar(id),()->centro.reintentar(id)).
        var resultados=paralelos(()->centro.reintentar(id),()->centro.reintentar(id));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(1,resultados.stream().filter(Boolean.TRUE::equals).count());
        assertEquals(1,resultados.stream().filter(Boolean.TRUE::equals).count());
        // Una transacción antigua no debe reprogramar una fila adelantada por otra más nueva.
        // [Doc] Ejecuta una modificación SQL con los parámetros indicados; participa en la transacción si existe.
        jdbc.update("UPDATE correo_pendiente SET proximo_intento=CURRENT_TIMESTAMP+INTERVAL '1 hour' WHERE id=?",id);
        // [Doc] Ejecuta un bloque con tratamiento de excepciones; los catch asociados atienden los fallos declarados y finally se ejecuta al salir si existe.
        try(var pool=Executors.newSingleThreadExecutor()) {
            // [Doc] Variable local iniciada (var; tipo inferido por Java): iniciada. Se inicializa con new CountDownLatch(1).
            var iniciada=new CountDownLatch(1); var continuar=new CountDownLatch(1);
            // [Doc] Variable local antigua (var; tipo inferido por Java): antigua. Se inicializa con pool.submit(()->new org.springframework.transaction.support.TransactionTemplate(txManager).execute(status->....
            var antigua=pool.submit(()->new org.springframework.transaction.support.TransactionTemplate(txManager).execute(status->{
                // [Doc] Ejecuta la llamada o modificación jdbc.queryForObject("SELECT CURRENT_TIMESTAMP",java.sql.Timestamp.class); Sus efectos dependen del método invocado y del objeto receptor.
                jdbc.queryForObject("SELECT CURRENT_TIMESTAMP",java.sql.Timestamp.class);
                // [Doc] Ejecuta la llamada o modificación iniciada.countDown(); Sus efectos dependen del método invocado y del objeto receptor.
                iniciada.countDown();
                // [Doc] Ejecuta un bloque con tratamiento de excepciones; los catch asociados atienden los fallos declarados y finally se ejecuta al salir si existe.
                try { if(!continuar.await(5,TimeUnit.SECONDS)) throw new IllegalStateException("Timeout"); }
                catch(InterruptedException ex) { Thread.currentThread().interrupt(); throw new IllegalStateException(ex); }
                // [Doc] Termina el método o la función y devuelve centro.reintentar(id).
                return centro.reintentar(id);
            }));
            // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(iniciada.await(5,TimeUnit.SECONDS));
            assertTrue(iniciada.await(5,TimeUnit.SECONDS));
            // [Doc] Ejecuta un bloque con tratamiento de excepciones; los catch asociados atienden los fallos declarados y finally se ejecuta al salir si existe.
            try { assertTrue(centro.reintentar(id)); } finally { continuar.countDown(); }
            // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertFalse(antigua.get(5,TimeUnit.SECONDS));
            assertFalse(antigua.get(5,TimeUnit.SECONDS));
        }
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(1,correos());
        assertEquals(1,correos());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(2,jdbc.queryForObject("SELECT intentos FROM correo_pendiente WHERE id=?",Integer.class,id));
        assertEquals(2,jdbc.queryForObject("SELECT intentos FROM correo_pendiente WHERE id=?",Integer.class,id));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(entrega.entregarSiguiente());
        assertTrue(entrega.entregarSiguiente());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertFalse(centro.reintentar(id));
        assertFalse(centro.reintentar(id));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(1,centro.correos("ENVIADO","destino",0).getTotalElements());
        assertEquals(1,centro.correos("ENVIADO","destino",0).getTotalElements());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(0,centro.correos("REINTENTO","",0).getTotalElements());
        assertEquals(0,centro.correos("REINTENTO","",0).getTotalElements());
        // [Doc] Ejecuta la llamada o modificación verify(smtp,times(1)).send(any(SimpleMailMessage.class)); Sus efectos dependen del método invocado y del objeto receptor.
        verify(smtp,times(1)).send(any(SimpleMailMessage.class));
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/admin/correos").session(sesion(admin)).param("estado","INVALIDO")).andExpect(status().isBadRequest()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/admin/correos").session(sesion(admin)).param("estado","INVALIDO")).andExpect(status().isBadRequest());
    }


    // [Doc] Atributo seguimiento (SeguimientoPedidoService): colaborador que concentra operaciones de seguimiento pedido.
    @Autowired SeguimientoPedidoService seguimiento;
    // [Doc] Atributo servicioProductos (IProductoService): colaborador que concentra operaciones de producto.
    @Autowired IProductoService servicioProductos;

    /** [Doc]
     * Comprueba el escenario «seguimiento registra cambios reales sin duplicados yprotege acceso» mediante las
     * aserciones del cuerpo.
     */
    @Test void seguimientoRegistraCambiosRealesSinDuplicadosYProtegeAcceso() throws Exception {
        // [Doc] Variable local p (Pedido): pedido consultado para esta operación. Se inicializa con comprar(1).
        Pedido p=comprar(1);
        // [Doc] Ejecuta la llamada o modificación pedidos.cambiarEstado(p.getId_pedido(),"ENVIADO"); Sus efectos dependen del método invocado y del objeto receptor.
        pedidos.cambiarEstado(p.getId_pedido(),"ENVIADO");
        // [Doc] Ejecuta la llamada o modificación pedidos.cambiarEstado(p.getId_pedido(),"ENVIADO"); Sus efectos dependen del método invocado y del objeto receptor.
        pedidos.cambiarEstado(p.getId_pedido(),"ENVIADO");
        // [Doc] Ejecuta la llamada o modificación pedidos.marcarEntregado(p.getId_pedido()); Sus efectos dependen del método invocado y del objeto receptor.
        pedidos.marcarEntregado(p.getId_pedido());
        // [Doc] Variable local eventos (var; tipo inferido por Java): eventos. Se inicializa con seguimiento.listar(p.getId_pedido()).
        var eventos=seguimiento.listar(p.getId_pedido());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(List.of("PREPARANDO","ENVIADO","ENTREGADO"), eventos.stream().map(e->e.get("estado")).toList());
        assertEquals(List.of("PREPARANDO","ENVIADO","ENTREGADO"), eventos.stream().map(e->e.get("estado")).toList());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(eventos.stream().allMatch(e->e.get("fecha")!=null));
        assertTrue(eventos.stream().allMatch(e->e.get("fecha")!=null));
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/pedido/"+p.getId_pedido()).session(sesion(titular))).andExpect(status().isOk()) .andExpect(jsonPath("$.historial.length()").value(3)); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/pedido/"+p.getId_pedido()).session(sesion(titular))).andExpect(status().isOk())
            .andExpect(jsonPath("$.historial.length()").value(3));
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/pedido/"+p.getId_pedido()).session(sesion(ajeno))).andExpect(status().isForbidden()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/pedido/"+p.getId_pedido()).session(sesion(ajeno))).andExpect(status().isForbidden());
    }
    /** [Doc]
     * Comprueba el escenario «seguimiento antiguo no inventa fechas yrevierte si falla el aviso» mediante las
     * aserciones del cuerpo.
     */
    @Test void seguimientoAntiguoNoInventaFechasYRevierteSiFallaElAviso() {
        // [Doc] Variable local p (Pedido): pedido consultado para esta operación. Se inicializa con comprar(1).
        Pedido p=comprar(1);
        // [Doc] Ejecuta una modificación SQL con los parámetros indicados; participa en la transacción si existe.
        jdbc.update("DELETE FROM pedido_historial WHERE id_pedido=?",p.getId_pedido());
        // [Doc] Ejecuta la llamada o modificación pedidos.solicitarCancelacion(p.getId_pedido(),titular.getId()); Sus efectos dependen del método invocado y del objeto receptor.
        pedidos.solicitarCancelacion(p.getId_pedido(),titular.getId());
        // [Doc] Variable local eventos (var; tipo inferido por Java): eventos. Se inicializa con seguimiento.listar(p.getId_pedido()).
        var eventos=seguimiento.listar(p.getId_pedido());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(2,eventos.size());
        assertEquals(2,eventos.size()); assertNull(eventos.get(0).get("fecha"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals("PREPARANDO",eventos.get(0).get("estado"));
        assertEquals("PREPARANDO",eventos.get(0).get("estado"));
        // [Doc] Ejecuta la llamada o modificación doThrow(new IllegalStateException("Fallo simulado")).when(cola).encolar(anyString(),anyString(),anyString()); Sus efectos dependen del método invocado y del objeto receptor.
        doThrow(new IllegalStateException("Fallo simulado")).when(cola).encolar(anyString(),anyString(),anyString());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertThrows(IllegalStateException.class,()->pedidos.confirmarCancelacion(p.getId_pedido()));
        assertThrows(IllegalStateException.class,()->pedidos.confirmarCancelacion(p.getId_pedido()));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(2,seguimiento.listar(p.getId_pedido()).size());
        assertEquals(2,seguimiento.listar(p.getId_pedido()).size()); assertEquals(9,stock());
    }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: alertas stock.
     * @return long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    long alertasStock() { return jdbc.queryForObject("SELECT count(*) FROM notificacion WHERE destinatario='ADMIN' AND descripcion LIKE 'Stock mínimo:%'",Long.class); }
    /** [Doc]
     * Comprueba el escenario «stock minimo avisa una vez por episodio yse rearma al reponer» mediante las aserciones
     * del cuerpo.
     */
    @Test void stockMinimoAvisaUnaVezPorEpisodioYSeRearmaAlReponer() throws Exception {
        // [Doc] Variable local p (Pedido): pedido consultado para esta operación. Se inicializa con comprar(5).
        Pedido p=comprar(5);
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(5,stock());
        assertEquals(5,stock()); assertEquals(1,alertasStock());
        // [Doc] Variable local resultados (var; tipo inferido por Java): resultados. Se inicializa con paralelos(()->comprar(1),()->comprar(1)).
        var resultados=paralelos(()->comprar(1),()->comprar(1));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(resultados.stream().allMatch(Pedido.class::isInstance));
        assertTrue(resultados.stream().allMatch(Pedido.class::isInstance));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(3,stock());
        assertEquals(3,stock()); assertEquals(1,alertasStock());
        // [Doc] Ejecuta la llamada o modificación pedidos.cambiarEstado(p.getId_pedido(),"CANCELADO"); Sus efectos dependen del método invocado y del objeto receptor.
        pedidos.cambiarEstado(p.getId_pedido(),"CANCELADO");
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(8,stock());
        assertEquals(8,stock());
        // [Doc] Ejecuta la llamada o modificación comprar(3); Sus efectos dependen del método invocado y del objeto receptor.
        comprar(3); assertEquals(5,stock()); assertEquals(2,alertasStock());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(2,jdbc.queryForObject("SELECT count(*) FROM correo_pendiente WHERE asunto LIKE 'Stock mínimo:%'",Integer.class));
        assertEquals(2,jdbc.queryForObject("SELECT count(*) FROM correo_pendiente WHERE asunto LIKE 'Stock mínimo:%'",Integer.class));
    }
    /** [Doc]
     * Comprueba el escenario «stock manual genera aviso yel fallo de cola revierte toda la compra» mediante las
     * aserciones del cuerpo.
     */
    @Test void stockManualGeneraAvisoYElFalloDeColaRevierteTodaLaCompra() {
        // [Doc] Variable local nuevo (Producto): estado de destino normalizado. Se inicializa con producto(10).
        Producto nuevo=producto(10);
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: nuevo.setNombre_producto("Producto "+"x".repeat(240));
        nuevo.setNombre_producto("Producto "+"x".repeat(240));
        // [Doc] Actualiza el stock de la entidad en memoria; la persistencia se realiza en la transacción: nuevo.setStock(2);
        nuevo.setStock(2); servicioProductos.saveProducto(nuevo);
        // [Doc] Ejecuta la llamada o modificación servicioProductos.saveProducto(nuevo); Sus efectos dependen del método invocado y del objeto receptor.
        servicioProductos.saveProducto(nuevo); assertEquals(1,alertasStock());
        // [Doc] Actualiza el stock de la entidad en memoria; la persistencia se realiza en la transacción: nuevo.setStock(9);
        nuevo.setStock(9); servicioProductos.saveProducto(nuevo);
        // [Doc] Actualiza el stock de la entidad en memoria; la persistencia se realiza en la transacción: nuevo.setStock(0);
        nuevo.setStock(0); servicioProductos.saveProducto(nuevo); assertEquals(2,alertasStock());
        // [Doc] Variable local antes (long): antes. Se inicializa con jdbc.queryForObject("SELECT count(*) FROM producto_alerta_stock",Long.class).
        long antes=jdbc.queryForObject("SELECT count(*) FROM producto_alerta_stock",Long.class);
        // [Doc] Ejecuta la llamada o modificación doThrow(new IllegalStateException("Fallo simulado")).when(cola).encolar(anyString(),anyString(),anyString()); Sus efectos dependen del método invocado y del objeto receptor.
        doThrow(new IllegalStateException("Fallo simulado")).when(cola).encolar(anyString(),anyString(),anyString());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertThrows(IllegalStateException.class,()->comprar(6));
        assertThrows(IllegalStateException.class,()->comprar(6));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(10,stock());
        assertEquals(10,stock()); assertEquals(0,pedidoRepo.count());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(antes,jdbc.queryForObject("SELECT count(*) FROM producto_alerta_stock",Long.class));
        assertEquals(antes,jdbc.queryForObject("SELECT count(*) FROM producto_alerta_stock",Long.class));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM pedido_historial",Integer.class));
        assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM pedido_historial",Integer.class));
    }
}

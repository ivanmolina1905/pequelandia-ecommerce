package com.pequelandia.pequelandia;

import com.pequelandia.pequelandia.config.CsrfInterceptor;
import com.pequelandia.pequelandia.model.*;
import com.pequelandia.pequelandia.repository.*;
import com.pequelandia.pequelandia.service.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Conciliacion y casos de borde: se conservan fallos reales para depurar. */
/** [Doc]
 * Agrupa pruebas automatizadas de auditoria ampliada.
 */
class AuditoriaAmpliadaIntegrationTest extends IntegrationSupport {
    // [Doc] Atributo mvc (MockMvc): mvc.
    @Autowired MockMvc mvc;
    // [Doc] Atributo jdbc (JdbcTemplate): acceso SQL parametrizado mediante JdbcTemplate.
    @Autowired JdbcTemplate jdbc;
    // [Doc] Atributo productos (IProductoRepository): acceso de persistencia a producto.
    @Autowired IProductoRepository productos;
    // [Doc] Atributo usuarios (IUsuarioRepository): acceso de persistencia a usuario.
    @Autowired IUsuarioRepository usuarios;
    // [Doc] Atributo repositorioPedidos (IPedidoRepository): acceso de persistencia a pedido.
    @Autowired IPedidoRepository repositorioPedidos;
    // [Doc] Atributo pedidos (IPedidoService): pedidos o servicio de pedidos según el tipo declarado.
    @Autowired IPedidoService pedidos;
    // [Doc] Atributo estadisticas (IEstadisticasService): colaborador que concentra operaciones de estadisticas.
    @Autowired IEstadisticasService estadisticas;
    // [Doc] Atributo smtp (JavaMailSender): smtp.
    @MockitoBean JavaMailSender smtp;
    // [Doc] Atributo producto (Producto): producto procesado en este punto.
    Producto producto;
    // [Doc] Atributo comprador (Usuario): comprador.
    Usuario admin, comprador;
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: preparar.
     */
    @BeforeEach void preparar() {
        // [Doc] Ejecuta la llamada o modificación jdbc.execute("TRUNCATE usuario, localidad, producto, correo_pendiente RESTART IDENTITY CASCADE"); Sus efectos dependen del método invocado y del objeto receptor.
        jdbc.execute("TRUNCATE usuario, localidad, producto, correo_pendiente RESTART IDENTITY CASCADE");
        // [Doc] Ejecuta la llamada o modificación admin=usuario("admin"); Sus efectos dependen del método invocado y del objeto receptor.
        admin=usuario("admin"); comprador=usuario("comprador");
        // [Doc] Ejecuta la llamada o modificación producto=new Producto(); Sus efectos dependen del método invocado y del objeto receptor.
        producto=new Producto(); producto.setNombre_producto("Remera conciliacion");
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: producto.setPrecio(12.5f);
        producto.setPrecio(12.5f); producto.setStock(100); producto.setEstado("activo");
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: producto.setTalle_producto("4");
        producto.setTalle_producto("4"); producto.setCategorias(new ArrayList<>());
        // [Doc] Solicita persistir la entidad; saveAndFlush también fuerza la sincronización: producto=productos.save(producto);
        producto=productos.save(producto);
    }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: usuario.
     * @param nombre nombre normalizado o visible de la entidad.
     * @return Usuario con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    Usuario usuario(String nombre) {
        // [Doc] Variable local u (var; tipo inferido por Java): cuenta de usuario utilizada por la operación. Se inicializa con new Usuario().
        var u=new Usuario(); u.setNombre_usuario(nombre); u.setEmail(nombre+"@example.invalid");
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: u.setContraseña("Ficticia-2026");
        u.setContraseña("Ficticia-2026"); return usuarios.save(u);
    }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: sesion.
     * @param u cuenta de usuario utilizada por la operación.
     * @return MockHttpSession con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    MockHttpSession sesion(Usuario u) {
        // [Doc] Variable local s (var; tipo inferido por Java): s. Se inicializa con new MockHttpSession().
        var s=new MockHttpSession(); s.setAttribute("usuario",u); s.setAttribute(CsrfInterceptor.KEY,"ampliada"); return s;
    }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: comprar.
     * @return Pedido con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    Pedido comprar() { return pedidos.comprar(comprador.getId(),Map.of(producto.getIdProducto(),2)); }
    /** [Doc]
     * Comprueba el escenario «series dia mes anio concuerdan con compra conocida» mediante las aserciones del
     * cuerpo.
     */
    @Test void seriesDiaMesAnioConcuerdanConCompraConocida() {
        // [Doc] Ejecuta la llamada o modificación comprar(); Sus efectos dependen del método invocado y del objeto receptor.
        comprar(); var hoy=LocalDate.now();
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(25,estadisticas.getVentasPorDia(hoy,hoy).getFirst().getValor(),0.001);
        assertEquals(25,estadisticas.getVentasPorDia(hoy,hoy).getFirst().getValor(),0.001);
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(25,estadisticas.getVentasPorMes(hoy.getYear()).getFirst().getValor(),0.001);
        assertEquals(25,estadisticas.getVentasPorMes(hoy.getYear()).getFirst().getValor(),0.001);
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(25,estadisticas.getVentasPorAnio(hoy.getYear(),hoy.getYear()).getFirst().getValor(),0.001);
        assertEquals(25,estadisticas.getVentasPorAnio(hoy.getYear(),hoy.getYear()).getFirst().getValor(),0.001);
    }
    /** [Doc]
     * Comprueba el escenario «cambiar precio no reescribe ventas historicas» mediante las aserciones del cuerpo.
     */
    @Test void cambiarPrecioNoReescribeVentasHistoricas() {
        // [Doc] Ejecuta la llamada o modificación comprar(); Sus efectos dependen del método invocado y del objeto receptor.
        comprar(); producto.setPrecio(99); productos.save(producto);
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(25,estadisticas.getDashboardResumen().getVentasHoy(),0.001);
        assertEquals(25,estadisticas.getDashboardResumen().getVentasHoy(),0.001);
    }
    /** [Doc]
     * Comprueba el escenario «cambiar precio no reescribe gasto del usuario» mediante las aserciones del cuerpo.
     */
    @Test void cambiarPrecioNoReescribeGastoDelUsuario() {
        // [Doc] Ejecuta la llamada o modificación comprar(); Sus efectos dependen del método invocado y del objeto receptor.
        comprar(); producto.setPrecio(99); productos.save(producto);
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(25,repositorioPedidos.totalGastadoPorUsuario(comprador.getId()),0.001);
        assertEquals(25,repositorioPedidos.totalGastadoPorUsuario(comprador.getId()),0.001);
    }
    /** [Doc]
     * Comprueba el escenario «ventas netas excluyen pedido cancelado» mediante las aserciones del cuerpo.
     */
    @Test void ventasNetasExcluyenPedidoCancelado() {
        // [Doc] Variable local p (var; tipo inferido por Java): p. Se inicializa con comprar().
        var p=comprar(); pedidos.cambiarEstado(p.getId_pedido(),"CANCELADO");
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(0,estadisticas.getDashboardResumen().getVentasHoy(),0.001);
        assertEquals(0,estadisticas.getDashboardResumen().getVentasHoy(),0.001);
    }
    /** [Doc]
     * Comprueba el escenario «top vendidos no cuenta carrito sin comprar» mediante las aserciones del cuerpo.
     */
    @Test void topVendidosNoCuentaCarritoSinComprar() {
        // Un carrito abierto no representa una venta.
        // [Doc] Ejecuta una modificación SQL con los parámetros indicados; participa en la transacción si existe.
        jdbc.update("INSERT INTO carrito_de_compra (id_usuario,total) VALUES (?,?)",comprador.getId(),25);
        // [Doc] Variable local id (Long): identificador del registro. Se inicializa con jdbc.queryForObject("SELECT max(id_carrito) FROM carrito_de_compra",Long.class).
        Long id=jdbc.queryForObject("SELECT max(id_carrito) FROM carrito_de_compra",Long.class);
        // [Doc] Ejecuta una modificación SQL con los parámetros indicados; participa en la transacción si existe.
        jdbc.update("INSERT INTO detalle_carrito (id_carrito,id_producto,cantidad,precio) VALUES (?,?,?,?)",id,producto.getIdProducto(),2,12.5);
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(estadisticas.getDashboardResumen().getTopProductos().isEmpty());
        assertTrue(estadisticas.getDashboardResumen().getTopProductos().isEmpty());
    }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: checkout rechaza producto no comercializable.
     * @param estado estado usado por la operación.
     */
    @ParameterizedTest @ValueSource(strings={"inactivo","discontinuado"})
    void checkoutRechazaProductoNoComercializable(String estado) {
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: producto.setEstado(estado);
        producto.setEstado(estado); productos.save(producto);
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertThrows(IllegalStateException.class,this::comprar);
        assertThrows(IllegalStateException.class,this::comprar);
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(0,repositorioPedidos.count());
        assertEquals(0,repositorioPedidos.count());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(100,productos.findById(producto.getIdProducto()).orElseThrow().getStock());
        assertEquals(100,productos.findById(producto.getIdProducto()).orElseThrow().getStock());
    }
    /** [Doc]
     * Comprueba el escenario «checkout rechaza precio negativo sin guardar pedido» mediante las aserciones del
     * cuerpo.
     */
    @Test void checkoutRechazaPrecioNegativoSinGuardarPedido() {
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: producto.setPrecio(-1);
        producto.setPrecio(-1); productos.save(producto);
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertThrows(IllegalStateException.class,this::comprar);
        assertThrows(IllegalStateException.class,this::comprar);
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(0,repositorioPedidos.count());
        assertEquals(0,repositorioPedidos.count());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(100,productos.findById(producto.getIdProducto()).orElseThrow().getStock());
        assertEquals(100,productos.findById(producto.getIdProducto()).orElseThrow().getStock());
    }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: checkout rechaza cantidad invalida sin descontar.
     * @param cantidad unidades de una línea de carrito.
     */
    @ParameterizedTest @ValueSource(ints={0,-1,Integer.MAX_VALUE})
    void checkoutRechazaCantidadInvalidaSinDescontar(int cantidad) {
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertThrows(RuntimeException.class,()->pedidos.comprar(comprador.getId(),Map.of(producto.getIdProducto(),cantidad)));
        assertThrows(RuntimeException.class,()->pedidos.comprar(comprador.getId(),Map.of(producto.getIdProducto(),cantidad)));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(0,repositorioPedidos.count());
        assertEquals(0,repositorioPedidos.count());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(100,productos.findById(producto.getIdProducto()).orElseThrow().getStock());
        assertEquals(100,productos.findById(producto.getIdProducto()).orElseThrow().getStock());
    }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: cliente no accede arutas administrativas.
     * @param ruta ruta.
     */
    @ParameterizedTest @ValueSource(strings={"/admin/usuarios","/admin/productos","/admin/pedidos","/admin/estadisticas","/admin/correos","/usuarios/traer","/admin/export/usuarios","/admin/export/productos","/admin/export/pedidos"})
    void clienteNoAccedeARutasAdministrativas(String ruta) throws Exception {
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get(ruta).session(sesion(comprador))).andExpect(status().isForbidden()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get(ruta).session(sesion(comprador))).andExpect(status().isForbidden());
    }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: paginas publicas responden sin sesion.
     * @param ruta ruta.
     */
    @ParameterizedTest @ValueSource(strings={"/","/productos","/auth/login","/auth/registro","/auth/recuperar"})
    void paginasPublicasRespondenSinSesion(String ruta) throws Exception {
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get(ruta)).andExpect(status().isOk()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get(ruta)).andExpect(status().isOk());
    }
    /** [Doc]
     * Comprueba el escenario «filtro precio ytalle excluye producto fuera de rango» mediante las aserciones del
     * cuerpo.
     */
    @Test void filtroPrecioYTalleExcluyeProductoFueraDeRango() throws Exception {
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/productos").param("precioMin","20").param("talle","4")) .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/productos").param("precioMin","20").param("talle","4"))
            .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Remera conciliacion"))));
    }
    /** [Doc]
     * Comprueba el escenario «rango diario invertido devuelve error de validacion» mediante las aserciones del
     * cuerpo.
     */
    @Test void rangoDiarioInvertidoDevuelveErrorDeValidacion() throws Exception {
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/admin/estadisticas/serie-dia").session(sesion(admin)) .param("desde","2026-10-30").param("hasta","2026-10-01")).andExpect(status().isBadRequest()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/admin/estadisticas/serie-dia").session(sesion(admin))
            .param("desde","2026-10-30").param("hasta","2026-10-01")).andExpect(status().isBadRequest());
    }
    /** [Doc]
     * Comprueba el escenario «exportar fecha invalida devuelve400» mediante las aserciones del cuerpo.
     */
    @Test void exportarFechaInvalidaDevuelve400() throws Exception {
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/admin/export/ventas-dia").session(sesion(admin)) .param("desde","no-es-fecha").param("hasta","2026-10-30")).andExpect(status().isBadRequest()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/admin/export/ventas-dia").session(sesion(admin))
            .param("desde","no-es-fecha").param("hasta","2026-10-30")).andExpect(status().isBadRequest());
    }
    /** [Doc]
     * Comprueba el escenario «excel pedidos concilia importe yusuario» mediante las aserciones del cuerpo.
     */
    @Test void excelPedidosConciliaImporteYUsuario() throws Exception {
        // [Doc] Ejecuta la llamada o modificación comprar(); Sus efectos dependen del método invocado y del objeto receptor.
        comprar();
        // [Doc] Variable local datos (byte[]): datos. Se inicializa con mvc.perform(get("/admin/export/pedidos").session(sesion(admin))).andExpect(status().isOk()) .andReturn().ge....
        byte[] datos=mvc.perform(get("/admin/export/pedidos").session(sesion(admin))).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsByteArray();
        // [Doc] Ejecuta un bloque con tratamiento de excepciones; los catch asociados atienden los fallos declarados y finally se ejecuta al salir si existe.
        try(var libro=new XSSFWorkbook(new ByteArrayInputStream(datos))) {
            // [Doc] Variable local fila (var; tipo inferido por Java): registro SQL seleccionado. Se inicializa con libro.getSheetAt(0).getRow(1).
            var fila=libro.getSheetAt(0).getRow(1);
            // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals("comprador",fila.getCell(1).getStringCellValue());
            assertEquals("comprador",fila.getCell(1).getStringCellValue());
            // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(25,fila.getCell(3).getNumericCellValue(),0.001);
            assertEquals(25,fila.getCell(3).getNumericCellValue(),0.001);
        }
    }
    /** [Doc]
     * Comprueba el escenario «excel serie diaria concilia importe» mediante las aserciones del cuerpo.
     */
    @Test void excelSerieDiariaConciliaImporte() throws Exception {
        // [Doc] Ejecuta la llamada o modificación comprar(); Sus efectos dependen del método invocado y del objeto receptor.
        comprar(); String hoy=LocalDate.now().toString();
        // [Doc] Variable local datos (byte[]): datos. Se inicializa con mvc.perform(get("/admin/export/ventas-dia").session(sesion(admin)).param("desde",hoy).param("hasta",hoy)) .....
        byte[] datos=mvc.perform(get("/admin/export/ventas-dia").session(sesion(admin)).param("desde",hoy).param("hasta",hoy))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        // [Doc] Ejecuta un bloque con tratamiento de excepciones; los catch asociados atienden los fallos declarados y finally se ejecuta al salir si existe.
        try(var libro=new XSSFWorkbook(new ByteArrayInputStream(datos))) {
            // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(25,libro.getSheetAt(0).getRow(1).getCell(1).getNumericCellValue(),0.001);
            assertEquals(25,libro.getSheetAt(0).getRow(1).getCell(1).getNumericCellValue(),0.001);
        }
    }

    @Test void todasLasSeriesConservanPrecioHistoricoYExcluyenCancelados() {
        var pedido = comprar();
        producto.setPrecio(99); productos.save(producto);
        var hoy = LocalDate.now();
        assertEquals(25, estadisticas.getVentasPorDia(hoy, hoy).getFirst().getValor(), 0.001);
        assertEquals(25, estadisticas.getVentasPorMes(hoy.getYear()).getFirst().getValor(), 0.001);
        assertEquals(25, estadisticas.getVentasPorAnio(hoy.getYear(), hoy.getYear()).getFirst().getValor(), 0.001);
        assertEquals(25, ((Number) repositorioPedidos.comprasPorMes(comprador.getId(), hoy.getYear()).getFirst()[1]).doubleValue(), 0.001);
        assertEquals(2, estadisticas.getDashboardResumen().getTopProductos().getFirst().getCantidadVendida());
        pedidos.cambiarEstado(pedido.getId_pedido(), "CANCELADO");
        assertEquals(0, repositorioPedidos.totalGastadoPorUsuario(comprador.getId()), 0.001);
        assertEquals(0, repositorioPedidos.totalProductosComprados(comprador.getId()));
        assertTrue(estadisticas.getVentasPorDia(hoy, hoy).isEmpty());
        assertTrue(estadisticas.getVentasPorMes(hoy.getYear()).isEmpty());
        assertTrue(estadisticas.getVentasPorAnio(hoy.getYear(), hoy.getYear()).isEmpty());
        assertTrue(repositorioPedidos.comprasPorMes(comprador.getId(), hoy.getYear()).isEmpty());
        assertTrue(estadisticas.getDashboardResumen().getTopProductos().isEmpty());
    }

    @ParameterizedTest @ValueSource(strings={"/admin/export/ventas-dia", "/admin/estadisticas/serie-dia"})
    void rangosInvertidosSeRechazanTambienAlExportar(String ruta) throws Exception {
        mvc.perform(get(ruta).session(sesion(admin)).param("desde", "2026-10-30").param("hasta", "2026-10-01"))
            .andExpect(status().isBadRequest());
    }

    @ParameterizedTest @ValueSource(strings={"/admin/export/ventas-anio", "/admin/estadisticas/serie-anio"})
    void rangosAnualesInvertidosSeRechazan(String ruta) throws Exception {
        mvc.perform(get(ruta).session(sesion(admin)).param("inicio", "2026").param("fin", "2025"))
            .andExpect(status().isBadRequest());
    }
}

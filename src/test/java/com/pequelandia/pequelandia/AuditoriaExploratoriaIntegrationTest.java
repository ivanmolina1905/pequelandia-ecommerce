package com.pequelandia.pequelandia;

import com.pequelandia.pequelandia.config.CsrfInterceptor;
import com.pequelandia.pequelandia.model.Producto;
import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.repository.IProductoRepository;
import com.pequelandia.pequelandia.repository.IUsuarioRepository;
import com.pequelandia.pequelandia.util.CartSessionUtil;
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
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Exige el comportamiento esperado; los fallos quedan visibles para depuracion. */
/** [Doc]
 * Agrupa pruebas automatizadas de auditoria exploratoria.
 */
class AuditoriaExploratoriaIntegrationTest extends IntegrationSupport {
    // [Doc] Atributo mvc (MockMvc): mvc.
    @Autowired MockMvc mvc;
    // [Doc] Atributo jdbc (JdbcTemplate): acceso SQL parametrizado mediante JdbcTemplate.
    @Autowired JdbcTemplate jdbc;
    // [Doc] Atributo productos (IProductoRepository): acceso de persistencia a producto.
    @Autowired IProductoRepository productos;
    // [Doc] Atributo usuarios (IUsuarioRepository): acceso de persistencia a usuario.
    @Autowired IUsuarioRepository usuarios;
    // [Doc] Atributo smtp (JavaMailSender): smtp.
    @MockitoBean JavaMailSender smtp;
    // [Doc] Atributo producto (Producto): producto procesado en este punto.
    Producto producto;
    // [Doc] Atributo admin (Usuario): admin.
    Usuario admin;

    /** [Doc]
     * Prepara o auxilia el escenario de prueba: preparar.
     */
    @BeforeEach void preparar() {
        // [Doc] Ejecuta la llamada o modificación jdbc.execute("TRUNCATE usuario, localidad, producto, correo_pendiente RESTART IDENTITY CASCADE"); Sus efectos dependen del método invocado y del objeto receptor.
        jdbc.execute("TRUNCATE usuario, localidad, producto, correo_pendiente RESTART IDENTITY CASCADE");
        // [Doc] Ejecuta la llamada o modificación admin = new Usuario(); Sus efectos dependen del método invocado y del objeto receptor.
        admin = new Usuario(); admin.setNombre_usuario("admin");
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: admin.setEmail("admin@example.invalid");
        admin.setEmail("admin@example.invalid"); admin=usuarios.save(admin);
        // [Doc] Ejecuta la llamada o modificación producto = new Producto(); Sus efectos dependen del método invocado y del objeto receptor.
        producto = new Producto(); producto.setNombre_producto("Remera auditoria");
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: producto.setPrecio(100);
        producto.setPrecio(100); producto.setStock(3); producto.setEstado("activo");
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: producto.setCategorias(new ArrayList<>());
        producto.setCategorias(new ArrayList<>()); producto=productos.save(producto);
    }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: sesion.
     * @param administrativa administrativa.
     * @return MockHttpSession con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    MockHttpSession sesion(boolean administrativa) {
        // [Doc] Variable local s (var; tipo inferido por Java): s. Se inicializa con new MockHttpSession().
        var s=new MockHttpSession(); s.setAttribute(CsrfInterceptor.KEY,"auditoria");
        // [Doc] Evalúa administrativa. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if(administrativa) s.setAttribute("usuario",admin);
        // [Doc] Termina el método o la función y devuelve s.
        return s;
    }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: catalogo renderiza todos los ordenes.
     * @param orden criterio SQL de ordenación.
     */
    @ParameterizedTest @ValueSource(strings={"relevancia","precio-asc","precio-desc","alfabetico"})
    void catalogoRenderizaTodosLosOrdenes(String orden) throws Exception {
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/productos").param("ordenar",orden)).andExpect(status().isOk()) .andExpect(content().string(org.hamcrest.Matchers.containsString("Remera auditoria"))); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/productos").param("ordenar",orden)).andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Remera auditoria")));
    }
    /** [Doc]
     * Comprueba el escenario «api rechaza stock insuficiente sin modificar carrito» mediante las aserciones del
     * cuerpo.
     */
    @Test void apiRechazaStockInsuficienteSinModificarCarrito() throws Exception {
        // [Doc] Variable local s (var; tipo inferido por Java): s. Se inicializa con sesion(false).
        var s=sesion(false);
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/productos/api/agregar").session(s).param("_csrf","auditoria") .param("productoId",""+producto.getIdProducto()).param("cantidad","4")) .andExpect(jso... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/productos/api/agregar").session(s).param("_csrf","auditoria")
            .param("productoId",""+producto.getIdProducto()).param("cantidad","4"))
            .andExpect(jsonPath("$.ok").value(false));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(CartSessionUtil.getOrCreate(s).isEmpty());
        assertTrue(CartSessionUtil.getOrCreate(s).isEmpty());
    }
    /** [Doc]
     * Comprueba el escenario «formulario tambien rechaza stock insuficiente» mediante las aserciones del cuerpo.
     */
    @Test void formularioTambienRechazaStockInsuficiente() throws Exception {
        // [Doc] Variable local s (var; tipo inferido por Java): s. Se inicializa con sesion(false).
        var s=sesion(false);
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/productos/agregar").session(s).param("_csrf","auditoria") .param("productoId",""+producto.getIdProducto()).param("cantidad","4")); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/productos/agregar").session(s).param("_csrf","auditoria")
            .param("productoId",""+producto.getIdProducto()).param("cantidad","4"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(CartSessionUtil.getOrCreate(s).isEmpty(),"El formulario agrego mas unidades que el stock disponible");
        assertTrue(CartSessionUtil.getOrCreate(s).isEmpty(),"El formulario agrego mas unidades que el stock disponible");
    }
    /** [Doc]
     * Comprueba el escenario «api rechaza producto inactivo» mediante las aserciones del cuerpo.
     */
    @Test void apiRechazaProductoInactivo() throws Exception {
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: producto.setEstado("inactivo");
        producto.setEstado("inactivo"); productos.save(producto);
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/productos/api/agregar").session(sesion(false)).param("_csrf","auditoria") .param("productoId",""+producto.getIdProducto()).param("cantidad","1")) .a... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/productos/api/agregar").session(sesion(false)).param("_csrf","auditoria")
            .param("productoId",""+producto.getIdProducto()).param("cantidad","1"))
            .andExpect(jsonPath("$.ok").value(false));
    }
    /** [Doc]
     * Comprueba el escenario «administrador no puede crear precio negativo» mediante las aserciones del cuerpo.
     */
    @Test void administradorNoPuedeCrearPrecioNegativo() throws Exception {
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/admin/productos/nuevo").session(sesion(true)).param("_csrf","auditoria") .param("nombre_producto","Precio invalido").param("precio","-10").param("st... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/admin/productos/nuevo").session(sesion(true)).param("_csrf","auditoria")
            .param("nombre_producto","Precio invalido").param("precio","-10").param("stock","2"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM producto WHERE precio < 0",Integer.class));
        assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM producto WHERE precio < 0",Integer.class));
    }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: exportaciones generan excel legible.
     * @param entidad entidad.
     */
    @ParameterizedTest @ValueSource(strings={"usuarios","productos","pedidos","stock-bajo","top-productos"})
    void exportacionesGeneranExcelLegible(String entidad) throws Exception {
        // [Doc] Variable local contenido (byte[]): contenido. Se inicializa con mvc.perform(get("/admin/export/"+entidad).session(sesion(true))) .andExpect(status().isOk()).andReturn().ge....
        byte[] contenido=mvc.perform(get("/admin/export/"+entidad).session(sesion(true)))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        // [Doc] Ejecuta un bloque con tratamiento de excepciones; los catch asociados atienden los fallos declarados y finally se ejecuta al salir si existe.
        try(var libro=new XSSFWorkbook(new ByteArrayInputStream(contenido))) {
            // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(libro.getNumberOfSheets()>0);
            assertTrue(libro.getNumberOfSheets()>0);
            // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertNotNull(libro.getSheetAt(0).getRow(0));
            assertNotNull(libro.getSheetAt(0).getRow(0));
        }
    }

    @ParameterizedTest @ValueSource(strings={"/productos/agregar", "/productos/api/agregar"})
    void agregarRepetidoNoSuperaStockNiDesborda(String ruta) throws Exception {
        var session = sesion(false);
        mvc.perform(post(ruta).session(session).param("_csrf", "auditoria")
            .param("productoId", "" + producto.getIdProducto()).param("cantidad", "2"));
        for (String cantidad : new String[]{"2", "2147483647", "0", "-1"}) {
            mvc.perform(post(ruta).session(session).param("_csrf", "auditoria")
                .param("productoId", "" + producto.getIdProducto()).param("cantidad", cantidad));
            assertEquals(2, CartSessionUtil.getOrCreate(session).getItems().get(producto.getIdProducto()));
        }
    }

    @ParameterizedTest @ValueSource(strings={"/productos/agregar", "/productos/api/agregar"})
    void ambasEntradasRechazanProductoNoComercializable(String ruta) throws Exception {
        var session = sesion(false);
        for (String estado : new String[]{"inactivo", "discontinuado"}) {
            producto.setEstado(estado); productos.save(producto);
            mvc.perform(post(ruta).session(session).param("_csrf", "auditoria")
                .param("productoId", "" + producto.getIdProducto()).param("cantidad", "1"));
            assertTrue(CartSessionUtil.getOrCreate(session).isEmpty());
        }
    }

    @ParameterizedTest @ValueSource(strings={"-1", "NaN", "Infinity"})
    void administradorRechazaImportesInvalidosAlCrearYEditar(String importe) throws Exception {
        var session = sesion(true);
        mvc.perform(post("/admin/productos/nuevo").session(session).param("_csrf", "auditoria")
            .param("nombre_producto", "Inválido").param("precio", importe).param("stock", "2"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/admin/productos/" + producto.getIdProducto() + "/editar").session(session).param("_csrf", "auditoria")
            .param("precio", importe).param("stock", "3"))
            .andExpect(status().isBadRequest());
        assertEquals(1, productos.count());
        assertEquals(100, productos.findById(producto.getIdProducto()).orElseThrow().getPrecio());
    }
}

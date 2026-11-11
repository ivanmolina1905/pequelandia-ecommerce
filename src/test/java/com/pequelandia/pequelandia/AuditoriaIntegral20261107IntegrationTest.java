package com.pequelandia.pequelandia;

import com.pequelandia.pequelandia.config.CsrfInterceptor;
import com.pequelandia.pequelandia.model.*;
import com.pequelandia.pequelandia.repository.*;
import com.pequelandia.pequelandia.service.*;
import com.pequelandia.pequelandia.util.CartSessionUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Regresiones de auditoría: expresan el comportamiento esperado, sin modificar negocio. */
class AuditoriaIntegral20261107IntegrationTest extends IntegrationSupport {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired IUsuarioRepository usuarios;
    @Autowired IProductoRepository productos;
    @Autowired IPedidoService pedidos;
    @MockitoBean JavaMailSender smtp;
    Usuario admin, cliente;
    Producto producto;

    @BeforeEach void preparar() {
        jdbc.execute("TRUNCATE usuario, localidad, producto, correo_pendiente RESTART IDENTITY CASCADE");
        admin = usuario("admin"); cliente = usuario("cliente");
        producto = new Producto(); producto.setNombre_producto("Producto auditoria");
        producto.setPrecio(12.5f); producto.setStock(10); producto.setEstado("activo");
        producto.setImagenUrl("/imagenes/anterior.png"); producto.setCategorias(new ArrayList<>());
        producto = productos.save(producto);
    }
    Usuario usuario(String nombre) {
        var u = new Usuario(); u.setNombre_usuario(nombre); u.setEmail(nombre+"@example.invalid");
        u.setContraseña("Ficticia-2026"); return usuarios.save(u);
    }
    MockHttpSession sesion(Usuario u) {
        var s = new MockHttpSession(); s.setAttribute("usuario", u);
        s.setAttribute(CsrfInterceptor.KEY,"auditoria"); return s;
    }

    @Test void inicioDeSesionActualizaUltimaConexion() throws Exception {
        mvc.perform(post("/auth/login").session(sesion(null)).param("_csrf","auditoria")
                .param("username",cliente.getEmail()).param("password","Ficticia-2026"))
                .andExpect(status().is3xxRedirection());
        assertNotNull(usuarios.findById(cliente.getId()).orElseThrow().getUltima_conexion());
    }
    @Test void catalogoExcluyeDiscontinuados() throws Exception {
        producto.setEstado("discontinuado"); productos.save(producto);
        mvc.perform(get("/productos")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Producto auditoria"))));
    }
    @Test void editarImagenAdministrativaPersisteElCambio() throws Exception {
        mvc.perform(post("/admin/productos/"+producto.getIdProducto()+"/editar")
                .session(sesion(admin)).param("_csrf","auditoria")
                .param("nombre_producto","Producto auditoria").param("precio","12.5")
                .param("stock","10").param("estado","activo")
                .param("imagenUrl","/imagenes/nueva.png")).andExpect(status().is3xxRedirection());
        assertEquals("/imagenes/nueva.png", productos.findById(producto.getIdProducto()).orElseThrow().getImagenUrl());
    }
    @Test void cantidadNoAgregaUnaLineaAusente() throws Exception {
        var s=sesion(cliente);
        mvc.perform(post("/carrito/cantidad").session(s).param("_csrf","auditoria")
                .param("productoId",String.valueOf(producto.getIdProducto())).param("cantidad","2"));
        assertTrue(CartSessionUtil.getOrCreate(s).isEmpty(), "Modificar cantidad debe exigir una linea existente");
    }
    @Test void altaAdministrativaRechazaDatosInvalidos() throws Exception {
        jdbc.update("INSERT INTO localidad (nombre_localidad,codigo_postal) VALUES ('Localidad ficticia',1000)");
        Long localidad=jdbc.queryForObject("SELECT max(id_localidad) FROM localidad",Long.class);
        mvc.perform(post("/admin/usuarios/nuevo").session(sesion(admin)).param("_csrf","auditoria")
                .param("nombre_usuario","invalido").param("apellido","Prueba")
                .param("email","correo-invalido").param("dni","22333444")
                .param("direccion","Direccion ficticia").param("contraseña","Ficticia-2026")
                .param("localidadId",localidad.toString()));
        assertEquals(2,usuarios.count(), "Un email invalido no debe crear una cuenta");
    }
    @Test void edicionAdministrativaRechazaEmailDuplicado() throws Exception {
        mvc.perform(post("/admin/usuarios/"+cliente.getId()+"/editar").session(sesion(admin))
                .param("_csrf","auditoria").param("email",admin.getEmail()));
        assertEquals("cliente@example.invalid",usuarios.findById(cliente.getId()).orElseThrow().getEmail());
    }
    @Test void aliasCarritoConservaElModeloDeLaVista() throws Exception {
        var s=sesion(cliente); CartSessionUtil.getOrCreate(s).add(producto.getIdProducto(),2);
        mvc.perform(get("/carrito.html").session(s)).andExpect(status().isOk())
                .andExpect(model().attributeExists("detalles","items","total"));
    }
    @Test void usuariosJsonConComprasNoTieneRecursion() throws Exception {
        pedidos.comprar(cliente.getId(),Map.of(producto.getIdProducto(),1));
        var respuesta=mvc.perform(get("/usuarios/traer").session(sesion(admin))).andReturn().getResponse();
        assertEquals(200,respuesta.getStatus());
        assertDoesNotThrow(()->new com.fasterxml.jackson.databind.ObjectMapper().readTree(respuesta.getContentAsString()));
    }
}

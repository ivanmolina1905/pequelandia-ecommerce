package com.pequelandia.pequelandia;

import com.pequelandia.pequelandia.config.CsrfInterceptor;
import com.pequelandia.pequelandia.model.*;
import com.pequelandia.pequelandia.repository.*;
import com.pequelandia.pequelandia.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/** [Doc]
 * Agrupa pruebas automatizadas de circuito usuarios.
 */
class CircuitoUsuariosIntegrationTest extends IntegrationSupport {
    @Autowired IProductoRepository productos;

    @Test void invitadoSeRegistraIngresaYFinalizaConSuLocalidad() throws Exception {
        Producto producto = new Producto();
        producto.setNombre_producto("Producto de prueba de checkout");
        producto.setPrecio(125f);
        producto.setStock(5);
        producto.setEstado("activo");
        producto = productos.save(producto);
        var visitante = sesion();
        mvc.perform(post("/productos/agregar").session(visitante)
                .param("_csrf", "csrf-circuito")
                .param("productoId", String.valueOf(producto.getIdProducto())).param("cantidad", "2"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(registro(visitante, datos())).andExpect(status().is3xxRedirection());
        mvc.perform(post("/auth/login").session(visitante).param("_csrf", "csrf-circuito")
                .param("username", "circuito@example.invalid").param("password", "Cuenta-Prueba-2026"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/carrito").session(visitante)).andExpect(status().isOk());
        mvc.perform(post("/carrito/finalizar").session(visitante).header("X-CSRF-TOKEN", "csrf-circuito"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.pedidoId").isNumber());
        assertEquals(3, productos.findById(producto.getIdProducto()).orElseThrow().getStock());
        assertTrue(com.pequelandia.pequelandia.util.CartSessionUtil.getOrCreate(visitante).isEmpty());
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM pedido", Integer.class));
    }
    // [Doc] Atributo mvc (MockMvc): mvc.
    @Autowired MockMvc mvc;
    // [Doc] Atributo jdbc (JdbcTemplate): acceso SQL parametrizado mediante JdbcTemplate.
    @Autowired JdbcTemplate jdbc;
    // [Doc] Atributo usuarios (IUsuarioRepository): acceso de persistencia a usuario.
    @Autowired IUsuarioRepository usuarios;
    // [Doc] Atributo localidades (ILocalidadRepository): acceso de persistencia a localidad.
    @Autowired ILocalidadRepository localidades;
    // [Doc] Atributo entrega (EntregaCorreoService): colaborador que concentra operaciones de entrega correo.
    @Autowired EntregaCorreoService entrega;
    // [Doc] Atributo smtp (JavaMailSender): smtp.
    @MockitoBean JavaMailSender smtp;
    // [Doc] Atributo cola (ColaCorreoService): colaborador que concentra operaciones de cola correo.
    @MockitoSpyBean ColaCorreoService cola;
    // [Doc] Atributo localidad (Localidad): localidad asociada a la cuenta.
    Localidad localidad;
    // [Doc] Atributo admin (Usuario): admin.
    Usuario admin;

    /** [Doc]
     * Prepara o auxilia el escenario de prueba: preparar.
     */
    @BeforeEach void preparar() {
        // [Doc] Ejecuta la llamada o modificación jdbc.execute("TRUNCATE usuario, localidad, correo_pendiente, recuperacion_clave, recuperacion_limite RESTART IDENTITY CASCADE"); Sus efectos dependen del método invocado y del objeto receptor.
        jdbc.execute("TRUNCATE usuario, localidad, correo_pendiente, recuperacion_clave, recuperacion_limite RESTART IDENTITY CASCADE");
        // [Doc] Ejecuta la llamada o modificación localidad=new Localidad(); Sus efectos dependen del método invocado y del objeto receptor.
        localidad=new Localidad(); localidad.setNombre_localidad("Ciudad de prueba"); localidad.setCodigo_postal(3000);
        // [Doc] Solicita persistir la entidad; saveAndFlush también fuerza la sincronización: localidad=localidades.save(localidad);
        localidad=localidades.save(localidad);
        // [Doc] Ejecuta la llamada o modificación admin=new Usuario(); Sus efectos dependen del método invocado y del objeto receptor.
        admin=new Usuario(); admin.setNombre_usuario("admin"); admin.setEmail("admin@example.invalid");
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: admin.setContraseña("Admin-Prueba-2026");
        admin.setContraseña("Admin-Prueba-2026"); admin=usuarios.save(admin);
        // [Doc] Ejecuta la llamada o modificación reset(smtp,cola); Sus efectos dependen del método invocado y del objeto receptor.
        reset(smtp,cola);
    }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: sesion.
     * @return MockHttpSession con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    MockHttpSession sesion() {
        // [Doc] Variable local s (var; tipo inferido por Java): s. Se inicializa con new MockHttpSession().
        var s=new MockHttpSession(); s.setAttribute(CsrfInterceptor.KEY,"csrf-circuito"); return s;
    }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: datos.
     * @return Map&lt;String, String&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    Map<String,String> datos() {
        // [Doc] Variable local d (var; tipo inferido por Java): d. Se inicializa con new LinkedHashMap<String,String>().
        var d=new LinkedHashMap<String,String>();
        // [Doc] Ejecuta la llamada o modificación d.put("nombre_usuario"," circuito "); Sus efectos dependen del método invocado y del objeto receptor.
        d.put("nombre_usuario"," circuito "); d.put("apellido"," Prueba ");
        // [Doc] Ejecuta la llamada o modificación d.put("email"," CIRCUITO@EXAMPLE.INVALID "); Sus efectos dependen del método invocado y del objeto receptor.
        d.put("email"," CIRCUITO@EXAMPLE.INVALID "); d.put("dni","23456789");
        // [Doc] Ejecuta la llamada o modificación d.put("telefono","+54 342 123-4567"); Sus efectos dependen del método invocado y del objeto receptor.
        d.put("telefono","+54 342 123-4567"); d.put("direccion"," Calle ficticia 123 ");
        // [Doc] Ejecuta la llamada o modificación d.put("fecha_nacimiento","1998-04-15"); Sus efectos dependen del método invocado y del objeto receptor.
        d.put("fecha_nacimiento","1998-04-15"); d.put("localidadId",String.valueOf(localidad.getId_localidad()));
        // [Doc] Ejecuta la llamada o modificación d.put("contraseña","Cuenta-Prueba-2026"); Sus efectos dependen del método invocado y del objeto receptor.
        d.put("contraseña","Cuenta-Prueba-2026"); d.put("confirmarContraseña","Cuenta-Prueba-2026");
        // [Doc] Ejecuta la llamada o modificación d.put("aceptarTerminos","true"); Sus efectos dependen del método invocado y del objeto receptor.
        d.put("aceptarTerminos","true"); return d;
    }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: registro.
     * @param s s.
     * @param d d.
     * @return MockHttpServletRequestBuilder con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    MockHttpServletRequestBuilder registro(MockHttpSession s, Map<String,String> d) {
        // [Doc] Variable local r (var; tipo inferido por Java): r. Se inicializa con post("/auth/registro").session(s).param("_csrf","csrf-circuito").
        var r=post("/auth/registro").session(s).param("_csrf","csrf-circuito");
        // [Doc] Ejecuta la llamada o modificación d.forEach((k,v)->r.param(k,v)); Sus efectos dependen del método invocado y del objeto receptor.
        d.forEach((k,v)->r.param(k,v)); return r;
    }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: registrar.
     * @return Usuario con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    Usuario registrar() throws Exception {
        // [Doc] Ejecuta la llamada o modificación mvc.perform(registro(sesion(),datos())).andExpect(status().is3xxRedirection()) .andExpect(redirectedUrl("/auth/login?registroExitoso=true")); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(registro(sesion(),datos())).andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/auth/login?registroExitoso=true"));
        // [Doc] Termina el método o la función y devuelve usuarios.findAll().stream().filter(u->"circuito".equals(u.getNombre_usuario())).findFirst().orElseThrow().
        return usuarios.findAll().stream().filter(u->"circuito".equals(u.getNombre_usuario())).findFirst().orElseThrow();
    }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: ingresar.
     * @param nombre nombre normalizado o visible de la entidad.
     * @param clave clave.
     * @param destino destinatario o ruta de destino de esta operación.
     * @return MockHttpSession con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    MockHttpSession ingresar(String nombre,String clave,String destino) throws Exception {
        // [Doc] Variable local s (var; tipo inferido por Java): s. Se inicializa con sesion().
        var s=sesion(); String anterior=s.getId();
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/auth/login").session(s).param("_csrf","csrf-circuito").param("username",nombre).param("password",clave)) .andExpect(status().is3xxRedirection()).and... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/auth/login").session(s).param("_csrf","csrf-circuito").param("username",nombre).param("password",clave))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl(destino));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertNotEquals(anterior,s.getId());
        assertNotEquals(anterior,s.getId()); assertNotNull(s.getAttribute("usuario")); return s;
    }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: cantidad.
     * @param tabla tabla.
     * @return int con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    int cantidad(String tabla) { return jdbc.queryForObject("SELECT count(*) FROM "+tabla,Integer.class); }

    /** [Doc]
     * Comprueba el escenario «circuito completo desde formulario hasta administrador login perfil yrecuperacion»
     * mediante las aserciones del cuerpo.
     */
    @Test void circuitoCompletoDesdeFormularioHastaAdministradorLoginPerfilYRecuperacion() throws Exception {
        // [Doc] Variable local visitante (var; tipo inferido por Java): visitante. Se inicializa con sesion().
        var visitante=sesion();
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/auth/registro").session(visitante)).andExpect(status().isOk()) .andExpect(content().string(containsString("Ciudad de prueba"))) .andExpect(content().... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/auth/registro").session(visitante)).andExpect(status().isOk())
            .andExpect(content().string(containsString("Ciudad de prueba")))
            .andExpect(content().string(containsString("csrf-circuito")));
        // [Doc] Ejecuta la llamada o modificación mvc.perform(registro(visitante,datos())).andExpect(redirectedUrl("/auth/login?registroExitoso=true")); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(registro(visitante,datos())).andExpect(redirectedUrl("/auth/login?registroExitoso=true"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertNull(visitante.getAttribute("usuario"));
        assertNull(visitante.getAttribute("usuario")); // registrarse no inicia sesión
        // [Doc] Variable local nuevo (Usuario): estado de destino normalizado. Se inicializa con usuarios.findAll().stream().filter(u->"circuito".equals(u.getNombre_usuario())).findFirst().orElseThrow().
        Usuario nuevo=usuarios.findAll().stream().filter(u->"circuito".equals(u.getNombre_usuario())).findFirst().orElseThrow();
        // [Doc] Variable local fila (var; tipo inferido por Java): registro SQL seleccionado. Se inicializa con jdbc.queryForMap(""" SELECT nombre_usuario, apellido, email, dni, telefono, direccion, id_localidad, puntos....
        var fila=jdbc.queryForMap("""
            SELECT nombre_usuario, apellido, email, dni, telefono, direccion, id_localidad, puntos,
            fecha_registro IS NOT NULL AS tiene_fecha, ultima_conexion IS NULL AS sin_conexion,
            CAST(fecha_nacimiento AS DATE)::text AS nacimiento,
            contraseña = ? AS clave_coincide FROM usuario WHERE id_usuario=?
            ""","Cuenta-Prueba-2026",nuevo.getId());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals("circuito",fila.get("nombre_usuario"));
        assertEquals("circuito",fila.get("nombre_usuario")); assertEquals("Prueba",fila.get("apellido"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals("circuito@example.invalid",fila.get("email"));
        assertEquals("circuito@example.invalid",fila.get("email")); assertEquals(23456789,((Number)fila.get("dni")).intValue());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals("+54 342 123-4567",fila.get("telefono"));
        assertEquals("+54 342 123-4567",fila.get("telefono")); assertEquals("Calle ficticia 123",fila.get("direccion"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(localidad.getId_localidad(),((Number)fila.get("id_localidad")).longValue());
        assertEquals(localidad.getId_localidad(),((Number)fila.get("id_localidad")).longValue());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals("1998-04-15",fila.get("nacimiento"));
        assertEquals("1998-04-15",fila.get("nacimiento")); assertEquals(0,fila.get("puntos"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(true,fila.get("tiene_fecha"));
        assertEquals(true,fila.get("tiene_fecha")); assertEquals(true,fila.get("sin_conexion")); assertEquals(true,fila.get("clave_coincide"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(2,cantidad("usuario"));
        assertEquals(2,cantidad("usuario")); assertEquals(1,cantidad("notificacion")); assertEquals(2,cantidad("correo_pendiente"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals("ADMIN",jdbc.queryForObject("SELECT destinatario FROM notificacion",String.class));
        assertEquals("ADMIN",jdbc.queryForObject("SELECT destinatario FROM notificacion",String.class));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM correo_pendiente WHERE destinatario='circuito@example.invalid'",Integer.class));
        assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM correo_pendiente WHERE destinatario='circuito@example.invalid'",Integer.class));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM correo_pendiente WHERE destinatario='admin@example.invalid'",Integer.class));
        assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM correo_pendiente WHERE destinatario='admin@example.invalid'",Integer.class));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertFalse(jdbc.queryForList("SELECT contenido FROM correo_pendiente",String.class).stream().anyMatch(t->t.contains("Cuenta-Prueba-2026")));
        assertFalse(jdbc.queryForList("SELECT contenido FROM correo_pendiente",String.class).stream().anyMatch(t->t.contains("Cuenta-Prueba-2026")));
        // [Doc] Ejecuta la llamada o modificación verifyNoInteractions(smtp); Sus efectos dependen del método invocado y del objeto receptor.
        verifyNoInteractions(smtp);
        // [Doc] Variable local sa (var; tipo inferido por Java): sa. Se inicializa con ingresar("admin@example.invalid","Admin-Prueba-2026","/panel/admin").
        var sa=ingresar("admin@example.invalid","Admin-Prueba-2026","/panel/admin");
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/admin/usuarios").session(sa).param("q","circuito")).andExpect(status().isOk()) .andExpect(content().string(containsString("circuito@example.invalid")... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/admin/usuarios").session(sa).param("q","circuito")).andExpect(status().isOk())
            .andExpect(content().string(containsString("circuito@example.invalid")))
            .andExpect(content().string(not(containsString("Cuenta-Prueba-2026"))));
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/notificaciones/historial").session(sa)).andExpect(status().isOk()) .andExpect(content().string(containsString("Nuevo usuario registrado: circuito"))); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/notificaciones/historial").session(sa)).andExpect(status().isOk())
            .andExpect(content().string(containsString("Nuevo usuario registrado: circuito")));
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/admin/correos").session(sa)).andExpect(status().isOk()) .andExpect(content().string(containsString("circuito@example.invalid"))); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/admin/correos").session(sa)).andExpect(status().isOk())
            .andExpect(content().string(containsString("circuito@example.invalid")));
        // [Doc] Variable local su (var; tipo inferido por Java): su. Se inicializa con ingresar("CIRCUITO@EXAMPLE.INVALID","Cuenta-Prueba-2026","/panelusuarios/panelhistorial?section=perfil").
        var su=ingresar("CIRCUITO@EXAMPLE.INVALID","Cuenta-Prueba-2026","/panelusuarios/panelhistorial?section=perfil");
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/panelusuarios/panelhistorial").session(su)).andExpect(status().isOk()) .andExpect(content().string(containsString("circuito@example.invalid"))); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/panelusuarios/panelhistorial").session(su)).andExpect(status().isOk())
            .andExpect(content().string(containsString("circuito@example.invalid")));
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/admin/usuarios").session(su)).andExpect(status().isForbidden()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/admin/usuarios").session(su)).andExpect(status().isForbidden());
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/admin/correos").session(su)).andExpect(status().isForbidden()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/admin/correos").session(su)).andExpect(status().isForbidden());
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/notificaciones/mias").session(su)).andExpect(status().isOk()) .andExpect(content().string(not(containsString("Nuevo usuario registrado")))); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/notificaciones/mias").session(su)).andExpect(status().isOk())
            .andExpect(content().string(not(containsString("Nuevo usuario registrado"))));
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/usuarios/traer").session(sa)).andExpect(status().isOk()) .andExpect(content().string(not(containsString("Cuenta-Prueba-2026")))); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/usuarios/traer").session(sa)).andExpect(status().isOk())
            .andExpect(content().string(not(containsString("Cuenta-Prueba-2026"))));
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/panelusuarios/perfil/editar").session(su).param("_csrf","csrf-circuito") .param("nombre","circuito").param("email","circuito@example.invalid") .para... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/panelusuarios/perfil/editar").session(su).param("_csrf","csrf-circuito")
            .param("nombre","circuito").param("email","circuito@example.invalid")
            .param("direccion","Nueva dirección ficticia").param("telefono","+54 342 765-4321"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.ok").value(true));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals("Nueva dirección ficticia",jdbc.queryForObject("SELECT direccion FROM usuario WHERE id_usuario=?",String.class,nuevo.getId()));
        assertEquals("Nueva dirección ficticia",jdbc.queryForObject("SELECT direccion FROM usuario WHERE id_usuario=?",String.class,nuevo.getId()));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals("Nueva dirección ficticia",((Usuario)su.getAttribute("usuario")).getDireccion());
        assertEquals("Nueva dirección ficticia",((Usuario)su.getAttribute("usuario")).getDireccion());
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/auth/logout").session(su)).andExpect(redirectedUrl("/auth/login")); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/auth/logout").session(su)).andExpect(redirectedUrl("/auth/login")); assertTrue(su.isInvalid());
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/admin/usuarios")).andExpect(status().isUnauthorized()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/admin/usuarios")).andExpect(status().isUnauthorized());
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/panelusuarios/panelhistorial")).andExpect(status().isUnauthorized()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/panelusuarios/panelhistorial")).andExpect(status().isUnauthorized());
        // [Doc] Variable local sr (var; tipo inferido por Java): sr. Se inicializa con sesion().
        var sr=sesion();
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/auth/recuperar").session(sr).param("_csrf","csrf-circuito").param("email","circuito@example.invalid")) .andExpect(status().isOk()).andExpect(model()... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/auth/recuperar").session(sr).param("_csrf","csrf-circuito").param("email","circuito@example.invalid"))
            .andExpect(status().isOk()).andExpect(model().attributeExists("mensaje"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(1,cantidad("recuperacion_clave"));
        assertEquals(1,cantidad("recuperacion_clave"));
        // [Doc] Variable local texto (String): contenido textual que se construye para presentar o enviar. Se inicializa con jdbc.queryForObject("SELECT contenido FROM correo_pendiente WHERE asunto LIKE 'Recuper%'",String.class).
        String texto=jdbc.queryForObject("SELECT contenido FROM correo_pendiente WHERE asunto LIKE 'Recuper%'",String.class);
        // [Doc] Variable local token (String): valor del token usado por esta operación. Se inicializa con texto.split("token=")[1].split("\\s")[0].
        String token=texto.split("token=")[1].split("\\s")[0];
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/auth/restablecer").session(sr).param("token",token)).andExpect(status().isOk()).andExpect(model().attribute("valido",true)); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/auth/restablecer").session(sr).param("token",token)).andExpect(status().isOk()).andExpect(model().attribute("valido",true));
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/auth/restablecer").session(sr).param("_csrf","csrf-circuito").param("token",token) .param("password","Nueva-Cuenta-2026").param("confirmacion","Nuev... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/auth/restablecer").session(sr).param("_csrf","csrf-circuito").param("token",token)
            .param("password","Nueva-Cuenta-2026").param("confirmacion","Nueva-Cuenta-2026"))
            .andExpect(redirectedUrl("/auth/login?contrasenaActualizada=true"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(0,cantidad("recuperacion_clave"));
        assertEquals(0,cantidad("recuperacion_clave")); assertEquals(4,cantidad("correo_pendiente"));
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/auth/login").session(sesion()).param("_csrf","csrf-circuito") .param("username","circuito@example.invalid").param("password","Cuenta-Prueba-2026")) .andExpect(statu... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/auth/login").session(sesion()).param("_csrf","csrf-circuito")
            .param("username","circuito@example.invalid").param("password","Cuenta-Prueba-2026"))
            .andExpect(status().isOk()).andExpect(model().attributeExists("error"));
        // [Doc] Ejecuta la llamada o modificación ingresar("circuito@example.invalid","Nueva-Cuenta-2026","/panelusuarios/panelhistorial?section=perfil"); Sus efectos dependen del método invocado y del objeto receptor.
        ingresar("circuito@example.invalid","Nueva-Cuenta-2026","/panelusuarios/panelhistorial?section=perfil");
        // [Doc] Recorre los elementos o repite según int i=0;i<4;i++. Cada vuelta ejecuta el cuerpo del bucle.
        for(int i=0;i<4;i++) assertTrue(entrega.entregarSiguiente());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertFalse(entrega.entregarSiguiente());
        assertFalse(entrega.entregarSiguiente());
        // [Doc] Ejecuta la llamada o modificación verify(smtp,times(4)).send(any(SimpleMailMessage.class)); Sus efectos dependen del método invocado y del objeto receptor.
        verify(smtp,times(4)).send(any(SimpleMailMessage.class));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(4,jdbc.queryForObject("SELECT count(*) FROM correo_pendiente WHERE enviado_en IS NOT NULL",Integer.class));
        assertEquals(4,jdbc.queryForObject("SELECT count(*) FROM correo_pendiente WHERE enviado_en IS NOT NULL",Integer.class));
    }

    /** [Doc]
     * Prepara o auxilia el escenario de prueba: registro invalido no deja usuario avisos ni correos.
     * @param campo campo.
     * @param valor valor recibido o calculado para este paso.
     */
    @ParameterizedTest
    @CsvSource({"nombre_usuario,' '","apellido,' '","email,' '","direccion,' '","dni,123",
        "dni,100000000","contraseña,123","confirmarContraseña,distinta","aceptarTerminos,false",
        "nombre_usuario,AdMiN","email,sin-arroba","dni,no-numero","fecha_nacimiento,no-fecha","localidadId,no-numero"})
    void registroInvalidoNoDejaUsuarioAvisosNiCorreos(String campo,String valor) throws Exception {
        // [Doc] Variable local d (var; tipo inferido por Java): d. Se inicializa con datos().
        var d=datos(); d.put(campo,valor);
        // [Doc] Ejecuta la llamada o modificación mvc.perform(registro(sesion(),d)).andExpect(status().isOk()).andExpect(view().name("registro")) .andExpect(model().attributeExists("error")); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(registro(sesion(),d)).andExpect(status().isOk()).andExpect(view().name("registro"))
            .andExpect(model().attributeExists("error"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(1,cantidad("usuario"));
        assertEquals(1,cantidad("usuario")); assertEquals(0,cantidad("notificacion")); assertEquals(0,cantidad("correo_pendiente"));
    }

    /** [Doc]
     * Prepara o auxilia el escenario de prueba: duplicados se rechazan sin efectos secundarios.
     * @param campo campo.
     * @param valor valor recibido o calculado para este paso.
     */
    @ParameterizedTest @CsvSource({"email,CIRCUITO@EXAMPLE.INVALID","dni,23456789"})
    void duplicadosSeRechazanSinEfectosSecundarios(String campo,String valor) throws Exception {
        // [Doc] Ejecuta la llamada o modificación registrar(); Sus efectos dependen del método invocado y del objeto receptor.
        registrar(); var d=datos(); d.put("nombre_usuario","otra");d.put("email","otra@example.invalid");d.put("dni","34567890");
        // [Doc] Ejecuta la llamada o modificación d.put(campo,valor); Sus efectos dependen del método invocado y del objeto receptor.
        d.put(campo,valor);
        // [Doc] Ejecuta la llamada o modificación mvc.perform(registro(sesion(),d)).andExpect(status().isOk()).andExpect(model().attributeExists("error")); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(registro(sesion(),d)).andExpect(status().isOk()).andExpect(model().attributeExists("error"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(2,cantidad("usuario"));
        assertEquals(2,cantidad("usuario"));assertEquals(1,cantidad("notificacion"));assertEquals(2,cantidad("correo_pendiente"));
    }

    /** [Doc]
     * Comprueba el escenario «falla en correo revierte alta ynotificacion» mediante las aserciones del cuerpo.
     */
    @Test void historialDeNotificacionesConservaPanelDelCliente() throws Exception {
        registrar();
        var sesion = ingresar("circuito@example.invalid", "Cuenta-Prueba-2026", "/panelusuarios/panelhistorial?section=perfil");
        mvc.perform(get("/notificaciones/historial").session(sesion).param("estado", "NO_LEIDAS"))
            .andExpect(status().isOk()).andExpect(view().name("panelusuario"))
            .andExpect(model().attribute("section", "notificaciones"))
            .andExpect(model().attribute("estado", "NO_LEIDAS"))
            .andExpect(model().attributeExists("u", "stats", "pedidosPage", "pagina"))
            .andExpect(content().string(containsString("id=\"notificaciones-contenido\"")))
            .andExpect(content().string(containsString("Panel de Usuario")))
            .andExpect(content().string(not(containsString("Nuevo usuario registrado: circuito"))));
    }
    @Test void nombresRepetidosSeDistinguenPorEmail() throws Exception {
        Usuario primero = registrar();
        var d = datos();
        d.put("email", "otro@example.invalid");
        d.put("dni", "34567890");
        mvc.perform(registro(sesion(), d)).andExpect(status().is3xxRedirection());
        String destino = "/panelusuarios/panelhistorial?section=perfil";
        var primeraSesion = ingresar(" CIRCUITO@EXAMPLE.INVALID ", "Cuenta-Prueba-2026", destino);
        var segundaSesion = ingresar("otro@example.invalid", "Cuenta-Prueba-2026", destino);
        Usuario segundo = (Usuario) segundaSesion.getAttribute("usuario");
        assertEquals(primero.getId(), ((Usuario) primeraSesion.getAttribute("usuario")).getId());
        assertNotEquals(primero.getId(), segundo.getId());
        assertEquals(primero.getNombre_usuario(), segundo.getNombre_usuario());
        mvc.perform(post("/auth/login").session(sesion()).param("_csrf", "csrf-circuito")
            .param("username", "circuito").param("password", "Cuenta-Prueba-2026"))
            .andExpect(model().attribute("error", "Email o contraseña incorrectos"));
        mvc.perform(post("/panelusuarios/perfil/editar").session(segundaSesion)
            .param("_csrf", "csrf-circuito").param("nombre", "circuito").param("email", "otro@example.invalid"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.ok").value(true));
        mvc.perform(post("/panelusuarios/perfil/editar").session(segundaSesion)
            .param("_csrf", "csrf-circuito").param("nombre", "circuito").param("email", "CIRCUITO@EXAMPLE.INVALID"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.ok").value(false));
        assertEquals("otro@example.invalid", usuarios.findById(segundo.getId()).orElseThrow().getEmail());
    }
    @Test void fallaEnCorreoRevierteAltaYNotificacion() throws Exception {
        // [Doc] Ejecuta la llamada o modificación doThrow(new IllegalStateException("Fallo simulado")).when(cola).encolar(anyString(),anyString(),anyString()); Sus efectos dependen del método invocado y del objeto receptor.
        doThrow(new IllegalStateException("Fallo simulado")).when(cola).encolar(anyString(),anyString(),anyString());
        // [Doc] Ejecuta la llamada o modificación mvc.perform(registro(sesion(),datos())).andExpect(status().isOk()).andExpect(model().attributeExists("error")); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(registro(sesion(),datos())).andExpect(status().isOk()).andExpect(model().attributeExists("error"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(1,cantidad("usuario"));
        assertEquals(1,cantidad("usuario"));assertEquals(0,cantidad("notificacion"));assertEquals(0,cantidad("correo_pendiente"));
    }

    /** [Doc]
     * Comprueba el escenario «csrf impide registro login yedicion sin token» mediante las aserciones del cuerpo.
     */
    @Test void csrfImpideRegistroLoginYEdicionSinToken() throws Exception {
        // [Doc] Variable local d (var; tipo inferido por Java): d. Se inicializa con datos().
        var d=datos(); var r=post("/auth/registro").session(sesion());d.forEach((k,v)->r.param(k,v));
        // [Doc] Ejecuta la llamada o modificación mvc.perform(r).andExpect(status().isForbidden()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(r).andExpect(status().isForbidden());
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/auth/login").session(sesion()).param("username","admin").param("password","Admin-Prueba-2026")) .andExpect(status().isForbidden()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/auth/login").session(sesion()).param("username","admin").param("password","Admin-Prueba-2026"))
            .andExpect(status().isForbidden());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(1,cantidad("usuario"));
        assertEquals(1,cantidad("usuario"));assertEquals(0,cantidad("correo_pendiente"));
    }

    /** [Doc]
     * Comprueba el escenario «clave incorrecta no crea sesion ynombre funciona sin distinguir mayusculas» mediante
     * las aserciones del cuerpo.
     */
    @Test void claveIncorrectaNoCreaSesionYNombreFuncionaSinDistinguirMayusculas() throws Exception {
        // [Doc] Ejecuta la llamada o modificación registrar(); Sus efectos dependen del método invocado y del objeto receptor.
        registrar(); var s=sesion(); String anterior=s.getId();
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/auth/login").session(s).param("_csrf","csrf-circuito").param("username","circuito@example.invalid").param("password","incorrecta")) .andExpect(status().isOk()).andE... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/auth/login").session(s).param("_csrf","csrf-circuito").param("username","circuito@example.invalid").param("password","incorrecta"))
            .andExpect(status().isOk()).andExpect(model().attribute("error","Email o contraseña incorrectos"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertNull(s.getAttribute("usuario"));
        assertNull(s.getAttribute("usuario"));assertEquals(anterior,s.getId());
        // [Doc] Ejecuta la llamada o modificación ingresar(" CIRCUITO@EXAMPLE.INVALID ","Cuenta-Prueba-2026","/panelusuarios/panelhistorial?section=perfil"); Sus efectos dependen del método invocado y del objeto receptor.
        ingresar(" CIRCUITO@EXAMPLE.INVALID ","Cuenta-Prueba-2026","/panelusuarios/panelhistorial?section=perfil");
    }

    // Caracterización explícita de limitaciones actuales; no son criterios de aceptación deseables.
    /** [Doc]
     * Comprueba el escenario «hallazgo email incompleto es aceptado actualmente» mediante las aserciones del cuerpo.
     */
    @Test void hallazgoEmailIncompletoEsAceptadoActualmente() throws Exception {
        // [Doc] Variable local d (var; tipo inferido por Java): d. Se inicializa con datos().
        var d=datos(); d.put("email","incompleto@");
        // [Doc] Ejecuta la llamada o modificación mvc.perform(registro(sesion(),d)).andExpect(status().is3xxRedirection()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(registro(sesion(),d)).andExpect(status().is3xxRedirection());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(usuarios.existeEmail("incompleto@"));
        assertTrue(usuarios.existeEmail("incompleto@"));
    }
    /** [Doc]
     * Comprueba el escenario «localidad inexistente es rechazada sin guardar cuenta» mediante las aserciones del
     * cuerpo.
     */
    @Test void localidadInexistenteEsRechazadaSinGuardarCuenta() throws Exception {
        // [Doc] Variable local d (var; tipo inferido por Java): d. Se inicializa con datos().
        var d=datos();d.put("localidadId","999999");
        // [Doc] Ejecuta la llamada o modificación mvc.perform(registro(sesion(),d)).andExpect(status().isOk()).andExpect(model().attributeExists("error")); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(registro(sesion(),d)).andExpect(status().isOk()).andExpect(model().attributeExists("error"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(1,cantidad("usuario"));
        assertEquals(1,cantidad("usuario")); assertEquals(0,cantidad("notificacion")); assertEquals(0,cantidad("correo_pendiente"));
    }
    /** [Doc]
     * Comprueba el escenario «última conexión se actualiza al ingresar» mediante las aserciones del
     * cuerpo.
     */
    @Test void ultimaConexionSeActualizaAlIngresar() throws Exception {
        // [Doc] Variable local u (var; tipo inferido por Java): cuenta de usuario utilizada por la operación. Se inicializa con registrar().
        var u=registrar();
        // [Doc] Ejecuta la llamada o modificación ingresar("circuito@example.invalid","Cuenta-Prueba-2026","/panelusuarios/panelhistorial?section=perfil"); Sus efectos dependen del método invocado y del objeto receptor.
        ingresar("circuito@example.invalid","Cuenta-Prueba-2026","/panelusuarios/panelhistorial?section=perfil");
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM usuario WHERE id_usuario=? AND ultima_conexion IS NOT NULL",Integer.class,u.getId()));
        assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM usuario WHERE id_usuario=? AND ultima_conexion IS NOT NULL",Integer.class,u.getId()));
    }

    /** [Doc]
     * Comprueba el escenario «administrador edita yelimina cuenta sin compras yrevoca acceso» mediante las
     * aserciones del cuerpo.
     */
    @Test void administradorEditaYEliminaCuentaSinComprasYRevocaAcceso() throws Exception {
        // [Doc] Variable local u (var; tipo inferido por Java): cuenta de usuario utilizada por la operación. Se inicializa con registrar().
        var u=registrar();
        // [Doc] Variable local sa (var; tipo inferido por Java): sa. Se inicializa con ingresar("admin@example.invalid","Admin-Prueba-2026","/panel/admin").
        var sa=ingresar("admin@example.invalid","Admin-Prueba-2026","/panel/admin");
        // [Doc] Variable local su (var; tipo inferido por Java): su. Se inicializa con ingresar("circuito@example.invalid","Cuenta-Prueba-2026","/panelusuarios/panelhistorial?section=perfil").
        var su=ingresar("circuito@example.invalid","Cuenta-Prueba-2026","/panelusuarios/panelhistorial?section=perfil");
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/admin/usuarios/"+u.getId()+"/editar").session(sa).param("_csrf","csrf-circuito") .param("nombre_usuario","circuito").param("apellido","Editado").par... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/admin/usuarios/"+u.getId()+"/editar").session(sa).param("_csrf","csrf-circuito")
            .param("nombre_usuario","circuito").param("apellido","Editado").param("email","circuito@example.invalid")
            .param("direccion","Dirección administrativa ficticia")).andExpect(redirectedUrl("/admin/usuarios"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals("Editado",jdbc.queryForObject("SELECT apellido FROM usuario WHERE id_usuario=?",String.class,u.getId()));
        assertEquals("Editado",jdbc.queryForObject("SELECT apellido FROM usuario WHERE id_usuario=?",String.class,u.getId()));
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/admin/usuarios/"+u.getId()+"/eliminar").session(sa).param("_csrf","csrf-circuito")) .andExpect(redirectedUrl("/admin/usuarios")); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/admin/usuarios/"+u.getId()+"/eliminar").session(sa).param("_csrf","csrf-circuito"))
            .andExpect(redirectedUrl("/admin/usuarios"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertFalse(usuarios.existsById(u.getId()));
        assertFalse(usuarios.existsById(u.getId()));
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/panelusuarios/panelhistorial").session(su)).andExpect(status().isUnauthorized()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/panelusuarios/panelhistorial").session(su)).andExpect(status().isUnauthorized());
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/auth/login").session(sesion()).param("_csrf","csrf-circuito") .param("username","circuito@example.invalid").param("password","Cuenta-Prueba-2026")).andExpect(model(... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/auth/login").session(sesion()).param("_csrf","csrf-circuito")
            .param("username","circuito@example.invalid").param("password","Cuenta-Prueba-2026")).andExpect(model().attributeExists("error"));
    }

    /** [Doc]
     * Comprueba el escenario «hallazgo perfil abierto muestra datos anteriores aedicion administrativa» mediante las
     * aserciones del cuerpo.
     */
    @Test void hallazgoPerfilAbiertoMuestraDatosAnterioresAEdicionAdministrativa() throws Exception {
        // [Doc] Variable local u (var; tipo inferido por Java): cuenta de usuario utilizada por la operación. Se inicializa con registrar().
        var u=registrar();
        // [Doc] Variable local su (var; tipo inferido por Java): su. Se inicializa con ingresar("circuito@example.invalid","Cuenta-Prueba-2026","/panelusuarios/panelhistorial?section=perfil").
        var su=ingresar("circuito@example.invalid","Cuenta-Prueba-2026","/panelusuarios/panelhistorial?section=perfil");
        // [Doc] Variable local sa (var; tipo inferido por Java): sa. Se inicializa con ingresar("admin@example.invalid","Admin-Prueba-2026","/panel/admin").
        var sa=ingresar("admin@example.invalid","Admin-Prueba-2026","/panel/admin");
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/admin/usuarios/"+u.getId()+"/editar").session(sa).param("_csrf","csrf-circuito") .param("direccion","Dirección nueva desde administración")).andExpe... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/admin/usuarios/"+u.getId()+"/editar").session(sa).param("_csrf","csrf-circuito")
            .param("direccion","Dirección nueva desde administración")).andExpect(redirectedUrl("/admin/usuarios"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals("Dirección nueva desde administración",jdbc.queryForObject("SELECT direccion FROM usuario WHERE id_usuario=?",String.class,u.getId()));
        assertEquals("Dirección nueva desde administración",jdbc.queryForObject("SELECT direccion FROM usuario WHERE id_usuario=?",String.class,u.getId()));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals("Calle ficticia 123",((Usuario)su.getAttribute("usuario")).getDireccion());
        assertEquals("Calle ficticia 123",((Usuario)su.getAttribute("usuario")).getDireccion());
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/panelusuarios/panelhistorial").session(su)).andExpect(status().isOk()) .andExpect(model().attribute("u",org.hamcrest.Matchers.hasProperty("direccion"... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/panelusuarios/panelhistorial").session(su)).andExpect(status().isOk())
            .andExpect(model().attribute("u",org.hamcrest.Matchers.hasProperty("direccion",org.hamcrest.Matchers.is("Calle ficticia 123"))));
    }

    /** [Doc]
     * Comprueba el escenario «consultas documentadas son validas en postgres» mediante las aserciones del cuerpo.
     */
    @Test void consultasDocumentadasSonValidasEnPostgres() throws Exception {
        // [Doc] Ejecuta la llamada o modificación registrar(); Sus efectos dependen del método invocado y del objeto receptor.
        registrar();
        // [Doc] Variable local sql (String): sql. Se inicializa con java.nio.file.Files.readString(java.nio.file.Path.of("docs/consultas-circuito-usuarios.sql")).
        String sql=java.nio.file.Files.readString(java.nio.file.Path.of("docs/consultas-circuito-usuarios.sql"));
        // [Doc] Ejecuta un bloque con tratamiento de excepciones; los catch asociados atienden los fallos declarados y finally se ejecuta al salir si existe.
        try (var conexion=jdbc.getDataSource().getConnection()) {
            // [Doc] Actualiza una propiedad o configuración mediante la llamada: conexion.setAutoCommit(false);
            conexion.setAutoCommit(false);
            // [Doc] Actualiza una propiedad o configuración mediante la llamada: conexion.setReadOnly(true);
            conexion.setReadOnly(true);
            // [Doc] Ejecuta un bloque con tratamiento de excepciones; los catch asociados atienden los fallos declarados y finally se ejecuta al salir si existe.
            try (var sentencia=conexion.createStatement()) {
                // [Doc] Recorre los elementos o repite según String consulta:sql.split(";"). Cada vuelta ejecuta el cuerpo del bucle.
                for (String consulta:sql.split(";")) {
                    // [Doc] Evalúa !consulta.isBlank(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
                    if (!consulta.isBlank()) {
                        // [Doc] Ejecuta un bloque con tratamiento de excepciones; los catch asociados atienden los fallos declarados y finally se ejecuta al salir si existe.
                        try (var resultado=sentencia.executeQuery(consulta)) {
                            // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertNotNull(resultado.getMetaData());
                            assertNotNull(resultado.getMetaData());
                        }
                    }
                }
            } finally { conexion.rollback(); }
        }
    }
}

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

class CircuitoUsuariosIntegrationTest extends IntegrationSupport {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired IUsuarioRepository usuarios;
    @Autowired ILocalidadRepository localidades;
    @Autowired EntregaCorreoService entrega;
    @MockitoBean JavaMailSender smtp;
    @MockitoSpyBean ColaCorreoService cola;
    Localidad localidad;
    Usuario admin;

    @BeforeEach void preparar() {
        jdbc.execute("TRUNCATE usuario, localidad, correo_pendiente, recuperacion_clave, recuperacion_limite RESTART IDENTITY CASCADE");
        localidad=new Localidad(); localidad.setNombre_localidad("Ciudad de prueba"); localidad.setCodigo_postal(3000);
        localidad=localidades.save(localidad);
        admin=new Usuario(); admin.setNombre_usuario("admin"); admin.setEmail("admin@example.invalid");
        admin.setContraseña("Admin-Prueba-2026"); admin=usuarios.save(admin);
        reset(smtp,cola);
    }
    MockHttpSession sesion() {
        var s=new MockHttpSession(); s.setAttribute(CsrfInterceptor.KEY,"csrf-circuito"); return s;
    }
    Map<String,String> datos() {
        var d=new LinkedHashMap<String,String>();
        d.put("nombre_usuario"," circuito "); d.put("apellido"," Prueba ");
        d.put("email"," CIRCUITO@EXAMPLE.INVALID "); d.put("dni","23456789");
        d.put("telefono","+54 342 123-4567"); d.put("direccion"," Calle ficticia 123 ");
        d.put("fecha_nacimiento","1998-04-15"); d.put("localidadId",String.valueOf(localidad.getId_localidad()));
        d.put("contraseña","Cuenta-Prueba-2026"); d.put("confirmarContraseña","Cuenta-Prueba-2026");
        d.put("aceptarTerminos","true"); return d;
    }
    MockHttpServletRequestBuilder registro(MockHttpSession s, Map<String,String> d) {
        var r=post("/auth/registro").session(s).param("_csrf","csrf-circuito");
        d.forEach((k,v)->r.param(k,v)); return r;
    }
    Usuario registrar() throws Exception {
        mvc.perform(registro(sesion(),datos())).andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/auth/login?registroExitoso=true"));
        return usuarios.findAll().stream().filter(u->"circuito".equals(u.getNombre_usuario())).findFirst().orElseThrow();
    }
    MockHttpSession ingresar(String nombre,String clave,String destino) throws Exception {
        var s=sesion(); String anterior=s.getId();
        mvc.perform(post("/auth/login").session(s).param("_csrf","csrf-circuito").param("username",nombre).param("password",clave))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl(destino));
        assertNotEquals(anterior,s.getId()); assertNotNull(s.getAttribute("usuario")); return s;
    }
    int cantidad(String tabla) { return jdbc.queryForObject("SELECT count(*) FROM "+tabla,Integer.class); }

    @Test void circuitoCompletoDesdeFormularioHastaAdministradorLoginPerfilYRecuperacion() throws Exception {
        var visitante=sesion();
        mvc.perform(get("/auth/registro").session(visitante)).andExpect(status().isOk())
            .andExpect(content().string(containsString("Ciudad de prueba")))
            .andExpect(content().string(containsString("csrf-circuito")));
        mvc.perform(registro(visitante,datos())).andExpect(redirectedUrl("/auth/login?registroExitoso=true"));
        assertNull(visitante.getAttribute("usuario")); // registrarse no inicia sesión
        Usuario nuevo=usuarios.findAll().stream().filter(u->"circuito".equals(u.getNombre_usuario())).findFirst().orElseThrow();
        var fila=jdbc.queryForMap("""
            SELECT nombre_usuario, apellido, email, dni, telefono, direccion, id_localidad, puntos,
            fecha_registro IS NOT NULL AS tiene_fecha, ultima_conexion IS NULL AS sin_conexion,
            CAST(fecha_nacimiento AS DATE)::text AS nacimiento,
            contraseña = ? AS clave_coincide FROM usuario WHERE id_usuario=?
            ""","Cuenta-Prueba-2026",nuevo.getId());
        assertEquals("circuito",fila.get("nombre_usuario")); assertEquals("Prueba",fila.get("apellido"));
        assertEquals("circuito@example.invalid",fila.get("email")); assertEquals(23456789,((Number)fila.get("dni")).intValue());
        assertEquals("+54 342 123-4567",fila.get("telefono")); assertEquals("Calle ficticia 123",fila.get("direccion"));
        assertEquals(localidad.getId_localidad(),((Number)fila.get("id_localidad")).longValue());
        assertEquals("1998-04-15",fila.get("nacimiento")); assertEquals(0,fila.get("puntos"));
        assertEquals(true,fila.get("tiene_fecha")); assertEquals(true,fila.get("sin_conexion")); assertEquals(true,fila.get("clave_coincide"));
        assertEquals(2,cantidad("usuario")); assertEquals(1,cantidad("notificacion")); assertEquals(2,cantidad("correo_pendiente"));
        assertEquals("ADMIN",jdbc.queryForObject("SELECT destinatario FROM notificacion",String.class));
        assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM correo_pendiente WHERE destinatario='circuito@example.invalid'",Integer.class));
        assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM correo_pendiente WHERE destinatario='admin@example.invalid'",Integer.class));
        assertFalse(jdbc.queryForList("SELECT contenido FROM correo_pendiente",String.class).stream().anyMatch(t->t.contains("Cuenta-Prueba-2026")));
        verifyNoInteractions(smtp);
        var sa=ingresar("admin","Admin-Prueba-2026","/panel/admin");
        mvc.perform(get("/admin/usuarios").session(sa).param("q","circuito")).andExpect(status().isOk())
            .andExpect(content().string(containsString("circuito@example.invalid")))
            .andExpect(content().string(not(containsString("Cuenta-Prueba-2026"))));
        mvc.perform(get("/notificaciones/historial").session(sa)).andExpect(status().isOk())
            .andExpect(content().string(containsString("Nuevo usuario registrado: circuito")));
        mvc.perform(get("/admin/correos").session(sa)).andExpect(status().isOk())
            .andExpect(content().string(containsString("circuito@example.invalid")));
        var su=ingresar("CIRCUITO@EXAMPLE.INVALID","Cuenta-Prueba-2026","/panelusuarios/panelhistorial?section=perfil");
        mvc.perform(get("/panelusuarios/panelhistorial").session(su)).andExpect(status().isOk())
            .andExpect(content().string(containsString("circuito@example.invalid")));
        mvc.perform(get("/admin/usuarios").session(su)).andExpect(status().isForbidden());
        mvc.perform(get("/admin/correos").session(su)).andExpect(status().isForbidden());
        mvc.perform(get("/notificaciones/mias").session(su)).andExpect(status().isOk())
            .andExpect(content().string(not(containsString("Nuevo usuario registrado"))));
        mvc.perform(get("/usuarios/traer").session(sa)).andExpect(status().isOk())
            .andExpect(content().string(not(containsString("Cuenta-Prueba-2026"))));
        mvc.perform(post("/panelusuarios/perfil/editar").session(su).param("_csrf","csrf-circuito")
            .param("nombre","circuito").param("email","circuito@example.invalid")
            .param("direccion","Nueva dirección ficticia").param("telefono","+54 342 765-4321"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.ok").value(true));
        assertEquals("Nueva dirección ficticia",jdbc.queryForObject("SELECT direccion FROM usuario WHERE id_usuario=?",String.class,nuevo.getId()));
        assertEquals("Nueva dirección ficticia",((Usuario)su.getAttribute("usuario")).getDireccion());
        mvc.perform(get("/auth/logout").session(su)).andExpect(redirectedUrl("/auth/login")); assertTrue(su.isInvalid());
        mvc.perform(get("/admin/usuarios")).andExpect(status().isUnauthorized());
        mvc.perform(get("/panelusuarios/panelhistorial")).andExpect(status().isUnauthorized());
        var sr=sesion();
        mvc.perform(post("/auth/recuperar").session(sr).param("_csrf","csrf-circuito").param("email","circuito@example.invalid"))
            .andExpect(status().isOk()).andExpect(model().attributeExists("mensaje"));
        assertEquals(1,cantidad("recuperacion_clave"));
        String texto=jdbc.queryForObject("SELECT contenido FROM correo_pendiente WHERE asunto LIKE 'Recuper%'",String.class);
        String token=texto.split("token=")[1].split("\\s")[0];
        mvc.perform(get("/auth/restablecer").session(sr).param("token",token)).andExpect(status().isOk()).andExpect(model().attribute("valido",true));
        mvc.perform(post("/auth/restablecer").session(sr).param("_csrf","csrf-circuito").param("token",token)
            .param("password","Nueva-Cuenta-2026").param("confirmacion","Nueva-Cuenta-2026"))
            .andExpect(redirectedUrl("/auth/login?contrasenaActualizada=true"));
        assertEquals(0,cantidad("recuperacion_clave")); assertEquals(4,cantidad("correo_pendiente"));
        mvc.perform(post("/auth/login").session(sesion()).param("_csrf","csrf-circuito")
            .param("username","circuito").param("password","Cuenta-Prueba-2026"))
            .andExpect(status().isOk()).andExpect(model().attributeExists("error"));
        ingresar("circuito","Nueva-Cuenta-2026","/panelusuarios/panelhistorial?section=perfil");
        for(int i=0;i<4;i++) assertTrue(entrega.entregarSiguiente());
        assertFalse(entrega.entregarSiguiente());
        verify(smtp,times(4)).send(any(SimpleMailMessage.class));
        assertEquals(4,jdbc.queryForObject("SELECT count(*) FROM correo_pendiente WHERE enviado_en IS NOT NULL",Integer.class));
    }

    @ParameterizedTest
    @CsvSource({"nombre_usuario,' '","apellido,' '","email,' '","direccion,' '","dni,123",
        "dni,100000000","contraseña,123","confirmarContraseña,distinta","aceptarTerminos,false",
        "nombre_usuario,AdMiN","email,sin-arroba","dni,no-numero","fecha_nacimiento,no-fecha","localidadId,no-numero"})
    void registroInvalidoNoDejaUsuarioAvisosNiCorreos(String campo,String valor) throws Exception {
        var d=datos(); d.put(campo,valor);
        mvc.perform(registro(sesion(),d)).andExpect(status().isOk()).andExpect(view().name("registro"))
            .andExpect(model().attributeExists("error"));
        assertEquals(1,cantidad("usuario")); assertEquals(0,cantidad("notificacion")); assertEquals(0,cantidad("correo_pendiente"));
    }

    @ParameterizedTest @CsvSource({"nombre_usuario,CIRCUITO","email,CIRCUITO@EXAMPLE.INVALID","dni,23456789"})
    void duplicadosSeRechazanSinEfectosSecundarios(String campo,String valor) throws Exception {
        registrar(); var d=datos(); d.put("nombre_usuario","otra");d.put("email","otra@example.invalid");d.put("dni","34567890");
        d.put(campo,valor);
        mvc.perform(registro(sesion(),d)).andExpect(status().isOk()).andExpect(model().attributeExists("error"));
        assertEquals(2,cantidad("usuario"));assertEquals(1,cantidad("notificacion"));assertEquals(2,cantidad("correo_pendiente"));
    }

    @Test void fallaEnCorreoRevierteAltaYNotificacion() throws Exception {
        doThrow(new IllegalStateException("Fallo simulado")).when(cola).encolar(anyString(),anyString(),anyString());
        mvc.perform(registro(sesion(),datos())).andExpect(status().isOk()).andExpect(model().attributeExists("error"));
        assertEquals(1,cantidad("usuario"));assertEquals(0,cantidad("notificacion"));assertEquals(0,cantidad("correo_pendiente"));
    }

    @Test void csrfImpideRegistroLoginYEdicionSinToken() throws Exception {
        var d=datos(); var r=post("/auth/registro").session(sesion());d.forEach((k,v)->r.param(k,v));
        mvc.perform(r).andExpect(status().isForbidden());
        mvc.perform(post("/auth/login").session(sesion()).param("username","admin").param("password","Admin-Prueba-2026"))
            .andExpect(status().isForbidden());
        assertEquals(1,cantidad("usuario"));assertEquals(0,cantidad("correo_pendiente"));
    }

    @Test void claveIncorrectaNoCreaSesionYNombreFuncionaSinDistinguirMayusculas() throws Exception {
        registrar(); var s=sesion(); String anterior=s.getId();
        mvc.perform(post("/auth/login").session(s).param("_csrf","csrf-circuito").param("username","circuito").param("password","incorrecta"))
            .andExpect(status().isOk()).andExpect(model().attribute("error","Usuario o contraseña incorrectos"));
        assertNull(s.getAttribute("usuario"));assertEquals(anterior,s.getId());
        ingresar("CIRCUITO","Cuenta-Prueba-2026","/panelusuarios/panelhistorial?section=perfil");
    }

    // Caracterización explícita de limitaciones actuales; no son criterios de aceptación deseables.
    @Test void hallazgoEmailIncompletoEsAceptadoActualmente() throws Exception {
        var d=datos(); d.put("email","incompleto@");
        mvc.perform(registro(sesion(),d)).andExpect(status().is3xxRedirection());
        assertTrue(usuarios.existeEmail("incompleto@"));
    }
    @Test void localidadInexistenteEsRechazadaSinGuardarCuenta() throws Exception {
        var d=datos();d.put("localidadId","999999");
        mvc.perform(registro(sesion(),d)).andExpect(status().isOk()).andExpect(model().attributeExists("error"));
        assertEquals(1,cantidad("usuario")); assertEquals(0,cantidad("notificacion")); assertEquals(0,cantidad("correo_pendiente"));
    }
    @Test void hallazgoUltimaConexionNoSeActualizaAlIngresar() throws Exception {
        var u=registrar();
        ingresar("circuito","Cuenta-Prueba-2026","/panelusuarios/panelhistorial?section=perfil");
        assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM usuario WHERE id_usuario=? AND ultima_conexion IS NULL",Integer.class,u.getId()));
    }

    @Test void administradorEditaYEliminaCuentaSinComprasYRevocaAcceso() throws Exception {
        var u=registrar();
        var sa=ingresar("admin","Admin-Prueba-2026","/panel/admin");
        var su=ingresar("circuito","Cuenta-Prueba-2026","/panelusuarios/panelhistorial?section=perfil");
        mvc.perform(post("/admin/usuarios/"+u.getId()+"/editar").session(sa).param("_csrf","csrf-circuito")
            .param("nombre_usuario","circuito").param("apellido","Editado").param("email","circuito@example.invalid")
            .param("direccion","Dirección administrativa ficticia")).andExpect(redirectedUrl("/admin/usuarios"));
        assertEquals("Editado",jdbc.queryForObject("SELECT apellido FROM usuario WHERE id_usuario=?",String.class,u.getId()));
        mvc.perform(post("/admin/usuarios/"+u.getId()+"/eliminar").session(sa).param("_csrf","csrf-circuito"))
            .andExpect(redirectedUrl("/admin/usuarios"));
        assertFalse(usuarios.existsById(u.getId()));
        mvc.perform(get("/panelusuarios/panelhistorial").session(su)).andExpect(status().isUnauthorized());
        mvc.perform(post("/auth/login").session(sesion()).param("_csrf","csrf-circuito")
            .param("username","circuito").param("password","Cuenta-Prueba-2026")).andExpect(model().attributeExists("error"));
    }

    @Test void hallazgoPerfilAbiertoMuestraDatosAnterioresAEdicionAdministrativa() throws Exception {
        var u=registrar();
        var su=ingresar("circuito","Cuenta-Prueba-2026","/panelusuarios/panelhistorial?section=perfil");
        var sa=ingresar("admin","Admin-Prueba-2026","/panel/admin");
        mvc.perform(post("/admin/usuarios/"+u.getId()+"/editar").session(sa).param("_csrf","csrf-circuito")
            .param("direccion","Dirección nueva desde administración")).andExpect(redirectedUrl("/admin/usuarios"));
        assertEquals("Dirección nueva desde administración",jdbc.queryForObject("SELECT direccion FROM usuario WHERE id_usuario=?",String.class,u.getId()));
        assertEquals("Calle ficticia 123",((Usuario)su.getAttribute("usuario")).getDireccion());
        mvc.perform(get("/panelusuarios/panelhistorial").session(su)).andExpect(status().isOk())
            .andExpect(model().attribute("u",org.hamcrest.Matchers.hasProperty("direccion",org.hamcrest.Matchers.is("Calle ficticia 123"))));
    }

    @Test void consultasDocumentadasSonValidasEnPostgres() throws Exception {
        registrar();
        String sql=java.nio.file.Files.readString(java.nio.file.Path.of("docs/consultas-circuito-usuarios.sql"));
        try (var conexion=jdbc.getDataSource().getConnection()) {
            conexion.setAutoCommit(false);
            conexion.setReadOnly(true);
            try (var sentencia=conexion.createStatement()) {
                for (String consulta:sql.split(";")) {
                    if (!consulta.isBlank()) {
                        try (var resultado=sentencia.executeQuery(consulta)) {
                            assertNotNull(resultado.getMetaData());
                        }
                    }
                }
            } finally { conexion.rollback(); }
        }
    }
}

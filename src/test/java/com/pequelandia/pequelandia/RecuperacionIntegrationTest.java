package com.pequelandia.pequelandia;

import com.pequelandia.pequelandia.config.CsrfInterceptor;
import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.repository.IUsuarioRepository;
import com.pequelandia.pequelandia.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** [Doc]
 * Agrupa pruebas automatizadas de recuperacion.
 */
@org.springframework.test.context.TestPropertySource(properties = "server.port=8091")
class RecuperacionIntegrationTest extends IntegrationSupport {
    // [Doc] Atributo recuperacion (RecuperacionService): colaborador que concentra operaciones de recuperacion.
    @Autowired RecuperacionService recuperacion;
    // [Doc] Atributo entrega (EntregaCorreoService): colaborador que concentra operaciones de entrega correo.
    @Autowired EntregaCorreoService entrega;
    // [Doc] Atributo usuarios (IUsuarioRepository): acceso de persistencia a usuario.
    @Autowired IUsuarioRepository usuarios;
    // [Doc] Atributo jdbc (JdbcTemplate): acceso SQL parametrizado mediante JdbcTemplate.
    @Autowired JdbcTemplate jdbc;
    // [Doc] Atributo mvc (MockMvc): mvc.
    @Autowired MockMvc mvc;
    // [Doc] Atributo smtp (JavaMailSender): smtp.
    @MockitoBean JavaMailSender smtp;
    // [Doc] Atributo cola (ColaCorreoService): colaborador que concentra operaciones de cola correo.
    @MockitoSpyBean ColaCorreoService cola;
    // [Doc] Atributo usuario (Usuario): cuenta de usuario utilizada por la operación.
    Usuario usuario;

    /** [Doc]
     * Prepara o auxilia el escenario de prueba: preparar.
     */
    @BeforeEach void preparar() {
        // [Doc] Ejecuta la llamada o modificación jdbc.execute("TRUNCATE usuario, correo_pendiente, recuperacion_clave, recuperacion_limite RESTART IDENTITY CASCADE"); Sus efectos dependen del método invocado y del objeto receptor.
        jdbc.execute("TRUNCATE usuario, correo_pendiente, recuperacion_clave, recuperacion_limite RESTART IDENTITY CASCADE");
        // [Doc] Ejecuta la llamada o modificación usuario = new Usuario(); Sus efectos dependen del método invocado y del objeto receptor.
        usuario = new Usuario();
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: usuario.setNombre_usuario("prueba");
        usuario.setNombre_usuario("prueba"); usuario.setEmail("prueba@example.invalid");
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: usuario.setContraseña("original");
        usuario.setContraseña("original"); usuario = usuarios.save(usuario);
        // [Doc] Ejecuta la llamada o modificación reset(cola, smtp); Sus efectos dependen del método invocado y del objeto receptor.
        reset(cola, smtp);
    }

    /** [Doc]
     * Prepara o auxilia el escenario de prueba: token.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    String token() {
        // [Doc] Variable local texto (String): contenido textual que se construye para presentar o enviar. Se inicializa con jdbc.queryForObject("SELECT contenido FROM correo_pendiente WHERE asunto LIKE 'Recuper%' ORDER BY id DESC L....
        String texto = jdbc.queryForObject("SELECT contenido FROM correo_pendiente WHERE asunto LIKE 'Recuper%' ORDER BY id DESC LIMIT 1", String.class);
        // [Doc] Termina el método o la función y devuelve texto.split("token=")[1].split("\\s")[0].
        return texto.split("token=")[1].split("\\s")[0];
    }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: correos.
     * @return int con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    int correos() { return jdbc.queryForObject("SELECT count(*) FROM correo_pendiente", Integer.class); }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: sesion.
     * @return MockHttpSession con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    MockHttpSession sesion() {
        // [Doc] Variable local s (var; tipo inferido por Java): s. Se inicializa con new MockHttpSession().
        var s = new MockHttpSession(); s.setAttribute(CsrfInterceptor.KEY, "csrf"); return s;
    }
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: pedir.
     */
    void pedir() { recuperacion.solicitar(usuario.getEmail(), "127.0.0.1"); }


    /** [Doc]
     * Comprueba el escenario «entrega recuperacion con spring mail yurl local del puerto configurado» mediante las
     * aserciones del cuerpo.
     */
    @Test void entregaRecuperacionConSpringMailYUrlLocalDelPuertoConfigurado() {
        // [Doc] Ejecuta la llamada o modificación pedir(); Sus efectos dependen del método invocado y del objeto receptor.
        pedir();
        // [Doc] Variable local t (String): t. Se inicializa con token().
        String t = token();
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(entrega.entregarSiguiente());
        assertTrue(entrega.entregarSiguiente());
        // [Doc] Variable local mensaje (var; tipo inferido por Java): mensaje que se prepara para enviar o responder. Se inicializa con org.mockito.ArgumentCaptor.forClass(org.springframework.mail.SimpleMailMessage.class).
        var mensaje = org.mockito.ArgumentCaptor.forClass(org.springframework.mail.SimpleMailMessage.class);
        // [Doc] Ejecuta la llamada o modificación verify(smtp).send(mensaje.capture()); Sus efectos dependen del método invocado y del objeto receptor.
        verify(smtp).send(mensaje.capture());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals("pruebas@example.invalid", mensaje.getValue().getFrom());
        assertEquals("pruebas@example.invalid", mensaje.getValue().getFrom());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertArrayEquals(new String[] { usuario.getEmail() }, mensaje.getValue().getTo());
        assertArrayEquals(new String[] { usuario.getEmail() }, mensaje.getValue().getTo());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(mensaje.getValue().getText().contains("http://localhost:8091/auth/restablecer?token=" + t));
        assertTrue(mensaje.getValue().getText().contains("http://localhost:8091/auth/restablecer?token=" + t));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM correo_pendiente WHERE enviado_en IS NOT NULL", Integer.class));
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM correo_pendiente WHERE enviado_en IS NOT NULL", Integer.class));
    }
    /** [Doc]
     * Comprueba el escenario «flujo completo correo hash yuso unico» mediante las aserciones del cuerpo.
     */
    @Test void flujoCompletoCorreoHashYUsoUnico() throws Exception {
        // [Doc] Ejecuta la llamada o modificación pedir(); Sus efectos dependen del método invocado y del objeto receptor.
        pedir(); String t = token();
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(43, t.length());
        assertEquals(43, t.length());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertNotEquals(t, jdbc.queryForObject("SELECT token_hash FROM recuperacion_clave", String.class));
        assertNotEquals(t, jdbc.queryForObject("SELECT token_hash FROM recuperacion_clave", String.class));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(recuperacion.vigente(t));
        assertTrue(recuperacion.vigente(t));
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/auth/restablecer").param("token", t)) .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Crear contraseña n... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/auth/restablecer").param("token", t))
            .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Crear contraseña nueva")))
            .andExpect(header().string("Cache-Control", "no-store"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(recuperacion.restablecer(t, "nueva-segura", "nueva-segura", "127.0.0.1"));
        assertTrue(recuperacion.restablecer(t, "nueva-segura", "nueva-segura", "127.0.0.1"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals("nueva-segura", usuarios.findById(usuario.getId()).orElseThrow().getContraseña());
        assertEquals("nueva-segura", usuarios.findById(usuario.getId()).orElseThrow().getContraseña());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(usuarios.login("prueba@example.invalid", "nueva-segura").isPresent());
        assertTrue(usuarios.login("prueba@example.invalid", "nueva-segura").isPresent());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertFalse(usuarios.login("prueba@example.invalid", "original").isPresent());
        assertFalse(usuarios.login("prueba@example.invalid", "original").isPresent());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertFalse(recuperacion.vigente(t));
        assertFalse(recuperacion.vigente(t));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertFalse(recuperacion.restablecer(t, "otra-clave", "otra-clave", "127.0.0.1"));
        assertFalse(recuperacion.restablecer(t, "otra-clave", "otra-clave", "127.0.0.1"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(2, correos());
        assertEquals(2, correos());
        // [Doc] Ejecuta la llamada o modificación verifyNoInteractions(smtp); Sus efectos dependen del método invocado y del objeto receptor.
        verifyNoInteractions(smtp);
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertFalse(jdbc.queryForObject("SELECT contenido FROM correo_pendiente ORDER BY id DESC LIMIT 1", String.class).contains("nueva-segura"));
        assertFalse(jdbc.queryForObject("SELECT contenido FROM correo_pendiente ORDER BY id DESC LIMIT 1", String.class).contains("nueva-segura"));
    }

    /** [Doc]
     * Comprueba el escenario «vencimiento ycorreo modificado invalidan enlace» mediante las aserciones del cuerpo.
     */
    @Test void vencimientoYCorreoModificadoInvalidanEnlace() {
        // [Doc] Ejecuta la llamada o modificación pedir(); Sus efectos dependen del método invocado y del objeto receptor.
        pedir(); String t = token();
        // [Doc] Ejecuta una modificación SQL con los parámetros indicados; participa en la transacción si existe.
        jdbc.update("UPDATE recuperacion_clave SET vence=clock_timestamp()-INTERVAL '1 second'");
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertFalse(recuperacion.vigente(t));
        assertFalse(recuperacion.vigente(t));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertFalse(recuperacion.restablecer(t, "nueva-segura", "nueva-segura", "127.0.0.1"));
        assertFalse(recuperacion.restablecer(t, "nueva-segura", "nueva-segura", "127.0.0.1"));
        // [Doc] Ejecuta una modificación SQL con los parámetros indicados; participa en la transacción si existe.
        jdbc.update("UPDATE recuperacion_clave SET vence=clock_timestamp()+INTERVAL '1 minute'");
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: usuario.setEmail("otro@example.invalid");
        usuario.setEmail("otro@example.invalid"); usuarios.saveAndFlush(usuario);
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertFalse(recuperacion.restablecer(t, "nueva-segura", "nueva-segura", "127.0.0.1"));
        assertFalse(recuperacion.restablecer(t, "nueva-segura", "nueva-segura", "127.0.0.1"));
    }

    /** [Doc]
     * Comprueba el escenario «contrasenas invalidas no consumen token» mediante las aserciones del cuerpo.
     */
    @Test void contrasenasInvalidasNoConsumenToken() {
        // [Doc] Ejecuta la llamada o modificación pedir(); Sus efectos dependen del método invocado y del objeto receptor.
        pedir(); String t = token();
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertFalse(recuperacion.restablecer(t, "corta", "corta", "127.0.0.1"));
        assertFalse(recuperacion.restablecer(t, "corta", "corta", "127.0.0.1"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertFalse(recuperacion.restablecer(t, "nueva-segura", "distinta", "127.0.0.1"));
        assertFalse(recuperacion.restablecer(t, "nueva-segura", "distinta", "127.0.0.1"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(recuperacion.vigente(t));
        assertTrue(recuperacion.vigente(t)); assertEquals(1, correos());
    }

    /** [Doc]
     * Comprueba el escenario «respuesta generica yproteccion csrf» mediante las aserciones del cuerpo.
     */
    @Test void respuestaGenericaYProteccionCsrf() throws Exception {
        // [Doc] Variable local s (var; tipo inferido por Java): s. Se inicializa con sesion().
        var s = sesion();
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/auth/recuperar").session(s).param("email", usuario.getEmail())).andExpect(status().isForbidden()); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/auth/recuperar").session(s).param("email", usuario.getEmail())).andExpect(status().isForbidden());
        // [Doc] Variable local real (String): real. Se inicializa con mvc.perform(post("/auth/recuperar").session(s).param("_csrf", "csrf").param("email", usuario.getEmail())) .....
        String real = mvc.perform(post("/auth/recuperar").session(s).param("_csrf", "csrf").param("email", usuario.getEmail()))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        // [Doc] Variable local inexistente (String): inexistente. Se inicializa con mvc.perform(post("/auth/recuperar").session(s).param("_csrf", "csrf").param("email", "nadie@example.invalid....
        String inexistente = mvc.perform(post("/auth/recuperar").session(s).param("_csrf", "csrf").param("email", "nadie@example.invalid"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(real, inexistente);
        assertEquals(real, inexistente);
        // [Doc] Ejecuta la llamada o modificación mvc.perform(get("/auth/restablecer").param("token", "invalido")) .andExpect(status().isOk()).andExpect(model().attribute("valido", false)); Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(get("/auth/restablecer").param("token", "invalido"))
            .andExpect(status().isOk()).andExpect(model().attribute("valido", false));
    }

    /** [Doc]
     * Comprueba el escenario «espera limite correo yreemplazo de token» mediante las aserciones del cuerpo.
     */
    @Test void esperaLimiteCorreoYReemplazoDeToken() {
        // [Doc] Ejecuta la llamada o modificación pedir(); Sus efectos dependen del método invocado y del objeto receptor.
        pedir(); String primero = token();
        // [Doc] Ejecuta la llamada o modificación pedir(); Sus efectos dependen del método invocado y del objeto receptor.
        pedir(); assertEquals(1, correos());
        // [Doc] Ejecuta una modificación SQL con los parámetros indicados; participa en la transacción si existe.
        jdbc.update("UPDATE recuperacion_limite SET inicio=inicio-INTERVAL '61 seconds'");
        // [Doc] Ejecuta la llamada o modificación pedir(); Sus efectos dependen del método invocado y del objeto receptor.
        pedir(); assertEquals(2, correos()); assertFalse(recuperacion.vigente(primero));
        // [Doc] Ejecuta una modificación SQL con los parámetros indicados; participa en la transacción si existe.
        jdbc.update("UPDATE recuperacion_limite SET inicio=inicio-INTERVAL '61 seconds'");
        // [Doc] Ejecuta la llamada o modificación pedir(); Sus efectos dependen del método invocado y del objeto receptor.
        pedir(); assertEquals(2, correos()); // tercer intento ya contabilizado durante la espera
    }

    /** [Doc]
     * Comprueba el escenario «limite ip yde intentos de restablecimiento» mediante las aserciones del cuerpo.
     */
    @Test void limiteIpYDeIntentosDeRestablecimiento() {
        // [Doc] Recorre los elementos o repite según int i=0; i<10; i++. Cada vuelta ejecuta el cuerpo del bucle.
        for (int i=0; i<10; i++) recuperacion.solicitar("nadie"+i+"@example.invalid", "ip");
        // [Doc] Ejecuta la llamada o modificación recuperacion.solicitar(usuario.getEmail(), "ip"); Sus efectos dependen del método invocado y del objeto receptor.
        recuperacion.solicitar(usuario.getEmail(), "ip"); assertEquals(0, correos());
        // [Doc] Ejecuta la llamada o modificación pedir(); Sus efectos dependen del método invocado y del objeto receptor.
        pedir(); String t=token();
        // [Doc] Recorre los elementos o repite según int i=0; i<20; i++. Cada vuelta ejecuta el cuerpo del bucle.
        for (int i=0; i<20; i++) assertFalse(recuperacion.restablecer("invalido", "clave-segura", "clave-segura", "ip"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertFalse(recuperacion.restablecer(t, "clave-segura", "clave-segura", "ip"));
        assertFalse(recuperacion.restablecer(t, "clave-segura", "clave-segura", "ip"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(recuperacion.vigente(t));
        assertTrue(recuperacion.vigente(t));
    }

    /** [Doc]
     * Comprueba el escenario «dos consumos concurrentes solo cambian una vez» mediante las aserciones del cuerpo.
     */
    @Test void dosConsumosConcurrentesSoloCambianUnaVez() throws Exception {
        // [Doc] Ejecuta la llamada o modificación pedir(); Sus efectos dependen del método invocado y del objeto receptor.
        pedir(); String t=token();
        // [Doc] Ejecuta un bloque con tratamiento de excepciones; los catch asociados atienden los fallos declarados y finally se ejecuta al salir si existe.
        try (var pool=Executors.newVirtualThreadPerTaskExecutor()) {
            // [Doc] Variable local inicio (var; tipo inferido por Java): límite inicial del intervalo. Se inicializa con new CountDownLatch(1).
            var inicio=new CountDownLatch(1);
            // [Doc] Variable local tarea (Callable<Boolean>): tarea. Se inicializa con ()-> { inicio.await(); return recuperacion.restablecer(t,"nueva-segura","nueva-segura",Thread.currentThread....
            Callable<Boolean> tarea=()-> { inicio.await(); return recuperacion.restablecer(t,"nueva-segura","nueva-segura",Thread.currentThread().getName()); };
            // [Doc] Variable local a (var; tipo inferido por Java): a. Se inicializa con pool.submit(tarea).
            var a=pool.submit(tarea); var b=pool.submit(tarea); inicio.countDown();
            // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertNotEquals(a.get(15,TimeUnit.SECONDS), b.get(15,TimeUnit.SECONDS));
            assertNotEquals(a.get(15,TimeUnit.SECONDS), b.get(15,TimeUnit.SECONDS));
        }
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals(2, correos());
        assertEquals(2, correos());
    }

    /** [Doc]
     * Comprueba el escenario «fallo en cola revierte cambio yconsumo» mediante las aserciones del cuerpo.
     */
    @Test void falloEnColaRevierteCambioYConsumo() {
        // [Doc] Ejecuta la llamada o modificación pedir(); Sus efectos dependen del método invocado y del objeto receptor.
        pedir(); String t=token();
        // [Doc] Ejecuta la llamada o modificación doThrow(new IllegalStateException("fallo simulado")).when(cola).encolar(anyString(),anyString(),anyString()); Sus efectos dependen del método invocado y del objeto receptor.
        doThrow(new IllegalStateException("fallo simulado")).when(cola).encolar(anyString(),anyString(),anyString());
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertThrows(IllegalStateException.class, ()->recuperacion.restablecer(t,"nueva-segura","nueva-segura","ip"));
        assertThrows(IllegalStateException.class, ()->recuperacion.restablecer(t,"nueva-segura","nueva-segura","ip"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(recuperacion.vigente(t));
        assertTrue(recuperacion.vigente(t));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertEquals("original", usuarios.findById(usuario.getId()).orElseThrow().getContraseña());
        assertEquals("original", usuarios.findById(usuario.getId()).orElseThrow().getContraseña());
    }

    /** [Doc]
     * Comprueba el escenario «post exitoso redirige einvalida sesion» mediante las aserciones del cuerpo.
     */
    @Test void postExitosoRedirigeEInvalidaSesion() throws Exception {
        // [Doc] Ejecuta la llamada o modificación pedir(); Sus efectos dependen del método invocado y del objeto receptor.
        pedir(); var s=sesion();
        // [Doc] Ejecuta la llamada o modificación mvc.perform(post("/auth/restablecer").session(s).param("_csrf","csrf").param("token",token()) .param("password","nueva-segura").param("confirmacion","nueva-segura")) .... Sus efectos dependen del método invocado y del objeto receptor.
        mvc.perform(post("/auth/restablecer").session(s).param("_csrf","csrf").param("token",token())
                .param("password","nueva-segura").param("confirmacion","nueva-segura"))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/auth/login?contrasenaActualizada=true"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(s.isInvalid());
        assertTrue(s.isInvalid());
    }
}

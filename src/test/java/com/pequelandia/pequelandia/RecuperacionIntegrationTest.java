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

class RecuperacionIntegrationTest extends IntegrationSupport {
    @Autowired RecuperacionService recuperacion;
    @Autowired IUsuarioRepository usuarios;
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;
    @MockitoBean JavaMailSender smtp;
    @MockitoSpyBean ColaCorreoService cola;
    Usuario usuario;

    @BeforeEach void preparar() {
        jdbc.execute("TRUNCATE usuario, correo_pendiente, recuperacion_clave, recuperacion_limite RESTART IDENTITY CASCADE");
        usuario = new Usuario();
        usuario.setNombre_usuario("prueba"); usuario.setEmail("prueba@example.invalid");
        usuario.setContraseña("original"); usuario = usuarios.save(usuario);
        reset(cola, smtp);
    }

    String token() {
        String texto = jdbc.queryForObject("SELECT contenido FROM correo_pendiente WHERE asunto LIKE 'Recuper%' ORDER BY id DESC LIMIT 1", String.class);
        return texto.split("token=")[1].split("\\s")[0];
    }
    int correos() { return jdbc.queryForObject("SELECT count(*) FROM correo_pendiente", Integer.class); }
    MockHttpSession sesion() {
        var s = new MockHttpSession(); s.setAttribute(CsrfInterceptor.KEY, "csrf"); return s;
    }
    void pedir() { recuperacion.solicitar(usuario.getEmail(), "127.0.0.1"); }

    @Test void flujoCompletoCorreoHashYUsoUnico() throws Exception {
        pedir(); String t = token();
        assertEquals(43, t.length());
        assertNotEquals(t, jdbc.queryForObject("SELECT token_hash FROM recuperacion_clave", String.class));
        assertTrue(recuperacion.vigente(t));
        mvc.perform(get("/auth/restablecer").param("token", t))
            .andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Crear contraseña nueva")))
            .andExpect(header().string("Cache-Control", "no-store"));
        assertTrue(recuperacion.restablecer(t, "nueva-segura", "nueva-segura", "127.0.0.1"));
        assertEquals("nueva-segura", usuarios.findById(usuario.getId()).orElseThrow().getContraseña());
        assertTrue(usuarios.login("prueba", "nueva-segura").isPresent());
        assertFalse(usuarios.login("prueba", "original").isPresent());
        assertFalse(recuperacion.vigente(t));
        assertFalse(recuperacion.restablecer(t, "otra-clave", "otra-clave", "127.0.0.1"));
        assertEquals(2, correos());
        verifyNoInteractions(smtp);
        assertFalse(jdbc.queryForObject("SELECT contenido FROM correo_pendiente ORDER BY id DESC LIMIT 1", String.class).contains("nueva-segura"));
    }

    @Test void vencimientoYCorreoModificadoInvalidanEnlace() {
        pedir(); String t = token();
        jdbc.update("UPDATE recuperacion_clave SET vence=clock_timestamp()-INTERVAL '1 second'");
        assertFalse(recuperacion.vigente(t));
        assertFalse(recuperacion.restablecer(t, "nueva-segura", "nueva-segura", "127.0.0.1"));
        jdbc.update("UPDATE recuperacion_clave SET vence=clock_timestamp()+INTERVAL '1 minute'");
        usuario.setEmail("otro@example.invalid"); usuarios.saveAndFlush(usuario);
        assertFalse(recuperacion.restablecer(t, "nueva-segura", "nueva-segura", "127.0.0.1"));
    }

    @Test void contrasenasInvalidasNoConsumenToken() {
        pedir(); String t = token();
        assertFalse(recuperacion.restablecer(t, "corta", "corta", "127.0.0.1"));
        assertFalse(recuperacion.restablecer(t, "nueva-segura", "distinta", "127.0.0.1"));
        assertTrue(recuperacion.vigente(t)); assertEquals(1, correos());
    }

    @Test void respuestaGenericaYProteccionCsrf() throws Exception {
        var s = sesion();
        mvc.perform(post("/auth/recuperar").session(s).param("email", usuario.getEmail())).andExpect(status().isForbidden());
        String real = mvc.perform(post("/auth/recuperar").session(s).param("_csrf", "csrf").param("email", usuario.getEmail()))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String inexistente = mvc.perform(post("/auth/recuperar").session(s).param("_csrf", "csrf").param("email", "nadie@example.invalid"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals(real, inexistente);
        mvc.perform(get("/auth/restablecer").param("token", "invalido"))
            .andExpect(status().isOk()).andExpect(model().attribute("valido", false));
    }

    @Test void esperaLimiteCorreoYReemplazoDeToken() {
        pedir(); String primero = token();
        pedir(); assertEquals(1, correos());
        jdbc.update("UPDATE recuperacion_limite SET inicio=inicio-INTERVAL '61 seconds'");
        pedir(); assertEquals(2, correos()); assertFalse(recuperacion.vigente(primero));
        jdbc.update("UPDATE recuperacion_limite SET inicio=inicio-INTERVAL '61 seconds'");
        pedir(); assertEquals(2, correos()); // tercer intento ya contabilizado durante la espera
    }

    @Test void limiteIpYDeIntentosDeRestablecimiento() {
        for (int i=0; i<10; i++) recuperacion.solicitar("nadie"+i+"@example.invalid", "ip");
        recuperacion.solicitar(usuario.getEmail(), "ip"); assertEquals(0, correos());
        pedir(); String t=token();
        for (int i=0; i<20; i++) assertFalse(recuperacion.restablecer("invalido", "clave-segura", "clave-segura", "ip"));
        assertFalse(recuperacion.restablecer(t, "clave-segura", "clave-segura", "ip"));
        assertTrue(recuperacion.vigente(t));
    }

    @Test void dosConsumosConcurrentesSoloCambianUnaVez() throws Exception {
        pedir(); String t=token();
        try (var pool=Executors.newVirtualThreadPerTaskExecutor()) {
            var inicio=new CountDownLatch(1);
            Callable<Boolean> tarea=()-> { inicio.await(); return recuperacion.restablecer(t,"nueva-segura","nueva-segura",Thread.currentThread().getName()); };
            var a=pool.submit(tarea); var b=pool.submit(tarea); inicio.countDown();
            assertNotEquals(a.get(15,TimeUnit.SECONDS), b.get(15,TimeUnit.SECONDS));
        }
        assertEquals(2, correos());
    }

    @Test void falloEnColaRevierteCambioYConsumo() {
        pedir(); String t=token();
        doThrow(new IllegalStateException("fallo simulado")).when(cola).encolar(anyString(),anyString(),anyString());
        assertThrows(IllegalStateException.class, ()->recuperacion.restablecer(t,"nueva-segura","nueva-segura","ip"));
        assertTrue(recuperacion.vigente(t));
        assertEquals("original", usuarios.findById(usuario.getId()).orElseThrow().getContraseña());
    }

    @Test void postExitosoRedirigeEInvalidaSesion() throws Exception {
        pedir(); var s=sesion();
        mvc.perform(post("/auth/restablecer").session(s).param("_csrf","csrf").param("token",token())
                .param("password","nueva-segura").param("confirmacion","nueva-segura"))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/auth/login?contrasenaActualizada=true"));
        assertTrue(s.isInvalid());
    }
}

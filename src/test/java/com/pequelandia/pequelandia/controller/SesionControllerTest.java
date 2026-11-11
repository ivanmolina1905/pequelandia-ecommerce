package com.pequelandia.pequelandia.controller;

import com.pequelandia.pequelandia.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

class SesionControllerTest {
    @Test void rechazaSesionAusenteSinAutenticarla() throws Exception {
        MockMvcBuilders.standaloneSetup(new LoginController()).build()
                .perform(get("/auth/session"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Cache-Control", "no-store"));
    }

    @Test void confirmaSesionYPublicaPlazoConfigurado() throws Exception {
        var session = new MockHttpSession();
        session.setAttribute("usuario", new Usuario());
        session.setMaxInactiveInterval(86400);
        long inicio = System.currentTimeMillis();
        var result = MockMvcBuilders.standaloneSetup(new LoginController()).build()
                .perform(get("/auth/session").session(session))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andReturn();
        long vence = Long.parseLong(result.getResponse().getHeader("X-Session-Expires"));
        assertTrue(vence >= inicio + 86400000L);
        assertTrue(vence <= System.currentTimeMillis() + 86400000L);
    }
}

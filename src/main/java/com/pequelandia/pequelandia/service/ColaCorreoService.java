package com.pequelandia.pequelandia.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** El alta comparte la transacción del pedido. El worker solo ve filas confirmadas. */
@Service
public class ColaCorreoService {
    private final JdbcTemplate jdbc;
    public ColaCorreoService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional
    public void encolar(String destinatario, String asunto, String contenido) {
        jdbc.update("INSERT INTO correo_pendiente(destinatario, asunto, contenido) VALUES (?, ?, ?)",
                destinatario, asunto, contenido);
    }
}

package com.pequelandia.pequelandia.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EntregaCorreoService {
    private static final Logger log = LoggerFactory.getLogger(EntregaCorreoService.class);
    private final JdbcTemplate jdbc;
    private final JavaMailSender sender;
    private final String emisor;
    public EntregaCorreoService(JdbcTemplate jdbc, JavaMailSender sender,
            @Value("${spring.mail.username}") String emisor) {
        this.jdbc = jdbc; this.sender = sender; this.emisor = emisor;
    }

    @Transactional
    public boolean entregarSiguiente() {
        var filas = jdbc.queryForList("""
            SELECT id, destinatario, asunto, contenido, intentos FROM correo_pendiente
            WHERE enviado_en IS NULL AND proximo_intento <= CURRENT_TIMESTAMP
            ORDER BY proximo_intento, id LIMIT 1 FOR UPDATE SKIP LOCKED
            """);
        if (filas.isEmpty()) return false;
        var fila = filas.get(0);
        long id = ((Number) fila.get("id")).longValue();
        int intentos = ((Number) fila.get("intentos")).intValue();
        try {
            var mensaje = new SimpleMailMessage();
            mensaje.setFrom(emisor);
            mensaje.setTo((String) fila.get("destinatario"));
            mensaje.setSubject((String) fila.get("asunto"));
            mensaje.setText((String) fila.get("contenido"));
            sender.send(mensaje);
        } catch (org.springframework.mail.MailException ex) {
            long espera = Math.min(3600, 30L * (1L << Math.min(intentos, 7)));
            jdbc.update("""
                UPDATE correo_pendiente SET intentos = intentos + 1,
                proximo_intento = CURRENT_TIMESTAMP + (? * INTERVAL '1 second') WHERE id = ?
                """, espera, id);
            log.warn("Correo pendiente {}: intento fallido; se reintentará. Tipo: {}", id, ex.getClass().getSimpleName());
            return true;
        }
        jdbc.update("UPDATE correo_pendiente SET enviado_en = CURRENT_TIMESTAMP, intentos = intentos + 1 WHERE id = ?", id);
        return true;
    }
}

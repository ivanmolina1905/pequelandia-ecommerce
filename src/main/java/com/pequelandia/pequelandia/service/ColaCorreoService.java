package com.pequelandia.pequelandia.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** El alta comparte la transacción del pedido. El worker solo ve filas confirmadas. */
/** [Doc]
 * Inserta correos pendientes en la misma transacción de la operación que los origina.
 */
@Service
public class ColaCorreoService {
    // [Doc] Atributo jdbc (JdbcTemplate): acceso SQL parametrizado mediante JdbcTemplate. Su referencia no se reasigna después de inicializarse.
    private final JdbcTemplate jdbc;
    /** [Doc]
     * Construye ColaCorreoService y conserva sus dependencias o valores iniciales.
     * @param jdbc acceso SQL parametrizado mediante JdbcTemplate.
     */
    public ColaCorreoService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** [Doc]
     * Inserta destinatario, asunto y contenido en correo_pendiente dentro de la transacción activa.
     * @param destinatario identidad o dirección que recibirá el aviso o correo.
     * @param asunto asunto.
     * @param contenido contenido.
     */
    @Transactional
    public void encolar(String destinatario, String asunto, String contenido) {
        // [Doc] Ejecuta una modificación SQL con los parámetros indicados; participa en la transacción si existe.
        jdbc.update("INSERT INTO correo_pendiente(destinatario, asunto, contenido) VALUES (?, ?, ?)",
                destinatario, asunto, contenido);
    }
}

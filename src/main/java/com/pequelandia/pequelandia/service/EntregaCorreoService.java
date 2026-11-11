package com.pequelandia.pequelandia.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** [Doc]
 * Toma un correo pendiente con bloqueo, intenta enviarlo por SMTP y registra envío o próxima fecha de reintento.
 */
@Service
public class EntregaCorreoService {
    // [Doc] Atributo log (Logger): registrador de eventos de la clase. Se inicializa con LoggerFactory.getLogger(EntregaCorreoService.class). Su referencia no se reasigna después de inicializarse.
    private static final Logger log = LoggerFactory.getLogger(EntregaCorreoService.class);
    // [Doc] Atributo jdbc (JdbcTemplate): acceso SQL parametrizado mediante JdbcTemplate. Su referencia no se reasigna después de inicializarse.
    private final JdbcTemplate jdbc;
    // [Doc] Atributo sender (JavaMailSender): cliente JavaMailSender que entrega mensajes SMTP. Su referencia no se reasigna después de inicializarse.
    private final JavaMailSender sender;
    // [Doc] Atributo emisor (String): dirección remitente configurada. Su referencia no se reasigna después de inicializarse.
    private final String emisor;
    /** [Doc]
     * Construye EntregaCorreoService y conserva sus dependencias o valores iniciales.
     * @param jdbc acceso SQL parametrizado mediante JdbcTemplate.
     * @param sender cliente JavaMailSender que entrega mensajes SMTP.
     * @param emisor dirección remitente configurada.
     */
    public EntregaCorreoService(JdbcTemplate jdbc, JavaMailSender sender,
            @Value("${spring.mail.username}") String emisor) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.jdbc = jdbc;
        this.jdbc = jdbc; this.sender = sender; this.emisor = emisor;
    }

    /** [Doc]
     * Selecciona un mensaje listo con FOR UPDATE SKIP LOCKED; intenta SMTP y registra éxito o espera exponencial de
     * hasta una hora.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Transactional
    public boolean entregarSiguiente() {
        // [Doc] Variable local filas (var; tipo inferido por Java): registros devueltos por la consulta SQL. Se inicializa con jdbc.queryForList(""" SELECT id, destinatario, asunto, contenido, intentos FROM correo_pendiente WHERE envi....
        var filas = jdbc.queryForList("""
            SELECT id, destinatario, asunto, contenido, intentos FROM correo_pendiente
            WHERE enviado_en IS NULL AND proximo_intento <= CURRENT_TIMESTAMP
            ORDER BY proximo_intento, id LIMIT 1 FOR UPDATE SKIP LOCKED
            """);
        // [Doc] Evalúa filas.isEmpty(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (filas.isEmpty()) return false;
        // [Doc] Variable local fila (var; tipo inferido por Java): registro SQL seleccionado. Se inicializa con filas.get(0).
        var fila = filas.get(0);
        // [Doc] Variable local id (long): identificador del registro. Se inicializa con ((Number) fila.get("id")).longValue().
        long id = ((Number) fila.get("id")).longValue();
        // [Doc] Variable local intentos (int): cantidad de intentos de entrega ya registrados. Se inicializa con ((Number) fila.get("intentos")).intValue().
        int intentos = ((Number) fila.get("intentos")).intValue();
        // [Doc] Ejecuta un bloque con tratamiento de excepciones; los catch asociados atienden los fallos declarados y finally se ejecuta al salir si existe.
        try {
            // [Doc] Variable local mensaje (var; tipo inferido por Java): mensaje que se prepara para enviar o responder. Se inicializa con new SimpleMailMessage().
            var mensaje = new SimpleMailMessage();
            // [Doc] Actualiza una propiedad o configuración mediante la llamada: mensaje.setFrom(emisor);
            mensaje.setFrom(emisor);
            // [Doc] Actualiza una propiedad o configuración mediante la llamada: mensaje.setTo((String) fila.get("destinatario"));
            mensaje.setTo((String) fila.get("destinatario"));
            // [Doc] Actualiza una propiedad o configuración mediante la llamada: mensaje.setSubject((String) fila.get("asunto"));
            mensaje.setSubject((String) fila.get("asunto"));
            // [Doc] Actualiza una propiedad o configuración mediante la llamada: mensaje.setText((String) fila.get("contenido"));
            mensaje.setText((String) fila.get("contenido"));
            // [Doc] Ejecuta la llamada o modificación sender.send(mensaje); Sus efectos dependen del método invocado y del objeto receptor.
            sender.send(mensaje);
        } catch (org.springframework.mail.MailException ex) {
            // [Doc] Variable local espera (long): segundos hasta el próximo reintento. Se inicializa con Math.min(3600, 30L * (1L << Math.min(intentos, 7))).
            long espera = Math.min(3600, 30L * (1L << Math.min(intentos, 7)));
            // [Doc] Ejecuta una modificación SQL con los parámetros indicados; participa en la transacción si existe.
            jdbc.update("""
                UPDATE correo_pendiente SET intentos = intentos + 1,
                proximo_intento = CURRENT_TIMESTAMP + (? * INTERVAL '1 second') WHERE id = ?
                """, espera, id);
            // [Doc] Ejecuta la llamada o modificación log.warn("Correo pendiente {}: intento fallido; se reintentará. Tipo: {}", id, ex.getClass().getSimpleName()); Sus efectos dependen del método invocado y del objeto receptor.
            log.warn("Correo pendiente {}: intento fallido; se reintentará. Tipo: {}", id, ex.getClass().getSimpleName());
            // [Doc] Termina el método o la función y devuelve true.
            return true;
        }
        // [Doc] Ejecuta una modificación SQL con los parámetros indicados; participa en la transacción si existe.
        jdbc.update("UPDATE correo_pendiente SET enviado_en = CURRENT_TIMESTAMP, intentos = intentos + 1 WHERE id = ?", id);
        // [Doc] Termina el método o la función y devuelve true.
        return true;
    }
}

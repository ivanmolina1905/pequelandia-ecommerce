package com.pequelandia.pequelandia.service;

import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/** [Doc]
 * Consulta avisos y correos con filtros y páginas de veinte registros y adelanta reintentos permitidos.
 */
@Service
@Transactional(readOnly = true)
public class CentroAvisosService {
    // [Doc] Atributo jdbc (JdbcTemplate): acceso SQL parametrizado mediante JdbcTemplate. Su referencia no se reasigna después de inicializarse.
    private final JdbcTemplate jdbc;
    /** [Doc]
     * Construye CentroAvisosService y conserva sus dependencias o valores iniciales.
     * @param jdbc acceso SQL parametrizado mediante JdbcTemplate.
     */
    public CentroAvisosService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** [Doc]
     * Exige que el valor pertenezca al conjunto de opciones permitidas; de lo contrario responde con HTTP 400.
     * @param valor valor recibido o calculado para este paso.
     * @param permitidos valores admitidos por la validación o transición.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public static String filtro(String valor, String... permitidos) {
        // [Doc] Evalúa !Arrays.asList(permitidos).contains(valor). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (!Arrays.asList(permitidos).contains(valor))
            // [Doc] Interrumpe el flujo lanzando new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filtro no válido"). El llamador o el manejador de errores decide la respuesta.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filtro no válido");
        // [Doc] Termina el método o la función y devuelve valor.
        return valor;
    }

    /** [Doc]
     * Construye la consulta por destinatario y filtro de lectura y devuelve una página de hasta veinte avisos.
     * @param destinatario identidad o dirección que recibirá el aviso o correo.
     * @param estado estado usado por la operación.
     * @param pagina índice de página solicitado, comenzando en cero.
     * @return Page&lt;Map&lt;String, Object&gt;&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public Page<Map<String, Object>> notificaciones(String destinatario, String estado, int pagina) {
        // [Doc] Ejecuta la llamada o modificación filtro(estado, "TODAS", "NO_LEIDAS", "LEIDAS"); Sus efectos dependen del método invocado y del objeto receptor.
        filtro(estado, "TODAS", "NO_LEIDAS", "LEIDAS");
        // [Doc] Variable local from (String): fragmento SQL de tablas, relaciones y condiciones. Se inicializa con " FROM notificacion n LEFT JOIN notificacion_lectura l ON l.id_notificacion=n.id_notificacion AND l.destina....
        String from = " FROM notificacion n LEFT JOIN notificacion_lectura l ON l.id_notificacion=n.id_notificacion AND l.destinatario=n.destinatario WHERE n.destinatario=?";
        // [Doc] Evalúa estado.equals("LEIDAS"). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (estado.equals("LEIDAS")) from += " AND l.id IS NOT NULL";
        // [Doc] Evalúa estado.equals("NO_LEIDAS"). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (estado.equals("NO_LEIDAS")) from += " AND l.id IS NULL";
        // [Doc] Termina el método o la función y devuelve paginar("SELECT n.id_notificacion AS id, n.descripcion, n.fecha_notificacion AS fecha, n.id_pedido AS pedido, (l.id IS NOT NULL) AS leida", from, " ORDER BY ....
        return paginar("SELECT n.id_notificacion AS id, n.descripcion, n.fecha_notificacion AS fecha, n.id_pedido AS pedido, (l.id IS NOT NULL) AS leida",
                from, " ORDER BY n.fecha_notificacion DESC, n.id_notificacion DESC", List.of(destinatario), pagina);
    }

    /** [Doc]
     * Filtra la cola por destinatario o asunto y estado de entrega y devuelve una página con sus datos de reintento.
     * @param estado estado usado por la operación.
     * @param busqueda busqueda.
     * @param pagina índice de página solicitado, comenzando en cero.
     * @return Page&lt;Map&lt;String, Object&gt;&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public Page<Map<String, Object>> correos(String estado, String busqueda, int pagina) {
        // [Doc] Ejecuta la llamada o modificación filtro(estado, "TODOS", "PENDIENTE", "REINTENTO", "ENVIADO"); Sus efectos dependen del método invocado y del objeto receptor.
        filtro(estado, "TODOS", "PENDIENTE", "REINTENTO", "ENVIADO");
        // [Doc] Variable local from (String): fragmento SQL de tablas, relaciones y condiciones. Se inicializa con " FROM correo_pendiente WHERE (strpos(lower(destinatario), lower(?))>0 OR strpos(lower(asunto), lower(?))>0)".
        String from = " FROM correo_pendiente WHERE (strpos(lower(destinatario), lower(?))>0 OR strpos(lower(asunto), lower(?))>0)";
        // [Doc] Ejecuta la llamada o modificación from += switch (estado) { case "PENDIENTE" -> " AND enviado_en IS NULL AND intentos=0"; case "REINTENTO" -> " AND enviado_en IS NULL AND intentos>0"; case "ENVIADO" ->... Sus efectos dependen del método invocado y del objeto receptor.
        from += switch (estado) {
            // [Doc] Ejecuta la instrucción Java case: case "PENDIENTE" -> " AND enviado_en IS NULL AND intentos=0";
            case "PENDIENTE" -> " AND enviado_en IS NULL AND intentos=0";
            // [Doc] Ejecuta la instrucción Java case: case "REINTENTO" -> " AND enviado_en IS NULL AND intentos>0";
            case "REINTENTO" -> " AND enviado_en IS NULL AND intentos>0";
            // [Doc] Ejecuta la instrucción Java case: case "ENVIADO" -> " AND enviado_en IS NOT NULL";
            case "ENVIADO" -> " AND enviado_en IS NOT NULL";
            // [Doc] Ejecuta la instrucción Java case: default -> "";
            default -> "";
        };
        // [Doc] Termina el método o la función y devuelve paginar("SELECT id, destinatario, asunto, intentos, proximo_intento, enviado_en, (enviado_en IS NULL AND intentos>0 AND proximo_intento>CURRENT_TIMESTAMP) AS....
        return paginar("SELECT id, destinatario, asunto, intentos, proximo_intento, enviado_en, (enviado_en IS NULL AND intentos>0 AND proximo_intento>CURRENT_TIMESTAMP) AS reintentable, CASE WHEN enviado_en IS NOT NULL THEN 'Enviado' WHEN intentos>0 THEN 'Reintento pendiente' ELSE 'Pendiente' END AS estado",
                from, " ORDER BY id DESC", List.of(busqueda, busqueda), pagina);
    }

    /** [Doc]
     * Cuenta resultados, limita el índice a una página existente y devuelve hasta veinte filas con LIMIT y OFFSET.
     * @param select columnas de la consulta SQL.
     * @param from fragmento SQL de tablas, relaciones y condiciones.
     * @param orden criterio SQL de ordenación.
     * @param parametros valores que se vinculan a los marcadores de la consulta SQL.
     * @param pagina índice de página solicitado, comenzando en cero.
     * @return Page&lt;Map&lt;String, Object&gt;&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private Page<Map<String, Object>> paginar(String select, String from, String orden, List<Object> parametros, int pagina) {
        // [Doc] Variable local total (long): importe acumulado. Se inicializa con jdbc.queryForObject("SELECT count(*)" + from, Long.class, parametros.toArray()).
        long total = jdbc.queryForObject("SELECT count(*)" + from, Long.class, parametros.toArray());
        // [Doc] Variable local ultima (int): índice máximo de página calculado para los resultados. Se inicializa con (int) Math.max(0, (total - 1) / 20).
        int ultima = (int) Math.max(0, (total - 1) / 20);
        // [Doc] Variable local pageable (var; tipo inferido por Java): parámetros de paginación y orden. Se inicializa con PageRequest.of(Math.min(Math.max(0, pagina), ultima), 20).
        var pageable = PageRequest.of(Math.min(Math.max(0, pagina), ultima), 20);
        // [Doc] Variable local args (var; tipo inferido por Java): valores vinculados a los parámetros de consulta. Se inicializa con new ArrayList<>(parametros).
        var args = new ArrayList<>(parametros);
        // [Doc] Ejecuta la llamada o modificación args.add(pageable.getPageSize()); Sus efectos dependen del método invocado y del objeto receptor.
        args.add(pageable.getPageSize()); args.add(pageable.getOffset());
        // [Doc] Termina el método o la función y devuelve new PageImpl<>(jdbc.queryForList(select + from + orden + " LIMIT ? OFFSET ?", args.toArray()), pageable, total).
        return new PageImpl<>(jdbc.queryForList(select + from + orden + " LIMIT ? OFFSET ?", args.toArray()), pageable, total);
    }

    /** [Doc]
     * Adelanta la fecha del correo fallido todavía no enviado; no modifica mensajes enviados ni mensajes que ya
     * están listos.
     * @param id identificador del registro.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Transactional
    public boolean reintentar(long id) {
        // La misma fila se bloquea contra el worker. No recrear ni reenviar mensajes aceptados.
        // Si ya está listo para procesarse, un segundo clic no cambia nada.
        // [Doc] Termina el método o la función y devuelve jdbc.update("UPDATE correo_pendiente SET proximo_intento=clock_timestamp() WHERE id=? AND enviado_en IS NULL AND intentos>0 AND proximo_intento>clock_timesta....
        return jdbc.update("UPDATE correo_pendiente SET proximo_intento=clock_timestamp() WHERE id=? AND enviado_en IS NULL AND intentos>0 AND proximo_intento>clock_timestamp()", id) == 1;
    }
}

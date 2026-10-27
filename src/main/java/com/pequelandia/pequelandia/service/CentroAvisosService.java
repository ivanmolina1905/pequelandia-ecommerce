package com.pequelandia.pequelandia.service;

import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@Transactional(readOnly = true)
public class CentroAvisosService {
    private final JdbcTemplate jdbc;
    public CentroAvisosService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public static String filtro(String valor, String... permitidos) {
        if (!Arrays.asList(permitidos).contains(valor))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filtro no válido");
        return valor;
    }

    public Page<Map<String, Object>> notificaciones(String destinatario, String estado, int pagina) {
        filtro(estado, "TODAS", "NO_LEIDAS", "LEIDAS");
        String from = " FROM notificacion n LEFT JOIN notificacion_lectura l ON l.id_notificacion=n.id_notificacion AND l.destinatario=n.destinatario WHERE n.destinatario=?";
        if (estado.equals("LEIDAS")) from += " AND l.id IS NOT NULL";
        if (estado.equals("NO_LEIDAS")) from += " AND l.id IS NULL";
        return paginar("SELECT n.id_notificacion AS id, n.descripcion, n.fecha_notificacion AS fecha, n.id_pedido AS pedido, (l.id IS NOT NULL) AS leida",
                from, " ORDER BY n.fecha_notificacion DESC, n.id_notificacion DESC", List.of(destinatario), pagina);
    }

    public Page<Map<String, Object>> correos(String estado, String busqueda, int pagina) {
        filtro(estado, "TODOS", "PENDIENTE", "REINTENTO", "ENVIADO");
        String from = " FROM correo_pendiente WHERE (strpos(lower(destinatario), lower(?))>0 OR strpos(lower(asunto), lower(?))>0)";
        from += switch (estado) {
            case "PENDIENTE" -> " AND enviado_en IS NULL AND intentos=0";
            case "REINTENTO" -> " AND enviado_en IS NULL AND intentos>0";
            case "ENVIADO" -> " AND enviado_en IS NOT NULL";
            default -> "";
        };
        return paginar("SELECT id, destinatario, asunto, intentos, proximo_intento, enviado_en, (enviado_en IS NULL AND intentos>0 AND proximo_intento>CURRENT_TIMESTAMP) AS reintentable, CASE WHEN enviado_en IS NOT NULL THEN 'Enviado' WHEN intentos>0 THEN 'Reintento pendiente' ELSE 'Pendiente' END AS estado",
                from, " ORDER BY id DESC", List.of(busqueda, busqueda), pagina);
    }

    private Page<Map<String, Object>> paginar(String select, String from, String orden, List<Object> parametros, int pagina) {
        long total = jdbc.queryForObject("SELECT count(*)" + from, Long.class, parametros.toArray());
        int ultima = (int) Math.max(0, (total - 1) / 20);
        var pageable = PageRequest.of(Math.min(Math.max(0, pagina), ultima), 20);
        var args = new ArrayList<>(parametros);
        args.add(pageable.getPageSize()); args.add(pageable.getOffset());
        return new PageImpl<>(jdbc.queryForList(select + from + orden + " LIMIT ? OFFSET ?", args.toArray()), pageable, total);
    }

    @Transactional
    public boolean reintentar(long id) {
        // La misma fila se bloquea contra el worker. No recrear ni reenviar mensajes aceptados.
        // Si ya está listo para procesarse, un segundo clic no cambia nada.
        return jdbc.update("UPDATE correo_pendiente SET proximo_intento=clock_timestamp() WHERE id=? AND enviado_en IS NULL AND intentos>0 AND proximo_intento>clock_timestamp()", id) == 1;
    }
}

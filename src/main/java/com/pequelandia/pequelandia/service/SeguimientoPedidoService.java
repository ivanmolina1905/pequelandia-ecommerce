package com.pequelandia.pequelandia.service;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** [Doc]
 * Persiste y consulta los cambios de estado; conserva la ausencia de fechas históricas cuando no existen
 * eventos.
 */
@Service
@Transactional
public class SeguimientoPedidoService {
    // [Doc] Atributo jdbc (JdbcTemplate): acceso SQL parametrizado mediante JdbcTemplate. Su referencia no se reasigna después de inicializarse.
    private final JdbcTemplate jdbc;
    /** [Doc]
     * Construye SeguimientoPedidoService y conserva sus dependencias o valores iniciales.
     * @param jdbc acceso SQL parametrizado mediante JdbcTemplate.
     */
    public SeguimientoPedidoService(JdbcTemplate jdbc) { this.jdbc=jdbc; }

    // Se invoca dentro de la transacción que ya bloqueó el pedido.
    /** [Doc]
     * Inserta el estado nuevo con la fecha actual; para pedidos antiguos sin historial agrega primero el estado
     * anterior con fecha nula.
     * @param id identificador del registro.
     * @param anterior estado previo normalizado.
     * @param nuevo estado de destino normalizado.
     */
    public void registrar(long id, String anterior, String nuevo) {
        // [Doc] Evalúa anterior != null && jdbc.queryForObject("SELECT count(*) FROM pedido_historial WHERE id_pedido=?", Long.class,id)==0. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (anterior != null && jdbc.queryForObject("SELECT count(*) FROM pedido_historial WHERE id_pedido=?", Long.class,id)==0) {
            // [Doc] Ejecuta una modificación SQL con los parámetros indicados; participa en la transacción si existe.
            jdbc.update("INSERT INTO pedido_historial(id_pedido,estado,fecha) VALUES (?,?,NULL)",id,anterior);
        }
        // [Doc] Ejecuta una modificación SQL con los parámetros indicados; participa en la transacción si existe.
        jdbc.update("INSERT INTO pedido_historial(id_pedido,estado,fecha) VALUES (?,?,clock_timestamp())",id,nuevo);
    }
    /** [Doc]
     * Devuelve los eventos del pedido ordenados por su identificador de inserción.
     * @param id identificador del registro.
     * @return List&lt;Map&lt;String, Object&gt;&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Transactional(readOnly=true)
    public List<Map<String,Object>> listar(long id) {
        // [Doc] Termina el método o la función y devuelve jdbc.queryForList("SELECT estado,fecha FROM pedido_historial WHERE id_pedido=? ORDER BY id",id).
        return jdbc.queryForList("SELECT estado,fecha FROM pedido_historial WHERE id_pedido=? ORDER BY id",id);
    }
    /** [Doc]
     * Elimina los eventos de seguimiento asociados al pedido.
     * @param id identificador del registro.
     */
    public void eliminar(long id) { jdbc.update("DELETE FROM pedido_historial WHERE id_pedido=?",id); }
}

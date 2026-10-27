package com.pequelandia.pequelandia.service;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SeguimientoPedidoService {
    private final JdbcTemplate jdbc;
    public SeguimientoPedidoService(JdbcTemplate jdbc) { this.jdbc=jdbc; }

    // Se invoca dentro de la transacción que ya bloqueó el pedido.
    public void registrar(long id, String anterior, String nuevo) {
        if (anterior != null && jdbc.queryForObject("SELECT count(*) FROM pedido_historial WHERE id_pedido=?", Long.class,id)==0) {
            jdbc.update("INSERT INTO pedido_historial(id_pedido,estado,fecha) VALUES (?,?,NULL)",id,anterior);
        }
        jdbc.update("INSERT INTO pedido_historial(id_pedido,estado,fecha) VALUES (?,?,clock_timestamp())",id,nuevo);
    }
    @Transactional(readOnly=true)
    public List<Map<String,Object>> listar(long id) {
        return jdbc.queryForList("SELECT estado,fecha FROM pedido_historial WHERE id_pedido=? ORDER BY id",id);
    }
    public void eliminar(long id) { jdbc.update("DELETE FROM pedido_historial WHERE id_pedido=?",id); }
}

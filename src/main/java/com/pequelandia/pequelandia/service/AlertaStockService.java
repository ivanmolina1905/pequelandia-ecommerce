package com.pequelandia.pequelandia.service;

import com.pequelandia.pequelandia.model.Producto;
import com.pequelandia.pequelandia.model.Notificacion;
import com.pequelandia.pequelandia.repository.INotificacionRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AlertaStockService {
    private final JdbcTemplate jdbc;
    private final INotificacionRepository avisos;
    private final CorreoService correos;
    private final int minimo;
    public AlertaStockService(JdbcTemplate jdbc, INotificacionRepository avisos, CorreoService correos,
            @Value("${app.stock.minimo:5}") int minimo) {
        if(minimo<0) throw new IllegalArgumentException("El stock mínimo no puede ser negativo");
        this.jdbc=jdbc; this.avisos=avisos; this.correos=correos; this.minimo=minimo;
    }
    // El llamador mantiene el bloqueo del producto hasta completar la transacción.
    public void evaluar(Producto p) {
        if(p.getStock()>minimo || "inactivo".equalsIgnoreCase(p.getEstado()) || "discontinuado".equalsIgnoreCase(p.getEstado())) {
            jdbc.update("UPDATE producto_alerta_stock SET activa=false WHERE id_producto=?",p.getIdProducto());
            return;
        }
        var nueva=jdbc.queryForList("""
            INSERT INTO producto_alerta_stock(id_producto,activa) VALUES (?,true)
            ON CONFLICT(id_producto) DO UPDATE SET activa=true WHERE NOT producto_alerta_stock.activa
            RETURNING id_producto
            """,p.getIdProducto());
        if(nueva.isEmpty()) return;
        String nombre=p.getNombre_producto()==null ? "Sin nombre" : p.getNombre_producto();
        if(nombre.length()>120) nombre=nombre.substring(0,117)+"...";
        String texto="Stock mínimo: producto #"+p.getIdProducto()+" — "+nombre
                +". Quedan "+p.getStock()+" unidades (umbral: "+minimo+").";
        var aviso=new Notificacion();
        aviso.setDestinatario("ADMIN"); aviso.setDescripcion(texto);
        aviso.setFecha_notificacion(LocalDateTime.now()); avisos.save(aviso);
        correos.enviarAlAdmin("Stock mínimo: producto #"+p.getIdProducto(),texto);
    }
    public void eliminar(long id) { jdbc.update("DELETE FROM producto_alerta_stock WHERE id_producto=?",id); }
}

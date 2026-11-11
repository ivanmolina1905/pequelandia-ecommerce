package com.pequelandia.pequelandia.service;

import com.pequelandia.pequelandia.model.Producto;
import com.pequelandia.pequelandia.model.Notificacion;
import com.pequelandia.pequelandia.repository.INotificacionRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** [Doc]
 * Registra episodios de stock bajo para avisar una vez y rearmar la alerta cuando se repone.
 */
@Service
@Transactional
public class AlertaStockService {
    // [Doc] Atributo jdbc (JdbcTemplate): acceso SQL parametrizado mediante JdbcTemplate. Su referencia no se reasigna después de inicializarse.
    private final JdbcTemplate jdbc;
    // [Doc] Atributo avisos (INotificacionRepository): acceso de persistencia a notificacion. Su referencia no se reasigna después de inicializarse.
    private final INotificacionRepository avisos;
    // [Doc] Atributo correos (CorreoService): colaborador que concentra operaciones de correo. Su referencia no se reasigna después de inicializarse.
    private final CorreoService correos;
    // [Doc] Atributo minimo (int): umbral de unidades que activa el aviso de stock bajo. Su referencia no se reasigna después de inicializarse.
    private final int minimo;
    /** [Doc]
     * Construye AlertaStockService y conserva sus dependencias o valores iniciales.
     * @param jdbc acceso SQL parametrizado mediante JdbcTemplate.
     * @param avisos acceso de persistencia a notificacion.
     * @param correos colaborador que concentra operaciones de correo.
     * @param minimo umbral de unidades que activa el aviso de stock bajo.
     */
    public AlertaStockService(JdbcTemplate jdbc, INotificacionRepository avisos, CorreoService correos,
            @Value("${app.stock.minimo:5}") int minimo) {
        // [Doc] Evalúa minimo<0. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if(minimo<0) throw new IllegalArgumentException("El stock mínimo no puede ser negativo");
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.jdbc=jdbc;
        this.jdbc=jdbc; this.avisos=avisos; this.correos=correos; this.minimo=minimo;
    }
    // El llamador mantiene el bloqueo del producto hasta completar la transacción.
    /** [Doc]
     * Desactiva el episodio si hay stock suficiente o el producto no se comercializa; si cruza el umbral, crea una
     * única alerta y encola correo administrativo.
     * @param p producto consultado para esta operación.
     */
    public void evaluar(Producto p) {
        // [Doc] Evalúa p.getStock()>minimo || "inactivo".equalsIgnoreCase(p.getEstado()) || "discontinuado".equalsIgnoreCase(p.getEstado()). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if(p.getStock()>minimo || "inactivo".equalsIgnoreCase(p.getEstado()) || "discontinuado".equalsIgnoreCase(p.getEstado())) {
            // [Doc] Ejecuta una modificación SQL con los parámetros indicados; participa en la transacción si existe.
            jdbc.update("UPDATE producto_alerta_stock SET activa=false WHERE id_producto=?",p.getIdProducto());
            // [Doc] Termina este método sin devolver un valor.
            return;
        }
        // [Doc] Variable local nueva (var; tipo inferido por Java): resultado de la inserción o actualización que indica si comenzó un nuevo episodio. Se inicializa con jdbc.queryForList(""" INSERT INTO producto_alerta_stock(id_producto,activa) VALUES (?,true) ON CONFLICT(id_....
        var nueva=jdbc.queryForList("""
            INSERT INTO producto_alerta_stock(id_producto,activa) VALUES (?,true)
            ON CONFLICT(id_producto) DO UPDATE SET activa=true WHERE NOT producto_alerta_stock.activa
            RETURNING id_producto
            """,p.getIdProducto());
        // [Doc] Evalúa nueva.isEmpty(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if(nueva.isEmpty()) return;
        // [Doc] Variable local nombre (String): nombre normalizado o visible de la entidad. Se inicializa con p.getNombre_producto()==null ? "Sin nombre" : p.getNombre_producto().
        String nombre=p.getNombre_producto()==null ? "Sin nombre" : p.getNombre_producto();
        // [Doc] Evalúa nombre.length()>120. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if(nombre.length()>120) nombre=nombre.substring(0,117)+"...";
        // [Doc] Variable local texto (String): contenido textual que se construye para presentar o enviar. Se inicializa con "Stock mínimo: producto #"+p.getIdProducto()+" — "+nombre +". Quedan "+p.getStock()+" unidades (umbral: "+m....
        String texto="Stock mínimo: producto #"+p.getIdProducto()+" — "+nombre
                +". Quedan "+p.getStock()+" unidades (umbral: "+minimo+").";
        // [Doc] Variable local aviso (var; tipo inferido por Java): aviso. Se inicializa con new Notificacion().
        var aviso=new Notificacion();
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: aviso.setDestinatario("ADMIN");
        aviso.setDestinatario("ADMIN"); aviso.setDescripcion(texto);
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: aviso.setFecha_notificacion(LocalDateTime.now());
        aviso.setFecha_notificacion(LocalDateTime.now()); avisos.save(aviso);
        // [Doc] Ejecuta la llamada o modificación correos.enviarAlAdmin("Stock mínimo: producto #"+p.getIdProducto(),texto); Sus efectos dependen del método invocado y del objeto receptor.
        correos.enviarAlAdmin("Stock mínimo: producto #"+p.getIdProducto(),texto);
    }
    /** [Doc]
     * Elimina la marca de episodio de stock asociada al producto.
     * @param id identificador del registro.
     */
    public void eliminar(long id) { jdbc.update("DELETE FROM producto_alerta_stock WHERE id_producto=?",id); }
}

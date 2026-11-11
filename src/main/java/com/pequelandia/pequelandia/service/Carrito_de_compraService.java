package com.pequelandia.pequelandia.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pequelandia.pequelandia.model.Carrito_de_compra;
import com.pequelandia.pequelandia.model.Detalle_carrito;
import com.pequelandia.pequelandia.model.Producto;
import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.repository.ICarrito_de_compraRepository;
import com.pequelandia.pequelandia.repository.IDetalle_carritoRepository;
import com.pequelandia.pequelandia.repository.IProductoRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

/** [Doc]
 * Gestiona carritos persistentes, líneas, totales y fusión del carrito invitado, protegiendo los carritos
 * históricos.
 */
@Service
@Transactional
public class Carrito_de_compraService implements ICarrito_de_compraService{

    // [Doc] Atributo carritoRepo (ICarrito_de_compraRepository): acceso de persistencia a carrito de compra.
	@Autowired
	private ICarrito_de_compraRepository carritoRepo;
    // [Doc] Atributo detalleRepo (IDetalle_carritoRepository): acceso de persistencia a detalle carrito.
    @Autowired
	private IDetalle_carritoRepository detalleRepo;
    // [Doc] Atributo productoRepo (IProductoRepository): acceso de persistencia a producto.
    @Autowired 
    private IProductoRepository productoRepo;
    // [Doc] Atributo detalleCarritoService (IDetalle_carritoService): colaborador que concentra operaciones de detalle carrito.
    @Autowired
    private IDetalle_carritoService detalleCarritoService;
    
    // [Doc] Atributo em (EntityManager): contexto JPA utilizado para refrescar entidades con bloqueo.
    @PersistenceContext
    private EntityManager em;

    // ---------------- Carrito ----------------
    /** [Doc]
     * Consulta y devuelve los datos de la operación en el ámbito de Carrito_de_compraService.
     * @return List&lt;Carrito_de_compra&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public List<Carrito_de_compra> listar() {
        // [Doc] Termina el método o la función y devuelve carritoRepo.findAll().
        return carritoRepo.findAll();
    }

    /** [Doc]
     * Obtiene los datos de la operación en el ámbito de Carrito_de_compraService.
     * @param id identificador del registro.
     * @return Carrito_de_compra con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public Carrito_de_compra obtener(Long id) {
        // [Doc] Termina el método o la función y devuelve carritoRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Carrito no encontrado id=" + id)).
        return carritoRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Carrito no encontrado id=" + id));
    }

    /** [Doc]
     * Guarda los datos de la operación en el ámbito de Carrito_de_compraService.
     * @param carrito carrito persistente procesado.
     * @return Carrito_de_compra con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public Carrito_de_compra guardar(Carrito_de_compra carrito) {
    	 // Si viene null → setear en 0
        // [Doc] Evalúa carrito.getTotal() == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (carrito.getTotal() == null) {
            // [Doc] Actualiza una propiedad o configuración mediante la llamada: carrito.setTotal(BigDecimal.ZERO);
            carrito.setTotal(BigDecimal.ZERO);
        }
        // [Doc] Termina el método o la función y devuelve carritoRepo.save(carrito).
        return carritoRepo.save(carrito);
    }

    /** [Doc]
     * Elimina los datos de la operación en el ámbito de Carrito_de_compraService.
     * @param id identificador del registro.
     */
    @Override
    public void eliminar(Long id) {
        // [Doc] Ejecuta la llamada o modificación editable(id); Sus efectos dependen del método invocado y del objeto receptor.
        editable(id);
        // [Doc] Evalúa !carritoRepo.existsById(id). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (!carritoRepo.existsById(id)) {
            // [Doc] Interrumpe el flujo lanzando new NoSuchElementException("Carrito no encontrado id=" + id). El llamador o el manejador de errores decide la respuesta.
            throw new NoSuchElementException("Carrito no encontrado id=" + id);
        }
        // [Doc] Ejecuta la llamada o modificación carritoRepo.deleteById(id); Sus efectos dependen del método invocado y del objeto receptor.
        carritoRepo.deleteById(id);
    }

    /** [Doc]
     * Exige un carrito editable, sustituye un total nulo por cero y guarda el nuevo importe.
     * @param carritoId identificador del carrito persistente.
     * @param nuevoTotal nuevo total.
     * @return Carrito_de_compra con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public Carrito_de_compra actualizarTotal(Long carritoId, BigDecimal nuevoTotal) {
        // [Doc] Variable local c (Carrito_de_compra): carrito persistente procesado. Se inicializa con editable(carritoId).
        Carrito_de_compra c = editable(carritoId);
        // [Doc] Evalúa nuevoTotal == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (nuevoTotal == null)
            // [Doc] Ejecuta la llamada o modificación nuevoTotal = BigDecimal.ZERO; Sus efectos dependen del método invocado y del objeto receptor.
            nuevoTotal = BigDecimal.ZERO;
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: c.setTotal(nuevoTotal);
        c.setTotal(nuevoTotal);
        // [Doc] Termina el método o la función y devuelve carritoRepo.save(c).
        return carritoRepo.save(c);
    }
    
    /** [Doc]
     * Recalcula total1 en el ámbito de Carrito_de_compraService.
     * @param carritoId identificador del carrito persistente.
     * @return BigDecimal con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public BigDecimal recalcularTotal1(Long carritoId){
        // [Doc] Variable local total (Double): importe acumulado. Se inicializa con detalleRepo.totalCarrito(carritoId).
        Double total = detalleRepo.totalCarrito(carritoId); // SUM(cantidad * precio)
        // [Doc] Variable local bd (BigDecimal): bd. Se inicializa con BigDecimal.valueOf(total != null ? total : 0.0).
        BigDecimal bd = BigDecimal.valueOf(total != null ? total : 0.0);
        // [Doc] Variable local c (Carrito_de_compra): carrito persistente procesado. Se inicializa con editable(carritoId).
        Carrito_de_compra c = editable(carritoId);
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: c.setTotal(bd);
        c.setTotal(bd);
        // [Doc] Solicita persistir la entidad; saveAndFlush también fuerza la sincronización: carritoRepo.save(c);
        carritoRepo.save(c);
        // [Doc] Termina el método o la función y devuelve bd.
        return bd;
    }

    // ---------------- Detalles (tocar total) ----------------
    /** [Doc]
     * Agrega detalle en el ámbito de Carrito_de_compraService.
     * @param carritoId identificador del carrito persistente.
     * @param productoId identificador del producto.
     * @param cantidad unidades de una línea de carrito.
     * @return Detalle_carrito con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public Detalle_carrito agregarDetalle(Long carritoId, Long productoId, Integer cantidad) {

        // [Doc] Evalúa cantidad == null || cantidad <= 0. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (cantidad == null || cantidad <= 0)
            // [Doc] Interrumpe el flujo lanzando new IllegalArgumentException("La cantidad debe ser mayor a 0."). El llamador o el manejador de errores decide la respuesta.
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0.");

        // [Doc] Variable local carrito (Carrito_de_compra): carrito persistente procesado. Se inicializa con editable(carritoId).
        Carrito_de_compra carrito = editable(carritoId);

        // 1) Buscar producto (para obtener PRECIO ACTUAL)
        // [Doc] Variable local producto (Producto): producto procesado en este punto. Se inicializa con productoRepo.findById(productoId) .orElseThrow(() -> new NoSuchElementException("Producto no encontrado id=....
        Producto producto = productoRepo.findById(productoId)
                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado id=" + productoId));

        // 2) Ver si el detalle ya existe
        // [Doc] Variable local det (Detalle_carrito): línea persistente del carrito. Se inicializa con detalleRepo.findOneByCarritoAndProductoNative(carritoId, productoId) .orElseGet(() -> { Detalle_carrito nue....
        Detalle_carrito det = detalleRepo.findOneByCarritoAndProductoNative(carritoId, productoId)
                .orElseGet(() -> {
                    // [Doc] Variable local nuevo (Detalle_carrito): estado de destino normalizado. Se inicializa con new Detalle_carrito().
                    Detalle_carrito nuevo = new Detalle_carrito();
                    // [Doc] Actualiza una propiedad o configuración mediante la llamada: nuevo.setCarrito(carrito);
                    nuevo.setCarrito(carrito);
                    // [Doc] Actualiza una propiedad o configuración mediante la llamada: nuevo.setProducto(producto);
                    nuevo.setProducto(producto); // referencia real
                    // [Doc] Actualiza una propiedad o configuración mediante la llamada: nuevo.setCantidad(0);
                    nuevo.setCantidad(0);
                    // [Doc] Termina el método o la función y devuelve nuevo.
                    return nuevo;
                });

        // 3) Actualizar cantidad
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: det.setCantidad(Math.addExact(det.getCantidad(), cantidad));
        det.setCantidad(Math.addExact(det.getCantidad(), cantidad));

        // 4) Guardar precio actual del producto 💥 CLAVE para estadísticas
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: det.setPrecio(producto.getPrecio());
        det.setPrecio(producto.getPrecio());

        // [Doc] Termina el método o la función y devuelve detalleRepo.save(det).
        return detalleRepo.save(det);
    }

    /** [Doc]
     * Exige un carrito editable, busca la línea del producto y la elimina o informa que no existe.
     * @param carritoId identificador del carrito persistente.
     * @param productoId identificador del producto.
     */
    @Override
    public void quitarDetalle(Long carritoId, Long productoId) {
        // [Doc] Ejecuta la llamada o modificación editable(carritoId); Sus efectos dependen del método invocado y del objeto receptor.
        editable(carritoId);

        // [Doc] Variable local det (Detalle_carrito): línea persistente del carrito. Se inicializa con detalleRepo.findOneByCarritoAndProductoNative(carritoId, productoId) .orElseThrow(() -> new NoSuchElementEx....
        Detalle_carrito det = detalleRepo.findOneByCarritoAndProductoNative(carritoId, productoId)
                .orElseThrow(() ->
                        new NoSuchElementException("El producto " + productoId +
                                " no está en el carrito " + carritoId));

        // [Doc] Ejecuta la llamada o modificación detalleRepo.delete(det); Sus efectos dependen del método invocado y del objeto receptor.
        detalleRepo.delete(det);
    }

    /** [Doc]
     * Devuelve or create carrito, según el cuerpo o la implementación del contrato.
     * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
     * @return Carrito_de_compra con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
 // 🔑 MÉTODO CLAVE
    public Carrito_de_compra getOrCreateCarrito(HttpSession session) {

        // [Doc] Variable local u (Usuario): cuenta de usuario utilizada por la operación. Se inicializa con (Usuario) session.getAttribute("usuario").
        Usuario u = (Usuario) session.getAttribute("usuario");

        // ======================
        // USUARIO LOGUEADO
        // ======================
        // [Doc] Evalúa u != null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (u != null) {
            // buscar carrito activo del usuario
            // [Doc] Termina el método o la función y devuelve carritoRepo.findFirstByUsuarioAndPedidoIsNullOrderByIdCarritoDesc(u) .orElseGet(() -> { Carrito_de_compra c = new Carrito_de_compra(); c.setUsuario(u); c.set....
            return carritoRepo.findFirstByUsuarioAndPedidoIsNullOrderByIdCarritoDesc(u)
                .orElseGet(() -> {
                    // [Doc] Variable local c (Carrito_de_compra): carrito persistente procesado. Se inicializa con new Carrito_de_compra().
                    Carrito_de_compra c = new Carrito_de_compra();
                    // [Doc] Actualiza una propiedad o configuración mediante la llamada: c.setUsuario(u);
                    c.setUsuario(u);
                    // [Doc] Actualiza una propiedad o configuración mediante la llamada: c.setTotal(BigDecimal.ZERO);
                    c.setTotal(BigDecimal.ZERO);
                    // [Doc] Termina el método o la función y devuelve carritoRepo.save(c).
                    return carritoRepo.save(c);
                });
        }

        // ======================
        // USUARIO INVITADO
        // ======================
        // [Doc] Variable local carritoId (Long): identificador del carrito persistente. Se inicializa con (Long) session.getAttribute("CARRITO_ID").
        Long carritoId = (Long) session.getAttribute("CARRITO_ID");

        // [Doc] Evalúa carritoId != null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (carritoId != null) {
            // [Doc] Termina el método o la función y devuelve carritoRepo.findById(carritoId) .filter(c -> c.getPedido() == null && c.getUsuario() == null) .orElseGet(() -> crearCarritoInvitado(session)).
            return carritoRepo.findById(carritoId)
                .filter(c -> c.getPedido() == null && c.getUsuario() == null)
                .orElseGet(() -> crearCarritoInvitado(session));
        }

        // [Doc] Termina el método o la función y devuelve crearCarritoInvitado(session).
        return crearCarritoInvitado(session);
    }

  
    /** [Doc]
     * Guarda un carrito sin usuario con total cero y conserva su ID en el atributo CARRITO_ID de sesión.
     * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
     * @return Carrito_de_compra con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private Carrito_de_compra crearCarritoInvitado(HttpSession session) {
        // [Doc] Variable local c (Carrito_de_compra): carrito persistente procesado. Se inicializa con new Carrito_de_compra().
        Carrito_de_compra c = new Carrito_de_compra();
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: c.setTotal(BigDecimal.ZERO);
        c.setTotal(BigDecimal.ZERO);
        // [Doc] Solicita persistir la entidad; saveAndFlush también fuerza la sincronización: c = carritoRepo.save(c);
        c = carritoRepo.save(c);
        // [Doc] Almacena un atributo en la sesión o petición: session.setAttribute("CARRITO_ID", c.getId_carrito());
        session.setAttribute("CARRITO_ID", c.getId_carrito());
        // [Doc] Termina el método o la función y devuelve c.
        return c;
    }
    

 /** [Doc]
  * Exige origen invitado y destino de usuario editables, acumula sus líneas, elimina el origen y recalcula el
  * total del destino.
  * @param carritoInvitadoId carrito invitado id.
  * @param carritoUsuarioId carrito usuario id.
  */
 @Override public void mergeCarritos(Long carritoInvitadoId, Long
 carritoUsuarioId) {
 
 // [Doc] Evalúa carritoInvitadoId.equals(carritoUsuarioId). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
 if (carritoInvitadoId.equals(carritoUsuarioId)) return;
 // [Doc] Variable local origen (Carrito_de_compra): carrito persistente procesado. Se inicializa con editable(carritoInvitadoId).
 Carrito_de_compra origen = editable(carritoInvitadoId);
 // [Doc] Variable local destino (Carrito_de_compra): carrito persistente procesado. Se inicializa con editable(carritoUsuarioId).
 Carrito_de_compra destino = editable(carritoUsuarioId);
 // [Doc] Evalúa origen.getUsuario() != null || destino.getUsuario() == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
 if (origen.getUsuario() != null || destino.getUsuario() == null)
     // [Doc] Interrumpe el flujo lanzando new IllegalArgumentException("Solo se puede incorporar un carrito invitado a un usuario"). El llamador o el manejador de errores decide la respuesta.
     throw new IllegalArgumentException("Solo se puede incorporar un carrito invitado a un usuario");

 // [Doc] Variable local items (List<Detalle_carrito>): elementos del carrito o de la colección procesada. Se inicializa con detalleRepo.findAllByCarritoNative(carritoInvitadoId).
 List<Detalle_carrito> items =
 detalleRepo.findAllByCarritoNative(carritoInvitadoId);
 
 // [Doc] Recorre los elementos o repite según Detalle_carrito d : items. Cada vuelta ejecuta el cuerpo del bucle.
 for (Detalle_carrito d : items) { detalleCarritoService.upsert(
	  carritoUsuarioId, d.getProducto().getIdProducto(), d.getCantidad() ); }
	  
 // [Doc] Ejecuta la llamada o modificación detalleRepo.deleteAll(items); Sus efectos dependen del método invocado y del objeto receptor.
 detalleRepo.deleteAll(items);
  // [Doc] Ejecuta la llamada o modificación detalleRepo.flush(); Sus efectos dependen del método invocado y del objeto receptor.
  detalleRepo.flush();
  // [Doc] Ejecuta la llamada o modificación carritoRepo.deleteById(carritoInvitadoId); Sus efectos dependen del método invocado y del objeto receptor.
  carritoRepo.deleteById(carritoInvitadoId);
	  
      // [Doc] Ejecuta la llamada o modificación recalcularTotal1(carritoUsuarioId); Sus efectos dependen del método invocado y del objeto receptor.
	  recalcularTotal1(carritoUsuarioId); }
	 
 /** [Doc]
  * Cambia cantidad en el ámbito de Carrito_de_compraService.
  * @param carritoId identificador del carrito persistente.
  * @param productoId identificador del producto.
  * @param cantidadNueva cantidad nueva.
  */
 @Override
 public void cambiarCantidad(Long carritoId, Long productoId, int cantidadNueva){
        // [Doc] Ejecuta la llamada o modificación editable(carritoId); Sus efectos dependen del método invocado y del objeto receptor.
        editable(carritoId);
     // [Doc] Variable local d (Detalle_carrito): línea del carrito procesada. Se inicializa con detalleRepo .findByCarrito_IdCarritoAndProducto_IdProducto(carritoId, productoId) .orElseThrow(() -> new Ru....
     Detalle_carrito d = detalleRepo
             .findByCarrito_IdCarritoAndProducto_IdProducto(carritoId, productoId)
             .orElseThrow(() -> new RuntimeException("No existe línea"));

     // [Doc] Evalúa cantidadNueva <= 0. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
     if(cantidadNueva <= 0){
         // [Doc] Ejecuta la llamada o modificación detalleRepo.delete(d); Sus efectos dependen del método invocado y del objeto receptor.
         detalleRepo.delete(d);
     }else{
         // [Doc] Actualiza una propiedad o configuración mediante la llamada: d.setCantidad(cantidadNueva);
         d.setCantidad(cantidadNueva);
         // [Doc] Solicita persistir la entidad; saveAndFlush también fuerza la sincronización: detalleRepo.save(d);
         detalleRepo.save(d);
     }

     // [Doc] Ejecuta la llamada o modificación recalcularTotal1(carritoId); Sus efectos dependen del método invocado y del objeto receptor.
     recalcularTotal1(carritoId);
 }

 
 /** [Doc]
  * Elimina linea en el ámbito de Carrito_de_compraService.
  * @param carritoId identificador del carrito persistente.
  * @param productoId identificador del producto.
  */
 public void eliminarLinea(Long carritoId, Long productoId){
        // [Doc] Ejecuta la llamada o modificación editable(carritoId); Sus efectos dependen del método invocado y del objeto receptor.
        editable(carritoId);
     // [Doc] Ejecuta la llamada o modificación detalleRepo.findByCarrito_IdCarritoAndProducto_IdProducto(carritoId, productoId) .ifPresent(detalleRepo::delete); Sus efectos dependen del método invocado y del objeto receptor.
     detalleRepo.findByCarrito_IdCarritoAndProducto_IdProducto(carritoId, productoId)
             .ifPresent(detalleRepo::delete);

     // [Doc] Ejecuta la llamada o modificación recalcularTotal1(carritoId); Sus efectos dependen del método invocado y del objeto receptor.
     recalcularTotal1(carritoId);
 }

 /** [Doc]
  * Consulta la suma de unidades de las líneas del carrito persistente.
  * @param carritoId identificador del carrito persistente.
  * @return int con el resultado de la operación; véase el cuerpo para las ramas de error.
  */
 @Override
 public int items(Long carritoId){
     // [Doc] Termina el método o la función y devuelve detalleRepo.itemsCarrito(carritoId).intValue().
     return detalleRepo.itemsCarrito(carritoId).intValue();
 }

 /** [Doc]
  * Consulta el importe del carrito y lo convierte a BigDecimal con dos decimales y redondeo HALF_UP; el parámetro
  * fallback no se utiliza.
  * @param fallback fallback.
  * @param carritoId identificador del carrito persistente.
  * @return BigDecimal con el resultado de la operación; véase el cuerpo para las ramas de error.
  */
 @Override
 public BigDecimal total(BigDecimal fallback, Long carritoId){
     // totalCarrito devuelve Double; lo convertimos a BigDecimal
     // [Doc] Variable local t (Double): t. Se inicializa con detalleRepo.totalCarrito(carritoId).
     Double t = detalleRepo.totalCarrito(carritoId);
     // [Doc] Variable local total (BigDecimal): importe acumulado. Se inicializa con BigDecimal.valueOf(t != null ? t : 0.0).setScale(2, RoundingMode.HALF_UP).
     BigDecimal total = BigDecimal.valueOf(t != null ? t : 0.0).setScale(2, RoundingMode.HALF_UP);
     // [Doc] Termina el método o la función y devuelve total.
     return total;
 }
 /** [Doc]
  * Recalcula total en el ámbito de Carrito_de_compraService.
  * @param carritoId identificador del carrito persistente.
  * @return BigDecimal con el resultado de la operación; véase el cuerpo para las ramas de error.
  */
 public BigDecimal recalcularTotal(Long carritoId){
     // [Doc] Variable local total (BigDecimal): importe acumulado. Se inicializa con total(BigDecimal.ZERO, carritoId).
     BigDecimal total = total(BigDecimal.ZERO, carritoId);
     // [Doc] Variable local c (Carrito_de_compra): carrito persistente procesado. Se inicializa con editable(carritoId).
     Carrito_de_compra c = editable(carritoId);
     // [Doc] Actualiza una propiedad o configuración mediante la llamada: c.setTotal(total);
     c.setTotal(total);
     // [Doc] Solicita persistir la entidad; saveAndFlush también fuerza la sincronización: carritoRepo.save(c);
     carritoRepo.save(c);
     // [Doc] Termina el método o la función y devuelve total.
     return total;
 }


	
	

    /** [Doc]
     * Actualiza able en el ámbito de Carrito_de_compraService.
     * @param id identificador del registro.
     * @return Carrito_de_compra con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private Carrito_de_compra editable(Long id) {
        // [Doc] Variable local carrito (Carrito_de_compra): carrito persistente procesado. Se inicializa con carritoRepo.bloquear(id).orElseThrow(() -> new NoSuchElementException("Carrito no encontrado")).
        Carrito_de_compra carrito = carritoRepo.bloquear(id).orElseThrow(() -> new NoSuchElementException("Carrito no encontrado"));
        // [Doc] Recarga la entidad desde la base usando el modo de bloqueo declarado: em.refresh(carrito, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        em.refresh(carrito, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        // [Doc] Evalúa carrito.getPedido() != null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (carrito.getPedido() != null) throw new IllegalStateException("No se puede modificar el carrito de un pedido confirmado");
        // [Doc] Termina el método o la función y devuelve carrito.
        return carrito;
    }
}

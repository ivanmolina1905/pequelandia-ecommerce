package com.pequelandia.pequelandia.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.pequelandia.pequelandia.model.*;
import com.pequelandia.pequelandia.repository.*;

/** [Doc]
 * Coordina compra, bloqueo de stock, cálculo del total, transiciones, reposición, seguimiento y avisos dentro de
 * transacciones.
 */
@Service
@Transactional
public class PedidoService implements IPedidoService {
    // [Doc] Atributo em (jakarta.persistence.EntityManager): contexto JPA utilizado para refrescar entidades con bloqueo.
    @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager em;
    // [Doc] Atributo pedidoRepo (IPedidoRepository): acceso de persistencia a pedido.
    @Autowired private IPedidoRepository pedidoRepo;
    // [Doc] Atributo carritoRepo (ICarrito_de_compraRepository): acceso de persistencia a carrito de compra.
    @Autowired private ICarrito_de_compraRepository carritoRepo;
    // [Doc] Atributo detalleRepo (IDetalle_carritoRepository): acceso de persistencia a detalle carrito.
    @Autowired private IDetalle_carritoRepository detalleRepo;
    // [Doc] Atributo productoRepo (IProductoRepository): acceso de persistencia a producto.
    @Autowired private IProductoRepository productoRepo;
    // [Doc] Atributo usuarioRepo (IUsuarioRepository): acceso de persistencia a usuario.
    @Autowired private IUsuarioRepository usuarioRepo;
    // [Doc] Atributo notificaciones (INotificacionService): colaborador que concentra operaciones de notificacion.
    @Autowired private INotificacionService notificaciones;

    // [Doc] Atributo seguimiento (SeguimientoPedidoService): colaborador que concentra operaciones de seguimiento pedido.
    @Autowired private SeguimientoPedidoService seguimiento;
    // [Doc] Atributo alertaStock (AlertaStockService): colaborador que concentra operaciones de alerta stock.
    @Autowired private AlertaStockService alertaStock;

    // [Doc] Atributo ESTADOS (Set<String>): conjunto de estados de pedido reconocidos. Se inicializa con Set.of("PENDIENTE", "PREPARANDO", "ENVIADO", "ENTREGADO", "CANCELACION_SOLICITADA", "CANCELADO"). Su referencia no se reasigna después de inicializarse.
    private static final Set<String> ESTADOS = Set.of("PENDIENTE", "PREPARANDO", "ENVIADO",
            "ENTREGADO", "CANCELACION_SOLICITADA", "CANCELADO");
    /** [Doc]
     * Normaliza espacios y mayúsculas del estado y convierte PREPARACIÓN o PREPARACION en PREPARANDO.
     * @param valor valor recibido o calculado para este paso.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private static String estado(String valor) {
        // [Doc] Variable local normal (String): texto normalizado para comparar valores. Se inicializa con valor == null ? "" : valor.trim().toUpperCase(Locale.ROOT).
        String normal = valor == null ? "" : valor.trim().toUpperCase(Locale.ROOT);
        // [Doc] Evalúa normal.equals("PREPARACIÓN") || normal.equals("PREPARACION"). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (normal.equals("PREPARACIÓN") || normal.equals("PREPARACION")) return "PREPARANDO";
        // [Doc] Termina el método o la función y devuelve normal.
        return normal;
    }

    /** [Doc]
     * Devuelve pedido, según el cuerpo o la implementación del contrato.
     * @return List&lt;Pedido&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override public List<Pedido> getPedido() { return pedidoRepo.findAll(); }
    /** [Doc]
     * Consulta persistencia según el criterio expresado por findPedido; la consulta declarada o la convención de
     * Spring Data determina el resultado.
     * @param id identificador del registro.
     * @return Pedido con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override public Pedido findPedido(Long id) {
        // [Doc] Termina el método o la función y devuelve pedidoRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Pedido no encontrado")).
        return pedidoRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Pedido no encontrado"));
    }
    /** [Doc]
     * Guarda pedido en el ámbito de PedidoService.
     * @param pedido pedido procesado en este punto.
     */
    @Override public void savePedido(Pedido pedido) {
        // [Doc] Evalúa pedido.getId_pedido() == 0. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (pedido.getId_pedido() == 0) {
            // [Doc] Evalúa pedido.getUsuario() == null || pedido.getCarrito() == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
            if (pedido.getUsuario() == null || pedido.getCarrito() == null)
                // [Doc] Interrumpe el flujo lanzando new IllegalArgumentException("El pedido requiere usuario y carrito"). El llamador o el manejador de errores decide la respuesta.
                throw new IllegalArgumentException("El pedido requiere usuario y carrito");
            // [Doc] Ejecuta la llamada o modificación finalizarCompra(pedido.getUsuario().getId(), pedido.getCarrito().getIdCarrito()); Sus efectos dependen del método invocado y del objeto receptor.
            finalizarCompra(pedido.getUsuario().getId(), pedido.getCarrito().getIdCarrito());
        } else {
            // [Doc] Ejecuta la llamada o modificación cambiarEstadoConReposicion(pedido.getId_pedido(), pedido.getEstado_pedido()); Sus efectos dependen del método invocado y del objeto receptor.
            cambiarEstadoConReposicion(pedido.getId_pedido(), pedido.getEstado_pedido());
        }
    }
    /** [Doc]
     * Actualiza pedido en el ámbito de PedidoService.
     * @param id identificador del registro.
     * @param cambios pedido procesado en este punto.
     */
    @Override public void editPedido(Long id, Pedido cambios) {
        // [Doc] Variable local actual (Pedido): pedido procesado en este punto. Se inicializa con bloquearPedido(id).
        Pedido actual = bloquearPedido(id);
        // [Doc] Evalúa cambios.getUsuario() != null && !Objects.equals(actual.getUsuario().getId(), cambios.getUsuario().getId()). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (cambios.getUsuario() != null && !Objects.equals(actual.getUsuario().getId(), cambios.getUsuario().getId()))
            // [Doc] Interrumpe el flujo lanzando new IllegalArgumentException("No se puede transferir un pedido a otro usuario"). El llamador o el manejador de errores decide la respuesta.
            throw new IllegalArgumentException("No se puede transferir un pedido a otro usuario");
        // [Doc] Evalúa cambios.getCarrito() != null && !Objects.equals(actual.getCarrito().getIdCarrito(), cambios.getCarrito().getIdCarrito()). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (cambios.getCarrito() != null && !Objects.equals(actual.getCarrito().getIdCarrito(), cambios.getCarrito().getIdCarrito()))
            // [Doc] Interrumpe el flujo lanzando new IllegalArgumentException("No se puede reemplazar el carrito de un pedido"). El llamador o el manejador de errores decide la respuesta.
            throw new IllegalArgumentException("No se puede reemplazar el carrito de un pedido");
        // [Doc] Evalúa cambios.getEstado_pedido() != null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (cambios.getEstado_pedido() != null) transicionar(actual, cambios.getEstado_pedido());
        // [Doc] Evalúa cambios.getFecha_pedido() != null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (cambios.getFecha_pedido() != null) actual.setFecha_pedido(cambios.getFecha_pedido());
        // [Doc] Solicita persistir la entidad; saveAndFlush también fuerza la sincronización: pedidoRepo.save(actual);
        pedidoRepo.save(actual);
    }
    /** [Doc]
     * Elimina pedido en el ámbito de PedidoService.
     * @param id identificador del registro.
     */
    @Override public void deletePedido(Long id) {
        // [Doc] Variable local pedido (Pedido): pedido procesado en este punto. Se inicializa con bloquearPedido(id).
        Pedido pedido = bloquearPedido(id);
        // [Doc] Evalúa !Set.of("CANCELADO", "ENTREGADO").contains(estado(pedido.getEstado_pedido())). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (!Set.of("CANCELADO", "ENTREGADO").contains(estado(pedido.getEstado_pedido())))
            // [Doc] Ejecuta la llamada o modificación transicionar(pedido, "CANCELADO"); Sus efectos dependen del método invocado y del objeto receptor.
            transicionar(pedido, "CANCELADO");
        // [Doc] Ejecuta la llamada o modificación seguimiento.eliminar(id); Sus efectos dependen del método invocado y del objeto receptor.
        seguimiento.eliminar(id);
        // [Doc] Ejecuta la llamada o modificación pedidoRepo.delete(pedido); Sus efectos dependen del método invocado y del objeto receptor.
        pedidoRepo.delete(pedido);
    }

    /** [Doc]
     * Crea un carrito persistente desde la copia de sesión, agrega sus líneas en orden de ID y delega la
     * confirmación transaccional.
     * @param usuarioId identificador del usuario.
     * @param items elementos del carrito o de la colección procesada.
     * @return Pedido con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override public Pedido comprar(Long usuarioId, Map<Long, Integer> items) {
        // [Doc] Evalúa items == null || items.isEmpty(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("Carrito vacío");
        // [Doc] Variable local usuario (Usuario): cuenta de usuario utilizada por la operación. Se inicializa con usuarioRepo.findById(usuarioId).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado")).
        Usuario usuario = usuarioRepo.findById(usuarioId).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        // [Doc] Variable local carrito (Carrito_de_compra): carrito persistente procesado. Se inicializa con new Carrito_de_compra().
        Carrito_de_compra carrito = new Carrito_de_compra();
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: carrito.setUsuario(usuario);
        carrito.setUsuario(usuario);
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: carrito.setTotal(BigDecimal.ZERO);
        carrito.setTotal(BigDecimal.ZERO);
        // [Doc] Solicita persistir la entidad; saveAndFlush también fuerza la sincronización: carritoRepo.save(carrito);
        carritoRepo.save(carrito);
        // Orden estable para evitar interbloqueos entre compras con varios productos.
        // [Doc] Recorre los elementos o repite según var entry : new TreeMap<>(items).entrySet(). Cada vuelta ejecuta el cuerpo del bucle.
        for (var entry : new TreeMap<>(items).entrySet()) {
            // [Doc] Variable local cantidad (int): unidades de una línea de carrito. Se inicializa con cantidadValida(entry.getValue()).
            int cantidad = cantidadValida(entry.getValue());
            // [Doc] Variable local producto (Producto): producto procesado en este punto. Se inicializa con bloquearProducto(entry.getKey()).
            Producto producto = bloquearProducto(entry.getKey());
            // [Doc] Variable local detalle (Detalle_carrito): línea persistente del carrito. Se inicializa con new Detalle_carrito().
            Detalle_carrito detalle = new Detalle_carrito();
            // [Doc] Actualiza una propiedad o configuración mediante la llamada: detalle.setCarrito(carrito);
            detalle.setCarrito(carrito);
            // [Doc] Actualiza una propiedad o configuración mediante la llamada: detalle.setProducto(producto);
            detalle.setProducto(producto);
            // [Doc] Actualiza una propiedad o configuración mediante la llamada: detalle.setCantidad(cantidad);
            detalle.setCantidad(cantidad);
            // [Doc] Actualiza una propiedad o configuración mediante la llamada: detalle.setPrecio(producto.getPrecio());
            detalle.setPrecio(producto.getPrecio());
            // [Doc] Solicita persistir la entidad; saveAndFlush también fuerza la sincronización: detalleRepo.save(detalle);
            detalleRepo.save(detalle);
        }
        // [Doc] Termina el método o la función y devuelve finalizarCompra(usuarioId, carrito.getIdCarrito()).
        return finalizarCompra(usuarioId, carrito.getIdCarrito());
    }

    /** [Doc]
     * Bloquea y refresca el carrito, exige titularidad, reutiliza un pedido ya creado o valida y descuenta stock;
     * guarda total, seguimiento y avisos.
     * @param usuarioId identificador del usuario.
     * @param carritoId identificador del carrito persistente.
     * @return Pedido con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override public Pedido finalizarCompra(Long usuarioId, Long carritoId) {
        // [Doc] Evalúa usuarioId == null || carritoId == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (usuarioId == null || carritoId == null) throw new IllegalArgumentException("Usuario y carrito requeridos");
        // [Doc] Variable local carrito (Carrito_de_compra): carrito persistente procesado. Se inicializa con carritoRepo.bloquear(carritoId) .orElseThrow(() -> new NoSuchElementException("Carrito no encontrado")).
        Carrito_de_compra carrito = carritoRepo.bloquear(carritoId)
                .orElseThrow(() -> new NoSuchElementException("Carrito no encontrado"));
        // [Doc] Recarga la entidad desde la base usando el modo de bloqueo declarado: em.refresh(carrito, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        em.refresh(carrito, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        // [Doc] Evalúa carrito.getUsuario() == null || !Objects.equals(carrito.getUsuario().getId(), usuarioId). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (carrito.getUsuario() == null || !Objects.equals(carrito.getUsuario().getId(), usuarioId))
            // [Doc] Interrumpe el flujo lanzando new ResponseStatusException(HttpStatus.FORBIDDEN, "El carrito no pertenece al usuario"). El llamador o el manejador de errores decide la respuesta.
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El carrito no pertenece al usuario");
        // [Doc] Variable local existente (var; tipo inferido por Java): existente. Se inicializa con pedidoRepo.buscarPorCarrito(carritoId).
        var existente = pedidoRepo.buscarPorCarrito(carritoId);
        // [Doc] Evalúa existente.isPresent(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (existente.isPresent()) return existente.get();
        // [Doc] Variable local detalles (List<Detalle_carrito>): líneas asociadas al carrito. Se inicializa con detalleRepo.findByCarrito_IdCarrito(carritoId).
        List<Detalle_carrito> detalles = detalleRepo.findByCarrito_IdCarrito(carritoId);
        // [Doc] Evalúa detalles.isEmpty(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (detalles.isEmpty()) throw new IllegalStateException("Carrito vacío");
        // [Doc] Variable local cantidades (Map<Long, Integer>): cantidades. Se inicializa con cantidades(detalles).
        Map<Long, Integer> cantidades = cantidades(detalles);
        // [Doc] Recorre los elementos o repite según var entry : cantidades.entrySet(). Cada vuelta ejecuta el cuerpo del bucle.
        for (var entry : cantidades.entrySet()) {
            // [Doc] Variable local producto (Producto): producto procesado en este punto. Se inicializa con bloquearProducto(entry.getKey()).
            Producto producto = bloquearProducto(entry.getKey());
            // [Doc] Evalúa "inactivo".equalsIgnoreCase(producto.getEstado()) || "discontinuado".equalsIgnoreCase(producto.getEstado()). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
            if ("inactivo".equalsIgnoreCase(producto.getEstado()) || "discontinuado".equalsIgnoreCase(producto.getEstado()))
                // [Doc] Interrumpe el flujo lanzando new IllegalStateException("Producto no disponible: " + producto.getNombre_producto()). El llamador o el manejador de errores decide la respuesta.
                throw new IllegalStateException("Producto no disponible: " + producto.getNombre_producto());
            // [Doc] Evalúa producto.getStock() < entry.getValue(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
            if (producto.getStock() < entry.getValue())
                // [Doc] Interrumpe el flujo lanzando new IllegalStateException("Stock insuficiente para " + producto.getNombre_producto()). El llamador o el manejador de errores decide la respuesta.
                throw new IllegalStateException("Stock insuficiente para " + producto.getNombre_producto());
            // [Doc] Actualiza el stock de la entidad en memoria; la persistencia se realiza en la transacción: producto.setStock(producto.getStock() - entry.getValue());
            producto.setStock(producto.getStock() - entry.getValue());
            // [Doc] Solicita persistir la entidad; saveAndFlush también fuerza la sincronización: productoRepo.save(producto);
            productoRepo.save(producto);
            // [Doc] Ejecuta la llamada o modificación alertaStock.evaluar(producto); Sus efectos dependen del método invocado y del objeto receptor.
            alertaStock.evaluar(producto);
        }
        // [Doc] Variable local total (BigDecimal): importe acumulado. Se inicializa con BigDecimal.ZERO.
        BigDecimal total = BigDecimal.ZERO;
        // [Doc] Recorre los elementos o repite según Detalle_carrito d : detalles. Cada vuelta ejecuta el cuerpo del bucle.
        for (Detalle_carrito d : detalles) {
            // [Doc] Evalúa !Float.isFinite(d.getPrecio()) || d.getPrecio() < 0. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
            if (!Float.isFinite(d.getPrecio()) || d.getPrecio() < 0) throw new IllegalStateException("Precio inválido");
            // [Doc] Ejecuta la llamada o modificación total = total.add(new BigDecimal(Float.toString(d.getPrecio())).multiply(BigDecimal.valueOf(d.getCantidad()))); Sus efectos dependen del método invocado y del objeto receptor.
            total = total.add(new BigDecimal(Float.toString(d.getPrecio())).multiply(BigDecimal.valueOf(d.getCantidad())));
        }
        // [Doc] Variable local pedido (Pedido): pedido procesado en este punto. Se inicializa con new Pedido().
        Pedido pedido = new Pedido();
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: pedido.setUsuario(carrito.getUsuario());
        pedido.setUsuario(carrito.getUsuario());
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: pedido.setCarrito(carrito);
        pedido.setCarrito(carrito);
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: pedido.setFecha_pedido(java.sql.Date.valueOf(LocalDate.now()));
        pedido.setFecha_pedido(java.sql.Date.valueOf(LocalDate.now()));
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: pedido.setEstado_pedido("PREPARANDO");
        pedido.setEstado_pedido("PREPARANDO");
        // [Doc] Solicita persistir la entidad; saveAndFlush también fuerza la sincronización: pedidoRepo.save(pedido);
        pedidoRepo.save(pedido);
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: carrito.setPedido(pedido);
        carrito.setPedido(pedido);
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: carrito.setTotal(total);
        carrito.setTotal(total);
        // [Doc] Solicita persistir la entidad; saveAndFlush también fuerza la sincronización: carritoRepo.save(carrito);
        carritoRepo.save(carrito);
        // [Doc] Ejecuta la llamada o modificación seguimiento.registrar(pedido.getId_pedido(), null, pedido.getEstado_pedido()); Sus efectos dependen del método invocado y del objeto receptor.
        seguimiento.registrar(pedido.getId_pedido(), null, pedido.getEstado_pedido());
        // [Doc] Ejecuta la llamada o modificación notificaciones.notificarNuevoPedido(pedido); Sus efectos dependen del método invocado y del objeto receptor.
        notificaciones.notificarNuevoPedido(pedido);
        // [Doc] Termina el método o la función y devuelve pedido.
        return pedido;
    }

    /** [Doc]
     * Solicita cancelacion en el ámbito de PedidoService.
     * @param id identificador del registro.
     * @param usuarioId identificador del usuario.
     * @return Pedido con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override public Pedido solicitarCancelacion(Long id, Long usuarioId) {
        // [Doc] Variable local pedido (Pedido): pedido procesado en este punto. Se inicializa con bloquearPedido(id).
        Pedido pedido = bloquearPedido(id);
        // [Doc] Evalúa pedido.getUsuario() == null || !Objects.equals(pedido.getUsuario().getId(), usuarioId). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (pedido.getUsuario() == null || !Objects.equals(pedido.getUsuario().getId(), usuarioId))
            // [Doc] Interrumpe el flujo lanzando new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado"). El llamador o el manejador de errores decide la respuesta.
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        // [Doc] Termina el método o la función y devuelve transicionar(pedido, "CANCELACION_SOLICITADA").
        return transicionar(pedido, "CANCELACION_SOLICITADA");
    }
    /** [Doc]
     * Confirma cancelacion en el ámbito de PedidoService.
     * @param id identificador del registro.
     * @return Pedido con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override public Pedido confirmarCancelacion(Long id) {
        // [Doc] Variable local pedido (Pedido): pedido procesado en este punto. Se inicializa con bloquearPedido(id).
        Pedido pedido = bloquearPedido(id);
        // [Doc] Variable local actual (String): valor o entidad vigente antes de aplicar cambios. Se inicializa con estado(pedido.getEstado_pedido()).
        String actual = estado(pedido.getEstado_pedido());
        // [Doc] Evalúa !actual.equals("CANCELADO") && !actual.equals("CANCELACION_SOLICITADA"). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (!actual.equals("CANCELADO") && !actual.equals("CANCELACION_SOLICITADA"))
            // [Doc] Interrumpe el flujo lanzando new IllegalStateException("El pedido no tiene una solicitud de cancelación"). El llamador o el manejador de errores decide la respuesta.
            throw new IllegalStateException("El pedido no tiene una solicitud de cancelación");
        // [Doc] Termina el método o la función y devuelve transicionar(pedido, "CANCELADO").
        return transicionar(pedido, "CANCELADO");
    }
    /** [Doc]
     * Registra entregado en el ámbito de PedidoService.
     * @param id identificador del registro.
     * @return Pedido con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override public Pedido marcarEntregado(Long id) { return cambiarEstadoConReposicion(id, "ENTREGADO"); }
    /** [Doc]
     * Cambia estado en el ámbito de PedidoService.
     * @param id identificador del registro.
     * @param nuevo estado de destino normalizado.
     * @return Pedido con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override public Pedido cambiarEstado(Long id, String nuevo) { return cambiarEstadoConReposicion(id, nuevo); }
    /** [Doc]
     * Cambia estado con reposicion en el ámbito de PedidoService.
     * @param id identificador del registro.
     * @param nuevo estado de destino normalizado.
     * @return Pedido con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override public Pedido cambiarEstadoConReposicion(Long id, String nuevo) {
        // [Doc] Termina el método o la función y devuelve transicionar(bloquearPedido(id), nuevo).
        return transicionar(bloquearPedido(id), nuevo);
    }

    /** [Doc]
     * Valida el cambio contra los destinos admitidos, repone stock al cancelar y registra el estado y sus avisos en
     * la misma transacción.
     * @param pedido pedido procesado en este punto.
     * @param valor valor recibido o calculado para este paso.
     * @return Pedido con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private Pedido transicionar(Pedido pedido, String valor) {
        // [Doc] Variable local nuevo (String): estado de destino normalizado. Se inicializa con estado(valor).
        String anterior = estado(pedido.getEstado_pedido()), nuevo = estado(valor);
        // [Doc] Evalúa !ESTADOS.contains(nuevo). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (!ESTADOS.contains(nuevo)) throw new IllegalArgumentException("Estado inválido");
        // [Doc] Evalúa anterior.equals(nuevo). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (anterior.equals(nuevo)) return pedido;
        // [Doc] Evalúa Set.of("CANCELADO", "ENTREGADO").contains(anterior). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (Set.of("CANCELADO", "ENTREGADO").contains(anterior))
            // [Doc] Interrumpe el flujo lanzando new IllegalStateException("No se puede modificar un pedido cancelado o entregado"). El llamador o el manejador de errores decide la respuesta.
            throw new IllegalStateException("No se puede modificar un pedido cancelado o entregado");
        // [Doc] Variable local permitidos (Set<String>): valores admitidos por la validación o transición. Se inicializa con switch (anterior) { case "PENDIENTE" -> Set.of("PREPARANDO", "CANCELACION_SOLICITADA", "CANCELADO"); case "....
        Set<String> permitidos = switch (anterior) {
            // [Doc] Ejecuta la instrucción Java case: case "PENDIENTE" -> Set.of("PREPARANDO", "CANCELACION_SOLICITADA", "CANCELADO");
            case "PENDIENTE" -> Set.of("PREPARANDO", "CANCELACION_SOLICITADA", "CANCELADO");
            // [Doc] Ejecuta la instrucción Java case: case "PREPARANDO" -> Set.of("ENVIADO", "ENTREGADO", "CANCELACION_SOLICITADA", "CANCELADO");
            case "PREPARANDO" -> Set.of("ENVIADO", "ENTREGADO", "CANCELACION_SOLICITADA", "CANCELADO");
            // [Doc] Ejecuta la instrucción Java case: case "ENVIADO" -> Set.of("ENTREGADO", "CANCELACION_SOLICITADA", "CANCELADO");
            case "ENVIADO" -> Set.of("ENTREGADO", "CANCELACION_SOLICITADA", "CANCELADO");
            // Rechazar una solicitud permite continuar preparando o enviando el pedido.
            // [Doc] Ejecuta la instrucción Java case: case "CANCELACION_SOLICITADA" -> Set.of("PREPARANDO", "ENVIADO", "CANCELADO");
            case "CANCELACION_SOLICITADA" -> Set.of("PREPARANDO", "ENVIADO", "CANCELADO");
            // [Doc] Ejecuta la instrucción Java case: default -> Set.of();
            default -> Set.of();
        };
        // [Doc] Evalúa !permitidos.contains(nuevo). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (!permitidos.contains(nuevo)) throw new IllegalStateException("Transición no permitida: " + anterior + " → " + nuevo);
        // [Doc] Evalúa nuevo.equals("CANCELADO"). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (nuevo.equals("CANCELADO")) {
            // [Doc] Variable local detalles (var; tipo inferido por Java): líneas asociadas al carrito. Se inicializa con detalleRepo.findByCarrito_IdCarrito(pedido.getCarrito().getIdCarrito()).
            var detalles = detalleRepo.findByCarrito_IdCarrito(pedido.getCarrito().getIdCarrito());
            // [Doc] Evalúa detalles.isEmpty(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
            if (detalles.isEmpty()) throw new IllegalStateException("No se puede reponer un pedido sin detalles");
            // [Doc] Recorre los elementos o repite según var entry : cantidades(detalles).entrySet(). Cada vuelta ejecuta el cuerpo del bucle.
            for (var entry : cantidades(detalles).entrySet()) {
                // [Doc] Variable local producto (Producto): producto procesado en este punto. Se inicializa con bloquearProducto(entry.getKey()).
                Producto producto = bloquearProducto(entry.getKey());
                // [Doc] Actualiza el stock de la entidad en memoria; la persistencia se realiza en la transacción: producto.setStock(Math.addExact(producto.getStock(), entry.getValue()));
                producto.setStock(Math.addExact(producto.getStock(), entry.getValue()));
                // [Doc] Solicita persistir la entidad; saveAndFlush también fuerza la sincronización: productoRepo.save(producto);
                productoRepo.save(producto);
                // [Doc] Ejecuta la llamada o modificación alertaStock.evaluar(producto); Sus efectos dependen del método invocado y del objeto receptor.
                alertaStock.evaluar(producto);
            }
        }
        // [Doc] Ejecuta la llamada o modificación seguimiento.registrar(pedido.getId_pedido(), anterior, nuevo); Sus efectos dependen del método invocado y del objeto receptor.
        seguimiento.registrar(pedido.getId_pedido(), anterior, nuevo);
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: pedido.setEstado_pedido(nuevo);
        pedido.setEstado_pedido(nuevo);
        // [Doc] Solicita persistir la entidad; saveAndFlush también fuerza la sincronización: pedidoRepo.save(pedido);
        pedidoRepo.save(pedido);
        // [Doc] Evalúa nuevo.equals("CANCELACION_SOLICITADA"). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (nuevo.equals("CANCELACION_SOLICITADA")) notificaciones.notificarSolicitudCancelacion(pedido);
        else notificaciones.notificarCambioEstado(pedido, anterior);
        // [Doc] Termina el método o la función y devuelve pedido.
        return pedido;
    }

    /** [Doc]
     * Obtiene el pedido con bloqueo pesimista y refresca su estado antes de una transición.
     * @param id identificador del registro.
     * @return Pedido con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private Pedido bloquearPedido(Long id) {
        // [Doc] Variable local pedido (Pedido): pedido procesado en este punto. Se inicializa con pedidoRepo.bloquear(id).orElseThrow(() -> new NoSuchElementException("Pedido no encontrado")).
        Pedido pedido = pedidoRepo.bloquear(id).orElseThrow(() -> new NoSuchElementException("Pedido no encontrado"));
        // [Doc] Recarga la entidad desde la base usando el modo de bloqueo declarado: em.refresh(pedido, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        em.refresh(pedido, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        // [Doc] Termina el método o la función y devuelve pedido.
        return pedido;
    }
    /** [Doc]
     * Obtiene el producto con bloqueo pesimista y refresca su estado para comprobar el stock vigente.
     * @param id identificador del registro.
     * @return Producto con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private Producto bloquearProducto(Long id) {
        // [Doc] Variable local producto (Producto): producto procesado en este punto. Se inicializa con productoRepo.bloquear(id).orElseThrow(() -> new NoSuchElementException("Producto no encontrado")).
        Producto producto = productoRepo.bloquear(id).orElseThrow(() -> new NoSuchElementException("Producto no encontrado"));
        // [Doc] Recarga la entidad desde la base usando el modo de bloqueo declarado: em.refresh(producto, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        em.refresh(producto, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        // [Doc] Termina el método o la función y devuelve producto.
        return producto;
    }
    /** [Doc]
     * Rechaza cantidades nulas o no positivas y devuelve la cantidad válida.
     * @param cantidad unidades de una línea de carrito.
     * @return int con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private int cantidadValida(Integer cantidad) {
        // [Doc] Evalúa cantidad == null || cantidad <= 0. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (cantidad == null || cantidad <= 0) throw new IllegalArgumentException("Cantidad inválida");
        // [Doc] Termina el método o la función y devuelve cantidad.
        return cantidad;
    }
    /** [Doc]
     * Agrupa cantidades por producto en un TreeMap y detecta desbordamientos mediante Math.addExact.
     * @param detalles líneas asociadas al carrito.
     * @return Map&lt;Long, Integer&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private Map<Long, Integer> cantidades(List<Detalle_carrito> detalles) {
        // [Doc] Variable local resultado (Map<Long, Integer>): colección o valor que acumula el resultado de la operación. Se inicializa con new TreeMap<>().
        Map<Long, Integer> resultado = new TreeMap<>();
        // [Doc] Recorre los elementos o repite según Detalle_carrito d : detalles. Cada vuelta ejecuta el cuerpo del bucle.
        for (Detalle_carrito d : detalles) {
            // [Doc] Evalúa d.getProducto() == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
            if (d.getProducto() == null) throw new IllegalStateException("Detalle sin producto");
            // [Doc] Ejecuta la llamada o modificación resultado.merge(d.getProducto().getIdProducto(), cantidadValida(d.getCantidad()), Math::addExact); Sus efectos dependen del método invocado y del objeto receptor.
            resultado.merge(d.getProducto().getIdProducto(), cantidadValida(d.getCantidad()), Math::addExact);
        }
        // [Doc] Termina el método o la función y devuelve resultado.
        return resultado;
    }
    // ==========================================================
    // MÉTODOS PARA DASHBOARD (usan los queries del repo)
    // ==========================================================
    
	
    /** [Doc]
     * Cuenta pedidos en el ámbito de PedidoService.
     * @return Long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	@Override
    public Long contarPedidos() {
        // [Doc] Termina el método o la función y devuelve pedidoRepo.contarPedidos().
        return pedidoRepo.contarPedidos();
    }

    /** [Doc]
     * Consulta importes de ventas de del dia en el ámbito de PedidoService.
     * @param fecha fecha o etiqueta temporal de la operación.
     * @return Double con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public Double ventasDelDia(LocalDate fecha) {
        // [Doc] Termina el método o la función y devuelve pedidoRepo.ventasDelDia(fecha).
        return pedidoRepo.ventasDelDia(fecha);
    }

    /** [Doc]
     * Consulta importes de ventas de del mes en el ámbito de PedidoService.
     * @param anio año de la consulta.
     * @param mes mes de la consulta.
     * @return Double con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public Double ventasDelMes(int anio, int mes) {
        // [Doc] Termina el método o la función y devuelve pedidoRepo.ventasDelMes(anio, mes).
        return pedidoRepo.ventasDelMes(anio, mes);
    }

    /** [Doc]
     * Consulta importes de ventas de por dia en el ámbito de PedidoService.
     * @param desde inicio del intervalo consultado.
     * @param hasta fin del intervalo consultado.
     * @return List&lt;Object[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public List<Object[]> ventasPorDia(LocalDate desde, LocalDate hasta) {
        // [Doc] Termina el método o la función y devuelve pedidoRepo.ventasPorDia(desde, hasta).
        return pedidoRepo.ventasPorDia(desde, hasta);
    }

    /** [Doc]
     * Consulta importes de ventas de por mes en el ámbito de PedidoService.
     * @param anio año de la consulta.
     * @return List&lt;Object[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public List<Object[]> ventasPorMes(int anio) {
        // [Doc] Termina el método o la función y devuelve pedidoRepo.ventasPorMes(anio).
        return pedidoRepo.ventasPorMes(anio);
    }

    /** [Doc]
     * Consulta importes de ventas de por anio en el ámbito de PedidoService.
     * @param inicio límite inicial del intervalo.
     * @param fin límite final del intervalo.
     * @return List&lt;Object[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public List<Object[]> ventasPorAnio(int inicio, int fin) {
        // [Doc] Termina el método o la función y devuelve pedidoRepo.ventasPorAnio(inicio, fin).
        return pedidoRepo.ventasPorAnio(inicio, fin);
    }

    /** [Doc]
     * Resuelve la operación productos mas vendidos dentro de PedidoService; el detalle de instrucciones muestra sus
     * pasos y llamadas.
     * @return List&lt;Object[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public List<Object[]> productosMasVendidos() {
        // [Doc] Termina el método o la función y devuelve pedidoRepo.productosMasVendidos().
        return pedidoRepo.productosMasVendidos();
    }
	}
		
	



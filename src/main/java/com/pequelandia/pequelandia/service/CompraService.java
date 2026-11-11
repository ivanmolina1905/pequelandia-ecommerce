package com.pequelandia.pequelandia.service;


import com.pequelandia.pequelandia.dto.DetalleProductoDTO;
import com.pequelandia.pequelandia.dto.compraResumenDTO;
import com.pequelandia.pequelandia.model.Pedido;
import com.pequelandia.pequelandia.model.Carrito_de_compra;
import com.pequelandia.pequelandia.model.Detalle_carrito; // AJUSTAR NOMBRE
import com.pequelandia.pequelandia.repository.IPedidoRepository;

import com.pequelandia.pequelandia.service.CompraService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

/** [Doc]
 * Transforma pedidos y detalles en resúmenes de compras para presentar el historial.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CompraService implements ICompraService {

    // [Doc] Atributo pedidoRepo (IPedidoRepository): acceso de persistencia a pedido.
	@Autowired
    private  IPedidoRepository pedidoRepo;

    // [Doc] Atributo SDF (java.time.format.DateTimeFormatter): formato de fecha utilizado en los resúmenes. Se inicializa con java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"). Su referencia no se reasigna después de inicializarse.
    private static final java.time.format.DateTimeFormatter SDF = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** [Doc]
     * Obtiene compras por usuario en el ámbito de CompraService.
     * @param idUsuario identificador del usuario.
     * @return List&lt;compraResumenDTO&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public List<compraResumenDTO> obtenerComprasPorUsuario(Long idUsuario) {
        // [Doc] Variable local pedidos (List<Pedido>): pedidos o servicio de pedidos según el tipo declarado. Se inicializa con pedidoRepo.findByUsuario_Id(idUsuario).
        List<Pedido> pedidos = pedidoRepo.findByUsuario_Id(idUsuario);

        // [Doc] Termina el método o la función y devuelve pedidos.stream() .map(this::mapearAPedidoResumen) .collect(Collectors.toList()).
        return pedidos.stream()
                .map(this::mapearAPedidoResumen)
                .collect(Collectors.toList());
    }

    /** [Doc]
     * Obtiene detalle compra en el ámbito de CompraService.
     * @param idPedido identificador del pedido.
     * @return List&lt;DetalleProductoDTO&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public List<DetalleProductoDTO> obtenerDetalleCompra(Long idPedido) {
        // [Doc] Variable local pedido (Pedido): pedido procesado en este punto. Se inicializa con pedidoRepo.findById(idPedido) .orElseThrow(() -> new RuntimeException("Pedido no encontrado")).
        Pedido pedido = pedidoRepo.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // [Doc] Variable local carrito (Carrito_de_compra): carrito persistente procesado. Se inicializa con pedido.getCarrito().
        Carrito_de_compra carrito = pedido.getCarrito();
        // [Doc] Evalúa carrito == null || carrito.getDetalles() == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (carrito == null || carrito.getDetalles() == null) { 
            // [Doc] Termina el método o la función y devuelve List.of().
            return List.of();
        }

        // [Doc] Termina el método o la función y devuelve carrito.getDetalles().stream() .map(this::mapearADetalleProducto) .collect(Collectors.toList()).
        return carrito.getDetalles().stream() 
                .map(this::mapearADetalleProducto)
                .collect(Collectors.toList());
    }

    // ---- Helpers ----

    /** [Doc]
     * Convierte un pedido en su ID, fecha formateada e importe calculado para el resumen de compra.
     * @param pedido pedido procesado en este punto.
     * @return compraResumenDTO con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private compraResumenDTO mapearAPedidoResumen(Pedido pedido) {

        // [Doc] Variable local fecha (String): fecha o etiqueta temporal de la operación. Se inicializa con pedido.getFecha_pedido() != null ? SDF.format(java.time.Instant.ofEpochMilli(pedido.getFecha_pedido().getTi....
        String fecha = pedido.getFecha_pedido() != null
                ? SDF.format(java.time.Instant.ofEpochMilli(pedido.getFecha_pedido().getTime()).atZone(java.time.ZoneId.systemDefault()))
                : "-";

        // [Doc] Variable local total (double): importe acumulado. Se inicializa con calcularTotalPedido(pedido).
        double total = calcularTotalPedido(pedido);

        // [Doc] Termina el método o la función y devuelve new compraResumenDTO( pedido.getId_pedido(), fecha, total ).
        return new compraResumenDTO(
                pedido.getId_pedido(),
                fecha,
                total
        );
    }

    /** [Doc]
     * Calcula total pedido en el ámbito de CompraService.
     * @param pedido pedido procesado en este punto.
     * @return double con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private double calcularTotalPedido(Pedido pedido) {
        // [Doc] Variable local carrito (Carrito_de_compra): carrito persistente procesado. Se inicializa con pedido.getCarrito().
        Carrito_de_compra carrito = pedido.getCarrito();
        // [Doc] Evalúa carrito == null || carrito.getDetalles() == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (carrito == null || carrito.getDetalles() == null) {
            // [Doc] Termina el método o la función y devuelve 0.0.
            return 0.0;
        }

        // [Doc] Termina el método o la función y devuelve carrito.getDetalles().stream() .mapToDouble(item -> item.getCantidad() * item.getPrecio()) .sum().
        return carrito.getDetalles().stream()
                .mapToDouble(item -> item.getCantidad() * item.getPrecio())
                .sum();
    }

    /** [Doc]
     * Convierte una línea de carrito en nombre de producto, cantidad y precio unitario para mostrar la compra.
     * @param item línea del carrito procesada.
     * @return DetalleProductoDTO con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private DetalleProductoDTO mapearADetalleProducto(Detalle_carrito item) {
        // [Doc] Variable local nombreProd (String): nombre del producto mostrado en el detalle de compra. Se inicializa con item.getProducto().getNombre_producto().
        String nombreProd = item.getProducto().getNombre_producto();   // AJUSTAR
        // [Doc] Variable local cant (int): cantidad de unidades. Se inicializa con item.getCantidad().
        int cant = item.getCantidad();
        // [Doc] Variable local precio (double): importe unitario usado en el cálculo. Se inicializa con obtenerPrecioUnitario(item).
        double precio = obtenerPrecioUnitario(item);

        // [Doc] Termina el método o la función y devuelve new DetalleProductoDTO(nombreProd, cant, precio).
        return new DetalleProductoDTO(nombreProd, cant, precio);
    }

    /** [Doc]
     * Obtiene precio unitario en el ámbito de CompraService.
     * @param item línea del carrito procesada.
     * @return double con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private double obtenerPrecioUnitario(Detalle_carrito item) {
        // Si el ítem tiene precioUnitario propio, usá eso, si no, del producto
        // [Doc] Evalúa item.getPrecio() != 0. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (item.getPrecio() != 0) {   // AJUSTAR nombre
            // [Doc] Termina el método o la función y devuelve item.getPrecio().
            return item.getPrecio();
        }
        // [Doc] Termina el método o la función y devuelve item.getProducto().getPrecio().
        return item.getProducto().getPrecio();    // AJUSTAR según tu Producto
    }
}

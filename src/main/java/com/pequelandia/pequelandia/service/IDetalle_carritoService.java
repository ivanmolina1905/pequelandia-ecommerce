package com.pequelandia.pequelandia.service;

import java.util.List;

import com.pequelandia.pequelandia.model.Detalle_carrito;

/** [Doc]
 * Define el contrato de operaciones de detalle carrito que implementa el servicio correspondiente.
 */
public interface IDetalle_carritoService {

    /** [Doc]
     * Consulta y devuelve por carrito en el ámbito de IDetalle_carritoService.
     * @param carritoId identificador del carrito persistente.
     * @return List&lt;Detalle_carrito&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	List<Detalle_carrito> listarPorCarrito(Long carritoId);

    // Crea o incrementa la cantidad si ya existe la línea (carrito, producto)
    /** [Doc]
     * Exige un carrito editable y una cantidad positiva; busca o crea la línea, acumula unidades y actualiza el
     * precio desde el producto. No recalcula el total del carrito.
     * @param carritoId identificador del carrito persistente.
     * @param productoId identificador del producto.
     * @param cantidad unidades de una línea de carrito.
     * @return Detalle_carrito con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    Detalle_carrito upsert(Long carritoId, Long productoId, Integer cantidad);

    // Reemplaza la cantidad exacta
    /** [Doc]
     * Asigna unidades de una línea de carrito con el valor recibido.
     * @param carritoId identificador del carrito persistente.
     * @param productoId identificador del producto.
     * @param cantidad unidades de una línea de carrito.
     * @return Detalle_carrito con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    Detalle_carrito setCantidad(Long carritoId, Long productoId, Integer cantidad);

    // Elimina la línea (carrito, producto)
    /** [Doc]
     * Elimina los datos de la operación en el ámbito de IDetalle_carritoService.
     * @param carritoId identificador del carrito persistente.
     * @param productoId identificador del producto.
     */
    void eliminar(Long carritoId, Long productoId);
}

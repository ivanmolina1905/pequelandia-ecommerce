package com.pequelandia.pequelandia.service;

import java.util.List;

import com.pequelandia.pequelandia.dto.compraResumenDTO;
import com.pequelandia.pequelandia.dto.DetalleProductoDTO;
import com.pequelandia.pequelandia.dto.compraResumenDTO;

/** [Doc]
 * Define el contrato de operaciones de compra que implementa el servicio correspondiente.
 */
public interface ICompraService {

    /**
     * Devuelve todas las compras realizadas por un usuario.
     *
     * @param idUsuario ID del usuario.
     * @return Lista de compras resumidas para mostrar en el historial.
     */
    /** [Doc]
     * Obtiene compras por usuario en el ámbito de ICompraService.
     * @param idUsuario identificador del usuario.
     * @return List&lt;compraResumenDTO&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    List<compraResumenDTO> obtenerComprasPorUsuario(Long idUsuario);

    /**
     * Devuelve el detalle completo de productos pertenecientes a una compra.
     *
     * @param idPedido ID del pedido / compra.
     * @return Lista de productos con cantidad, precio y subtotal.
     */
    /** [Doc]
     * Obtiene detalle compra en el ámbito de ICompraService.
     * @param idPedido identificador del pedido.
     * @return List&lt;DetalleProductoDTO&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    List<DetalleProductoDTO> obtenerDetalleCompra(Long idPedido);
}

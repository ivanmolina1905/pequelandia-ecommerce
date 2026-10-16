package com.pequelandia.pequelandia.service;

import java.util.List;

import com.pequelandia.pequelandia.dto.compraResumenDTO;
import com.pequelandia.pequelandia.dto.DetalleProductoDTO;
import com.pequelandia.pequelandia.dto.compraResumenDTO;

public interface ICompraService {

    /**
     * Devuelve todas las compras realizadas por un usuario.
     *
     * @param idUsuario ID del usuario.
     * @return Lista de compras resumidas para mostrar en el historial.
     */
    List<compraResumenDTO> obtenerComprasPorUsuario(Long idUsuario);

    /**
     * Devuelve el detalle completo de productos pertenecientes a una compra.
     *
     * @param idPedido ID del pedido / compra.
     * @return Lista de productos con cantidad, precio y subtotal.
     */
    List<DetalleProductoDTO> obtenerDetalleCompra(Long idPedido);
}

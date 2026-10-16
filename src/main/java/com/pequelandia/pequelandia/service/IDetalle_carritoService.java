package com.pequelandia.pequelandia.service;

import java.util.List;

import com.pequelandia.pequelandia.model.Detalle_carrito;

public interface IDetalle_carritoService {

	List<Detalle_carrito> listarPorCarrito(Long carritoId);

    // Crea o incrementa la cantidad si ya existe la línea (carrito, producto)
    Detalle_carrito upsert(Long carritoId, Long productoId, Integer cantidad);

    // Reemplaza la cantidad exacta
    Detalle_carrito setCantidad(Long carritoId, Long productoId, Integer cantidad);

    // Elimina la línea (carrito, producto)
    void eliminar(Long carritoId, Long productoId);
}

package com.pequelandia.pequelandia.service;

import java.math.BigDecimal;
import java.util.List;

import com.pequelandia.pequelandia.model.Carrito_de_compra;
import com.pequelandia.pequelandia.model.Detalle_carrito;

import jakarta.servlet.http.HttpSession;

public interface ICarrito_de_compraService {
	 // Carrito
    List<Carrito_de_compra> listar();
    Carrito_de_compra obtener(Long id);
    Carrito_de_compra guardar(Carrito_de_compra carrito);            // usa total del front
    void eliminar(Long id);

    // Actualizar sólo el total (sin recalcular)
    Carrito_de_compra actualizarTotal(Long carritoId, BigDecimal nuevoTotal);

    // Detalles (sin tocar el total)
    Detalle_carrito agregarDetalle(Long carritoId, Long productoId, Integer cantidad);
    void quitarDetalle(Long carritoId, Long productoId);
	Carrito_de_compra getOrCreateCarrito(HttpSession session);
	
	void mergeCarritos(Long carritoInvitadoId, Long carritoUsuarioId);
	BigDecimal recalcularTotal1(Long carritoId);
	
	int items(Long carritoId);
	
	void cambiarCantidad(Long carritoId, Long productoId, int cantidadNueva);
	BigDecimal total(BigDecimal fallback, Long carritoId);
	
	
    
 
}

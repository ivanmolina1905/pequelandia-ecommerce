package com.pequelandia.pequelandia.service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pequelandia.pequelandia.model.Comprobante;
import com.pequelandia.pequelandia.model.Pedido;
import com.pequelandia.pequelandia.repository.IComprobanteRepository;
import com.pequelandia.pequelandia.repository.IPedidoRepository;

@Service
public class ComprobanteService implements IComprobanteService{

		@Autowired
		private IComprobanteRepository compRepo;
		@Autowired
	    private IPedidoRepository pedidoRepo;
		
		@Override
		public Comprobante emitir(Long pedidoId, String descripcion, String tipoComprobante) {
		    // buscar el pedido
			if (pedidoId == null)
	            throw new IllegalArgumentException("El ID del pedido es obligatorio");

	        // Buscar el pedido
	        Pedido pedido = pedidoRepo.findById(pedidoId)
	                .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado id=" + pedidoId));

	        // Evitar duplicados: 1 pedido → 1 comprobante
	        if (compRepo.existsByPedidoId(pedidoId)) {
	            throw new IllegalStateException("Ese pedido ya tiene un comprobante emitido");
	        }

	        if (pedido.getCarrito() == null)
	            throw new IllegalStateException("El pedido no tiene carrito asociado");

	        if (pedido.getCarrito().getTotal() == null)
	            throw new IllegalStateException("El carrito no tiene total definido");

	        // ======================================================
	        // GENERAR NÚMERO DE COMPROBANTE
	        // ======================================================
	        int nuevoNumero = (int) (compRepo.count() + 1);

	        // ======================================================
	        // Armamos el comprobante
	        // ======================================================
	        Comprobante c = new Comprobante();
	        c.setPedido(pedido);

	        c.setDescripcion(
	                (descripcion == null || descripcion.isBlank())
	                        ? "Compra realizada en Pequelandia"
	                        : descripcion.trim()
	        );

	        c.setFecha_venta(LocalDateTime.now());
	        c.setTotal_venta(pedido.getCarrito().getTotal());

	        c.setTipo_comprobante(
	                (tipoComprobante == null || tipoComprobante.isBlank())
	                        ? "TICKET"
	                        : tipoComprobante.trim().toUpperCase()
	        );

	        c.setN_comprobante(nuevoNumero);

	        // Guardar y retornar
	        return compRepo.save(c);
	    }
	     

}
		


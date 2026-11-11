package com.pequelandia.pequelandia.service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pequelandia.pequelandia.model.Comprobante;
import com.pequelandia.pequelandia.model.Pedido;
import com.pequelandia.pequelandia.repository.IComprobanteRepository;
import com.pequelandia.pequelandia.repository.IPedidoRepository;

/** [Doc]
 * Emite un comprobante asociado al pedido y comprueba la existencia de una emisión previa.
 */
@Service
public class ComprobanteService implements IComprobanteService{

        // [Doc] Atributo compRepo (IComprobanteRepository): acceso de persistencia a comprobante.
		@Autowired
		private IComprobanteRepository compRepo;
        // [Doc] Atributo pedidoRepo (IPedidoRepository): acceso de persistencia a pedido.
		@Autowired
	    private IPedidoRepository pedidoRepo;
		
        /** [Doc]
         * Exige un pedido con total y sin comprobante previo; calcula el número como cantidad de comprobantes más uno y
         * guarda fecha, tipo e importe.
         * @param pedidoId identificador del pedido.
         * @param descripcion texto descriptivo de la entidad u operación.
         * @param tipoComprobante tipo comprobante.
         * @return Comprobante con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
		@Override
		public Comprobante emitir(Long pedidoId, String descripcion, String tipoComprobante) {
		    // buscar el pedido
            // [Doc] Evalúa pedidoId == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
			if (pedidoId == null)
                // [Doc] Interrumpe el flujo lanzando new IllegalArgumentException("El ID del pedido es obligatorio"). El llamador o el manejador de errores decide la respuesta.
	            throw new IllegalArgumentException("El ID del pedido es obligatorio");

	        // Buscar el pedido
            // [Doc] Variable local pedido (Pedido): pedido procesado en este punto. Se inicializa con pedidoRepo.findById(pedidoId) .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado id=" + pe....
	        Pedido pedido = pedidoRepo.findById(pedidoId)
	                .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado id=" + pedidoId));

	        // Evitar duplicados: 1 pedido → 1 comprobante
            // [Doc] Evalúa compRepo.existsByPedidoId(pedidoId). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
	        if (compRepo.existsByPedidoId(pedidoId)) {
                // [Doc] Interrumpe el flujo lanzando new IllegalStateException("Ese pedido ya tiene un comprobante emitido"). El llamador o el manejador de errores decide la respuesta.
	            throw new IllegalStateException("Ese pedido ya tiene un comprobante emitido");
	        }

            // [Doc] Evalúa pedido.getCarrito() == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
	        if (pedido.getCarrito() == null)
                // [Doc] Interrumpe el flujo lanzando new IllegalStateException("El pedido no tiene carrito asociado"). El llamador o el manejador de errores decide la respuesta.
	            throw new IllegalStateException("El pedido no tiene carrito asociado");

            // [Doc] Evalúa pedido.getCarrito().getTotal() == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
	        if (pedido.getCarrito().getTotal() == null)
                // [Doc] Interrumpe el flujo lanzando new IllegalStateException("El carrito no tiene total definido"). El llamador o el manejador de errores decide la respuesta.
	            throw new IllegalStateException("El carrito no tiene total definido");

	        // ======================================================
	        // GENERAR NÚMERO DE COMPROBANTE
	        // ======================================================
            // [Doc] Variable local nuevoNumero (int): nuevo numero. Se inicializa con (int) (compRepo.count() + 1).
	        int nuevoNumero = (int) (compRepo.count() + 1);

	        // ======================================================
	        // Armamos el comprobante
	        // ======================================================
            // [Doc] Variable local c (Comprobante): comprobante de la compra. Se inicializa con new Comprobante().
	        Comprobante c = new Comprobante();
            // [Doc] Actualiza una propiedad o configuración mediante la llamada: c.setPedido(pedido);
	        c.setPedido(pedido);

            // [Doc] Actualiza una propiedad o configuración mediante la llamada: c.setDescripcion( (descripcion == null || descripcion.isBlank()) ? "Compra realizada en Pequelandia" : descripcion.trim() );
	        c.setDescripcion(
	                (descripcion == null || descripcion.isBlank())
	                        ? "Compra realizada en Pequelandia"
	                        : descripcion.trim()
	        );

            // [Doc] Actualiza una propiedad o configuración mediante la llamada: c.setFecha_venta(LocalDateTime.now());
	        c.setFecha_venta(LocalDateTime.now());
            // [Doc] Actualiza una propiedad o configuración mediante la llamada: c.setTotal_venta(pedido.getCarrito().getTotal());
	        c.setTotal_venta(pedido.getCarrito().getTotal());

            // [Doc] Actualiza una propiedad o configuración mediante la llamada: c.setTipo_comprobante( (tipoComprobante == null || tipoComprobante.isBlank()) ? "TICKET" : tipoComprobante.trim().toUpperCase() );
	        c.setTipo_comprobante(
	                (tipoComprobante == null || tipoComprobante.isBlank())
	                        ? "TICKET"
	                        : tipoComprobante.trim().toUpperCase()
	        );

            // [Doc] Actualiza una propiedad o configuración mediante la llamada: c.setN_comprobante(nuevoNumero);
	        c.setN_comprobante(nuevoNumero);

	        // Guardar y retornar
            // [Doc] Termina el método o la función y devuelve compRepo.save(c).
	        return compRepo.save(c);
	    }
	     

}
		


package com.pequelandia.pequelandia.service;

import java.math.BigDecimal;
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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;

/** [Doc]
 * Gestiona las líneas de un carrito editable y recalcula cantidades y precios.
 */
@Service
@org.springframework.transaction.annotation.Transactional
public class Detalle_carritoService implements IDetalle_carritoService{

        // [Doc] Atributo repo (IDetalle_carritoRepository): acceso de persistencia a detalle carrito.
		@Autowired
	    private IDetalle_carritoRepository repo;
        // [Doc] Atributo carritoRepo (ICarrito_de_compraRepository): acceso de persistencia a carrito de compra.
		@Autowired
	    private ICarrito_de_compraRepository carritoRepo;

        // [Doc] Atributo em (EntityManager): contexto JPA utilizado para refrescar entidades con bloqueo.
	    @PersistenceContext
	    private EntityManager em;
	    
        /** [Doc]
         * Consulta y devuelve por carrito en el ámbito de Detalle_carritoService.
         * @param carritoId identificador del carrito persistente.
         * @return List&lt;Detalle_carrito&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
	    @Override
	    public List<Detalle_carrito> listarPorCarrito(Long carritoId) {
            // [Doc] Termina el método o la función y devuelve repo.findAllByCarritoNative(carritoId).
	        return repo.findAllByCarritoNative(carritoId);
	    }

        /** [Doc]
         * Exige un carrito editable y una cantidad positiva; busca o crea la línea, acumula unidades y actualiza el
         * precio desde el producto. No recalcula el total del carrito.
         * @param carritoId identificador del carrito persistente.
         * @param productoId identificador del producto.
         * @param cantidad unidades de una línea de carrito.
         * @return Detalle_carrito con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
	    @Override
	   
	    public Detalle_carrito upsert(Long carritoId, Long productoId, Integer cantidad) {
            // [Doc] Ejecuta la llamada o modificación exigirEditable(carritoId); Sus efectos dependen del método invocado y del objeto receptor.
            exigirEditable(carritoId);
            // [Doc] Evalúa carritoId == null || productoId == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
	        if (carritoId == null || productoId == null)
                // [Doc] Interrumpe el flujo lanzando new IllegalArgumentException("carritoId y productoId son obligatorios"). El llamador o el manejador de errores decide la respuesta.
	            throw new IllegalArgumentException("carritoId y productoId son obligatorios");
            // [Doc] Evalúa cantidad == null || cantidad <= 0. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
	        if (cantidad == null || cantidad <= 0)
                // [Doc] Interrumpe el flujo lanzando new IllegalArgumentException("cantidad debe ser > 0"). El llamador o el manejador de errores decide la respuesta.
	            throw new IllegalArgumentException("cantidad debe ser > 0");

	        // Busca la línea; si existe, suma cantidad. Si no, crea una nueva.
            // [Doc] Variable local det (Detalle_carrito): línea persistente del carrito. Se inicializa con repo.findOneByCarritoAndProductoNative(carritoId, productoId) .orElseGet(() -> { Detalle_carrito n = new De....
	        Detalle_carrito det = repo.findOneByCarritoAndProductoNative(carritoId, productoId)
	                .orElseGet(() -> {
                        // [Doc] Variable local n (Detalle_carrito): línea persistente del carrito. Se inicializa con new Detalle_carrito().
	                    Detalle_carrito n = new Detalle_carrito();
                        // [Doc] Actualiza una propiedad o configuración mediante la llamada: n.setCarrito(em.getReference(Carrito_de_compra.class, carritoId));
	                    n.setCarrito(em.getReference(Carrito_de_compra.class, carritoId)); // sin SELECT
                        // [Doc] Actualiza una propiedad o configuración mediante la llamada: n.setProducto(em.getReference(Producto.class, productoId));
	                    n.setProducto(em.getReference(Producto.class, productoId));      // sin SELECT
                        // [Doc] Actualiza una propiedad o configuración mediante la llamada: n.setCantidad(0);
	                    n.setCantidad(0);
                        // [Doc] Termina el método o la función y devuelve n.
	                    return n;
	                });

            // [Doc] Actualiza una propiedad o configuración mediante la llamada: det.setCantidad(Math.addExact(det.getCantidad(), cantidad));
	        det.setCantidad(Math.addExact(det.getCantidad(), cantidad));
            // [Doc] Actualiza una propiedad o configuración mediante la llamada: det.setPrecio(det.getProducto().getPrecio());
            det.setPrecio(det.getProducto().getPrecio());
            // [Doc] Termina el método o la función y devuelve repo.save(det).
	        return repo.save(det); // NO recalculamos total del carrito
	    }

        /** [Doc]
         * Asigna unidades de una línea de carrito con el valor recibido.
         * @param carritoId identificador del carrito persistente.
         * @param productoId identificador del producto.
         * @param cantidad unidades de una línea de carrito.
         * @return Detalle_carrito con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
	    @Override
	    public Detalle_carrito setCantidad(Long carritoId, Long productoId, Integer cantidad) {
            // [Doc] Ejecuta la llamada o modificación exigirEditable(carritoId); Sus efectos dependen del método invocado y del objeto receptor.
            exigirEditable(carritoId);
            // [Doc] Evalúa cantidad == null || cantidad < 0. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
	        if (cantidad == null || cantidad < 0)
                // [Doc] Interrumpe el flujo lanzando new IllegalArgumentException("cantidad debe ser >= 0"). El llamador o el manejador de errores decide la respuesta.
	            throw new IllegalArgumentException("cantidad debe ser >= 0");

            // [Doc] Variable local det (Detalle_carrito): línea persistente del carrito. Se inicializa con repo.findOneByCarritoAndProductoNative(carritoId, productoId) .orElseThrow(() -> new NoSuchElementException....
	        Detalle_carrito det = repo.findOneByCarritoAndProductoNative(carritoId, productoId)
	                .orElseThrow(() -> new NoSuchElementException("Detalle no encontrado (carrito="
	                        + carritoId + ", producto=" + productoId + ")"));

            // [Doc] Evalúa cantidad == 0. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
	        if (cantidad == 0) { // si te mandan 0, borramos la fila
                // [Doc] Ejecuta la llamada o modificación repo.delete(det); Sus efectos dependen del método invocado y del objeto receptor.
	            repo.delete(det);
                // [Doc] Termina el método o la función y devuelve det.
	            return det;
	        }

            // [Doc] Actualiza una propiedad o configuración mediante la llamada: det.setCantidad(cantidad);
	        det.setCantidad(cantidad);
            // [Doc] Termina el método o la función y devuelve repo.save(det).
	        return repo.save(det); // NO tocamos el total
	    }

		/*
		 * @Override public void eliminar(Long carritoId, Long productoId) {
		 * Detalle_carrito det = repo.findOneByCarritoAndProductoNative(carritoId,
		 * productoId) .orElseThrow(() -> new
		 * NoSuchElementException("Detalle no encontrado (carrito=" + carritoId +
		 * ", producto=" + productoId + ")")); repo.delete(det); // NO tocamos el total
		 * }
		 */
	    
        /** [Doc]
         * Elimina los datos de la operación en el ámbito de Detalle_carritoService.
         * @param carritoId identificador del carrito persistente.
         * @param productoId identificador del producto.
         */
	    @Override
	    public void eliminar(Long carritoId, Long productoId) {
            // [Doc] Ejecuta la llamada o modificación exigirEditable(carritoId); Sus efectos dependen del método invocado y del objeto receptor.
            exigirEditable(carritoId);
            // [Doc] Ejecuta la llamada o modificación repo.findOneByCarritoAndProductoNative(carritoId, productoId) .ifPresent(repo::delete); Sus efectos dependen del método invocado y del objeto receptor.
	        repo.findOneByCarritoAndProductoNative(carritoId, productoId)
	            .ifPresent(repo::delete);
	    }
	    

    /** [Doc]
     * Bloquea y refresca el carrito y rechaza la modificación si ya pertenece a un pedido.
     * @param id identificador del registro.
     */
    private void exigirEditable(Long id) {
        // [Doc] Evalúa id == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (id == null) throw new IllegalArgumentException("Carrito requerido");
        // [Doc] Variable local carrito (var; tipo inferido por Java): carrito. Se inicializa con carritoRepo.bloquear(id).orElseThrow(() -> new NoSuchElementException("Carrito no encontrado")).
        var carrito = carritoRepo.bloquear(id).orElseThrow(() -> new NoSuchElementException("Carrito no encontrado"));
        // [Doc] Recarga la entidad desde la base usando el modo de bloqueo declarado: em.refresh(carrito, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        em.refresh(carrito, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        // [Doc] Evalúa carrito.getPedido() != null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (carrito.getPedido() != null) throw new IllegalStateException("El carrito ya pertenece a un pedido");
    }
}
	
	


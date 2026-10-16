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

@Service
public class Detalle_carritoService implements IDetalle_carritoService{

		@Autowired
	    private IDetalle_carritoRepository repo;
		@Autowired
	    private ICarrito_de_compraRepository carritoRepo;

	    @PersistenceContext
	    private EntityManager em;
	    
	    @Override
	    public List<Detalle_carrito> listarPorCarrito(Long carritoId) {
	        return repo.findAllByCarritoNative(carritoId);
	    }

	    @Override
	   
	    public Detalle_carrito upsert(Long carritoId, Long productoId, Integer cantidad) {
	        if (carritoId == null || productoId == null)
	            throw new IllegalArgumentException("carritoId y productoId son obligatorios");
	        if (cantidad == null || cantidad <= 0)
	            throw new IllegalArgumentException("cantidad debe ser > 0");

	        // Busca la línea; si existe, suma cantidad. Si no, crea una nueva.
	        Detalle_carrito det = repo.findOneByCarritoAndProductoNative(carritoId, productoId)
	                .orElseGet(() -> {
	                    Detalle_carrito n = new Detalle_carrito();
	                    n.setCarrito(em.getReference(Carrito_de_compra.class, carritoId)); // sin SELECT
	                    n.setProducto(em.getReference(Producto.class, productoId));      // sin SELECT
	                    n.setCantidad(0);
	                    return n;
	                });

	        det.setCantidad(det.getCantidad() + cantidad);
	        return repo.save(det); // NO recalculamos total del carrito
	    }

	    @Override
	    public Detalle_carrito setCantidad(Long carritoId, Long productoId, Integer cantidad) {
	        if (cantidad == null || cantidad < 0)
	            throw new IllegalArgumentException("cantidad debe ser >= 0");

	        Detalle_carrito det = repo.findOneByCarritoAndProductoNative(carritoId, productoId)
	                .orElseThrow(() -> new NoSuchElementException("Detalle no encontrado (carrito="
	                        + carritoId + ", producto=" + productoId + ")"));

	        if (cantidad == 0) { // si te mandan 0, borramos la fila
	            repo.delete(det);
	            return det;
	        }

	        det.setCantidad(cantidad);
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
	    
	    @Override
	    public void eliminar(Long carritoId, Long productoId) {
	        repo.findOneByCarritoAndProductoNative(carritoId, productoId)
	            .ifPresent(repo::delete);
	    }
	    
	 
	}
	
	


package com.pequelandia.pequelandia.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pequelandia.pequelandia.model.Carrito_de_compra;
import com.pequelandia.pequelandia.model.Detalle_carrito;
import com.pequelandia.pequelandia.model.Pedido;
import com.pequelandia.pequelandia.model.Producto;
import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.repository.ICarrito_de_compraRepository;
import com.pequelandia.pequelandia.repository.IDetalle_carritoRepository;
import com.pequelandia.pequelandia.repository.IPedidoRepository;
import com.pequelandia.pequelandia.repository.IProductoRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;


@Service
public class PedidoService implements IPedidoService{

	
	@Autowired
	private IPedidoRepository pedidoRepo;
	@Autowired
	private ICarrito_de_compraRepository carritoRepo;
	
	@Autowired private IDetalle_carritoRepository detalleRepo;
    @Autowired private IProductoRepository productoRepo;
    
    
	 @PersistenceContext
	    private EntityManager em;
	
	@Override
	public List<Pedido> getPedido() {
		return pedidoRepo.findAll();
	}
	@Override
	public void savePedido(Pedido pedi) {
		pedidoRepo.save(pedi);
	
	
	}
		
	

	@Override
	public void deletePedido(Long id) {
		if (!pedidoRepo.existsById(id)) {
            throw new NoSuchElementException("Pedido no encontrado id=" + id);
        }
        pedidoRepo.deleteById(id);	
	}

	@Override
	public Pedido findPedido(Long id) {
		 return pedidoRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Pedido no encontrado id=" + id));
	    }

	@Override
	public void editPedido(Long id, Pedido pedi) {
		Pedido db = findPedido(id); // lanza si no existe

       
         if (pedi.getUsuario() != null) db.setUsuario(pedi.getUsuario());
        if (pedi.getCarrito() != null) db.setCarrito(pedi.getCarrito());
        if (pedi.getFecha_pedido() != null) db.setFecha_pedido(pedi.getFecha_pedido());
        if (pedi.getEstado_pedido() != null) db.setEstado_pedido(pedi.getEstado_pedido());


       

        pedidoRepo.save(db);
    }
	
	@Transactional
	@Override
	public Pedido finalizarCompra(Long usuarioId, Long carritoId) {

	    if (usuarioId == null || carritoId == null)
	        throw new IllegalArgumentException("usuarioId y carritoId son obligatorios");

	    // No permitir dos pedidos para el mismo carrito
	    // ⚠️ ojo: tu método se llama existsByPedidoId(carritoId) pero debería chequear por carrito
	    // Lo dejo como lo tenés, pero lo ideal es existsByCarrito_IdCarrito(carritoId)
	    if (pedidoRepo.existsByPedidoId(carritoId))
	        throw new IllegalStateException("El carrito id=" + carritoId + " ya tiene un pedido");

	    // 1) carrito existe
	    Carrito_de_compra carrito = carritoRepo.findById(carritoId)
	            .orElseThrow(() -> new NoSuchElementException("Carrito no encontrado id=" + carritoId));

	    // 2) traer detalles del carrito
	    List<Detalle_carrito> detalles = detalleRepo.findByCarrito_IdCarrito(carritoId);
	    if (detalles == null || detalles.isEmpty())
	        throw new IllegalStateException("No podés finalizar: el carrito está vacío");

	    // 3) validar stock + descontar stock
	    for (Detalle_carrito d : detalles) {

	        int cant = (d.getCantidad() != null ? d.getCantidad() : 0);
	        if (cant <= 0)
	            throw new IllegalStateException("Cantidad inválida en el carrito");

	        Long prodId = d.getProducto().getIdProducto();

	        Producto p = productoRepo.findById(prodId)
	                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado id=" + prodId));

	        if (p.getStock() < cant) {
	            throw new IllegalStateException(
	                "Stock insuficiente para " + p.getNombre_producto() +
	                " (stock " + p.getStock() + ", pedido " + cant + ")"
	            );
	        }

	        p.setStock(p.getStock() - cant);
	        productoRepo.save(p);

	        // (opcional recomendado) congelar precio del detalle si no lo guardaste al agregar
	        // d.setPrecio(p.getPrecio());
	    }

	    // 4) calcular total desde detalle (cantidad * precio guardado en detalle)
	    // Si en tu sistema el precio "real" es producto.precio, entonces guardalo en detalle al agregar.
	    Double totalD = detalleRepo.totalCarrito(carritoId);
	    BigDecimal total = BigDecimal.valueOf(totalD != null ? totalD : 0.0);

	    // 5) crear pedido
	    Pedido pedido = new Pedido();
	    pedido.setUsuario(em.getReference(Usuario.class, usuarioId));
	    pedido.setCarrito(carrito);
	    pedido.setFecha_pedido(java.sql.Date.valueOf(LocalDate.now()));
	    pedido.setEstado_pedido("PREPARANDO");

	    // Si tu entidad Pedido tiene total, setearlo:
	    // pedido.setTotal(total);

	    Pedido finalizado = pedidoRepo.save(pedido);

	    // 6) enlazar inverso + guardar total en carrito
	    carrito.setPedido(finalizado);
	    carrito.setTotal(total);
	    carritoRepo.save(carrito);

	    return finalizado;
	}
	
	
	
	@Override
	public Pedido marcarEntregado(Long pedidoId) {
		   Pedido p = pedidoRepo.findById(pedidoId)
	                .orElseThrow(() -> new java.util.NoSuchElementException("Pedido no encontrado"));
	        // (opcional) validar transición solo desde PREPARANDO
	        if (!"PREPARANDO".equalsIgnoreCase(p.getEstado_pedido()))
	            throw new IllegalStateException("Solo se puede entregar un pedido en PREPARANDO");
	        p.setEstado_pedido("ENTREGADO");
	        return pedidoRepo.save(p);
	    }
	@Override
    public Pedido cambiarEstado(Long pedidoId, String nuevoEstado) {
        Pedido p = findPedido(pedidoId);

        if (nuevoEstado == null || nuevoEstado.isBlank())
            throw new IllegalArgumentException("Estado inválido");

        p.setEstado_pedido(nuevoEstado.trim().toUpperCase());
        return pedidoRepo.save(p);
    }
	
	public Pedido cambiarEstadoConReposicion(Long pedidoId, String nuevoEstado){

	    Pedido p = pedidoRepo.findById(pedidoId)
	            .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado id=" + pedidoId));

	    String anterior = (p.getEstado_pedido() != null) ? p.getEstado_pedido().trim().toUpperCase() : "";
	    String nuevo = (nuevoEstado != null) ? nuevoEstado.trim().toUpperCase() : "";

	    if (nuevo.isBlank())
	        throw new IllegalArgumentException("Estado inválido");

	    // ✅ Reponer solo una vez
	    if (!"CANCELADO".equals(anterior) && "CANCELADO".equals(nuevo)) {

	        Carrito_de_compra carrito = p.getCarrito();
	        if (carrito != null && carrito.getDetalles() != null) {

	            for (Detalle_carrito d : carrito.getDetalles()) {
	                if (d.getProducto() == null) continue;

	                Long prodId = d.getProducto().getIdProducto();
	                Producto prod = productoRepo.findById(prodId)
	                        .orElseThrow(() -> new NoSuchElementException("Producto no encontrado id=" + prodId));

	                int cant = (d.getCantidad() != null) ? d.getCantidad() : 0;
	                if (cant > 0) {
	                    prod.setStock(prod.getStock() + cant);
	                    productoRepo.save(prod);
	                }
	            }
	        }
	    }

	    p.setEstado_pedido(nuevo);
	    return pedidoRepo.save(p);
	}

    // ==========================================================
    // MÉTODOS PARA DASHBOARD (usan los queries del repo)
    // ==========================================================
    
	
	@Override
    public Long contarPedidos() {
        return pedidoRepo.contarPedidos();
    }

    @Override
    public Double ventasDelDia(LocalDate fecha) {
        return pedidoRepo.ventasDelDia(fecha);
    }

    @Override
    public Double ventasDelMes(int anio, int mes) {
        return pedidoRepo.ventasDelMes(anio, mes);
    }

    @Override
    public List<Object[]> ventasPorDia(LocalDate desde, LocalDate hasta) {
        return pedidoRepo.ventasPorDia(desde, hasta);
    }

    @Override
    public List<Object[]> ventasPorMes(int anio) {
        return pedidoRepo.ventasPorMes(anio);
    }

    @Override
    public List<Object[]> ventasPorAnio(int inicio, int fin) {
        return pedidoRepo.ventasPorAnio(inicio, fin);
    }

    @Override
    public List<Object[]> productosMasVendidos() {
        return pedidoRepo.productosMasVendidos();
    }
	}
		
	



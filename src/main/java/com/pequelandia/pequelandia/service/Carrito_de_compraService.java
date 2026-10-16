package com.pequelandia.pequelandia.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import com.pequelandia.pequelandia.repository.IProductoRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class Carrito_de_compraService implements ICarrito_de_compraService{

	@Autowired
	private ICarrito_de_compraRepository carritoRepo;
    @Autowired
	private IDetalle_carritoRepository detalleRepo;
    @Autowired 
    private IProductoRepository productoRepo;
    @Autowired
    private IDetalle_carritoService detalleCarritoService;
    
    @PersistenceContext
    private EntityManager em;

    // ---------------- Carrito ----------------
    @Override
    public List<Carrito_de_compra> listar() {
        return carritoRepo.findAll();
    }

    @Override
    public Carrito_de_compra obtener(Long id) {
        return carritoRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Carrito no encontrado id=" + id));
    }

    @Override
    public Carrito_de_compra guardar(Carrito_de_compra carrito) {
    	 // Si viene null → setear en 0
        if (carrito.getTotal() == null) {
            carrito.setTotal(BigDecimal.ZERO);
        }
        return carritoRepo.save(carrito);
    }

    @Override
    public void eliminar(Long id) {
        editable(id);
        if (!carritoRepo.existsById(id)) {
            throw new NoSuchElementException("Carrito no encontrado id=" + id);
        }
        carritoRepo.deleteById(id);
    }

    @Override
    public Carrito_de_compra actualizarTotal(Long carritoId, BigDecimal nuevoTotal) {
        Carrito_de_compra c = editable(carritoId);
        if (nuevoTotal == null)
            nuevoTotal = BigDecimal.ZERO;
        c.setTotal(nuevoTotal);
        return carritoRepo.save(c);
    }
    
    @Override
    public BigDecimal recalcularTotal1(Long carritoId){
        Double total = detalleRepo.totalCarrito(carritoId); // SUM(cantidad * precio)
        BigDecimal bd = BigDecimal.valueOf(total != null ? total : 0.0);
        Carrito_de_compra c = editable(carritoId);
        c.setTotal(bd);
        carritoRepo.save(c);
        return bd;
    }

    // ---------------- Detalles (tocar total) ----------------
    @Override
    public Detalle_carrito agregarDetalle(Long carritoId, Long productoId, Integer cantidad) {

        if (cantidad == null || cantidad <= 0)
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0.");

        Carrito_de_compra carrito = editable(carritoId);

        // 1) Buscar producto (para obtener PRECIO ACTUAL)
        Producto producto = productoRepo.findById(productoId)
                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado id=" + productoId));

        // 2) Ver si el detalle ya existe
        Detalle_carrito det = detalleRepo.findOneByCarritoAndProductoNative(carritoId, productoId)
                .orElseGet(() -> {
                    Detalle_carrito nuevo = new Detalle_carrito();
                    nuevo.setCarrito(carrito);
                    nuevo.setProducto(producto); // referencia real
                    nuevo.setCantidad(0);
                    return nuevo;
                });

        // 3) Actualizar cantidad
        det.setCantidad(Math.addExact(det.getCantidad(), cantidad));

        // 4) Guardar precio actual del producto 💥 CLAVE para estadísticas
        det.setPrecio(producto.getPrecio());

        return detalleRepo.save(det);
    }

    @Override
    public void quitarDetalle(Long carritoId, Long productoId) {
        editable(carritoId);

        Detalle_carrito det = detalleRepo.findOneByCarritoAndProductoNative(carritoId, productoId)
                .orElseThrow(() ->
                        new NoSuchElementException("El producto " + productoId +
                                " no está en el carrito " + carritoId));

        detalleRepo.delete(det);
    }

    @Override
 // 🔑 MÉTODO CLAVE
    public Carrito_de_compra getOrCreateCarrito(HttpSession session) {

        Usuario u = (Usuario) session.getAttribute("usuario");

        // ======================
        // USUARIO LOGUEADO
        // ======================
        if (u != null) {
            // buscar carrito activo del usuario
            return carritoRepo.findFirstByUsuarioAndPedidoIsNullOrderByIdCarritoDesc(u)
                .orElseGet(() -> {
                    Carrito_de_compra c = new Carrito_de_compra();
                    c.setUsuario(u);
                    c.setTotal(BigDecimal.ZERO);
                    return carritoRepo.save(c);
                });
        }

        // ======================
        // USUARIO INVITADO
        // ======================
        Long carritoId = (Long) session.getAttribute("CARRITO_ID");

        if (carritoId != null) {
            return carritoRepo.findById(carritoId)
                .filter(c -> c.getPedido() == null && c.getUsuario() == null)
                .orElseGet(() -> crearCarritoInvitado(session));
        }

        return crearCarritoInvitado(session);
    }

  
    private Carrito_de_compra crearCarritoInvitado(HttpSession session) {
        Carrito_de_compra c = new Carrito_de_compra();
        c.setTotal(BigDecimal.ZERO);
        c = carritoRepo.save(c);
        session.setAttribute("CARRITO_ID", c.getId_carrito());
        return c;
    }
    

 @Override public void mergeCarritos(Long carritoInvitadoId, Long
 carritoUsuarioId) {
 
 if (carritoInvitadoId.equals(carritoUsuarioId)) return;
 Carrito_de_compra origen = editable(carritoInvitadoId);
 Carrito_de_compra destino = editable(carritoUsuarioId);
 if (origen.getUsuario() != null || destino.getUsuario() == null)
     throw new IllegalArgumentException("Solo se puede incorporar un carrito invitado a un usuario");

 List<Detalle_carrito> items =
 detalleRepo.findAllByCarritoNative(carritoInvitadoId);
 
 for (Detalle_carrito d : items) { detalleCarritoService.upsert(
	  carritoUsuarioId, d.getProducto().getIdProducto(), d.getCantidad() ); }
	  
 detalleRepo.deleteAll(items);
  detalleRepo.flush();
  carritoRepo.deleteById(carritoInvitadoId);
	  
	  recalcularTotal1(carritoUsuarioId); }
	 
 @Override
 public void cambiarCantidad(Long carritoId, Long productoId, int cantidadNueva){
        editable(carritoId);
     Detalle_carrito d = detalleRepo
             .findByCarrito_IdCarritoAndProducto_IdProducto(carritoId, productoId)
             .orElseThrow(() -> new RuntimeException("No existe línea"));

     if(cantidadNueva <= 0){
         detalleRepo.delete(d);
     }else{
         d.setCantidad(cantidadNueva);
         detalleRepo.save(d);
     }

     recalcularTotal1(carritoId);
 }

 
 public void eliminarLinea(Long carritoId, Long productoId){
        editable(carritoId);
     detalleRepo.findByCarrito_IdCarritoAndProducto_IdProducto(carritoId, productoId)
             .ifPresent(detalleRepo::delete);

     recalcularTotal1(carritoId);
 }

 @Override
 public int items(Long carritoId){
     return detalleRepo.itemsCarrito(carritoId).intValue();
 }

 @Override
 public BigDecimal total(BigDecimal fallback, Long carritoId){
     // totalCarrito devuelve Double; lo convertimos a BigDecimal
     Double t = detalleRepo.totalCarrito(carritoId);
     BigDecimal total = BigDecimal.valueOf(t != null ? t : 0.0).setScale(2, RoundingMode.HALF_UP);
     return total;
 }
 public BigDecimal recalcularTotal(Long carritoId){
     BigDecimal total = total(BigDecimal.ZERO, carritoId);
     Carrito_de_compra c = editable(carritoId);
     c.setTotal(total);
     carritoRepo.save(c);
     return total;
 }


	
	

    private Carrito_de_compra editable(Long id) {
        Carrito_de_compra carrito = carritoRepo.bloquear(id).orElseThrow(() -> new NoSuchElementException("Carrito no encontrado"));
        em.refresh(carrito, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        if (carrito.getPedido() != null) throw new IllegalStateException("No se puede modificar el carrito de un pedido confirmado");
        return carrito;
    }
}

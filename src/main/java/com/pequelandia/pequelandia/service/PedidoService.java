package com.pequelandia.pequelandia.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.pequelandia.pequelandia.model.*;
import com.pequelandia.pequelandia.repository.*;

@Service
@Transactional
public class PedidoService implements IPedidoService {
    @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager em;
    @Autowired private IPedidoRepository pedidoRepo;
    @Autowired private ICarrito_de_compraRepository carritoRepo;
    @Autowired private IDetalle_carritoRepository detalleRepo;
    @Autowired private IProductoRepository productoRepo;
    @Autowired private IUsuarioRepository usuarioRepo;
    @Autowired private INotificacionService notificaciones;

    @Autowired private SeguimientoPedidoService seguimiento;
    @Autowired private AlertaStockService alertaStock;

    private static final Set<String> ESTADOS = Set.of("PENDIENTE", "PREPARANDO", "ENVIADO",
            "ENTREGADO", "CANCELACION_SOLICITADA", "CANCELADO");
    private static String estado(String valor) {
        String normal = valor == null ? "" : valor.trim().toUpperCase(Locale.ROOT);
        if (normal.equals("PREPARACIÓN") || normal.equals("PREPARACION")) return "PREPARANDO";
        return normal;
    }

    @Override public List<Pedido> getPedido() { return pedidoRepo.findAll(); }
    @Override public Pedido findPedido(Long id) {
        return pedidoRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Pedido no encontrado"));
    }
    @Override public void savePedido(Pedido pedido) {
        if (pedido.getId_pedido() == 0) {
            if (pedido.getUsuario() == null || pedido.getCarrito() == null)
                throw new IllegalArgumentException("El pedido requiere usuario y carrito");
            finalizarCompra(pedido.getUsuario().getId(), pedido.getCarrito().getIdCarrito());
        } else {
            cambiarEstadoConReposicion(pedido.getId_pedido(), pedido.getEstado_pedido());
        }
    }
    @Override public void editPedido(Long id, Pedido cambios) {
        Pedido actual = bloquearPedido(id);
        if (cambios.getUsuario() != null && !Objects.equals(actual.getUsuario().getId(), cambios.getUsuario().getId()))
            throw new IllegalArgumentException("No se puede transferir un pedido a otro usuario");
        if (cambios.getCarrito() != null && !Objects.equals(actual.getCarrito().getIdCarrito(), cambios.getCarrito().getIdCarrito()))
            throw new IllegalArgumentException("No se puede reemplazar el carrito de un pedido");
        if (cambios.getEstado_pedido() != null) transicionar(actual, cambios.getEstado_pedido());
        if (cambios.getFecha_pedido() != null) actual.setFecha_pedido(cambios.getFecha_pedido());
        pedidoRepo.save(actual);
    }
    @Override public void deletePedido(Long id) {
        Pedido pedido = bloquearPedido(id);
        if (!Set.of("CANCELADO", "ENTREGADO").contains(estado(pedido.getEstado_pedido())))
            transicionar(pedido, "CANCELADO");
        seguimiento.eliminar(id);
        pedidoRepo.delete(pedido);
    }

    @Override public Pedido comprar(Long usuarioId, Map<Long, Integer> items) {
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("Carrito vacío");
        Usuario usuario = usuarioRepo.findById(usuarioId).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        Carrito_de_compra carrito = new Carrito_de_compra();
        carrito.setUsuario(usuario);
        carrito.setTotal(BigDecimal.ZERO);
        carritoRepo.save(carrito);
        // Orden estable para evitar interbloqueos entre compras con varios productos.
        for (var entry : new TreeMap<>(items).entrySet()) {
            int cantidad = cantidadValida(entry.getValue());
            Producto producto = bloquearProducto(entry.getKey());
            Detalle_carrito detalle = new Detalle_carrito();
            detalle.setCarrito(carrito);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecio(producto.getPrecio());
            detalleRepo.save(detalle);
        }
        return finalizarCompra(usuarioId, carrito.getIdCarrito());
    }

    @Override public Pedido finalizarCompra(Long usuarioId, Long carritoId) {
        if (usuarioId == null || carritoId == null) throw new IllegalArgumentException("Usuario y carrito requeridos");
        Carrito_de_compra carrito = carritoRepo.bloquear(carritoId)
                .orElseThrow(() -> new NoSuchElementException("Carrito no encontrado"));
        em.refresh(carrito, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        if (carrito.getUsuario() == null || !Objects.equals(carrito.getUsuario().getId(), usuarioId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El carrito no pertenece al usuario");
        var existente = pedidoRepo.buscarPorCarrito(carritoId);
        if (existente.isPresent()) return existente.get();
        List<Detalle_carrito> detalles = detalleRepo.findByCarrito_IdCarrito(carritoId);
        if (detalles.isEmpty()) throw new IllegalStateException("Carrito vacío");
        Map<Long, Integer> cantidades = cantidades(detalles);
        for (var entry : cantidades.entrySet()) {
            Producto producto = bloquearProducto(entry.getKey());
            if ("inactivo".equalsIgnoreCase(producto.getEstado()) || "discontinuado".equalsIgnoreCase(producto.getEstado()))
                throw new IllegalStateException("Producto no disponible: " + producto.getNombre_producto());
            if (producto.getStock() < entry.getValue())
                throw new IllegalStateException("Stock insuficiente para " + producto.getNombre_producto());
            producto.setStock(producto.getStock() - entry.getValue());
            productoRepo.save(producto);
            alertaStock.evaluar(producto);
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Detalle_carrito d : detalles) {
            if (!Float.isFinite(d.getPrecio()) || d.getPrecio() < 0) throw new IllegalStateException("Precio inválido");
            total = total.add(new BigDecimal(Float.toString(d.getPrecio())).multiply(BigDecimal.valueOf(d.getCantidad())));
        }
        Pedido pedido = new Pedido();
        pedido.setUsuario(carrito.getUsuario());
        pedido.setCarrito(carrito);
        pedido.setFecha_pedido(java.sql.Date.valueOf(LocalDate.now()));
        pedido.setEstado_pedido("PREPARANDO");
        pedidoRepo.save(pedido);
        carrito.setPedido(pedido);
        carrito.setTotal(total);
        carritoRepo.save(carrito);
        seguimiento.registrar(pedido.getId_pedido(), null, pedido.getEstado_pedido());
        notificaciones.notificarNuevoPedido(pedido);
        return pedido;
    }

    @Override public Pedido solicitarCancelacion(Long id, Long usuarioId) {
        Pedido pedido = bloquearPedido(id);
        if (pedido.getUsuario() == null || !Objects.equals(pedido.getUsuario().getId(), usuarioId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        return transicionar(pedido, "CANCELACION_SOLICITADA");
    }
    @Override public Pedido confirmarCancelacion(Long id) {
        Pedido pedido = bloquearPedido(id);
        String actual = estado(pedido.getEstado_pedido());
        if (!actual.equals("CANCELADO") && !actual.equals("CANCELACION_SOLICITADA"))
            throw new IllegalStateException("El pedido no tiene una solicitud de cancelación");
        return transicionar(pedido, "CANCELADO");
    }
    @Override public Pedido marcarEntregado(Long id) { return cambiarEstadoConReposicion(id, "ENTREGADO"); }
    @Override public Pedido cambiarEstado(Long id, String nuevo) { return cambiarEstadoConReposicion(id, nuevo); }
    @Override public Pedido cambiarEstadoConReposicion(Long id, String nuevo) {
        return transicionar(bloquearPedido(id), nuevo);
    }

    private Pedido transicionar(Pedido pedido, String valor) {
        String anterior = estado(pedido.getEstado_pedido()), nuevo = estado(valor);
        if (!ESTADOS.contains(nuevo)) throw new IllegalArgumentException("Estado inválido");
        if (anterior.equals(nuevo)) return pedido;
        if (Set.of("CANCELADO", "ENTREGADO").contains(anterior))
            throw new IllegalStateException("No se puede modificar un pedido cancelado o entregado");
        Set<String> permitidos = switch (anterior) {
            case "PENDIENTE" -> Set.of("PREPARANDO", "CANCELACION_SOLICITADA", "CANCELADO");
            case "PREPARANDO" -> Set.of("ENVIADO", "ENTREGADO", "CANCELACION_SOLICITADA", "CANCELADO");
            case "ENVIADO" -> Set.of("ENTREGADO", "CANCELACION_SOLICITADA", "CANCELADO");
            // Rechazar una solicitud permite continuar preparando o enviando el pedido.
            case "CANCELACION_SOLICITADA" -> Set.of("PREPARANDO", "ENVIADO", "CANCELADO");
            default -> Set.of();
        };
        if (!permitidos.contains(nuevo)) throw new IllegalStateException("Transición no permitida: " + anterior + " → " + nuevo);
        if (nuevo.equals("CANCELADO")) {
            var detalles = detalleRepo.findByCarrito_IdCarrito(pedido.getCarrito().getIdCarrito());
            if (detalles.isEmpty()) throw new IllegalStateException("No se puede reponer un pedido sin detalles");
            for (var entry : cantidades(detalles).entrySet()) {
                Producto producto = bloquearProducto(entry.getKey());
                producto.setStock(Math.addExact(producto.getStock(), entry.getValue()));
                productoRepo.save(producto);
                alertaStock.evaluar(producto);
            }
        }
        seguimiento.registrar(pedido.getId_pedido(), anterior, nuevo);
        pedido.setEstado_pedido(nuevo);
        pedidoRepo.save(pedido);
        if (nuevo.equals("CANCELACION_SOLICITADA")) notificaciones.notificarSolicitudCancelacion(pedido);
        else notificaciones.notificarCambioEstado(pedido, anterior);
        return pedido;
    }

    private Pedido bloquearPedido(Long id) {
        Pedido pedido = pedidoRepo.bloquear(id).orElseThrow(() -> new NoSuchElementException("Pedido no encontrado"));
        em.refresh(pedido, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        return pedido;
    }
    private Producto bloquearProducto(Long id) {
        Producto producto = productoRepo.bloquear(id).orElseThrow(() -> new NoSuchElementException("Producto no encontrado"));
        em.refresh(producto, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        return producto;
    }
    private int cantidadValida(Integer cantidad) {
        if (cantidad == null || cantidad <= 0) throw new IllegalArgumentException("Cantidad inválida");
        return cantidad;
    }
    private Map<Long, Integer> cantidades(List<Detalle_carrito> detalles) {
        Map<Long, Integer> resultado = new TreeMap<>();
        for (Detalle_carrito d : detalles) {
            if (d.getProducto() == null) throw new IllegalStateException("Detalle sin producto");
            resultado.merge(d.getProducto().getIdProducto(), cantidadValida(d.getCantidad()), Math::addExact);
        }
        return resultado;
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
		
	



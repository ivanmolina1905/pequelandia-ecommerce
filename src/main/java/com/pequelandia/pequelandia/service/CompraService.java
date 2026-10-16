package com.pequelandia.pequelandia.service;


import com.pequelandia.pequelandia.dto.DetalleProductoDTO;
import com.pequelandia.pequelandia.dto.compraResumenDTO;
import com.pequelandia.pequelandia.model.Pedido;
import com.pequelandia.pequelandia.model.Carrito_de_compra;
import com.pequelandia.pequelandia.model.Detalle_carrito; // AJUSTAR NOMBRE
import com.pequelandia.pequelandia.repository.IPedidoRepository;

import com.pequelandia.pequelandia.service.CompraService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CompraService implements ICompraService {

	@Autowired
    private  IPedidoRepository pedidoRepo;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    public List<compraResumenDTO> obtenerComprasPorUsuario(Long idUsuario) {
        List<Pedido> pedidos = pedidoRepo.findByUsuario_Id(idUsuario);

        return pedidos.stream()
                .map(this::mapearAPedidoResumen)
                .collect(Collectors.toList());
    }

    @Override
    public List<DetalleProductoDTO> obtenerDetalleCompra(Long idPedido) {
        Pedido pedido = pedidoRepo.findById(idPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        Carrito_de_compra carrito = pedido.getCarrito();
        if (carrito == null || carrito.getDetalles() == null) { 
            return List.of();
        }

        return carrito.getDetalles().stream() 
                .map(this::mapearADetalleProducto)
                .collect(Collectors.toList());
    }

    // ---- Helpers ----

    private compraResumenDTO mapearAPedidoResumen(Pedido pedido) {

        String fecha = pedido.getFecha_pedido() != null
                ? SDF.format(pedido.getFecha_pedido())
                : "-";

        double total = calcularTotalPedido(pedido);

        return new compraResumenDTO(
                pedido.getId_pedido(),
                fecha,
                total
        );
    }

    private double calcularTotalPedido(Pedido pedido) {
        Carrito_de_compra carrito = pedido.getCarrito();
        if (carrito == null || carrito.getDetalles() == null) {
            return 0.0;
        }

        return carrito.getDetalles().stream()
                .mapToDouble(item -> item.getCantidad() * item.getPrecio())
                .sum();
    }

    private DetalleProductoDTO mapearADetalleProducto(Detalle_carrito item) {
        String nombreProd = item.getProducto().getNombre_producto();   // AJUSTAR
        int cant = item.getCantidad();
        double precio = obtenerPrecioUnitario(item);

        return new DetalleProductoDTO(nombreProd, cant, precio);
    }

    private double obtenerPrecioUnitario(Detalle_carrito item) {
        // Si el ítem tiene precioUnitario propio, usá eso, si no, del producto
        if (item.getPrecio() != 0) {   // AJUSTAR nombre
            return item.getPrecio();
        }
        return item.getProducto().getPrecio();    // AJUSTAR según tu Producto
    }
}

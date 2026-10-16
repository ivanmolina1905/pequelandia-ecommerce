package com.pequelandia.pequelandia.controller;



import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pequelandia.pequelandia.model.Carrito_de_compra;
import com.pequelandia.pequelandia.model.Detalle_carrito;
import com.pequelandia.pequelandia.model.Pedido;
import com.pequelandia.pequelandia.model.Producto;
import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.repository.ICarrito_de_compraRepository;
import com.pequelandia.pequelandia.repository.IDetalle_carritoRepository;
import com.pequelandia.pequelandia.repository.IPedidoRepository;
import com.pequelandia.pequelandia.repository.IProductoRepository;
import com.pequelandia.pequelandia.service.ICarrito_de_compraService;
import com.pequelandia.pequelandia.service.IDetalle_carritoService;
import com.pequelandia.pequelandia.service.IPedidoService;

import jakarta.servlet.http.HttpSession;
import com.pequelandia.pequelandia.service.INotificacionService;
@Controller
@RequestMapping("/carrito")
public class CarritoController {
	
	@Autowired
	private ICarrito_de_compraService carritoService;
	 @Autowired private ICarrito_de_compraRepository carritoRepo;
	    @Autowired private IDetalle_carritoService detalleService;
	    @Autowired private IProductoRepository productoRepo;
	    @Autowired private IPedidoRepository pedidoRepo; // el tuyo
	    @Autowired private IPedidoService pedidoService;
	    @Autowired private IDetalle_carritoRepository detalleRepo;
	    @Autowired
	    private INotificacionService notificacionService;

@PostMapping("/cantidad")
@ResponseBody
public Map<String, Object> apiCantidad(
        @RequestParam Long productoId,
        @RequestParam Integer cantidad,
        HttpSession session
){
    try{
    	if (cantidad == null || cantidad < 1) {
            cantidad = 1;
        }

        Producto p = productoRepo.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (p.getStock() == 0 || p.getStock() <= 0) {
            return Map.of(
                    "ok", false,
                    "error", "Sin stock disponible para " + p.getNombre_producto()
            );
        }

        if (cantidad > p.getStock()) {
            return Map.of(
                    "ok", false,
                    "error", "Stock insuficiente para " + p.getNombre_producto(),
                    "stockDisponible", p.getStock()
            );
        }
        var cart = com.pequelandia.pequelandia.util.CartSessionUtil.getOrCreate(session);
        cart.set(productoId, (cantidad == null ? 1 : cantidad));

        BigDecimal total = BigDecimal.ZERO;
        for (var entry : cart.getItems().entrySet()) {
            Producto p1 = productoRepo.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado id=" + entry.getKey()));
            int qty = entry.getValue();

            BigDecimal precio = BigDecimal.valueOf(p1.getPrecio()); // convertir float/double
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(qty));

            total = total.add(subtotal);
        }

        return Map.of("ok", true, "items", cart.getTotalItems(), "total", total);
    }catch(Exception e){
        return Map.of("ok", false, "error", e.getMessage());
    }
}

@PostMapping("/eliminar")
@ResponseBody
public Map<String, Object> apiEliminar(
        @RequestParam Long productoId,
        HttpSession session
){
    try{
        var cart = com.pequelandia.pequelandia.util.CartSessionUtil.getOrCreate(session);
        cart.remove(productoId);

        BigDecimal total = BigDecimal.ZERO;
        for (var entry : cart.getItems().entrySet()) {
            Producto p = productoRepo.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado id=" + entry.getKey()));
            int qty = entry.getValue();
            BigDecimal precio = BigDecimal.valueOf(p.getPrecio());
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(qty));

            total = total.add(subtotal);
        }

        return Map.of("ok", true, "items", cart.getTotalItems(), "total", total);
    }catch(Exception e){
        return Map.of("ok", false, "error", e.getMessage());
    }
}


@GetMapping("/vercarrito")
public String verCarrito(HttpSession session, Model model){

    var cart = com.pequelandia.pequelandia.util.CartSessionUtil.getOrCreate(session);

    // lista "vista"
    record Linea(Producto producto, int cantidad, BigDecimal subtotal) {}

    List<Linea> detalles = cart.getItems().entrySet().stream().map(entry -> {
        Producto p = productoRepo.findById(entry.getKey())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado id=" + entry.getKey()));
        int qty = entry.getValue();
        
        BigDecimal precio = BigDecimal.valueOf(p.getPrecio());
        BigDecimal sub = precio.multiply(BigDecimal.valueOf(qty));
        return new Linea(p, qty, sub);
    }).toList();

    BigDecimal total = detalles.stream()
            .map(Linea::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    model.addAttribute("detalles", detalles);
    model.addAttribute("items", cart.getTotalItems());
    model.addAttribute("total", total);

    return "carrito";
}

@PostMapping("/finalizar")
@ResponseBody

public Map<String, Object> finalizar(HttpSession session){

    Usuario u = (Usuario) session.getAttribute("usuario");
    if(u == null) {
        return Map.of("ok", false, "redirect", "/auth/login");
    }

    var cart = com.pequelandia.pequelandia.util.CartSessionUtil.getOrCreate(session);
    if (cart.isEmpty()) {
        return Map.of("ok", false, "redirect", "/carrito/vercarrito", "error", "Carrito vacío");
    }
    
 // 1) VALIDAR STOCK ANTES DE CREAR NADA
    for (var entry : cart.getItems().entrySet()) {
        Long productoId = entry.getKey();
        int qty = entry.getValue();

        Producto p = productoRepo.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado id=" + productoId));

        if (p.getStock() == 0 || p.getStock() < qty) {
            return Map.of(
                    "ok", false,
                    "error", "Stock insuficiente para: " + p.getNombre_producto(),
                    "stockDisponible", p.getStock() == 0 ? 0 : p.getStock()
            );
        }
    }
    
    // 2) crear carrito en DB 
    Carrito_de_compra c = new Carrito_de_compra();
    c.setUsuario(u);
    c.setTotal(BigDecimal.ZERO);
    c = carritoRepo.save(c);

    // 3) crear detalles en DB
    BigDecimal total = BigDecimal.ZERO;

    for (var entry : cart.getItems().entrySet()) {
        Long productoId = entry.getKey();
        int qty = entry.getValue();

        Producto p = productoRepo.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado id=" + productoId));

        BigDecimal precio = BigDecimal.valueOf(p.getPrecio());
        BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(qty));

        Detalle_carrito det = new Detalle_carrito();
        det.setCarrito(c);
        det.setProducto(p);
        det.setCantidad(qty);
        det.setPrecio(p.getPrecio());
        detalleRepo.save(det);

     
        
        total = total.add(subtotal);
    }

    c.setTotal(total);
    carritoRepo.save(c);

    // 4) generar pedido
    Pedido pedido = pedidoService.finalizarCompra(u.getId(), c.getId_carrito());
    
    notificacionService.notificarNuevoPedido(pedido);
    
    // 5) limpiar carrito de sesión
    com.pequelandia.pequelandia.util.CartSessionUtil.clear(session);

    return Map.of("ok", true, "pedidoId", pedido.getId_pedido());
}


}
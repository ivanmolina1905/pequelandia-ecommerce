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
/** [Doc]
 * Actualiza el carrito de sesión, calcula importes para la vista y delega la compra en el servicio de pedidos.
 */
@Controller
@RequestMapping("/carrito")
public class CarritoController {
	
    // [Doc] Atributo carritoService (ICarrito_de_compraService): colaborador que concentra operaciones de carrito de compra.
	@Autowired
	private ICarrito_de_compraService carritoService;
     // [Doc] Atributo carritoRepo (ICarrito_de_compraRepository): acceso de persistencia a carrito de compra.
	 @Autowired private ICarrito_de_compraRepository carritoRepo;
        // [Doc] Atributo detalleService (IDetalle_carritoService): colaborador que concentra operaciones de detalle carrito.
	    @Autowired private IDetalle_carritoService detalleService;
        // [Doc] Atributo productoRepo (IProductoRepository): acceso de persistencia a producto.
	    @Autowired private IProductoRepository productoRepo;
        // [Doc] Atributo pedidoRepo (IPedidoRepository): acceso de persistencia a pedido.
	    @Autowired private IPedidoRepository pedidoRepo; // el tuyo
        // [Doc] Atributo pedidoService (IPedidoService): colaborador que concentra operaciones de pedido.
	    @Autowired private IPedidoService pedidoService;
        // [Doc] Atributo detalleRepo (IDetalle_carritoRepository): acceso de persistencia a detalle carrito.
	    @Autowired private IDetalle_carritoRepository detalleRepo;


/** [Doc]
 * Valida la cantidad contra el stock del producto, actualiza el carrito de sesión y devuelve contador e importe
 * total o un error.
 * @param productoId identificador del producto.
 * @param cantidad unidades de una línea de carrito.
 * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
 * @return Map&lt;String, Object&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
 */
@PostMapping("/cantidad")
@ResponseBody
public Map<String, Object> apiCantidad(
        @RequestParam Long productoId,
        @RequestParam Integer cantidad,
        HttpSession session
){
    // [Doc] Ejecuta un bloque con tratamiento de excepciones; los catch asociados atienden los fallos declarados y finally se ejecuta al salir si existe.
    try{
        // [Doc] Evalúa cantidad == null || cantidad < 1. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
    	if (cantidad == null || cantidad < 1) {
            // [Doc] Ejecuta la llamada o modificación cantidad = 1; Sus efectos dependen del método invocado y del objeto receptor.
            cantidad = 1;
        }

        // [Doc] Variable local p (Producto): producto consultado para esta operación. Se inicializa con productoRepo.findById(productoId) .orElseThrow(() -> new RuntimeException("Producto no encontrado")).
        Producto p = productoRepo.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // [Doc] Evalúa p.getStock() == 0 || p.getStock() <= 0. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (p.getStock() == 0 || p.getStock() <= 0) {
            // [Doc] Termina el método o la función y devuelve Map.of( "ok", false, "error", "Sin stock disponible para " + p.getNombre_producto() ).
            return Map.of(
                    "ok", false,
                    "error", "Sin stock disponible para " + p.getNombre_producto()
            );
        }

        // [Doc] Evalúa cantidad > p.getStock(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (cantidad > p.getStock()) {
            // [Doc] Termina el método o la función y devuelve Map.of( "ok", false, "error", "Stock insuficiente para " + p.getNombre_producto(), "stockDisponible", p.getStock() ).
            return Map.of(
                    "ok", false,
                    "error", "Stock insuficiente para " + p.getNombre_producto(),
                    "stockDisponible", p.getStock()
            );
        }
        // [Doc] Variable local cart (var; tipo inferido por Java): carrito en memoria de la sesión. Se inicializa con com.pequelandia.pequelandia.util.CartSessionUtil.getOrCreate(session).
        var cart = com.pequelandia.pequelandia.util.CartSessionUtil.getOrCreate(session);
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: cart.set(productoId, (cantidad == null ? 1 : cantidad));
        cart.set(productoId, (cantidad == null ? 1 : cantidad));

        // [Doc] Variable local total (BigDecimal): importe acumulado. Se inicializa con BigDecimal.ZERO.
        BigDecimal total = BigDecimal.ZERO;
        // [Doc] Recorre los elementos o repite según var entry : cart.getItems().entrySet(). Cada vuelta ejecuta el cuerpo del bucle.
        for (var entry : cart.getItems().entrySet()) {
            // [Doc] Variable local p1 (Producto): producto consultado para esta operación. Se inicializa con productoRepo.findById(entry.getKey()) .orElseThrow(() -> new RuntimeException("Producto no encontrado id=" ....
            Producto p1 = productoRepo.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado id=" + entry.getKey()));
            // [Doc] Variable local qty (int): unidades de una línea. Se inicializa con entry.getValue().
            int qty = entry.getValue();

            // [Doc] Variable local precio (BigDecimal): importe unitario usado en el cálculo. Se inicializa con BigDecimal.valueOf(p1.getPrecio()).
            BigDecimal precio = BigDecimal.valueOf(p1.getPrecio()); // convertir float/double
            // [Doc] Variable local subtotal (BigDecimal): importe de una línea: precio por cantidad. Se inicializa con precio.multiply(BigDecimal.valueOf(qty)).
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(qty));

            // [Doc] Ejecuta la llamada o modificación total = total.add(subtotal); Sus efectos dependen del método invocado y del objeto receptor.
            total = total.add(subtotal);
        }

        // [Doc] Termina el método o la función y devuelve Map.of("ok", true, "items", cart.getTotalItems(), "total", total).
        return Map.of("ok", true, "items", cart.getTotalItems(), "total", total);
    }catch(Exception e){
        // [Doc] Termina el método o la función y devuelve Map.of("ok", false, "error", e.getMessage()).
        return Map.of("ok", false, "error", e.getMessage());
    }
}

/** [Doc]
 * Quita un producto del carrito de sesión y recalcula contador e importe para actualizar la pantalla.
 * @param productoId identificador del producto.
 * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
 * @return Map&lt;String, Object&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
 */
@PostMapping("/eliminar")
@ResponseBody
public Map<String, Object> apiEliminar(
        @RequestParam Long productoId,
        HttpSession session
){
    // [Doc] Ejecuta un bloque con tratamiento de excepciones; los catch asociados atienden los fallos declarados y finally se ejecuta al salir si existe.
    try{
        // [Doc] Variable local cart (var; tipo inferido por Java): carrito en memoria de la sesión. Se inicializa con com.pequelandia.pequelandia.util.CartSessionUtil.getOrCreate(session).
        var cart = com.pequelandia.pequelandia.util.CartSessionUtil.getOrCreate(session);
        // [Doc] Ejecuta la llamada o modificación cart.remove(productoId); Sus efectos dependen del método invocado y del objeto receptor.
        cart.remove(productoId);

        // [Doc] Variable local total (BigDecimal): importe acumulado. Se inicializa con BigDecimal.ZERO.
        BigDecimal total = BigDecimal.ZERO;
        // [Doc] Recorre los elementos o repite según var entry : cart.getItems().entrySet(). Cada vuelta ejecuta el cuerpo del bucle.
        for (var entry : cart.getItems().entrySet()) {
            // [Doc] Variable local p (Producto): producto consultado para esta operación. Se inicializa con productoRepo.findById(entry.getKey()) .orElseThrow(() -> new RuntimeException("Producto no encontrado id=" ....
            Producto p = productoRepo.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado id=" + entry.getKey()));
            // [Doc] Variable local qty (int): unidades de una línea. Se inicializa con entry.getValue().
            int qty = entry.getValue();
            // [Doc] Variable local precio (BigDecimal): importe unitario usado en el cálculo. Se inicializa con BigDecimal.valueOf(p.getPrecio()).
            BigDecimal precio = BigDecimal.valueOf(p.getPrecio());
            // [Doc] Variable local subtotal (BigDecimal): importe de una línea: precio por cantidad. Se inicializa con precio.multiply(BigDecimal.valueOf(qty)).
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(qty));

            // [Doc] Ejecuta la llamada o modificación total = total.add(subtotal); Sus efectos dependen del método invocado y del objeto receptor.
            total = total.add(subtotal);
        }

        // [Doc] Termina el método o la función y devuelve Map.of("ok", true, "items", cart.getTotalItems(), "total", total).
        return Map.of("ok", true, "items", cart.getTotalItems(), "total", total);
    }catch(Exception e){
        // [Doc] Termina el método o la función y devuelve Map.of("ok", false, "error", e.getMessage()).
        return Map.of("ok", false, "error", e.getMessage());
    }
}


/** [Doc]
 * Construye las líneas con producto, cantidad y subtotal, calcula el total y renderiza la plantilla carrito.
 * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
 * @param model atributos que recibe la plantilla Thymeleaf.
 * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
 */
@GetMapping("/vercarrito")
public String verCarrito(HttpSession session, Model model){

    // [Doc] Variable local cart (var; tipo inferido por Java): carrito en memoria de la sesión. Se inicializa con com.pequelandia.pequelandia.util.CartSessionUtil.getOrCreate(session).
    var cart = com.pequelandia.pequelandia.util.CartSessionUtil.getOrCreate(session);

    // lista "vista"
    /** [Doc]
     * Define el tipo Linea y los datos u operaciones declarados en su cuerpo.
     */
    record Linea(Producto producto, int cantidad, BigDecimal subtotal) {}

    // [Doc] Variable local detalles (List<Linea>): líneas asociadas al carrito. Se inicializa con cart.getItems().entrySet().stream().map(entry -> { Producto p = productoRepo.findById(entry.getKey()) .orEl....
    List<Linea> detalles = cart.getItems().entrySet().stream().map(entry -> {
        // [Doc] Variable local p (Producto): producto consultado para esta operación. Se inicializa con productoRepo.findById(entry.getKey()) .orElseThrow(() -> new RuntimeException("Producto no encontrado id=" ....
        Producto p = productoRepo.findById(entry.getKey())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado id=" + entry.getKey()));
        // [Doc] Variable local qty (int): unidades de una línea. Se inicializa con entry.getValue().
        int qty = entry.getValue();
        
        // [Doc] Variable local precio (BigDecimal): importe unitario usado en el cálculo. Se inicializa con BigDecimal.valueOf(p.getPrecio()).
        BigDecimal precio = BigDecimal.valueOf(p.getPrecio());
        // [Doc] Variable local sub (BigDecimal): subtotal de la línea. Se inicializa con precio.multiply(BigDecimal.valueOf(qty)).
        BigDecimal sub = precio.multiply(BigDecimal.valueOf(qty));
        // [Doc] Termina el método o la función y devuelve new Linea(p, qty, sub).
        return new Linea(p, qty, sub);
    }).toList();

    // [Doc] Variable local total (BigDecimal): importe acumulado. Se inicializa con detalles.stream() .map(Linea::subtotal) .reduce(BigDecimal.ZERO, BigDecimal::add).
    BigDecimal total = detalles.stream()
            .map(Linea::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("detalles", detalles);
    model.addAttribute("detalles", detalles);
    // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("items", cart.getTotalItems());
    model.addAttribute("items", cart.getTotalItems());
    // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("total", total);
    model.addAttribute("total", total);

    // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
    return "carrito";
}

/** [Doc]
 * Exige usuario, serializa el checkout de la sesión, compra desde una copia del carrito y lo vacía después de la
 * compra exitosa.
 * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
 * @return Map&lt;String, Object&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
 */
@PostMapping("/finalizar")
@ResponseBody

public Map<String, Object> finalizar(HttpSession session){

    // [Doc] Variable local u (Usuario): cuenta de usuario utilizada por la operación. Se inicializa con (Usuario) session.getAttribute("usuario").
    Usuario u = (Usuario) session.getAttribute("usuario");
    // [Doc] Evalúa u == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
    if(u == null) {
        // [Doc] Termina el método o la función y devuelve Map.of("ok", false, "redirect", "/auth/login").
        return Map.of("ok", false, "redirect", "/auth/login");
    }

    // Serializa el checkout de la misma sesión; el servicio protege el stock en PostgreSQL.
    // [Doc] Exige acceso exclusivo al monitor org.springframework.web.util.WebUtils.getSessionMutex(session) mientras se ejecuta el bloque, para serializar operaciones concurrentes.
    synchronized (org.springframework.web.util.WebUtils.getSessionMutex(session)) {
        // [Doc] Variable local cart (var; tipo inferido por Java): carrito en memoria de la sesión. Se inicializa con com.pequelandia.pequelandia.util.CartSessionUtil.getOrCreate(session).
        var cart = com.pequelandia.pequelandia.util.CartSessionUtil.getOrCreate(session);
        // [Doc] Evalúa cart.isEmpty(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (cart.isEmpty()) return Map.of("ok", false, "error", "Carrito vacío");
        // [Doc] Variable local pedido (Pedido): pedido procesado en este punto. Se inicializa con pedidoService.comprar(u.getId(), cart.snapshot()).
        Pedido pedido = pedidoService.comprar(u.getId(), cart.snapshot());
        // [Doc] Ejecuta la llamada o modificación com.pequelandia.pequelandia.util.CartSessionUtil.clear(session); Sus efectos dependen del método invocado y del objeto receptor.
        com.pequelandia.pequelandia.util.CartSessionUtil.clear(session);
        // [Doc] Termina el método o la función y devuelve Map.of("ok", true, "pedidoId", pedido.getId_pedido()).
        return Map.of("ok", true, "pedidoId", pedido.getId_pedido());
    }
}
}

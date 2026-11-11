package com.pequelandia.pequelandia.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pequelandia.pequelandia.model.Producto;
import com.pequelandia.pequelandia.repository.ICarrito_de_compraRepository;
import com.pequelandia.pequelandia.repository.IProductoRepository;
import com.pequelandia.pequelandia.service.ICarrito_de_compraService;
import com.pequelandia.pequelandia.service.ICategoriaService;
import com.pequelandia.pequelandia.service.IDetalle_carritoService;
import com.pequelandia.pequelandia.service.IProductoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** [Doc]
 * Presenta el catálogo filtrado y permite agregar productos al carrito.
 */
@Controller
@RequestMapping("/productos")
public class CatalogoController {

    // [Doc] Atributo productoService (IProductoService): colaborador que concentra operaciones de producto.
    @Autowired private IProductoService productoService;
    // [Doc] Atributo categoriaService (ICategoriaService): colaborador que concentra operaciones de categoria.
    @Autowired private ICategoriaService categoriaService;
    // [Doc] Atributo carritoService (ICarrito_de_compraService): colaborador que concentra operaciones de carrito de compra.
    @Autowired private ICarrito_de_compraService carritoService;
    // [Doc] Atributo carritorepo (ICarrito_de_compraRepository): acceso de persistencia a carrito de compra.
    @Autowired private ICarrito_de_compraRepository carritorepo;
    // [Doc] Atributo detalleCarritoService (IDetalle_carritoService): colaborador que concentra operaciones de detalle carrito.
    @Autowired private IDetalle_carritoService detalleCarritoService;
    // [Doc] Atributo productoRepo (IProductoRepository): acceso de persistencia a producto.
    @Autowired private IProductoRepository productoRepo;

    /** [Doc]
     * Aplica búsqueda, filtros, orden y paginación del catálogo y expone los productos a su plantilla.
     * @param q q.
     * @param categoriaId categoria id.
     * @param precioMin precio min.
     * @param precioMax precio max.
     * @param talle talle.
     * @param ordenar ordenar.
     * @param pageable parámetros de paginación y orden.
     * @param model atributos que recibe la plantilla Thymeleaf.
     * @param request petición HTTP entrante.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping
    public String catalogo(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Double precioMin,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) String talle,
            @RequestParam(defaultValue = "relevancia") String ordenar,
            @PageableDefault(size = 12) Pageable pageable,
            Model model, HttpServletRequest request
    ) {
        // [Doc] Variable local page (int): índice de página solicitado, comenzando en cero. Se inicializa con Math.max(pageable.getPageNumber(), 0).
        int page = Math.max(pageable.getPageNumber(), 0);
        // [Doc] Variable local size (int): size. Se inicializa con Math.max(1, Math.min(pageable.getPageSize(), 60)).
        int size = Math.max(1, Math.min(pageable.getPageSize(), 60));

        // [Doc] Variable local sort (Sort): sort. Se inicializa con switch (ordenar) { case "precio-asc" -> Sort.by("precio").ascending(); case "precio-desc" -> Sort.by("preci....
        Sort sort = switch (ordenar) {
            // [Doc] Ejecuta la instrucción Java case: case "precio-asc" -> Sort.by("precio").ascending();
            case "precio-asc"  -> Sort.by("precio").ascending();
            // [Doc] Ejecuta la instrucción Java case: case "precio-desc" -> Sort.by("precio").descending();
            case "precio-desc" -> Sort.by("precio").descending();
            // [Doc] Ejecuta la instrucción Java case: case "alfabetico" -> Sort.by("nombre_producto").ascending();
            case "alfabetico"  -> Sort.by("nombre_producto").ascending();
            // [Doc] Ejecuta la instrucción Java case: default -> Sort.unsorted();
            default            -> Sort.unsorted();
        };
        // [Doc] Variable local p2 (Pageable): p2. Se inicializa con PageRequest.of(page, size, sort).
        Pageable p2 = PageRequest.of(page, size, sort);
        
     // Normalizar filtros
        // [Doc] Variable local qNorm (String): q norm. Se inicializa con (q == null) ? null : q.trim().
        String qNorm = (q == null) ? null : q.trim();
        // [Doc] Evalúa qNorm != null && qNorm.isEmpty(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (qNorm != null && qNorm.isEmpty()) qNorm = null;

        // [Doc] Variable local talleNorm (String): talle norm. Se inicializa con (talle == null) ? null : talle.trim().
        String talleNorm = (talle == null) ? null : talle.trim();
        // [Doc] Evalúa talleNorm != null && talleNorm.isEmpty(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (talleNorm != null && talleNorm.isEmpty()) talleNorm = null;
        
        // [Doc] Variable local filtroCat (Long): filtro cat. Se inicializa con (categoriaId != null && categoriaId == 0) ? null : categoriaId.
        Long filtroCat = (categoriaId != null && categoriaId == 0) ? null : categoriaId;
        
     // 👉 usar filtroCat (no categoriaId)
        // [Doc] Variable local productos (Page<Producto>): productos. Se inicializa con productoRepo.buscarPublico( qNorm, filtroCat, precioMin, precioMax, talleNorm, p2 ).
        Page<Producto> productos = productoRepo.buscarPublico(
                qNorm,
                filtroCat,
                precioMin,
                precioMax,
                talleNorm,
                p2
        );
        
        // [Doc] Variable local currentUrl (String): current url. Se inicializa con request.getRequestURI().
        String currentUrl = request.getRequestURI();
        // [Doc] Evalúa request.getQueryString() != null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (request.getQueryString() != null) {
            // [Doc] Ejecuta la llamada o modificación currentUrl += "?" + request.getQueryString(); Sus efectos dependen del método invocado y del objeto receptor.
            currentUrl += "?" + request.getQueryString();
        }
        
        // [Doc] Variable local categoriaNombre (String): categoria nombre. Se inicializa con null.
        String categoriaNombre = null;
        // [Doc] Evalúa filtroCat != null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (filtroCat != null) {
            // [Doc] Ejecuta la llamada o modificación categoriaNombre = categoriaService.getCategorias().stream() .filter(c -> c.getId_categoria()==filtroCat.longValue()) .map(c -> c.getNombre_categoria()) .findFirst() .o... Sus efectos dependen del método invocado y del objeto receptor.
            categoriaNombre = categoriaService.getCategorias().stream()
                    .filter(c -> c.getId_categoria()==filtroCat.longValue())
                    .map(c -> c.getNombre_categoria())
                    .findFirst()
                    .orElse("HCategoría");
        }

        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("productos", productos);
        model.addAttribute("productos", productos);
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("q", qNorm);
        model.addAttribute("q", qNorm);
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("ordenar", ordenar);
        model.addAttribute("ordenar", ordenar);
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("categoriaId", categoriaId);
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("precioMin", precioMin);
        model.addAttribute("precioMin", precioMin);
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("precioMax", precioMax);
        model.addAttribute("precioMax", precioMax);
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("talle", talleNorm);
        model.addAttribute("talle", talleNorm);
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("categoriaNombre", categoriaNombre);
        model.addAttribute("categoriaNombre", categoriaNombre);
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("categorias", categoriaService.getCategorias());
        model.addAttribute("categorias", categoriaService.getCategorias());
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("currentUrl", currentUrl);
        model.addAttribute("currentUrl", currentUrl);
        // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
        return "verproductos";
    }
   
	
      /** [Doc]
       * Agrega al carrito en el ámbito de CatalogoController.
       * @param productoId identificador del producto.
       * @param cantidad unidades de una línea de carrito.
       * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
       * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
       */
	  @PostMapping("/agregar") 
	  public String agregarAlCarrito(
	  
		  @RequestParam Long productoId,
	        @RequestParam(defaultValue = "1") Integer cantidad,
	        HttpSession session
	) {
        // [Doc] Variable local cart (var; tipo inferido por Java): carrito en memoria de la sesión. Se inicializa con com.pequelandia.pequelandia.util.CartSessionUtil.getOrCreate(session).
	    var cart = com.pequelandia.pequelandia.util.CartSessionUtil.getOrCreate(session);
        synchronized (cart) {
            validarAgregado(productoId, cantidad, cart.getItems().getOrDefault(productoId, 0));
            cart.add(productoId, cantidad);
        }
        // [Doc] Termina la acción y solicita al navegador una redirección a la ruta indicada.
	    return "redirect:/productos";
	  }
 
	  
      /** [Doc]
       * Valida el producto y la cantidad solicitados y agrega unidades al carrito de sesión mediante la API.
       * @param productoId identificador del producto.
       * @param cantidad unidades de una línea de carrito.
       * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
       * @return Map&lt;String, Object&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
       */
	  @PostMapping("/api/agregar")
	    @ResponseBody
	    public Map<String, Object> apiAgregar(
	            @RequestParam Long productoId,
	            @RequestParam(defaultValue = "1") Integer cantidad,
	            HttpSession session
	    ) {
            // [Doc] Ejecuta un bloque con tratamiento de excepciones; los catch asociados atienden los fallos declarados y finally se ejecuta al salir si existe.
	        try {
                // [Doc] Evalúa cantidad == null || cantidad < 1. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
	            var cart = com.pequelandia.pequelandia.util.CartSessionUtil.getOrCreate(session);
            Producto producto;
            int nuevoTotalProducto;
            synchronized (cart) {
                producto = validarAgregado(productoId, cantidad, cart.getItems().getOrDefault(productoId, 0));
                nuevoTotalProducto = Math.addExact(cart.getItems().getOrDefault(productoId, 0), cantidad);
                cart.add(productoId, cantidad);
            }

            BigDecimal total = BigDecimal.ZERO;
                // [Doc] Recorre los elementos o repite según var entry : cart.getItems().entrySet(). Cada vuelta ejecuta el cuerpo del bucle.
	            for (var entry : cart.getItems().entrySet()) {
                    // [Doc] Variable local p (Producto): producto consultado para esta operación. Se inicializa con productoRepo.findById(entry.getKey()).orElseThrow().
	                Producto p = productoRepo.findById(entry.getKey()).orElseThrow();

                    // [Doc] Variable local precio (BigDecimal): importe unitario usado en el cálculo. Se inicializa con BigDecimal.valueOf(p.getPrecio()).
	                BigDecimal precio = BigDecimal.valueOf(p.getPrecio());
                    // [Doc] Variable local subtotal (BigDecimal): importe de una línea: precio por cantidad. Se inicializa con precio.multiply(BigDecimal.valueOf(entry.getValue())).
	                BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(entry.getValue()));
                    // [Doc] Ejecuta la llamada o modificación total = total.add(subtotal); Sus efectos dependen del método invocado y del objeto receptor.
	                total = total.add(subtotal);
	            }

                // [Doc] Variable local items (int): elementos del carrito o de la colección procesada. Se inicializa con cart.getTotalItems().
	            int items = cart.getTotalItems();

                // [Doc] Termina el método o la función y devuelve Map.of( "ok", true, "items", items, "total", total, "stockDisponible", producto.getStock() - nuevoTotalProducto ).
	            return Map.of(
	                    "ok", true,
	                    "items", items,
	                    "total", total,
	                    "stockDisponible", producto.getStock() - nuevoTotalProducto
	            );

	        } catch (Exception e) {
                // [Doc] Termina el método o la función y devuelve Map.of("ok", false, "error", e.getMessage()).
	            return Map.of("ok", false, "error", e.getMessage());
	        }
	    }
	    private Producto validarAgregado(Long id, Integer cantidad, int actual) {
        if (cantidad == null || cantidad < 1) throw new IllegalArgumentException("Cantidad inválida");
        Producto p = productoRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        if (!"activo".equalsIgnoreCase(p.getEstado())) throw new IllegalArgumentException("Producto no disponible para la venta");
        if (!Float.isFinite(p.getPrecio()) || p.getPrecio() < 0) throw new IllegalArgumentException("Precio inválido");
        if ((long) actual + cantidad > p.getStock()) throw new IllegalArgumentException("Stock insuficiente");
        return p;
    }
}

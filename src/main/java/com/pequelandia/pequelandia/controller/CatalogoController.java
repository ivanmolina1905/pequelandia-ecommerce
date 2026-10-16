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

@Controller
@RequestMapping("/productos")
public class CatalogoController {

    @Autowired private IProductoService productoService;
    @Autowired private ICategoriaService categoriaService;
    @Autowired private ICarrito_de_compraService carritoService;
    @Autowired private ICarrito_de_compraRepository carritorepo;
    @Autowired private IDetalle_carritoService detalleCarritoService;
    @Autowired private IProductoRepository productoRepo;

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
        int page = Math.max(pageable.getPageNumber(), 0);
        int size = Math.max(1, Math.min(pageable.getPageSize(), 60));

        Sort sort = switch (ordenar) {
            case "precio-asc"  -> Sort.by("precio").ascending();
            case "precio-desc" -> Sort.by("precio").descending();
            case "alfabetico"  -> Sort.by("nombre_producto").ascending();
            default            -> Sort.unsorted();
        };
        Pageable p2 = PageRequest.of(page, size, sort);
        
     // Normalizar filtros
        String qNorm = (q == null) ? null : q.trim();
        if (qNorm != null && qNorm.isEmpty()) qNorm = null;

        String talleNorm = (talle == null) ? null : talle.trim();
        if (talleNorm != null && talleNorm.isEmpty()) talleNorm = null;
        
        Long filtroCat = (categoriaId != null && categoriaId == 0) ? null : categoriaId;
        
     // 👉 usar filtroCat (no categoriaId)
        Page<Producto> productos = productoRepo.buscarPublico(
                qNorm,
                filtroCat,
                precioMin,
                precioMax,
                talleNorm,
                p2
        );
        
        String currentUrl = request.getRequestURI();
        if (request.getQueryString() != null) {
            currentUrl += "?" + request.getQueryString();
        }
        
        String categoriaNombre = null;
        if (filtroCat != null) {
            categoriaNombre = categoriaService.getCategorias().stream()
                    .filter(c -> c.getId_categoria()==filtroCat.longValue())
                    .map(c -> c.getNombre_categoria())
                    .findFirst()
                    .orElse("HCategoría");
        }

        model.addAttribute("productos", productos);
        model.addAttribute("q", qNorm);
        model.addAttribute("ordenar", ordenar);
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("precioMin", precioMin);
        model.addAttribute("precioMax", precioMax);
        model.addAttribute("talle", talleNorm);
        model.addAttribute("categoriaNombre", categoriaNombre);
        model.addAttribute("categorias", categoriaService.getCategorias());
        model.addAttribute("currentUrl", currentUrl);
        return "verproductos";
    }
   
	
	  @PostMapping("/agregar") 
	  public String agregarAlCarrito(
	  
		  @RequestParam Long productoId,
	        @RequestParam(defaultValue = "1") Integer cantidad,
	        HttpSession session
	) {
	    var cart = com.pequelandia.pequelandia.util.CartSessionUtil.getOrCreate(session);
	    cart.add(productoId, cantidad == null ? 1 : cantidad);
	    return "redirect:/productos";
	  }
 
	  
	  @PostMapping("/api/agregar")
	    @ResponseBody
	    public Map<String, Object> apiAgregar(
	            @RequestParam Long productoId,
	            @RequestParam(defaultValue = "1") Integer cantidad,
	            HttpSession session
	    ) {
	        try {
	            if (cantidad == null || cantidad < 1) {
	                cantidad = 1;
	            }

	            Producto producto = productoRepo.findById(productoId).orElse(null);
	            if (producto == null) {
	                return Map.of("ok", false, "error", "Producto no encontrado");
	            }

	            if (producto.getStock() == 0 || producto.getStock() <= 0) {
	                return Map.of("ok", false, "error", "Sin stock");
	            }

	            var cart = com.pequelandia.pequelandia.util.CartSessionUtil.getOrCreate(session);

	            int enCarrito = cart.getItems().getOrDefault(productoId, 0);
	            int nuevoTotalProducto = enCarrito + cantidad;

	            if (nuevoTotalProducto > producto.getStock()) {
	                return Map.of(
	                        "ok", false,
	                        "error", "Stock insuficiente",
	                        "stockDisponible", producto.getStock()
	                );
	            }

	            cart.add(productoId, cantidad);

	            BigDecimal total = BigDecimal.ZERO;
	            for (var entry : cart.getItems().entrySet()) {
	                Producto p = productoRepo.findById(entry.getKey()).orElseThrow();

	                BigDecimal precio = BigDecimal.valueOf(p.getPrecio());
	                BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(entry.getValue()));
	                total = total.add(subtotal);
	            }

	            int items = cart.getTotalItems();

	            return Map.of(
	                    "ok", true,
	                    "items", items,
	                    "total", total,
	                    "stockDisponible", producto.getStock() - nuevoTotalProducto
	            );

	        } catch (Exception e) {
	            return Map.of("ok", false, "error", e.getMessage());
	        }
	    }
	}
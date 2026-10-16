package com.pequelandia.pequelandia.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pequelandia.pequelandia.model.Categoria;
import com.pequelandia.pequelandia.model.Producto;
import com.pequelandia.pequelandia.service.ICategoriaService;
import com.pequelandia.pequelandia.service.IEstadisticasService;
import com.pequelandia.pequelandia.service.IProductoService;

@Controller
@RequestMapping("/admin/productos")
public class AdminProductoController {

    @Autowired private IProductoService productoService;
    @Autowired private ICategoriaService categoriaService;
    @Autowired private IEstadisticasService estadisticasService;

    // LISTAR + BUSCAR + PAGINAR
    @GetMapping
    public String listar(@RequestParam(required = false) String q,
                         @PageableDefault(size = 10) Pageable pageable,
                         Model model) {

        int page = Math.max(pageable.getPageNumber(), 0);
        int size = pageable.getPageSize() <= 0 ? 10 : Math.min(pageable.getPageSize(), 100);
        Pageable normalizado = PageRequest.of(page, size, Sort.by(Sort.Order.asc("idProducto")));

        Page<Producto> productos = productoService.buscar(q, normalizado);

        model.addAttribute("productos", productos);
        model.addAttribute("q", q);
        model.addAttribute("seccion", "productos");
        model.addAttribute("categorias", categoriaService.getCategorias()); // para selects
        model.addAttribute("estad", estadisticasService.getDashboardResumen());
      
        return "paneladministracion";
    }

    // NUEVO (POST del modal "newProductModal")  >>> AHORA acepta varias categorías
    @PostMapping("/nuevo")
    public String crear(Producto producto,
                        @RequestParam(name = "categoriaIds", required = false) List<Long> categoriaIds,
                        RedirectAttributes ra) {

        // Normalizar lista (evitar null)
        List<Categoria> cats = (categoriaIds == null || categoriaIds.isEmpty())
                ? List.of()
                : categoriaService.findAllById(categoriaIds);  // <-- asegurate de tener este método

        producto.setCategorias(cats);

        if (producto.getEstado() == null || producto.getEstado().isBlank())
            producto.setEstado("activo");

        productoService.saveProducto(producto);
        ra.addFlashAttribute("ok", "Producto creado correctamente.");
        return "redirect:/admin/productos";
    }

    // EDITAR (POST del modal "editProductModal")  >>> AHORA acepta varias categorías
    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id,
                         Producto cambios,
                         @RequestParam(name = "categoriaIds", required = false) List<Long> categoriaIds,
                         RedirectAttributes ra) {

        Producto db = productoService.findProducto(id);
        if (db == null) {
            ra.addFlashAttribute("error", "El producto no existe.");
            return "redirect:/admin/productos";
        }

        // Copiar campos
        db.setNombre_producto(cambios.getNombre_producto());
        db.setDescripcion_producto(cambios.getDescripcion_producto());
        db.setPrecio(cambios.getPrecio());
        db.setStock(cambios.getStock());
        db.setMarca_producto(cambios.getMarca_producto());
        db.setModelo_producto(cambios.getModelo_producto());
        db.setTalle_producto(cambios.getTalle_producto());
        db.setEstado(cambios.getEstado());

        // Categorías muchas-a-muchas
        List<Categoria> cats = (categoriaIds == null || categoriaIds.isEmpty())
                ? List.of()
                : categoriaService.findAllById(categoriaIds);
        db.setCategorias(cats);

        productoService.saveProducto(db);
        ra.addFlashAttribute("ok", "Producto actualizado.");
        return "redirect:/admin/productos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            productoService.deleteProducto(id);
            ra.addFlashAttribute("ok", "Producto eliminado.");
        } catch (DataIntegrityViolationException ex) {
            ra.addFlashAttribute("error", "No se puede eliminar: el producto tiene registros asociados.");
        }
        return "redirect:/admin/productos";
    }
}

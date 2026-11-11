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

/** [Doc]
 * Gestiona creación, edición, listado y eliminación de productos desde administración.
 */
@Controller
@RequestMapping("/admin/productos")
public class AdminProductoController {

    // [Doc] Atributo productoService (IProductoService): colaborador que concentra operaciones de producto.
    @Autowired private IProductoService productoService;
    // [Doc] Atributo categoriaService (ICategoriaService): colaborador que concentra operaciones de categoria.
    @Autowired private ICategoriaService categoriaService;
    // [Doc] Atributo estadisticasService (IEstadisticasService): colaborador que concentra operaciones de estadisticas.
    @Autowired private IEstadisticasService estadisticasService;

    // LISTAR + BUSCAR + PAGINAR
    /** [Doc]
     * Consulta y devuelve los datos de la operación en el ámbito de AdminProductoController.
     * @param q q.
     * @param pageable parámetros de paginación y orden.
     * @param model atributos que recibe la plantilla Thymeleaf.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping
    public String listar(@RequestParam(required = false) String q,
                         @PageableDefault(size = 10) Pageable pageable,
                         Model model) {

        // [Doc] Variable local page (int): índice de página solicitado, comenzando en cero. Se inicializa con Math.max(pageable.getPageNumber(), 0).
        int page = Math.max(pageable.getPageNumber(), 0);
        // [Doc] Variable local size (int): size. Se inicializa con pageable.getPageSize() <= 0 ? 10 : Math.min(pageable.getPageSize(), 100).
        int size = pageable.getPageSize() <= 0 ? 10 : Math.min(pageable.getPageSize(), 100);
        // [Doc] Variable local normalizado (Pageable): normalizado. Se inicializa con PageRequest.of(page, size, Sort.by(Sort.Order.asc("idProducto"))).
        Pageable normalizado = PageRequest.of(page, size, Sort.by(Sort.Order.asc("idProducto")));

        // [Doc] Variable local productos (Page<Producto>): productos. Se inicializa con productoService.buscar(q, normalizado).
        Page<Producto> productos = productoService.buscar(q, normalizado);

        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("productos", productos);
        model.addAttribute("productos", productos);
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("q", q);
        model.addAttribute("q", q);
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("seccion", "productos");
        model.addAttribute("seccion", "productos");
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("categorias", categoriaService.getCategorias());
        model.addAttribute("categorias", categoriaService.getCategorias()); // para selects
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("estad", estadisticasService.getDashboardResumen());
        model.addAttribute("estad", estadisticasService.getDashboardResumen());
      
        // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
        return "paneladministracion";
    }

    // NUEVO (POST del modal "newProductModal")  >>> AHORA acepta varias categorías
    /** [Doc]
     * Asocia las categorías elegidas, aplica el estado activo por defecto y delega el guardado antes de redirigir al
     * listado.
     * @param producto producto procesado en este punto.
     * @param categoriaIds categoria ids.
     * @param ra ra.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @PostMapping("/nuevo")
    public String crear(Producto producto,
                        @RequestParam(name = "categoriaIds", required = false) List<Long> categoriaIds,
                        RedirectAttributes ra) {

        // Normalizar lista (evitar null)
        // [Doc] Variable local cats (List<Categoria>): cats. Se inicializa con (categoriaIds == null || categoriaIds.isEmpty()) ? List.of() : categoriaService.findAllById(categoriaIds).
        List<Categoria> cats = (categoriaIds == null || categoriaIds.isEmpty())
                ? List.of()
                : categoriaService.findAllById(categoriaIds);  // <-- asegurate de tener este método

        // [Doc] Actualiza una propiedad o configuración mediante la llamada: producto.setCategorias(cats);
        producto.setCategorias(cats);

        // [Doc] Evalúa producto.getEstado() == null || producto.getEstado().isBlank(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (producto.getEstado() == null || producto.getEstado().isBlank())
            // [Doc] Actualiza una propiedad o configuración mediante la llamada: producto.setEstado("activo");
            producto.setEstado("activo");

        // [Doc] Ejecuta la llamada o modificación productoService.saveProducto(producto); Sus efectos dependen del método invocado y del objeto receptor.
        productoService.saveProducto(producto);
        // [Doc] Ejecuta la llamada o modificación ra.addFlashAttribute("ok", "Producto creado correctamente."); Sus efectos dependen del método invocado y del objeto receptor.
        ra.addFlashAttribute("ok", "Producto creado correctamente.");
        // [Doc] Termina la acción y solicita al navegador una redirección a la ruta indicada.
        return "redirect:/admin/productos";
    }

    // EDITAR (POST del modal "editProductModal")  >>> AHORA acepta varias categorías
    /** [Doc]
     * Actualiza los datos de la operación en el ámbito de AdminProductoController.
     * @param id identificador del registro.
     * @param cambios producto procesado en este punto.
     * @param categoriaIds categoria ids.
     * @param ra ra.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id,
                         Producto cambios,
                         @RequestParam(name = "categoriaIds", required = false) List<Long> categoriaIds,
                         RedirectAttributes ra) {

        // [Doc] Variable local db (Producto): producto procesado en este punto. Se inicializa con productoService.findProducto(id).
        Producto db = productoService.findProducto(id);
        // [Doc] Evalúa db == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (db == null) {
            // [Doc] Ejecuta la llamada o modificación ra.addFlashAttribute("error", "El producto no existe."); Sus efectos dependen del método invocado y del objeto receptor.
            ra.addFlashAttribute("error", "El producto no existe.");
            // [Doc] Termina la acción y solicita al navegador una redirección a la ruta indicada.
            return "redirect:/admin/productos";
        }

        // Copiar campos
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: db.setNombre_producto(cambios.getNombre_producto());
        db.setNombre_producto(cambios.getNombre_producto());
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: db.setDescripcion_producto(cambios.getDescripcion_producto());
        db.setDescripcion_producto(cambios.getDescripcion_producto());
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: db.setPrecio(cambios.getPrecio());
        db.setPrecio(cambios.getPrecio());
        // [Doc] Actualiza el stock de la entidad en memoria; la persistencia se realiza en la transacción: db.setStock(cambios.getStock());
        db.setStock(cambios.getStock());
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: db.setMarca_producto(cambios.getMarca_producto());
        db.setMarca_producto(cambios.getMarca_producto());
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: db.setModelo_producto(cambios.getModelo_producto());
        db.setModelo_producto(cambios.getModelo_producto());
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: db.setTalle_producto(cambios.getTalle_producto());
        db.setTalle_producto(cambios.getTalle_producto());
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: db.setEstado(cambios.getEstado());
        db.setEstado(cambios.getEstado());

        // Categorías muchas-a-muchas
        // [Doc] Variable local cats (List<Categoria>): cats. Se inicializa con (categoriaIds == null || categoriaIds.isEmpty()) ? List.of() : categoriaService.findAllById(categoriaIds).
        List<Categoria> cats = (categoriaIds == null || categoriaIds.isEmpty())
                ? List.of()
                : categoriaService.findAllById(categoriaIds);
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: db.setCategorias(cats);
        db.setCategorias(cats);

        // [Doc] Ejecuta la llamada o modificación productoService.saveProducto(db); Sus efectos dependen del método invocado y del objeto receptor.
        productoService.saveProducto(db);
        // [Doc] Ejecuta la llamada o modificación ra.addFlashAttribute("ok", "Producto actualizado."); Sus efectos dependen del método invocado y del objeto receptor.
        ra.addFlashAttribute("ok", "Producto actualizado.");
        // [Doc] Termina la acción y solicita al navegador una redirección a la ruta indicada.
        return "redirect:/admin/productos";
    }

    /** [Doc]
     * Elimina los datos de la operación en el ámbito de AdminProductoController.
     * @param id identificador del registro.
     * @param ra ra.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        // [Doc] Ejecuta un bloque con tratamiento de excepciones; los catch asociados atienden los fallos declarados y finally se ejecuta al salir si existe.
        try {
            // [Doc] Ejecuta la llamada o modificación productoService.deleteProducto(id); Sus efectos dependen del método invocado y del objeto receptor.
            productoService.deleteProducto(id);
            // [Doc] Ejecuta la llamada o modificación ra.addFlashAttribute("ok", "Producto eliminado."); Sus efectos dependen del método invocado y del objeto receptor.
            ra.addFlashAttribute("ok", "Producto eliminado.");
        } catch (DataIntegrityViolationException ex) {
            // [Doc] Ejecuta la llamada o modificación ra.addFlashAttribute("error", "No se puede eliminar: el producto tiene registros asociados."); Sus efectos dependen del método invocado y del objeto receptor.
            ra.addFlashAttribute("error", "No se puede eliminar: el producto tiene registros asociados.");
        }
        // [Doc] Termina la acción y solicita al navegador una redirección a la ruta indicada.
        return "redirect:/admin/productos";
    }
}

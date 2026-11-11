package com.pequelandia.pequelandia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.pequelandia.pequelandia.service.IProductoService;

/** [Doc]
 * Resuelve páginas iniciales y redirecciones a las vistas principales.
 */
@Controller
public class HomeController {

     // [Doc] Atributo productoService (IProductoService): colaborador que concentra operaciones de producto.
	 @Autowired private IProductoService productoService;
	 // ======================================================
    // HOME / INDEX
    // ======================================================
    
    
    /** [Doc]
     * Consulta ocho productos aleatorios, los expone como productosRandom y presenta indexv1.
     * @param model atributos que recibe la plantilla Thymeleaf.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping({"/", "/indexv1","/indexv1.html"})
    public String home(Model model) {
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("productosRandom", productoService.obtenerAleatorios(8));
        model.addAttribute("productosRandom", productoService.obtenerAleatorios(8));
        // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
        return "indexv1";
    }

 // ======================================================
    // LOGIN
    // ======================================================
    /** [Doc]
     * Presenta la plantilla login para las rutas de acceso compatibles.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping({"/login", "/login.html"})
    public String login() { return "login"; }

 // ======================================================
    // PANEL USUARIO
    // ======================================================
    /** [Doc]
     * Redirige las rutas compatibles del panel personal al historial del usuario.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping({"/panelusuario", "/panelusuario.html"})
    public String panelUsuario() { return "redirect:/panelusuarios/panelhistorial"; }
    
    
 // ======================================================
    // PANEL ADMIN
    // ======================================================
    /** [Doc]
     * Redirige las rutas compatibles de administración al listado administrativo de usuarios.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping({"/paneladministracion", "/paneladministracion.html"})
    public String panelAdmin() { return "redirect:/admin/usuarios"; }

 // ======================================================
    // CARRITO
    // ======================================================
    /** [Doc]
     * Presenta la plantilla carrito desde las rutas de acceso compatibles.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping({"/carrito", "/carrito.html"})
    public String carrito() { return "carrito"; }
    
 // ======================================================
    // REDIRECCIÓN A LISTADO DE PRODUCTOS
    // ======================================================
    /** [Doc]
     * Redirige las rutas antiguas de productos a /productos.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping({"/verproductos", "/verproductos.html"})
    public String verProductosRedirect() {
        // [Doc] Termina la acción y solicita al navegador una redirección a la ruta indicada.
        return "redirect:/productos";
    }
}

package com.pequelandia.pequelandia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.pequelandia.pequelandia.service.IProductoService;

@Controller
public class HomeController {

	 @Autowired private IProductoService productoService;
	 // ======================================================
    // HOME / INDEX
    // ======================================================
    
    
    @GetMapping({"/", "/indexv1","/indexv1.html"})
    public String home(Model model) {
        model.addAttribute("productosRandom", productoService.obtenerAleatorios(8));
        return "indexv1";
    }

 // ======================================================
    // LOGIN
    // ======================================================
    @GetMapping({"/login", "/login.html"})
    public String login() { return "login"; }

 // ======================================================
    // PANEL USUARIO
    // ======================================================
    @GetMapping({"/panelusuario", "/panelusuario.html"})
    public String panelUsuario() { return "panelusuario"; }
    
    
 // ======================================================
    // PANEL ADMIN
    // ======================================================
    @GetMapping({"/paneladministracion", "/paneladministracion.html"})
    public String panelAdmin() { return "paneladministracion"; }

 // ======================================================
    // CARRITO
    // ======================================================
    @GetMapping({"/carrito", "/carrito.html"})
    public String carrito() { return "carrito"; }
    
 // ======================================================
    // REDIRECCIÓN A LISTADO DE PRODUCTOS
    // ======================================================
    @GetMapping({"/verproductos", "/verproductos.html"})
    public String verProductosRedirect() {
        return "redirect:/productos";
    }
}

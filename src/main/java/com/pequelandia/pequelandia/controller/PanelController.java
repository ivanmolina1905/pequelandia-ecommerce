package com.pequelandia.pequelandia.controller;

import com.pequelandia.pequelandia.dto.EstadisticasAdminDTO;
import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.service.IEstadisticasService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PanelController {

	@Autowired
    private IEstadisticasService estadisticasService;

	
    @GetMapping("/panel/usuario")
    public String panelUsuario(HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuario");
        if (u == null) {
            return "redirect:/login";
        }
        return "redirect:/panelusuarios/panelhistorial?section=perfil";
    }

    @GetMapping("/panel/admin")
    public String panelAdmin(HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuario");
        if (u == null) {
            return "redirect:/login";
        }
        // Doble seguridad: si no es admin, no entra
        //if (!"admin".equalsIgnoreCase(u.getNombre_Usuario())) {
          //  return "redirect:/panel/usuario";
        //}
        EstadisticasAdminDTO estad = estadisticasService.getDashboardResumen();
        model.addAttribute("estad", estad);
        return "redirect:/admin/usuarios";
    }
    
    @GetMapping("/panel/indexv1.html")
    public String index() { return "indexv1"; }


   
}



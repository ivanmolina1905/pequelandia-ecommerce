package com.pequelandia.pequelandia.controller;

import com.pequelandia.pequelandia.dto.EstadisticasAdminDTO;
import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.service.IEstadisticasService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** [Doc]
 * Resuelve los accesos a los paneles de administración y usuario.
 */
@Controller
public class PanelController {

    // [Doc] Atributo estadisticasService (IEstadisticasService): colaborador que concentra operaciones de estadisticas.
	@Autowired
    private IEstadisticasService estadisticasService;

	
    /** [Doc]
     * Exige sesión y redirige al historial personal con la sección de perfil activa.
     * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping("/panel/usuario")
    public String panelUsuario(HttpSession session) {
        // [Doc] Variable local u (Usuario): cuenta de usuario utilizada por la operación. Se inicializa con (Usuario) session.getAttribute("usuario").
        Usuario u = (Usuario) session.getAttribute("usuario");
        // [Doc] Evalúa u == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (u == null) {
            // [Doc] Termina la acción y solicita al navegador una redirección a la ruta indicada.
            return "redirect:/login";
        }
        // [Doc] Termina la acción y solicita al navegador una redirección a la ruta indicada.
        return "redirect:/panelusuarios/panelhistorial?section=perfil";
    }

    /** [Doc]
     * Comprueba la sesión, consulta el resumen estadístico y redirige al listado administrativo de usuarios.
     * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
     * @param model atributos que recibe la plantilla Thymeleaf.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping("/panel/admin")
    public String panelAdmin(HttpSession session, Model model) {
        // [Doc] Variable local u (Usuario): cuenta de usuario utilizada por la operación. Se inicializa con (Usuario) session.getAttribute("usuario").
        Usuario u = (Usuario) session.getAttribute("usuario");
        // [Doc] Evalúa u == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (u == null) {
            // [Doc] Termina la acción y solicita al navegador una redirección a la ruta indicada.
            return "redirect:/login";
        }
        // Doble seguridad: si no es admin, no entra
        //if (!"admin".equalsIgnoreCase(u.getNombre_Usuario())) {
          //  return "redirect:/panel/usuario";
        //}
        // [Doc] Variable local estad (EstadisticasAdminDTO): estad. Se inicializa con estadisticasService.getDashboardResumen().
        EstadisticasAdminDTO estad = estadisticasService.getDashboardResumen();
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("estad", estad);
        model.addAttribute("estad", estad);
        // [Doc] Termina la acción y solicita al navegador una redirección a la ruta indicada.
        return "redirect:/admin/usuarios";
    }
    
    /** [Doc]
     * Presenta la plantilla indexv1 desde su ruta alternativa bajo /panel.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping("/panel/indexv1.html")
    public String index() { return "indexv1"; }


   
}



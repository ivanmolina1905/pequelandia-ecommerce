package com.pequelandia.pequelandia.controller;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.pequelandia.pequelandia.dto.DetalleProductoDTO;
import com.pequelandia.pequelandia.dto.compraResumenDTO;
import com.pequelandia.pequelandia.model.Localidad;
import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.service.ICompraService;
import com.pequelandia.pequelandia.service.IEstadisticasService;
import com.pequelandia.pequelandia.service.ILocalidadService;
import com.pequelandia.pequelandia.service.IUsuarioService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/usuarios")

public class AdminUsuarioController {

	@Autowired
	private IUsuarioService usuarioService;
	@Autowired
	private ILocalidadService localidadService;
	@Autowired private IEstadisticasService estadisticasService;
	@Autowired  private  ICompraService compraService;
//	public AdminUsuarioController(IUsuarioService usuarioService, ILocalidadService localidadService) {
//        this.usuarioService = usuarioService;
//        this.localidadService = localidadService;
//    }
	
	// ---- Binder: fecha + bloquear teléfono ----
    @InitBinder("usuario")
    public void initBinder(WebDataBinder binder) {
        // Fecha: yyyy-MM-dd -> java.util.Date
        var sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(sdf, true));

        
    }
    
	//listar + busqueda+ paginacion
	

	@GetMapping
	public String listar(
	        @RequestParam(name = "q", defaultValue = "") String q,
	        @PageableDefault(size = 10) Pageable pageable,
	        Model model
	) {
	    int page = Math.max(pageable.getPageNumber(), 0);
	    int size = pageable.getPageSize() <= 0 ? 10 : Math.min(pageable.getPageSize(), 100);

	    // ⚠️ Forzamos SIEMPRE a ordenar por "id" y NO usamos el sort de la request
	    Pageable normalizado = PageRequest.of(page, size, Sort.by(Sort.Order.asc("id")));

	    Page<Usuario> usuarios = usuarioService.listar(q, normalizado);

	    // (opcional para debug rápido)
	    System.out.printf("page=%d size=%d elemsPage=%d totalElems=%d%n",
	            usuarios.getNumber(), usuarios.getSize(),
	            usuarios.getNumberOfElements(), usuarios.getTotalElements());

	    model.addAttribute("usuarios", usuarios);
	    model.addAttribute("q", q);
	    model.addAttribute("seccion", "gestion");
	    
	   model.addAttribute("localidades", localidadService.getTodas()); // o getActivas()
	   model.addAttribute("estad", estadisticasService.getDashboardResumen());

	    return "paneladministracion";
	}
	
	
	
	 
	
	
	//ver historial (Redireccion a detalles )
	
	//@GetMapping("/{id}/historial")
	//public String historial (@PathVariable Long id, Model model) {
	//	model.addAttribute("Id_usuario", id);
	//	return "redirect:/admin/usuarios/"+id+"/historial/detalle";
	//}
	
	
	
	 
	 // EDITAR (guardar)
	
	  @PostMapping("/{id}/editar")
	  public String editarGuardar(@PathVariable Long id,@ModelAttribute("usuario") Usuario datos) {
	  usuarioService.editUsuario(id, datos); 
	  return "redirect:/admin/usuarios"; }
	  
	 // ELIMINAR
	 
	 @PostMapping("/{id}/eliminar") 
	 public String eliminar(@PathVariable Long id)
	 { usuarioService.deleteUsuario(id); return "redirect:/admin/usuarios"; }
	 
	
	 //alta usuario
	 @PostMapping("/nuevo")
	 public String crearUsuario(
	         @Valid @ModelAttribute("usuario") Usuario datos,
	         BindingResult br,
	         @RequestParam("localidadId") Long localidadId,
	         Model model,
	         RedirectAttributes ra
	 ) {

	     // ======================================================
	     // VALIDAR TELÉFONO
	     // ======================================================
	     String telefono = datos.getTelefono();

	     if (telefono != null && !telefono.isBlank()) {

	         // Permitimos números, espacios, guiones y prefijo +
	         String telefonoLimpio = telefono.trim();

	         if (!telefonoLimpio.matches("[0-9+\\-\\s]{8,20}")) {
	             br.rejectValue(
	                 "telefono",
	                 "format",
	                 "Ingresá un teléfono válido."
	             );
	         } else {
	             datos.setTelefono(telefonoLimpio);
	         }

	     } else {
	         datos.setTelefono(null);
	     }

	     // ======================================================
	     // SI HAY ERRORES: VOLVER A MOSTRAR EL MODAL
	     // ======================================================
	     if (br.hasErrors()) {

	         model.addAttribute(
	             "localidades",
	             localidadService.getTodas()
	         );

	         model.addAttribute(
	             "seccion",
	             "gestion"
	         );

	         model.addAttribute(
	             "showNewUserModal",
	             true
	         );

	         Page<Usuario> usuarios =
	             usuarioService.listar(
	                 "",
	                 PageRequest.of(
	                     0,
	                     10,
	                     Sort.by("id").ascending()
	                 )
	             );

	         model.addAttribute("usuarios", usuarios);
	         model.addAttribute("q", "");
	         model.addAttribute(
	             "estad",
	             estadisticasService.getDashboardResumen()
	         );

	         return "paneladministracion";
	     }

	     // ======================================================
	     // VALORES INICIALES DEL USUARIO
	     // ======================================================
	     datos.setFecha_registro(new Date());
	     datos.setUltima_conexion(null);
	     datos.setPuntos(0);

	     Localidad localidad =
	         localidadService.findLocalidad(localidadId);

	     datos.setLocalidad(localidad);

	     usuarioService.saveUsuario(datos);

	     ra.addFlashAttribute(
	         "ok",
	         "Usuario creado correctamente."
	     );

	     return "redirect:/admin/usuarios";
	 }
	 
	 
	 @GetMapping("/{idUsuario}/compras")
	    @ResponseBody
	    public List<compraResumenDTO> historialCompras(@PathVariable Long idUsuario) {
	        return compraService.obtenerComprasPorUsuario(idUsuario);
	    }

	    @GetMapping("/compras/{idCompra}/detalle")
	    @ResponseBody
	    public List<DetalleProductoDTO> detalleCompra(@PathVariable Long idCompra) {
	        return compraService.obtenerDetalleCompra(idCompra);
	    }
	    
	    
	 
	 }
	 


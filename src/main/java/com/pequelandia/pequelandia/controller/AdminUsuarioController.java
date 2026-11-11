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

/** [Doc]
 * Gestiona las cuentas desde administración y consulta su historial de compras.
 */
@Controller
@RequestMapping("/admin/usuarios")

public class AdminUsuarioController {

    // [Doc] Atributo usuarioService (IUsuarioService): colaborador que concentra operaciones de usuario.
	@Autowired
	private IUsuarioService usuarioService;
    // [Doc] Atributo localidadService (ILocalidadService): colaborador que concentra operaciones de localidad.
	@Autowired
	private ILocalidadService localidadService;
    // [Doc] Atributo estadisticasService (IEstadisticasService): colaborador que concentra operaciones de estadisticas.
	@Autowired private IEstadisticasService estadisticasService;
    // [Doc] Atributo compraService (ICompraService): colaborador que concentra operaciones de compra.
	@Autowired  private  ICompraService compraService;
//	public AdminUsuarioController(IUsuarioService usuarioService, ILocalidadService localidadService) {
//        this.usuarioService = usuarioService;
//        this.localidadService = localidadService;
//    }
	
	// ---- Binder: fecha + bloquear teléfono ----
    /** [Doc]
     * Configura la conversión de valores del formulario antes de vincularlos a objetos Java.
     * @param binder binder.
     */
    @InitBinder("usuario")
    public void initBinder(WebDataBinder binder) {
        // Fecha: yyyy-MM-dd -> java.util.Date
        // [Doc] Variable local sdf (var; tipo inferido por Java): sdf. Se inicializa con new SimpleDateFormat("yyyy-MM-dd").
        var sdf = new SimpleDateFormat("yyyy-MM-dd");
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: sdf.setLenient(false);
        sdf.setLenient(false);
        // [Doc] Ejecuta la llamada o modificación binder.registerCustomEditor(Date.class, new CustomDateEditor(sdf, true));
        binder.setAllowedFields("nombre_usuario", "apellido", "email", "dni", "fecha_nacimiento",
                "telefono", "direccion", "contraseña", "rol");
        binder.registerCustomEditor(Date.class, new CustomDateEditor(sdf, true));

        
    }
    
	//listar + busqueda+ paginacion
	

    /** [Doc]
     * Consulta y devuelve los datos de la operación en el ámbito de AdminUsuarioController.
     * @param q q.
     * @param pageable parámetros de paginación y orden.
     * @param model atributos que recibe la plantilla Thymeleaf.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	@GetMapping
	public String listar(
	        @RequestParam(name = "q", defaultValue = "") String q,
	        @PageableDefault(size = 10) Pageable pageable,
	        Model model
	) {
        // [Doc] Variable local page (int): índice de página solicitado, comenzando en cero. Se inicializa con Math.max(pageable.getPageNumber(), 0).
	    int page = Math.max(pageable.getPageNumber(), 0);
        // [Doc] Variable local size (int): size. Se inicializa con pageable.getPageSize() <= 0 ? 10 : Math.min(pageable.getPageSize(), 100).
	    int size = pageable.getPageSize() <= 0 ? 10 : Math.min(pageable.getPageSize(), 100);

	    // ⚠️ Forzamos SIEMPRE a ordenar por "id" y NO usamos el sort de la request
        // [Doc] Variable local normalizado (Pageable): normalizado. Se inicializa con PageRequest.of(page, size, Sort.by(Sort.Order.asc("id"))).
	    Pageable normalizado = PageRequest.of(page, size, Sort.by(Sort.Order.asc("id")));

        // [Doc] Variable local usuarios (Page<Usuario>): usuarios. Se inicializa con usuarioService.listar(q, normalizado).
	    Page<Usuario> usuarios = usuarioService.listar(q, normalizado);

	    // (opcional para debug rápido)
        // [Doc] Ejecuta la llamada o modificación System.out.printf("page=%d size=%d elemsPage=%d totalElems=%d%n", usuarios.getNumber(), usuarios.getSize(), usuarios.getNumberOfElements(), usuarios.getTotalElements()); Sus efectos dependen del método invocado y del objeto receptor.
	    System.out.printf("page=%d size=%d elemsPage=%d totalElems=%d%n",
	            usuarios.getNumber(), usuarios.getSize(),
	            usuarios.getNumberOfElements(), usuarios.getTotalElements());

        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("usuarios", usuarios);
	    model.addAttribute("usuarios", usuarios);
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("q", q);
	    model.addAttribute("q", q);
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("seccion", "gestion");
	    model.addAttribute("seccion", "gestion");
	    
       // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("localidades", localidadService.getTodas());
	   model.addAttribute("localidades", localidadService.getTodas()); // o getActivas()
       // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("estad", estadisticasService.getDashboardResumen());
	   model.addAttribute("estad", estadisticasService.getDashboardResumen());

        // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
	    return "paneladministracion";
	}
	
	
	
	 
	
	
	//ver historial (Redireccion a detalles )
	
	//@GetMapping("/{id}/historial")
	//public String historial (@PathVariable Long id, Model model) {
	//	model.addAttribute("Id_usuario", id);
	//	return "redirect:/admin/usuarios/"+id+"/historial/detalle";
	//}
	
	
	
	 
	 // EDITAR (guardar)
	
      /** [Doc]
       * Actualiza guardar en el ámbito de AdminUsuarioController.
       * @param id identificador del registro.
       * @param datos datos.
       * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
       */
	  @PostMapping("/{id}/editar")
	  public String editarGuardar(@PathVariable Long id,@ModelAttribute("usuario") Usuario datos) {
      // [Doc] Ejecuta la llamada o modificación usuarioService.editUsuario(id, datos); Sus efectos dependen del método invocado y del objeto receptor.
	  usuarioService.editUsuario(id, datos); 
      // [Doc] Termina la acción y solicita al navegador una redirección a la ruta indicada.
	  return "redirect:/admin/usuarios"; }
	  
	 // ELIMINAR
	 
     /** [Doc]
      * Elimina los datos de la operación en el ámbito de AdminUsuarioController.
      * @param id identificador del registro.
      * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
      */
	 @PostMapping("/{id}/eliminar") 
	 public String eliminar(@PathVariable Long id)
	 { usuarioService.deleteUsuario(id); return "redirect:/admin/usuarios"; }
	 
	
	 //alta usuario
     /** [Doc]
      * Recibe los datos de una cuenta creada desde administración y delega su validación y guardado.
      * @param datos datos.
      * @param br br.
      * @param localidadId identificador de localidad elegido en el formulario.
      * @param model atributos que recibe la plantilla Thymeleaf.
      * @param ra ra.
      * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
      */
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
         // [Doc] Variable local telefono (String): telefono. Se inicializa con datos.getTelefono().
	     if (datos.getRol() == null && !br.hasFieldErrors("rol")) datos.setRol(Usuario.Rol.USUARIO);
         String clave = datos.getContraseña();
         if (clave == null || clave.isBlank() || clave.length() < 8 || clave.length() > 128) {
             br.rejectValue("contraseña", "format", "La contraseña debe tener entre 8 y 128 caracteres.");
         }
         String telefono = datos.getTelefono();

         // [Doc] Evalúa telefono != null && !telefono.isBlank(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
	     if (telefono != null && !telefono.isBlank()) {

	         // Permitimos números, espacios, guiones y prefijo +
             // [Doc] Variable local telefonoLimpio (String): telefono limpio. Se inicializa con telefono.trim().
	         String telefonoLimpio = telefono.trim();

             // [Doc] Evalúa !telefonoLimpio.matches("[0-9+\\-\\s]{8,20}"). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
	         if (!telefonoLimpio.matches("[0-9+\\-\\s]{8,20}")) {
                 // [Doc] Ejecuta la llamada o modificación br.rejectValue( "telefono", "format", "Ingresá un teléfono válido." ); Sus efectos dependen del método invocado y del objeto receptor.
	             br.rejectValue(
	                 "telefono",
	                 "format",
	                 "Ingresá un teléfono válido."
	             );
	         } else {
                 // [Doc] Actualiza una propiedad o configuración mediante la llamada: datos.setTelefono(telefonoLimpio);
	             datos.setTelefono(telefonoLimpio);
	         }

	     } else {
             // [Doc] Actualiza una propiedad o configuración mediante la llamada: datos.setTelefono(null);
	         datos.setTelefono(null);
	     }

	     // ======================================================
	     // SI HAY ERRORES: VOLVER A MOSTRAR EL MODAL
	     // ======================================================
         // [Doc] Evalúa br.hasErrors(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
	     if (br.hasErrors()) {

             // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute( "localidades", localidadService.getTodas() );
	         model.addAttribute(
	             "localidades",
	             localidadService.getTodas()
	         );

             // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute( "seccion", "gestion" );
	         model.addAttribute(
	             "seccion",
	             "gestion"
	         );

             // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute( "showNewUserModal", true );
	         model.addAttribute(
	             "showNewUserModal",
	             true
	         );

             // [Doc] Variable local usuarios (Page<Usuario>): usuarios. Se inicializa con usuarioService.listar( "", PageRequest.of( 0, 10, Sort.by("id").ascending() ) ).
	         Page<Usuario> usuarios =
	             usuarioService.listar(
	                 "",
	                 PageRequest.of(
	                     0,
	                     10,
	                     Sort.by("id").ascending()
	                 )
	             );

             // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("usuarios", usuarios);
	         model.addAttribute("usuarios", usuarios);
             // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("q", "");
	         model.addAttribute("q", "");
             // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute( "estad", estadisticasService.getDashboardResumen() );
	         model.addAttribute(
	             "estad",
	             estadisticasService.getDashboardResumen()
	         );

             // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
	         return "paneladministracion";
	     }

	     // ======================================================
	     // VALORES INICIALES DEL USUARIO
	     // ======================================================
         // [Doc] Actualiza una propiedad o configuración mediante la llamada: datos.setFecha_registro(new Date());
	     datos.setFecha_registro(new Date());
         // [Doc] Actualiza una propiedad o configuración mediante la llamada: datos.setUltima_conexion(null);
	     datos.setUltima_conexion(null);
         // [Doc] Actualiza una propiedad o configuración mediante la llamada: datos.setPuntos(0);
	     datos.setPuntos(0);

         // [Doc] Variable local localidad (Localidad): localidad asociada a la cuenta. Se inicializa con localidadService.findLocalidad(localidadId).
	     Localidad localidad =
	         localidadService.findLocalidad(localidadId);

         // [Doc] Actualiza una propiedad o configuración mediante la llamada: datos.setLocalidad(localidad);
	     datos.setLocalidad(localidad);

         // [Doc] Ejecuta la llamada o modificación usuarioService.saveUsuario(datos); Sus efectos dependen del método invocado y del objeto receptor.
	     usuarioService.saveUsuario(datos);

         // [Doc] Ejecuta la llamada o modificación ra.addFlashAttribute( "ok", "Usuario creado correctamente." ); Sus efectos dependen del método invocado y del objeto receptor.
	     ra.addFlashAttribute(
	         "ok",
	         "Usuario creado correctamente."
	     );

         // [Doc] Termina la acción y solicita al navegador una redirección a la ruta indicada.
	     return "redirect:/admin/usuarios";
	 }
	 
	 
     /** [Doc]
      * Devuelve los resúmenes de compras del usuario seleccionado para la consulta administrativa.
      * @param idUsuario identificador del usuario.
      * @return List&lt;compraResumenDTO&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
      */
	 @GetMapping("/{idUsuario}/compras")
	    @ResponseBody
	    public List<compraResumenDTO> historialCompras(@PathVariable Long idUsuario) {
            // [Doc] Termina el método o la función y devuelve compraService.obtenerComprasPorUsuario(idUsuario).
	        return compraService.obtenerComprasPorUsuario(idUsuario);
	    }

        /** [Doc]
         * Devuelve los productos, cantidades y precios del pedido seleccionado en el historial.
         * @param idCompra id compra.
         * @return List&lt;DetalleProductoDTO&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
	    @GetMapping("/compras/{idCompra}/detalle")
	    @ResponseBody
	    public List<DetalleProductoDTO> detalleCompra(@PathVariable Long idCompra) {
            // [Doc] Termina el método o la función y devuelve compraService.obtenerDetalleCompra(idCompra).
	        return compraService.obtenerDetalleCompra(idCompra);
	    }
	    
	    
	 
	 }
	 


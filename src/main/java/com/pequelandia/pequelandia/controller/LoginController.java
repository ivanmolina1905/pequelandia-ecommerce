package com.pequelandia.pequelandia.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pequelandia.pequelandia.dto.RegistroUsuarioDTO;
import com.pequelandia.pequelandia.model.Carrito_de_compra;
import com.pequelandia.pequelandia.model.Localidad;
import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.service.CorreoService;
import com.pequelandia.pequelandia.service.ICarrito_de_compraService;
import com.pequelandia.pequelandia.service.ILocalidadService;
import com.pequelandia.pequelandia.service.INotificacionService;
import com.pequelandia.pequelandia.service.IUsuarioService;

import jakarta.servlet.http.HttpSession;
import org.springframework.validation.BindingResult;
/** [Doc]
 * Presenta registro y login, valida formularios, renueva la sesión al ingresar y coordina la fusión del carrito.
 */
@Controller
@RequestMapping("/auth")
public class LoginController {

/** Confirma y renueva una sesión existente sin autenticar sesiones vencidas. */
@GetMapping("/session")
@ResponseBody
public org.springframework.http.ResponseEntity<Void> session(jakarta.servlet.http.HttpServletRequest request) {
    var session = request.getSession(false);
    if (session == null || session.getAttribute("usuario") == null) {
        return org.springframework.http.ResponseEntity.status(401)
                .cacheControl(org.springframework.http.CacheControl.noStore()).build();
    }
    return org.springframework.http.ResponseEntity.noContent()
            .cacheControl(org.springframework.http.CacheControl.noStore())
            .header("X-Session-Expires", Long.toString(System.currentTimeMillis()
                    + session.getMaxInactiveInterval() * 1000L)).build();
}

// [Doc] Atributo usuarioService (IUsuarioService): colaborador que concentra operaciones de usuario.
@Autowired
private IUsuarioService usuarioService;
// [Doc] Atributo identidad (com.pequelandia.pequelandia.service.IdentidadService): colaborador que concentra operaciones de com.pequelandia.pequelandia.service.identidad.
@Autowired private com.pequelandia.pequelandia.service.IdentidadService identidad;
// [Doc] Atributo carritoService (ICarrito_de_compraService): colaborador que concentra operaciones de carrito de compra.
@Autowired 
private ICarrito_de_compraService carritoService;



//1) Mostrar formulario de login (GET)
/** [Doc]
 * Presenta la plantilla del formulario de inicio de sesión.
 * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
 */
@GetMapping("/login")
public String loginForm() {
    // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
    return "login"; // templates/login.html
}

/** [Doc]
 * Valida credenciales, cambia el ID de sesión, conserva la identidad y fusiona el carrito persistente invitado
 * si existe; redirige según permisos.
 * @param username username.
 * @param password contraseña recibida para la operación de cuenta.
 * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
 * @param request petición HTTP entrante.
 * @param model atributos que recibe la plantilla Thymeleaf.
 * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
 */
@PostMapping("/login")
public String procesarLogin(@RequestParam String username,
                            @RequestParam String password,
                            HttpSession session,
                            jakarta.servlet.http.HttpServletRequest request,
                            Model model) {

    // [Doc] Variable local optUsuario (Optional<Usuario>): opt usuario. Se inicializa con usuarioService.login(username, password).
    Optional<Usuario> optUsuario = usuarioService.login(username, password);
    // [Doc] Evalúa optUsuario.isEmpty(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
    if (optUsuario.isEmpty()) {
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("error", "Email o contraseña incorrectos");
        model.addAttribute("error", "Email o contraseña incorrectos");
        // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
        return "login";
    }

    // [Doc] Variable local usuario (Usuario): cuenta de usuario utilizada por la operación. Se inicializa con optUsuario.get().
    Usuario usuario = optUsuario.get();

    // carrito invitado (si existía)
    // [Doc] Variable local carritoInvitadoId (Long): carrito invitado id. Se inicializa con (Long) session.getAttribute("CARRITO_ID").
    Long carritoInvitadoId = (Long) session.getAttribute("CARRITO_ID");

    // [Doc] Ejecuta la llamada o modificación request.changeSessionId(); Sus efectos dependen del método invocado y del objeto receptor.
    request.changeSessionId();
    // guardar usuario logueado
    // [Doc] Almacena un atributo en la sesión o petición: session.setAttribute("usuario", usuario);
    session.setAttribute("usuario", usuario);

    // [Doc] Evalúa carritoInvitadoId != null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
    if (carritoInvitadoId != null) {
        // [Doc] Variable local carritoUsuario (Carrito_de_compra): carrito persistente procesado. Se inicializa con carritoService.getOrCreateCarrito(session).
        Carrito_de_compra carritoUsuario =
            carritoService.getOrCreateCarrito(session);

        // [Doc] Ejecuta la llamada o modificación carritoService.mergeCarritos( carritoInvitadoId, carritoUsuario.getId_carrito() ); Sus efectos dependen del método invocado y del objeto receptor.
        carritoService.mergeCarritos(
            carritoInvitadoId,
            carritoUsuario.getId_carrito()
        );

        // [Doc] Almacena un atributo en la sesión o petición: session.setAttribute("CARRITO_ID", carritoUsuario.getId_carrito());
        session.setAttribute("CARRITO_ID", carritoUsuario.getId_carrito());
    }

    // [Doc] Evalúa identidad.esAdmin(usuario). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
    if (identidad.esAdmin(usuario)) {
        // [Doc] Termina la acción y solicita al navegador una redirección a la ruta indicada.
        return "redirect:/panel/admin";
    }
    // [Doc] Termina la acción y solicita al navegador una redirección a la ruta indicada.
    return "redirect:/panelusuarios/panelhistorial?section=perfil";
}


/** [Doc]
 * Invalida la sesión HTTP y redirige al formulario de login.
 * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
 * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
 */
@GetMapping("/logout")
public String logout(HttpSession session) {
    // [Doc] Ejecuta la llamada o modificación session.invalidate(); Sus efectos dependen del método invocado y del objeto receptor.
    session.invalidate();
    // [Doc] Termina la acción y solicita al navegador una redirección a la ruta indicada.
    return "redirect:/auth/login";
}


// [Doc] Atributo localidadService (ILocalidadService): colaborador que concentra operaciones de localidad.
@Autowired
private ILocalidadService localidadService;

//======================================================
//MOSTRAR FORMULARIO DE REGISTRO
//======================================================

/** [Doc]
 * Prepara un DTO vacío y el listado de localidades para presentar el formulario de registro.
 * @param model atributos que recibe la plantilla Thymeleaf.
 * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
 */
@GetMapping("/registro")
public String mostrarRegistro(Model model) {

 // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("registro", new RegistroUsuarioDTO());
 model.addAttribute("registro", new RegistroUsuarioDTO());
 // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("localidades", localidadService.getTodas());
 model.addAttribute("localidades", localidadService.getTodas());

 // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
 return "registro";
}


//======================================================
//PROCESAR REGISTRO
//======================================================

/** [Doc]
 * Valida campos, DNI, correo básico, contraseña, confirmación, términos y duplicados; delega el alta
 * transaccional o vuelve al formulario.
 * @param dto objeto de transferencia con los datos de la operación.
 * @param bindingResult errores de conversión y enlace del formulario.
 * @param model atributos que recibe la plantilla Thymeleaf.
 * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
 */
@PostMapping("/registro")
public String registrarUsuario(
     @ModelAttribute("registro") RegistroUsuarioDTO dto,BindingResult bindingResult,
     Model model
) {

 // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("localidades", localidadService.getTodas());
 model.addAttribute("localidades", localidadService.getTodas());

 // [Doc] Evalúa bindingResult.hasErrors(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
 if (bindingResult.hasErrors()) {
     // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("error", "Revisá los datos ingresados.");
     model.addAttribute("error", "Revisá los datos ingresados.");
     // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
     return "registro";
 }

 
 // Campos obligatorios
 // [Doc] Evalúa dto.getNombre_usuario() == null || dto.getNombre_usuario().isBlank() || dto.getApellido() == null || dto.getApellido().isBlank() || dto.getEmail() == null || dto.getEmail().isBl.... Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
 if (dto.getNombre_usuario() == null || dto.getNombre_usuario().isBlank()
         || dto.getApellido() == null || dto.getApellido().isBlank()
         || dto.getEmail() == null || dto.getEmail().isBlank()
         || dto.getDireccion() == null || dto.getDireccion().isBlank()  
         || dto.getContraseña() == null || dto.getContraseña().isBlank()
         || dto.getLocalidadId() == null) {

     // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("error", "Completá todos los campos obligatorios.");
     model.addAttribute("error", "Completá todos los campos obligatorios.");
     // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
     return "registro";
 }

 // Validación DNI
 // [Doc] Evalúa dto.getDni() < 1_000_000 || dto.getDni() > 99_999_999. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
 if (dto.getDni() < 1_000_000 || dto.getDni() > 99_999_999) {
     // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("error", "Ingresá un DNI válido.");
     model.addAttribute("error", "Ingresá un DNI válido.");
     // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
     return "registro";
 }

 // Email básico
 // [Doc] Evalúa !dto.getEmail().contains("@"). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
 if (!dto.getEmail().contains("@")) {
     // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("error", "Ingresá un email válido.");
     model.addAttribute("error", "Ingresá un email válido.");
     // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
     return "registro";
 }

 // Contraseña mínima
 // [Doc] Evalúa dto.getContraseña().length() < 6. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
 if (dto.getContraseña().length() < 6) {
     // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
     model.addAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
     // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
     return "registro";
 }

 // Confirmación contraseña
 // [Doc] Evalúa !dto.getContraseña().equals(dto.getConfirmarContraseña()). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
 if (!dto.getContraseña().equals(dto.getConfirmarContraseña())) {
     // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("error", "Las contraseñas no coinciden.");
     model.addAttribute("error", "Las contraseñas no coinciden.");
     // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
     return "registro";
 }

 // Términos
 // [Doc] Evalúa !dto.isAceptarTerminos(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
 if (!dto.isAceptarTerminos()) {
     // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("error", "Debés aceptar los términos y condiciones.");
     model.addAttribute("error", "Debés aceptar los términos y condiciones.");
     // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
     return "registro";
 }

 // [Doc] Evalúa "admin".equalsIgnoreCase(dto.getNombre_usuario().trim()). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
 if ("admin".equalsIgnoreCase(dto.getNombre_usuario().trim())) {
     // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("error", "Ese nombre de usuario está reservado.");
     model.addAttribute("error", "Ese nombre de usuario está reservado.");
     // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
     return "registro";
 }

 // Evitar duplicados
 // [Doc] Evalúa usuarioService.existeEmail(dto.getEmail()). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
 if (usuarioService.existeEmail(dto.getEmail())) {
     // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("error", "Ese email ya está registrado.");
     model.addAttribute("error", "Ese email ya está registrado.");
     // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
     return "registro";
 }

 // [Doc] Evalúa usuarioService.existeDni(dto.getDni()). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
 if (usuarioService.existeDni(dto.getDni())) {
     // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("error", "Ese DNI ya está registrado.");
     model.addAttribute("error", "Ese DNI ya está registrado.");
     // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
     return "registro";
 }

 // [Doc] Ejecuta un bloque con tratamiento de excepciones; los catch asociados atienden los fallos declarados y finally se ejecuta al salir si existe.
 try {
     // [Doc] Variable local localidad (Localidad): localidad asociada a la cuenta. Se inicializa con localidadService.findLocalidad(dto.getLocalidadId()).
     Localidad localidad =
             localidadService.findLocalidad(dto.getLocalidadId());

     // [Doc] Variable local usuarioCreado (Usuario): cuenta de usuario utilizada por la operación. Se inicializa con usuarioService.registrar(dto, localidad).
     Usuario usuarioCreado =
    	        usuarioService.registrar(dto, localidad);

  // bienvenida al correo del usuario
     // La bienvenida se encola desde el servicio transaccional.

     // campanita y correo para admin
     // La notificación se crea desde el servicio transaccional.

     // [Doc] Termina la acción y solicita al navegador una redirección a la ruta indicada.
     return "redirect:/auth/login?registroExitoso=true";

 } catch (Exception e) {
     // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute( "error", "No se pudo crear la cuenta. Revisá los datos ingresados." );
     model.addAttribute(
             "error",
             "No se pudo crear la cuenta. Revisá los datos ingresados."
     );

     // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
     return "registro";
 }
}




}



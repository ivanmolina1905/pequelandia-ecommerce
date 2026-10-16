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
@Controller
@RequestMapping("/auth")
public class LoginController {

@Autowired
private IUsuarioService usuarioService;
@Autowired 
private ICarrito_de_compraService carritoService;
@Autowired
private CorreoService correoService;
@Autowired
private INotificacionService notificacionService;

//1) Mostrar formulario de login (GET)
@GetMapping("/login")
public String loginForm() {
    return "login"; // templates/login.html
}

@PostMapping("/login")
public String procesarLogin(@RequestParam String username,
                            @RequestParam String password,
                            HttpSession session,
                            jakarta.servlet.http.HttpServletRequest request,
                            Model model) {

    Optional<Usuario> optUsuario = usuarioService.login(username, password);
    if (optUsuario.isEmpty()) {
        model.addAttribute("error", "Usuario o contraseña incorrectos");
        return "login";
    }

    Usuario usuario = optUsuario.get();

    // carrito invitado (si existía)
    Long carritoInvitadoId = (Long) session.getAttribute("CARRITO_ID");

    request.changeSessionId();
    // guardar usuario logueado
    session.setAttribute("usuario", usuario);

    if (carritoInvitadoId != null) {
        Carrito_de_compra carritoUsuario =
            carritoService.getOrCreateCarrito(session);

        carritoService.mergeCarritos(
            carritoInvitadoId,
            carritoUsuario.getId_carrito()
        );

        session.setAttribute("CARRITO_ID", carritoUsuario.getId_carrito());
    }

    if ("admin".equalsIgnoreCase(usuario.getNombre_usuario())) {
        return "redirect:/panel/admin";
    }
    return "redirect:/panelusuarios/panelhistorial?section=perfil";
}


@GetMapping("/logout")
public String logout(HttpSession session) {
    session.invalidate();
    return "redirect:/auth/login";
}


@Autowired
private ILocalidadService localidadService;

//======================================================
//MOSTRAR FORMULARIO DE REGISTRO
//======================================================

@GetMapping("/registro")
public String mostrarRegistro(Model model) {

 model.addAttribute("registro", new RegistroUsuarioDTO());
 model.addAttribute("localidades", localidadService.getTodas());

 return "registro";
}


//======================================================
//PROCESAR REGISTRO
//======================================================

@PostMapping("/registro")
public String registrarUsuario(
     @ModelAttribute("registro") RegistroUsuarioDTO dto,BindingResult bindingResult,
     Model model
) {

 model.addAttribute("localidades", localidadService.getTodas());

 if (bindingResult.hasErrors()) {
     model.addAttribute("error", "Revisá los datos ingresados.");
     return "registro";
 }

 
 // Campos obligatorios
 if (dto.getNombre_usuario() == null || dto.getNombre_usuario().isBlank()
         || dto.getApellido() == null || dto.getApellido().isBlank()
         || dto.getEmail() == null || dto.getEmail().isBlank()
         || dto.getDireccion() == null || dto.getDireccion().isBlank()  
         || dto.getContraseña() == null || dto.getContraseña().isBlank()
         || dto.getLocalidadId() == null) {

     model.addAttribute("error", "Completá todos los campos obligatorios.");
     return "registro";
 }

 // Validación DNI
 if (dto.getDni() < 1_000_000 || dto.getDni() > 99_999_999) {
     model.addAttribute("error", "Ingresá un DNI válido.");
     return "registro";
 }

 // Email básico
 if (!dto.getEmail().contains("@")) {
     model.addAttribute("error", "Ingresá un email válido.");
     return "registro";
 }

 // Contraseña mínima
 if (dto.getContraseña().length() < 6) {
     model.addAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
     return "registro";
 }

 // Confirmación contraseña
 if (!dto.getContraseña().equals(dto.getConfirmarContraseña())) {
     model.addAttribute("error", "Las contraseñas no coinciden.");
     return "registro";
 }

 // Términos
 if (!dto.isAceptarTerminos()) {
     model.addAttribute("error", "Debés aceptar los términos y condiciones.");
     return "registro";
 }

 if ("admin".equalsIgnoreCase(dto.getNombre_usuario().trim())) {
     model.addAttribute("error", "Ese nombre de usuario está reservado.");
     return "registro";
 }

 // Evitar duplicados
 if (usuarioService.existeNombreUsuario(dto.getNombre_usuario())) {
     model.addAttribute("error", "Ese nombre de usuario ya está registrado.");
     return "registro";
 }

 if (usuarioService.existeEmail(dto.getEmail())) {
     model.addAttribute("error", "Ese email ya está registrado.");
     return "registro";
 }

 if (usuarioService.existeDni(dto.getDni())) {
     model.addAttribute("error", "Ese DNI ya está registrado.");
     return "registro";
 }

 try {
     Localidad localidad =
             localidadService.findLocalidad(dto.getLocalidadId());

     Usuario usuarioCreado =
    	        usuarioService.registrar(dto, localidad);

  // bienvenida al correo del usuario
     correoService.enviarBienvenida(usuarioCreado);

     // campanita y correo para admin
     notificacionService.notificarNuevoUsuario(usuarioCreado);

     return "redirect:/auth/login?registroExitoso=true";

 } catch (Exception e) {
     model.addAttribute(
             "error",
             "No se pudo crear la cuenta. Revisá los datos ingresados."
     );

     return "registro";
 }
}




}



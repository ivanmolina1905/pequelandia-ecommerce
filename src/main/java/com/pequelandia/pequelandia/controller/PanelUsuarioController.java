package com.pequelandia.pequelandia.controller;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pequelandia.pequelandia.model.Pedido;
import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.repository.IPedidoRepository;
import com.pequelandia.pequelandia.repository.IUsuarioRepository;
import com.pequelandia.pequelandia.service.INotificacionService;
import com.pequelandia.pequelandia.service.IPedidoService;
import com.pequelandia.pequelandia.service.PanelUsuarioService;

import jakarta.servlet.http.HttpSession;


/** [Doc]
 * Construye el panel personal, presenta compras y permite editar perfil y solicitar cancelaciones.
 */
@Controller
@RequestMapping("/panelusuarios")
public class PanelUsuarioController {
	
// [Doc] Atributo pedidoRepo (IPedidoRepository): acceso de persistencia a pedido.
@Autowired	
private IPedidoRepository pedidoRepo;
// [Doc] Atributo pedidoService (IPedidoService): colaborador que concentra operaciones de pedido.
@Autowired private IPedidoService pedidoService;
// [Doc] Atributo panelusuarioService (PanelUsuarioService): colaborador que concentra operaciones de panel usuario.
@Autowired
private PanelUsuarioService panelusuarioService;
// [Doc] Atributo usuarioRepo (IUsuarioRepository): acceso de persistencia a usuario.
@Autowired
private IUsuarioRepository usuarioRepo;
// [Doc] Atributo identidad (com.pequelandia.pequelandia.service.IdentidadService): colaborador que concentra operaciones de com.pequelandia.pequelandia.service.identidad.
@Autowired private com.pequelandia.pequelandia.service.IdentidadService identidad;


/** [Doc]
 * Reúne perfil, pedidos y resumen del usuario para presentar su panel e historial personal.
 * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
 * @param model atributos que recibe la plantilla Thymeleaf.
 * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
 */
@GetMapping("/verpedidos")
public String panelUsuario(HttpSession session, Model model){
    // [Doc] Variable local u (Usuario): cuenta de usuario utilizada por la operación. Se inicializa con (Usuario) session.getAttribute("usuario").
    Usuario u = (Usuario) session.getAttribute("usuario");
    // [Doc] Evalúa u == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
    if(u == null) return "redirect:/auth/login";

    // [Doc] Variable local pedidos (List<Pedido>): pedidos o servicio de pedidos según el tipo declarado. Se inicializa con pedidoRepo.listarPorUsuarioOrdenados(u.getId()).
    List<Pedido> pedidos = pedidoRepo.listarPorUsuarioOrdenados(u.getId());

    // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("pedidos", pedidos);
    model.addAttribute("pedidos", pedidos);
    // [Doc] Termina la acción y solicita al navegador una redirección a la ruta indicada.
    return "redirect:/panelusuarios/panelhistorial";
}

/** [Doc]
 * Solicita cancelacion en el ámbito de PanelUsuarioController.
 * @param id identificador del registro.
 * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
 * @return Map&lt;String, Object&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
 */
@PostMapping("/{id}/cancelar")
@ResponseBody
public Map<String, Object> solicitarCancelacion(@PathVariable Long id, HttpSession session){
    // [Doc] Variable local u (Usuario): cuenta de usuario utilizada por la operación. Se inicializa con (Usuario) session.getAttribute("usuario").
    Usuario u = (Usuario) session.getAttribute("usuario");
    // [Doc] Evalúa u == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
    if(u == null) return Map.of("ok", false, "redirect", "/auth/login");

    // [Doc] Ejecuta la llamada o modificación pedidoService.solicitarCancelacion(id, u.getId()); Sus efectos dependen del método invocado y del objeto receptor.
    pedidoService.solicitarCancelacion(id, u.getId());

    // [Doc] Termina el método o la función y devuelve Map.of("ok", true).
    return Map.of("ok", true);
}

//panel usuario
/** [Doc]
 * Reúne perfil, pedidos y resumen del usuario para presentar su panel e historial personal.
 * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
 * @param page índice de página solicitado, comenzando en cero.
 * @param size size.
 * @param section section.
 * @param model atributos que recibe la plantilla Thymeleaf.
 * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
 */
@GetMapping("/panelhistorial")
public String panelUsuario(
    jakarta.servlet.http.HttpSession session,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "perfil") String section,
    Model model
) {
  // [Doc] Variable local u (Usuario): cuenta de usuario utilizada por la operación. Se inicializa con (Usuario) session.getAttribute("usuario").
  Usuario u = (Usuario) session.getAttribute("usuario");
  // [Doc] Evalúa u == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
  if (u == null) return "redirect:/auth/login";

  //usuario a modificar
  // [Doc] Variable local um (Usuario): um. Se inicializa con usuarioRepo.findById(u.getId()) .orElse(null).
  Usuario um = usuarioRepo.findById(u.getId())
          .orElse(null);

  // [Doc] Evalúa um == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
  if (um == null) {
      // [Doc] Ejecuta la llamada o modificación session.invalidate(); Sus efectos dependen del método invocado y del objeto receptor.
      session.invalidate();
      // [Doc] Termina la acción y solicita al navegador una redirección a la ruta indicada.
      return "redirect:/auth/login";
  }
  
  // [Doc] Variable local pedidos (Page<Pedido>): pedidos o servicio de pedidos según el tipo declarado. Se inicializa con pedidoRepo.findByUsuarioId( u.getId(), PageRequest.of(Math.max(0, page), Math.max(1, Math.min(100, size)), ....
  Page<Pedido> pedidos = pedidoRepo.findByUsuarioId(
      u.getId(),
      PageRequest.of(Math.max(0, page), Math.max(1, Math.min(100, size)), Sort.by(Sort.Direction.DESC, "fecha_pedido"))
  );
  
  
  // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("u", u);
  model.addAttribute("u", u);
  // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("pedidosPage", pedidos);
  model.addAttribute("pedidosPage", pedidos);
  // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("section", section);
  model.addAttribute("section", section);

  // estadísticas reales
  // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("stats", panelusuarioService.build(u.getId()));
  model.addAttribute("stats", panelusuarioService.build(u.getId()));
  
  // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
  return "panelusuario";
}

/** [Doc]
 * Actualiza perfil en el ámbito de PanelUsuarioController.
 * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
 * @param nombre nombre normalizado o visible de la entidad.
 * @param email dirección de correo de la cuenta.
 * @param direccion direccion.
 * @param telefono telefono.
 * @return Map&lt;String, Object&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
 */
@PostMapping("/perfil/editar")
@ResponseBody
public Map<String, Object> editarPerfil(
        HttpSession session,
        @RequestParam String nombre,
        @RequestParam String email,
        @RequestParam(required = false) String direccion,
        @RequestParam(required = false) String telefono
) {
    // [Doc] Variable local sesionUsuario (Usuario): sesion usuario. Se inicializa con (Usuario) session.getAttribute("usuario").
    Usuario sesionUsuario = (Usuario) session.getAttribute("usuario");

    // [Doc] Evalúa sesionUsuario == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
    if (sesionUsuario == null) {
        // [Doc] Termina el método o la función y devuelve Map.of( "ok", false, "redirect", "/auth/login" ).
        return Map.of(
                "ok", false,
                "redirect", "/auth/login"
        );
    }

    // [Doc] Variable local u (Usuario): cuenta de usuario utilizada por la operación. Se inicializa con usuarioRepo.findById(sesionUsuario.getId()) .orElse(null).
    Usuario u = usuarioRepo.findById(sesionUsuario.getId())
            .orElse(null);

    // [Doc] Evalúa u == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
    if (u == null) {
        // [Doc] Ejecuta la llamada o modificación session.invalidate(); Sus efectos dependen del método invocado y del objeto receptor.
        session.invalidate();

        // [Doc] Termina el método o la función y devuelve Map.of( "ok", false, "redirect", "/auth/login" ).
        return Map.of(
                "ok", false,
                "redirect", "/auth/login"
        );
    }

    // ======================================================
    // VALIDAR NOMBRE
    // ======================================================
    // [Doc] Evalúa nombre == null || nombre.trim().isEmpty(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
    if (nombre == null || nombre.trim().isEmpty()) {
        // [Doc] Termina el método o la función y devuelve Map.of( "ok", false, "error", "El nombre no puede estar vacío" ).
        return Map.of(
                "ok", false,
                "error", "El nombre no puede estar vacío"
        );
    }

    // ======================================================
    // VALIDAR EMAIL
    // ======================================================
    // [Doc] Evalúa email == null || email.trim().isEmpty(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
    if (email == null || email.trim().isEmpty()) {
        // [Doc] Termina el método o la función y devuelve Map.of( "ok", false, "error", "El email no puede estar vacío" ).
        return Map.of(
                "ok", false,
                "error", "El email no puede estar vacío"
        );
    }

    // [Doc] Evalúa !email.contains("@"). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
    if (!email.contains("@")) {
        // [Doc] Termina el método o la función y devuelve Map.of( "ok", false, "error", "Ingresá un email válido" ).
        return Map.of(
                "ok", false,
                "error", "Ingresá un email válido"
        );
    }

    // ======================================================
    // VALIDAR TELÉFONO COMO TEXTO
    // ======================================================
    // [Doc] Variable local telefonoLimpio (String): telefono limpio. Se inicializa con "".
    String telefonoLimpio = "";

    // [Doc] Evalúa telefono != null && !telefono.trim().isEmpty(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
    if (telefono != null && !telefono.trim().isEmpty()) {
        // [Doc] Ejecuta la llamada o modificación telefonoLimpio = telefono.trim(); Sus efectos dependen del método invocado y del objeto receptor.
        telefonoLimpio = telefono.trim();

        /*
         * Permite números, espacios, guiones y prefijo +.
         * Ejemplos válidos:
         * 3424280512
         * +54 342 428-0512
         */
        // [Doc] Evalúa !telefonoLimpio.matches("[0-9+\\-\\s]{8,20}"). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (!telefonoLimpio.matches("[0-9+\\-\\s]{8,20}")) {
            // [Doc] Termina el método o la función y devuelve Map.of( "ok", false, "error", "Ingresá un teléfono válido" ).
            return Map.of(
                    "ok", false,
                    "error", "Ingresá un teléfono válido"
            );
        }
    }

    // ======================================================
    // ACTUALIZAR USUARIO
    // ======================================================
    // [Doc] Ejecuta la llamada o modificación identidad.validarNombre(u, nombre); Sus efectos dependen del método invocado y del objeto receptor.
    identidad.validarNombre(u, nombre);
    // [Doc] Evalúa usuarioRepo.emailEnUso(u.getId(), email.trim()). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
    if (usuarioRepo.emailEnUso(u.getId(), email.trim())) {
        // [Doc] Termina el método o la función y devuelve Map.of("ok", false, "error", "Ese email ya está registrado").
        return Map.of("ok", false, "error", "Ese email ya está registrado");
    }
    // [Doc] Actualiza una propiedad o configuración mediante la llamada: u.setNombre_usuario(nombre.trim());
    u.setNombre_usuario(nombre.trim());
    // [Doc] Actualiza una propiedad o configuración mediante la llamada: u.setEmail(email.trim().toLowerCase());
    u.setEmail(email.trim().toLowerCase());
    // [Doc] Actualiza una propiedad o configuración mediante la llamada: u.setDireccion(direccion != null ? direccion.trim() : "");
    u.setDireccion(direccion != null ? direccion.trim() : "");
    // [Doc] Actualiza una propiedad o configuración mediante la llamada: u.setTelefono(telefonoLimpio);
    u.setTelefono(telefonoLimpio);

    // [Doc] Variable local guardado (Usuario): guardado. Se inicializa con usuarioRepo.save(u).
    Usuario guardado = usuarioRepo.save(u);

    // Actualizar también el usuario guardado en sesión
    // [Doc] Almacena un atributo en la sesión o petición: session.setAttribute("usuario", guardado);
    session.setAttribute("usuario", guardado);

    // [Doc] Termina el método o la función y devuelve Map.of( "ok", true, "nombre", guardado.getNombre_usuario(), "email", guardado.getEmail(), "direccion", guardado.getDireccion() != null ? guardado.getDireccio....
    return Map.of(
            "ok", true,
            "nombre", guardado.getNombre_usuario(),
            "email", guardado.getEmail(),
            "direccion", guardado.getDireccion() != null
                    ? guardado.getDireccion()
                    : "",
            "telefono", guardado.getTelefono() != null
                    ? guardado.getTelefono()
                    : ""
    );
}
}






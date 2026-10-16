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


@Controller
@RequestMapping("/panelusuarios")
public class PanelUsuarioController {
	
@Autowired	
private IPedidoRepository pedidoRepo;
@Autowired private IPedidoService pedidoService;
@Autowired
private PanelUsuarioService panelusuarioService;
@Autowired
private IUsuarioRepository usuarioRepo;
@Autowired
private INotificacionService notificacionService;

@GetMapping("/verpedidos")
public String panelUsuario(HttpSession session, Model model){
    Usuario u = (Usuario) session.getAttribute("usuario");
    if(u == null) return "redirect:/auth/login";

    List<Pedido> pedidos = pedidoRepo.listarPorUsuarioOrdenados(u.getId());

    model.addAttribute("pedidos", pedidos);
    return "redirect:/panelusuarios/panelhistorial";
}

@PostMapping("/{id}/cancelar")
@ResponseBody
public Map<String, Object> solicitarCancelacion(@PathVariable Long id, HttpSession session){
    Usuario u = (Usuario) session.getAttribute("usuario");
    if(u == null) return Map.of("ok", false, "redirect", "/auth/login");

    Pedido p = pedidoService.findPedido(id);

    // seguridad: que el pedido sea del usuario
    if (
    	    p.getUsuario() == null ||
    	    !Objects.equals(p.getUsuario().getId(), u.getId())
    	) {
    	    return Map.of("ok", false, "error", "No autorizado");
    	}

    // reglas mínimas
    String estado = p.getEstado_pedido();
    if(estado != null && (estado.equalsIgnoreCase("ENTREGADO") || estado.equalsIgnoreCase("CANCELADO"))){
        return Map.of("ok", false, "error", "No se puede cancelar un pedido entregado/cancelado");
    }

    p.setEstado_pedido("CANCELACION_SOLICITADA");
    pedidoService.savePedido(p);
    
    notificacionService.notificarSolicitudCancelacion(p);

    // opcional: crear Notificacion para admin si ya tenés esa entidad
    // notificacionService.crearParaAdmin("Solicitud de cancelación pedido #" + p.getId_pedido(), ...);

    return Map.of("ok", true);
}

//panel usuario
@GetMapping("/panelhistorial")
public String panelUsuario(
    jakarta.servlet.http.HttpSession session,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "perfil") String section,
    Model model
) {
  Usuario u = (Usuario) session.getAttribute("usuario");
  if (u == null) return "redirect:/auth/login";

  //usuario a modificar
  Usuario um = usuarioRepo.findById(u.getId())
          .orElse(null);

  if (um == null) {
      session.invalidate();
      return "redirect:/auth/login";
  }
  
  Page<Pedido> pedidos = pedidoRepo.findByUsuarioId(
      u.getId(),
      PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fecha_pedido"))
  );
  
  
  model.addAttribute("u", u);
  model.addAttribute("pedidosPage", pedidos);
  model.addAttribute("section", section);

  // estadísticas reales
  model.addAttribute("stats", panelusuarioService.build(u.getId()));
  
  return "panelusuario";
}

@PostMapping("/perfil/editar")
@ResponseBody
public Map<String, Object> editarPerfil(
        HttpSession session,
        @RequestParam String nombre,
        @RequestParam String email,
        @RequestParam(required = false) String direccion,
        @RequestParam(required = false) String telefono
) {
    Usuario sesionUsuario = (Usuario) session.getAttribute("usuario");

    if (sesionUsuario == null) {
        return Map.of(
                "ok", false,
                "redirect", "/auth/login"
        );
    }

    Usuario u = usuarioRepo.findById(sesionUsuario.getId())
            .orElse(null);

    if (u == null) {
        session.invalidate();

        return Map.of(
                "ok", false,
                "redirect", "/auth/login"
        );
    }

    // ======================================================
    // VALIDAR NOMBRE
    // ======================================================
    if (nombre == null || nombre.trim().isEmpty()) {
        return Map.of(
                "ok", false,
                "error", "El nombre no puede estar vacío"
        );
    }

    // ======================================================
    // VALIDAR EMAIL
    // ======================================================
    if (email == null || email.trim().isEmpty()) {
        return Map.of(
                "ok", false,
                "error", "El email no puede estar vacío"
        );
    }

    if (!email.contains("@")) {
        return Map.of(
                "ok", false,
                "error", "Ingresá un email válido"
        );
    }

    // ======================================================
    // VALIDAR TELÉFONO COMO TEXTO
    // ======================================================
    String telefonoLimpio = "";

    if (telefono != null && !telefono.trim().isEmpty()) {
        telefonoLimpio = telefono.trim();

        /*
         * Permite números, espacios, guiones y prefijo +.
         * Ejemplos válidos:
         * 3424280512
         * +54 342 428-0512
         */
        if (!telefonoLimpio.matches("[0-9+\\-\\s]{8,20}")) {
            return Map.of(
                    "ok", false,
                    "error", "Ingresá un teléfono válido"
            );
        }
    }

    // ======================================================
    // ACTUALIZAR USUARIO
    // ======================================================
    if ("admin".equalsIgnoreCase(nombre.trim()) && !"admin".equalsIgnoreCase(u.getNombre_usuario())) {
        return Map.of("ok", false, "error", "Ese nombre de usuario está reservado.");
    }
    u.setNombre_usuario(nombre.trim());
    u.setEmail(email.trim().toLowerCase());
    u.setDireccion(direccion != null ? direccion.trim() : "");
    u.setTelefono(telefonoLimpio);

    Usuario guardado = usuarioRepo.save(u);

    // Actualizar también el usuario guardado en sesión
    session.setAttribute("usuario", guardado);

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






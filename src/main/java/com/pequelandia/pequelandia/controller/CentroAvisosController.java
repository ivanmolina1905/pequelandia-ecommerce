package com.pequelandia.pequelandia.controller;

import com.pequelandia.pequelandia.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Controller
public class CentroAvisosController {
    private final CentroAvisosService centro;
    private final IdentidadService identidad;
    private final INotificacionService notificaciones;
    private final boolean entregaHabilitada;
    public CentroAvisosController(CentroAvisosService centro, IdentidadService identidad, INotificacionService notificaciones,
            @org.springframework.beans.factory.annotation.Value("${app.mail.delivery-enabled:true}") boolean entregaHabilitada) {
        this.centro=centro; this.identidad=identidad; this.notificaciones=notificaciones;
        this.entregaHabilitada=entregaHabilitada;
    }
    private String destinatario(HttpSession session) {
        var usuario=identidad.exigirUsuario(session);
        return identidad.esAdmin(usuario) ? "ADMIN" : "USUARIO_"+usuario.getId();
    }
    @GetMapping("/notificaciones/historial")
    public String historial(HttpSession session, Model model,
            @RequestParam(defaultValue="TODAS") String estado, @RequestParam(defaultValue="0") int page) {
        String destino=destinatario(session);
        model.addAttribute("admin", destino.equals("ADMIN"));
        model.addAttribute("pagina", centro.notificaciones(destino, estado, page));
        model.addAttribute("noLeidas", notificaciones.contarNoLeidas(destino));
        model.addAttribute("estado", estado);
        return "historial-notificaciones";
    }
    @PostMapping("/notificaciones/historial/{id}/leer")
    public String leer(@PathVariable long id, HttpSession session,
            @RequestParam(defaultValue="TODAS") String estado, @RequestParam(defaultValue="0") int page,
            RedirectAttributes flash) {
        CentroAvisosService.filtro(estado, "TODAS", "LEIDAS", "NO_LEIDAS");
        try { notificaciones.marcarComoLeida(id, destinatario(session)); }
        catch (IllegalArgumentException ex) { throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificación no disponible"); }
        flash.addAttribute("estado", estado); flash.addAttribute("page", Math.max(0,page));
        return "redirect:/notificaciones/historial";
    }
    @PostMapping("/notificaciones/historial/leer-todas")
    public String leerTodas(HttpSession session, RedirectAttributes flash) {
        notificaciones.marcarTodasComoLeidas(destinatario(session));
        flash.addFlashAttribute("mensaje", "Tus notificaciones quedaron marcadas como leídas.");
        return "redirect:/notificaciones/historial";
    }
    @GetMapping("/admin/correos")
    public String correos(HttpSession session, Model model,
            @RequestParam(defaultValue="TODOS") String estado, @RequestParam(defaultValue="") String q,
            @RequestParam(defaultValue="0") int page) {
        identidad.exigirAdmin(session);
        if(q.length()>200) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La búsqueda es demasiado larga");
        q=q.trim();
        model.addAttribute("pagina", centro.correos(estado,q,page));
        model.addAttribute("estado", estado); model.addAttribute("q", q); model.addAttribute("admin", true);
        model.addAttribute("entregaHabilitada", entregaHabilitada);
        return "panel-correos";
    }
    @PostMapping("/admin/correos/{id}/reintentar")
    public String reintentar(@PathVariable long id, HttpSession session, RedirectAttributes flash,
            @RequestParam(defaultValue="TODOS") String estado, @RequestParam(defaultValue="") String q,
            @RequestParam(defaultValue="0") int page) {
        identidad.exigirAdmin(session);
        CentroAvisosService.filtro(estado,"TODOS","PENDIENTE","REINTENTO","ENVIADO");
        if(q.length()>200) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La búsqueda es demasiado larga");
        boolean cambio=centro.reintentar(id);
        flash.addAttribute("estado",estado); flash.addAttribute("q",q.trim()); flash.addAttribute("page",Math.max(0,page));
        flash.addFlashAttribute("mensaje", cambio ? "Reintento programado. Se procesará cuando la entrega de correos esté habilitada." : "No se modificó el correo: ya está enviado, pendiente de procesarse o no está disponible.");
        return "redirect:/admin/correos";
    }
}

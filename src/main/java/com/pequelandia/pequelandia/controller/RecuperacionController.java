package com.pequelandia.pequelandia.controller;

import com.pequelandia.pequelandia.service.RecuperacionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class RecuperacionController {
    private final RecuperacionService recuperacion;
    public RecuperacionController(RecuperacionService recuperacion) { this.recuperacion = recuperacion; }

    @ModelAttribute
    void privacidad(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("X-Robots-Tag", "noindex, nofollow");
    }

    @GetMapping("/recuperar")
    public String formulario() { return "recuperar"; }

    @PostMapping("/recuperar")
    public String solicitar(@RequestParam(defaultValue="") String email, HttpServletRequest request, Model model) {
        recuperacion.solicitar(email, request.getRemoteAddr());
        model.addAttribute("mensaje", "Si el correo está registrado, te enviaremos las instrucciones. Revisá también spam. Si ya lo pediste, esperá unos minutos antes de volver a intentarlo.");
        return "recuperar";
    }

    @GetMapping("/restablecer")
    public String nueva(@RequestParam(defaultValue="") String token, Model model) {
        model.addAttribute("valido", recuperacion.vigente(token));
        model.addAttribute("token", token);
        return "restablecer";
    }

    @PostMapping("/restablecer")
    public String guardar(@RequestParam(defaultValue="") String token,
            @RequestParam(defaultValue="") String password, @RequestParam(defaultValue="") String confirmacion,
            HttpServletRequest request, Model model) {
        if (recuperacion.restablecer(token, password, confirmacion, request.getRemoteAddr())) {
            var session = request.getSession(false);
            if (session != null) session.invalidate();
            return "redirect:/auth/login?contrasenaActualizada=true";
        }
        model.addAttribute("valido", recuperacion.vigente(token));
        model.addAttribute("token", token);
        model.addAttribute("error", "No se pudo cambiar la contraseña. Usá entre 8 y 128 caracteres, confirmá que coincidan y verificá que el enlace siga vigente. Si hiciste muchos intentos, esperá 15 minutos.");
        return "restablecer";
    }
}

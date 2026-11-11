package com.pequelandia.pequelandia.controller;

import com.pequelandia.pequelandia.service.RecuperacionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** [Doc]
 * Presenta los formularios de recuperación y restablecimiento y aplica respuestas que preservan la privacidad.
 */
@Controller
@RequestMapping("/auth")
public class RecuperacionController {
    // [Doc] Atributo recuperacion (RecuperacionService): colaborador que concentra operaciones de recuperacion. Su referencia no se reasigna después de inicializarse.
    private final RecuperacionService recuperacion;
    /** [Doc]
     * Construye RecuperacionController y conserva sus dependencias o valores iniciales.
     * @param recuperacion colaborador que concentra operaciones de recuperacion.
     */
    public RecuperacionController(RecuperacionService recuperacion) { this.recuperacion = recuperacion; }

    /** [Doc]
     * Añade cabeceras para impedir almacenamiento en caché, envío de referencia e indexación de los formularios de
     * recuperación.
     * @param response respuesta HTTP que se envía al navegador.
     */
    @ModelAttribute
    void privacidad(HttpServletResponse response) {
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: response.setHeader("Cache-Control", "no-store");
        response.setHeader("Cache-Control", "no-store");
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("Referrer-Policy", "no-referrer");
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: response.setHeader("X-Robots-Tag", "noindex, nofollow");
        response.setHeader("X-Robots-Tag", "noindex, nofollow");
    }

    /** [Doc]
     * Presenta el formulario para solicitar un enlace de recuperación.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping("/recuperar")
    public String formulario() { return "recuperar"; }

    /** [Doc]
     * Solicita los datos de la operación en el ámbito de RecuperacionController.
     * @param email dirección de correo de la cuenta.
     * @param request petición HTTP entrante.
     * @param model atributos que recibe la plantilla Thymeleaf.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @PostMapping("/recuperar")
    public String solicitar(@RequestParam(defaultValue="") String email, HttpServletRequest request, Model model) {
        // [Doc] Ejecuta la llamada o modificación recuperacion.solicitar(email, request.getRemoteAddr()); Sus efectos dependen del método invocado y del objeto receptor.
        recuperacion.solicitar(email, request.getRemoteAddr());
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("mensaje", "Si el correo está registrado, te enviaremos las instrucciones. Revisá también spam. Si ya lo pediste, esperá unos minutos antes de volve...
        model.addAttribute("mensaje", "Si el correo está registrado, te enviaremos las instrucciones. Revisá también spam. Si ya lo pediste, esperá unos minutos antes de volver a intentarlo.");
        // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
        return "recuperar";
    }

    /** [Doc]
     * Comprueba si el token sigue vigente y presenta el formulario de nueva contraseña con ese resultado.
     * @param token valor del token usado por esta operación.
     * @param model atributos que recibe la plantilla Thymeleaf.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping("/restablecer")
    public String nueva(@RequestParam(defaultValue="") String token, Model model) {
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("valido", recuperacion.vigente(token));
        model.addAttribute("valido", recuperacion.vigente(token));
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("token", token);
        model.addAttribute("token", token);
        // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
        return "restablecer";
    }

    /** [Doc]
     * Guarda los datos de la operación en el ámbito de RecuperacionController.
     * @param token valor del token usado por esta operación.
     * @param password contraseña recibida para la operación de cuenta.
     * @param confirmacion segunda copia de la contraseña para comprobar coincidencia.
     * @param request petición HTTP entrante.
     * @param model atributos que recibe la plantilla Thymeleaf.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @PostMapping("/restablecer")
    public String guardar(@RequestParam(defaultValue="") String token,
            @RequestParam(defaultValue="") String password, @RequestParam(defaultValue="") String confirmacion,
            HttpServletRequest request, Model model) {
        // [Doc] Evalúa recuperacion.restablecer(token, password, confirmacion, request.getRemoteAddr()). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (recuperacion.restablecer(token, password, confirmacion, request.getRemoteAddr())) {
            // [Doc] Variable local session (var; tipo inferido por Java): sesión HTTP que conserva identidad, carrito y token CSRF. Se inicializa con request.getSession(false).
            var session = request.getSession(false);
            // [Doc] Evalúa session != null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
            if (session != null) session.invalidate();
            // [Doc] Termina la acción y solicita al navegador una redirección a la ruta indicada.
            return "redirect:/auth/login?contrasenaActualizada=true";
        }
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("valido", recuperacion.vigente(token));
        model.addAttribute("valido", recuperacion.vigente(token));
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("token", token);
        model.addAttribute("token", token);
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("error", "No se pudo cambiar la contraseña. Usá entre 8 y 128 caracteres, confirmá que coincidan y verificá que el enlace siga vigente. Si hiciste m...
        model.addAttribute("error", "No se pudo cambiar la contraseña. Usá entre 8 y 128 caracteres, confirmá que coincidan y verificá que el enlace siga vigente. Si hiciste muchos intentos, esperá 15 minutos.");
        // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
        return "restablecer";
    }
}

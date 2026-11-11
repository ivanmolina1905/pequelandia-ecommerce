package com.pequelandia.pequelandia.controller;

import com.pequelandia.pequelandia.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/** [Doc]
 * Presenta el historial de avisos y el panel de correos y exige la identidad correspondiente a cada acción.
 */
@Controller
public class CentroAvisosController {
    // [Doc] Atributo estadisticas (IEstadisticasService): colaborador que concentra operaciones de estadisticas.
    @org.springframework.beans.factory.annotation.Autowired
    private IEstadisticasService estadisticas;
    @org.springframework.beans.factory.annotation.Autowired
    private PanelUsuarioService panelUsuario;
    @org.springframework.beans.factory.annotation.Autowired
    private com.pequelandia.pequelandia.repository.IPedidoRepository pedidos;
    // [Doc] Atributo centro (CentroAvisosService): colaborador que concentra operaciones de centro avisos. Su referencia no se reasigna después de inicializarse.
    private final CentroAvisosService centro;
    // [Doc] Atributo identidad (IdentidadService): colaborador que concentra operaciones de identidad. Su referencia no se reasigna después de inicializarse.
    private final IdentidadService identidad;
    // [Doc] Atributo notificaciones (INotificacionService): colaborador que concentra operaciones de notificacion. Su referencia no se reasigna después de inicializarse.
    private final INotificacionService notificaciones;
    // [Doc] Atributo entregaHabilitada (boolean): indicador configurado que habilita la entrega de correos. Su referencia no se reasigna después de inicializarse.
    private final boolean entregaHabilitada;
    /** [Doc]
     * Construye CentroAvisosController y conserva sus dependencias o valores iniciales.
     * @param centro colaborador que concentra operaciones de centro avisos.
     * @param identidad colaborador que concentra operaciones de identidad.
     * @param notificaciones colaborador que concentra operaciones de notificacion.
     * @param entregaHabilitada indicador configurado que habilita la entrega de correos.
     */
    public CentroAvisosController(CentroAvisosService centro, IdentidadService identidad, INotificacionService notificaciones,
            @org.springframework.beans.factory.annotation.Value("${app.mail.delivery-enabled:true}") boolean entregaHabilitada) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.centro=centro;
        this.centro=centro; this.identidad=identidad; this.notificaciones=notificaciones;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.entregaHabilitada=entregaHabilitada;
        this.entregaHabilitada=entregaHabilitada;
    }
    /** [Doc]
     * Exige una cuenta vigente y obtiene ADMIN o USUARIO_ seguido de su ID según sus permisos.
     * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private String destinatario(HttpSession session) {
        // [Doc] Variable local usuario (var; tipo inferido por Java): cuenta de usuario utilizada por la operación. Se inicializa con identidad.exigirUsuario(session).
        var usuario=identidad.exigirUsuario(session);
        // [Doc] Termina el método o la función y devuelve identidad.esAdmin(usuario) ? "ADMIN" : "USUARIO_"+usuario.getId().
        return identidad.esAdmin(usuario) ? "ADMIN" : "USUARIO_"+usuario.getId();
    }
    /** [Doc]
     * Consulta la página de avisos del destinatario, su contador y filtro; presenta el panel administrativo o el
     * historial del cliente.
     * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
     * @param model atributos que recibe la plantilla Thymeleaf.
     * @param estado estado usado por la operación.
     * @param page índice de página solicitado, comenzando en cero.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping("/notificaciones/historial")
    public String historial(HttpSession session, Model model,
            @RequestParam(defaultValue="TODAS") String estado, @RequestParam(defaultValue="0") int page) {
        // [Doc] Variable local destino (String): destinatario o ruta de destino de esta operación. Se inicializa con destinatario(session).
        String destino=destinatario(session);
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("admin", destino.equals("ADMIN"));
        model.addAttribute("admin", destino.equals("ADMIN"));
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("pagina", centro.notificaciones(destino, estado, page));
        model.addAttribute("pagina", centro.notificaciones(destino, estado, page));
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("noLeidas", notificaciones.contarNoLeidas(destino));
        model.addAttribute("noLeidas", notificaciones.contarNoLeidas(destino));
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("estado", estado);
        model.addAttribute("estado", estado);
        // [Doc] Evalúa destino.equals("ADMIN"). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (destino.equals("ADMIN")) return panelAdmin(model, "notificaciones");
        // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
        var usuario = identidad.exigirUsuario(session);
        model.addAttribute("u", usuario);
        model.addAttribute("section", "notificaciones");
        model.addAttribute("stats", panelUsuario.build(usuario.getId()));
        model.addAttribute("pedidosPage", pedidos.findByUsuarioId(usuario.getId(),
                org.springframework.data.domain.PageRequest.of(0, 10,
                        org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "fecha_pedido"))));
        return "panelusuario";
    }
    /** [Doc]
     * Valida el filtro, marca el aviso del destinatario como leído y vuelve al historial conservando filtro y
     * página.
     * @param id identificador del registro.
     * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
     * @param estado estado usado por la operación.
     * @param page índice de página solicitado, comenzando en cero.
     * @param flash atributos conservados durante una redirección.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @PostMapping("/notificaciones/historial/{id}/leer")
    public String leer(@PathVariable long id, HttpSession session,
            @RequestParam(defaultValue="TODAS") String estado, @RequestParam(defaultValue="0") int page,
            RedirectAttributes flash) {
        // [Doc] Ejecuta la llamada o modificación CentroAvisosService.filtro(estado, "TODAS", "LEIDAS", "NO_LEIDAS"); Sus efectos dependen del método invocado y del objeto receptor.
        CentroAvisosService.filtro(estado, "TODAS", "LEIDAS", "NO_LEIDAS");
        // [Doc] Ejecuta un bloque con tratamiento de excepciones; los catch asociados atienden los fallos declarados y finally se ejecuta al salir si existe.
        try { notificaciones.marcarComoLeida(id, destinatario(session)); }
        catch (IllegalArgumentException ex) { throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificación no disponible"); }
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: flash.addAttribute("estado", estado);
        flash.addAttribute("estado", estado); flash.addAttribute("page", Math.max(0,page));
        // [Doc] Termina la acción y solicita al navegador una redirección a la ruta indicada.
        return "redirect:/notificaciones/historial";
    }
    /** [Doc]
     * Marca los avisos del destinatario como leídos y vuelve al historial con sus filtros.
     * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
     * @param flash atributos conservados durante una redirección.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @PostMapping("/notificaciones/historial/leer-todas")
    public String leerTodas(HttpSession session, RedirectAttributes flash) {
        // [Doc] Ejecuta la llamada o modificación notificaciones.marcarTodasComoLeidas(destinatario(session)); Sus efectos dependen del método invocado y del objeto receptor.
        notificaciones.marcarTodasComoLeidas(destinatario(session));
        // [Doc] Ejecuta la llamada o modificación flash.addFlashAttribute("mensaje", "Tus notificaciones quedaron marcadas como leídas."); Sus efectos dependen del método invocado y del objeto receptor.
        flash.addFlashAttribute("mensaje", "Tus notificaciones quedaron marcadas como leídas.");
        // [Doc] Termina la acción y solicita al navegador una redirección a la ruta indicada.
        return "redirect:/notificaciones/historial";
    }
    /** [Doc]
     * Exige administración, limita y normaliza la búsqueda y presenta la página de correos filtrados dentro del
     * panel.
     * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
     * @param model atributos que recibe la plantilla Thymeleaf.
     * @param estado estado usado por la operación.
     * @param q q.
     * @param page índice de página solicitado, comenzando en cero.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping("/admin/correos")
    public String correos(HttpSession session, Model model,
            @RequestParam(defaultValue="TODOS") String estado, @RequestParam(defaultValue="") String q,
            @RequestParam(defaultValue="0") int page) {
        // [Doc] Ejecuta la llamada o modificación identidad.exigirAdmin(session); Sus efectos dependen del método invocado y del objeto receptor.
        identidad.exigirAdmin(session);
        // [Doc] Evalúa q.length()>200. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if(q.length()>200) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La búsqueda es demasiado larga");
        // [Doc] Ejecuta la llamada o modificación q=q.trim(); Sus efectos dependen del método invocado y del objeto receptor.
        q=q.trim();
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("pagina", centro.correos(estado,q,page));
        model.addAttribute("pagina", centro.correos(estado,q,page));
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("estado", estado);
        model.addAttribute("estado", estado); model.addAttribute("q", q); model.addAttribute("admin", true);
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("entregaHabilitada", entregaHabilitada);
        model.addAttribute("entregaHabilitada", entregaHabilitada);
        // [Doc] Termina el método o la función y devuelve panelAdmin(model, "correos").
        return panelAdmin(model, "correos");
    }
    /** [Doc]
     * Añade la sección activa y el resumen estadístico al modelo y renderiza paneladministracion.
     * @param model atributos que recibe la plantilla Thymeleaf.
     * @param seccion seccion.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private String panelAdmin(Model model, String seccion) {
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("seccion", seccion);
        model.addAttribute("seccion", seccion);
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: model.addAttribute("estad", estadisticas.getDashboardResumen());
        model.addAttribute("estad", estadisticas.getDashboardResumen());
        // [Doc] Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.
        return "paneladministracion";
    }
    /** [Doc]
     * Exige administración y solicita adelantar la próxima entrega de un correo pendiente que admite reintento.
     * @param id identificador del registro.
     * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
     * @param flash atributos conservados durante una redirección.
     * @param estado estado usado por la operación.
     * @param q q.
     * @param page índice de página solicitado, comenzando en cero.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @PostMapping("/admin/correos/{id}/reintentar")
    public String reintentar(@PathVariable long id, HttpSession session, RedirectAttributes flash,
            @RequestParam(defaultValue="TODOS") String estado, @RequestParam(defaultValue="") String q,
            @RequestParam(defaultValue="0") int page) {
        // [Doc] Ejecuta la llamada o modificación identidad.exigirAdmin(session); Sus efectos dependen del método invocado y del objeto receptor.
        identidad.exigirAdmin(session);
        // [Doc] Ejecuta la llamada o modificación CentroAvisosService.filtro(estado,"TODOS","PENDIENTE","REINTENTO","ENVIADO"); Sus efectos dependen del método invocado y del objeto receptor.
        CentroAvisosService.filtro(estado,"TODOS","PENDIENTE","REINTENTO","ENVIADO");
        // [Doc] Evalúa q.length()>200. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if(q.length()>200) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La búsqueda es demasiado larga");
        // [Doc] Variable local cambio (boolean): cambio. Se inicializa con centro.reintentar(id).
        boolean cambio=centro.reintentar(id);
        // [Doc] Agrega un dato al modelo que recibirá la plantilla: flash.addAttribute("estado",estado);
        flash.addAttribute("estado",estado); flash.addAttribute("q",q.trim()); flash.addAttribute("page",Math.max(0,page));
        // [Doc] Ejecuta la llamada o modificación flash.addFlashAttribute("mensaje", cambio ? "Reintento programado. Se procesará cuando la entrega de correos esté habilitada." : "No se modificó el correo: ya está env... Sus efectos dependen del método invocado y del objeto receptor.
        flash.addFlashAttribute("mensaje", cambio ? "Reintento programado. Se procesará cuando la entrega de correos esté habilitada." : "No se modificó el correo: ya está enviado, pendiente de procesarse o no está disponible.");
        // [Doc] Termina la acción y solicita al navegador una redirección a la ruta indicada.
        return "redirect:/admin/correos";
    }
}

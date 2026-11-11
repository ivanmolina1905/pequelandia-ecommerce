package com.pequelandia.pequelandia.service;

import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.repository.IUsuarioRepository;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.Objects;

/** Autoriza por rol persistido y conserva compatibilidad con el administrador anterior. */
/** [Doc]
 * Resuelve la identidad desde la base y autoriza al administrador mediante un ID fijado al iniciar.
 */
@Service
public class IdentidadService {
    // [Doc] Atributo usuarios (IUsuarioRepository): acceso de persistencia a usuario. Su referencia no se reasigna después de inicializarse.
    private final IUsuarioRepository usuarios;
    // [Doc] Atributo configurado (Long): ID administrativo recibido de la configuración. Su referencia no se reasigna después de inicializarse.
    private final Long configurado;
    // [Doc] Atributo administradorId (Long): ID administrativo fijado al inicializar.
    private Long administradorId;

    /** [Doc]
     * Construye IdentidadService y conserva sus dependencias o valores iniciales.
     * @param usuarios acceso de persistencia a usuario.
     * @param configurado ID administrativo recibido de la configuración.
     */
    public IdentidadService(IUsuarioRepository usuarios,
            @Value("${app.security.admin-id:0}") Long configurado) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.usuarios = usuarios;
        this.usuarios = usuarios;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.configurado = configurado;
        this.configurado = configurado;
    }

    /** [Doc]
     * Fija el ID administrativo configurado o resuelve la única cuenta admin existente al iniciar.
     */
    @PostConstruct
    public void inicializar() {
        // [Doc] Evalúa configurado > 0. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (configurado > 0) {
            // [Doc] Ejecuta la llamada o modificación administradorId = configurado; Sus efectos dependen del método invocado y del objeto receptor.
            administradorId = configurado;
        } else {
            // Compatibilidad con la cuenta existente. Se fija una vez al arrancar.
            // [Doc] Variable local ids (var; tipo inferido por Java): identificadores devueltos por la búsqueda. Se inicializa con usuarios.idsPorNombre("admin").
            var ids = usuarios.idsPorNombre("admin");
            // [Doc] Evalúa ids.size() > 1. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
            if (ids.size() > 1) throw new IllegalStateException("Configurá app.security.admin-id: hay varios admin");
            // [Doc] Ejecuta la llamada o modificación administradorId = ids.isEmpty() ? null : ids.get(0); Sus efectos dependen del método invocado y del objeto receptor.
            administradorId = ids.isEmpty() ? null : ids.get(0);
        }
    }

    /** [Doc]
     * Compara el ID de la cuenta con el ID administrativo inicializado.
     * @param usuario cuenta de usuario utilizada por la operación.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public boolean esAdmin(Usuario usuario) {
        // [Doc] Termina el método o la función y devuelve usuario != null && administradorId != null && Objects.equals(administradorId, usuario.getId()).
        return usuario != null && (usuario.getRol() == Usuario.Rol.ADMINISTRADOR
                || (usuario.getRol() == null && administradorId != null
                    && Objects.equals(administradorId, usuario.getId())));
    }

    /** [Doc]
     * Lee el ID de la sesión y recarga la cuenta desde la base; rechaza sesiones sin una cuenta vigente.
     * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
     * @return Usuario con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public Usuario exigirUsuario(HttpSession session) {
        // [Doc] Variable local anterior (Usuario): estado previo normalizado. Se inicializa con session == null ? null : (Usuario) session.getAttribute("usuario").
        Usuario anterior = session == null ? null : (Usuario) session.getAttribute("usuario");
        // [Doc] Evalúa anterior == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (anterior == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Iniciá sesión");
        // [Doc] Termina el método o la función y devuelve usuarios.findById(anterior.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La cuenta ya no existe")).
        return usuarios.findById(anterior.getId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La cuenta ya no existe"));
    }

    /** [Doc]
     * Exige una cuenta vigente cuyo ID sea el administrador.
     * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
     */
    public void exigirAdmin(HttpSession session) {
        // [Doc] Evalúa !esAdmin(exigirUsuario(session)). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (!esAdmin(exigirUsuario(session)))
            // [Doc] Interrumpe el flujo lanzando new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso reservado a administración"). El llamador o el manejador de errores decide la respuesta.
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso reservado a administración");
    }

    /** [Doc]
     * Reserva admin para la cuenta administrativa y exige configurar su ID antes de renombrarla en modo de
     * compatibilidad.
     * @param usuario cuenta de usuario utilizada por la operación.
     * @param nombre nombre normalizado o visible de la entidad.
     */
    public void validarNombre(Usuario usuario, String nombre) {
        // [Doc] Evalúa "admin".equalsIgnoreCase(nombre.trim()) && !esAdmin(usuario). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if ("admin".equalsIgnoreCase(nombre.trim()) && !esAdmin(usuario))
            // [Doc] Interrumpe el flujo lanzando new IllegalArgumentException("El nombre admin está reservado"). El llamador o el manejador de errores decide la respuesta.
            throw new IllegalArgumentException("El nombre admin está reservado");
        // [Doc] Evalúa configurado == 0 && esAdmin(usuario) && !"admin".equalsIgnoreCase(nombre.trim()). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (configurado == 0 && usuario.getRol() == null && esAdmin(usuario) && !"admin".equalsIgnoreCase(nombre.trim()))
            // [Doc] Interrumpe el flujo lanzando new IllegalArgumentException("Configurá app.security.admin-id antes de renombrar al administrador"). El llamador o el manejador de errores decide la respuesta.
            throw new IllegalArgumentException("Configurá app.security.admin-id antes de renombrar al administrador");
    }
}

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

/** La autorización se resuelve por ID; nunca por el nombre enviado por el navegador. */
@Service
public class IdentidadService {
    private final IUsuarioRepository usuarios;
    private final Long configurado;
    private Long administradorId;

    public IdentidadService(IUsuarioRepository usuarios,
            @Value("${app.security.admin-id:0}") Long configurado) {
        this.usuarios = usuarios;
        this.configurado = configurado;
    }

    @PostConstruct
    public void inicializar() {
        if (configurado > 0) {
            administradorId = configurado;
        } else {
            // Compatibilidad con la cuenta existente. Se fija una vez al arrancar.
            var ids = usuarios.idsPorNombre("admin");
            if (ids.size() > 1) throw new IllegalStateException("Configurá app.security.admin-id: hay varios admin");
            administradorId = ids.isEmpty() ? null : ids.get(0);
        }
    }

    public boolean esAdmin(Usuario usuario) {
        return usuario != null && administradorId != null
                && Objects.equals(administradorId, usuario.getId());
    }

    public Usuario exigirUsuario(HttpSession session) {
        Usuario anterior = session == null ? null : (Usuario) session.getAttribute("usuario");
        if (anterior == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Iniciá sesión");
        return usuarios.findById(anterior.getId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La cuenta ya no existe"));
    }

    public void exigirAdmin(HttpSession session) {
        if (!esAdmin(exigirUsuario(session)))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso reservado a administración");
    }

    public void validarNombre(Usuario usuario, String nombre) {
        if ("admin".equalsIgnoreCase(nombre.trim()) && !esAdmin(usuario))
            throw new IllegalArgumentException("El nombre admin está reservado");
        if (configurado == 0 && esAdmin(usuario) && !"admin".equalsIgnoreCase(nombre.trim()))
            throw new IllegalArgumentException("Configurá app.security.admin-id antes de renombrar al administrador");
    }
}

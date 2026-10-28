package com.pequelandia.pequelandia.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pequelandia.pequelandia.repository.IUsuarioRepository;

@Service
public class RecuperacionService {
    private final JdbcTemplate jdbc;
    private final IUsuarioRepository usuarios;
    private final ColaCorreoService correo;
    private final String baseUrl;
    private final SecureRandom random = new SecureRandom();

    public RecuperacionService(JdbcTemplate jdbc, IUsuarioRepository usuarios, ColaCorreoService correo,
            @Value("${app.security.public-url:http://localhost:8080}") String baseUrl) {
        URI uri = URI.create(baseUrl);
        if (!("https".equals(uri.getScheme()) || ("http".equals(uri.getScheme())
                && ("localhost".equals(uri.getHost()) || "127.0.0.1".equals(uri.getHost()))))
                || uri.getHost() == null || uri.getRawQuery() != null || uri.getFragment() != null
                || uri.getUserInfo() != null) {
            throw new IllegalArgumentException("app.security.public-url debe ser HTTPS (HTTP solo en localhost)");
        }
        this.jdbc = jdbc; this.usuarios = usuarios; this.correo = correo;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
    }

    static String hash(String valor) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }

    // Contadores persistentes y atómicos compartidos por todas las instancias.
    private boolean permitir(String clave, int maximo, int segundos) {
        return jdbc.update("""
            INSERT INTO recuperacion_limite(clave, inicio, intentos) VALUES (?, clock_timestamp(), 1)
            ON CONFLICT (clave) DO UPDATE SET
              intentos = CASE WHEN recuperacion_limite.inicio < clock_timestamp() - (? * INTERVAL '1 second')
                         THEN 1 ELSE recuperacion_limite.intentos + 1 END,
              inicio = CASE WHEN recuperacion_limite.inicio < clock_timestamp() - (? * INTERVAL '1 second')
                       THEN clock_timestamp() ELSE recuperacion_limite.inicio END
            WHERE recuperacion_limite.inicio < clock_timestamp() - (? * INTERVAL '1 second')
               OR recuperacion_limite.intentos < ?
            """, hash(clave), segundos, segundos, segundos, maximo) == 1;
    }

    @Transactional
    public void solicitar(String email, String ip) {
        jdbc.update("DELETE FROM recuperacion_limite WHERE inicio < clock_timestamp() - INTERVAL '1 day'");
        jdbc.update("DELETE FROM recuperacion_clave WHERE vence < clock_timestamp() - INTERVAL '1 day'");
        if (!permitir("solicitud-ip:" + ip, 10, 900)) return;
        if (email == null || email.length() > 320) return;
        String normalizado = email.trim().toLowerCase(java.util.Locale.ROOT);
        if (!normalizado.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) return;
        if (!permitir("solicitud-correo:" + normalizado, 3, 3600)
                || !permitir("espera-correo:" + normalizado, 1, 60)) return;
        var ids = jdbc.queryForList("SELECT id_usuario FROM usuario WHERE lower(trim(email)) = ?",
                Long.class, normalizado);
        // No elegir arbitrariamente una cuenta con un correo duplicado heredado.
        if (ids.size() != 1) return;
        Long id = ids.getFirst();
        jdbc.queryForObject("SELECT id_usuario FROM usuario WHERE id_usuario=? FOR UPDATE", Long.class, id);
        byte[] bytes = new byte[32]; random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        jdbc.update("DELETE FROM recuperacion_clave WHERE usuario_id=?", id);
        jdbc.update("""
            INSERT INTO recuperacion_clave(token_hash, usuario_id, email, vence)
            VALUES (?, ?, ?, clock_timestamp() + INTERVAL '30 minutes')
            """, hash(token), id, normalizado);
        correo.encolar(normalizado, "Recuperá tu contraseña de Pequelandia",
                "Para crear una contraseña nueva, abrí este enlace:\n\n"
                + baseUrl + "/auth/restablecer?token=" + token
                + "\n\nVence en 30 minutos y solo puede usarse una vez."
                + "\nSi no lo solicitaste, podés ignorar este correo. Tu contraseña no cambió.");
    }

    private boolean formatoToken(String token) {
        return token != null && token.matches("[A-Za-z0-9_-]{43}");
    }

    public boolean vigente(String token) {
        if (!formatoToken(token)) return false;
        return Boolean.TRUE.equals(jdbc.queryForObject("""
            SELECT EXISTS(SELECT 1 FROM recuperacion_clave r JOIN usuario u ON u.id_usuario=r.usuario_id
            WHERE r.token_hash=? AND r.vence > clock_timestamp() AND lower(trim(u.email))=r.email)
            """, Boolean.class, hash(token)));
    }

    @Transactional
    public boolean restablecer(String token, String password, String confirmacion, String ip) {
        if (!permitir("restablecer-ip:" + ip, 20, 900)) return false;
        if (!formatoToken(token) || password == null || password.length() < 8 || password.length() > 128
                || password.isBlank() || !password.equals(confirmacion)) return false;
        var ids = jdbc.queryForList("SELECT usuario_id FROM recuperacion_clave WHERE token_hash=?",
                Long.class, hash(token));
        if (ids.isEmpty()) return false;
        // Serializa solicitudes y consumos para impedir reutilización simultánea.
        var usuario = usuarios.findByIdParaRecuperacion(ids.getFirst());
        if (usuario.isEmpty() || !vigente(token)) return false;
        var u = usuario.get();
        u.setContraseña(password);
        usuarios.saveAndFlush(u);
        jdbc.update("DELETE FROM recuperacion_clave WHERE usuario_id=?", u.getId());
        correo.encolar(u.getEmail(), "Tu contraseña de Pequelandia fue modificada",
                "Tu contraseña se cambió correctamente. Ya podés iniciar sesión con la nueva contraseña."
                + "\nSi no realizaste este cambio, recuperá tu cuenta y contactá a Pequelandia.");
        return true;
    }
}

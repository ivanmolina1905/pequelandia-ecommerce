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

/** [Doc]
 * Gestiona enlaces de recuperación de uso único, vencimiento, límites persistentes y cambio de contraseña.
 */
@Service
public class RecuperacionService {
    // [Doc] Atributo jdbc (JdbcTemplate): acceso SQL parametrizado mediante JdbcTemplate. Su referencia no se reasigna después de inicializarse.
    private final JdbcTemplate jdbc;
    // [Doc] Atributo usuarios (IUsuarioRepository): acceso de persistencia a usuario. Su referencia no se reasigna después de inicializarse.
    private final IUsuarioRepository usuarios;
    // [Doc] Atributo correo (ColaCorreoService): colaborador que concentra operaciones de cola correo. Su referencia no se reasigna después de inicializarse.
    private final ColaCorreoService correo;
    // [Doc] Atributo baseUrl (String): URL pública de base para construir enlaces de recuperación. Su referencia no se reasigna después de inicializarse.
    private final String baseUrl;
    // [Doc] Atributo random (SecureRandom): generador aleatorio criptográfico para tokens de recuperación. Se inicializa con new SecureRandom(). Su referencia no se reasigna después de inicializarse.
    private final SecureRandom random = new SecureRandom();

    /** [Doc]
     * Construye RecuperacionService y conserva sus dependencias o valores iniciales.
     * @param jdbc acceso SQL parametrizado mediante JdbcTemplate.
     * @param usuarios acceso de persistencia a usuario.
     * @param correo colaborador que concentra operaciones de cola correo.
     * @param baseUrl URL pública de base para construir enlaces de recuperación.
     */
    public RecuperacionService(JdbcTemplate jdbc, IUsuarioRepository usuarios, ColaCorreoService correo,
            @Value("${app.security.public-url:http://localhost:8080}") String baseUrl) {
        // [Doc] Variable local uri (URI): dirección analizada para comprobar la URL pública. Se inicializa con URI.create(baseUrl).
        URI uri = URI.create(baseUrl);
        // [Doc] Evalúa !("https".equals(uri.getScheme()) || ("http".equals(uri.getScheme()) && ("localhost".equals(uri.getHost()) || "127.0.0.1".equals(uri.getHost())))) || uri.getHost() == null || ur.... Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (!("https".equals(uri.getScheme()) || ("http".equals(uri.getScheme())
                && ("localhost".equals(uri.getHost()) || "127.0.0.1".equals(uri.getHost()))))
                || uri.getHost() == null || uri.getRawQuery() != null || uri.getFragment() != null
                || uri.getUserInfo() != null) {
            // [Doc] Interrumpe el flujo lanzando new IllegalArgumentException("app.security.public-url debe ser HTTPS (HTTP solo en localhost)"). El llamador o el manejador de errores decide la respuesta.
            throw new IllegalArgumentException("app.security.public-url debe ser HTTPS (HTTP solo en localhost)");
        }
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.jdbc = jdbc;
        this.jdbc = jdbc; this.usuarios = usuarios; this.correo = correo;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.baseUrl = baseUrl.replaceAll("/+$", "");
    }

    /** [Doc]
     * Calcula SHA-256 del token o clave de límite y lo devuelve como texto hexadecimal; no transforma contraseñas de
     * usuarios.
     * @param valor valor recibido o calculado para este paso.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    static String hash(String valor) {
        // [Doc] Ejecuta un bloque con tratamiento de excepciones; los catch asociados atienden los fallos declarados y finally se ejecuta al salir si existe.
        try {
            // [Doc] Termina el método o la función y devuelve HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256") .digest(valor.getBytes(StandardCharsets.UTF_8))).
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }

    // Contadores persistentes y atómicos compartidos por todas las instancias.
    /** [Doc]
     * Incrementa atómicamente un contador persistente dentro de una ventana y devuelve si aún se admite la
     * solicitud.
     * @param clave clave.
     * @param maximo maximo.
     * @param segundos segundos.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private boolean permitir(String clave, int maximo, int segundos) {
        // [Doc] Termina el método o la función y devuelve jdbc.update(""" INSERT INTO recuperacion_limite(clave, inicio, intentos) VALUES (?, clock_timestamp(), 1) ON CONFLICT (clave) DO UPDATE SET intentos = CASE W....
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

    /** [Doc]
     * Aplica límites por IP y correo, identifica una única cuenta, reemplaza su token y encola un enlace que vence a
     * los treinta minutos.
     * @param email dirección de correo de la cuenta.
     * @param ip dirección IP usada para limitar solicitudes de recuperación.
     */
    @Transactional
    public void solicitar(String email, String ip) {
        // [Doc] Ejecuta una modificación SQL con los parámetros indicados; participa en la transacción si existe.
        jdbc.update("DELETE FROM recuperacion_limite WHERE inicio < clock_timestamp() - INTERVAL '1 day'");
        // [Doc] Ejecuta una modificación SQL con los parámetros indicados; participa en la transacción si existe.
        jdbc.update("DELETE FROM recuperacion_clave WHERE vence < clock_timestamp() - INTERVAL '1 day'");
        // [Doc] Evalúa !permitir("solicitud-ip:" + ip, 10, 900). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (!permitir("solicitud-ip:" + ip, 10, 900)) return;
        // [Doc] Evalúa email == null || email.length() > 320. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (email == null || email.length() > 320) return;
        // [Doc] Variable local normalizado (String): normalizado. Se inicializa con email.trim().toLowerCase(java.util.Locale.ROOT).
        String normalizado = email.trim().toLowerCase(java.util.Locale.ROOT);
        // [Doc] Evalúa !normalizado.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (!normalizado.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) return;
        // [Doc] Evalúa !permitir("solicitud-correo:" + normalizado, 3, 3600) || !permitir("espera-correo:" + normalizado, 1, 60). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (!permitir("solicitud-correo:" + normalizado, 3, 3600)
                || !permitir("espera-correo:" + normalizado, 1, 60)) return;
        // [Doc] Variable local ids (var; tipo inferido por Java): identificadores devueltos por la búsqueda. Se inicializa con jdbc.queryForList("SELECT id_usuario FROM usuario WHERE lower(trim(email)) = ?", Long.class, normalizado).
        var ids = jdbc.queryForList("SELECT id_usuario FROM usuario WHERE lower(trim(email)) = ?",
                Long.class, normalizado);
        // No elegir arbitrariamente una cuenta con un correo duplicado heredado.
        // [Doc] Evalúa ids.size() != 1. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (ids.size() != 1) return;
        // [Doc] Variable local id (Long): identificador del registro. Se inicializa con ids.getFirst().
        Long id = ids.getFirst();
        // [Doc] Ejecuta la llamada o modificación jdbc.queryForObject("SELECT id_usuario FROM usuario WHERE id_usuario=? FOR UPDATE", Long.class, id); Sus efectos dependen del método invocado y del objeto receptor.
        jdbc.queryForObject("SELECT id_usuario FROM usuario WHERE id_usuario=? FOR UPDATE", Long.class, id);
        // [Doc] Variable local bytes (byte[]): bytes aleatorios o codificados de la operación. Se inicializa con new byte[32].
        byte[] bytes = new byte[32]; random.nextBytes(bytes);
        // [Doc] Variable local token (String): valor del token usado por esta operación. Se inicializa con Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        // [Doc] Ejecuta una modificación SQL con los parámetros indicados; participa en la transacción si existe.
        jdbc.update("DELETE FROM recuperacion_clave WHERE usuario_id=?", id);
        // [Doc] Ejecuta una modificación SQL con los parámetros indicados; participa en la transacción si existe.
        jdbc.update("""
            INSERT INTO recuperacion_clave(token_hash, usuario_id, email, vence)
            VALUES (?, ?, ?, clock_timestamp() + INTERVAL '30 minutes')
            """, hash(token), id, normalizado);
        // [Doc] Ejecuta la llamada o modificación correo.encolar(normalizado, "Recuperá tu contraseña de Pequelandia", "Para crear una contraseña nueva, abrí este enlace:\n\n" + baseUrl + "/auth/restablecer?token=" + ... Sus efectos dependen del método invocado y del objeto receptor.
        correo.encolar(normalizado, "Recuperá tu contraseña de Pequelandia",
                "Para crear una contraseña nueva, abrí este enlace:\n\n"
                + baseUrl + "/auth/restablecer?token=" + token
                + "\n\nVence en 30 minutos y solo puede usarse una vez."
                + "\nSi no lo solicitaste, podés ignorar este correo. Tu contraseña no cambió.");
    }

    /** [Doc]
     * Comprueba que el token tenga cuarenta y tres caracteres válidos de Base64 URL sin relleno.
     * @param token valor del token usado por esta operación.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private boolean formatoToken(String token) {
        // [Doc] Termina el método o la función y devuelve token != null && token.matches("[A-Za-z0-9_-]{43}").
        return token != null && token.matches("[A-Za-z0-9_-]{43}");
    }

    /** [Doc]
     * Comprueba formato, hash, vencimiento y coincidencia entre correo actual y correo del token.
     * @param token valor del token usado por esta operación.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public boolean vigente(String token) {
        // [Doc] Evalúa !formatoToken(token). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (!formatoToken(token)) return false;
        // [Doc] Termina el método o la función y devuelve Boolean.TRUE.equals(jdbc.queryForObject(""" SELECT EXISTS(SELECT 1 FROM recuperacion_clave r JOIN usuario u ON u.id_usuario=r.usuario_id WHERE r.token_hash=?....
        return Boolean.TRUE.equals(jdbc.queryForObject("""
            SELECT EXISTS(SELECT 1 FROM recuperacion_clave r JOIN usuario u ON u.id_usuario=r.usuario_id
            WHERE r.token_hash=? AND r.vence > clock_timestamp() AND lower(trim(u.email))=r.email)
            """, Boolean.class, hash(token)));
    }

    /** [Doc]
     * Valida límites, token y contraseña, bloquea la cuenta y cambia la clave, consume el token y encola el aviso en
     * una transacción.
     * @param token valor del token usado por esta operación.
     * @param password contraseña recibida para la operación de cuenta.
     * @param confirmacion segunda copia de la contraseña para comprobar coincidencia.
     * @param ip dirección IP usada para limitar solicitudes de recuperación.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Transactional
    public boolean restablecer(String token, String password, String confirmacion, String ip) {
        // [Doc] Evalúa !permitir("restablecer-ip:" + ip, 20, 900). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (!permitir("restablecer-ip:" + ip, 20, 900)) return false;
        // [Doc] Evalúa !formatoToken(token) || password == null || password.length() < 8 || password.length() > 128 || password.isBlank() || !password.equals(confirmacion). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (!formatoToken(token) || password == null || password.length() < 8 || password.length() > 128
                || password.isBlank() || !password.equals(confirmacion)) return false;
        // [Doc] Variable local ids (var; tipo inferido por Java): identificadores devueltos por la búsqueda. Se inicializa con jdbc.queryForList("SELECT usuario_id FROM recuperacion_clave WHERE token_hash=?", Long.class, hash(token)).
        var ids = jdbc.queryForList("SELECT usuario_id FROM recuperacion_clave WHERE token_hash=?",
                Long.class, hash(token));
        // [Doc] Evalúa ids.isEmpty(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (ids.isEmpty()) return false;
        // Serializa solicitudes y consumos para impedir reutilización simultánea.
        // [Doc] Variable local usuario (var; tipo inferido por Java): cuenta de usuario utilizada por la operación. Se inicializa con usuarios.findByIdParaRecuperacion(ids.getFirst()).
        var usuario = usuarios.findByIdParaRecuperacion(ids.getFirst());
        // [Doc] Evalúa usuario.isEmpty() || !vigente(token). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (usuario.isEmpty() || !vigente(token)) return false;
        // [Doc] Variable local u (var; tipo inferido por Java): cuenta de usuario utilizada por la operación. Se inicializa con usuario.get().
        var u = usuario.get();
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: u.setContraseña(password);
        u.setContraseña(password);
        // [Doc] Solicita persistir la entidad; saveAndFlush también fuerza la sincronización: usuarios.saveAndFlush(u);
        usuarios.saveAndFlush(u);
        // [Doc] Ejecuta una modificación SQL con los parámetros indicados; participa en la transacción si existe.
        jdbc.update("DELETE FROM recuperacion_clave WHERE usuario_id=?", u.getId());
        // [Doc] Ejecuta la llamada o modificación correo.encolar(u.getEmail(), "Tu contraseña de Pequelandia fue modificada", "Tu contraseña se cambió correctamente. Ya podés iniciar sesión con la nueva contraseña." +... Sus efectos dependen del método invocado y del objeto receptor.
        correo.encolar(u.getEmail(), "Tu contraseña de Pequelandia fue modificada",
                "Tu contraseña se cambió correctamente. Ya podés iniciar sesión con la nueva contraseña."
                + "\nSi no realizaste este cambio, recuperá tu cuenta y contactá a Pequelandia.");
        // [Doc] Termina el método o la función y devuelve true.
        return true;
    }
}

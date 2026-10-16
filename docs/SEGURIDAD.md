# Seguridad de publicación

No publicar `config/application-local.properties`, variables de entorno, `target/`, bases, backups, logs ni exportaciones. La configuración privada original se conservó localmente en el archivo ignorado para evitar perder la conexión de desarrollo. Cambiar la contraseña original de PostgreSQL si estuvo expuesta o compartida. Eliminarla del código no la revoca.

Las contraseñas de usuarios se mantienen sin hash por decisión de alcance. No usar clientes reales ni exponer una instancia de producción en estas condiciones.

Se agregaron controles MVC para administración, consultas de usuarios, paneles y titularidad de pedidos. La confirmación de cancelación queda reservada a administración. El campo contraseña no se serializa a JSON y el login rota la sesión. La identificación administrativa sigue dependiendo del nombre de usuario `admin`: faltan roles explícitos, CSRF, protección de intentos y auditoría integral.

Antes de cada publicación: revisar `git status`, `git diff --cached`, archivos ignorados y secretos; verificar que no existan datos personales en código, imágenes o historial. Revisar también binarios y archivos nuevos. Mantener las configuraciones de Eclipse locales. No agregar autor ni licencia sin confirmación.

# Seguridad de publicación

No publicar `config/application-local.properties`, variables de entorno, `target/`, bases, backups, logs ni exportaciones. La configuración privada original se conservó localmente en el archivo ignorado para evitar perder la conexión de desarrollo. Cambiar la contraseña original de PostgreSQL si estuvo expuesta o compartida. Eliminarla del código no la revoca.

Las contraseñas de usuarios se mantienen sin hash por decisión de alcance. No usar clientes reales ni exponer una instancia de producción en estas condiciones.

Se agregaron controles MVC para administración, consultas de usuarios, paneles y titularidad de pedidos. La confirmación de cancelación queda reservada a administración. El campo contraseña no se serializa a JSON y el login rota la sesión. Las operaciones POST requieren token CSRF, tanto en formularios como en fetch.

La administración se autoriza por un ID fijo configurado mediante `app.security.admin-id` o `PEQUELANDIA_ADMIN_ID`. Por compatibilidad, con valor 0 se resuelve una única cuenta `admin` al arrancar y se conserva su ID. Los cambios de nombre no conceden permisos; el nombre reservado no puede asignarse a otra cuenta. Se recomienda configurar el ID explícitamente. No se implementó un sistema de múltiples roles ni límites de intentos de login.

Los cambios de pedidos, stock y avisos son transaccionales. Se probaron accesos ajenos, CSRF, cancelaciones y compras simultáneas contra PostgreSQL descartable. Esto no equivale a una auditoría integral de seguridad ni a una validación de producción. Ver [funcionamiento y pruebas](NOTIFICACIONES.md).

Antes de cada publicación: revisar `git status`, `git diff --cached`, archivos ignorados y secretos; verificar que no existan datos personales en código, imágenes o historial. Revisar también binarios y archivos nuevos. Mantener las configuraciones de Eclipse locales. No agregar autor ni licencia sin confirmación.

# Recuperación de contraseña por correo

Desde **Olvidé mi contraseña**, el usuario ingresa su correo y recibe un enlace para elegir y confirmar una nueva contraseña. La respuesta es igual para correos existentes, inexistentes o limitados. La nueva clave debe tener entre 8 y 128 caracteres; nunca se envían contraseñas por correo.

## Configuración

En la configuración local (ignorada por Git), configurar:

- No hace falta una URL pública para desarrollo: Spring usa `http://localhost:${server.port:8080}${server.servlet.context-path:}`. Con el puerto predeterminado, el enlace comienza en `http://localhost:8080/auth/restablecer`. Si cambiás el puerto o contexto en Spring, los enlaces se ajustan. Opcionalmente, `PEQUELANDIA_PUBLIC_URL` o `app.security.public-url` permite fijar otra dirección. No se usa el encabezado Host del visitante.
- Configuración SMTP existente y `app.mail.delivery-enabled=true`. Con entrega desactivada, los correos permanecen en cola y no llegan al buzón.

El script aditivo `db/notificaciones.sql` crea automáticamente las tablas de recuperación y límites. No modifica contraseñas existentes.

El correo se envía mediante el flujo existente de Spring: `RecuperacionService` → `ColaCorreoService` → `CorreoScheduler` → `EntregaCorreoService` → `JavaMailSender`. No se usa un cliente de correo externo a Spring.

Para Gmail siguen siendo necesarias las variables locales `PEQUELANDIA_MAIL_USERNAME`, `PEQUELANDIA_MAIL_PASSWORD` (contraseña de aplicación) y `PEQUELANDIA_ADMIN_EMAIL`. Si arrancás desde Eclipse, definirlas en Environment de la configuración de ejecución. No versionar estos valores. Spring no reemplaza la autenticación que exige Gmail.

Abrí el enlace de localhost en la misma computadora donde corre Spring. En otro dispositivo, localhost apunta a ese otro dispositivo.

## Controles

- Tokens aleatorios de 256 bits; únicamente su hash SHA-256 se guarda en la tabla de recuperación.
- Vencimiento a los 30 minutos desde la solicitud, uso único y reemplazo de enlaces anteriores al generar uno nuevo.
- Validación de que el correo de la cuenta no cambió desde la solicitud. Correos duplicados heredados no generan enlaces: corregir esas cuentas primero.
- Bloqueo transaccional por usuario para impedir consumos concurrentes; cambio, consumo y aviso por correo se confirman juntos o se revierten juntos.
- Hasta 10 solicitudes por IP cada 15 minutos; hasta 3 intentos por correo cada hora, con espera mínima de 60 segundos entre envíos. Los intentos durante la espera también cuentan.
- Hasta 20 intentos de restablecimiento por IP cada 15 minutos. Los límites persisten en PostgreSQL; no se reinician al reiniciar la aplicación.
- CSRF en ambos formularios; páginas sin caché, sin envío de referencia y excluidas de indexación. No se registra el token en logs de aplicación.
- Aviso de cambio por correo, cierre de la sesión del navegador que completó la recuperación y regreso al login.
- La cola administrativa muestra metadatos de envío, no el contenido del enlace. La cola de correo necesita conservar el mensaje para entregarlo: restringir acceso a la base y no registrar cuerpos ni parámetros de recuperación en proxies.
- Los mensajes pueden llegar tarde si SMTP falla: un enlace vencido requiere una nueva solicitud.
- Detrás de un proxy, configurar el servidor para obtener la IP real exclusivamente de un proxy confiable; la aplicación no confía directamente en X-Forwarded-For enviado por visitantes.

Se conserva el almacenamiento de contraseñas existente, conforme a AGENTS.md. El administrador no recibe la clave nueva y el campo de contraseña sigue excluido de respuestas JSON. Las sesiones abiertas en otros navegadores mantienen el comportamiento actual del sistema.

## Verificación

`RecuperacionIntegrationTest` usa PostgreSQL aislado y correo simulado. Cubre flujo completo y login, token vencido/usado, correo cambiado, confirmación de clave, respuesta genérica, CSRF, límites, consumo concurrente, rollback y redirección al login. Ver `NOTIFICACIONES.md` para configurar la base descartable de pruebas.

Verificación del 27/10/2026: 43 pruebas Java aprobadas (9 de recuperación), sin fallos ni omisiones, contra PostgreSQL 18 aislado; 4 pruebas JavaScript aprobadas. Se renderizaron los formularios con MockMvc/Thymeleaf y se simularon los envíos SMTP. No se verificó entrega a un buzón real.

Verificación del modo local (27/10/2026): 44 pruebas Java y 4 JavaScript aprobadas. La nueva prueba verifica el enlace con server.port=8091 y la entrega mediante EntregaCorreoService y JavaMailSender simulado. Sin envío a buzones reales.

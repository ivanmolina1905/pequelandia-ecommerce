# Verificación de registro — 31/10/2026

Se creó mediante POST al formulario `/auth/registro`, con CSRF, el usuario ficticio `prueba_registro_3110`, correo `registro3110@example.invalid`, ID 2. Se utilizó exclusivamente la base nueva `pequelandia_registro_20261031`, PostgreSQL local puerto 55439; no se importó la configuración privada habitual.

Resultados comprobados en ejecución:

- Formulario de registro HTTP 200 y alta con redirección a login exitoso.
- Inicio de sesión del cliente y panel de perfil con su correo.
- Inicio de sesión del administrador; búsqueda en `/admin/usuarios` mostrando el nuevo correo.
- Historial administrativo con aviso de nuevo usuario y panel de correos mostrando su destinatario.
- Consulta SQL directa confirmó ID, nombre, correo y DNI ficticio en `usuario`.
- JavaMailSender se conectó por SMTP a un receptor local de captura: recibió dos mensajes completos, aviso administrativo y bienvenida. Ambos registros de `correo_pendiente` quedaron con `intentos=1` y `enviado_en` informado.
- Las 27 pruebas existentes de `CircuitoUsuariosIntegrationTest` aprobaron: 0 fallos, 0 errores y 0 omitidas. Tres son caracterizaciones de limitaciones conocidas, no criterios funcionales deseables: email incompleto aceptado, última conexión sin actualizar y perfil de sesión anterior a edición administrativa.

Las comprobaciones HTTP utilizaron MockMvc con controladores, interceptores, servicios, plantillas y PostgreSQL reales. No se realizó una inspección visual en navegador. SMTP fue local de captura, sin autenticación ni TLS: no se verificó Gmail ni recepción en un buzón externo. No había credenciales SMTP disponibles en las variables del proceso.

Evidencia local ignorada por Git: `.tools/registro-tests-ejecucion-20261031.log`, `.tools/registro-completo-20261031.log`, `.tools/registro-smtp-1.eml`, `.tools/registro-smtp-2.eml` y respuestas HTML `.tools/registro-*-20261031.html`. El lanzador temporal es `.tools/RegistroCompleto.java`. La cuenta y los correos permanecen en la base aislada para revisión. Se detuvieron los servidores de prueba al finalizar. No se modificó código de negocio ni se hizo commit o push.

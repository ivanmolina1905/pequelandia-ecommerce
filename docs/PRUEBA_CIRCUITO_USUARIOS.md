# Prueba detallada del circuito de usuarios

Fecha: 27/10/2026. Se usaron datos ficticios y PostgreSQL local aislado, base pequelandia_verificacion. No se modificaron cuentas reales ni se enviaron mensajes a personas.

## Resultado de la ejecución

71 pruebas Java verificadas, sin fallos ni omisiones: 44 existentes y 27 del circuito ampliado. La suite completa de 70 pasó; luego se añadió y verificó la consulta SQL documental, repitiendo los 27 casos del circuito. También pasaron las 4 pruebas JavaScript. Tres casos reproducen los problemas documentados abajo; su aprobación confirma el hallazgo y no significa que el comportamiento sea correcto.

Las consultas adjuntas se ejecutaron correctamente contra PostgreSQL en una transacción de solo lectura.

## Qué se ejecutó

Las pruebas pasan por los controladores Spring MVC, servicios, repositorios JPA, transacciones, plantillas Thymeleaf y PostgreSQL real. MockMvc simula las solicitudes HTTP y las sesiones; JavaMailSender está simulado. Esto verifica el backend y el HTML generado, pero no constituye una revisión visual en navegador ni una entrega real en Gmail.

El archivo CircuitoUsuariosIntegrationTest agrega un recorrido completo y casos de validación y administración. Las consultas SQL verifican lo guardado directamente, después de que las solicitudes terminan, sin envolver el test entero en una transacción que oculte los commits.

## Recorrido comprobado

1. Un visitante abre /auth/registro. Spring entrega el formulario, localidades y token CSRF.
2. Envía nombre, apellido, email, DNI, teléfono, domicilio, nacimiento, localidad, clave, confirmación y aceptación de términos.
3. El servidor valida los datos y rechaza el nombre reservado admin.
4. UsuarioService.registrar inicia una transacción. Recorta espacios de nombre, apellido y dirección; guarda el email en minúsculas; asigna fecha de registro y puntos=0.
5. PostgreSQL asigna id_usuario y relaciona la cuenta con localidad mediante id_localidad.
6. Se inserta una notificación para ADMIN y se encolan dos correos: aviso administrativo y bienvenida.
7. Solo al confirmar todo se conserva el alta. Un fallo al encolar revierte la cuenta, la notificación y los mensajes.
8. El navegador vuelve a /auth/login?registroExitoso=true. Crear la cuenta no inicia sesión automáticamente.
9. El administrador inicia sesión y llega a /panel/admin. En /admin/usuarios puede buscar al usuario por nombre, apellido o correo. La vista muestra nombre, apellido, ID bajo la columna “Usuario”, email, última conexión, historial y acciones.
10. En /notificaciones/historial aparece “Nuevo usuario registrado: circuito”. En /admin/correos aparecen los metadatos de los correos. La contraseña no aparece en el HTML ni en la respuesta JSON de usuarios.
11. El usuario inicia sesión por nombre o email, sin distinguir mayúsculas. La contraseña debe coincidir. Spring cambia el ID de sesión al ingresar y guarda el usuario en la sesión.
12. El usuario llega a /panelusuarios/panelhistorial?section=perfil. No puede acceder a la lista administrativa ni a la cola de correos.
13. Al editar su perfil, se actualizan PostgreSQL y su sesión. Se comprobó dirección y teléfono.
14. Al salir, se invalida la sesión. Sin sesión, el acceso a paneles protegidos recibe 401.
15. El usuario solicita recuperación. Se guarda el hash del token con vencimiento de 30 minutos y se encola el enlace.
16. Al elegir una clave nueva se elimina el token y se encola el aviso de cambio. La clave anterior deja de autenticar y la nueva permite ingresar.
17. EntregaCorreoService procesa los cuatro mensajes generados en el recorrido: dos del registro, recuperación y aviso de cambio. Se verifica que Spring llama cuatro veces a JavaMailSender y marca las filas como enviadas. La aceptación real por Gmail no está probada.

## Datos comprobados en PostgreSQL

| Tabla / campo | Comprobación |
| --- | --- |
| usuario.id_usuario | ID generado por PostgreSQL; distinto del administrador |
| nombre_usuario | circuito, sin espacios de los extremos |
| apellido | Prueba, sin espacios de los extremos |
| email | circuito@example.invalid, normalizado a minúsculas |
| dni | 23456789 |
| telefono | +54 342 123-4567 |
| direccion | Calle ficticia 123; luego actualizada desde el perfil |
| fecha_nacimiento | 1998-04-15 |
| fecha_registro | No nula, asignada durante el registro |
| ultima_conexion | Nula incluso después del login: limitación confirmada |
| id_localidad | Coincide con la localidad seleccionada |
| puntos | 0 |
| contraseña | Coincide con la elegida; cambia al recuperar la cuenta |
| notificacion | Una fila para ADMIN por el alta |
| correo_pendiente | Dos filas por registro; cuatro al completar recuperación |
| recuperacion_clave | Una fila al pedir el enlace, cero al utilizarlo |
| recuperacion_limite | Contadores persistentes de solicitudes e intentos |

La contraseña permanece en texto legible en la base por la instrucción vigente de AGENTS.md. Que no se muestre en HTML/JSON no significa que esté cifrada o hasheada. Las pruebas la comparan sin imprimir su valor. La confirmación de contraseña y la aceptación de términos no tienen columnas propias en Usuario: son validaciones del formulario, no registros de auditoría almacenados.

## Casos negativos y administración

Se probaron campos obligatorios vacíos, DNI fuera de rango, DNI no numérico, contraseña corta, confirmación diferente, términos sin aceptar, nombre admin, correo sin arroba, fecha inválida, localidad no numérica y localidad inexistente.

También se probaron duplicados de nombre/email sin distinguir mayúsculas y duplicado de DNI, registro y login sin CSRF, login incorrecto sin crear sesión autenticada, permisos de usuario/administrador, edición administrativa, eliminación de una cuenta sin compras y rechazo de acceso/login de la cuenta eliminada.

Las pruebas existentes amplían la revisión a compras, carrito, stock, pedidos, historial, permisos de acceso a pedidos ajenos, cancelación, concurrencia, reintentos de correo y recuperación con enlace vencido o ya utilizado.

## Hallazgos reproducidos que quedan pendientes

| Hallazgo | Efecto observado | Cambio recomendado |
| --- | --- | --- |
| Email incompleto aceptado en backend | Un POST con incompleto@ crea la cuenta; la validación actual solo exige una arroba. El navegador puede impedirlo, pero el servidor sigue aceptándolo. | Validar formato de email en registro y perfil del lado del servidor. |
| Última conexión no se actualiza | La columna sigue nula después de un login exitoso; el administrador ve un guion. | Registrar fecha/hora solo cuando las credenciales son correctas. |
| Perfil de una sesión abierta queda desactualizado tras edición administrativa | PostgreSQL tiene la dirección nueva, pero el modelo del perfil sigue tomando el usuario guardado anteriormente en la sesión. | Refrescar el usuario de sesión o construir el modelo con el usuario leído de la base. |

Estos tres casos tienen pruebas de caracterización: pasan al reproducir el comportamiento actual y se identifican como hallazgos, no como comportamiento deseable. El resto de los casos exige el resultado funcional esperado. No se modificó el código de negocio durante esta revisión.

No se verificó aquí registro simultáneo de dos cuentas con el mismo email/DNI, eliminación de usuarios con compras históricas, carga masiva, correo real, comportamiento visual de modales/JavaScript, ni persistencia después de reiniciar PostgreSQL. No atribuir estos resultados a las pruebas de este informe.

## Cómo probarlo vos en el navegador

Usá una base de desarrollo y dos perfiles de navegador o ventanas privadas separadas: una para administración y otra para el usuario. Evitá cerrar la sesión de una al entrar con la otra. No ejecutes los tests automatizados contra la base que estés usando manualmente: las pruebas vacían sus tablas.

### Preparación con Spring/Eclipse

1. Iniciá PostgreSQL de desarrollo y la aplicación desde la configuración habitual de Spring/Eclipse.
2. Verificá que haya una localidad y una cuenta administrativa. La autorización administrativa depende del ID configurado; escribir admin como nombre no convierte a una cuenta en administradora.
3. Abrí http://localhost:8080/auth/registro; si Spring usa otro puerto o contexto, ajustá la dirección.
4. Para recibir correos reales, la ejecución de Spring necesita PEQUELANDIA_MAIL_USERNAME, PEQUELANDIA_MAIL_PASSWORD y PEQUELANDIA_ADMIN_EMAIL. Configuralas en Environment de Eclipse. No pegues contraseñas en el chat ni las subas al repositorio.
5. Si no están disponibles, mantené la entrega desactivada para la prueba manual y comprobá la cola. Un correo pendiente no significa que haya llegado al buzón.

### Registro y administrador

1. En la ventana del usuario registrá un nombre de prueba único, un email de prueba, un DNI ficticio y una localidad existente. Usá una clave temporal propia y aceptá los términos.
2. Esperado: volver al login con el aviso de cuenta creada, sin sesión iniciada.
3. En la ventana administrativa ingresá con la cuenta administrativa de tu base.
4. Abrí Gestión de usuarios y buscá el email exacto. Comprobá nombre, apellido, ID y correo. Abrí Editar para revisar los datos del perfil.
5. Abrí el historial de avisos y verificá “Nuevo usuario registrado: ...”.
6. Abrí Correos y filtrá por el email. Debe haber bienvenida al usuario y aviso a la dirección administrativa; pueden estar pendientes, en reintento o enviados.
7. Ejecutá las consultas del archivo consultas-circuito-usuarios.sql en pgAdmin, reemplazando solamente el email de prueba.

### Login, perfil y permisos

1. En la ventana del usuario intentá entrar con una clave equivocada: debe aparecer un error sin iniciar sesión.
2. Ingresá correctamente por nombre. Cerrá sesión y repetí usando el email.
3. Revisá Mi perfil, historial vacío si no compraste, dirección y teléfono.
4. Modificá dirección/teléfono y guardá. Volvé a consultar PostgreSQL: deben coincidir con la pantalla.
5. Intentá abrir /admin/usuarios y /admin/correos con esa sesión: debe denegarse el acceso.
6. Cerrá sesión y volvé a abrir el panel del usuario: debe exigir autenticación.
7. Probá registrar otra vez el mismo nombre, correo o DNI: debe rechazarse sin agregar cuentas ni correos.
8. Tené en cuenta los hallazgos de “Última conexión” y datos de sesión desactualizados al editar desde administración.

### Recuperación y cierre

1. Desde el login elegí Olvidé mi contraseña.
2. Indicá el email de prueba: la respuesta debe ser genérica, exista o no la cuenta.
3. Si SMTP está configurado, abrí el correo y luego el enlace en la misma computadora donde corre Spring. Localhost en el celular apunta al celular y no a tu computadora.
4. Definí una clave nueva y repetila. Debe regresar al login con aviso de cambio.
5. La clave anterior debe fallar y la nueva debe funcionar. Volver a usar el enlace debe mostrar que no es válido.
6. Revisá el aviso de cambio de contraseña y las filas de PostgreSQL.
7. Para revisar vencimiento real sin tocar la base, esperá más de 30 minutos antes de utilizar un enlace nuevo.
8. Eliminá solo la cuenta ficticia si no tiene compras y ya terminaste. No borres cuentas o historiales reales.

## Repetir la verificación automatizada

Ver docs/NOTIFICACIONES.md para iniciar la instancia independiente. Las pruebas requieren la base descartable pequelandia_verificacion y el usuario pequelandia_test en PostgreSQL local.

En PowerShell:

    $env:PEQUELANDIA_TEST_DB_URL='jdbc:postgresql://127.0.0.1:55439/pequelandia_verificacion'
    .\mvnw.cmd -Dtest=CircuitoUsuariosIntegrationTest test
    .\mvnw.cmd test
    node --test src/test/js/seguridad.test.cjs

Cada prueba recrea sus datos de trabajo; el esquema de negocio de esa base se elimina al cerrar el contexto. Las credenciales ficticias que aparecen en el código solo pertenecen a las pruebas, no sirven para ingresar a tu base habitual.

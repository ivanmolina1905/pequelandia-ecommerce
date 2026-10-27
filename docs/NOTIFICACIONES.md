# Pedidos, stock y notificaciones

## Flujo transaccional

`PedidoService` concentra la compra, los cambios de estado y las cancelaciones. Carrito, detalles, stock, pedido, avisos internos y correos pendientes se guardan en una misma transacción. Si falla la persistencia de cualquiera de esas partes, se revierte la operación completa.

Se conservan ambas rutas de solicitud de cancelación (`/pedido/{id}/cancelar` y `/panelusuarios/{id}/cancelar`), el cambio administrativo y la confirmación de cancelación. Todas delegan en el servicio. La solicitud verifica titularidad; la confirmación exige administrador y una solicitud previa. El administrador también puede cancelar directamente desde el cambio de estado.

Los pedidos y productos se bloquean durante la operación. Los productos se procesan en orden de ID. Se refrescan los valores después de adquirir el bloqueo para no reutilizar un estado viejo cargado por la vista. Repetir el mismo estado no vuelve a modificar stock ni generar avisos. Los carritos confirmados quedan protegidos contra cambios de cantidades.

| Estado actual | Cambios permitidos |
| --- | --- |
| PENDIENTE | PREPARANDO, CANCELACION_SOLICITADA, CANCELADO |
| PREPARANDO | ENVIADO, ENTREGADO, CANCELACION_SOLICITADA, CANCELADO |
| ENVIADO | ENTREGADO, CANCELACION_SOLICITADA, CANCELADO |
| CANCELACION_SOLICITADA | PREPARANDO, ENVIADO, CANCELADO |
| ENTREGADO / CANCELADO | Solo repetir el mismo estado, sin efectos adicionales |

Solicitar cancelación no repone stock. La reposición ocurre al pasar efectivamente a CANCELADO. Una compra repetida para el mismo carrito devuelve el pedido existente. El carrito en sesión se serializa por sesión durante sus modificaciones y el checkout, evitando pedidos duplicados por doble clic dentro de la misma instancia. Una futura instalación con sesiones distribuidas necesitará una clave persistente de checkout compartida entre instancias.

## Administración y formularios

Configurar `app.security.admin-id` en el archivo local o `PEQUELANDIA_ADMIN_ID` con el ID de la cuenta administrativa. Los permisos se comparan por ID, no por el nombre editable.

Por compatibilidad, si vale 0 se busca una única cuenta llamada `admin` al iniciar y se fija su ID en memoria. Sin esa cuenta no se habilita administración; con varias cuentas homónimas se rechaza el arranque y se debe configurar el ID. El nombre `admin` queda reservado. Para renombrar al administrador del modo compatible, primero hay que fijar su ID en configuración. No hay una migración de roles ni cambios en las contraseñas.

Las peticiones POST requieren el token CSRF de la sesión, enviado como `_csrf` en formularios o `X-CSRF-TOKEN` en fetch. Las vistas y `static/js/seguridad.js` lo incorporan. Los permisos se aplican también a PDF, consultas JSON, exportaciones y las rutas alternativas de paneles. Las respuestas de error incluyen un mensaje sin exponer consultas SQL.

## Campanitas

Se mantienen los destinatarios `ADMIN` y `USUARIO_<id>`. Las lecturas se guardan en `NotificacionLectura`, con inserciones que toleran conflictos de unicidad. La entidad `Notificacion.java` permanece intacta.

La campanita muestra los últimos 12 avisos y cuenta todas las notificaciones no leídas. Se actualiza al abrirla, al volver a la pestaña y cada 30 segundos mientras está visible. Al pulsar un aviso se marca como leído y se abre el pedido relacionado mediante el mismo endpoint protegido del historial.

## Historial y panel administrativo

El historial `/notificaciones/historial` está disponible para clientes y administrador desde sus paneles y desde «Ver todas las notificaciones» en la campanita. Muestra 20 avisos por página, filtros por leídas/no leídas, lectura individual o de todos los avisos propios, y un detalle del pedido con enlace a PDF. El destinatario siempre se obtiene de la sesión validada: nunca se acepta un ID de usuario ni un destinatario enviado por el navegador. El administrador ve el destinatario `ADMIN`; los clientes ven exclusivamente `USUARIO_<id>`.

El panel `/admin/correos` permite buscar por destinatario o asunto y filtrar entre pendientes, reintentos pendientes y enviados. Muestra intentos y fecha de envío o próximo intento. “Enviado” significa aceptado por SMTP; no confirma recepción o lectura. Si la entrega automática está desactivada, la pantalla lo informa.

«Reintentar ahora» solo adelanta el próximo intento de una fila que ya falló y todavía tiene una espera futura. No crea filas, no borra el contador, no envía correo dentro de la petición HTTP y no reenvía correos ya aceptados. La actualización condicional se serializa con el bloqueo del worker. Las peticiones concurrentes para una misma espera solo programan un adelanto; cuando ya está listo se muestra «En cola». El worker conserva sus reintentos automáticos. Todas las acciones usan POST con CSRF y el panel de correos exige administrador tanto en el interceptor como en el controlador.

No se modifica `Notificacion.java` ni se agrega una migración de tablas para estas pantallas. Se reutilizan `notificacion`, `notificacion_lectura` y `correo_pendiente`.

## Seguimiento de pedidos y stock mínimo

El JSON protegido `/pedido/{id}` incluye `historial`, ordenado por registro, con estado y fecha. La compra registra el estado inicial y cada transición efectiva agrega un evento en la misma transacción, bajo el bloqueo del pedido. Repetir el estado no agrega eventos; un fallo revierte estado, stock, historial y avisos. El detalle del carrito, los paneles de cliente/administrador y el historial de notificaciones muestran la línea de tiempo. Los pedidos antiguos sin eventos se identifican como tales; en su primer cambio se conserva el estado previo con fecha desconocida, sin inventar fechas anteriores. Las fechas nuevas corresponden al momento real del cambio.

`app.stock.minimo` / `PEQUELANDIA_STOCK_MINIMO` configura un umbral global no negativo (5 por defecto). Al crear o guardar un producto, comprar o reponer por cancelación, se evalúa el stock. Si queda en el umbral o por debajo, un aviso `ADMIN` y un correo a `app.admin.email` identifican producto, existencias y umbral. No se muestran a clientes. Los productos inactivos o discontinuados no generan alertas. No se realiza un barrido del inventario histórico al iniciar: los productos existentes se evalúan al modificarse o participar en estas operaciones.

Se avisa una sola vez mientras persiste el stock bajo. Las compras sucesivas y concurrentes no duplican la alerta. Reponer por encima del umbral desactiva la marca y permite otro aviso si vuelve a bajar. Guardar un producto con stock negativo se rechaza. La marca y el aviso se guardan en la misma transacción; si falla el correo pendiente, se revierte también la modificación de stock.

El script aditivo `db/notificaciones.sql` crea `pedido_historial` y `producto_alerta_stock` e índices, sin cambiar `Notificacion.java`. Estas tablas usan identificadores del negocio; los servicios eliminan sus registros auxiliares al eliminar el pedido o producto. Las pruebas aisladas también limpian ambas tablas para no reutilizar marcas entre casos.

## Entrega de correos

`src/main/resources/db/notificaciones.sql` crea `correo_pendiente`, `pedido_historial`, `producto_alerta_stock` y sus índices de forma idempotente al arrancar. No modifica las tablas de pedidos ni notificaciones. El usuario de PostgreSQL debe tener permiso para crear estas tablas; si se aplica el script por separado, se puede desactivar la inicialización automática con `spring.sql.init.mode=never`.

Los mensajes se guardan antes del commit de la operación. El worker solo consume filas confirmadas y usa `FOR UPDATE SKIP LOCKED` para que dos workers no envíen simultáneamente la misma fila. Cada lote procesa hasta 10 mensajes. Los errores SMTP se reintentan con espera creciente, desde 30 segundos hasta una hora. Los mensajes enviados se conservan con `enviado_en`.

- `app.mail.delivery-enabled` / `PEQUELANDIA_MAIL_DELIVERY_ENABLED`: habilita la entrega; `false` conserva la cola sin conectarse a SMTP.
- `app.mail.poll-ms`: intervalo entre lotes, 5000 ms por defecto.
- `CorreoService.enviar()` devuelve si el mensaje fue aceptado en la cola; no certifica que haya llegado al buzón.

SMTP no garantiza entrega exactamente una vez: si el servidor acepta un mensaje y el proceso falla antes de registrar el envío, el reintento puede repetir ese correo. La deduplicación de las acciones de pedidos evita generar filas repetidas por solicitudes repetidas; no elimina esa ventana propia del protocolo.

## Pruebas reproducibles

Las pruebas de integración usan exclusivamente una base local descartable llamada `pequelandia_verificacion`, con usuario `pequelandia_test`. Crean y eliminan el esquema de negocio y usan datos ficticios. No apuntar una base existente a este nombre para ejecutar pruebas.

```powershell
# Con un PostgreSQL de pruebas independiente ya iniciado y la base vacía creada:
$env:PEQUELANDIA_TEST_DB_URL='jdbc:postgresql://127.0.0.1:55439/pequelandia_verificacion'
.\mvnw.cmd test
```

Sin esa variable, se ejecutan los tests unitarios y se omiten explícitamente los de integración. `IntegrationSupport` sobrescribe la configuración local de conexión, desactiva el worker de correo y valida el nombre y host de la base. Las pruebas de entrega sustituyen SMTP por un mock.

Se prueban permisos HTTP, CSRF, registro, renderizado Thymeleaf, PDF, compra y cancelación concurrentes, rollback ante fallos, congelación de carritos históricos, unión del carrito invitado, lecturas concurrentes y reintentos de correo. La entrega a buzones reales y un recorrido visual completo en navegador requieren una comprobación adicional con la configuración del entorno.

Verificación realizada el 16 de octubre de 2026: 28 pruebas Java aprobadas, sin fallos ni omisiones, usando Java 22 y PostgreSQL 18.4 aislado. Otras 4 pruebas JavaScript verifican envío del token CSRF, conservación de headers, ausencia de filtración del token a otros sitios y escape de texto. Se ejecutan con `node --test src/test/js/seguridad.test.cjs`, sin instalar paquetes.

Verificación del historial y panel de correos (22/10/2026): 30 pruebas Java y 4 JavaScript aprobadas. Se comprobó en navegador el acceso desde ambos paneles, filtros, lectura individual, detalle del pedido y reintento manual. Se usaron datos ficticios, PostgreSQL aislado y SMTP desactivado durante la revisión visual. Para esa instancia se utilizó el conector NIO2 temporal por el fallo local de sockets de Java 22; no se cambió el arranque habitual de Eclipse.

Verificación del seguimiento y stock mínimo (26/10/2026): 34 pruebas Java y 4 JavaScript aprobadas. Incluyen permisos, historial sin duplicados, fechas anteriores desconocidas, alertas concurrentes, reposición y rollback. Se corrigió además el reintento manual de correos para comparar con `clock_timestamp()` y evitar que una transacción antigua vuelva a adelantar una fila ya programada; su regresión determinista se ejecutó también por separado. En navegador se verificaron preparación, envío y entrega con horario argentino y el aviso administrativo de 4 unidades con umbral 5. Se usaron datos ficticios, PostgreSQL aislado, SMTP desactivado y el conector NIO2 temporal, sin alterar Eclipse.

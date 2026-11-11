# Correcciones de estadísticas y validaciones — 02/11/2026

Rama: `fix/estadisticas-validaciones-comerciales`.

## Cambios

- Ventas diarias, mensuales y anuales, gasto total y series personales usan el precio congelado en `detalle_carrito.precio`.
- Los importes y unidades netos excluyen pedidos CANCELADO. Los conteos generales de pedidos conservan todos los estados para administración.
- El ranking exige que exista un pedido no cancelado; no incluye carritos abiertos.
- ENVIADO tiene etiqueta, icono, color, ayuda y lugar en el flujo sugerido del panel administrativo.
- Formulario y API del catálogo comparten validaciones de existencia, estado activo, precio finito no negativo, cantidad positiva y stock acumulado. La comparación evita desbordamientos y la validación y el agregado se realizan bajo el bloqueo del carrito.
- Alta y edición de productos por servicio rechazan precio/costo negativos o no finitos, además del stock negativo. Los campos de precio del panel incluyen mínimo cero.
- Los rangos diarios y anuales invertidos se rechazan desde el servicio compartido por estadísticas y exportaciones. Las fechas de exportación se convierten en la entrada y los formatos inválidos devuelven HTTP 400.

## Regresiones

Se conservaron activas las pruebas fallidas anteriores. Se agregaron casos para precio histórico y cancelación en todas las series, ranking y gasto personal, rangos invertidos en exportaciones y estadísticas, agregados acumulados y desbordamientos, productos no comercializables en ambas entradas e importes inválidos al crear y editar. También se verifica la ayuda de ENVIADO.

## Alcance

Las pruebas de integración utilizan únicamente PostgreSQL local descartable `pequelandia_verificacion`, con entrega de correo desactivada y datos ficticios. No se modificó la configuración de Eclipse ni la base habitual. No se agregaron restricciones ni se ejecutaron migraciones sobre datos existentes.

No se hizo commit ni push: había numerosos cambios previos, incluida documentación y pruebas sin versionar, que se conservaron. Mensaje propuesto para un commit aislado: `fix: corregir estadísticas históricas y validaciones comerciales`.

Los hallazgos anteriores de email, última conexión, perfil de sesión y otros detalles del catálogo no forman parte de estas correcciones. Las caracterizaciones existentes de usuarios siguen reproduciendo esos defectos; que aprueben no significa que estén corregidos.

## Resultado de verificación

- Maven offline con Java 22: **129 casos Java aprobados, 0 fallos, 0 errores, 0 omitidos; BUILD SUCCESS**.
- Node: **6 casos JavaScript aprobados, 0 fallos**.
- `git diff --check`: sin errores de espacios.
- Total: **135 casos automatizados aprobados**. No se realizó una nueva revisión visual en navegador ni una prueba sobre la base habitual.

Los primeros intentos de Java no pudieron conectar con PostgreSQL por las restricciones del entorno y la duración del proceso del servidor. La ejecución final inició el servidor y ejecutó la suite completa en el mismo proceso, con acceso local habilitado. El servidor descartable se detuvo al terminar.

Log local de la ejecución válida: `.tools/correcciones-verificadas.log`. Los logs y la base permanecen excluidos de Git.

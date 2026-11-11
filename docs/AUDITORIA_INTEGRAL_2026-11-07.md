# Auditoría integral del proyecto — 7 de noviembre de 2026

## Alcance y cómo interpretar el informe

Se revisó el estado local actual del proyecto, incluidos sus cambios sin commit: 98 archivos Java de producción, 19 plantillas HTML, los 3 JavaScript propios, los 5 CSS propios, repositorios/consultas, entidades, configuración, Maven, Docker, documentación y pruebas. Bootstrap se trató como dependencia de terceros; sus variantes minificadas no se consideran código propio sin uso por el mero hecho de no tener llamadas Java.

Se combinaron lectura de código, búsquedas de referencias, análisis sintáctico de scripts, ejecución de JavaScript en Node y pruebas Java/MockMvc contra un PostgreSQL nuevo y descartable. No se modificó la lógica de producción, no se utilizaron datos reales y no se hizo commit ni push. Se añadieron ocho pruebas que expresan comportamientos esperados para poder corregir los hallazgos después.

**Una revisión amplia no demuestra que se hayan ejecutado todas las combinaciones posibles.** En esta sesión no se realizó un recorrido nuevo de todas las pantallas en navegador, ni pruebas de carga sostenida, ni auditoría externa de vulnerabilidades de dependencias, ni envío a buzones reales. Las verificaciones visuales de octubre y noviembre ya documentadas no se presentan como ejecutadas nuevamente.

Etiquetas utilizadas:

- **Ejecución:** reproducido con pruebas en esta auditoría.
- **Código:** causa identificada en las fuentes; no se afirma una reproducción visual o concurrente.
- **Riesgo/decisión:** falta una regla de negocio o una prueba adicional antes de definir la solución.
- **Limpieza:** elementos sin consumidores encontrados o redundantes; hay que preservar los puntos de entrada de Spring, JPA y Thymeleaf.

## Resultado de las verificaciones

La suite existente tiene **134 casos Java**. Ejecutada con una JVM separada por clase, pasó completa. La primera ejecución conjunta tuvo fallos internos de Mockito al resolver sus mocks; no eran 35 errores funcionales de la aplicación. La ejecución con aislamiento eliminó esas interferencias.

Al añadir las ocho regresiones iniciales se ejecutaron **142 casos: 135 aprobados, 7 fallidos, 0 errores y 0 omitidos**. Se afinó después el escenario de alta administrativa y se volvió a ejecutar únicamente la clase nueva: **8 casos, 8 fallidos, 0 errores y 0 omitidos**. El octavo fallo demuestra que se guarda un usuario con email inválido cuando se completan los demás datos. El estado comprobado es, por tanto, **134 casos existentes aprobados y 8 regresiones nuevas fallidas**, mediante esas dos ejecuciones; no se inventa una tercera ejecución agregada.

Los **7 tests JavaScript existentes aprobaron**. Los scripts inline analizados con `vm.Script`, excluyendo comentarios HTML, no presentaron errores de parseo. Esto no detecta funciones inexistentes en ejecución: por ejemplo, el inicio tiene JavaScript sintácticamente válido que invoca `MiniCart` sin definirlo.

**Compilación desde cero: aprobada.** Se compilaron los 98 archivos Java de producción y los 13 archivos Java de pruebas con javac y `release 22`, usando una salida nueva en `.tools/auditoria-20261107-compilacion/`. Para ello se creó una copia temporal del POM con las rutas originales de fuentes; el POM real no se modificó. Log: `.tools/auditoria-20261107-compilacion.log`.

El intento previo de `clean test` offline no pudo ejecutar `maven-clean-plugin` por una dependencia faltante de la caché local (`org/codehaus/plexus/util/Os`). Se resolvió la verificación de compilación con esa salida nueva. El problema del plugin de limpieza no es un error de sintaxis del proyecto.

Entorno: JDK 22, Maven local 3.9.11, PostgreSQL 18, base `pequelandia_verificacion` en **127.0.0.1:55447**, SMTP simulado. La base pertenece exclusivamente a esta auditoría.

## Correcciones de prioridad alta

### 1. El inicio no puede ejecutar los botones de compra rápida

**Código + ejecución JS.** `src/main/resources/templates/indexv1.html:266`, `:268` y `:277` llaman a `MiniCart.dec`, `MiniCart.inc` y `MiniCart.agregarApi`, pero la página no define `MiniCart` ni carga un script que lo defina. Al evaluar sus scripts en Node con un DOM mínimo, `typeof MiniCart` resulta `undefined`.

**Consecuencia:** los botones de cantidad y agregar del inicio tienen una referencia inexistente. Extraer el componente funcional del catálogo a un JS compartido, cargarlo en ambas páginas y comprobar agregar, stock insuficiente, cantidades y errores de red. No copiar otra vez toda la implementación inline.

### 2. El listado JSON de usuarios se rompe con relaciones reales

**Ejecución.** `controller/UsuarioController.java:27` devuelve directamente `List<Usuario>`. Las entidades exponen relaciones bidireccionales: usuario → carritos/pedidos → usuario y pedido → notificaciones → pedido. Con una compra ficticia, `/usuarios/traer` respondió con un cuerpo que Jackson no pudo volver a parsear; la prueba detectó JSON inválido alrededor del carácter 91491.

**Corrección:** devolver DTOs explícitos y paginados; incluir solamente campos necesarios. Mantener excluida la contraseña. Una prueba sobre un usuario sin relaciones, como la existente, no cubre este fallo. Revisar también exposición innecesaria de datos personales en ese endpoint administrativo.

### 3. La edición administrativa permite duplicar el email de otra cuenta

**Ejecución.** `controller/AdminUsuarioController.java:155` llama a `service/UsuarioService.java:editUsuario`, que asigna email sin consultar `emailEnUso`. La prueba cambió el correo del cliente al del administrador y el cambio se persistió.

**Consecuencia:** login con `Optional<Usuario>` puede encontrar más de una fila, y recuperación exige exactamente una cuenta por email, por lo que puede dejar de enviar enlaces. Unificar normalización y validación en el servicio; añadir unicidad en base mediante una migración después de resolver duplicados existentes. Definir también la unicidad de DNI. Los controles previos del registro público no resuelven carreras concurrentes ni las altas administrativas.

### 4. Las validaciones de cuentas están repartidas y no son equivalentes

**Ejecución + código.** `LoginController.java:218` y `PanelUsuarioController.java:225` aceptan cualquier texto que contenga `@`. Existe una prueba que registra `incompleto@` y aprueba porque caracteriza ese defecto. La nueva prueba administrativa creó una tercera cuenta con email `correo-invalido`, aunque se esperaba que continuaran solamente las dos cuentas iniciales. El DTO de registro y la entidad `Usuario` no tienen restricciones de Bean Validation que resuelvan esto. El `@Valid` del alta administrativa no equivale por sí solo a validar los campos.

**Corrección:** DTOs específicos para registro, alta y edición; validar email completo, longitudes, DNI, localidad existente, fechas y campos requeridos desde una capa común. La contraseña del registro exige 6 caracteres y la recuperación 8–128: definir una regla uniforme. La edición administrativa debe manejar `BindingResult` y mostrar errores en el formulario.

### 5. La edición de stock puede sobrescribir una compra concurrente

**Código; carrera pendiente de reproducción controlada.** `AdminProductoController.java:131` lee el producto y lo modifica antes de entrar a `ProductoService.saveProducto`. Ese servicio adquiere el bloqueo en `:50`, pero guarda el objeto previamente leído; no hay `@Version` en `Producto` ni comprobación de la versión que editó el administrador.

**Escenario:** formulario muestra stock 10; una compra baja a 9; guardar el formulario con 10 puede reponer de hecho una unidad vendida. El bloqueo de checkout no detecta que el formulario es antiguo. Realizar la actualización dentro de una transacción, definir ajuste absoluto frente a movimiento de inventario y rechazar versiones obsoletas. Añadir una prueba que intercale lectura del formulario, compra y guardado.

### 6. Se muestran descuentos y cuotas que el pedido no modela

**Código/decisión comercial.** `indexv1.html` calcula un precio con 10 % de descuento y tres cuotas; `producto_detalle.html` también muestra `p.precio * 0.90` para transferencia. `PedidoService.finalizarCompra` calcula el total a partir del precio completo y no registra medio de pago, descuento aplicado ni plan de cuotas.

**Corrección:** definir qué importe se ofrece y en qué momento se aplica. Si esas condiciones son reales, representarlas y validarlas en servidor y conservarlas en el pedido. Si son contenido de demostración, retirarlas o explicarlas en el flujo. No basta con cambiar el total en JavaScript.

### 7. No se revocan las otras sesiones al restablecer una contraseña

**Código.** `RecuperacionController.java:93–97` invalida solamente la sesión del navegador que envía el restablecimiento. `IdentidadService.exigirUsuario` verifica que el usuario exista, pero no comprueba versión de credenciales ni una fecha de revocación.

**Consecuencia:** otra sesión ya iniciada conserva acceso. Añadir versión/fecha de credenciales, guardarla en sesión y compararla en cada operación protegida; incrementar al restablecer o cambiar contraseña. Probar con dos sesiones distintas.

### 8. Login y registro carecen de límites de intentos

**Código.** La recuperación tiene límites persistentes, pero `/auth/login` y `/auth/registro` no tienen una protección equivalente. CSRF y renovación del ID de sesión no limitan intentos de contraseña ni creación repetida de cuentas.

**Corrección:** límites por IP y cuenta, respuestas consistentes, registro de eventos sin credenciales y pruebas de recuperación del límite. Evitar bloqueos permanentes que permitan impedir el acceso a otra persona.

### 9. Las contraseñas permanecen en texto plano por instrucción vigente

**Riesgo aceptado explícitamente en `AGENTS.md`.** La comparación de login y el almacenamiento siguen usando texto plano. No se cambió ni se propone modificarlo automáticamente contra esa instrucción. Registrar esta limitación como condición para no utilizar credenciales reales; si el propietario cambia el alcance, preparar una migración a almacenamiento adecuado y revisar todas las rutas que modifican contraseñas.

## Errores funcionales y coherencia de circuitos

### 10. Cambiar la imagen de un producto no guarda el cambio

**Ejecución.** El formulario tiene `name="imagenUrl"` (`paneladministracion.html:1828`), pero `AdminProductoController.editar` no copia esa propiedad. La prueba envió `/imagenes/nueva.png` y la base conservó `/imagenes/anterior.png`. Además, el script del modal no precarga `prodImagen`.

**Corrección:** cargar el valor actual y persistir la edición con una política explícita para borrar la imagen. Validar longitudes y tipos de URL aceptados.

### 11. Los discontinuados aparecen como productos disponibles

**Ejecución + código.** `IProductoRepository.buscarPublico` y `obtenerAleatorios` excluyen solamente `inactivo`; `ProductoController.java:22` hace lo mismo. Agregar al carrito exige `activo` y checkout rechaza `discontinuado`.

**Consecuencia:** catálogo/inicio/detalle ofrecen artículos que después se rechazan al comprar. Unificar el predicado de disponibilidad; si se quiere mostrar un discontinuado, debe verse claramente como no comprable y no ofrecer el botón habitual.

### 12. Ordenar y paginar borra filtros del catálogo

**Código.** `verproductos.html:222` conserva búsqueda y categoría en el formulario de orden, pero omite `precioMin`, `precioMax` y `talle`. Los enlaces de paginación en `:368`, `:375` y `:380` también los omiten.

**Corrección:** conservar todos los filtros, tamaño y orden; reiniciar la página al cambiar un filtro. Añadir un caso con varias páginas y precio/talle activos, no solamente comprobar que el primer resultado esté filtrado.

### 13. Los alias del carrito renderizan una vista sin sus datos

**Ejecución.** `HomeController.java:74` devuelve `carrito` para `/carrito` y `/carrito.html` sin cargar `detalles`, `items` ni `total`. `/carrito/vercarrito` sí prepara ese modelo. La prueba con dos unidades detectó la ausencia del atributo `detalles`.

**Corrección:** redirigir ambos alias a la ruta canónica, o delegar en el mismo constructor de modelo. No mantener dos formas diferentes de abrir el carrito.

### 14. Modificar cantidad agrega una línea que no existía

**Ejecución/contrato de API.** `CarritoController.apiCantidad` usa `cart.set` sin exigir una línea previa. En una sesión vacía, enviar producto válido y cantidad 2 crea un artículo. El flujo de agregar sí tiene validaciones de estado que esta ruta no aplica.

**Corrección:** decidir si este endpoint es un upsert o una modificación. Para el comportamiento presentado en la UI, exigir línea existente y compartir la validación de disponibilidad. Si se conserva el upsert, documentarlo y aplicar las mismas reglas de agregar.

### 15. La última conexión nunca se actualiza al ingresar

**Ejecución.** `LoginController.procesarLogin` autentica y abre sesión sin guardar `ultima_conexion`. La nueva prueba esperaba un valor y encontró `null`; la suite anterior ya caracteriza esta ausencia.

**Corrección:** guardar fecha/hora al autenticar correctamente, sin actualizarla en intentos fallidos. Definir zona horaria y probar dos ingresos sucesivos.

### 16. El perfil sigue mostrando datos antiguos después de una edición administrativa

**Ejecución en la suite existente.** `PanelUsuarioController.java:121` obtiene `um` desde la base, pero en `:140` entrega `u`, la copia antigua de sesión. `AccessInterceptor` también consulta el usuario actual sin refrescar esa copia. Una caracterización verifica expresamente que se muestra la dirección antigua.

**Corrección:** usar el usuario actual para construir el modelo y actualizar el dato mínimo necesario en sesión. Conservar pruebas de cuenta eliminada y de permisos.

### 17. El modal del carrito muestra subtotal bajo “Precio Unitario”

**Código.** `carrito.html:455` titula la columna “Precio Unitario”, mientras `:633` imprime `i.subtotal`. Para 2 unidades de 12,50 muestra 25 como precio unitario.

**Corrección:** imprimir `i.precio` y añadir subtotal, o renombrar la columna. Conciliar la misma compra en carrito, modal de usuario, modal administrativo, PDF y Excel.

### 18. El botón Comprar del modal del inicio no tiene acción

**Código.** El botón en `indexv1.html:457` no tiene enlace, manejador ni identificador de producto conectado a una operación. Es distinto de la compra rápida afectada por `MiniCart`.

**Corrección:** llevar al detalle del producto o incorporar una acción definida de agregar. Comprobar que el producto del modal coincida con el agregado y que el cierre sea accesible.

### 19. Los precios usan float y conversiones distintas según pantalla

**Código.** `Producto.precio/costo` y `Detalle_carrito.precio` usan `float`; la compra construye `BigDecimal` desde `Float.toString`, mientras carrito/API convierten el float promovido a double con `BigDecimal.valueOf`. Las consultas agregadas y DTOs vuelven a usar double.

**Corrección:** usar `BigDecimal`/NUMERIC con escala y redondeo definidos de extremo a extremo. Agregar pruebas con 0,10; 19,99; múltiples unidades y montos grandes. Varias vistas además formatean importes con cero decimales: no ocultar centavos que luego aparecen en documentos.

### 20. El historial no conserva una copia de todos los datos de la compra

**Código/decisión.** Se congela el precio, pero los detalles/PDF leen el nombre actual de `Producto`; comprador, email y dirección se obtienen de `Usuario`. Renombrar un producto o modificar la cuenta cambia la descripción de documentos históricos.

**Corrección:** definir qué debe permanecer inmutable y almacenar una instantánea de identificación del producto y datos de facturación/entrega necesarios. No confundir un PDF de pedido con un comprobante fiscal.

### 21. El promedio de compra mezcla ventas netas con pedidos cancelados

**Código/definición.** `PanelUsuarioService.java:35–52` excluye cancelados del gasto a través de la consulta de ventas, pero divide por `countByUsuario_Id`, que cuenta todos los pedidos. Una compra de 100 y otra cancelada produce promedio 50 si se usa ese denominador.

**Corrección:** definir si el indicador es promedio de compras vigentes o de pedidos registrados. Usar estados coherentes y un nombre que explique el indicador. También revisar si “ventas” debe incluir todos los pedidos no cancelados aunque no exista pago confirmado.

### 22. Orden del historial y último pedido tienen empates sin resolver

**Código.** `PedidoService.java:239` guarda `java.sql.Date.valueOf(LocalDate.now())`, perdiendo la hora. `findByUsuarioId` ordena por fecha sin desempate por ID; el panel usa esa consulta para “último pedido”.

**Corrección:** conservar un instante de creación y ordenar por fecha e ID. Si se mantiene solo fecha, añadir al menos el desempate estable. Probar varias compras el mismo día y el paso entre páginas.

### 23. La eliminación de usuarios no tiene un circuito de conflictos amigable

**Código.** `AdminUsuarioController.java:169` elimina directamente. Con compras/carritos asociados puede fallar por claves foráneas; no tiene un tratamiento como el de eliminación de productos. El código tampoco impide explícitamente eliminar al único administrador.

**Corrección:** definir baja lógica o conservación de histórico, mostrar un mensaje de negocio y proteger la continuidad de administración. Probar usuario sin compras, con compras, con recuperación activa y administrador actual. No usar borrado en cascada como solución automática. La operación interna `PedidoService.deletePedido`, hoy sin ruta activa encontrada, también debe considerar comprobantes/notificaciones referenciados antes de habilitarse.

### 24. Los estados se repiten y el rechazo de cancelación pierde contexto

**Código/decisión.** Estados y normalización están duplicados en Java, SQL y varios JS. Desde `CANCELACION_SOLICITADA` se permite volver tanto a PREPARANDO como a ENVIADO sin recordar formalmente cuál era el estado previo; se podría hacer retroceder un pedido enviado.

**Corrección:** centralizar estados/transiciones, conservar el estado previo o separar solicitud de cancelación del estado logístico. Alinear las opciones de la UI con transiciones permitidas; hoy el servidor termina rechazando opciones que el selector ofrece.

### 25. Respuestas y errores de API son inconsistentes

**Código.** Cantidad/agregado/notificaciones capturan excepciones y devuelven HTTP 200 con `ok=false`; otros métodos usan 400/403/404. `Map.of("error", e.getMessage())` puede fallar si el mensaje es null. Algunos `RuntimeException` no están cubiertos por `ErroresApi`; el advice global también devuelve JSON para formularios HTML.

**Corrección:** contrato uniforme con estados HTTP y mensajes seguros, diferenciar presentación HTML/API y registrar causas internas sin exponer SQL. Manejar conversiones, registros inexistentes, integridad y errores inesperados. Probar productos borrados que permanecen en un carrito de sesión.

### 26. Se aceptan filtros y rangos poco consistentes

**Código.** Se valida el rango invertido de estadísticas, pero no el rango de precio del catálogo ni `desde > hasta` del listado administrativo de pedidos. No hay límites razonables para todas las búsquedas, cadenas o rangos anuales.

**Corrección:** normalizar y rechazar entradas inválidas de forma consistente; verificar fechas extremas, NaN/infinito donde el tipo lo admita, cantidades enormes y búsquedas largas. No interpretar una lista vacía por rango invertido como una validación correcta.

## Persistencia, rendimiento y operación

### 27. El catálogo pagina en memoria

**Ejecución.** Hibernate volvió a emitir `HHH90003004`: `firstResult/maxResults specified with collection fetch; applying in memory`. `IProductoRepository.java:50` combina `@EntityGraph(categorias)` con una consulta paginada de colección.

**Corrección:** paginar IDs/productos en SQL y cargar categorías en una consulta separada, manteniendo orden y conteo. Medir con miles de productos; 12 elementos visibles no garantizan 12 filas recuperadas.

### 28. Hay consultas adicionales por fila y por notificación

**Código.** `NotificacionService.listarUltimas` consulta lectura para cada aviso. Carrito obtiene cada producto individualmente. Paneles recalculan resúmenes completos incluso al abrir correos/notificaciones, y las exportaciones cargan todas las entidades en memoria.

**Corrección:** obtener productos/lecturas por lote o proyecciones; medir SQL por pantalla; separar indicadores necesarios de los accesorios. Para exportaciones grandes, paginar o transmitir filas y evitar `findAll` + workbook completo + varias copias de bytes. No desactivar open-in-view antes de preparar DTOs y transacciones de lectura.

### 29. Dos sistemas de carrito conviven sin una persistencia de sesión coherente

**Código.** Las operaciones activas usan `CART_SESSION`/`CartSession`. El login aún consulta `CARRITO_ID` y fusiona carritos de base, pero el catálogo actual no llena ese carrito persistido. Checkout crea otro carrito en base a partir del mapa de sesión.

**Consecuencia:** conservar el carrito invitado funciona por continuidad de sesión, pero no equivale a recuperar el carrito desde otro dispositivo o después de logout/vencimiento. Definir el comportamiento deseado y reducir la implementación antigua solo después de comprobar sus consumidores y tests.

### 30. La inicialización de una instalación nueva no es completa ni versionada

**Código.** La configuración principal exige `ddl-auto=validate`, mientras Docker de demostración fuerza `update`. `db/notificaciones.sql` crea tablas auxiliares con `IF NOT EXISTS` pero no mantiene versiones ni modifica estructuras existentes. No hay semilla reproducible de localidades/categorías ni alta inicial del administrador; el registro público reserva `admin` y la identidad se fija al arrancar.

**Corrección:** migraciones versionadas, datos ficticios optativos e instrucciones de bootstrap verificables para la administración. Probar arranque desde cero y actualización desde un esquema anterior; no depender de que la base del desarrollador ya esté preparada. Añadir integridad y limpieza de huérfanos en tablas auxiliares según el diseño aprobado.

### 31. El umbral de stock no coincide entre alertas y estadísticas

**Código.** `AlertaStockService` utiliza `app.stock.minimo` (5 por defecto) y avisa con stock <= umbral. `EstadisticasService.java:94` consulta `obtenerStockBajo(3)`, cuya condición es `< 3`.

**Corrección:** compartir umbral y semántica inclusiva/exclusiva, o mostrar claramente que son indicadores distintos. Aplicar también a Excel de stock bajo y definir si se incluyen productos inactivos.

### 32. La cola de correo necesita una política de fallos y conservación

**Código/riesgo operativo.** `EntregaCorreoService` reintenta indefinidamente y mantiene la transacción y bloqueo mientras hace SMTP. Un envío aceptado seguido de un fallo antes de confirmar la transacción puede repetirse; no existe entrega exactamente una vez por guardar una fila. Los mensajes enviados no se purgan y algunos contienen enlaces de recuperación.

**Corrección:** definir entrega al menos una vez, identificación del mensaje, retención, fallos permanentes y estado agotado/revisión manual. Probar caída después de SMTP y antes del commit, demora SMTP y mensajes inválidos. Una prueba con SMTP simulado no demuestra entrega real.

### 33. Las llamadas periódicas pueden impedir la expiración por inactividad humana

**Código/decisión.** Los paneles consultan notificaciones cada 30 segundos mientras la pestaña está visible. Cada petición vuelve a extender la sesión y `seguridad.js` renueva su temporizador usando `X-Session-Expires`.

**Corrección:** definir inactividad de usuario frente a actividad HTTP automática. Si se espera cierre tras ausencia de interacción, registrar actividad humana o un vencimiento absoluto que el polling no renueve. Probar con una pestaña abierta sin tocar mouse/teclado.

### 34. Las rutas y algunos recursos asumen despliegue en la raíz

**Código.** Hay `fetch('/pedido/...')`, `/productos/...` y redirecciones JS absolutas repartidas por las plantillas. `app.security.public-url` contempla contexto, pero esos enlaces no. Además, `HomeController` conserva alias relativos que no cargan los mismos datos que la ruta original.

**Corrección:** inyectar URLs generadas por Thymeleaf o una base de contexto compartida y probar bajo `/pequelandia`. Para publicación, verificar HTTPS/proxy y cookie Secure según ese despliegue; no se probó una publicación real en esta sesión.

## Interfaz, accesibilidad y sintaxis

### 35. La compra rápida del catálogo permanece oculta para accesibilidad

**Código.** `.peq-quick` conserva `aria-hidden="true"` (`verproductos.html:294`). El manejador solo cambia `quick-open`; los cambios de `aria-hidden` encontrados pertenecen al mini carrito, que es otro elemento.

**Corrección:** sincronizar estado accesible, foco, retorno al botón y cierre con Escape. Revisar teclado y lector de pantalla en inicio y catálogo. No considerar los tests de render HTML como una prueba de accesibilidad completa.

### 36. Hay HTML inválido y controles que aparentan funciones pendientes

**Código.** `indexv1.html:372–377` cierra el contenedor antes de cerrar el enlace (`</div></a>`); el navegador debe reparar ese árbol. Hay enlaces `href="#"` para idioma/modo nocturno sin circuito implementado. Categorías del inicio usan IDs fijos 1, 4, 5, 6, 7, 8 y 9.

**Corrección:** corregir anidamiento, validar HTML renderizado, retirar o implementar controles y obtener categorías desde datos estables. Probar una base nueva cuyos IDs no coincidan con los de desarrollo. Completar etiquetas y textos alternativos de imágenes de navegación.

### 37. La interpolación HTML no se protege en todos los modales

**Código.** `paneladministracion.html:2480` inserta `${p.nombreProducto}` directamente en `tr.innerHTML` en el detalle de compras por usuario. Otros detalles ya escapan el nombre. Un nombre con etiquetas se interpreta como HTML en ese modal.

**Corrección:** crear nodos con `textContent` o usar el escape compartido en todas las interpolaciones. El circuito de carga del nombre es administrativo en la implementación actual; no se afirma que cualquier visitante pueda explotar esa ruta. Probar nombres con `<`, comillas y etiquetas completas en todos los lugares de salida.

### 38. Los gráficos dependen de una URL externa sin versión fija

**Código.** `paneladministracion.html:3383` carga `https://cdn.jsdelivr.net/npm/chart.js`; panel de usuario también depende de Chart.js externo. Sin acceso al CDN, las tablas pueden funcionar pero los gráficos no.

**Corrección:** fijar la versión y política de integridad/carga o servir el recurso aprobado localmente. Mostrar un estado útil si no carga. Esto no es una afirmación de vulnerabilidad conocida en la versión utilizada.

### 39. Falta unificar y reducir CSS/JS inline acumulado

**Limpieza.** El panel administrativo supera 6300 líneas y el de usuario 2400, con estilos sucesivos y bloques comentados de implementaciones previas. El inicio conserva `changeQty`/`removeLine` que llaman `/carrito/api/cantidad` y `/carrito/api/eliminar`, rutas que no existen; tampoco están los elementos de resumen esperados en esa página.

**Corrección:** extraer módulos de pedidos, catálogo, perfiles y avisos, y estilos compartidos. Eliminar bloques muertos después de verificar referencias; conservar el comportamiento real de los modales y responsive. Las búsquedas por selector son candidatos de limpieza, no una autorización para borrar reglas usadas por clases dinámicas.

### 40. La suite conserva pruebas que aprueban defectos conocidos

**Ejecución/código.** Las pruebas `hallazgoEmailIncompletoEsAceptadoActualmente`, `hallazgoUltimaConexionNoSeActualizaAlIngresar` y `hallazgoPerfilAbiertoMuestraDatosAnterioresAEdicionAdministrativa` aprueban mientras los defectos existen. La suite completa puede estar verde sin que esos comportamientos sean correctos.

**Corrección:** convertirlas en regresiones del comportamiento deseado al corregir cada causa; nombrar explícitamente las caracterizaciones mientras continúen. Configurar el aislamiento de Mockito observado y el agente oficial en Maven de forma reproducible. Mantener un perfil de integración cuya omisión sea visible en CI.

## Inventario concreto de limpieza

### DTOs sin consumidores encontrados en producción

`AgrupacionAnioDTO`, `AgrupacionFechaDTO`, `AgrupacionMesDTO`, `ExcelFileDTO`, `ProductoStockBajoDTO`, `ProductoVendidoDTO` y `TopProductoDTO`, todos en `src/main/java/com/pequelandia/pequelandia/dto/`.

No se encontraron referencias de otras clases de producción a esos tipos. Revisar documentación y consumidores externos antes de retirarlos. Las clases de controllers, filtros, scheduler y advice sin referencias directas **no** son código muerto: Spring las descubre por anotaciones.

### Plantillas sin ruta/renderizado encontrado

- `templates/html/carrito.html`: versión de demostración con `/checkout.html` y recursos ficticios.
- `templates/html/index.html`: versión antigua y rutas Bootstrap duplicadas.
- `templates/html/loginbk.html`: formularios antiguos hacia `/auth/register`, `/auth/forgot` y `/auth/reset` sin endpoints correspondientes.
- `templates/html/usuario-editar.html`: edición antigua sin retorno de vista identificado.
- `templates/html/verproductosa.html`: catálogo estático con imágenes ficticias y un ID `filtro2` repetido.
- `templates/pedido_detalle.html`: contiene únicamente un comentario; el detalle actual se entrega como JSON/modal.

Son candidatas a retirar o archivar fuera de las plantillas de ejecución. `historial-notificaciones.html` y `panel-correos.html` **sí** se reutilizan como fragmentos: conservarlos.

### Dependencias y configuración aparentemente sobrantes

No hay imports de Spring AI ni lectura de PDF mediante ese framework en el código propio. Revisar eliminación de `spring-ai-pdf-document-reader`, `spring-ai-spring-boot-docker-compose`, BOM `spring-ai-bom` y propiedad `spring-ai.version`. La función PDF actual usa OpenPDF; Docker Compose de Spring está desactivado. Confirmar con `dependency:analyze` y empaquetado antes de retirar.

El Dockerfile usa JDK/JRE 25 mientras el proyecto declara Java 22. No se marca automáticamente como incompatibilidad, pero debe existir una prueba de build/arranque con el runtime del contenedor. El Dockerfile empaqueta omitiendo tests: el pipeline debe ejecutarlos antes. Limpiar metadatos vacíos del POM sin inventar autor/licencia.

### Servicios/métodos sin consumidores de producción encontrados

- `ComprobanteService.emitir` / `IComprobanteService`: no hay controller ni llamada de compra que lo invoque. Decidir si se integra o se retira. Si se activa, `count()+1` no es una numeración segura ante borrados o concurrencia; requiere secuencia/contabilidad y unicidad apropiadas.
- `ProductoService.editProduto`: el controlador edita manualmente y luego llama a `saveProducto`; consolidar en una operación de edición.
- `PedidoService.savePedido`, `editPedido`, `deletePedido`, `marcarEntregado` y `cambiarEstado`: revisar consumidores de tests y contratos; las rutas activas usan otras operaciones. No borrar `finalizarCompra`, que tiene llamadas internas reales.
- Operaciones persistidas de carrito `guardar`, `actualizarTotal`, `agregarDetalle`, `quitarDetalle`, `cambiarCantidad`, `eliminarLinea`, `items`, `recalcularTotal` y `Detalle_carritoService.listarPorCarrito`: revisar junto a la decisión de unificar los dos carritos. La fusión de login y sus pruebas todavía usan parte de esa infraestructura.
- `IProductoRepository.productosMasVendidos(int)`: consulta antigua que cuenta detalles sin exigir pedido; el dashboard usa la versión corregida de `IPedidoRepository`. Retirar la antigua si no hay consumidores, para no reintroducir el defecto.
- `CartSession.snapshot()` y `getItems()` devuelven ambas copia del mapa; unificar si no se necesita la distinción semántica. Helpers de relaciones `addDetalle/removeDetalle/addCategoria/removeCategoria` no tienen llamadas de producción encontradas; decidir si forman parte del contrato de entidades.

### Inyecciones y consultas evitables

`CarritoController` conserva inyectados `carritoService`, `carritoRepo`, `detalleService`, `pedidoRepo` y `detalleRepo`, aunque el circuito activo opera con `productoRepo`, `pedidoService` y el carrito de sesión. `CatalogoController` conserva colaboradores del carrito persistido que no utiliza. `PanelController.panelAdmin` calcula estadísticas que pierde al redirigir. `PanelUsuarioController.verpedidos` carga pedidos/modelo que también pierde al redirigir.

Eliminar estas dependencias y consultas tras comprobar referencias reales. Sustituir `System.out` de usuarios por logging controlado y evitar imprimir búsquedas con datos personales.

### Imports redundantes y estilo

Se encontraron **27 imports explícitos sin referencias en el código de sus archivos**, descontando comentarios. El anexo lista los candidatos. Hay además autoimport en `CompraService`, `@RequiredArgsConstructor` sobre clases con campos inyectados no final y getters/setters manuales combinados con Lombok.

Elegir una convención y aplicarla gradualmente: clases en PascalCase, campos Java en camelCase, `CompraResumenDTO` en vez de `compraResumenDTO`, `editarProducto` en vez de `editProduto`, nombres sin duplicar aliases `getId_carrito/getIdCarrito`. Mantener los nombres SQL mediante mappings para no cambiar el esquema accidentalmente. Estos nombres son deuda de mantenimiento, no errores de sintaxis.

Los comentarios generados `[Doc]` que explican cada asignación aumentan mucho el volumen y algunos repiten instrucciones obvias o anteriores. Mantener documentación de reglas, transacciones, permisos y decisiones; evitar que un generador sobrescriba comentarios útiles. Regenerar la referencia/documentación únicamente después de estabilizar las correcciones.

## Aspectos que ya están corregidos y no deben reabrirse como defectos actuales

La suite actual volvió a comprobar precio histórico en ventas/gasto, exclusión de cancelados de esas ventas, exclusión de carritos abiertos del top utilizado por el dashboard, rechazo de precios negativos, validación de stock/estado al agregar, rangos invertidos de estadísticas y exportación, detalle `/producto/{id}`, representación administrativa ENVIADO, CSRF, titularidad de pedidos, renovación de sesión al login, reposición idempotente y rollback de compra/notificaciones.

Estos resultados no garantizan todos los bordes posibles, pero evitan arrastrar como vigentes fallos que solo aparecen en informes anteriores.

## Orden propuesto de trabajo

1. Corregir compra del inicio, JSON de usuarios, edición de imagen, duplicación de email y reglas de disponibilidad.
2. Unificar validaciones y revocación de sesiones; definir la política comercial de descuentos y los ajustes concurrentes de stock.
3. Corregir filtros, alias del carrito, última conexión, perfil desactualizado y unitario/subtotal.
4. Migrar importes y preservar datos históricos; estabilizar indicadores y transiciones.
5. Optimizar consultas, versionar esquema y terminar bootstrap/operación de correo.
6. Retirar código sin uso, extraer JS/CSS compartido y completar navegación/accesibilidad.
7. Ejecutar regresiones, recorridos reales de escritorio/móvil, pruebas de volumen y build/arranque de contenedor.

## Evidencia y reproducción

- Nueva clase: `src/test/java/com/pequelandia/pequelandia/AuditoriaIntegral20261107IntegrationTest.java`.
- Log de suite completa con aislamiento: `.tools/auditoria-20261107-final.log`.
- Log de regresiones afinadas: `.tools/auditoria-20261107-regresiones.log`.
- Inventarios auxiliares: `.tools/auditoria-20261107-estatico.json` y `.tools/auditoria-20261107-limpieza.json`. Son búsquedas heurísticas; sus falsos positivos de Spring/overrides/interpolaciones no se trasladan automáticamente a los hallazgos.
- Reportes JUnit de la última ejecución por clase: `target/surefire-reports/`. Se sobrescriben al repetir una clase; el log de la suite completa conserva el resultado agregado anterior.

Para repetir, usar Java 22, Maven disponible y una base **nueva y descartable**, con el nombre y guardas de `IntegrationSupport`. Los tests recrean/vacían tablas y la contraseña del usuario de pruebas está vacía por tratarse de un servidor local aislado. Nunca apuntar a la base habitual.

```powershell
$env:JAVA_HOME='C:/Program Files/Java/jdk-22'
$env:PEQUELANDIA_TEST_DB_URL='jdbc:postgresql://127.0.0.1:55447/pequelandia_verificacion'
# MAVEN_AUDITORIA debe apuntar al mvn.cmd disponible localmente.
& $env:MAVEN_AUDITORIA -o '-DreuseForks=false' test
node --test src/test/js/*.test.cjs
```

La ejecución de esta auditoría agregó `-javaagent` con la copia local del JAR oficial de Mockito para evitar el attach dinámico. El comando de arriba puede requerir ese parámetro según la configuración de Java; debe incorporarse de forma mantenible a Maven.

No se eliminaron archivos antiguos, secretos, configuración local ni cambios previos. Las recomendaciones de este documento son trabajo pendiente, no modificaciones ya aplicadas.


## Anexo: imports candidatos a retirar

| Archivo Java | Import sin uso local encontrado |
| --- | --- |
| `controller/AdminPedidoController.java` | `Sort` |
| `controller/AdminPedidoController.java` | `INotificacionService` |
| `controller/CarritoController.java` | `Carrito_de_compra` |
| `controller/CarritoController.java` | `Detalle_carrito` |
| `controller/CarritoController.java` | `INotificacionService` |
| `controller/LoginController.java` | `ResponseBody` |
| `controller/LoginController.java` | `CorreoService` |
| `controller/LoginController.java` | `INotificacionService` |
| `controller/PanelUsuarioController.java` | `Objects` |
| `controller/PanelUsuarioController.java` | `INotificacionService` |
| `controller/PedidoController.java` | `IProductoRepository` |
| `controller/PedidoController.java` | `Transactional` |
| `dto/ProductoMasVendidoDTO.java` | `AllArgsConstructor` |
| `dto/PuntoSerieDTO.java` | `AllArgsConstructor` |
| `model/Carrito_de_compra.java` | `RoundingMode` |
| `model/Categoria.java` | `OneToMany` |
| `model/Usuario.java` | `LocalDate` |
| `service/CompraService.java` | `SimpleDateFormat` |
| `service/CorreoService.java` | `SimpleMailMessage` |
| `service/CorreoService.java` | `JavaMailSender` |
| `service/Detalle_carritoService.java` | `BigDecimal` |
| `service/Detalle_carritoService.java` | `Usuario` |
| `service/Detalle_carritoService.java` | `HttpSession` |
| `service/EstadisticasService.java` | `YearMonth` |
| `service/ILocalidadService.java` | `Usuario` |
| `service/LocalidadService.java` | `Usuario` |
| `service/NotificacionService.java` | `NotificacionLectura` |

## Anexo: inventario de operaciones HTTP

Cada fila corresponde a un mapping encontrado en el código actual. Su presencia no significa que cada combinación de parámetros esté probada. Las rutas administrativas requieren sesión administrativa; pedidos requieren titularidad o administración; CSRF se comprueba en operaciones de escritura. Los alias se conservan en el inventario para revisar su coherencia.

| Método | Ruta | Controller y línea de declaración |
| --- | --- | --- |
| GET | `/admin/pedidos/lista` | `AdminPedidoController.java:51` |
| POST | `/admin/pedidos/{id}/estado` | `AdminPedidoController.java:163` |
| GET | `/admin/productos` | `AdminProductoController.java:49` |
| POST | `/admin/productos/nuevo` | `AdminProductoController.java:88` |
| POST | `/admin/productos/{id}/editar` | `AdminProductoController.java:124` |
| POST | `/admin/productos/{id}/eliminar` | `AdminProductoController.java:180` |
| GET | `/admin/usuarios` | `AdminUsuarioController.java:91` |
| POST | `/admin/usuarios/{id}/editar` | `AdminUsuarioController.java:155` |
| POST | `/admin/usuarios/{id}/eliminar` | `AdminUsuarioController.java:169` |
| POST | `/admin/usuarios/nuevo` | `AdminUsuarioController.java:184` |
| GET | `/admin/usuarios/{idUsuario}/compras` | `AdminUsuarioController.java:309` |
| GET | `/admin/usuarios/compras/{idCompra}/detalle` | `AdminUsuarioController.java:321` |
| POST | `/carrito/cantidad` | `CarritoController.java:65` |
| POST | `/carrito/eliminar` | `CarritoController.java:140` |
| GET | `/carrito/vercarrito` | `CarritoController.java:186` |
| POST | `/carrito/finalizar` | `CarritoController.java:236` |
| GET | `/productos` | `CatalogoController.java:64` |
| POST | `/productos/agregar` | `CatalogoController.java:171` |
| POST | `/productos/api/agregar` | `CatalogoController.java:196` |
| GET | `/notificaciones/historial` | `CentroAvisosController.java:66` |
| POST | `/notificaciones/historial/{id}/leer` | `CentroAvisosController.java:101` |
| POST | `/notificaciones/historial/leer-todas` | `CentroAvisosController.java:121` |
| GET | `/admin/correos` | `CentroAvisosController.java:140` |
| POST | `/admin/correos/{id}/reintentar` | `CentroAvisosController.java:183` |
| GET | `/admin/estadisticas` | `EstadisticasController.java:36` |
| GET | `/admin/estadisticas/serie-dia` | `EstadisticasController.java:59` |
| GET | `/admin/estadisticas/serie-mes` | `EstadisticasController.java:74` |
| GET | `/admin/estadisticas/serie-anio` | `EstadisticasController.java:87` |
| GET | `/admin/export/usuarios` | `ExcelExportController.java:60` |
| GET | `/admin/export/productos` | `ExcelExportController.java:72` |
| GET | `/admin/export/pedidos` | `ExcelExportController.java:84` |
| GET | `/admin/export/ventas-dia` | `ExcelExportController.java:101` |
| GET | `/admin/export/ventas-mes/{anio}` | `ExcelExportController.java:120` |
| GET | `/admin/export/ventas-anio` | `ExcelExportController.java:135` |
| GET | `/admin/export/top-productos` | `ExcelExportController.java:151` |
| GET | `/admin/export/stock-bajo` | `ExcelExportController.java:167` |
| GET | `/` | `HomeController.java:28` |
| GET | `/indexv1` | `HomeController.java:28` |
| GET | `/indexv1.html` | `HomeController.java:28` |
| GET | `/login` | `HomeController.java:43` |
| GET | `/login.html` | `HomeController.java:43` |
| GET | `/panelusuario` | `HomeController.java:53` |
| GET | `/panelusuario.html` | `HomeController.java:53` |
| GET | `/paneladministracion` | `HomeController.java:64` |
| GET | `/paneladministracion.html` | `HomeController.java:64` |
| GET | `/carrito` | `HomeController.java:74` |
| GET | `/carrito.html` | `HomeController.java:74` |
| GET | `/verproductos` | `HomeController.java:84` |
| GET | `/verproductos.html` | `HomeController.java:84` |
| GET | `/auth/login` | `LoginController.java:50` |
| POST | `/auth/login` | `LoginController.java:66` |
| GET | `/auth/logout` | `LoginController.java:127` |
| GET | `/auth/registro` | `LoginController.java:149` |
| POST | `/auth/registro` | `LoginController.java:174` |
| GET | `/notificaciones/mias` | `NotificacionController.java:49` |
| POST | `/notificaciones/{id}/leer` | `NotificacionController.java:96` |
| POST | `/notificaciones/leer-todas` | `NotificacionController.java:147` |
| GET | `/panel/usuario` | `PanelController.java:30` |
| GET | `/panel/admin` | `PanelController.java:49` |
| GET | `/panel/indexv1.html` | `PanelController.java:74` |
| GET | `/panelusuarios/verpedidos` | `PanelUsuarioController.java:59` |
| POST | `/panelusuarios/{id}/cancelar` | `PanelUsuarioController.java:81` |
| GET | `/panelusuarios/panelhistorial` | `PanelUsuarioController.java:106` |
| POST | `/panelusuarios/perfil/editar` | `PanelUsuarioController.java:163` |
| GET | `/pedido/{id}` | `PedidoController.java:56` |
| GET | `/pedido/{id}/pdf` | `PedidoController.java:96` |
| POST | `/pedido/{id}/cancelar` | `PedidoController.java:185` |
| POST | `/pedido/{id}/confirmar-cancelacion` | `PedidoController.java:205` |
| GET | `/producto/{id}` | `ProductoController.java:19` |
| GET | `/auth/recuperar` | `RecuperacionController.java:43` |
| POST | `/auth/recuperar` | `RecuperacionController.java:53` |
| GET | `/auth/restablecer` | `RecuperacionController.java:69` |
| POST | `/auth/restablecer` | `RecuperacionController.java:88` |
| GET | `/usuarios/traer` | `UsuarioController.java:27` |

Total inventariado: **74 mappings HTTP**, contando los alias explícitos. El scheduler de correo, servicios internos y posibles métodos no expuestos se revisan por separado.

## Anexo: regresiones nuevas y resultado definitivo

| Caso | Evidencia observada |
| --- | --- |
| `inicioDeSesionActualizaUltimaConexion` | Fecha sigue null después de login válido. |
| `catalogoExcluyeDiscontinuados` | Producto discontinuado sigue en el HTML. |
| `editarImagenAdministrativaPersisteElCambio` | Se conserva la URL anterior. |
| `cantidadNoAgregaUnaLineaAusente` | Se crea una línea mediante modificación de cantidad. |
| `altaAdministrativaRechazaDatosInvalidos` | Se crea cuenta con email sin formato válido. |
| `edicionAdministrativaRechazaEmailDuplicado` | Email del cliente queda igual al del administrador. |
| `aliasCarritoConservaElModeloDeLaVista` | Alias no aporta el modelo requerido. |
| `usuariosJsonConComprasNoTieneRecursion` | El cuerpo JSON con relaciones no se puede parsear. |

Las ocho fallan frente al comportamiento esperado. No se modificaron expectativas para ocultar los defectos.

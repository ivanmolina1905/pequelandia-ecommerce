# Auditoría ampliada — 30/10/2026

## Resultado de esta ejecución

Se ejecutaron 114 casos Java: 105 aprobados, 8 fallidos por aserciones y 1 error por excepción no controlada. No hubo omisiones. Se ejecutaron 5 casos JavaScript: 4 aprobados y 1 fallido. Total: 119 casos automatizados, 109 aprobados, 9 fallidos y 1 con error.

La suite anterior tenía 84 casos Java; se agregaron 30 casos de conciliación, permisos, exportaciones y entradas inválidas. También se añadió una regresión JavaScript para el estado ENVIADO. Tres casos aprobados de la suite anterior son caracterizaciones de defectos conocidos (email incompleto, última conexión y perfil desactualizado), no resultados funcionales deseables.

Se completaron además verificaciones manuales en navegador con datos ficticios. No se deben sumar estos pasos a los 119 casos de JUnit/Node ni interpretarlos como cobertura total.

No se corrigió código de negocio ni se alteró Eclipse. Se conservaron pruebas fallidas para empezar a depurar. No se hizo commit ni push.

## Entorno y aislamiento

Java 22.0.1, Maven local 3.9.11, Spring Boot 3.5.4, PostgreSQL 18.4; base local descartable pequelandia_verificacion en puerto 55439. Las pruebas automatizadas usan MockMvc y JavaMailSender simulado. La revisión visual usa otra ejecución de la aplicación contra esa misma base después de terminar la suite, con datos ficticios nuevos, HTTP 127.0.0.1:18081 y entrega SMTP desactivada. No se usaron cuentas ni direcciones reales.

El arranque visual utiliza un lanzador temporal en .tools con Tomcat NIO2, sin modificar el arranque habitual del proyecto. La configuración privada habitual no se importa en las pruebas.

## Problemas reproducidos y cómo depurarlos

| Prioridad | Problema | Esperado | Resultado comprobado | Acción propuesta |
| --- | --- | --- | --- | --- |
| Alta | Las estadísticas reescriben ventas históricas al cambiar el precio | Una compra de 2 × 12,50 sigue sumando 25 | Tras cambiar el precio a 99, ventas del día = 198 | Usar detalle_carrito.precio congelado, no producto.precio actual; aplicar a día/mes/año |
| Alta | El gasto histórico del usuario también cambia | Sigue siendo 25 | totalGastadoPorUsuario devolvió 198 | Corregir consultas de gasto y series personales con precio histórico |
| Alta | Se persiste precio negativo | Rechazo y ninguna fila negativa | POST administrativo guardó precio -10 | Validar precio/costo en servicio y formularios; restricciones de base con migración |
| Alta | El carrito por formulario supera el stock | Carrito vacío si se piden 4 y hay 3 | La ruta /productos/agregar lo modifica | Compartir reglas entre formulario y API; conservar validación de checkout |
| Alta | La API permite agregar productos inactivos | ok=false | ok=true | Validar estado comercial al agregar |
| Alta | Un carrito sin comprar entra al top de vendidos | Ranking vacío sin pedidos | Ranking no vacío al insertar un carrito abierto con 2 unidades | Relacionar detalles con pedidos válidos y definir estados incluidos |
| Alta | Estado ENVIADO se muestra PENDIENTE | Tabla muestra ENVIADO | PostgreSQL guardó ENVIADO; tabla mostró PENDIENTE | Incluir ENVIADO en renderBadgeEstadoPedido y revisar ayudas/iconos/colores |
| Media | Los cancelados siguen sumando ventas | Ventas netas 0 tras cancelar la única compra de 25 | Resumen sigue en 25 | Definir si se informa venta bruta o neta; si es neta, excluir cancelados y conciliar ranking/gastos. La expectativa de esta prueba es neta |
| Media | Rango diario invertido no se valida | HTTP 400 al pedir desde 30/10 hasta 01/10 | HTTP 200 | Validar desde <= hasta, también en exportaciones y series anuales |
| Media | Fecha inválida en exportación dispara excepción | HTTP 400 con mensaje | DateTimeParseException sin controlar; MockMvc terminó con ServletException | Convertir/validar fechas en la entrada y manejar errores de formato |
| Alta | Enlace Ver del catálogo roto | Detalle o modal del producto existente | /producto/1 mostró Whitelabel 404: No static resource producto/1 | Implementar ruta de detalle o reutilizar el modal del inicio |
| Media | Precio unitario del modal de compra es un subtotal | 2 unidades de 100: unitario 100, subtotal 200 | Bajo Precio Unitario aparece 200 | Mostrar precio unitario o cambiar el título a Subtotal; agregar ambos si ayuda |
| Media | Panel de compra oculto para lectores de pantalla | Controles accesibles cuando el panel está abierto | Visible en captura, pero .peq-quick mantiene aria-hidden=true; getByRole no encuentra AGREGAR AL CARRITO | Sincronizar aria-hidden al abrir/cerrar y gestionar foco/teclado |
| Media | Email incompleto aceptado | Validación del lado del servidor | La caracterización existente vuelve a aprobar reproduciendo la aceptación | Validación de email en registro y perfil |
| Media | Última conexión no actualizada | Fecha al autenticar correctamente | Caracterización aprobada; tabla visual mostró guion tras login | Registrar fecha/hora en login exitoso |
| Media | Perfil de sesión desactualizado tras edición administrativa | Perfil con datos actuales | Caracterización existente vuelve a reproducir datos anteriores | Leer perfil desde base o refrescar sesión |

Las dos fallas de precio histórico comparten causa. El conteo de pruebas fallidas no equivale a cantidad de causas independientes.

### Causas localizadas

- IPedidoRepository suma d.cantidad * prod.precio en lugar del precio congelado de detalle_carrito; afecta resumen y series.
- IPedidoRepository.productosMasVendidos agrupa detalle_carrito sin exigir pedido.
- CatalogoController.agregarAlCarrito no consulta existencia, estado ni stock. apiAgregar consulta existencia/stock pero omite estado.
- ProductoService valida stock negativo, pero no precio.
- ExcelExportController convierte entradas con LocalDate.parse; la excepción de formato no tiene un manejo específico que devuelva 400.
- paneladministracion.html: renderBadgeEstadoPedido no contiene ENVIADO y cae en el estilo PENDIENTE. La ayuda del selector también conserva un mensaje de preparación al seleccionar ENVIADO.
- verproductos.html enlaza /producto/{id}; en ejecución no hay manejador disponible.
- carrito.html arma las filas del modal con i.subtotal bajo el encabezado Precio Unitario.

## Casos que aprobaron y alcance exacto

### Negocio y permisos

- Compra con importes conocidos: series día, mes y año sumaron 25 correctamente antes de modificar el precio.
- Checkout rechazó productos inactivos y discontinuados, precios negativos y cantidades 0, -1 e Integer.MAX_VALUE. No guardó pedidos ni descontó stock en esos casos.
- Nueve rutas administrativas rechazaron una sesión de cliente con HTTP 403: usuarios, productos, pedidos, estadísticas, correos, listado JSON y exportaciones de usuarios/productos/pedidos.
- Inicio, catálogo, login, registro y recuperación respondieron sin sesión.
- Filtro de precio y talle excluyó un producto fuera del rango.
- Las 71 pruebas originales volvieron a aprobar: cuentas, validaciones, recuperación, CSRF, permisos, compras/cancelaciones concurrentes, última unidad, doble checkout, rollback, notificaciones, seguimiento y SMTP simulado. Tres de esas pruebas reproducen defectos conocidos.

### Excel

- Las cinco exportaciones básicas de la primera auditoría volvieron a aprobar la apertura con Apache POI.
- Con una compra real ficticia de 25, el Excel de pedidos mostró comprador e importe 25.
- El Excel de la serie diaria mostró 25 en la celda de valor.
- Esto concilia celdas concretas; no constituye revisión visual de todos los reportes ni prueba de miles de filas.

### JavaScript

Aprobaron los cuatro casos de CSRF/escape existentes. Falló la nueva regresión de representación de ENVIADO, corroborando el hallazgo visual. La regresión ejecuta la función real extraída de la plantilla dentro de un contexto vm; no replica su implementación.

## Recorrido real en navegador

1. Inicio con tres productos ficticios. Se abrió el modal Ver y se inspeccionaron imagen, nombre, descripción y precio. Su botón Comprar no produjo una compra ni una navegación visible durante este intento; no se concluye que Comprar deba completar una compra directamente.
2. Catálogo: se abrió el panel Comprar, se agregó una unidad de Remera prueba 1 por 100 y se verificó confirmación. El panel funciona visualmente, pero tiene el problema aria-hidden indicado arriba.
3. Carrito: una unidad, total 100. Se pulsó +: dos unidades, total 200.
4. Finalizar sin sesión llevó al formulario de login.
5. Login con cliente ficticio: perfil disponible; al volver al carrito se conservaron las dos unidades del invitado.
6. Finalizar creó pedido #1, PREPARANDO, total 200 y seguimiento con fecha/hora local. Se guardó captura de evidencia. El modal muestra subtotal 200 bajo Precio Unitario.
7. Descargar PDF abrió pedido-1.pdf en Adobe Acrobat. La espera automatizada del evento de descarga no terminó y agotó el tiempo; la descarga real sí ocurrió. No se confunde el timeout de herramienta con fallo del servidor.
8. El PDF descargado tiene una página, cantidad 2, subtotal 200 y total 200. Se extrajo texto y se renderizó con Poppler: contenido legible, sin cortes en este caso de una línea. No se probó un PDF con muchas páginas.
9. Vista móvil de catálogo a 390 × 844: filtros y tarjetas se apilan. Se midió innerWidth=390 y documentElement.scrollWidth=375, sin desbordamiento horizontal global en ese estado. No equivale a validar todos los componentes en móvil. Se restauró el tamaño predeterminado.
10. Ver del catálogo abrió /producto/1 y devolvió 404.
11. Login de administrador ficticio: tabla de dos usuarios, ambos sin última conexión.
12. Desde menú responsive se abrieron Estadísticas: 2 usuarios, 3 productos, 1 pedido y ventas 200; ranking con 2 unidades. En Pedidos se cargó la fila de clienteui por 200.
13. Se cambió el pedido a ENVIADO. La tabla lo pintó PENDIENTE; SQL confirmó estado_pedido=ENVIADO. La regresión JavaScript reproduce la causa.

Evidencia visual local: .tools/auditoria-pedido-ui.png y .tools/auditoria-admin-ui.png. PDF de prueba descargado: C:/Users/Ivan Molina/Downloads/pedido-1.pdf; render local: .tools/auditoria-pdf.png. Son datos ficticios, no se versionan.

## Respuesta HTTP y reinicio

Se hicieron 20 GET secuenciales a /productos: 20 respuestas HTTP 200, promedio 66,44 ms, mínimo 46,53 ms y máximo 241,55 ms. Es una muestra local pequeña con tres productos y un cliente secuencial, no una prueba de carga ni un objetivo de rendimiento.

Se reinició solamente el PostgreSQL descartable. Antes y después existían pedido #1 con estado ENVIADO y 7 mensajes en cola. Después del reinicio, /productos respondió HTTP 200. Esto verifica persistencia básica de esos datos y recuperación de conexión; no se reinició la aplicación ni se probó una caída abrupta o recuperación de mensajes a medio enviar.

## Advertencias y bloqueos

- Sigue apareciendo HHH90003004: paginación de una consulta con colección aplicada en memoria. Falta medir con volumen y corregir la estrategia de consulta.
- Siguen los avisos del agente dinámico Mockito, dialecto explícito, open-in-view y endpoints OpenAPI habilitados.
- El acceso de UI a Adobe Acrobat requirió una aprobación de aplicación que venció. Se inspeccionó el mismo PDF mediante lectura local, extracción de texto y render; no se modificaron permisos.
- El alias python de Windows no ejecutó. Se usó el Python incluido en el runtime de Codex. Poppler renderizó el PDF; sus avisos sobre fuentes Symbol/ArialUnicode no impidieron una imagen legible en este documento.
- No hubo envío a buzones reales. SMTP estuvo simulado en tests y desactivado en la revisión visual.
- Las contraseñas siguen sin hash por la instrucción de AGENTS.md; no se alteró ese alcance.

## Qué continúa pendiente

No es posible certificar absolutamente todas las combinaciones de un sistema con una suite finita. Esta segunda pasada amplía la evidencia y deja un punto de partida concreto para depurar. Quedan sin ejecutar: carga concurrente sostenida con catálogo grande, auditoría externa actualizada de dependencias, registro simultáneo de duplicados, reinicio de aplicación, caída abrupta de base/SMTP, entrega real a un buzón autorizado, accesibilidad integral de todas las pantallas, eliminación con compras históricas y conciliación visual completa de todos los Excel/PDF.

No se presenta un caso aprobado como prueba de funciones que sus aserciones no comprueban. No se afirma cobertura de producción ni ausencia de otros errores.

## Orden recomendado de corrección

1. Congelar la contabilidad en las consultas (precio histórico) y separar carritos de ventas; definir estados para ventas netas/brutas.
2. Arreglar la representación de ENVIADO y el enlace de detalle roto.
3. Unificar validaciones de producto/carrito y rechazar precios inválidos al administrar.
4. Corregir unitario/subtotal, accesibilidad y entradas de fecha/rango.
5. Resolver los tres defectos de usuarios ya conocidos y convertir caracterizaciones en regresiones del comportamiento correcto.
6. Repetir esta suite; luego ampliar volumen, concurrencia y recorridos pendientes.

## Reproducción

Con Java 22 y el PostgreSQL descartable independiente iniciado en 55439:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-22'
$env:PEQUELANDIA_TEST_DB_URL='jdbc:postgresql://127.0.0.1:55439/pequelandia_verificacion'
$maven=Get-ChildItem .tools/maven -Recurse -Filter mvn.cmd | Select-Object -First 1
& $maven.FullName --batch-mode --offline test
node --test src/test/js/seguridad.test.cjs src/test/js/estado-pedido.test.cjs
```

La base se recrea y vacía durante los tests; no usar la base de desarrollo habitual. Los tests fallidos se dejan activos intencionalmente. Maven termina BUILD FAILURE por los hallazgos. La última comprobación de empaquetado fue el 29/10 con skipTests, no se atribuye a esta ejecución.

Log Java local: .tools/auditoria-30.log. Detalles XML/trazas: target/surefire-reports. Logs visuales: .tools/auditoria-ui.log. No se incorporan logs ni bases a Git.

## Resultado de cada caso Java


### com.pequelandia.pequelandia.AuditoriaAmpliadaIntegrationTest

Casos: 30; fallos: 5; errores: 1; omitidos: 0.

| Caso | Resultado |
| --- | --- |
| paginasPublicasRespondenSinSesion(String)[1] | APROBÓ |
| paginasPublicasRespondenSinSesion(String)[2] | APROBÓ |
| paginasPublicasRespondenSinSesion(String)[3] | APROBÓ |
| paginasPublicasRespondenSinSesion(String)[4] | APROBÓ |
| paginasPublicasRespondenSinSesion(String)[5] | APROBÓ |
| excelSerieDiariaConciliaImporte | APROBÓ |
| cambiarPrecioNoReescribeVentasHistoricas | FALLÓ |
| excelPedidosConciliaImporteYUsuario | APROBÓ |
| filtroPrecioYTalleExcluyeProductoFueraDeRango | APROBÓ |
| checkoutRechazaCantidadInvalidaSinDescontar(int)[1] | APROBÓ |
| checkoutRechazaCantidadInvalidaSinDescontar(int)[2] | APROBÓ |
| checkoutRechazaCantidadInvalidaSinDescontar(int)[3] | APROBÓ |
| topVendidosNoCuentaCarritoSinComprar | FALLÓ |
| rangoDiarioInvertidoDevuelveErrorDeValidacion | FALLÓ |
| checkoutRechazaPrecioNegativoSinGuardarPedido | APROBÓ |
| checkoutRechazaProductoNoComercializable(String)[1] | APROBÓ |
| checkoutRechazaProductoNoComercializable(String)[2] | APROBÓ |
| cambiarPrecioNoReescribeGastoDelUsuario | FALLÓ |
| seriesDiaMesAnioConcuerdanConCompraConocida | APROBÓ |
| ventasNetasExcluyenPedidoCancelado | FALLÓ |
| exportarFechaInvalidaDevuelve400 | ERROR — excepción no controlada |
| clienteNoAccedeARutasAdministrativas(String)[1] | APROBÓ |
| clienteNoAccedeARutasAdministrativas(String)[2] | APROBÓ |
| clienteNoAccedeARutasAdministrativas(String)[3] | APROBÓ |
| clienteNoAccedeARutasAdministrativas(String)[4] | APROBÓ |
| clienteNoAccedeARutasAdministrativas(String)[5] | APROBÓ |
| clienteNoAccedeARutasAdministrativas(String)[6] | APROBÓ |
| clienteNoAccedeARutasAdministrativas(String)[7] | APROBÓ |
| clienteNoAccedeARutasAdministrativas(String)[8] | APROBÓ |
| clienteNoAccedeARutasAdministrativas(String)[9] | APROBÓ |

### com.pequelandia.pequelandia.AuditoriaExploratoriaIntegrationTest

Casos: 13; fallos: 3; errores: 0; omitidos: 0.

| Caso | Resultado |
| --- | --- |
| exportacionesGeneranExcelLegible(String)[1] | APROBÓ |
| exportacionesGeneranExcelLegible(String)[2] | APROBÓ |
| exportacionesGeneranExcelLegible(String)[3] | APROBÓ |
| exportacionesGeneranExcelLegible(String)[4] | APROBÓ |
| exportacionesGeneranExcelLegible(String)[5] | APROBÓ |
| apiRechazaProductoInactivo | FALLÓ |
| catalogoRenderizaTodosLosOrdenes(String)[1] | APROBÓ |
| catalogoRenderizaTodosLosOrdenes(String)[2] | APROBÓ |
| catalogoRenderizaTodosLosOrdenes(String)[3] | APROBÓ |
| catalogoRenderizaTodosLosOrdenes(String)[4] | APROBÓ |
| formularioTambienRechazaStockInsuficiente | FALLÓ |
| apiRechazaStockInsuficienteSinModificarCarrito | APROBÓ |
| administradorNoPuedeCrearPrecioNegativo | FALLÓ |

### com.pequelandia.pequelandia.CircuitoUsuariosIntegrationTest

Casos: 27; fallos: 0; errores: 0; omitidos: 0.

| Caso | Resultado |
| --- | --- |
| hallazgoPerfilAbiertoMuestraDatosAnterioresAEdicionAdministrativa | APROBÓ — reproduce defecto conocido |
| hallazgoUltimaConexionNoSeActualizaAlIngresar | APROBÓ — reproduce defecto conocido |
| localidadInexistenteEsRechazadaSinGuardarCuenta | APROBÓ |
| circuitoCompletoDesdeFormularioHastaAdministradorLoginPerfilYRecuperacion | APROBÓ |
| csrfImpideRegistroLoginYEdicionSinToken | APROBÓ |
| hallazgoEmailIncompletoEsAceptadoActualmente | APROBÓ — reproduce defecto conocido |
| duplicadosSeRechazanSinEfectosSecundarios(String, String)[1] | APROBÓ |
| duplicadosSeRechazanSinEfectosSecundarios(String, String)[2] | APROBÓ |
| duplicadosSeRechazanSinEfectosSecundarios(String, String)[3] | APROBÓ |
| fallaEnCorreoRevierteAltaYNotificacion | APROBÓ |
| registroInvalidoNoDejaUsuarioAvisosNiCorreos(String, String)[1] | APROBÓ |
| registroInvalidoNoDejaUsuarioAvisosNiCorreos(String, String)[2] | APROBÓ |
| registroInvalidoNoDejaUsuarioAvisosNiCorreos(String, String)[3] | APROBÓ |
| registroInvalidoNoDejaUsuarioAvisosNiCorreos(String, String)[4] | APROBÓ |
| registroInvalidoNoDejaUsuarioAvisosNiCorreos(String, String)[5] | APROBÓ |
| registroInvalidoNoDejaUsuarioAvisosNiCorreos(String, String)[6] | APROBÓ |
| registroInvalidoNoDejaUsuarioAvisosNiCorreos(String, String)[7] | APROBÓ |
| registroInvalidoNoDejaUsuarioAvisosNiCorreos(String, String)[8] | APROBÓ |
| registroInvalidoNoDejaUsuarioAvisosNiCorreos(String, String)[9] | APROBÓ |
| registroInvalidoNoDejaUsuarioAvisosNiCorreos(String, String)[10] | APROBÓ |
| registroInvalidoNoDejaUsuarioAvisosNiCorreos(String, String)[11] | APROBÓ |
| registroInvalidoNoDejaUsuarioAvisosNiCorreos(String, String)[12] | APROBÓ |
| registroInvalidoNoDejaUsuarioAvisosNiCorreos(String, String)[13] | APROBÓ |
| registroInvalidoNoDejaUsuarioAvisosNiCorreos(String, String)[14] | APROBÓ |
| claveIncorrectaNoCreaSesionYNombreFuncionaSinDistinguirMayusculas | APROBÓ |
| administradorEditaYEliminaCuentaSinComprasYRevocaAcceso | APROBÓ |
| consultasDocumentadasSonValidasEnPostgres | APROBÓ |

### com.pequelandia.pequelandia.config.AccessInterceptorTest

Casos: 7; fallos: 0; errores: 0; omitidos: 0.

| Caso | Resultado |
| --- | --- |
| requiereSesion | APROBÓ |
| pedidoExigeTitularidad | APROBÓ |
| jsonNoExponeContrasenaNiModificaSuAlmacenamiento | APROBÓ |
| permiteAdministracion | APROBÓ |
| serieConservaDatosParaEstadisticas | APROBÓ |
| pedidoInexistenteDevuelve404 | APROBÓ |
| bloqueaUsuarioComunEnAdministracionYExportaciones | APROBÓ |

### com.pequelandia.pequelandia.config.CsrfInterceptorTest

Casos: 2; fallos: 0; errores: 0; omitidos: 0.

| Caso | Resultado |
| --- | --- |
| aceptaTokenEnHeaderOFormulario | APROBÓ |
| rechazaPostSinTokenYConTokenAjeno | APROBÓ |

### com.pequelandia.pequelandia.PedidosIntegrationTest

Casos: 22; fallos: 0; errores: 0; omitidos: 0.

| Caso | Resultado |
| --- | --- |
| lecturaConcurrenteEsIdempotenteYProtegeDestinatario | APROBÓ |
| seguimientoRegistraCambiosRealesSinDuplicadosYProtegeAcceso | APROBÓ |
| carritoHistoricoNoSeReutilizaNiSeModifica | APROBÓ |
| stockMinimoAvisaUnaVezPorEpisodioYSeRearmaAlReponer | APROBÓ |
| falloDeAvisosRevierteLaCompraCompleta | APROBÓ |
| entregadoNoPuedeCancelarseNiReponerse | APROBÓ |
| compraAtomicaYReintentoDelMismoCarrito | APROBÓ |
| historialPaginaFiltraYAislaDestinatarios | APROBÓ |
| seguimientoAntiguoNoInventaFechasYRevierteSiFallaElAviso | APROBÓ |
| solicitudRepetidaYConfirmacionesConcurrentesReponenUnaVez | APROBÓ |
| stockManualGeneraAvisoYElFalloDeColaRevierteTodaLaCompra | APROBÓ |
| falloAlNotificarCancelacionRevierteStockYEstado | APROBÓ |
| checkoutConcurrenteDeUnaSesionSoloCreaUnPedido | APROBÓ |
| falloDeStockRevierteCarritoDetallesYDescuentos | APROBÓ |
| registroEncolaBienvenidaYAvisoSinEnviarSmtp | APROBÓ |
| smtpFallidoSeReintentaSinDuplicarAvisos | APROBÓ |
| panelCorreosProtegeDatosYReintentos | APROBÓ |
| dosComprasCompitenPorLaUltimaUnidad | APROBÓ |
| fusionDeInvitadoConservaCantidadesYPrecio | APROBÓ |
| panelesYCatalogoRenderizanConCsrfYPermisos | APROBÓ |
| seguridadHttpProtegePedidosAdminPerfilYCsrF | APROBÓ |
| vistasRenderizanTokenYCuentasNoSeEscalanPorNombreDeSesion | APROBÓ |

### com.pequelandia.pequelandia.PequelandiaApplicationTests

Casos: 1; fallos: 0; errores: 0; omitidos: 0.

| Caso | Resultado |
| --- | --- |
| contextLoads | APROBÓ |

### com.pequelandia.pequelandia.RecuperacionIntegrationTest

Casos: 10; fallos: 0; errores: 0; omitidos: 0.

| Caso | Resultado |
| --- | --- |
| entregaRecuperacionConSpringMailYUrlLocalDelPuertoConfigurado | APROBÓ |
| contrasenasInvalidasNoConsumenToken | APROBÓ |
| dosConsumosConcurrentesSoloCambianUnaVez | APROBÓ |
| respuestaGenericaYProteccionCsrf | APROBÓ |
| vencimientoYCorreoModificadoInvalidanEnlace | APROBÓ |
| falloEnColaRevierteCambioYConsumo | APROBÓ |
| flujoCompletoCorreoHashYUsoUnico | APROBÓ |
| postExitosoRedirigeEInvalidaSesion | APROBÓ |
| limiteIpYDeIntentosDeRestablecimiento | APROBÓ |
| esperaLimiteCorreoYReemplazoDeToken | APROBÓ |

### com.pequelandia.pequelandia.service.IdentidadServiceTest

Casos: 2; fallos: 0; errores: 0; omitidos: 0.

| Caso | Resultado |
| --- | --- |
| administradorConfiguradoPuedeCambiarNombreSinPerderPermisos | APROBÓ |
| compatibilidadFijaElIdYPermiteValidarUsuariosNuevos | APROBÓ |

## Detalle JavaScript y cierre

| Caso | Resultado |
| --- | --- |
| CSRF en POST local y conservación de cabeceras | APROBÓ |
| No filtrar CSRF a terceros ni GET | APROBÓ |
| Request conserva cabecera original | APROBÓ |
| Escape de nombres al generar HTML | APROBÓ |
| ENVIADO no se representa como PENDIENTE | FALLÓ |

Al finalizar se detuvieron la aplicación visual y el PostgreSQL descartable iniciados para esta auditoría. Se conservaron los datos ficticios en disco para revisión; la próxima ejecución automatizada los reemplaza. No se dejaron servidores de prueba corriendo.

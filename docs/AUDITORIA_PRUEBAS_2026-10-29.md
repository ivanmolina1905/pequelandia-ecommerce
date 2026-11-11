# Auditoría de pruebas de Pequelandia — 29/10/2026

## Resultado comprobado

Se ejecutaron 84 casos Java: 81 aprobados, 3 fallidos, 0 errores de ejecución y 0 omitidos. También se ejecutaron 4 pruebas JavaScript: todas aprobadas. Total: 88 casos, 85 aprobados y 3 fallidos.

La suite Java previa contiene 71 casos y aprobó completa. Se agregaron 13 casos exploratorios: 10 aprobaron y 3 revelaron defectos. Tres de los casos previos son caracterizaciones de defectos conocidos: su aprobación confirma el defecto, no un comportamiento correcto. En total se reprodujeron 6 defectos funcionales.

Entorno: Windows, Java 22.0.1, Maven 3.9.11, Spring Boot 3.5.4, PostgreSQL 18.4 local en 127.0.0.1:55439, base descartable pequelandia_verificacion. Se utilizaron datos ficticios, sesiones MockMvc y JavaMailSender simulado. Se detuvo la instancia PostgreSQL iniciada para esta auditoría al terminar.

No se modificó código de negocio ni configuración de Eclipse. No se hizo commit ni push. Se añadió AuditoriaExploratoriaIntegrationTest.java para conservar reproducciones ejecutables.

## Fallos nuevos reproducidos

### 1. Precio negativo persistido — prioridad alta

Solicitud autenticada como administrador: POST /admin/productos/nuevo con CSRF válido, nombre_producto=Precio invalido, precio=-10 y stock=2.

Esperado: rechazar la operación y no guardar un producto con precio negativo.

Observado: SELECT count(*) FROM producto WHERE precio < 0 devolvió 1. La prueba esperaba 0.

Prueba: administradorNoPuedeCrearPrecioNegativo, línea 75.

Origen: AdminProductoController.crear enlaza directamente Producto; ProductoService.saveProducto valida stock, pero no precio. El modelo tampoco define restricciones de precio.

Mejora: validar nombre, precio finito y positivo, costo y estado en el servidor; aplicar la misma regla a creación y edición. Agregar restricciones de base mediante migración. Revisar antes de operar pedidos con importes inválidos; esta prueba no demuestra una compra con total negativo.

### 2. API permite agregar un producto inactivo — prioridad alta

Preparación: producto con estado inactivo, precio 100 y stock 3.

Solicitud: POST /productos/api/agregar con productoId existente, cantidad=1 y CSRF válido.

Esperado: ok=false y carrito sin cambios.

Observado: ok=true. La prueba falló al comparar el indicador; no verificó el estado final del carrito después de ese fallo.

Prueba: apiRechazaProductoInactivo, línea 70.

Origen: CatalogoController.apiAgregar comprueba existencia y stock, pero no el estado del producto.

Mejora: centralizar la disponibilidad comercial y validarla al agregar y al confirmar la compra. Esta prueba no demuestra que un checkout con un producto inactivo pueda completarse.

### 3. Formulario permite superar el stock — prioridad alta

Preparación: producto activo con stock 3 y carrito vacío.

Solicitud: POST /productos/agregar con cantidad=4 y CSRF válido.

Esperado: rechazar la operación y conservar vacío el carrito.

Observado: el carrito dejó de estar vacío. Se incorporó una cantidad superior al stock mediante esta vía. En contraste, el caso equivalente por /productos/api/agregar fue rechazado y conservó vacío el carrito.

Prueba: formularioTambienRechazaStockInsuficiente, línea 64.

Origen: CatalogoController.agregarAlCarrito llama directamente CartSession.add sin consultar el producto ni el stock.

Mejora: compartir la validación de las dos rutas y conservar la validación transaccional del checkout. La falla de carrito no equivale a una sobreventa confirmada: las pruebas de concurrencia de compra aprobaron.

## Defectos anteriores confirmados nuevamente

| Hallazgo | Evidencia ejecutada | Mejora |
| --- | --- | --- |
| Email incompleto aceptado | hallazgoEmailIncompletoEsAceptadoActualmente aprueba reproduciendo el registro con email incompleto | Validación de email del lado del servidor en registro y perfil |
| Última conexión no actualizada | hallazgoUltimaConexionNoSeActualizaAlIngresar confirma que continúa sin fecha después de ingresar | Guardar fecha/hora tras autenticación correcta |
| Perfil desactualizado tras edición administrativa | hallazgoPerfilAbiertoMuestraDatosAnterioresAEdicionAdministrativa reproduce datos anteriores en una sesión abierta | Construir el perfil desde la base o refrescar la sesión |

Al corregir estos defectos, convertir sus caracterizaciones en pruebas que exijan el comportamiento correcto.

## Qué quedó comprobado

- Registro, validaciones parametrizadas, duplicados secuenciales, normalización y rollback al fallar la cola.
- Login, renovación de identificador de sesión, perfil, administración de usuarios y revocación de acceso de una cuenta eliminada.
- Recuperación: uso único, vencimiento, cambio de email, límites, concurrencia de consumo y rollback.
- Pedidos: compra atómica, stock insuficiente, última unidad concurrente, doble checkout de una sesión, carritos históricos y unión del carrito invitado.
- Cancelaciones: permisos, reposición una sola vez, estados y rollback.
- Notificaciones: destinatarios, filtros, lectura concurrente, historial y seguimiento.
- Correos: encolado, simulación de entrega, fallo SMTP, reintento y permisos del panel.
- Seguridad: controles de acceso, CSRF, pedidos ajenos y exclusión de contraseña del JSON comprobada por el circuito existente.
- HTML Thymeleaf de paneles, catálogo y formularios mediante MockMvc.
- Catálogo con los cuatro órdenes: relevancia, precio ascendente, precio descendente y alfabético.
- Cinco exportaciones Excel: usuarios, productos, pedidos, stock bajo y top productos. Se verificó HTTP 200, apertura con Apache POI, existencia de hoja y fila inicial. No se concilió cada celda ni se comprobó visualmente Excel; pedidos y top se probaron sin ventas en la nueva suite.
- JavaScript: CSRF en POST local, conservación de cabeceras, no filtración a otro sitio/GET, compatibilidad con Request y escape de texto.

Los nombres exactos y el estado de cada caso aparecen al final de este informe. El alcance de cada prueba depende de sus aserciones; una prueba aprobada no cubre todas las combinaciones de una funcionalidad.

## Advertencias y problemas del entorno

1. El wrapper intentó crear su caché fuera del espacio permitido y recibió acceso denegado. Con MAVEN_USER_HOME local apareció un problema de ejecución de rutas con espacios. Se usó el mvn.cmd instalado en .tools/maven invocándolo directamente desde PowerShell.
2. El aislamiento bloqueó conexiones TCP locales. Un intento posterior tampoco alcanzó el PostgreSQL iniciado en el proceso restringido. Se resolvió iniciando base y pruebas dentro de la misma ejecución con acceso local habilitado. Los 73 errores de contexto del intento intermedio fueron infraestructura y no 73 defectos independientes; no forman parte del resultado final.
3. Hibernate informó HHH90003004: paginación con colección cargada que se aplica en memoria. Revisar buscarPublico, separar página de IDs y carga de categorías, y medir con un catálogo grande. No se midió carga ni consumo de memoria en esta auditoría.
4. Mockito carga un agente dinámicamente y avisa de incompatibilidad futura con el comportamiento predeterminado de otros JDK. Preparar el agente en la configuración de pruebas cuando se actualice el entorno.
5. Hay advertencias de dialecto PostgreSQL explícito, open-in-view y documentación OpenAPI habilitada. Son observaciones de configuración; no se presentan como vulnerabilidades explotadas.

## Qué falta para una revisión integral

No corresponde afirmar que se probó absolutamente todo. Esta ejecución cubre backend, persistencia, HTML generado, exportaciones básicas y utilidades JavaScript. Quedan pendientes:

| Área pendiente | Prueba propuesta |
| --- | --- |
| Navegador de escritorio y móvil | Recorrido real de registro → compra → administración; modales, filtros, campana, errores, teclado y tamaños de pantalla |
| Accesibilidad | Foco, etiquetas, contraste y navegación sin mouse |
| Estadísticas | Conciliar importes y series día/mes/año con ventas conocidas, cancelaciones y límites de fecha |
| Excel y PDF | Conciliar todas las filas/importes y revisar formato visual y descarga desde navegador |
| Concurrencia adicional | Registro simultáneo con el mismo email/DNI; edición administrativa simultánea de stock |
| Validaciones extendidas | Precios no finitos, límites numéricos, cadenas enormes, fechas y filtros inválidos; comprobar HTTP y mensajes |
| Historial y eliminación | Eliminar usuarios/productos con compras y verificar conservación coherente de historial |
| Rendimiento | Grandes volúmenes, consultas por pantalla, paginación, latencia y memoria |
| Reinicios | Persistencia y recuperación de cola tras reiniciar aplicación y PostgreSQL |
| SMTP real | Entrega a una casilla de prueba autorizada; no se enviaron correos reales |
| Seguridad adicional | Límites de login, expiración de sesión y revisión integral de entradas/salidas |
| Dependencias | Auditoría actualizada de vulnerabilidades; no se hizo consulta de bases externas |

Las contraseñas continúan sin hash por instrucción expresa de AGENTS.md. No se cambió ese comportamiento. No se presenta como una solución adecuada para datos reales.

## Orden propuesto para depurar

1. Validar precios y disponibilidad del producto en el servidor; unificar agregar al carrito. Hacer pasar las tres pruebas nuevas.
2. Corregir email, última conexión y refresco de perfil; invertir las tres caracterizaciones para que exijan el resultado correcto.
3. Ampliar conciliación de estadísticas y documentos con ventas ficticias y casos de fechas.
4. Ejecutar recorridos reales en navegador y móvil.
5. Medir y corregir paginación y consultas con volumen; completar pruebas de reinicio y concurrencia.

La ejecución verify terminó en BUILD FAILURE por los tres fallos funcionales. Compilación de fuentes y tests completada. Luego se ejecutó package --offline -DskipTests por separado: BUILD SUCCESS y JAR ejecutable generado. Ese empaquetado omitió las pruebas y no cambia el resultado funcional de la suite. Log: .tools/auditoria-package.log.

## Evidencia reproducible

- Log local: .tools/auditoria-completa.log (ignorado por Git).
- XML y trazas de JUnit: target/surefire-reports (ignorados por Git).
- Tests añadidos: src/test/java/com/pequelandia/pequelandia/AuditoriaExploratoriaIntegrationTest.java.
- Ejecutar con Java 22, PostgreSQL descartable y PEQUELANDIA_TEST_DB_URL apuntando exclusivamente a pequelandia_verificacion. Los tests recrean y vacían tablas: no usar la base de desarrollo habitual.

## Detalle de cada caso Java


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


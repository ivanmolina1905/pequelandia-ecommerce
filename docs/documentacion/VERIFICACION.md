# Verificación de la edición documental

Fecha: 1 de noviembre de 2026. Rama de trabajo: `docs/documentacion-integral`.

## Alcance

Se conservaron los cambios locales presentes al comenzar. La tarea agrega explicaciones y herramientas de documentación; no modifica las decisiones de negocio ni la configuración privada. Bootstrap, imágenes, bases y herramientas locales no se reproducen como código propio.

El inventario abarca 108 archivos Java y 32 recursos propios. El análisis contiene 107 declaraciones de tipo, 769 métodos y constructores escritos explícitamente, 1673 declaraciones de variables o parámetros y 2009 instrucciones Java. Los tipos anidados también se cuentan. Los métodos generados por Lombok no son declaraciones escritas en los archivos.

Se agregaron 107 comentarios de tipo, 769 Javadoc de método, 733 comentarios de atributos o variables y 1676 comentarios de instrucciones. Los parámetros se explican con `@param`. Las declaraciones e instrucciones que comparten una línea, así como parámetros de lambdas y otras expresiones anidadas, se explican por separado en la referencia HTML. Los recursos propios tienen cabecera documental y explicación por línea en esa referencia.

## Comprobaciones estáticas y compilación

- El escáner del JDK 22 analiza todos los archivos Java sin errores de sintaxis.
- El generador compara los tokens antes y después de insertar comentarios: son iguales al excluir comentarios y espacios.
- El generador compara las categorías, tipos, nombres y relaciones sintácticas de los nodos antes y después: son iguales.
- Maven compiló 97 archivos de aplicación y 11 de pruebas con Java 22. La compilación se completó antes de ejecutar las pruebas seleccionadas. Las regeneraciones posteriores cambiaron solo comentarios y repitieron la comprobación de equivalencia sintáctica.
- `node --check` no encontró errores de sintaxis en los tres scripts propios del navegador.
- `git diff --check` no encontró problemas de espacios en los cambios finales revisados.

Se utilizó el Maven 3.9.11 ya disponible dentro de `.tools/maven/wrapper/dists`. El intento inicial mediante el wrapper normal no pudo crear su directorio global y el intento con un repositorio local nuevo carecía de dependencias; la compilación posterior utilizó el Maven disponible y las dependencias en caché, en modo offline.

## Pruebas Java

Ejecución seleccionada con el agente Mockito disponible y estas clases:

```text
AccessInterceptorTest:    7 ejecutadas, 0 fallos, 0 errores.
CsrfInterceptorTest:      2 ejecutadas, 0 fallos, 0 errores.
CentroAvisosPanelTest:    3 ejecutadas, 0 fallos, 0 errores.
IdentidadServiceTest:     2 ejecutadas, 1 fallo,  0 errores, al ejecutarlas juntas.
Total conjunto:         14 ejecutadas, 1 fallo,  0 errores.
```

El fallo conjunto de `compatibilidadFijaElIdYPermiteValidarUsuariosNuevos` se produce dentro de Mockito, en `MockUtil.getMockHandlerOrNull`, al configurar `thenReturn`. Se repitió la ejecución conjunta y volvió a aparecer. Al ejecutar únicamente `IdentidadServiceTest`, sus dos pruebas aprobaron. No se presenta la ejecución conjunta como aprobada ni se atribuye una causa definitiva al fallo del framework.

Un intento de la suite completa fue interrumpido después de que las suites de integración rechazaran la configuración de base: `IntegrationSupport` exige una URL local, puerto explícito y la base descartable `pequelandia_verificacion`. No se realizaron cambios para eludir esa protección ni se validó el circuito integral contra PostgreSQL en esta tarea. Los resultados antiguos presentes en `target` no se consideran evidencia de esta revisión.

## Pruebas JavaScript

Comando ejecutado:

```powershell
node --test src/test/js/seguridad.test.cjs src/test/js/estado-pedido.test.cjs
```

Resultado: cinco pruebas ejecutadas; cuatro aprobaron y una falló. Las cuatro pruebas de seguridad aprobaron. La prueba de estado detecta que `renderBadgeEstadoPedido("ENVIADO")` presenta `PENDIENTE` en la tabla administrativa.

Se volvió a evaluar esa función después de excluir el comentario HTML generado: sigue devolviendo `PENDIENTE`. El fallo no proviene de la cabecera documental. Se conserva la implementación existente y se registra el hallazgo sin modificar su comportamiento dentro de esta tarea.

## Documentos y referencia

- PDF final: 79 páginas con índice, marcadores, catálogo y numeración.
- Se renderizaron las 79 páginas con Poppler y se revisaron sus láminas de contacto; también se inspeccionaron páginas a mayor tamaño durante la preparación.
- La comprobación geométrica de todas las páginas mediante pdfplumber no encontró caracteres fuera de los márgenes definidos ni páginas sin texto.
- La referencia HTML contiene 140 entradas de archivo: 108 Java y 32 recursos. Ofrece código numerado, explicaciones y filtro de búsqueda local.
- Se conservaron fuentes editables y herramientas para regenerar comentarios, catálogo y PDF.

Las comprobaciones documentales no equivalen a una prueba visual completa de la aplicación ni a la comprobación de entrega real de correos.

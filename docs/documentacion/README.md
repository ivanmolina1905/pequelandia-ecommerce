# Documentación de Pequelandia

- [Manual PDF](Pequelandia_Documentacion_Completa.pdf): arquitectura, modelo de datos, recorridos, configuración, glosario y catálogo de clases y métodos.
- [Referencia completa](REFERENCIA_COMPLETA.html): abrir localmente en un navegador. Incluye buscador, código numerado y explicaciones de atributos, métodos, parámetros, variables locales e instrucciones Java; explica también los recursos propios por línea.
- [Manual editable en Markdown](MANUAL_PROYECTO.md): fuente de los capítulos generales del PDF.
- [Verificación](VERIFICACION.md): comprobaciones realizadas y sus límites.
- [Cobertura](cobertura.json): conteos y equivalencia sintáctica de Java.
- [Inventario](inventario.json): datos estructurados para regenerar el catálogo.

## Regeneración

Desde la carpeta del proyecto que contiene `pom.xml`, con `JAVA_HOME` apuntando al JDK 22:

```powershell
python docs/herramientas/documentar.py
python docs/herramientas/crear_manual.py
```

El primer comando usa el compilador del JDK para extraer la sintaxis sin resolver dependencias ni ejecutar la aplicación. Retira solamente sus comentarios marcados `[Doc]`, los vuelve a generar y verifica que no hayan cambiado los tokens ejecutables ni la estructura del árbol Java. Se conservan los comentarios originales. El segundo requiere ReportLab y las fuentes Arial de Windows para componer el PDF. Después se debe renderizar y revisar el PDF.

Los comentarios de negocio específicos se mantienen en `ROLES`, `SPECIAL` y `GLOSSARY` de `docs/herramientas/documentar.py`. Si cambia el comportamiento, actualizar esas descripciones y los capítulos del manual antes de regenerar. Las descripciones sintácticas automatizadas ayudan a leer el código y deben interpretarse junto con el bloque completo y sus llamadas.

La referencia cubre 108 archivos Java y 32 recursos de código o configuración propios. Los archivos de terceros, imágenes, salidas de compilación, configuración privada, herramientas locales y datos reales no se reproducen como código propio. Los métodos generados por Lombok se describen mediante las anotaciones de las clases.

Los números de línea corresponden a la versión documentada. La regeneración no conecta con PostgreSQL ni envía correos. Las pruebas de integración sí requieren una base descartable y deben ejecutarse con su configuración de prueba.

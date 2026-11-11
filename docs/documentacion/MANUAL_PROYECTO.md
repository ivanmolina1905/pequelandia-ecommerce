# Pequelandia

## Documentación técnica y guía de lectura

Edición del 1 de noviembre de 2026. Aplicación web de comercio electrónico de indumentaria infantil.

Este manual explica la arquitectura, las entidades, los recorridos de negocio, la configuración y las pruebas. La referencia REFERENCIA_COMPLETA.html acompaña al manual con el código numerado y explicaciones de tipos, métodos, atributos, parámetros, variables locales e instrucciones Java, además de los recursos propios por línea. Se puede abrir sin conexión en un navegador y buscar por nombre de archivo, clase, método o contenido.

## 1. Cómo estudiar el proyecto

Leer primero PequelandiaApplication, luego WebConfig y los interceptores. Continuar con las entidades Usuario, Producto, Carrito_de_compra, Detalle_carrito y Pedido. Para seguir una compra, abrir CatalogoController, CarritoController y PedidoService. Consultar los repositorios para comprender qué consulta se ejecuta y las plantillas para ver cómo se presenta el resultado.

Los comentarios que empiezan por [Doc] describen el código existente. Los Javadoc de clase explican su responsabilidad; los de método explican la operación y sus entradas y salida; los comentarios de variables explican el dato conservado. Los comentarios de instrucciones describen condiciones, bucles, retornos, consultas y modificaciones. La referencia separa también las instrucciones que comparten una misma línea del archivo.

Los nombres de getters y setters generados por Lombok no aparecen como declaraciones escritas: Lombok los agrega al compilar. @Getter permite leer un atributo y @Setter permite asignarlo. Los métodos explícitos equivalentes sí se incluyen en el inventario. Una clase puede tener constructores sobrecargados y métodos con el mismo nombre pero entradas diferentes.

## 2. Tecnologías y arquitectura

El proyecto utiliza Java 22 y Maven; Spring Boot 3.5.4 inicia y configura la aplicación. Spring MVC recibe las peticiones, Thymeleaf produce HTML y Spring Data JPA/Hibernate accede a PostgreSQL. JdbcTemplate ejecuta operaciones SQL específicas, especialmente las de correo, recuperación, seguimiento y alertas. Bootstrap 5.3.5 aporta estilos y componentes de interfaz. Apache POI crea Excel y OpenPDF crea el PDF de los pedidos. JavaMailSender realiza la entrega SMTP.

Recorrido general: navegador -> controlador -> servicio -> repositorio o JdbcTemplate -> PostgreSQL. El controlador devuelve una plantilla, una redirección, JSON o un archivo. El servicio concentra las decisiones de negocio. Los repositorios encapsulan consultas y persistencia. Las entidades modelan datos persistentes; los DTO transportan resultados de formularios, consultas o paneles.

Spring crea los componentes anotados y les entrega sus dependencias mediante constructores o @Autowired. @Controller atiende páginas y acciones MVC; @ResponseBody hace que el retorno se escriba en la respuesta. @Service identifica lógica de aplicación; @Configuration registra configuración. @Transactional delimita la transacción cuando la llamada entra por el proxy de Spring; una llamada interna de la misma instancia no crea por sí sola una nueva transacción independiente.

## 3. Estructura de carpetas

src/main/java/com/pequelandia/pequelandia contiene config, controller, service, repository, model, dto y util. src/main/resources/templates contiene las pantallas Thymeleaf y sus fragmentos. static/js y static/css contienen lógica y estilos propios. static/bootstrap-5.3.5-dist contiene una dependencia de terceros. static/imagenes contiene recursos gráficos.

src/main/resources/db/notificaciones.sql contiene el esquema aditivo de correo, seguimiento, alertas y recuperación. src/test/java contiene pruebas de servicios, interceptores, controladores e integración; src/test/js contiene pruebas del navegador simulado. pom.xml declara dependencias y compilación, compose.yaml facilita PostgreSQL local y application-example.properties sirve como ejemplo de configuración. config contiene configuración privada local que no debe compartirse.

Los archivos generados de target, las herramientas temporales de .tools, el historial de Git y las configuraciones personales de Eclipse no forman parte de la lógica propia que se explica por instrucción. Los binarios, imágenes y archivos de Bootstrap se identifican como recursos o dependencias; no se modifica su implementación.

## 4. Modelo de datos

Usuario representa la cuenta, nombre, apellido, DNI, correo, teléfono, dirección, fechas, contraseña, localidad y puntos. Mantiene relaciones con carritos y pedidos. La contraseña se excluye de la serialización JSON; su almacenamiento actual sigue siendo sin hash. Esa decisión del proyecto no debe confundirse con el hash de tokens de recuperación.

Producto representa nombre, descripción, stock, costo, precio, fechas, estado, marca, modelo, talle e imagen. Se relaciona con varias categorías mediante producto_categoria y con las líneas que lo compran. Categoria contiene nombre y descripción. Localidad contiene nombre y código postal.

Carrito_de_compra relaciona al usuario con sus Detalle_carrito, el total y un pedido opcional. Detalle_carrito contiene producto, cantidad y precio guardado de la línea. Pedido vincula usuario, carrito, fecha, estado, comprobante y notificaciones. Conservar el precio del detalle permite calcular importes de compras anteriores sin sustituirlos por el precio comercial actual.

Comprobante conserva descripción, fecha de venta, total, tipo y número. La emisión actual comprueba que no exista otro comprobante para el pedido y propone el número como count() + 1. Este mecanismo no equivale a una secuencia fiscal ni garantiza numeración única bajo concurrencia sin otras restricciones de la base.

Notificacion conserva destinatario, descripción, fecha y pedido opcional. NotificacionLectura conserva la lectura por destinatario. La ausencia de marca de lectura se interpreta como aviso no leído. Los destinatarios lógicos son ADMIN y USUARIO_ seguido por el ID de la cuenta.

@Entity identifica una entidad; @Id su clave; @GeneratedValue delega la generación; @Column y @Table indican nombres físicos. @ManyToOne, @OneToMany, @ManyToMany y @OneToOne expresan relaciones. mappedBy identifica el lado que mantiene la relación, y @JoinColumn o @JoinTable describen sus columnas o tabla de unión. Las anotaciones completas de cada entidad pueden consultarse en el código de la referencia.

## 5. Registro, inicio de sesión y permisos

GET /auth/registro presenta RegistroUsuarioDTO y localidades. El POST valida campos obligatorios, DNI, presencia de @ en el correo, contraseña mínima de seis caracteres, confirmación, términos, nombre reservado y duplicados. Esta validación de correo es básica. UsuarioService.registrar guarda la cuenta y genera bienvenida y aviso administrativo en una transacción. Un fallo propagado en esa operación puede revertir el conjunto.

POST /auth/login consulta credenciales mediante UsuarioService. Si fallan, vuelve a login con error. Si son válidas, cambia el identificador de sesión, conserva usuario y fusiona el carrito persistente invitado cuando existe CARRITO_ID. Redirige al panel administrativo o al historial/perfil. GET /auth/logout invalida la sesión.

IdentidadService fija el administrador por app.security.admin-id. Si el valor es cero, resuelve una única cuenta admin al arrancar. Las comprobaciones se hacen contra el ID, no contra un nombre enviado desde el navegador. exigirUsuario recarga la cuenta desde la base para comprobar que todavía existe; exigirAdmin exige además el ID administrativo.

WebConfig registra primero CSRF en todas las rutas y luego el control de acceso en rutas de administración, usuarios, paneles, pedidos, notificaciones y finalización del carrito. AccessInterceptor exige sesión, distingue rutas administrativas y verifica titularidad del pedido para clientes. Los controladores y servicios pueden aplicar comprobaciones adicionales. El permiso de una ruta debe estudiarse junto con su registro y sus validaciones internas.

## 6. Carrito de sesión y carrito persistente

CartSession mantiene un Map<Long, Integer>: la clave es el ID del producto y el valor es la cantidad. add acumula unidades positivas, set reemplaza la cantidad o elimina la línea si no es positiva y remove quita un producto. getTotalItems suma unidades; getItems y snapshot devuelven copias. Sus operaciones sincronizadas protegen el mapa dentro de una sesión.

CartSessionUtil administra ese objeto en HttpSession. CarritoController consulta productos para calcular precio por cantidad usando BigDecimal y devuelve datos de actualización al navegador. También existe un carrito persistente gestionado por Carrito_de_compraService y Detalle_carritoService. No deben confundirse la clave CARRITO_ID del carrito persistente y el mapa de CartSession: el código conserva ambos mecanismos.

Al finalizar desde CarritoController se exige una cuenta y se utiliza el mutex de sesión para serializar compras simultáneas de esa sesión. Se entrega una copia del mapa a PedidoService.comprar. El carrito de sesión se vacía después de una compra exitosa. Los errores no deben documentarse como compras confirmadas.

## 7. Compra, importes y concurrencia

PedidoService.comprar rechaza un mapa vacío, obtiene al usuario, crea un carrito persistente y procesa productos en orden de ID. El orden estable reduce la posibilidad de interbloqueos al adquirir varios productos. Guarda las líneas con el precio del producto y delega finalizarCompra.

finalizarCompra bloquea y refresca el carrito, verifica titularidad y busca un pedido ya asociado. Si lo encuentra, devuelve ese pedido. En caso contrario obtiene líneas, agrupa cantidades y bloquea los productos con escritura pesimista. Rechaza estados inactivo o discontinuado, stock insuficiente y precios negativos o no finitos.

Después descuenta stock, evalúa alertas, calcula el total con los precios guardados, crea el pedido en PREPARANDO, vincula carrito y pedido, registra seguimiento y genera avisos. Estas acciones participan en la transacción del servicio. Los fallos propagados cubiertos por las reglas de rollback permiten revertir los cambios relacionados.

BigDecimal evita acumular importes únicamente con aritmética binaria de punto flotante, pero el modelo todavía utiliza float para algunos precios. El servicio convierte el precio guardado mediante Float.toString. Math.addExact detecta desbordamientos de enteros al agregar cantidades. Los bloqueos de PostgreSQL protegen el stock entre distintas sesiones; el mutex HTTP por sí solo no lo haría.

## 8. Estados de pedido y cancelación

Los estados reconocidos son PENDIENTE, PREPARANDO, ENVIADO, ENTREGADO, CANCELACION_SOLICITADA y CANCELADO. La normalización acepta PREPARACIÓN o PREPARACION como PREPARANDO. Repetir el mismo estado devuelve el pedido sin crear una nueva transición. CANCELADO y ENTREGADO son estados finales.

Desde PENDIENTE se permite PREPARANDO, CANCELACION_SOLICITADA o CANCELADO. Desde PREPARANDO se permite ENVIADO, ENTREGADO, CANCELACION_SOLICITADA o CANCELADO. Desde ENVIADO se permite ENTREGADO, CANCELACION_SOLICITADA o CANCELADO. Desde CANCELACION_SOLICITADA se permite PREPARANDO, ENVIADO o CANCELADO.

solicitarCancelacion verifica el propietario. confirmarCancelacion exige una solicitud previa, salvo que ya esté cancelado. transicionar repone las cantidades al llegar a CANCELADO, evalúa alertas y guarda seguimiento y avisos. El bloqueo del pedido y la comprobación del estado impiden una segunda reposición por una confirmación repetida dentro del flujo implementado.

SeguimientoPedidoService escribe los eventos en pedido_historial. Si un pedido antiguo cambia por primera vez y no tiene historial, agrega su estado anterior con fecha nula: no inventa una fecha. seguimiento-pedido.js muestra la secuencia con texto DOM y formatea fechas para Buenos Aires.

## 9. Notificaciones, correo y alertas

NotificacionService crea avisos para administración y usuario según alta, pedido, solicitud o cambio de estado. La campana consulta hasta doce avisos y sus marcas de lectura. Marcar una notificación exige que su destinatario coincida con quien realiza la acción. El historial completo se pagina mediante CentroAvisosService en grupos de veinte registros.

CorreoService compone los mensajes y ColaCorreoService los inserta en correo_pendiente dentro de la transacción activa. CorreoScheduler se habilita con app.mail.delivery-enabled y, por defecto, sondea cada cinco segundos. Cada ejecución intenta procesar hasta diez mensajes.

EntregaCorreoService elige un mensaje listo con FOR UPDATE SKIP LOCKED. JavaMailSender intenta entregarlo. Ante un fallo SMTP se incrementan intentos y se calcula una espera exponencial que llega como máximo a una hora. Ante aceptación se registra enviado_en. El panel permite adelantar un reintento fallido todavía no enviado. La aceptación SMTP no prueba recepción en el buzón final y un fallo entre el envío externo y el commit puede producir reentrega: no hay una garantía absoluta de envío exactamente una vez.

AlertaStockService utiliza app.stock.minimo, cuyo valor por defecto es cinco. producto_alerta_stock mantiene si el episodio ya está activo para evitar repetir avisos mientras continúa bajo el umbral. Al reponer por encima del mínimo o al dejar de comercializar el producto se desactiva el episodio; una caída posterior puede generar una alerta nueva.

## 10. Recuperación de contraseña

RecuperacionController presenta solicitud y restablecimiento con mensajes generales. RecuperacionService valida la URL pública: exige HTTPS, con excepción de HTTP en localhost o 127.0.0.1; rechaza credenciales, consulta o fragmento en la URL. Los enlaces se construyen a partir de esa configuración.

La solicitud limita por IP a diez intentos por quince minutos, por correo a tres por hora y aplica una espera de un minuto entre correos. Limpia registros antiguos, exige una única cuenta con ese correo, genera treinta y dos bytes aleatorios y conserva solo SHA-256 del token en recuperacion_clave. El enlace lleva el token original, vence a los treinta minutos y reemplaza al anterior.

El restablecimiento limita a veinte intentos por IP cada quince minutos. Exige formato de token, contraseña de ocho a ciento veintiocho caracteres, contenido no vacío y confirmación coincidente. Bloquea la cuenta, comprueba vigencia y correo, guarda la nueva contraseña, elimina el token y encola el aviso. El hash del token protege la referencia de recuperación; las contraseñas de cuentas mantienen el almacenamiento actual del proyecto.

## 11. Vistas y JavaScript

Thymeleaf procesa las plantillas en el servidor. th:text escribe texto escapado, th:each repite elementos, th:if y th:unless controlan inclusión, th:field vincula formularios, th:href y th:src construyen enlaces y recursos. Los atributos Model que agrega un controlador permiten que la plantilla acceda a datos de la operación.

Las vistas principales cubren inicio, catálogo, carrito, login, registro, recuperación, restablecimiento, paneles, detalle de pedido, historial y correos. templates/html contiene vistas adicionales o alternativas que deben leerse según el controlador que realmente las devuelve. fragments/centro-avisos.html reúne el componente compartido de avisos. La existencia de una plantilla no garantiza que una ruta la utilice.

seguridad.js añade el token a formularios y solicitudes de modificación. historial-notificaciones.js maneja acciones del historial. seguimiento-pedido.js construye la línea de tiempo. Algunas pantallas contienen JavaScript y CSS embebidos: la referencia los incluye junto con cada HTML. Los recursos CSS propios separan estilo general, seguimiento, centro de avisos y administración de avisos.

En JavaScript, const impide reasignar la referencia, aunque el objeto puede seguir siendo mutable; let permite reasignar. addEventListener registra un callback. fetch inicia una solicitud HTTP y las promesas o await esperan su resultado. document.createElement crea nodos y textContent escribe texto. En CSS, el selector identifica los elementos y cada propiedad del bloque define su presentación.

## 12. Estadísticas y exportaciones

EstadisticasService reúne totales, series y productos relevantes. PanelUsuarioService construye el resumen individual. Las series admiten agrupaciones por día, mes y año y utilizan DTO específicos. Para interpretar ventas es necesario revisar la consulta concreta: el estado del pedido y la fuente del importe determinan qué se cuenta.

ExcelExportController prepara las descargas y ExcelExportService escribe libros con Apache POI. Incluyen entidades y estadísticas de ventas, productos vendidos y stock bajo. Util recibe encabezados y filas y genera un libro que conserva tipos numéricos y aplica formato a fechas. PedidoController.descargarPdf usa OpenPDF y los precios guardados en el detalle para presentar producto, cantidad, subtotal y total. El archivo descargado no constituye una integración de pagos ni una factura fiscal autorizada.

## 13. Esquema aditivo y configuración

correo_pendiente conserva destinatario, asunto, contenido, intentos, proximo_intento y enviado_en. pedido_historial conserva ID de pedido, estado y fecha opcional. producto_alerta_stock conserva ID de producto y episodio activo. recuperacion_clave conserva hash, ID de usuario, correo y vencimiento. recuperacion_limite conserva hash de clave, inicio e intentos.

El script se configura con spring.sql.init.mode=always y usa CREATE TABLE IF NOT EXISTS. Es aditivo y no crea por sí solo todo el esquema comercial. La configuración principal usa spring.jpa.hibernate.ddl-auto=validate: requiere las tablas comerciales previas. El ejemplo de desarrollo permite update sobre una base nueva y descartable.

PEQUELANDIA_DB_URL, PEQUELANDIA_DB_USERNAME y PEQUELANDIA_DB_PASSWORD definen la conexión. PEQUELANDIA_MAIL_HOST, PEQUELANDIA_MAIL_PORT, PEQUELANDIA_MAIL_USERNAME y PEQUELANDIA_MAIL_PASSWORD definen SMTP. PEQUELANDIA_ADMIN_EMAIL define el correo administrativo y PEQUELANDIA_ADMIN_ID el ID autorizado.

PEQUELANDIA_MAIL_DELIVERY_ENABLED activa el worker; PEQUELANDIA_STOCK_MINIMO configura el umbral y PEQUELANDIA_PUBLIC_URL la base de los enlaces. app.mail.poll-ms permite ajustar el sondeo. application.properties importa opcionalmente config/application-local.properties; no se deben copiar credenciales privadas al manual, a capturas ni al repositorio.

## 14. Ejecución y comprobaciones

Instalar JDK 22 y disponer de PostgreSQL. Crear config/application-local.properties a partir del ejemplo y completar valores para una base de desarrollo. Desde la carpeta que contiene pom.xml, ejecutar .\mvnw.cmd compile y .\mvnw.cmd spring-boot:run. Maven necesita dependencias ya disponibles o acceso al repositorio configurado. Docker Compose es una alternativa para PostgreSQL local.

Para las pruebas Java ejecutar .\mvnw.cmd test. Los casos de integración heredan soporte que requiere PEQUELANDIA_TEST_DB_URL con una URL JDBC de localhost o 127.0.0.1, puerto explícito y una base descartable llamada pequelandia_verificacion. Una URL ausente o incompatible puede impedir inicializar el contexto; comprobar en el informe si cada suite se ejecutó, se omitió o falló. No configurar esta prueba contra una base con información que deba conservarse.

Para las pruebas JavaScript ejecutar node --test src/test/js/seguridad.test.cjs src/test/js/estado-pedido.test.cjs. Los casos comprueban seguridad del navegador simulado y presentación de estados. Las pruebas unitarias y de integración cubren aspectos diferentes; su mera existencia no implica que se hayan ejecutado en una revisión concreta.

Los informes de auditoría y guías de docs registran pruebas anteriores. Para el resultado de la edición documental, consultar VERIFICACION.md, que distingue compilación, pruebas ejecutadas, casos omitidos y comprobaciones estáticas.

## 15. Glosario para leer instrucciones

Un atributo conserva estado en una instancia o clase. Un parámetro recibe un dato del llamador y una variable local existe durante un bloque. final impide reasignar; static pertenece al tipo. null representa ausencia de referencia. Optional obliga a tratar explícitamente que una búsqueda no encuentre un valor.

if elige un camino por una condición; for y while repiten; switch selecciona una rama. return termina la función devolviendo un resultado si corresponde. throw propaga un fallo. try y catch delimitan su tratamiento. synchronized serializa el acceso al monitor indicado. Una excepción capturada y no propagada no produce automáticamente los mismos efectos de rollback que una excepción que abandona el servicio transaccional.

List conserva elementos ordenados, Set evita duplicados y Map relaciona claves con valores. stream encadena transformaciones: filter selecciona, map transforma, collect o toList reúnen y reduce agrega. Una lambda expresa una función que se entrega a otra operación. new construye un objeto y this distingue los atributos de la instancia de parámetros del mismo nombre.

En SQL, SELECT consulta, INSERT agrega, UPDATE modifica y DELETE elimina. WHERE filtra, JOIN relaciona, GROUP BY agrupa y ORDER BY ordena. Los signos ? son parámetros vinculados por JDBC. FOR UPDATE protege filas durante la transacción y SKIP LOCKED permite a otro worker omitir filas ocupadas. Las consultas completas se consultan en las anotaciones @Query o en los bloques JdbcTemplate de la referencia.

## 16. Mantenimiento de esta documentación

El catálogo técnico del PDF y la referencia HTML se obtienen del árbol sintáctico del JDK. docs/herramientas/documentar.py regenera comentarios marcados [Doc], inventario y referencia; docs/herramientas/DocScan.java extrae posiciones y declaraciones sin ejecutar la aplicación. Se necesita JAVA_HOME apuntando al JDK y Python. Los comentarios anteriores del proyecto se conservan.

Tras un cambio de lógica deben revisarse las descripciones de negocio específicas, regenerar la referencia y volver a generar el PDF con docs/herramientas/crear_manual.py. Los números de línea corresponden a esa generación y deben actualizarse si se modifica el código. cobertura.json registra los elementos extraídos y las verificaciones de equivalencia sintáctica. Los archivos generados no deben editarse para simular funciones que el código no implementa.

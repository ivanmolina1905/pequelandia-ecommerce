"""Genera comentarios y referencia local a partir de la sintaxis real de Java.

Ejecutar desde la raíz del proyecto con Python del runtime de artefactos.
El escáner requiere JDK 22. No conecta con la base ni ejecuta la aplicación.
"""
from pathlib import Path
import base64
import collections
import hashlib
import html
import json
import os
import re
import subprocess
import textwrap

ROOT = Path(__file__).resolve().parents[2]
WORK = ROOT / 'tmp/documentacion'
OUT = ROOT / 'docs/documentacion'
WORK.mkdir(parents=True, exist_ok=True)
OUT.mkdir(parents=True, exist_ok=True)
JDK = Path(os.environ.get('JAVA_HOME', 'C:/Program Files/Java/jdk-22')) / 'bin'

ROLES = {
 'AccessInterceptor': 'Comprueba sesión, permisos administrativos y titularidad del pedido antes de ejecutar la ruta protegida.',
 'CsrfInterceptor': 'Genera el token de sesión y valida su envío en las peticiones que modifican datos.',
 'WebConfig': 'Registra los interceptores CSRF y de acceso y determina las rutas a las que se aplican.',
 'CarritoSesionFilter': 'Serializa mediante el mutex de sesión las operaciones POST de las rutas de carrito configuradas.',
 'CorreoScheduler': 'Procesa periódicamente la cola de correos cuando la entrega está habilitada.',
 'ErroresApi': 'Traduce excepciones de acceso, búsqueda y validación a respuestas HTTP.',
 'PedidoService': 'Coordina compra, bloqueo de stock, cálculo del total, transiciones, reposición, seguimiento y avisos dentro de transacciones.',
 'RecuperacionService': 'Gestiona enlaces de recuperación de uso único, vencimiento, límites persistentes y cambio de contraseña.',
 'IdentidadService': 'Resuelve la identidad desde la base y autoriza al administrador mediante un ID fijado al iniciar.',
 'ColaCorreoService': 'Inserta correos pendientes en la misma transacción de la operación que los origina.',
 'EntregaCorreoService': 'Toma un correo pendiente con bloqueo, intenta enviarlo por SMTP y registra envío o próxima fecha de reintento.',
 'NotificacionService': 'Crea avisos por destinatario, encola correos y administra las marcas de lectura.',
 'CentroAvisosService': 'Consulta avisos y correos con filtros y páginas de veinte registros y adelanta reintentos permitidos.',
 'AlertaStockService': 'Registra episodios de stock bajo para avisar una vez y rearmar la alerta cuando se repone.',
 'SeguimientoPedidoService': 'Persiste y consulta los cambios de estado; conserva la ausencia de fechas históricas cuando no existen eventos.',
 'CompraService': 'Transforma pedidos y detalles en resúmenes de compras para presentar el historial.',
 'CorreoService': 'Compone los mensajes de bienvenida, pedidos y cancelaciones y delega su almacenamiento en la cola.',
 'ExcelExportService': 'Construye libros Excel de usuarios, productos, pedidos, series, ventas y stock bajo.',
 'EstadisticasService': 'Construye indicadores administrativos y series de ventas a partir de consultas de repositorio.',
 'PanelUsuarioService': 'Reúne gasto, pedidos, cantidades y series mensuales del usuario para su panel.',
 'Carrito_de_compraService': 'Gestiona carritos persistentes, líneas, totales y fusión del carrito invitado, protegiendo los carritos históricos.',
 'Detalle_carritoService': 'Gestiona las líneas de un carrito editable y recalcula cantidades y precios.',
 'ComprobanteService': 'Emite un comprobante asociado al pedido y comprueba la existencia de una emisión previa.',
 'ProductoService': 'Gestiona productos, búsquedas del catálogo, edición parcial y evaluación de alertas de stock.',
 'UsuarioService': 'Gestiona cuentas, credenciales, duplicados y registro transaccional con bienvenida y aviso administrativo.',
 'CategoriaService': 'Consulta categorías y resuelve selecciones por identificador.',
 'LocalidadService': 'Consulta las localidades usadas al registrar o editar una cuenta.',
 'CartSession': 'Mantiene en memoria de sesión un mapa de producto a cantidad y permite obtener copias del carrito.',
 'CartSessionUtil': 'Obtiene, crea o vacía el carrito almacenado en HttpSession.',
 'PequelandiaApplication': 'Inicia Spring Boot y habilita el descubrimiento de componentes y las tareas programadas.',
 'LoginController': 'Presenta registro y login, valida formularios, renueva la sesión al ingresar y coordina la fusión del carrito.',
 'CarritoController': 'Actualiza el carrito de sesión, calcula importes para la vista y delega la compra en el servicio de pedidos.',
 'CatalogoController': 'Presenta el catálogo filtrado y permite agregar productos al carrito.',
 'PedidoController': 'Expone el detalle y PDF de un pedido y las operaciones de solicitud y confirmación de cancelación.',
 'CentroAvisosController': 'Presenta el historial de avisos y el panel de correos y exige la identidad correspondiente a cada acción.',
 'RecuperacionController': 'Presenta los formularios de recuperación y restablecimiento y aplica respuestas que preservan la privacidad.',
 'NotificacionController': 'Expone el listado y las operaciones de lectura de avisos para el destinatario de la sesión.',
 'HomeController': 'Resuelve páginas iniciales y redirecciones a las vistas principales.',
 'PanelController': 'Resuelve los accesos a los paneles de administración y usuario.',
 'PanelUsuarioController': 'Construye el panel personal, presenta compras y permite editar perfil y solicitar cancelaciones.',
 'AdminUsuarioController': 'Gestiona las cuentas desde administración y consulta su historial de compras.',
 'AdminProductoController': 'Gestiona creación, edición, listado y eliminación de productos desde administración.',
 'AdminPedidoController': 'Presenta pedidos para administración y delega los cambios de estado.',
 'EstadisticasController': 'Publica el resumen administrativo y las series de ventas por día, mes y año.',
 'ExcelExportController': 'Prepara respuestas de descarga Excel para entidades e indicadores.',
 'UsuarioController': 'Expone la consulta de usuarios mediante el servicio de cuentas.',
 'Usuario': 'Representa la cuenta y sus datos personales, localidad, carritos, pedidos y puntos.',
 'Producto': 'Representa el artículo comercial, sus atributos, categorías, stock y precios.',
 'Pedido': 'Representa una compra con usuario, fecha, estado, carrito, comprobante y avisos.',
 'Carrito_de_compra': 'Representa el carrito persistente y relaciona usuario, detalles, total y pedido.',
 'Detalle_carrito': 'Representa una línea con producto, cantidad y precio conservado en el carrito.',
 'Categoria': 'Representa una categoría de clasificación y su relación con productos.',
 'Localidad': 'Representa una localidad y su código postal para asociarlos a una cuenta.',
 'Comprobante': 'Representa los datos y el importe del comprobante asociado a un pedido.',
 'Notificacion': 'Representa un aviso persistente con destinatario, fecha, descripción y pedido opcional.',
 'NotificacionLectura': 'Representa la lectura de un aviso por un destinatario.',
 'Util': 'Genera libros Excel a partir de encabezados y filas, conservando números y aplicando formato a fechas.',
}

GLOSSARY = {
 'id': 'identificador del registro', 'idProducto': 'identificador del producto', 'productoId': 'identificador del producto',
 'idPedido': 'identificador del pedido', 'usuarioId': 'identificador del usuario', 'idUsuario': 'identificador del usuario',
 'carritoId': 'identificador del carrito persistente', 'idCarrito': 'identificador del carrito persistente',
 'stock': 'unidades disponibles del producto', 'cantidad': 'unidades de una línea de carrito', 'qty': 'unidades de una línea',
 'cant': 'cantidad de unidades', 'precio': 'importe unitario usado en el cálculo', 'costo': 'costo del producto',
 'total': 'importe acumulado', 'subtotal': 'importe de una línea: precio por cantidad', 'sub': 'subtotal de la línea',
 'session': 'sesión HTTP que conserva identidad, carrito y token CSRF', 'request': 'petición HTTP entrante',
 'response': 'respuesta HTTP que se envía al navegador', 'model': 'atributos que recibe la plantilla Thymeleaf',
 'jdbc': 'acceso SQL parametrizado mediante JdbcTemplate', 'em': 'contexto JPA utilizado para refrescar entidades con bloqueo',
 'password': 'contraseña recibida para la operación de cuenta', 'contraseña': 'contraseña de la cuenta, actualmente conservada sin hash',
 'token': 'valor del token usado por esta operación', 'destinatario': 'identidad o dirección que recibirá el aviso o correo',
 'email': 'dirección de correo de la cuenta', 'ip': 'dirección IP usada para limitar solicitudes de recuperación',
 'baseUrl': 'URL pública de base para construir enlaces de recuperación', 'pagina': 'índice de página solicitado, comenzando en cero',
 'pageable': 'parámetros de paginación y orden', 'estado': 'estado usado por la operación', 'nuevo': 'estado de destino normalizado',
 'anterior': 'estado previo normalizado', 'items': 'elementos del carrito o de la colección procesada',
 'detalles': 'líneas asociadas al carrito', 'pedidos': 'pedidos o servicio de pedidos según el tipo declarado',
 'configurado': 'ID administrativo recibido de la configuración', 'administradorId': 'ID administrativo fijado al inicializar',
 'KEY': 'clave de atributo usada para localizar el valor en la sesión', 'SAFE': 'métodos HTTP que no requieren token de modificación',
 'ESTADOS': 'conjunto de estados de pedido reconocidos', 'SDF': 'formato de fecha utilizado en los resúmenes',
 'sender': 'cliente JavaMailSender que entrega mensajes SMTP', 'emisor': 'dirección remitente configurada',
 'intentos': 'cantidad de intentos de entrega ya registrados', 'espera': 'segundos hasta el próximo reintento',
 'parametros': 'valores que se vinculan a los marcadores de la consulta SQL', 'bindingResult': 'errores de conversión y enlace del formulario',
 'dto': 'objeto de transferencia con los datos de la operación', 'dtoRegistro': 'datos recibidos del formulario de registro',
 'handler': 'manejador MVC que atenderá la petición', 'registry': 'registro de interceptores MVC',
 'fecha': 'fecha o etiqueta temporal de la operación', 'desde': 'inicio del intervalo consultado', 'hasta': 'fin del intervalo consultado',
 'anio': 'año de la consulta', 'mes': 'mes de la consulta', 'inicio': 'límite inicial del intervalo', 'fin': 'límite final del intervalo',
 'confirmacion': 'segunda copia de la contraseña para comprobar coincidencia', 'log': 'registrador de eventos de la clase',
 'random': 'generador aleatorio criptográfico para tokens de recuperación', 'nombre_producto': 'nombre visible del producto',
 'fecha_alta': 'fecha de alta del producto', 'fecha_baja': 'fecha de baja del producto',
 'fecha_nacimiento': 'fecha de nacimiento de la persona', 'fecha_registro': 'fecha de creación de la cuenta',
 'ultima_conexion': 'fecha de última conexión almacenada', 'dni': 'documento de identidad', 'puntos': 'puntos almacenados de la cuenta',
 'localidad': 'localidad asociada a la cuenta', 'localidadId': 'identificador de localidad elegido en el formulario',
 'aceptarTerminos': 'aceptación de términos del registro', 'confirmarContraseña': 'confirmación de contraseña del registro',
 'imagenUrl': 'ubicación de la imagen mostrada en el catálogo', 'categorias': 'categorías asociadas al producto',
}

SPECIAL = {
 'AccessInterceptor.rechazar': 'Escribe el estado HTTP de rechazo y una respuesta JSON con error y redirección al login cuando falta sesión.',
 'CarritoSesionFilter.doFilterInternal': 'Si la petición es POST y su ruta está en RUTAS, ejecuta la cadena bajo el mutex de sesión; en los demás casos continúa sin ese bloqueo.',
 'ErroresApi.acceso': 'Convierte una excepción de estado HTTP en una respuesta de error con el estado correspondiente.',
 'ErroresApi.inexistente': 'Convierte la ausencia de una entidad en una respuesta HTTP de recurso no encontrado.',
 'ErroresApi.invalido': 'Convierte una operación inválida en una respuesta de error de validación.',
 'AdminPedidoController.lista': 'Consulta pedidos con los filtros administrativos recibidos y prepara su listado en el panel.',
 'AdminProductoController.crear': 'Asocia las categorías elegidas, aplica el estado activo por defecto y delega el guardado antes de redirigir al listado.',
 'AdminUsuarioController.initBinder': 'Configura la conversión de valores del formulario antes de vincularlos a objetos Java.',
 'AdminUsuarioController.crearUsuario': 'Recibe los datos de una cuenta creada desde administración y delega su validación y guardado.',
 'AdminUsuarioController.historialCompras': 'Devuelve los resúmenes de compras del usuario seleccionado para la consulta administrativa.',
 'AdminUsuarioController.detalleCompra': 'Devuelve los productos, cantidades y precios del pedido seleccionado en el historial.',
 'CarritoController.apiCantidad': 'Valida la cantidad contra el stock del producto, actualiza el carrito de sesión y devuelve contador e importe total o un error.',
 'CarritoController.apiEliminar': 'Quita un producto del carrito de sesión y recalcula contador e importe para actualizar la pantalla.',
 'CarritoController.verCarrito': 'Construye las líneas con producto, cantidad y subtotal, calcula el total y renderiza la plantilla carrito.',
 'CatalogoController.catalogo': 'Aplica búsqueda, filtros, orden y paginación del catálogo y expone los productos a su plantilla.',
 'CatalogoController.apiAgregar': 'Valida el producto y la cantidad solicitados y agrega unidades al carrito de sesión mediante la API.',
 'CentroAvisosController.destinatario': 'Exige una cuenta vigente y obtiene ADMIN o USUARIO_ seguido de su ID según sus permisos.',
 'CentroAvisosController.historial': 'Consulta la página de avisos del destinatario, su contador y filtro; presenta el panel administrativo o el historial del cliente.',
 'CentroAvisosController.leer': 'Valida el filtro, marca el aviso del destinatario como leído y vuelve al historial conservando filtro y página.',
 'CentroAvisosController.leerTodas': 'Marca los avisos del destinatario como leídos y vuelve al historial con sus filtros.',
 'CentroAvisosController.correos': 'Exige administración, limita y normaliza la búsqueda y presenta la página de correos filtrados dentro del panel.',
 'CentroAvisosController.panelAdmin': 'Añade la sección activa y el resumen estadístico al modelo y renderiza paneladministracion.',
 'CentroAvisosController.reintentar': 'Exige administración y solicita adelantar la próxima entrega de un correo pendiente que admite reintento.',
 'EstadisticasController.dashboard': 'Carga el resumen de indicadores, activa la sección estadísticas y renderiza el panel administrativo.',
 'EstadisticasController.serieDia': 'Consulta la serie de ventas del intervalo de fechas para alimentar el gráfico diario.',
 'EstadisticasController.serieMes': 'Consulta la serie de ventas agrupadas por mes para el año solicitado.',
 'EstadisticasController.serieAnio': 'Consulta la serie de ventas por año dentro del intervalo solicitado.',
 'ExcelExportController.buildExcel': 'Construye una respuesta HTTP con los bytes del libro, el tipo MIME XLSX y la cabecera de descarga con el nombre indicado.',
 'ExcelExportController.excelTopProductos': 'Genera y devuelve la descarga Excel de productos más vendidos.',
 'ExcelExportController.excelStockBajo': 'Genera y devuelve la descarga Excel de productos con stock bajo.',
 'HomeController.home': 'Consulta ocho productos aleatorios, los expone como productosRandom y presenta indexv1.',
 'HomeController.login': 'Presenta la plantilla login para las rutas de acceso compatibles.',
 'HomeController.panelUsuario': 'Redirige las rutas compatibles del panel personal al historial del usuario.',
 'HomeController.panelAdmin': 'Redirige las rutas compatibles de administración al listado administrativo de usuarios.',
 'HomeController.carrito': 'Presenta la plantilla carrito desde las rutas de acceso compatibles.',
 'HomeController.verProductosRedirect': 'Redirige las rutas antiguas de productos a /productos.',
 'LoginController.loginForm': 'Presenta la plantilla del formulario de inicio de sesión.',
 'LoginController.logout': 'Invalida la sesión HTTP y redirige al formulario de login.',
 'LoginController.mostrarRegistro': 'Prepara un DTO vacío y el listado de localidades para presentar el formulario de registro.',
 'PanelController.panelUsuario': 'Exige sesión y redirige al historial personal con la sección de perfil activa.',
 'PanelController.panelAdmin': 'Comprueba la sesión, consulta el resumen estadístico y redirige al listado administrativo de usuarios.',
 'PanelController.index': 'Presenta la plantilla indexv1 desde su ruta alternativa bajo /panel.',
 'PanelUsuarioController.panelUsuario': 'Reúne perfil, pedidos y resumen del usuario para presentar su panel e historial personal.',
 'PedidoController.detalle': 'Devuelve fecha, estado, historial, total y líneas del pedido como JSON; usa el precio guardado de cada detalle.',
 'PedidoController.descargarPdf': 'Escribe la descarga PDF del pedido con sus datos, productos, cantidades, subtotales y total mediante OpenPDF.',
 'RecuperacionController.privacidad': 'Añade cabeceras para impedir almacenamiento en caché, envío de referencia e indexación de los formularios de recuperación.',
 'RecuperacionController.formulario': 'Presenta el formulario para solicitar un enlace de recuperación.',
 'RecuperacionController.nueva': 'Comprueba si el token sigue vigente y presenta el formulario de nueva contraseña con ese resultado.',
 'NotificacionController.respuestaSesionVencida': 'Construye la respuesta de error y redirección para una sesión que ya no permite consultar avisos.',
 'Carrito_de_compraService.actualizarTotal': 'Exige un carrito editable, sustituye un total nulo por cero y guarda el nuevo importe.',
 'Carrito_de_compraService.quitarDetalle': 'Exige un carrito editable, busca la línea del producto y la elimina o informa que no existe.',
 'Carrito_de_compraService.crearCarritoInvitado': 'Guarda un carrito sin usuario con total cero y conserva su ID en el atributo CARRITO_ID de sesión.',
 'Carrito_de_compraService.mergeCarritos': 'Exige origen invitado y destino de usuario editables, acumula sus líneas, elimina el origen y recalcula el total del destino.',
 'Carrito_de_compraService.items': 'Consulta la suma de unidades de las líneas del carrito persistente.',
 'Carrito_de_compraService.total': 'Consulta el importe del carrito y lo convierte a BigDecimal con dos decimales y redondeo HALF_UP; el parámetro fallback no se utiliza.',
 'Detalle_carritoService.upsert': 'Exige un carrito editable y una cantidad positiva; busca o crea la línea, acumula unidades y actualiza el precio desde el producto. No recalcula el total del carrito.',
 'Detalle_carritoService.exigirEditable': 'Bloquea y refresca el carrito y rechaza la modificación si ya pertenece a un pedido.',
 'CentroAvisosService.filtro': 'Exige que el valor pertenezca al conjunto de opciones permitidas; de lo contrario responde con HTTP 400.',
 'CentroAvisosService.notificaciones': 'Construye la consulta por destinatario y filtro de lectura y devuelve una página de hasta veinte avisos.',
 'CentroAvisosService.correos': 'Filtra la cola por destinatario o asunto y estado de entrega y devuelve una página con sus datos de reintento.',
 'CompraService.mapearAPedidoResumen': 'Convierte un pedido en su ID, fecha formateada e importe calculado para el resumen de compra.',
 'CompraService.mapearADetalleProducto': 'Convierte una línea de carrito en nombre de producto, cantidad y precio unitario para mostrar la compra.',
 'EstadisticasService.nombreMes': 'Convierte el número de mes en la etiqueta que utiliza la serie del gráfico.',
 'IdentidadService.validarNombre': 'Reserva admin para la cuenta administrativa y exige configurar su ID antes de renombrarla en modo de compatibilidad.',
 'NotificacionService.destinatarioUsuario': 'Construye la identidad lógica USUARIO_ seguida por el ID del usuario.',
 'NotificacionService.crear': 'Construye un aviso con fecha actual, destinatario, descripción y pedido opcional y lo guarda.',
 'PanelUsuarioService.build': 'Calcula gasto, unidades, cantidad y promedio de pedidos, obtiene el último pedido y completa los doce meses del año con sus importes.',
 'RecuperacionService.formatoToken': 'Comprueba que el token tenga cuarenta y tres caracteres válidos de Base64 URL sin relleno.',
 'Util.generarExcel': 'Crea un libro XLSX con encabezados y filas; conserva números, convierte booleanos a Sí o No, formatea fechas y ajusta el ancho de columnas.',
 'PedidoService.estado': 'Normaliza espacios y mayúsculas del estado y convierte PREPARACIÓN o PREPARACION en PREPARANDO.',
 'AlertaStockService.evaluar': 'Desactiva el episodio si hay stock suficiente o el producto no se comercializa; si cruza el umbral, crea una única alerta y encola correo administrativo.',
 'AlertaStockService.eliminar': 'Elimina la marca de episodio de stock asociada al producto.',
 'SeguimientoPedidoService.registrar': 'Inserta el estado nuevo con la fecha actual; para pedidos antiguos sin historial agrega primero el estado anterior con fecha nula.',
 'SeguimientoPedidoService.listar': 'Devuelve los eventos del pedido ordenados por su identificador de inserción.',
 'SeguimientoPedidoService.eliminar': 'Elimina los eventos de seguimiento asociados al pedido.',
 'ComprobanteService.emitir': 'Exige un pedido con total y sin comprobante previo; calcula el número como cantidad de comprobantes más uno y guarda fecha, tipo e importe.',
 'CorreoScheduler.enviarPendientes': 'Intenta procesar hasta diez mensajes por ejecución; termina antes cuando no hay otro mensaje disponible.',
 'CartSession.add': 'Exige ID y cantidad positiva y acumula unidades con detección de desbordamiento mediante Math.addExact.',
 'CartSession.set': 'Asigna la cantidad del producto; si es cero o negativa, elimina la línea.',
 'CartSession.remove': 'Elimina del mapa la línea del producto indicado.',
 'CartSession.getTotalItems': 'Suma las cantidades de todas las líneas del carrito; no cuenta solo los productos distintos.',
 'CartSession.getItems': 'Devuelve una copia no modificable del mapa de productos y cantidades.',
 'CartSession.isEmpty': 'Indica si el mapa del carrito no contiene líneas.',
 'ProductoService.setIfNotNull': 'Ejecuta el setter recibido únicamente cuando el valor no es nulo.',
 'ProductoService.setIfHasText': 'Ejecuta el setter recibido únicamente cuando el texto tiene contenido.',
 'UsuarioService.setIfNotNull': 'Ejecuta el setter recibido únicamente cuando el valor no es nulo.',
 'UsuarioService.setIfHasText': 'Ejecuta el setter recibido únicamente cuando el texto tiene contenido.',
 'PedidoService.comprar': 'Crea un carrito persistente desde la copia de sesión, agrega sus líneas en orden de ID y delega la confirmación transaccional.',
 'PedidoService.finalizarCompra': 'Bloquea y refresca el carrito, exige titularidad, reutiliza un pedido ya creado o valida y descuenta stock; guarda total, seguimiento y avisos.',
 'PedidoService.transicionar': 'Valida el cambio contra los destinos admitidos, repone stock al cancelar y registra el estado y sus avisos en la misma transacción.',
 'PedidoService.bloquearProducto': 'Obtiene el producto con bloqueo pesimista y refresca su estado para comprobar el stock vigente.',
 'PedidoService.bloquearPedido': 'Obtiene el pedido con bloqueo pesimista y refresca su estado antes de una transición.',
 'PedidoService.cantidades': 'Agrupa cantidades por producto en un TreeMap y detecta desbordamientos mediante Math.addExact.',
 'PedidoService.cantidadValida': 'Rechaza cantidades nulas o no positivas y devuelve la cantidad válida.',
 'RecuperacionService.hash': 'Calcula SHA-256 del token o clave de límite y lo devuelve como texto hexadecimal; no transforma contraseñas de usuarios.',
 'RecuperacionService.permitir': 'Incrementa atómicamente un contador persistente dentro de una ventana y devuelve si aún se admite la solicitud.',
 'RecuperacionService.solicitar': 'Aplica límites por IP y correo, identifica una única cuenta, reemplaza su token y encola un enlace que vence a los treinta minutos.',
 'RecuperacionService.vigente': 'Comprueba formato, hash, vencimiento y coincidencia entre correo actual y correo del token.',
 'RecuperacionService.restablecer': 'Valida límites, token y contraseña, bloquea la cuenta y cambia la clave, consume el token y encola el aviso en una transacción.',
 'EntregaCorreoService.entregarSiguiente': 'Selecciona un mensaje listo con FOR UPDATE SKIP LOCKED; intenta SMTP y registra éxito o espera exponencial de hasta una hora.',
 'CarritoController.finalizar': 'Exige usuario, serializa el checkout de la sesión, compra desde una copia del carrito y lo vacía después de la compra exitosa.',
 'LoginController.procesarLogin': 'Valida credenciales, cambia el ID de sesión, conserva la identidad y fusiona el carrito persistente invitado si existe; redirige según permisos.',
 'LoginController.registrarUsuario': 'Valida campos, DNI, correo básico, contraseña, confirmación, términos y duplicados; delega el alta transaccional o vuelve al formulario.',
 'IdentidadService.inicializar': 'Fija el ID administrativo configurado o resuelve la única cuenta admin existente al iniciar.',
 'IdentidadService.esAdmin': 'Compara el ID de la cuenta con el ID administrativo inicializado.',
 'IdentidadService.exigirUsuario': 'Lee el ID de la sesión y recarga la cuenta desde la base; rechaza sesiones sin una cuenta vigente.',
 'IdentidadService.exigirAdmin': 'Exige una cuenta vigente cuyo ID sea el administrador.',
 'CentroAvisosService.reintentar': 'Adelanta la fecha del correo fallido todavía no enviado; no modifica mensajes enviados ni mensajes que ya están listos.',
 'CentroAvisosService.paginar': 'Cuenta resultados, limita el índice a una página existente y devuelve hasta veinte filas con LIMIT y OFFSET.',
 'CartSession.snapshot': 'Devuelve una copia del mapa del carrito para procesar la compra sin compartir el mapa mutable de la sesión.',
 'NotificacionService.marcarComoLeida': 'Exige que el destinatario coincida con el aviso e inserta su marca de lectura.',
 'NotificacionService.listarUltimas': 'Consulta hasta doce avisos del destinatario y los transforma a DTO con su estado de lectura.',
 'ColaCorreoService.encolar': 'Inserta destinatario, asunto y contenido en correo_pendiente dentro de la transacción activa.',
 'UsuarioService.registrar': 'Guarda la cuenta y genera bienvenida y aviso de alta dentro de una transacción.',
 'CsrfInterceptor.preHandle': 'Crea o reutiliza el token CSRF de sesión, lo expone a la vista y compara el token recibido por cabecera o formulario.',
 'AccessInterceptor.preHandle': 'Rechaza falta de sesión o privilegios y comprueba titularidad en rutas de pedidos antes de continuar.',
}

def words(s):
    return re.sub(r'([a-záéíóúñ0-9])([A-ZÁÉÍÓÚÑ])', r'\1 \2', s).replace('_', ' ').strip().lower()

def value_role(name, typ=''):
    if name=='registry' and typ=='DynamicPropertyRegistry':return 'registro de propiedades dinámicas del contexto de pruebas'
    if name in GLOSSARY:
        return GLOSSARY[name]
    if typ.endswith('Repository') or name.endswith(('Repo', 'Repository')):
        return 'acceso de persistencia a ' + words(re.sub(r'^I(?=[A-Z])|Repository$', '', typ))
    if typ.endswith('Service') or name.endswith('Service'):
        return 'colaborador que concentra operaciones de ' + words(re.sub(r'^I(?=[A-Z])|Service$', '', typ))
    if name in ('p', 'p1') and 'Producto' in typ: return 'producto consultado para esta operación'
    if name in ('p',) and 'Pedido' in typ: return 'pedido consultado para esta operación'
    if name in ('u', 'usuario', 'usuarioCreado'): return 'cuenta de usuario utilizada por la operación'
    if name == 'cart': return 'carrito en memoria de la sesión'
    if name == 'e' or name == 'ex': return 'excepción capturada para tratar el fallo'
    if name in ('d', 'item') and 'Detalle' in typ: return 'línea del carrito procesada'
    if name == 'entry': return 'par clave y valor de la colección recorrida'
    entities={'Pedido':'pedido procesado en este punto','Producto':'producto procesado en este punto',
              'Carrito_de_compra':'carrito persistente procesado','Detalle_carrito':'línea persistente del carrito',
              'Notificacion':'aviso que se construye o consulta','Comprobante':'comprobante de la compra',
              'Categoria':'categoría del producto','Localidad':'localidad de la cuenta'}
    if typ in entities:return entities[typ]
    extra={'minimo':'umbral de unidades que activa el aviso de stock bajo','normal':'texto normalizado para comparar valores',
           'nueva':'resultado de la inserción o actualización que indica si comenzó un nuevo episodio',
           'texto':'contenido textual que se construye para presentar o enviar',
           'nombre':'nombre normalizado o visible de la entidad','actual':'valor o entidad vigente antes de aplicar cambios',
           'pedidoId':'identificador del pedido','valor':'valor recibido o calculado para este paso',
           'nombreProd':'nombre del producto mostrado en el detalle de compra','precioUnitario':'precio por unidad de la línea',
           'montoTotal':'importe total del resumen de compra','cambios':'datos nuevos que se aplican a la entidad existente',
           'resultado':'colección o valor que acumula el resultado de la operación','fila':'registro SQL seleccionado',
           'filas':'registros devueltos por la consulta SQL','args':'valores vinculados a los parámetros de consulta',
           'from':'fragmento SQL de tablas, relaciones y condiciones','select':'columnas de la consulta SQL',
           'orden':'criterio SQL de ordenación','ultima':'índice máximo de página calculado para los resultados',
           'uri':'dirección analizada para comprobar la URL pública','ids':'identificadores devueltos por la búsqueda',
           'bytes':'bytes aleatorios o codificados de la operación','mensaje':'mensaje que se prepara para enviar o responder',
           'permitidos':'valores admitidos por la validación o transición','descripcion':'texto descriptivo de la entidad u operación'}
    extra.update({'page':'índice de página solicitado, comenzando en cero','flash':'atributos conservados durante una redirección',
                  'destino':'destinatario o ruta de destino de esta operación','entregaHabilitada':'indicador configurado que habilita la entrega de correos'})
    return extra.get(name,words(name))

def class_role(n):
    name = n['name']
    if name in ROLES: return ROLES[name]
    if name.endswith('Repository'): return 'Declara consultas y operaciones JPA de ' + words(name.removeprefix('I').removesuffix('Repository')) + '; Spring Data crea la implementación.'
    if name.endswith('DTO'): return 'Transporta datos de ' + words(name.removesuffix('DTO')) + ' entre consultas, servicios, exportaciones y vistas sin representar una entidad persistente.'
    if name.startswith('I') and name.endswith('Service'): return 'Define el contrato de operaciones de ' + words(name[1:-7]) + ' que implementa el servicio correspondiente.'
    if 'Test' in name: return 'Agrupa pruebas automatizadas de ' + words(name.replace('IntegrationTest', '').replace('Tests', '').replace('Test', '')) + '.'
    if name == 'IntegrationSupport': return 'Configura el soporte compartido de las pruebas contra una base de datos descartable.'
    return 'Define el tipo ' + name + ' y los datos u operaciones declarados en su cuerpo.'

def method_role(n, file):
    name = n['name']; key = file.stem + '.' + name
    if key in SPECIAL: return SPECIAL[key]
    if file.stem.startswith('I') and file.stem[1:]+'.'+name in SPECIAL:return SPECIAL[file.stem[1:]+'.'+name]
    if name == '<init>': return 'Construye ' + file.stem + (' y conserva sus dependencias o valores iniciales.' if '(' in n['code'] else '.')
    if '/test/' in file.as_posix():
        if '@Test' in n['code']: return 'Comprueba el escenario «' + words(name) + '» mediante las aserciones del cuerpo.'
        return 'Prepara o auxilia el escenario de prueba: ' + words(name) + '.'
    if name.startswith('get') and len(name)>3: return 'Devuelve ' + value_role(name[3:4].lower()+name[4:]) + ', según el cuerpo o la implementación del contrato.'
    if name.startswith('set') and len(name)>3: return 'Asigna ' + value_role(name[3:4].lower()+name[4:]) + ' con el valor recibido.'
    if name.startswith('is') and len(name)>2: return 'Indica si se cumple la condición ' + words(name[2:]) + '.'
    if name.startswith(('find', 'exists', 'count')):
        return 'Consulta persistencia según el criterio expresado por ' + name + '; la consulta declarada o la convención de Spring Data determina el resultado.'
    verbs = {'listar':'Consulta y devuelve', 'obtener':'Obtiene', 'calcular':'Calcula', 'recalcular':'Recalcula',
             'contar':'Cuenta', 'buscar':'Busca', 'guardar':'Guarda', 'save':'Guarda', 'delete':'Elimina', 'eliminar':'Elimina',
             'edit':'Actualiza', 'editar':'Actualiza', 'enviar':'Compone y encola o delega el envío de', 'notificar':'Genera avisos de',
             'export':'Genera una exportación de', 'marcar':'Registra', 'normalizar':'Normaliza', 'ventas':'Consulta importes de ventas de',
             'agregar':'Agrega', 'remove':'Quita', 'add':'Agrega', 'solicitar':'Solicita', 'confirmar':'Confirma', 'cambiar':'Cambia'}
    for prefix, action in sorted(verbs.items(), key=lambda x: -len(x[0])):
        if name.startswith(prefix): return action + ' ' + (words(name[len(prefix):]) or 'los datos de la operación') + ' en el ámbito de ' + file.stem + '.'
    routes = re.findall(r'@(Get|Post|Put|Delete|Request)Mapping\s*\(([^\n]*)', n['code'])
    if routes: return 'Atiende la ruta HTTP declarada, prepara sus datos y devuelve la vista o respuesta definida en el cuerpo.'
    if name == 'main': return 'Inicia la aplicación Spring Boot con los argumentos de la línea de comandos.'
    if name == 'clear': return 'Vacía el carrito o elimina su atributo de sesión según la clase.'
    if name == 'preHandle': return 'Evalúa la petición antes de ejecutar el controlador y devuelve si puede continuar.'
    return 'Resuelve la operación ' + words(name) + ' dentro de ' + file.stem + '; el detalle de instrucciones muestra sus pasos y llamadas.'

def compact(s, limit=140):
    s = re.sub(r'\s+', ' ', s).strip()
    return s if len(s)<=limit else s[:limit-3]+'...'

def explain(n, file):
    cat=n['cat']; code=n['code']; kind=n['kind']
    if cat=='TYPE': return class_role(n)
    if cat=='METHOD': return method_role(n,file)
    if cat=='VARIABLE':
        where = 'Parámetro' if n['owner'] in ('METHOD','LAMBDA_EXPRESSION','CATCH') else ('Atributo' if n['owner'] in ('CLASS','INTERFACE','ENUM','RECORD') else 'Variable local')
        extra = ' Su referencia no se reasigna después de inicializarse.' if re.search(r'\bfinal\b',code) else ''
        typ='var; tipo inferido por Java' if n['type']=='null' else n['type']
        init=re.search(r'\b'+re.escape(n['name'])+r'\s*=\s*([\s\S]+)',code)
        initial=' Se inicializa con '+compact(init.group(1).removesuffix(';'),110)+'.' if init else ''
        return f"{where} {n['name']} ({typ}): {value_role(n['name'], n['type'])}."+initial+extra
    if kind=='IF':
        condition = balanced_condition(code)
        return 'Evalúa '+compact(condition,180)+'. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.'
    if kind in ('FOR_LOOP','ENHANCED_FOR_LOOP'): return 'Recorre los elementos o repite según '+compact(balanced_condition(code),150)+'. Cada vuelta ejecuta el cuerpo del bucle.'
    if kind in ('WHILE_LOOP','DO_WHILE_LOOP'): return 'Repite el cuerpo mientras se cumpla la condición del bucle: '+compact(balanced_condition(code),140)+'.'
    if kind=='RETURN':
        expr=code.strip()[6:].strip().removesuffix(';')
        if not expr: return 'Termina este método sin devolver un valor.'
        if expr.startswith('"redirect:'): return 'Termina la acción y solicita al navegador una redirección a la ruta indicada.'
        if expr.startswith('"') and file.parent.name=='controller': return 'Devuelve el nombre de la plantilla que Spring MVC debe renderizar, salvo que el método declare un cuerpo de respuesta.'
        return 'Termina el método o la función y devuelve '+compact(expr,160)+'.'
    if kind=='THROW': return 'Interrumpe el flujo lanzando '+compact(code.strip()[6:].removesuffix(';'),170)+'. El llamador o el manejador de errores decide la respuesta.'
    if kind=='TRY': return 'Ejecuta un bloque con tratamiento de excepciones; los catch asociados atienden los fallos declarados y finally se ejecuta al salir si existe.'
    if kind=='SYNCHRONIZED': return 'Exige acceso exclusivo al monitor '+compact(balanced_condition(code),110)+' mientras se ejecuta el bloque, para serializar operaciones concurrentes.'
    if kind=='SWITCH': return 'Selecciona la rama correspondiente al valor de '+compact(balanced_condition(code),130)+'.'
    if kind=='BREAK': return 'Sale del bucle o selección indicado.'
    if kind=='CONTINUE': return 'Pasa a la siguiente iteración del bucle.'
    if kind=='YIELD': return 'Produce el valor de esta rama de la expresión switch.'
    if kind=='ASSERT': return 'Comprueba una condición interna cuando están habilitadas las aserciones de Java.'
    if kind=='EXPRESSION_STATEMENT':
        c=compact(code,170)
        if re.match(r'this\.\w+\s*=',code.strip()): return 'Conserva en el atributo de la instancia el valor o colaborador recibido: '+c
        if 'addAttribute(' in code: return 'Agrega un dato al modelo que recibirá la plantilla: '+c
        if 'setAttribute(' in code: return 'Almacena un atributo en la sesión o petición: '+c
        if '.setStock(' in code: return 'Actualiza el stock de la entidad en memoria; la persistencia se realiza en la transacción: '+c
        if re.search(r'\.save(?:AndFlush)?\(',code): return 'Solicita persistir la entidad; saveAndFlush también fuerza la sincronización: '+c
        if 'jdbc.update(' in code: return 'Ejecuta una modificación SQL con los parámetros indicados; participa en la transacción si existe.'
        if '.refresh(' in code: return 'Recarga la entidad desde la base usando el modo de bloqueo declarado: '+c
        if re.search(r'\bassert\w*\(',code): return 'Verifica una expectativa de la prueba; una diferencia hace fallar el caso: '+c
        if '.set' in code: return 'Actualiza una propiedad o configuración mediante la llamada: '+c
        return 'Ejecuta la llamada o modificación '+c+' Sus efectos dependen del método invocado y del objeto receptor.'
    return 'Ejecuta la instrucción Java '+kind.lower()+': '+compact(code,140)

def balanced_condition(code):
    start=code.find('(')
    if start<0:return compact(code)
    depth=0
    for i in range(start,len(code)):
        if code[i]=='(':depth+=1
        elif code[i]==')':
            depth-=1
            if depth==0:return code[start+1:i]
    return code[start+1:]

def clean_generated(text):
    # Las etiquetas identifican exclusivamente comentarios creados por esta herramienta.
    text=re.sub(r'(?ms)^[ \t]*/\*\* \[Doc\].*?\*/[\r]?\n','',text)
    return re.sub(r'(?m)^[ \t]*// \[Doc\][^\n]*[\r]?\n','',text)

def scan():
    subprocess.run([str(JDK/'javac.exe'),'-encoding','UTF-8','-d',str(WORK),str(ROOT/'docs/herramientas/DocScan.java')],check=True)
    r=subprocess.run([str(JDK/'java.exe'),'-Dstdout.encoding=UTF-8','-Dstderr.encoding=UTF-8','-cp',str(WORK),'DocScan',str(ROOT)],capture_output=True,check=True)
    if r.stderr: raise RuntimeError(r.stderr.decode('utf-8'))
    result=collections.defaultdict(list)
    for path in sorted((ROOT/'src').rglob('*.java')):result[path]=[]
    for line in r.stdout.decode('utf-8').splitlines():
        p=line.split('\t'); dec=lambda s:base64.b64decode(s).decode('utf-8')
        file=Path(dec(p[0])); result[file].append(dict(cat=p[1],kind=p[2],start=int(p[3]),end=int(p[4]),line=int(p[5]),name=dec(p[6]),type=dec(p[7]),owner=p[8],code=dec(p[9])))
    return result

def java_tokens(text):
    # Conserva literales, incluidos bloques de texto; solo omite espacio y comentarios.
    pattern=r'"""[\s\S]*?"""|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|//[^\n]*|/\*[\s\S]*?\*/|\s+|[\w$]+|[^\w\s]'
    return [m.group() for m in re.finditer(pattern,text) if not m.group().isspace() and not m.group().startswith(('//','/*'))]

def annotate(data):
    stats=collections.Counter()
    for file,nodes in data.items():
        original=file.read_text(encoding='utf-8'); lines=original.splitlines(keepends=True)
        byline=collections.defaultdict(list)
        for n in nodes: byline[n['line']].append(n)
        result=[]
        for number,line in enumerate(lines,1):
            candidates=byline.get(number,[])
            # El nodo debe comenzar al principio lógico de la línea; nunca se inserta dentro de un literal.
            candidates=[n for n in candidates if original.encode('utf-16-le')[max(0,n['start']*2-2000):n['start']*2].decode('utf-16-le',errors='ignore').split('\n')[-1].strip()=='']
            if candidates:
                n=min(candidates,key=lambda n:({'TYPE':0,'METHOD':1,'VARIABLE':2,'STATEMENT':3}[n['cat']],-n['end']))
                if not (n['cat']=='VARIABLE' and n['owner'] in ('METHOD','LAMBDA_EXPRESSION','CATCH')):
                    desc=explain(n,file).replace('*/','* /').replace('\n',' ')
                    indent=re.match(r'\s*',line).group().replace('\r','').replace('\n','').expandtabs(4)
                    if n['cat'] in ('TYPE','METHOD'):
                        desc=html.escape(desc,quote=False)
                        body=['/** [Doc]']+[' * '+x for x in textwrap.wrap(desc,width=110,break_long_words=False)]
                        if n['cat']=='METHOD':
                            params=[p for p in nodes if p['cat']=='VARIABLE' and p['owner']=='METHOD' and n['start']<p['start']<n['end']]
                            for p in params:body.append(' * @param '+p['name']+' '+html.escape(value_role(p['name'],p['type']),quote=False)+'.')
                            if n['type'] not in ('void','null'):body.append(' * @return '+html.escape(n['type'],quote=False)+' con el resultado de la operación; véase el cuerpo para las ramas de error.')
                        body.append(' */')
                        comment=('\n'+indent).join(body)
                    else: comment='// [Doc] '+desc
                    result.append(indent+comment+'\n'); stats[n['cat']]+=1
            result.append(line)
        updated=''.join(result)
        if java_tokens(original)!=java_tokens(updated):raise RuntimeError('Cambio de tokens en '+str(file))
        if updated!=original:file.write_text(updated,encoding='utf-8',newline='')
    return stats

def annotate_resources():
    roles={
      'seguridad.js':'Agrega el token CSRF a formularios y peticiones del navegador.',
      'seguimiento-pedido.js':'Construye la línea de tiempo del pedido con nodos DOM y fechas de Buenos Aires.',
      'historial-notificaciones.js':'Procesa las acciones de lectura del historial y actualiza su presentación.',
      'seguridad.test.cjs':'Comprueba el agregado del token CSRF desde el navegador simulado.',
      'estado-pedido.test.cjs':'Comprueba la presentación de estados y acciones de pedidos.',
    }
    for folder in ['src/main/resources/static/js','src/test/js']:
        for p in (ROOT/folder).glob('*'):
            if p.suffix not in ('.js','.cjs'):continue
            s=clean_generated(p.read_text(encoding='utf-8'))
            p.write_text('// [Doc] '+roles.get(p.name,'Lógica de interfaz y sus pruebas.')+'\n// [Doc] Referencia de instrucciones: docs/documentacion/REFERENCIA_COMPLETA.html.\n'+s,encoding='utf-8',newline='')
    for p in (ROOT/'src/main/resources/templates').rglob('*.html'):
        s=re.sub(r'(?m)^<!-- \[Doc\].*?-->\r?\n','',p.read_text(encoding='utf-8'))
        desc='<!-- [Doc] Vista '+p.stem+': Thymeleaf evalúa th:* en el servidor; HTML estructura la pantalla, CSS la presenta y los scripts gestionan sus acciones. Véase docs/documentacion/REFERENCIA_COMPLETA.html. -->\n'
        m=re.match(r'(?i)\s*<!doctype[^>]*>\s*\n',s)
        at=m.end() if m else 0
        p.write_text(s[:at]+desc+s[at:],encoding='utf-8',newline='')
    for p in (ROOT/'src/main/resources/static/css').glob('*.css'):
        s=re.sub(r'(?m)^/\* \[Doc\].*?\*/\r?\n','',p.read_text(encoding='utf-8'))
        p.write_text('/* [Doc] Estilos propios de '+p.stem+': los selectores determinan qué elementos reciben cada regla. Referencia: docs/documentacion/REFERENCIA_COMPLETA.html. */\n'+s,encoding='utf-8',newline='')

def resource_explanation(line, suffix):
    s=line.strip()
    if not s:return 'Separación visual sin efecto en ejecución.'
    if s.startswith(('//','/*','*','<!--','#')):return 'Comentario o documentación: explica el archivo y no ejecuta una operación.'
    if suffix=='.html':
        if 'th:each' in s:return 'Thymeleaf repite el elemento para cada objeto de la colección indicada.'
        if 'th:if' in s or 'th:unless' in s:return 'Thymeleaf decide si incluye este elemento según la condición del servidor.'
        if 'th:field' in s:return 'Vincula el control al atributo del objeto de formulario que procesa Spring MVC.'
        if 'th:text' in s:return 'Muestra el valor evaluado por Thymeleaf como texto escapado.'
        if '_csrf' in s or 'csrfToken' in s:return 'Expone o transmite el token CSRF que el servidor valida en operaciones de modificación.'
        if '<script' in s:return 'Carga un script o inicia un bloque de lógica del navegador.'
        if '<form' in s:return 'Define el formulario y su destino o método de envío.'
        if 'th:' in s:return 'Evalúa atributos Thymeleaf en el servidor antes de entregar HTML al navegador.'
        if s.startswith('<'):return 'Abre, cierra o configura un elemento de la estructura HTML de la pantalla.'
    if suffix in ('.js','.cjs','.html'):
        if re.search(r'\b(const|let|var)\b',s):return 'Declara valores o referencias locales del script; la expresión de la derecha determina su valor inicial.'
        if 'fetch(' in s:return 'Inicia una petición HTTP; el código asíncrono procesa posteriormente la respuesta.'
        if 'addEventListener' in s:return 'Registra una función que se ejecutará cuando ocurra el evento indicado.'
        if 'textContent' in s:return 'Asigna o consulta texto en un nodo del DOM sin interpretarlo como HTML.'
        if 'createElement' in s:return 'Crea un elemento DOM que podrá incorporarse a la pantalla.'
        if re.match(r'(if|else|for|while|switch)\b',s):return 'Controla una condición o repetición de la lógica del navegador.'
        if 'return' in s:return 'Devuelve un resultado o termina el callback o función actual.'
        if 'function' in s or '=>' in s:return 'Declara una función o callback; su cuerpo se ejecuta al invocarlo.'
        if s in ('}', '};', '});', ');', ']', '];'):return 'Cierra el bloque, objeto, colección o llamada abierto anteriormente.'
        if suffix in ('.js','.cjs'):return 'Continúa la expresión, llamada o estructura JavaScript iniciada en este bloque.'
    if suffix=='.css':
        if ':' in s:return 'Define una propiedad CSS; el selector del bloque determina los elementos afectados.'
        return 'Define o cierra un selector o regla de presentación CSS.'
    if suffix=='.properties':return 'Configura '+s.split('=',1)[0]+'; ${...} obtiene una variable y el texto posterior a dos puntos indica su valor por defecto si existe.'
    if suffix=='.sql':return 'Declara o continúa una operación de esquema SQL; revisar el bloque completo para identificar tabla, columnas y restricciones.'
    if suffix in ('.xml','.yaml'):return 'Declara configuración de compilación, dependencias o servicios de desarrollo según la estructura del archivo.'
    return 'Contenido del recurso que se utiliza según su formato y su ubicación en el proyecto.'

def build_reference(data):
    inventory=[]; sections=[]
    for file,nodes in sorted(data.items()):
        rel=file.relative_to(ROOT).as_posix(); records=[]
        for n in nodes:
            record={k:n[k] for k in ('cat','kind','line','name','type','owner')}
            record['explanation']=explain(n,file)
            records.append(record)
        inventory.append({'path':rel,'nodes':records})
        chunks=['<h2>'+html.escape(rel)+'</h2>']
        full=file.read_text(encoding='utf-8').splitlines()
        chunks.append('<details><summary>Código completo, con numeración de líneas</summary><pre>'+html.escape('\n'.join(f'{i:5} | {line}' for i,line in enumerate(full,1)))+'</pre></details>')
        groups=collections.defaultdict(list)
        for n,r in zip(nodes,records):groups[n['line']].append((n,r))
        source=file.read_text(encoding='utf-8').splitlines()
        for ln,items in sorted(groups.items()):
            chunks.append(f'<article id="{html.escape(rel)}-L{ln}"><h3>Línea {ln}</h3><pre>'+html.escape('\n'.join(source[ln-1:min(len(source),ln+2)]))+'</pre>')
            for n,r in items:chunks.append('<p><b>'+html.escape(n['cat']+' '+n['name'])+'</b> - '+html.escape(r['explanation'])+'</p>')
            chunks.append('</article>')
        sections.append('<details><summary>'+html.escape(rel)+'</summary>'+''.join(chunks)+'</details>')
    resources=[]
    resources.extend((ROOT/'src/main/resources/templates').rglob('*.html'))
    resources.extend((ROOT/'src/main/resources/static/js').glob('*.js'))
    resources.extend((ROOT/'src/main/resources/static/css').glob('*.css'))
    resources.extend((ROOT/'src/test/js').glob('*.cjs'))
    resources.extend((ROOT/'src/main/resources/db').glob('*.sql'))
    resources.extend([ROOT/'src/main/resources/application.properties',ROOT/'application-example.properties',ROOT/'pom.xml',ROOT/'compose.yaml'])
    for p in sorted(resources):
        rel=p.relative_to(ROOT).as_posix()
        lines=p.read_text(encoding='utf-8').splitlines()
        content=['<h2>'+html.escape(rel)+'</h2><p>Leer las líneas como partes del bloque completo: una expresión puede ocupar varias líneas.</p>']
        for i,line in enumerate(lines,1):
            if not line.strip():continue
            content.append(f'<article><h3>Línea {i}</h3><pre>'+html.escape(line)+'</pre><p>'+html.escape(resource_explanation(line,p.suffix))+'</p></article>')
        sections.append('<details><summary>'+html.escape(rel)+'</summary>'+''.join(content)+'</details>')
    header='''<!doctype html><html lang="es"><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Pequelandia | Referencia completa</title><style>body{font:16px/1.6 system-ui;max-width:1100px;margin:40px auto;padding:0 24px;color:#183246;background:#f6f8fa}h1,h2,h3{line-height:1.25}h2{overflow-wrap:anywhere}header{background:white;padding:28px;border-radius:12px}details{background:white;border:1px solid #d7e0e5;margin:12px 0;padding:16px;border-radius:8px}summary{cursor:pointer;font-weight:bold;overflow-wrap:anywhere}article{border-top:1px solid #e0e5e9;margin-top:20px;padding-top:8px}pre{white-space:pre-wrap;overflow-wrap:anywhere;background:#edf2f6;padding:12px;font-size:13px}input{width:95%;padding:12px;font:inherit}button{padding:9px;margin:8px}small{color:#567} @media print{input,button{display:none}details{break-inside:auto}}</style><header><h1>Pequelandia</h1><h2>Referencia completa del código</h2><p>Clases, métodos, parámetros, atributos, variables locales e instrucciones Java; recursos propios explicados por línea. Las líneas corresponden a la versión documentada. Los comentarios [Doc] se generaron desde el árbol sintáctico del JDK; las reglas de negocio principales cuentan con descripciones específicas.</p><p>Un comentario describe el código existente; no sustituye sus validaciones ni constituye una prueba de ejecución. Bootstrap, imágenes, archivos generados, secretos y configuración privada quedan fuera del listado de instrucciones propio.</p><label for="filter">Buscar archivo, clase o método</label><input id="filter" placeholder="Ejemplo: transicionar, stock, registro"><p id="count"></p><button onclick="document.querySelectorAll('details:not([hidden])').forEach(d=>d.open=true)">Expandir resultados</button><button onclick="document.querySelectorAll('details').forEach(d=>d.open=false)">Contraer</button></header>'''
    tail='''<script>const field=document.getElementById('filter'),ds=[...document.querySelectorAll('details')];function filtrar(){const q=field.value.trim().toLocaleLowerCase('es');let n=0;for(const d of ds){d.hidden=!!q&&!d.textContent.toLocaleLowerCase('es').includes(q);if(!d.hidden)n++;}document.getElementById('count').textContent=n+' archivos visibles de '+ds.length;}field.addEventListener('input',filtrar);filtrar();</script></html>'''
    document=header+''.join(sections)+tail
    document=document.replace("querySelectorAll('details:not([hidden])')", "querySelectorAll('details.archivo:not([hidden])')")
    document=document.replace("querySelectorAll('details')", "querySelectorAll('details.archivo')")
    # Solo los detalles exteriores representan archivos; el código completo tiene su propio desplegable.
    document=document.replace('<details><summary>src/', '<details class="archivo"><summary>src/')
    for name in ('pom.xml','compose.yaml','application-example.properties'):
        document=document.replace('<details><summary>'+name, '<details class="archivo"><summary>'+name)
    (OUT/'REFERENCIA_COMPLETA.html').write_text(document,encoding='utf-8')
    (OUT/'inventario.json').write_text(json.dumps(inventory,ensure_ascii=False,indent=2),encoding='utf-8')
    return inventory, resources

def main():
    # Quitar solo nuestros comentarios permite regenerar sin duplicarlos.
    for p in (ROOT/'src').rglob('*.java'):
        s=p.read_text(encoding='utf-8'); clean=clean_generated(s)
        # Normalizar CRLF antes del escaneo alinea offsets UTF-16 con el texto leído por Python.
        p.write_text(clean,encoding='utf-8',newline='')
    before=scan(); fingerprint={p:java_tokens(p.read_text(encoding='utf-8')) for p in before}
    stats=annotate(before); annotate_resources(); after=scan()
    for p in before:
        if fingerprint[p]!=java_tokens(p.read_text(encoding='utf-8')):raise RuntimeError('Tokens alterados: '+str(p))
        shape=lambda ns:[(n['cat'],n['kind'],n['name'],n['type'],n['owner']) for n in ns]
        if shape(before[p])!=shape(after[p]):raise RuntimeError('AST alterado: '+str(p))
    inventory,resources=build_reference(after)
    report={'java_files':len(after),'resource_files':len(resources),'comments_added':dict(stats),
            'nodes':dict(collections.Counter(n['cat'] for ns in after.values() for n in ns)),
            'java_tokens_unchanged':True,'java_ast_unchanged':True}
    (OUT/'cobertura.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
    print(json.dumps(report))

if __name__=='__main__':main()

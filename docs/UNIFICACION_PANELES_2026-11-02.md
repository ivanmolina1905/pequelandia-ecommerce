# Unificación visual de paneles — 02/11/2026

Los paneles administrativo y de usuario, el historial de notificaciones y los correos comparten `css/paneles.css`, limitado a las páginas con `panel-theme`.

Paleta final: fondo y cabecera crema #fff8e1 (muestreado de la captura del inicio), superficies blancas #ffffff y superficies secundarias #fff3db, texto #3e3028 y acciones naranja pastel #ffcc80 con texto oscuro #633413. Los enlaces y las cifras destacadas usan #ad4b16. Se tomó como referencia el naranja #ffcc80 y los fondos crema existentes en style.css. Tarjetas, tablas, formularios, modales, navegación, paginación y gráficos usan el mismo criterio. Los estados de pedidos y las acciones destructivas mantienen colores semánticos. Se corrigió el desbordamiento de las estadísticas del usuario a 390 px.

Verificación en ejecución: compilación Maven correcta y navegación automatizada con Chrome a 1440 px y 390 px en una instancia aislada (puerto 8093), con cuentas ficticias y base `pequelandia_paneles_20261102`. Se revisaron usuarios, estadísticas, pedidos, avisos y correos del administrador; perfil, modal de edición, historial y estadísticas del cliente. Las capturas finales confirman las mismas bases de color en ambos paneles. No hubo desbordamiento horizontal en las vistas móviles finales (correos administrativos y estadísticas del cliente). No se verificaron envíos de correo ni se modificaron datos de la aplicación habitual.

Evidencia local ignorada: `.tools/paneles-*.png`, `.tools/paneles-visual-resultados.json`, `.tools/paneles-build.log`. No se hizo commit ni push; hay cambios anteriores en el árbol de trabajo. Mensaje sugerido para un commit aislado: `feat: unificar la apariencia de los paneles`.

Refinamiento del perfil: cabecera crema y durazno, avatar con marco blanco, jerarquía tipográfica más liviana, sección de información personal e iconos SVG de línea. Se verificó en Chrome el perfil a 1440 y 390 px y la apertura/cierre del modal de edición, sin desbordamiento horizontal. Se redujo además el margen superior de la distribución móvil. La evidencia usa exclusivamente cuentas ficticias.

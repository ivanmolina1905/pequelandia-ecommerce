# Historial de notificaciones dentro del panel de usuario

Se corrigió la ruta `/notificaciones/historial` para que los clientes vean sus avisos dentro de `panelusuario`, conservando el menú lateral, los datos de su cuenta y el estilo pastel. El contenido reutiliza el fragmento del historial. Los enlaces del menú de escritorio y del menú móvil activan la sección; al volver al perfil, compras o estadísticas, la URL corresponde otra vez al panel personal para permitir recargar correctamente.

Verificación: pruebas de controlador e integración contra `pequelandia_verificacion`. En Chrome con cuentas y avisos ficticios de `pequelandia_paneles_20261102` se comprobó el menú activo, la paginación, el filtro de no leídas, marcar todas como leídas, volver al perfil y recargar, y abrir el historial desde el menú móvil. Sin desbordamiento horizontal a 390 px. Capturas locales ignoradas: `.tools/avisos-panel-desktop.png` y `.tools/avisos-panel-mobile.png`.

No se modificaron datos de la aplicación habitual ni se hizo push. Mensaje sugerido: `fix: integrar notificaciones en el panel de usuario`.

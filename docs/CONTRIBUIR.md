# Flujo de trabajo

1. Crear una rama: `git switch -c feat/nombre-del-cambio`.
2. Implementar y comprobar con datos ficticios.
3. Revisar secretos, datos personales y `git diff`.
4. Preparar un commit con prefijo `feat:`, `fix:` o `docs:`.
5. Avisar al propietario antes de publicar. No ejecutar push automáticamente en futuras sesiones.
6. Integrar cambios verificados en `main`, preferentemente mediante pull request.

Los cambios locales de Eclipse y las configuraciones privadas se mantienen fuera del repositorio. No utilizar `git add -f` para saltar las exclusiones de seguridad.

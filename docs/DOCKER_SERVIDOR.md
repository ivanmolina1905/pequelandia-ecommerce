# Docker en Ubuntu / VirtualBox (demostración)

Ejecutar desde la carpeta que contiene pom.xml. Esta configuración es independiente
de compose.yaml (desarrollo). Crea una base nueva y vacía; no copia la base local.
Se necesitan localidades, categorías y productos ficticios para probar los flujos.
No usar datos reales: las contraseñas de cuentas aún se almacenan sin hash.

El Dockerfile compila Java 22 con JDK 25 y ejecuta con JRE 25.
La compilación omite los tests; no equivale a una verificación de funcionalidades.
Solo entran al contexto Maven y src; config, .env y bases quedan excluidos.

## Preparación

Copiar el proyecto a ~/pequelandia en Ubuntu (sin config, bases, backups ni .env
de Windows). En Ubuntu:

```bash
cd ~/pequelandia
umask 077
printf 'PEQUELANDIA_DB_PASSWORD=%s\n' "$(openssl rand -hex 24)" > .env
printf 'PEQUELANDIA_PUBLIC_URL=http://localhost:8080\n' >> .env
sudo docker compose -f compose.servidor.yaml config --quiet
```

La URL localhost es temporal para pruebas: determina los enlaces de recuperación,
no la interfaz de escucha. Esos enlaces no funcionarán desde otros equipos.
Para publicar, sustituirla por una URL HTTPS con TLS configurado.
Si Nginx ocupa 8080, detenerlo antes (se conserva su contenedor y sus archivos):

```bash
sudo docker stop servidor-web
sudo docker compose -f compose.servidor.yaml up -d --build
sudo docker compose -f compose.servidor.yaml ps
sudo docker compose -f compose.servidor.yaml logs --tail=100 web
```

Si Nginx tiene otro nombre, consultar primero `sudo docker ps`.
Abrir http://192.168.0.100:8080 desde la red local y verificar los recorridos.
PostgreSQL no publica ningún puerto a la red. El volumen datos_demo persiste
al detener los contenedores. No usar down -v: elimina esa base.
No cambiar la contraseña de .env después de inicializar la base sin actualizar
también la contraseña del usuario dentro de PostgreSQL.

## Estado de verificación

Archivos preparados mediante revisión estática. Construcción y arranque pendientes
en Ubuntu: Docker no está disponible en el entorno Windows de preparación.
Antes de publicar con usuarios reales: resolver contraseñas sin hash, configurar
HTTPS y revisar la seguridad de la aplicación. La configuración presente sirve
para demostraciones con datos ficticios; no certifica un despliegue de producción.

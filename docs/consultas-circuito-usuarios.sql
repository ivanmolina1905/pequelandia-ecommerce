-- Solo lectura. Ejecutar en la base de desarrollo desde pgAdmin.
-- Reemplazar circuito@example.invalid por el email de tu cuenta ficticia.
-- No incluye contraseñas, tokens ni cuerpos de correos.

SELECT current_database() AS base_actual, current_user AS usuario_conexion;

SELECT u.id_usuario, u.nombre_usuario, u.apellido, u.email, u.dni,
       u.telefono, u.direccion, u.fecha_nacimiento, u.fecha_registro,
       u.ultima_conexion, u.puntos, u.id_localidad, l.nombre_localidad
FROM usuario u LEFT JOIN localidad l ON l.id_localidad = u.id_localidad
WHERE lower(u.email) = lower('circuito@example.invalid');

SELECT id_notificacion, destinatario, descripcion, fecha_notificacion
FROM notificacion
WHERE destinatario = 'ADMIN' AND descripcion LIKE 'Nuevo usuario registrado:%'
ORDER BY id_notificacion DESC LIMIT 20;

SELECT id, destinatario, asunto, intentos, proximo_intento, enviado_en,
       CASE WHEN enviado_en IS NOT NULL THEN 'Enviado'
            WHEN intentos > 0 THEN 'Reintento pendiente' ELSE 'Pendiente' END AS estado
FROM correo_pendiente
WHERE lower(destinatario) = lower('circuito@example.invalid')
ORDER BY id;

SELECT r.usuario_id, r.email, r.vence,
       r.vence > clock_timestamp() AS vigente
FROM recuperacion_clave r JOIN usuario u ON u.id_usuario = r.usuario_id
WHERE lower(u.email) = lower('circuito@example.invalid');

-- Auditoría de duplicados: devuelve valores agregados sin emails ni DNI.
SELECT 'email' AS campo, count(*) AS grupos_duplicados
FROM (SELECT lower(trim(email)) FROM usuario GROUP BY lower(trim(email)) HAVING count(*) > 1) d
UNION ALL
SELECT 'nombre_usuario', count(*)
FROM (SELECT lower(trim(nombre_usuario)) FROM usuario GROUP BY lower(trim(nombre_usuario)) HAVING count(*) > 1) d
UNION ALL
SELECT 'dni', count(*)
FROM (SELECT dni FROM usuario GROUP BY dni HAVING count(*) > 1) d;

-- Estructura y restricciones reales de la base que estés inspeccionando.
SELECT table_name, column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_schema='public'
  AND table_name IN ('usuario','localidad','notificacion','correo_pendiente','recuperacion_clave','recuperacion_limite')
ORDER BY table_name, ordinal_position;

SELECT c.conrelid::regclass AS tabla, c.conname AS restriccion, pg_get_constraintdef(c.oid) AS definicion
FROM pg_constraint c
WHERE c.conrelid IN ('usuario'::regclass, 'localidad'::regclass, 'recuperacion_clave'::regclass)
ORDER BY tabla, restriccion;
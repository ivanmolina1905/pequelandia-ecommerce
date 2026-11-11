-- Ejecutar antes de iniciar esta versión (ddl-auto=validate).
-- NULL conserva la compatibilidad del administrador existente por ID.
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS rol varchar(20);

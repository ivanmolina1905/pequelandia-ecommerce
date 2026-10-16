-- Migración aditiva e idempotente. No modifica Notificacion ni los datos existentes.
CREATE TABLE IF NOT EXISTS correo_pendiente (
    id BIGSERIAL PRIMARY KEY,
    destinatario VARCHAR(320) NOT NULL,
    asunto TEXT NOT NULL,
    contenido TEXT NOT NULL,
    intentos INTEGER NOT NULL DEFAULT 0,
    proximo_intento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    enviado_en TIMESTAMP NULL
);
CREATE INDEX IF NOT EXISTS ix_correo_pendiente_entrega
    ON correo_pendiente(proximo_intento, id) WHERE enviado_en IS NULL;

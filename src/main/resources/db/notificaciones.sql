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

CREATE TABLE IF NOT EXISTS pedido_historial (
    id BIGSERIAL PRIMARY KEY,
    id_pedido BIGINT NOT NULL,
    estado VARCHAR(40) NOT NULL,
    fecha TIMESTAMP NULL
);
CREATE INDEX IF NOT EXISTS ix_pedido_historial ON pedido_historial(id_pedido, id);
CREATE TABLE IF NOT EXISTS producto_alerta_stock (
    id_producto BIGINT PRIMARY KEY,
    activa BOOLEAN NOT NULL
);

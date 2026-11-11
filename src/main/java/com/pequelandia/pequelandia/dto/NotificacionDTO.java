package com.pequelandia.pequelandia.dto;

import java.time.LocalDateTime;

/** [Doc]
 * Transporta datos de notificacion entre consultas, servicios, exportaciones y vistas sin representar una
 * entidad persistente.
 */
public record NotificacionDTO(
        // [Doc] Atributo id (Long): identificador del registro.
        Long id,
        // [Doc] Atributo descripcion (String): texto descriptivo de la entidad u operación.
        String descripcion,
        // [Doc] Atributo fecha (LocalDateTime): fecha o etiqueta temporal de la operación.
        LocalDateTime fecha,
        // [Doc] Atributo pedidoId (Long): identificador del pedido.
        Long pedidoId,
        // [Doc] Atributo leida (boolean): leida.
        boolean leida
) {
}
package com.pequelandia.pequelandia.dto;

import java.time.LocalDateTime;

public record NotificacionDTO(
        Long id,
        String descripcion,
        LocalDateTime fecha,
        Long pedidoId,
        boolean leida
) {
}
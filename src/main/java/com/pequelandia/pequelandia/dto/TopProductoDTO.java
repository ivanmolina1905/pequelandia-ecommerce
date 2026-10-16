package com.pequelandia.pequelandia.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopProductoDTO {
    private Long idProducto;
    private String nombreProducto;
    private long cantidadVendida;
    private BigDecimal totalFacturado;
}

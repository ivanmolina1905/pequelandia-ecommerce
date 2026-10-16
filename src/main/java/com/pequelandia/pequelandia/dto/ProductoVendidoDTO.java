package com.pequelandia.pequelandia.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoVendidoDTO {
    private Long idProducto;
    private String nombreProducto;
    private Long cantidadVendida;
}

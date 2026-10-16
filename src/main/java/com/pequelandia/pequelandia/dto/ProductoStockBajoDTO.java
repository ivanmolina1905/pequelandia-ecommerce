package com.pequelandia.pequelandia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductoStockBajoDTO {
    private Long idProducto;
    private String nombreProducto;
    private int stock;
}

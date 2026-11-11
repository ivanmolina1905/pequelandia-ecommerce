package com.pequelandia.pequelandia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** [Doc]
 * Transporta datos de producto stock bajo entre consultas, servicios, exportaciones y vistas sin representar una
 * entidad persistente.
 */
@Data
@AllArgsConstructor
public class ProductoStockBajoDTO {
    // [Doc] Atributo idProducto (Long): identificador del producto.
    private Long idProducto;
    // [Doc] Atributo nombreProducto (String): nombre producto.
    private String nombreProducto;
    // [Doc] Atributo stock (int): unidades disponibles del producto.
    private int stock;
}

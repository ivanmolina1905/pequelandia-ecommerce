package com.pequelandia.pequelandia.dto;

import lombok.*;

/** [Doc]
 * Transporta datos de producto vendido entre consultas, servicios, exportaciones y vistas sin representar una
 * entidad persistente.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoVendidoDTO {
    // [Doc] Atributo idProducto (Long): identificador del producto.
    private Long idProducto;
    // [Doc] Atributo nombreProducto (String): nombre producto.
    private String nombreProducto;
    // [Doc] Atributo cantidadVendida (Long): cantidad vendida.
    private Long cantidadVendida;
}

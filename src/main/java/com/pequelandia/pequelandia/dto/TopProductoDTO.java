package com.pequelandia.pequelandia.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

/** [Doc]
 * Transporta datos de top producto entre consultas, servicios, exportaciones y vistas sin representar una
 * entidad persistente.
 */
@Data
@AllArgsConstructor
public class TopProductoDTO {
    // [Doc] Atributo idProducto (Long): identificador del producto.
    private Long idProducto;
    // [Doc] Atributo nombreProducto (String): nombre producto.
    private String nombreProducto;
    // [Doc] Atributo cantidadVendida (long): cantidad vendida.
    private long cantidadVendida;
    // [Doc] Atributo totalFacturado (BigDecimal): total facturado.
    private BigDecimal totalFacturado;
}

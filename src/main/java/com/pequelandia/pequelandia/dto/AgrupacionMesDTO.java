package com.pequelandia.pequelandia.dto;

import lombok.*;

/** [Doc]
 * Transporta datos de agrupacion mes entre consultas, servicios, exportaciones y vistas sin representar una
 * entidad persistente.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgrupacionMesDTO {
    // [Doc] Atributo mes (Integer): mes de la consulta.
    private Integer mes;
    // [Doc] Atributo total (Double): importe acumulado.
    private Double total;
}

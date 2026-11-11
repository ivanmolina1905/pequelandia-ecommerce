package com.pequelandia.pequelandia.dto;

import lombok.*;

/** [Doc]
 * Transporta datos de agrupacion anio entre consultas, servicios, exportaciones y vistas sin representar una
 * entidad persistente.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgrupacionAnioDTO {
    // [Doc] Atributo anio (Integer): año de la consulta.
    private Integer anio;
    // [Doc] Atributo total (Double): importe acumulado.
    private Double total;
}

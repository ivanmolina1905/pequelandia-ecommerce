package com.pequelandia.pequelandia.dto;

import java.time.LocalDate;
import lombok.*;

/** [Doc]
 * Transporta datos de agrupacion fecha entre consultas, servicios, exportaciones y vistas sin representar una
 * entidad persistente.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgrupacionFechaDTO {
    // [Doc] Atributo fecha (LocalDate): fecha o etiqueta temporal de la operación.
    private LocalDate fecha;
    // [Doc] Atributo total (Double): importe acumulado.
    private Double total;
}

package com.pequelandia.pequelandia.dto;

import java.time.LocalDate;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgrupacionFechaDTO {
    private LocalDate fecha;
    private Double total;
}

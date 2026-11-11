package com.pequelandia.pequelandia.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** [Doc]
 * Transporta datos de excel file entre consultas, servicios, exportaciones y vistas sin representar una entidad
 * persistente.
 */
@Getter @Setter @AllArgsConstructor
public class ExcelFileDTO {
    // [Doc] Atributo fileName (String): file name.
    private String fileName;
    // [Doc] Atributo fileBytes (byte[]): file bytes.
    private byte[] fileBytes;
}
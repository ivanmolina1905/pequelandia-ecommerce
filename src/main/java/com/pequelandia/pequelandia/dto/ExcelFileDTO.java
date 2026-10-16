package com.pequelandia.pequelandia.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class ExcelFileDTO {
    private String fileName;
    private byte[] fileBytes;
}
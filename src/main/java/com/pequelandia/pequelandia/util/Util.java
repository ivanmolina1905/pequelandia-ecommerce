package com.pequelandia.pequelandia.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Util {

    public static ByteArrayInputStream generarExcel(
            String nombreHoja,
            List<String> columnas,
            List<List<Object>> datos) {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(nombreHoja);

            // === ESTILO HEADER ===
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // === ESTILO FECHA ===
            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat()
                    .getFormat("dd/MM/yyyy"));

            // === CREAR ENCABEZADO ===
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnas.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas.get(i));
                cell.setCellStyle(headerStyle);
            }

            // === CARGAR DATOS ===
            int rowIdx = 1;

            for (List<Object> fila : datos) {
                Row row = sheet.createRow(rowIdx++);
                int colIdx = 0;

                for (Object valor : fila) {
                    Cell cell = row.createCell(colIdx++);

                    if (valor == null) {
                        cell.setCellValue("");
                    }
                    else if (valor instanceof Number num) {
                        cell.setCellValue(num.doubleValue());
                    }
                    else if (valor instanceof Boolean bool) {
                        cell.setCellValue(bool ? "Sí" : "No");
                    }
                    else if (valor instanceof LocalDate ld) {
                        cell.setCellValue(Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                        cell.setCellStyle(dateStyle);
                    }
                    else if (valor instanceof LocalDateTime ldt) {
                        cell.setCellValue(Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
                        cell.setCellStyle(dateStyle);
                    }
                    else if (valor instanceof Date fecha) {
                        cell.setCellValue(fecha);
                        cell.setCellStyle(dateStyle);
                    }
                    else {
                        cell.setCellValue(valor.toString());
                    }
                }
            }

            // === AUTO-SIZE DE COLUMNAS ===
            for (int i = 0; i < columnas.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Error generando Excel", e);
        }
    }
}

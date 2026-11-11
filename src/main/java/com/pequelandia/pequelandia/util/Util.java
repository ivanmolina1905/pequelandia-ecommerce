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

/** [Doc]
 * Genera libros Excel a partir de encabezados y filas, conservando números y aplicando formato a fechas.
 */
public class Util {

    /** [Doc]
     * Crea un libro XLSX con encabezados y filas; conserva números, convierte booleanos a Sí o No, formatea fechas y
     * ajusta el ancho de columnas.
     * @param nombreHoja nombre hoja.
     * @param columnas columnas.
     * @param datos datos.
     * @return ByteArrayInputStream con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public static ByteArrayInputStream generarExcel(
            String nombreHoja,
            List<String> columnas,
            List<List<Object>> datos) {

        // [Doc] Ejecuta un bloque con tratamiento de excepciones; los catch asociados atienden los fallos declarados y finally se ejecuta al salir si existe.
        try (Workbook workbook = new XSSFWorkbook();
             // [Doc] Variable local out (ByteArrayOutputStream): out. Se inicializa con new ByteArrayOutputStream().
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // [Doc] Variable local sheet (Sheet): sheet. Se inicializa con workbook.createSheet(nombreHoja).
            Sheet sheet = workbook.createSheet(nombreHoja);

            // === ESTILO HEADER ===
            // [Doc] Variable local headerStyle (CellStyle): header style. Se inicializa con workbook.createCellStyle().
            CellStyle headerStyle = workbook.createCellStyle();
            // [Doc] Variable local headerFont (Font): header font. Se inicializa con workbook.createFont().
            Font headerFont = workbook.createFont();
            // [Doc] Actualiza una propiedad o configuración mediante la llamada: headerFont.setBold(true);
            headerFont.setBold(true);
            // [Doc] Actualiza una propiedad o configuración mediante la llamada: headerFont.setFontHeightInPoints((short) 11);
            headerFont.setFontHeightInPoints((short) 11);
            // [Doc] Actualiza una propiedad o configuración mediante la llamada: headerStyle.setFont(headerFont);
            headerStyle.setFont(headerFont);
            // [Doc] Actualiza una propiedad o configuración mediante la llamada: headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // === ESTILO FECHA ===
            // [Doc] Variable local dateStyle (CellStyle): date style. Se inicializa con workbook.createCellStyle().
            CellStyle dateStyle = workbook.createCellStyle();
            // [Doc] Variable local createHelper (CreationHelper): create helper. Se inicializa con workbook.getCreationHelper().
            CreationHelper createHelper = workbook.getCreationHelper();
            // [Doc] Actualiza una propiedad o configuración mediante la llamada: dateStyle.setDataFormat(createHelper.createDataFormat() .getFormat("dd/MM/yyyy"));
            dateStyle.setDataFormat(createHelper.createDataFormat()
                    .getFormat("dd/MM/yyyy"));

            // === CREAR ENCABEZADO ===
            // [Doc] Variable local headerRow (Row): header row. Se inicializa con sheet.createRow(0).
            Row headerRow = sheet.createRow(0);
            // [Doc] Recorre los elementos o repite según int i = 0; i < columnas.size(); i++. Cada vuelta ejecuta el cuerpo del bucle.
            for (int i = 0; i < columnas.size(); i++) {
                // [Doc] Variable local cell (Cell): cell. Se inicializa con headerRow.createCell(i).
                Cell cell = headerRow.createCell(i);
                // [Doc] Actualiza una propiedad o configuración mediante la llamada: cell.setCellValue(columnas.get(i));
                cell.setCellValue(columnas.get(i));
                // [Doc] Actualiza una propiedad o configuración mediante la llamada: cell.setCellStyle(headerStyle);
                cell.setCellStyle(headerStyle);
            }

            // === CARGAR DATOS ===
            // [Doc] Variable local rowIdx (int): row idx. Se inicializa con 1.
            int rowIdx = 1;

            // [Doc] Recorre los elementos o repite según List<Object> fila : datos. Cada vuelta ejecuta el cuerpo del bucle.
            for (List<Object> fila : datos) {
                // [Doc] Variable local row (Row): row. Se inicializa con sheet.createRow(rowIdx++).
                Row row = sheet.createRow(rowIdx++);
                // [Doc] Variable local colIdx (int): col idx. Se inicializa con 0.
                int colIdx = 0;

                // [Doc] Recorre los elementos o repite según Object valor : fila. Cada vuelta ejecuta el cuerpo del bucle.
                for (Object valor : fila) {
                    // [Doc] Variable local cell (Cell): cell. Se inicializa con row.createCell(colIdx++).
                    Cell cell = row.createCell(colIdx++);

                    // [Doc] Evalúa valor == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
                    if (valor == null) {
                        // [Doc] Actualiza una propiedad o configuración mediante la llamada: cell.setCellValue("");
                        cell.setCellValue("");
                    }
                    else if (valor instanceof Number num) {
                        // [Doc] Actualiza una propiedad o configuración mediante la llamada: cell.setCellValue(num.doubleValue());
                        cell.setCellValue(num.doubleValue());
                    }
                    else if (valor instanceof Boolean bool) {
                        // [Doc] Actualiza una propiedad o configuración mediante la llamada: cell.setCellValue(bool ? "Sí" : "No");
                        cell.setCellValue(bool ? "Sí" : "No");
                    }
                    else if (valor instanceof LocalDate ld) {
                        // [Doc] Actualiza una propiedad o configuración mediante la llamada: cell.setCellValue(Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                        cell.setCellValue(Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                        // [Doc] Actualiza una propiedad o configuración mediante la llamada: cell.setCellStyle(dateStyle);
                        cell.setCellStyle(dateStyle);
                    }
                    else if (valor instanceof LocalDateTime ldt) {
                        // [Doc] Actualiza una propiedad o configuración mediante la llamada: cell.setCellValue(Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
                        cell.setCellValue(Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
                        // [Doc] Actualiza una propiedad o configuración mediante la llamada: cell.setCellStyle(dateStyle);
                        cell.setCellStyle(dateStyle);
                    }
                    else if (valor instanceof Date fecha) {
                        // [Doc] Actualiza una propiedad o configuración mediante la llamada: cell.setCellValue(fecha);
                        cell.setCellValue(fecha);
                        // [Doc] Actualiza una propiedad o configuración mediante la llamada: cell.setCellStyle(dateStyle);
                        cell.setCellStyle(dateStyle);
                    }
                    else {
                        // [Doc] Actualiza una propiedad o configuración mediante la llamada: cell.setCellValue(valor.toString());
                        cell.setCellValue(valor.toString());
                    }
                }
            }

            // === AUTO-SIZE DE COLUMNAS ===
            // [Doc] Recorre los elementos o repite según int i = 0; i < columnas.size(); i++. Cada vuelta ejecuta el cuerpo del bucle.
            for (int i = 0; i < columnas.size(); i++) {
                // [Doc] Ejecuta la llamada o modificación sheet.autoSizeColumn(i); Sus efectos dependen del método invocado y del objeto receptor.
                sheet.autoSizeColumn(i);
            }

            // [Doc] Ejecuta la llamada o modificación workbook.write(out); Sus efectos dependen del método invocado y del objeto receptor.
            workbook.write(out);
            // [Doc] Termina el método o la función y devuelve new ByteArrayInputStream(out.toByteArray()).
            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            // [Doc] Interrumpe el flujo lanzando new RuntimeException("Error generando Excel", e). El llamador o el manejador de errores decide la respuesta.
            throw new RuntimeException("Error generando Excel", e);
        }
    }
}

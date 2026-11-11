package com.pequelandia.pequelandia.controller;

import com.pequelandia.pequelandia.dto.ProductoMasVendidoDTO;
import com.pequelandia.pequelandia.dto.PuntoSerieDTO;
import com.pequelandia.pequelandia.model.Producto;
import com.pequelandia.pequelandia.model.Pedido;
import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.service.*;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** [Doc]
 * Prepara respuestas de descarga Excel para entidades e indicadores.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/export")
public class ExcelExportController {

    // [Doc] Atributo excelService (ExcelExportService): colaborador que concentra operaciones de excel export.
    @Autowired private  ExcelExportService excelService;
    // [Doc] Atributo usuarioService (IUsuarioService): colaborador que concentra operaciones de usuario.
    @Autowired private  IUsuarioService usuarioService;
    // [Doc] Atributo productoService (IProductoService): colaborador que concentra operaciones de producto.
    @Autowired private  IProductoService productoService;
    // [Doc] Atributo pedidoService (IPedidoService): colaborador que concentra operaciones de pedido.
    @Autowired private  IPedidoService pedidoService;
    // [Doc] Atributo estadisticasService (IEstadisticasService): colaborador que concentra operaciones de estadisticas.
    @Autowired private  IEstadisticasService estadisticasService;

    /** [Doc]
     * Construye una respuesta HTTP con los bytes del libro, el tipo MIME XLSX y la cabecera de descarga con el
     * nombre indicado.
     * @param data data.
     * @param fileName file name.
     * @return ResponseEntity&lt;byte[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    private ResponseEntity<byte[]> buildExcel(byte[] data, String fileName) {
        // [Doc] Termina el método o la función y devuelve ResponseEntity.ok() .contentType(MediaType.parseMediaType( "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) .header(HttpHeaders.CONTENT_....
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .body(data);
    }

    // ============================
    // 1) EXPORTAR ENTIDADES
    // ============================
    /** [Doc]
     * Genera una exportación de usuarios en el ámbito de ExcelExportController.
     * @return ResponseEntity&lt;byte[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping("/usuarios")
    public ResponseEntity<byte[]> exportUsuarios() throws Exception {
        // [Doc] Variable local lista (List<Usuario>): lista. Se inicializa con usuarioService.getUsuarios().
        List<Usuario> lista = usuarioService.getUsuarios();
        // [Doc] Termina el método o la función y devuelve buildExcel(excelService.exportUsuarios(lista), "usuarios.xlsx").
        return buildExcel(excelService.exportUsuarios(lista), "usuarios.xlsx");
    }

    /** [Doc]
     * Genera una exportación de productos en el ámbito de ExcelExportController.
     * @return ResponseEntity&lt;byte[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping("/productos")
    public ResponseEntity<byte[]> exportProductos() throws Exception {
        // [Doc] Variable local lista (List<Producto>): lista. Se inicializa con productoService.getProducto().
        List<Producto> lista = productoService.getProducto();
        // [Doc] Termina el método o la función y devuelve buildExcel(excelService.exportProductos(lista), "productos.xlsx").
        return buildExcel(excelService.exportProductos(lista), "productos.xlsx");
    }

    /** [Doc]
     * Genera una exportación de pedidos en el ámbito de ExcelExportController.
     * @return ResponseEntity&lt;byte[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping("/pedidos")
    public ResponseEntity<byte[]> exportPedidos() throws Exception {
        // [Doc] Variable local lista (List<Pedido>): lista. Se inicializa con pedidoService.getPedido().
        List<Pedido> lista = pedidoService.getPedido();
        // [Doc] Termina el método o la función y devuelve buildExcel(excelService.exportPedidos(lista), "pedidos.xlsx").
        return buildExcel(excelService.exportPedidos(lista), "pedidos.xlsx");
    }

    // ============================
    // 2) EXPORTAR SERIES
    // ============================
    /** [Doc]
     * Consulta importes de ventas de dia en el ámbito de ExcelExportController.
     * @param desde inicio del intervalo consultado.
     * @param hasta fin del intervalo consultado.
     * @return ResponseEntity&lt;byte[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping("/ventas-dia")
    public ResponseEntity<byte[]> ventasDia(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate hasta) throws Exception {

        // [Doc] Variable local s (List<PuntoSerieDTO>): s. Se inicializa con estadisticasService.getVentasPorDia( desde, hasta).
        List<PuntoSerieDTO> s = estadisticasService.getVentasPorDia(
                desde, hasta);

        // [Doc] Termina el método o la función y devuelve buildExcel(excelService.exportSeries(s, "ventas-dia"), "ventas_dia.xlsx").
        return buildExcel(excelService.exportSeries(s, "ventas-dia"),
                "ventas_dia.xlsx");
    }

    /** [Doc]
     * Consulta importes de ventas de mes en el ámbito de ExcelExportController.
     * @param anio año de la consulta.
     * @return ResponseEntity&lt;byte[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping("/ventas-mes/{anio}")
    public ResponseEntity<byte[]> ventasMes(@PathVariable int anio) throws Exception {
        // [Doc] Variable local s (List<PuntoSerieDTO>): s. Se inicializa con estadisticasService.getVentasPorMes(anio).
        List<PuntoSerieDTO> s = estadisticasService.getVentasPorMes(anio);
        // [Doc] Termina el método o la función y devuelve buildExcel(excelService.exportSeries(s, "ventas-mes"), "ventas_mes.xlsx").
        return buildExcel(excelService.exportSeries(s, "ventas-mes"),
                "ventas_mes.xlsx");
    }

    /** [Doc]
     * Consulta importes de ventas de anio en el ámbito de ExcelExportController.
     * @param inicio límite inicial del intervalo.
     * @param fin límite final del intervalo.
     * @return ResponseEntity&lt;byte[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping("/ventas-anio")
    public ResponseEntity<byte[]> ventasAnio(
            @RequestParam int inicio,
            @RequestParam int fin) throws Exception {

        // [Doc] Variable local s (List<PuntoSerieDTO>): s. Se inicializa con estadisticasService.getVentasPorAnio(inicio, fin).
        List<PuntoSerieDTO> s = estadisticasService.getVentasPorAnio(inicio, fin);
        // [Doc] Termina el método o la función y devuelve buildExcel(excelService.exportSeries(s, "ventas-anio"), "ventas_anio.xlsx").
        return buildExcel(excelService.exportSeries(s, "ventas-anio"),
                "ventas_anio.xlsx");
    }
    
    /** [Doc]
     * Genera y devuelve la descarga Excel de productos más vendidos.
     * @return ResponseEntity&lt;byte[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping("/top-productos")
    public ResponseEntity<byte[]> excelTopProductos() throws Exception {
        // [Doc] Variable local produtop (List<ProductoMasVendidoDTO>): produtop. Se inicializa con estadisticasService.getDashboardResumen().getTopProductos().
        List<ProductoMasVendidoDTO> produtop=estadisticasService.getDashboardResumen().getTopProductos();
     
        // [Doc] Variable local excel (byte[]): excel. Se inicializa con excelService.exportTopProductos(produtop,"top productos").
        byte[] excel =excelService.exportTopProductos(produtop,"top productos");
        
        // [Doc] Termina el método o la función y devuelve buildExcel(excel, "top-productos.xlsx").
        return buildExcel(excel, "top-productos.xlsx");
    }

    /** [Doc]
     * Genera y devuelve la descarga Excel de productos con stock bajo.
     * @return ResponseEntity&lt;byte[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping("/stock-bajo")
    public ResponseEntity<byte[]> excelStockBajo() throws Exception {
        // [Doc] Variable local lista (var; tipo inferido por Java): lista. Se inicializa con estadisticasService.getDashboardResumen().getProductosStockBajo().
        var lista=estadisticasService.getDashboardResumen().getProductosStockBajo();
        // [Doc] Variable local excel (var; tipo inferido por Java): excel. Se inicializa con excelService.exportStockBajo(lista, "Stock Bajo").
        var excel =excelService.exportStockBajo(lista, "Stock Bajo");
        // [Doc] Termina el método o la función y devuelve buildExcel(excel,"stock_bajo.xlsx").
        return buildExcel(excel,"stock_bajo.xlsx");
        
    }

}

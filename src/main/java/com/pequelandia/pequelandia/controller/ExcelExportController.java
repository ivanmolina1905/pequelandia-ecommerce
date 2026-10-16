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

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/export")
public class ExcelExportController {

    @Autowired private  ExcelExportService excelService;
    @Autowired private  IUsuarioService usuarioService;
    @Autowired private  IProductoService productoService;
    @Autowired private  IPedidoService pedidoService;
    @Autowired private  IEstadisticasService estadisticasService;

    private ResponseEntity<byte[]> buildExcel(byte[] data, String fileName) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .body(data);
    }

    // ============================
    // 1) EXPORTAR ENTIDADES
    // ============================
    @GetMapping("/usuarios")
    public ResponseEntity<byte[]> exportUsuarios() throws Exception {
        List<Usuario> lista = usuarioService.getUsuarios();
        return buildExcel(excelService.exportUsuarios(lista), "usuarios.xlsx");
    }

    @GetMapping("/productos")
    public ResponseEntity<byte[]> exportProductos() throws Exception {
        List<Producto> lista = productoService.getProducto();
        return buildExcel(excelService.exportProductos(lista), "productos.xlsx");
    }

    @GetMapping("/pedidos")
    public ResponseEntity<byte[]> exportPedidos() throws Exception {
        List<Pedido> lista = pedidoService.getPedido();
        return buildExcel(excelService.exportPedidos(lista), "pedidos.xlsx");
    }

    // ============================
    // 2) EXPORTAR SERIES
    // ============================
    @GetMapping("/ventas-dia")
    public ResponseEntity<byte[]> ventasDia(
            @RequestParam String desde,
            @RequestParam String hasta) throws Exception {

        List<PuntoSerieDTO> s = estadisticasService.getVentasPorDia(
                LocalDate.parse(desde), LocalDate.parse(hasta));

        return buildExcel(excelService.exportSeries(s, "ventas-dia"),
                "ventas_dia.xlsx");
    }

    @GetMapping("/ventas-mes/{anio}")
    public ResponseEntity<byte[]> ventasMes(@PathVariable int anio) throws Exception {
        List<PuntoSerieDTO> s = estadisticasService.getVentasPorMes(anio);
        return buildExcel(excelService.exportSeries(s, "ventas-mes"),
                "ventas_mes.xlsx");
    }

    @GetMapping("/ventas-anio")
    public ResponseEntity<byte[]> ventasAnio(
            @RequestParam int inicio,
            @RequestParam int fin) throws Exception {

        List<PuntoSerieDTO> s = estadisticasService.getVentasPorAnio(inicio, fin);
        return buildExcel(excelService.exportSeries(s, "ventas-anio"),
                "ventas_anio.xlsx");
    }
    
    @GetMapping("/top-productos")
    public ResponseEntity<byte[]> excelTopProductos() throws Exception {
        List<ProductoMasVendidoDTO> produtop=estadisticasService.getDashboardResumen().getTopProductos();
     
        byte[] excel =excelService.exportTopProductos(produtop,"top productos");
        
        return buildExcel(excel, "top-productos.xlsx");
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<byte[]> excelStockBajo() throws Exception {
        var lista=estadisticasService.getDashboardResumen().getProductosStockBajo();
        var excel =excelService.exportStockBajo(lista, "Stock Bajo");
        return buildExcel(excel,"stock_bajo.xlsx");
        
    }

}

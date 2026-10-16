package com.pequelandia.pequelandia.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pequelandia.pequelandia.model.Pedido;
import com.pequelandia.pequelandia.model.Producto;
import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.repository.IProductoRepository;
import com.pequelandia.pequelandia.service.IPedidoService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/pedido")
public class PedidoController {

    private final IPedidoService pedidoService;

    public PedidoController(IPedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }
    
    @Autowired
    private IProductoRepository productoRepo;

    
    
    @GetMapping("/{id}")
    @ResponseBody
    public Map<String, Object> detalle(@PathVariable Long id) {

        Pedido p = pedidoService.findPedido(id);
                

        return Map.of(
                "id", p.getId_pedido(),
                "fecha", p.getFecha_pedido(),
                "estado", p.getEstado_pedido(),
                "total", p.getCarrito().getTotal(),
                "items", p.getCarrito().getDetalles().stream().map(d -> {
                    int cant = d.getCantidad() != null ? d.getCantidad() : 0;
                    BigDecimal precio = BigDecimal.valueOf((double) d.getPrecio()); // precio guardado en detalle
                    BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(cant));

                    return Map.of(
                        "producto", d.getProducto().getNombre_producto(),
                        "cantidad", cant,
                        "precio", precio,
                        "subtotal", subtotal
                    );
                }).toList()
            );
        }
    
    
    @GetMapping("/{id}/pdf")
    public void descargarPdf(@PathVariable Long id, HttpServletResponse response) throws IOException {

        Pedido p = pedidoService.findPedido(id);
            

        BigDecimal total = p.getCarrito().getTotal(); // ajustá tipo
        //int puntos = calcularPuntos(total);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=pedido-" + p.getId_pedido() + ".pdf");

        com.lowagie.text.Document doc = new com.lowagie.text.Document();
        com.lowagie.text.pdf.PdfWriter.getInstance(doc, response.getOutputStream());
        doc.open();

        var title = new com.lowagie.text.Paragraph("Pequelandia - Detalle del Pedido");
        title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        doc.add(title);
        doc.add(new com.lowagie.text.Paragraph(" "));

        doc.add(new com.lowagie.text.Paragraph("Pedido N°: " + p.getId_pedido()));
        doc.add(new com.lowagie.text.Paragraph("Fecha: " + String.valueOf(p.getFecha_pedido())));   // ajustá formato si querés
        doc.add(new com.lowagie.text.Paragraph("Estado: " + String.valueOf(p.getEstado_pedido())));
        //doc.add(new com.lowagie.text.Paragraph("Puntos ganados: " + puntos));
        doc.add(new com.lowagie.text.Paragraph(" "));

        // Tabla
        com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        table.addCell("Producto");
        table.addCell("Cantidad");
        table.addCell("Subtotal");

        for (var d : p.getCarrito().getDetalles()) {
            table.addCell(d.getProducto().getNombre_producto());
            table.addCell(String.valueOf(d.getCantidad()));
            table.addCell("$ " + String.valueOf(d.getCarrito().getTotal()));
        }

        doc.add(table);
        doc.add(new com.lowagie.text.Paragraph(" "));

        var totalPar = new com.lowagie.text.Paragraph("TOTAL: $ " + total.toPlainString());
        totalPar.setAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
        doc.add(totalPar);

        doc.close();
    }
    
    @PostMapping("/{id}/cancelar")
    @ResponseBody
    public Map<String, Object> solicitarCancelacion(@PathVariable Long id, HttpSession session){
        Usuario u = (Usuario) session.getAttribute("usuario");
        if(u == null) return Map.of("ok", false, "redirect", "/login");

        Pedido p = pedidoService.findPedido(id);

        if (
        	    p.getUsuario() == null ||
        	    p.getUsuario().getId() != u.getId()
        	) {
        	    return Map.of("ok", false, "error", "No autorizado");
        	}

        String est = p.getEstado_pedido() != null ? p.getEstado_pedido().toUpperCase() : "";
        if(est.equals("ENTREGADO") || est.equals("CANCELADO")){
            return Map.of("ok", false, "error", "No se puede cancelar este pedido");
        }

        p.setEstado_pedido("CANCELACION_SOLICITADA");
        pedidoService.savePedido(p);

        return Map.of("ok", true);
    }
    
    @PostMapping("/{id}/confirmar-cancelacion")
    @ResponseBody
    @Transactional
    public Map<String, Object> confirmarCancelacion(@PathVariable Long id, HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuario");
        if (u == null) {
            return Map.of("ok", false, "redirect", "/login");
        }

        Pedido p = pedidoService.findPedido(id);
        if (p == null) {
            return Map.of("ok", false, "error", "Pedido no encontrado");
        }

        String est = p.getEstado_pedido() != null ? p.getEstado_pedido().toUpperCase() : "";
        if (est.equals("CANCELADO")) {
            return Map.of("ok", false, "error", "El pedido ya está cancelado");
        }

        // DEVOLVER STOCK SOLO UNA VEZ
        for (var d : p.getCarrito().getDetalles()) {
            Producto prod = productoRepo.findById(d.getProducto().getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            int cantidad = d.getCantidad() != null ? d.getCantidad() : 0;
            prod.setStock(prod.getStock() + cantidad);
            productoRepo.save(prod);
        }

        p.setEstado_pedido("CANCELADO");
        pedidoService.savePedido(p);

        return Map.of("ok", true);
    }

    
}
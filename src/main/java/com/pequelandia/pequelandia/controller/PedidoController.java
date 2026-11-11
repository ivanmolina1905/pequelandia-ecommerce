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

/** [Doc]
 * Expone el detalle y PDF de un pedido y las operaciones de solicitud y confirmación de cancelación.
 */
@Controller
@RequestMapping("/pedido")
public class PedidoController {

    // [Doc] Atributo seguimiento (com.pequelandia.pequelandia.service.SeguimientoPedidoService): colaborador que concentra operaciones de com.pequelandia.pequelandia.service.seguimiento pedido.
    @Autowired private com.pequelandia.pequelandia.service.SeguimientoPedidoService seguimiento;
    // [Doc] Atributo pedidoService (IPedidoService): colaborador que concentra operaciones de pedido. Su referencia no se reasigna después de inicializarse.
    private final IPedidoService pedidoService;

    /** [Doc]
     * Construye PedidoController y conserva sus dependencias o valores iniciales.
     * @param pedidoService colaborador que concentra operaciones de pedido.
     */
    public PedidoController(IPedidoService pedidoService) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.pedidoService = pedidoService;
        this.pedidoService = pedidoService;
    }
    


    
    
    /** [Doc]
     * Devuelve fecha, estado, historial, total y líneas del pedido como JSON; usa el precio guardado de cada
     * detalle.
     * @param id identificador del registro.
     * @return Map&lt;String, Object&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @GetMapping("/{id}")
    @ResponseBody
    public Map<String, Object> detalle(@PathVariable Long id) {

        // [Doc] Variable local p (Pedido): pedido consultado para esta operación. Se inicializa con pedidoService.findPedido(id).
        Pedido p = pedidoService.findPedido(id);
                

        // [Doc] Termina el método o la función y devuelve Map.of( "id", p.getId_pedido(), "fecha", p.getFecha_pedido(), "estado", p.getEstado_pedido(), "historial", seguimiento.listar(id), "total", p.getCarrito().ge....
        return Map.of(
                "id", p.getId_pedido(),
                "fecha", p.getFecha_pedido(),
                "estado", p.getEstado_pedido(),
                "historial", seguimiento.listar(id),
                "total", p.getCarrito().getTotal(),
                "items", p.getCarrito().getDetalles().stream().map(d -> {
                    // [Doc] Variable local cant (int): cantidad de unidades. Se inicializa con d.getCantidad() != null ? d.getCantidad() : 0.
                    int cant = d.getCantidad() != null ? d.getCantidad() : 0;
                    // [Doc] Variable local precio (BigDecimal): importe unitario usado en el cálculo. Se inicializa con new BigDecimal(Float.toString(d.getPrecio())).
                    BigDecimal precio = new BigDecimal(Float.toString(d.getPrecio())); // precio guardado en detalle
                    // [Doc] Variable local subtotal (BigDecimal): importe de una línea: precio por cantidad. Se inicializa con precio.multiply(BigDecimal.valueOf(cant)).
                    BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(cant));

                    // [Doc] Termina el método o la función y devuelve Map.of( "producto", d.getProducto().getNombre_producto(), "cantidad", cant, "precio", precio, "subtotal", subtotal ).
                    return Map.of(
                        "producto", d.getProducto().getNombre_producto(),
                        "cantidad", cant,
                        "precio", precio,
                        "subtotal", subtotal
                    );
                }).toList()
            );
        }
    
    
    /** [Doc]
     * Escribe la descarga PDF del pedido con sus datos, productos, cantidades, subtotales y total mediante OpenPDF.
     * @param id identificador del registro.
     * @param response respuesta HTTP que se envía al navegador.
     */
    @GetMapping("/{id}/pdf")
    public void descargarPdf(@PathVariable Long id, HttpServletResponse response) throws IOException {

        // [Doc] Variable local p (Pedido): pedido consultado para esta operación. Se inicializa con pedidoService.findPedido(id).
        Pedido p = pedidoService.findPedido(id);
            

        // [Doc] Variable local total (BigDecimal): importe acumulado. Se inicializa con p.getCarrito().getTotal().
        BigDecimal total = p.getCarrito().getTotal(); // ajustá tipo
        //int puntos = calcularPuntos(total);

        // [Doc] Actualiza una propiedad o configuración mediante la llamada: response.setContentType("application/pdf");
        response.setContentType("application/pdf");
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: response.setHeader("Content-Disposition", "attachment; filename=pedido-" + p.getId_pedido() + ".pdf");
        response.setHeader("Content-Disposition", "attachment; filename=pedido-" + p.getId_pedido() + ".pdf");

        // [Doc] Variable local doc (com.lowagie.text.Document): doc. Se inicializa con new com.lowagie.text.Document().
        com.lowagie.text.Document doc = new com.lowagie.text.Document();
        // [Doc] Ejecuta la llamada o modificación com.lowagie.text.pdf.PdfWriter.getInstance(doc, response.getOutputStream()); Sus efectos dependen del método invocado y del objeto receptor.
        com.lowagie.text.pdf.PdfWriter.getInstance(doc, response.getOutputStream());
        // [Doc] Ejecuta la llamada o modificación doc.open(); Sus efectos dependen del método invocado y del objeto receptor.
        doc.open();

        // [Doc] Variable local title (var; tipo inferido por Java): title. Se inicializa con new com.lowagie.text.Paragraph("Pequelandia - Detalle del Pedido").
        var title = new com.lowagie.text.Paragraph("Pequelandia - Detalle del Pedido");
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        // [Doc] Ejecuta la llamada o modificación doc.add(title); Sus efectos dependen del método invocado y del objeto receptor.
        doc.add(title);
        // [Doc] Ejecuta la llamada o modificación doc.add(new com.lowagie.text.Paragraph(" ")); Sus efectos dependen del método invocado y del objeto receptor.
        doc.add(new com.lowagie.text.Paragraph(" "));

        // [Doc] Ejecuta la llamada o modificación doc.add(new com.lowagie.text.Paragraph("Pedido N°: " + p.getId_pedido())); Sus efectos dependen del método invocado y del objeto receptor.
        doc.add(new com.lowagie.text.Paragraph("Pedido N°: " + p.getId_pedido()));
        // [Doc] Ejecuta la llamada o modificación doc.add(new com.lowagie.text.Paragraph("Fecha: " + String.valueOf(p.getFecha_pedido()))); Sus efectos dependen del método invocado y del objeto receptor.
        doc.add(new com.lowagie.text.Paragraph("Fecha: " + String.valueOf(p.getFecha_pedido())));   // ajustá formato si querés
        // [Doc] Ejecuta la llamada o modificación doc.add(new com.lowagie.text.Paragraph("Estado: " + String.valueOf(p.getEstado_pedido()))); Sus efectos dependen del método invocado y del objeto receptor.
        doc.add(new com.lowagie.text.Paragraph("Estado: " + String.valueOf(p.getEstado_pedido())));
        //doc.add(new com.lowagie.text.Paragraph("Puntos ganados: " + puntos));
        // [Doc] Ejecuta la llamada o modificación doc.add(new com.lowagie.text.Paragraph(" ")); Sus efectos dependen del método invocado y del objeto receptor.
        doc.add(new com.lowagie.text.Paragraph(" "));

        // Tabla
        // [Doc] Variable local table (com.lowagie.text.pdf.PdfPTable): table. Se inicializa con new com.lowagie.text.pdf.PdfPTable(3).
        com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(3);
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: table.setWidthPercentage(100);
        table.setWidthPercentage(100);
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: table.setSpacingBefore(10);
        table.setSpacingBefore(10);

        // [Doc] Ejecuta la llamada o modificación table.addCell("Producto"); Sus efectos dependen del método invocado y del objeto receptor.
        table.addCell("Producto");
        // [Doc] Ejecuta la llamada o modificación table.addCell("Cantidad"); Sus efectos dependen del método invocado y del objeto receptor.
        table.addCell("Cantidad");
        // [Doc] Ejecuta la llamada o modificación table.addCell("Subtotal"); Sus efectos dependen del método invocado y del objeto receptor.
        table.addCell("Subtotal");

        // [Doc] Recorre los elementos o repite según var d : p.getCarrito().getDetalles(). Cada vuelta ejecuta el cuerpo del bucle.
        for (var d : p.getCarrito().getDetalles()) {
            // [Doc] Ejecuta la llamada o modificación table.addCell(d.getProducto().getNombre_producto()); Sus efectos dependen del método invocado y del objeto receptor.
            table.addCell(d.getProducto().getNombre_producto());
            // [Doc] Ejecuta la llamada o modificación table.addCell(String.valueOf(d.getCantidad())); Sus efectos dependen del método invocado y del objeto receptor.
            table.addCell(String.valueOf(d.getCantidad()));
            // [Doc] Ejecuta la llamada o modificación table.addCell("$ " + new BigDecimal(Float.toString(d.getPrecio())).multiply(BigDecimal.valueOf(d.getCantidad())).toPlainString()); Sus efectos dependen del método invocado y del objeto receptor.
            table.addCell("$ " + new BigDecimal(Float.toString(d.getPrecio())).multiply(BigDecimal.valueOf(d.getCantidad())).toPlainString());
        }

        // [Doc] Ejecuta la llamada o modificación doc.add(table); Sus efectos dependen del método invocado y del objeto receptor.
        doc.add(table);
        // [Doc] Ejecuta la llamada o modificación doc.add(new com.lowagie.text.Paragraph(" ")); Sus efectos dependen del método invocado y del objeto receptor.
        doc.add(new com.lowagie.text.Paragraph(" "));

        // [Doc] Variable local totalPar (var; tipo inferido por Java): total par. Se inicializa con new com.lowagie.text.Paragraph("TOTAL: $ " + total.toPlainString()).
        var totalPar = new com.lowagie.text.Paragraph("TOTAL: $ " + total.toPlainString());
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: totalPar.setAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
        totalPar.setAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
        // [Doc] Ejecuta la llamada o modificación doc.add(totalPar); Sus efectos dependen del método invocado y del objeto receptor.
        doc.add(totalPar);

        // [Doc] Ejecuta la llamada o modificación doc.close(); Sus efectos dependen del método invocado y del objeto receptor.
        doc.close();
    }
    
    /** [Doc]
     * Solicita cancelacion en el ámbito de PedidoController.
     * @param id identificador del registro.
     * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
     * @return Map&lt;String, Object&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @PostMapping("/{id}/cancelar")
    @ResponseBody
    public Map<String, Object> solicitarCancelacion(@PathVariable Long id, HttpSession session){
        // [Doc] Variable local u (Usuario): cuenta de usuario utilizada por la operación. Se inicializa con (Usuario) session.getAttribute("usuario").
        Usuario u = (Usuario) session.getAttribute("usuario");
        // [Doc] Evalúa u == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if(u == null) return Map.of("ok", false, "redirect", "/login");

        // [Doc] Ejecuta la llamada o modificación pedidoService.solicitarCancelacion(id, u.getId()); Sus efectos dependen del método invocado y del objeto receptor.
        pedidoService.solicitarCancelacion(id, u.getId());
        // [Doc] Termina el método o la función y devuelve Map.of("ok", true).
        return Map.of("ok", true);
    }
    
    /** [Doc]
     * Confirma cancelacion en el ámbito de PedidoController.
     * @param id identificador del registro.
     * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
     * @return Map&lt;String, Object&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @PostMapping("/{id}/confirmar-cancelacion")
    @ResponseBody
    public Map<String, Object> confirmarCancelacion(@PathVariable Long id, HttpSession session) {
        // [Doc] Variable local u (Usuario): cuenta de usuario utilizada por la operación. Se inicializa con (Usuario) session.getAttribute("usuario").
        Usuario u = (Usuario) session.getAttribute("usuario");
        // [Doc] Evalúa u == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (u == null) {
            // [Doc] Termina el método o la función y devuelve Map.of("ok", false, "redirect", "/login").
            return Map.of("ok", false, "redirect", "/login");
        }

        // [Doc] Ejecuta la llamada o modificación pedidoService.confirmarCancelacion(id); Sus efectos dependen del método invocado y del objeto receptor.
        pedidoService.confirmarCancelacion(id);
        // [Doc] Termina el método o la función y devuelve Map.of("ok", true).
        return Map.of("ok", true);
    }

    
}
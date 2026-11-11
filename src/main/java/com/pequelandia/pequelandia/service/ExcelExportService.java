package com.pequelandia.pequelandia.service;

import com.pequelandia.pequelandia.dto.ProductoMasVendidoDTO;
import com.pequelandia.pequelandia.dto.PuntoSerieDTO;
import com.pequelandia.pequelandia.model.Pedido;
import com.pequelandia.pequelandia.model.Producto;
import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.util.Util;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/** [Doc]
 * Construye libros Excel de usuarios, productos, pedidos, series, ventas y stock bajo.
 */
@Service
public class ExcelExportService {

    // ============================================
    // EXPORTAR USUARIOS
    // ============================================
    /** [Doc]
     * Genera una exportación de usuarios en el ámbito de ExcelExportService.
     * @param usuarios usuarios.
     * @return byte[] con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public byte[] exportUsuarios(List<Usuario> usuarios) throws IOException {

        // [Doc] Variable local columnas (List<String>): columnas. Se inicializa con List.of("ID", "Nombre", "Apellido", "Email", "Teléfono").
        List<String> columnas = List.of("ID", "Nombre", "Apellido", "Email", "Teléfono");

        // [Doc] Variable local filas (List<List<Object>>): registros devueltos por la consulta SQL. Se inicializa con usuarios.stream() .map(u -> Arrays.asList( (Object) u.getId(), (Object) u.getNombre_usuario(), (Object) u.g....
        List<List<Object>> filas = usuarios.stream()
                .map(u -> Arrays.asList(
                        (Object) u.getId(),
                        (Object) u.getNombre_usuario(),
                        (Object) u.getApellido(),
                        (Object) u.getEmail(),
                        (Object) u.getTelefono()
                ))
                .toList();

        // [Doc] Variable local in (ByteArrayInputStream): in. Se inicializa con Util.generarExcel("Usuarios", columnas, filas).
        ByteArrayInputStream in = Util.generarExcel("Usuarios", columnas, filas);
        // [Doc] Termina el método o la función y devuelve in.readAllBytes().
        return in.readAllBytes();
    }


    // ============================================
    // EXPORTAR PRODUCTOS
    // ============================================
    /** [Doc]
     * Genera una exportación de productos en el ámbito de ExcelExportService.
     * @param productos productos.
     * @return byte[] con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public byte[] exportProductos(List<Producto> productos) throws IOException {

        // [Doc] Variable local columnas (List<String>): columnas. Se inicializa con List.of( "ID", "Nombre", "Precio", "Stock", "Marca", "Modelo", "Talle", "Estado" ).
        List<String> columnas = List.of(
                "ID",
                "Nombre",
                "Precio",
                "Stock",
                "Marca",
                "Modelo",
                "Talle",
                "Estado"
        );

        // [Doc] Variable local filas (List<List<Object>>): registros devueltos por la consulta SQL. Se inicializa con productos.stream() .map(p -> Arrays.asList( (Object) p.getIdProducto(), (Object) p.getNombre_producto(), (O....
        List<List<Object>> filas = productos.stream()
                .map(p -> Arrays.asList(
                		(Object)  p.getIdProducto(),
                		(Object)  p.getNombre_producto(),
                		(Object)  p.getPrecio(),
                		(Object) p.getStock(),
                		(Object) p.getMarca_producto(),
                		(Object) p.getModelo_producto(),
                		(Object) p.getTalle_producto(),
                		(Object)  p.getEstado()
                ))
                .toList();

        // [Doc] Variable local in (ByteArrayInputStream): in. Se inicializa con Util.generarExcel("Productos", columnas, filas).
        ByteArrayInputStream in = Util.generarExcel("Productos", columnas, filas);
        // [Doc] Termina el método o la función y devuelve in.readAllBytes().
        return in.readAllBytes();
    }


    // ============================================
    // EXPORTAR PEDIDOS
    // ============================================
    /** [Doc]
     * Genera una exportación de pedidos en el ámbito de ExcelExportService.
     * @param pedidos pedidos o servicio de pedidos según el tipo declarado.
     * @return byte[] con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public byte[] exportPedidos(List<Pedido> pedidos) throws IOException {

        // [Doc] Variable local columnas (List<String>): columnas. Se inicializa con List.of( "ID Pedido", "Usuario", "Fecha", "Total" ).
        List<String> columnas = List.of(
                "ID Pedido",
                "Usuario",
                "Fecha",
                "Total"
        );

        // [Doc] Variable local filas (List<List<Object>>): registros devueltos por la consulta SQL. Se inicializa con pedidos.stream() .map(p -> Arrays.asList( (Object) p.getId_pedido(), (Object) p.getUsuario() != null ? p.ge....
        List<List<Object>> filas = pedidos.stream()
                .map(p -> Arrays.asList(
                		(Object) p.getId_pedido(),
                		(Object) p.getUsuario() != null ? p.getUsuario().getNombre_usuario() : "(sin usuario)",
                		(Object) p.getFecha_pedido(),
                		(Object) p.getCarrito().getTotal()
                ))
                .toList();

        // [Doc] Variable local in (ByteArrayInputStream): in. Se inicializa con Util.generarExcel("Pedidos", columnas, filas).
        ByteArrayInputStream in = Util.generarExcel("Pedidos", columnas, filas);
        // [Doc] Termina el método o la función y devuelve in.readAllBytes().
        return in.readAllBytes();
    }


    // ============================================
    // EXPORTAR SERIES (DÍA / MES / AÑO)
    // ============================================
    /** [Doc]
     * Genera una exportación de series en el ámbito de ExcelExportService.
     * @param serie serie.
     * @param nombreHoja nombre hoja.
     * @return byte[] con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public byte[] exportSeries(List<PuntoSerieDTO> serie, String nombreHoja) throws IOException {

        // [Doc] Variable local columnas (List<String>): columnas. Se inicializa con List.of("Etiqueta", "Valor").
        List<String> columnas = List.of("Etiqueta", "Valor");

        // [Doc] Variable local filas (List<List<Object>>): registros devueltos por la consulta SQL. Se inicializa con serie.stream() .map(p -> Arrays.asList( (Object) p.getLabel(), (Object) p.getValor() )) .toList().
        List<List<Object>> filas = serie.stream()
                .map(p -> Arrays.asList(
                		(Object) p.getLabel(),
                		(Object)  p.getValor()
                ))
                .toList();

        // [Doc] Variable local in (ByteArrayInputStream): in. Se inicializa con Util.generarExcel(nombreHoja, columnas, filas).
        ByteArrayInputStream in = Util.generarExcel(nombreHoja, columnas, filas);
        // [Doc] Termina el método o la función y devuelve in.readAllBytes().
        return in.readAllBytes();
    }
    
    /** [Doc]
     * Genera una exportación de top productos en el ámbito de ExcelExportService.
     * @param datos datos.
     * @param nombreHoja nombre hoja.
     * @return byte[] con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public byte[] exportTopProductos(List<ProductoMasVendidoDTO> datos, String nombreHoja) throws IOException {

        // [Doc] Variable local columnas (List<String>): columnas. Se inicializa con List.of("ID", "Producto", "Vendidos").
        List<String> columnas = List.of("ID", "Producto", "Vendidos");

        // [Doc] Variable local filas (List<List<Object>>): registros devueltos por la consulta SQL. Se inicializa con datos.stream() .map(p -> Arrays.asList( (Object) p.getIdProducto(), (Object) p.getNombre(), (Object) p.getC....
        List<List<Object>> filas = datos.stream()
                .map(p -> Arrays.asList(
                        (Object) p.getIdProducto(),
                        (Object) p.getNombre(),
                        (Object) p.getCantidadVendida()
                ))
                .toList();

        // [Doc] Variable local in (ByteArrayInputStream): in. Se inicializa con Util.generarExcel(nombreHoja, columnas, filas).
        ByteArrayInputStream in = Util.generarExcel(nombreHoja, columnas, filas);
        // [Doc] Termina el método o la función y devuelve in.readAllBytes().
        return in.readAllBytes();
    }

    /** [Doc]
     * Genera una exportación de stock bajo en el ámbito de ExcelExportService.
     * @param productos productos.
     * @param nombreHoja nombre hoja.
     * @return byte[] con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public byte[] exportStockBajo(List<Producto> productos, String nombreHoja) throws IOException {

        // [Doc] Variable local columnas (List<String>): columnas. Se inicializa con List.of("Id","Producto", "Stock").
        List<String> columnas = List.of("Id","Producto", "Stock");

        // [Doc] Variable local filas (List<List<Object>>): registros devueltos por la consulta SQL. Se inicializa con productos.stream() .map(p -> Arrays.asList( (Object) p.getIdProducto(), (Object) p.getNombre_producto(), (O....
        List<List<Object>> filas = productos.stream()
                .map(p -> Arrays.asList(
                		(Object) p.getIdProducto(),
                        (Object) p.getNombre_producto(),
                        (Object) p.getStock()
                ))
                .toList();

        // [Doc] Variable local in (ByteArrayInputStream): in. Se inicializa con Util.generarExcel(nombreHoja, columnas, filas).
        ByteArrayInputStream in = Util.generarExcel(nombreHoja, columnas, filas);
        // [Doc] Termina el método o la función y devuelve in.readAllBytes().
        return in.readAllBytes();
    }

}

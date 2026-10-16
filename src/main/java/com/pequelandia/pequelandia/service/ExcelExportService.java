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

@Service
public class ExcelExportService {

    // ============================================
    // EXPORTAR USUARIOS
    // ============================================
    public byte[] exportUsuarios(List<Usuario> usuarios) throws IOException {

        List<String> columnas = List.of("ID", "Nombre", "Apellido", "Email", "Teléfono");

        List<List<Object>> filas = usuarios.stream()
                .map(u -> Arrays.asList(
                        (Object) u.getId(),
                        (Object) u.getNombre_usuario(),
                        (Object) u.getApellido(),
                        (Object) u.getEmail(),
                        (Object) u.getTelefono()
                ))
                .toList();

        ByteArrayInputStream in = Util.generarExcel("Usuarios", columnas, filas);
        return in.readAllBytes();
    }


    // ============================================
    // EXPORTAR PRODUCTOS
    // ============================================
    public byte[] exportProductos(List<Producto> productos) throws IOException {

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

        ByteArrayInputStream in = Util.generarExcel("Productos", columnas, filas);
        return in.readAllBytes();
    }


    // ============================================
    // EXPORTAR PEDIDOS
    // ============================================
    public byte[] exportPedidos(List<Pedido> pedidos) throws IOException {

        List<String> columnas = List.of(
                "ID Pedido",
                "Usuario",
                "Fecha",
                "Total"
        );

        List<List<Object>> filas = pedidos.stream()
                .map(p -> Arrays.asList(
                		(Object) p.getId_pedido(),
                		(Object) p.getUsuario() != null ? p.getUsuario().getNombre_usuario() : "(sin usuario)",
                		(Object) p.getFecha_pedido(),
                		(Object) p.getCarrito().getTotal()
                ))
                .toList();

        ByteArrayInputStream in = Util.generarExcel("Pedidos", columnas, filas);
        return in.readAllBytes();
    }


    // ============================================
    // EXPORTAR SERIES (DÍA / MES / AÑO)
    // ============================================
    public byte[] exportSeries(List<PuntoSerieDTO> serie, String nombreHoja) throws IOException {

        List<String> columnas = List.of("Etiqueta", "Valor");

        List<List<Object>> filas = serie.stream()
                .map(p -> Arrays.asList(
                		(Object) p.getLabel(),
                		(Object)  p.getValor()
                ))
                .toList();

        ByteArrayInputStream in = Util.generarExcel(nombreHoja, columnas, filas);
        return in.readAllBytes();
    }
    
    public byte[] exportTopProductos(List<ProductoMasVendidoDTO> datos, String nombreHoja) throws IOException {

        List<String> columnas = List.of("ID", "Producto", "Vendidos");

        List<List<Object>> filas = datos.stream()
                .map(p -> Arrays.asList(
                        (Object) p.getIdProducto(),
                        (Object) p.getNombre(),
                        (Object) p.getCantidadVendida()
                ))
                .toList();

        ByteArrayInputStream in = Util.generarExcel(nombreHoja, columnas, filas);
        return in.readAllBytes();
    }

    public byte[] exportStockBajo(List<Producto> productos, String nombreHoja) throws IOException {

        List<String> columnas = List.of("Id","Producto", "Stock");

        List<List<Object>> filas = productos.stream()
                .map(p -> Arrays.asList(
                		(Object) p.getIdProducto(),
                        (Object) p.getNombre_producto(),
                        (Object) p.getStock()
                ))
                .toList();

        ByteArrayInputStream in = Util.generarExcel(nombreHoja, columnas, filas);
        return in.readAllBytes();
    }

}

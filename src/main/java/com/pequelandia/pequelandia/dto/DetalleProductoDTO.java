package com.pequelandia.pequelandia.dto;

public class DetalleProductoDTO {

    private String nombreProducto;
    private Integer cantidad;
    private Double precioUnitario;

    public DetalleProductoDTO(String nombreProducto, Integer cantidad, Double precioUnitario) {
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public String getNombreProducto() { return nombreProducto; }
    public Integer getCantidad() { return cantidad; }
    public Double getPrecioUnitario() { return precioUnitario; }
}
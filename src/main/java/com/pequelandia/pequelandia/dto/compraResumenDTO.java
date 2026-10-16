package com.pequelandia.pequelandia.dto;

public class compraResumenDTO {

    private Long id;
    private String fecha;       // formateada como texto
    private Double montoTotal;  // suma de la compra

    public compraResumenDTO(Long id, String fecha, Double montoTotal) {
        this.id = id;
        this.fecha = fecha;
        this.montoTotal = montoTotal;
    }

    public Long getId() { return id; }
    public String getFecha() { return fecha; }
    public Double getMontoTotal() { return montoTotal; }
}
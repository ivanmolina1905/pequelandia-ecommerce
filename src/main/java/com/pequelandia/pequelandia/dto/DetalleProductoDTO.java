package com.pequelandia.pequelandia.dto;

/** [Doc]
 * Transporta datos de detalle producto entre consultas, servicios, exportaciones y vistas sin representar una
 * entidad persistente.
 */
public class DetalleProductoDTO {

    // [Doc] Atributo nombreProducto (String): nombre producto.
    private String nombreProducto;
    // [Doc] Atributo cantidad (Integer): unidades de una línea de carrito.
    private Integer cantidad;
    // [Doc] Atributo precioUnitario (Double): precio por unidad de la línea.
    private Double precioUnitario;

    /** [Doc]
     * Construye DetalleProductoDTO y conserva sus dependencias o valores iniciales.
     * @param nombreProducto nombre producto.
     * @param cantidad unidades de una línea de carrito.
     * @param precioUnitario precio por unidad de la línea.
     */
    public DetalleProductoDTO(String nombreProducto, Integer cantidad, Double precioUnitario) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.nombreProducto = nombreProducto;
        this.nombreProducto = nombreProducto;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.cantidad = cantidad;
        this.cantidad = cantidad;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.precioUnitario = precioUnitario;
        this.precioUnitario = precioUnitario;
    }

    /** [Doc]
     * Devuelve nombre producto, según el cuerpo o la implementación del contrato.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public String getNombreProducto() { return nombreProducto; }
    /** [Doc]
     * Devuelve unidades de una línea de carrito, según el cuerpo o la implementación del contrato.
     * @return Integer con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public Integer getCantidad() { return cantidad; }
    /** [Doc]
     * Devuelve precio por unidad de la línea, según el cuerpo o la implementación del contrato.
     * @return Double con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public Double getPrecioUnitario() { return precioUnitario; }
}
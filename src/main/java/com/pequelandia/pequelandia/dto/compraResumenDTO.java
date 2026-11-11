package com.pequelandia.pequelandia.dto;

/** [Doc]
 * Transporta datos de compra resumen entre consultas, servicios, exportaciones y vistas sin representar una
 * entidad persistente.
 */
public class compraResumenDTO {

    // [Doc] Atributo id (Long): identificador del registro.
    private Long id;
    // [Doc] Atributo fecha (String): fecha o etiqueta temporal de la operación.
    private String fecha;       // formateada como texto
    // [Doc] Atributo montoTotal (Double): importe total del resumen de compra.
    private Double montoTotal;  // suma de la compra

    /** [Doc]
     * Construye compraResumenDTO y conserva sus dependencias o valores iniciales.
     * @param id identificador del registro.
     * @param fecha fecha o etiqueta temporal de la operación.
     * @param montoTotal importe total del resumen de compra.
     */
    public compraResumenDTO(Long id, String fecha, Double montoTotal) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.id = id;
        this.id = id;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.fecha = fecha;
        this.fecha = fecha;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.montoTotal = montoTotal;
        this.montoTotal = montoTotal;
    }

    /** [Doc]
     * Devuelve identificador del registro, según el cuerpo o la implementación del contrato.
     * @return Long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public Long getId() { return id; }
    /** [Doc]
     * Devuelve fecha o etiqueta temporal de la operación, según el cuerpo o la implementación del contrato.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public String getFecha() { return fecha; }
    /** [Doc]
     * Devuelve importe total del resumen de compra, según el cuerpo o la implementación del contrato.
     * @return Double con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public Double getMontoTotal() { return montoTotal; }
}
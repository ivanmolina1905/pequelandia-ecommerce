package com.pequelandia.pequelandia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** [Doc]
 * Transporta datos de punto serie entre consultas, servicios, exportaciones y vistas sin representar una entidad
 * persistente.
 */
@Data

@NoArgsConstructor
public class PuntoSerieDTO {
    /** [Doc]
     * Construye PuntoSerieDTO y conserva sus dependencias o valores iniciales.
     * @param string string.
     * @param total importe acumulado.
     */
    public PuntoSerieDTO(String string, Double total) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.label = string;
		this.label = string; this.valor = total;
	}
    // [Doc] Atributo label (String): label.
	private String label;   // día, mes o año
    // [Doc] Atributo valor (Double): valor recibido o calculado para este paso.
    private Double valor;   // total vendido
    
    /** [Doc]
     * Devuelve label, según el cuerpo o la implementación del contrato.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public String getLabel() {
        // [Doc] Termina el método o la función y devuelve label.
		return label;
	}
    /** [Doc]
     * Asigna label con el valor recibido.
     * @param label label.
     */
	public void setLabel(String label) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.label = label;
		this.label = label;
	}
    /** [Doc]
     * Devuelve valor recibido o calculado para este paso, según el cuerpo o la implementación del contrato.
     * @return Double con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public Double getValor() {
        // [Doc] Termina el método o la función y devuelve valor.
		return valor;
	}
    /** [Doc]
     * Asigna valor recibido o calculado para este paso con el valor recibido.
     * @param valor valor recibido o calculado para este paso.
     */
	public void setValor(Double valor) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.valor = valor;
		this.valor = valor;
	}
}

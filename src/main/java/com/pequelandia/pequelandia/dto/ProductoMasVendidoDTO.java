package com.pequelandia.pequelandia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** [Doc]
 * Transporta datos de producto mas vendido entre consultas, servicios, exportaciones y vistas sin representar
 * una entidad persistente.
 */
@Data

public class ProductoMasVendidoDTO {
    // [Doc] Atributo idProducto (Long): identificador del producto.
    private Long idProducto;
    // [Doc] Atributo nombre (String): nombre normalizado o visible de la entidad.
    private String nombre;
    // [Doc] Atributo cantidadVendida (Long): cantidad vendida.
    private Long cantidadVendida;
    
    /** [Doc]
     * Construye ProductoMasVendidoDTO y conserva sus dependencias o valores iniciales.
     * @param idProducto identificador del producto.
     * @param nombre nombre normalizado o visible de la entidad.
     * @param cantidadVendida cantidad vendida.
     */
    public ProductoMasVendidoDTO(Long idProducto, String nombre, Long cantidadVendida) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.idProducto = idProducto;
        this.idProducto = idProducto;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.nombre = nombre;
        this.nombre = nombre;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.cantidadVendida = cantidadVendida;
        this.cantidadVendida = cantidadVendida;
    }

    /** [Doc]
     * Devuelve identificador del producto, según el cuerpo o la implementación del contrato.
     * @return Long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public Long getIdProducto() {
        // [Doc] Termina el método o la función y devuelve idProducto.
		return idProducto;
	}

    /** [Doc]
     * Asigna identificador del producto con el valor recibido.
     * @param idProducto identificador del producto.
     */
	public void setIdProducto(Long idProducto) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.idProducto = idProducto;
		this.idProducto = idProducto;
	}

    /** [Doc]
     * Devuelve nombre normalizado o visible de la entidad, según el cuerpo o la implementación del contrato.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public String getNombre() {
        // [Doc] Termina el método o la función y devuelve nombre.
		return nombre;
	}

    /** [Doc]
     * Asigna nombre normalizado o visible de la entidad con el valor recibido.
     * @param nombre nombre normalizado o visible de la entidad.
     */
	public void setNombre(String nombre) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.nombre = nombre;
		this.nombre = nombre;
	}

    /** [Doc]
     * Devuelve cantidad vendida, según el cuerpo o la implementación del contrato.
     * @return Long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public Long getCantidadVendida() {
        // [Doc] Termina el método o la función y devuelve cantidadVendida.
		return cantidadVendida;
	}

    /** [Doc]
     * Asigna cantidad vendida con el valor recibido.
     * @param cantidadVendida cantidad vendida.
     */
	public void setCantidadVendida(Long cantidadVendida) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.cantidadVendida = cantidadVendida;
		this.cantidadVendida = cantidadVendida;
	}
    
}


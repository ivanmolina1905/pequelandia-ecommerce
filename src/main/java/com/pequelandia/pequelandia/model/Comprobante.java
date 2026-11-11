package com.pequelandia.pequelandia.model;



import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

//esta clase representa a un comprobante en la bs
//tabla comprobante(id_comprobante,descripcion,fecha_venta,total_venta,tipo_comprobante,n_comprobante,pedido)
/** [Doc]
 * Representa los datos y el importe del comprobante asociado a un pedido.
 */
@Getter @Setter
@Entity
public class Comprobante {
	
    // [Doc] Atributo id_comprobante (long): id comprobante.
	@Id@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id_comprobante;
    // [Doc] Atributo descripcion (String): texto descriptivo de la entidad u operación.
	private String descripcion;
    // [Doc] Atributo fecha_venta (LocalDateTime): fecha venta.
	private LocalDateTime fecha_venta;
    // [Doc] Atributo total_venta (BigDecimal): total venta.
	private BigDecimal total_venta;
    // [Doc] Atributo tipo_comprobante (String): tipo comprobante.
	private String tipo_comprobante;
    // [Doc] Atributo n_comprobante (int): n comprobante.
	private int n_comprobante;
    // [Doc] Atributo pedido (Pedido): pedido procesado en este punto.
	@OneToOne
    @JoinColumn(name = "id_pedido")
	private Pedido pedido;
	
	
    /** [Doc]
     * Construye Comprobante y conserva sus dependencias o valores iniciales.
     * @param id_comprobante id comprobante.
     * @param descripcion texto descriptivo de la entidad u operación.
     * @param fecha_venta fecha venta.
     * @param total_venta total venta.
     * @param tipo_comprobante tipo comprobante.
     * @param n_comrobante n comrobante.
     * @param pedido pedido procesado en este punto.
     */
	public Comprobante(long id_comprobante, String descripcion, LocalDateTime fecha_venta, BigDecimal total_venta,
			String tipo_comprobante, int n_comrobante, Pedido pedido) {
        // [Doc] Ejecuta la llamada o modificación super(); Sus efectos dependen del método invocado y del objeto receptor.
		super();
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.id_comprobante = id_comprobante;
		this.id_comprobante = id_comprobante;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.descripcion = descripcion;
		this.descripcion = descripcion;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.fecha_venta = fecha_venta;
		this.fecha_venta =  fecha_venta;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.total_venta = total_venta;
		this.total_venta = total_venta;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.tipo_comprobante = tipo_comprobante;
		this.tipo_comprobante = tipo_comprobante;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.n_comprobante = n_comrobante;
		this.n_comprobante = n_comrobante;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.pedido = pedido;
		this.pedido = pedido;
	}
	
    /** [Doc]
     * Construye Comprobante y conserva sus dependencias o valores iniciales.
     */
	public Comprobante() {
		
	}
	
    /** [Doc]
     * Devuelve id comprobante, según el cuerpo o la implementación del contrato.
     * @return long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public long getId_comprobante() {
        // [Doc] Termina el método o la función y devuelve id_comprobante.
		return id_comprobante;
	}
    /** [Doc]
     * Asigna id comprobante con el valor recibido.
     * @param id_comprobante id comprobante.
     */
	public void setId_comprobante(long id_comprobante) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.id_comprobante = id_comprobante;
		this.id_comprobante = id_comprobante;
	}
    /** [Doc]
     * Devuelve texto descriptivo de la entidad u operación, según el cuerpo o la implementación del contrato.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public String getDescripcion() {
        // [Doc] Termina el método o la función y devuelve descripcion.
		return descripcion;
	}
    /** [Doc]
     * Asigna texto descriptivo de la entidad u operación con el valor recibido.
     * @param descripcion texto descriptivo de la entidad u operación.
     */
	public void setDescripcion(String descripcion) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.descripcion = descripcion;
		this.descripcion = descripcion;
	}
    /** [Doc]
     * Devuelve fecha venta, según el cuerpo o la implementación del contrato.
     * @return LocalDateTime con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public LocalDateTime getFecha_venta() {
        // [Doc] Termina el método o la función y devuelve fecha_venta.
		return  fecha_venta;
	}
    /** [Doc]
     * Asigna fecha venta con el valor recibido.
     * @param localDate local date.
     */
	public void setFecha_venta(LocalDateTime localDate) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.fecha_venta = localDate;
		this.fecha_venta = localDate;
	}
    /** [Doc]
     * Devuelve total venta, según el cuerpo o la implementación del contrato.
     * @return BigDecimal con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public BigDecimal getTotal_venta() {
        // [Doc] Termina el método o la función y devuelve total_venta.
		return total_venta;
	}
    /** [Doc]
     * Asigna total venta con el valor recibido.
     * @param total_venta total venta.
     */
	public void setTotal_venta(BigDecimal total_venta) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.total_venta = total_venta;
		this.total_venta = total_venta;
	}
    /** [Doc]
     * Devuelve tipo comprobante, según el cuerpo o la implementación del contrato.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public String getTipo_comprobante() {
        // [Doc] Termina el método o la función y devuelve tipo_comprobante.
		return tipo_comprobante;
	}
    /** [Doc]
     * Asigna tipo comprobante con el valor recibido.
     * @param tipo_comprobante tipo comprobante.
     */
	public void setTipo_comprobante(String tipo_comprobante) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.tipo_comprobante = tipo_comprobante;
		this.tipo_comprobante = tipo_comprobante;
	}
    /** [Doc]
     * Devuelve n comprobante, según el cuerpo o la implementación del contrato.
     * @return int con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public int getN_comprobante() {
        // [Doc] Termina el método o la función y devuelve n_comprobante.
		return n_comprobante;
	}
    /** [Doc]
     * Asigna n comprobante con el valor recibido.
     * @param n_comprobante n comprobante.
     */
	public void setN_comprobante(int n_comprobante) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.n_comprobante = n_comprobante;
		this.n_comprobante = n_comprobante;
	}
    /** [Doc]
     * Devuelve pedido, según el cuerpo o la implementación del contrato.
     * @return Pedido con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public Pedido getPedido() {
        // [Doc] Termina el método o la función y devuelve pedido.
		return pedido;
	}
    /** [Doc]
     * Asigna pedido con el valor recibido.
     * @param pedido pedido procesado en este punto.
     */
	public void setPedido(Pedido pedido) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.pedido = pedido;
		this.pedido = pedido;
	}
	
	
	

}

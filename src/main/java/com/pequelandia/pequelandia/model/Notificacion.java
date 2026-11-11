package com.pequelandia.pequelandia.model;




import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

/*clase que representa a una notificacion en la bs*/
//tabla Notificacion(id_notificacion,fecha_notificacion,destinatario,descripcion,pedido)
/** [Doc]
 * Representa un aviso persistente con destinatario, fecha, descripción y pedido opcional.
 */
@Getter @Setter
@Entity
public class Notificacion {

    // [Doc] Atributo id_notificacion (long): id notificacion.
	@Id@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id_notificacion;
    // [Doc] Atributo fecha_notificacion (LocalDateTime): fecha notificacion.
	private LocalDateTime fecha_notificacion;
    // [Doc] Atributo destinatario (String): identidad o dirección que recibirá el aviso o correo.
	private String destinatario;
    // [Doc] Atributo descripcion (String): texto descriptivo de la entidad u operación.
	private String descripcion;
	
    // [Doc] Atributo pedido (Pedido): pedido procesado en este punto.
	@ManyToOne
	@JoinColumn(name="id_pedido")
	private Pedido pedido;
	
	
    /** [Doc]
     * Construye Notificacion y conserva sus dependencias o valores iniciales.
     * @param id_notificacion id notificacion.
     * @param fecha_notificacion fecha notificacion.
     * @param destinatario identidad o dirección que recibirá el aviso o correo.
     * @param descripcion texto descriptivo de la entidad u operación.
     * @param pedido pedido procesado en este punto.
     */
	public Notificacion(long id_notificacion, LocalDateTime fecha_notificacion, String destinatario, String descripcion,
			Pedido pedido) {
        // [Doc] Ejecuta la llamada o modificación super(); Sus efectos dependen del método invocado y del objeto receptor.
		super();
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.id_notificacion = id_notificacion;
		this.id_notificacion = id_notificacion;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.fecha_notificacion = fecha_notificacion;
		this.fecha_notificacion = fecha_notificacion;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.destinatario = destinatario;
		this.destinatario = destinatario;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.descripcion = descripcion;
		this.descripcion = descripcion;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.pedido =pedido;
		this.pedido =pedido;
	}


    /** [Doc]
     * Devuelve id notificacion, según el cuerpo o la implementación del contrato.
     * @return long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public long getId_notificacion() {
        // [Doc] Termina el método o la función y devuelve id_notificacion.
		return id_notificacion;
	}


    /** [Doc]
     * Asigna id notificacion con el valor recibido.
     * @param id_notificacion id notificacion.
     */
	public void setId_notificacion(long id_notificacion) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.id_notificacion = id_notificacion;
		this.id_notificacion = id_notificacion;
	}


    /** [Doc]
     * Devuelve fecha notificacion, según el cuerpo o la implementación del contrato.
     * @return LocalDateTime con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public LocalDateTime getFecha_notificacion() {
        // [Doc] Termina el método o la función y devuelve fecha_notificacion.
		return fecha_notificacion;
	}


    /** [Doc]
     * Asigna fecha notificacion con el valor recibido.
     * @param localDateTime local date time.
     */
	public void setFecha_notificacion(LocalDateTime localDateTime) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.fecha_notificacion = localDateTime;
		this.fecha_notificacion = localDateTime;
	}


    /** [Doc]
     * Devuelve identidad o dirección que recibirá el aviso o correo, según el cuerpo o la implementación del
     * contrato.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public String getDestinatario() {
        // [Doc] Termina el método o la función y devuelve destinatario.
		return destinatario;
	}


    /** [Doc]
     * Asigna identidad o dirección que recibirá el aviso o correo con el valor recibido.
     * @param destinatario identidad o dirección que recibirá el aviso o correo.
     */
	public void setDestinatario(String destinatario) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.destinatario = destinatario;
		this.destinatario = destinatario;
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


    /** [Doc]
     * Construye Notificacion y conserva sus dependencias o valores iniciales.
     */
	public Notificacion() {
		// TODO Auto-generated constructor stub
	}

}

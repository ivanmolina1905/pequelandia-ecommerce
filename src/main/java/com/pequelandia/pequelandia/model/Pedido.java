package com.pequelandia.pequelandia.model;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

/*clase que representa a un pedido en bs*/
//tabla pedido(id_pedido,usuario,fecha_pedido,estado_pedido,carrit)
/** [Doc]
 * Representa una compra con usuario, fecha, estado, carrito, comprobante y avisos.
 */
@Getter @Setter
@Entity
public class Pedido {

    // [Doc] Atributo id_pedido (long): id pedido.
	@Id@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id_pedido;
	
    // [Doc] Atributo usuario (Usuario): cuenta de usuario utilizada por la operación.
	@ManyToOne
	@JoinColumn(name = "id_usuario")
	private Usuario usuario;
	
	
    // [Doc] Atributo fecha_pedido (Date): fecha pedido.
	private Date fecha_pedido;
    // [Doc] Atributo estado_pedido (String): estado pedido.
	private String estado_pedido;
	
    // [Doc] Atributo comprobante (Comprobante): comprobante de la compra.
	@OneToOne(mappedBy = "pedido")
    private Comprobante comprobante; // 1-1

    // [Doc] Atributo notificaciones (List<Notificacion>): notificaciones. Se inicializa con new ArrayList<>().
	@OneToMany(mappedBy = "pedido")
    private List<Notificacion> notificaciones = new ArrayList<>();
	
    // [Doc] Atributo carrito (Carrito_de_compra): carrito persistente procesado.
	@OneToOne
    @JoinColumn(name = "id_carrito", unique = true)
    private Carrito_de_compra carrito; // FK → carrito_de_compra (1-1)

	
    /** [Doc]
     * Construye Pedido y conserva sus dependencias o valores iniciales.
     * @param id_pedido id pedido.
     * @param usuario cuenta de usuario utilizada por la operación.
     * @param fecha_pedido fecha pedido.
     * @param estado_pedido estado pedido.
     * @param comprobante comprobante de la compra.
     * @param notificaciones notificaciones.
     * @param carrito carrito persistente procesado.
     */
	public Pedido(long id_pedido, Usuario usuario, Date fecha_pedido, String estado_pedido, Comprobante comprobante,
			List<Notificacion> notificaciones, Carrito_de_compra carrito) {
        // [Doc] Ejecuta la llamada o modificación super(); Sus efectos dependen del método invocado y del objeto receptor.
		super();
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.id_pedido = id_pedido;
		this.id_pedido = id_pedido;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.usuario = usuario;
		this.usuario = usuario;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.fecha_pedido = fecha_pedido;
		this.fecha_pedido = fecha_pedido;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.estado_pedido = estado_pedido;
		this.estado_pedido = estado_pedido;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.comprobante = comprobante;
		this.comprobante = comprobante;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.notificaciones = notificaciones;
		this.notificaciones = notificaciones;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.carrito = carrito;
		this.carrito = carrito;
	}
	
    /** [Doc]
     * Construye Pedido y conserva sus dependencias o valores iniciales.
     */
	public Pedido() {
		
	}

    /** [Doc]
     * Devuelve id pedido, según el cuerpo o la implementación del contrato.
     * @return long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public long getId_pedido() {
        // [Doc] Termina el método o la función y devuelve id_pedido.
		return id_pedido;
	}

    /** [Doc]
     * Asigna id pedido con el valor recibido.
     * @param id_pedido id pedido.
     */
	public void setId_pedido(long id_pedido) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.id_pedido = id_pedido;
		this.id_pedido = id_pedido;
	}

    /** [Doc]
     * Devuelve cuenta de usuario utilizada por la operación, según el cuerpo o la implementación del contrato.
     * @return Usuario con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public Usuario getUsuario() {
        // [Doc] Termina el método o la función y devuelve usuario.
		return usuario;
	}

    /** [Doc]
     * Asigna cuenta de usuario utilizada por la operación con el valor recibido.
     * @param usuario cuenta de usuario utilizada por la operación.
     */
	public void setUsuario(Usuario usuario) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.usuario = usuario;
		this.usuario = usuario;
	}

    /** [Doc]
     * Devuelve fecha pedido, según el cuerpo o la implementación del contrato.
     * @return Date con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public Date getFecha_pedido() {
        // [Doc] Termina el método o la función y devuelve fecha_pedido.
		return fecha_pedido;
	}

    /** [Doc]
     * Asigna fecha pedido con el valor recibido.
     * @param fecha_pedido fecha pedido.
     */
	public void setFecha_pedido(Date fecha_pedido) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.fecha_pedido = fecha_pedido;
		this.fecha_pedido = fecha_pedido;
	}

    /** [Doc]
     * Devuelve estado pedido, según el cuerpo o la implementación del contrato.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public String getEstado_pedido() {
        // [Doc] Termina el método o la función y devuelve estado_pedido.
		return estado_pedido;
	}

    /** [Doc]
     * Asigna estado pedido con el valor recibido.
     * @param estado_pedido estado pedido.
     */
	public void setEstado_pedido(String estado_pedido) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.estado_pedido = estado_pedido;
		this.estado_pedido = estado_pedido;
	}

    /** [Doc]
     * Devuelve comprobante, según el cuerpo o la implementación del contrato.
     * @return Comprobante con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public Comprobante getComprobante() {
        // [Doc] Termina el método o la función y devuelve comprobante.
		return comprobante;
	}

    /** [Doc]
     * Asigna comprobante con el valor recibido.
     * @param comprobante comprobante de la compra.
     */
	public void setComprobante(Comprobante comprobante) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.comprobante = comprobante;
		this.comprobante = comprobante;
	}

    /** [Doc]
     * Devuelve notificaciones, según el cuerpo o la implementación del contrato.
     * @return List&lt;Notificacion&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public List<Notificacion> getNotificaciones() {
        // [Doc] Termina el método o la función y devuelve notificaciones.
		return notificaciones;
	}

    /** [Doc]
     * Asigna notificaciones con el valor recibido.
     * @param notificaciones notificaciones.
     */
	public void setNotificaciones(List<Notificacion> notificaciones) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.notificaciones = notificaciones;
		this.notificaciones = notificaciones;
	}

    /** [Doc]
     * Devuelve carrito, según el cuerpo o la implementación del contrato.
     * @return Carrito_de_compra con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public Carrito_de_compra getCarrito() {
        // [Doc] Termina el método o la función y devuelve carrito.
		return carrito;
	}

    /** [Doc]
     * Asigna carrito con el valor recibido.
     * @param carrito carrito persistente procesado.
     */
	public void setCarrito(Carrito_de_compra carrito) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.carrito = carrito;
		this.carrito = carrito;
	}

	
	
	




}

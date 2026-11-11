package com.pequelandia.pequelandia.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/*clase que representa a un carrito de compra*/
//tabla:carrito de compra(id_carrito,total,usuario)

/** [Doc]
 * Representa el carrito persistente y relaciona usuario, detalles, total y pedido.
 */
@Getter @Setter
@Entity
@Table (name="carrito_de_compra")
public class Carrito_de_compra {

    // [Doc] Atributo idCarrito (Long): identificador del carrito persistente.
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id_carrito")
    private Long idCarrito;
    // [Doc] Atributo total (BigDecimal): importe acumulado.
	private BigDecimal total;
    // [Doc] Atributo usuario (Usuario): cuenta de usuario utilizada por la operación.
	@ManyToOne
	@JoinColumn(name="id_usuario")
	private Usuario usuario;
	
	//nos permitira ver detalles de carritos
    // [Doc] Atributo detalles (List<Detalle_carrito>): líneas asociadas al carrito. Se inicializa con new ArrayList<>().
	@OneToMany(mappedBy = "carrito")
    private List<Detalle_carrito> detalles = new ArrayList<>();

    //nos permitira ver pedidos
    // [Doc] Atributo pedido (Pedido): pedido procesado en este punto.
    @OneToOne(mappedBy = "carrito")
    private Pedido pedido; // 1-1 con Pedido
	
    /** [Doc]
     * Construye Carrito_de_compra y conserva sus dependencias o valores iniciales.
     */
	public Carrito_de_compra() {
		// TODO Auto-generated constructor stub
	}

    /** [Doc]
     * Construye Carrito_de_compra y conserva sus dependencias o valores iniciales.
     * @param id_carrito id carrito.
     * @param total importe acumulado.
     * @param usuario cuenta de usuario utilizada por la operación.
     */
	public Carrito_de_compra(long id_carrito, BigDecimal total, Usuario usuario) {
        // [Doc] Ejecuta la llamada o modificación super(); Sus efectos dependen del método invocado y del objeto receptor.
		super();
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.idCarrito = id_carrito;
		this.idCarrito = id_carrito;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.total = total;
		this.total = total;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.usuario = usuario;
		this.usuario = usuario;
	}
	
    /** [Doc]
     * Agrega detalle en el ámbito de Carrito_de_compra.
     * @param d línea del carrito procesada.
     */
	public void addDetalle(Detalle_carrito d) {
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: d.setCarrito(this);
        d.setCarrito(this);
        // [Doc] Ejecuta la llamada o modificación detalles.add(d); Sus efectos dependen del método invocado y del objeto receptor.
        detalles.add(d);
    }
    /** [Doc]
     * Quita detalle en el ámbito de Carrito_de_compra.
     * @param d línea del carrito procesada.
     */
    public void removeDetalle(Detalle_carrito d) {
        // [Doc] Ejecuta la llamada o modificación detalles.remove(d); Sus efectos dependen del método invocado y del objeto receptor.
        detalles.remove(d);
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: d.setCarrito(null);
        d.setCarrito(null);
    }

   
    /** [Doc]
     * Devuelve id carrito, según el cuerpo o la implementación del contrato.
     * @return long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    // Acceso explícito para los servicios y Eclipse, incluso sin generación de Lombok.
    public Long getIdCarrito() {
        return idCarrito;
    }

    public void setIdCarrito(Long idCarrito) {
        this.idCarrito = idCarrito;
    }

	public long getId_carrito() {
        // [Doc] Termina el método o la función y devuelve idCarrito.
		return idCarrito;
	}

    /** [Doc]
     * Asigna id carrito con el valor recibido.
     * @param id_carrito id carrito.
     */
	public void setId_carrito(long id_carrito) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.idCarrito = id_carrito;
		this.idCarrito = id_carrito;
	}

    /** [Doc]
     * Devuelve importe acumulado, según el cuerpo o la implementación del contrato.
     * @return BigDecimal con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public BigDecimal getTotal() {
        // [Doc] Termina el método o la función y devuelve total.
		return total;
	}

    /** [Doc]
     * Asigna importe acumulado con el valor recibido.
     * @param total importe acumulado.
     */
	public void setTotal(BigDecimal total) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.total = total;
		this.total = total;
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
     * Devuelve líneas asociadas al carrito, según el cuerpo o la implementación del contrato.
     * @return List&lt;Detalle_carrito&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public List<Detalle_carrito> getDetalles() {
        // [Doc] Termina el método o la función y devuelve detalles.
		return detalles;
	}

    /** [Doc]
     * Asigna líneas asociadas al carrito con el valor recibido.
     * @param detalles líneas asociadas al carrito.
     */
	public void setDetalles(List<Detalle_carrito> detalles) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.detalles = detalles;
		this.detalles = detalles;
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

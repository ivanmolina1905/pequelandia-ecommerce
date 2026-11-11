package com.pequelandia.pequelandia.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/*clase que representa a un comprobante en la bs*/
//tabla detalle_carrito(id_detalle_carrito,producto,detalle carrito,cantidad)
/** [Doc]
 * Representa una línea con producto, cantidad y precio conservado en el carrito.
 */
@Getter @Setter
@Entity
@Table(name = "detalle_carrito") // ❗ NECESARIO PARA LOS QUERIES NATIVOS
public class Detalle_carrito {
    // [Doc] Atributo idDetalleCarrito (Long): id detalle carrito.
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id_detalle_carrito")
	private Long idDetalleCarrito;
    // [Doc] Atributo carrito (Carrito_de_compra): carrito persistente procesado.
	@ManyToOne
    @JoinColumn(name = "id_carrito")
    private Carrito_de_compra carrito; // FK → carrito_de_compra
    // [Doc] Atributo producto (Producto): producto procesado en este punto.
	@ManyToOne
	@JoinColumn(name = "id_producto")
	private Producto producto; // FK → producto
	
	
    // [Doc] Atributo cantidad (Integer): unidades de una línea de carrito.
	private Integer cantidad;
	
	 

 // Necesario para estadísticas y reportes
    // [Doc] Atributo precio (float): importe unitario usado en el cálculo.
    private float precio; 

    /** [Doc]
     * Devuelve unidades de una línea de carrito, según el cuerpo o la implementación del contrato.
     * @return Integer con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public Integer getCantidad() {
        // [Doc] Termina el método o la función y devuelve cantidad.
		return cantidad;
	}


    /** [Doc]
     * Asigna unidades de una línea de carrito con el valor recibido.
     * @param cantidad unidades de una línea de carrito.
     */
	public void setCantidad(int cantidad) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.cantidad = cantidad;
		this.cantidad = cantidad;
	}



    /** [Doc]
     * Devuelve id detalle carrito, según el cuerpo o la implementación del contrato.
     * @return long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public long getId_detalle_carrito() {
        // [Doc] Termina el método o la función y devuelve idDetalleCarrito.
		return idDetalleCarrito;
	}


    /** [Doc]
     * Asigna id detalle carrito con el valor recibido.
     * @param id_detalle_carrito id detalle carrito.
     */
	public void setId_detalle_carrito(long id_detalle_carrito) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.idDetalleCarrito = id_detalle_carrito;
		this.idDetalleCarrito = id_detalle_carrito;
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

    /** [Doc]
     * Devuelve producto, según el cuerpo o la implementación del contrato.
     * @return Producto con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public Producto getProducto() {
        // [Doc] Termina el método o la función y devuelve producto.
		return producto;
	}


    /** [Doc]
     * Asigna producto con el valor recibido.
     * @param producto producto procesado en este punto.
     */
	public void setProducto(Producto producto) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.producto = producto;
		this.producto = producto;
	}

    /** [Doc]
     * Construye Detalle_carrito y conserva sus dependencias o valores iniciales.
     */
	public Detalle_carrito() {
		// TODO Auto-generated constructor stub
	}


    /** [Doc]
     * Asigna importe unitario usado en el cálculo con el valor recibido.
     * @param precio2 precio2.
     */
	public void setPrecio(float precio2) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.precio=precio2;
		this.precio=precio2;
		
	}
    /** [Doc]
     * Devuelve importe unitario usado en el cálculo, según el cuerpo o la implementación del contrato.
     * @return float con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public float getPrecio() {
        // [Doc] Termina el método o la función y devuelve precio.
		return precio;
	}

}

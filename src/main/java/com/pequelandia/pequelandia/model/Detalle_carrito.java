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
@Getter @Setter
@Entity
@Table(name = "detalle_carrito") // ❗ NECESARIO PARA LOS QUERIES NATIVOS
public class Detalle_carrito {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id_detalle_carrito")
	private Long idDetalleCarrito;
	@ManyToOne
    @JoinColumn(name = "id_carrito")
    private Carrito_de_compra carrito; // FK → carrito_de_compra
	@ManyToOne
	@JoinColumn(name = "id_producto")
	private Producto producto; // FK → producto
	
	
	private Integer cantidad;
	
	 

 // Necesario para estadísticas y reportes
    private float precio; 

	public Integer getCantidad() {
		return cantidad;
	}


	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}



	public long getId_detalle_carrito() {
		return idDetalleCarrito;
	}


	public void setId_detalle_carrito(long id_detalle_carrito) {
		this.idDetalleCarrito = id_detalle_carrito;
	}

	public Carrito_de_compra getCarrito() {
		return carrito;
	}


	public void setCarrito(Carrito_de_compra carrito) {
		this.carrito = carrito;
	}

	public Producto getProducto() {
		return producto;
	}


	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public Detalle_carrito() {
		// TODO Auto-generated constructor stub
	}


	public void setPrecio(float precio2) {
		this.precio=precio2;
		
	}
	public float getPrecio() {
		return precio;
	}

}

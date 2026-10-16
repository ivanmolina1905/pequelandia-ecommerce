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
@Getter @Setter
@Entity
public class Pedido {

	@Id@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id_pedido;
	
	@ManyToOne
	@JoinColumn(name = "id_usuario")
	private Usuario usuario;
	
	
	private Date fecha_pedido;
	private String estado_pedido;
	
	@OneToOne(mappedBy = "pedido")
    private Comprobante comprobante; // 1-1

	@OneToMany(mappedBy = "pedido")
    private List<Notificacion> notificaciones = new ArrayList<>();
	
	@OneToOne
    @JoinColumn(name = "id_carrito", unique = true)
    private Carrito_de_compra carrito; // FK → carrito_de_compra (1-1)

	
	public Pedido(long id_pedido, Usuario usuario, Date fecha_pedido, String estado_pedido, Comprobante comprobante,
			List<Notificacion> notificaciones, Carrito_de_compra carrito) {
		super();
		this.id_pedido = id_pedido;
		this.usuario = usuario;
		this.fecha_pedido = fecha_pedido;
		this.estado_pedido = estado_pedido;
		this.comprobante = comprobante;
		this.notificaciones = notificaciones;
		this.carrito = carrito;
	}
	
	public Pedido() {
		
	}

	public long getId_pedido() {
		return id_pedido;
	}

	public void setId_pedido(long id_pedido) {
		this.id_pedido = id_pedido;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Date getFecha_pedido() {
		return fecha_pedido;
	}

	public void setFecha_pedido(Date fecha_pedido) {
		this.fecha_pedido = fecha_pedido;
	}

	public String getEstado_pedido() {
		return estado_pedido;
	}

	public void setEstado_pedido(String estado_pedido) {
		this.estado_pedido = estado_pedido;
	}

	public Comprobante getComprobante() {
		return comprobante;
	}

	public void setComprobante(Comprobante comprobante) {
		this.comprobante = comprobante;
	}

	public List<Notificacion> getNotificaciones() {
		return notificaciones;
	}

	public void setNotificaciones(List<Notificacion> notificaciones) {
		this.notificaciones = notificaciones;
	}

	public Carrito_de_compra getCarrito() {
		return carrito;
	}

	public void setCarrito(Carrito_de_compra carrito) {
		this.carrito = carrito;
	}

	
	
	




}

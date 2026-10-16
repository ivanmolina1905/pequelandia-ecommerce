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
@Getter @Setter
@Entity
public class Notificacion {

	@Id@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id_notificacion;
	private LocalDateTime fecha_notificacion;
	private String destinatario;
	private String descripcion;
	
	@ManyToOne
	@JoinColumn(name="id_pedido")
	private Pedido pedido;
	
	
	public Notificacion(long id_notificacion, LocalDateTime fecha_notificacion, String destinatario, String descripcion,
			Pedido pedido) {
		super();
		this.id_notificacion = id_notificacion;
		this.fecha_notificacion = fecha_notificacion;
		this.destinatario = destinatario;
		this.descripcion = descripcion;
		this.pedido =pedido;
	}


	public long getId_notificacion() {
		return id_notificacion;
	}


	public void setId_notificacion(long id_notificacion) {
		this.id_notificacion = id_notificacion;
	}


	public LocalDateTime getFecha_notificacion() {
		return fecha_notificacion;
	}


	public void setFecha_notificacion(LocalDateTime localDateTime) {
		this.fecha_notificacion = localDateTime;
	}


	public String getDestinatario() {
		return destinatario;
	}


	public void setDestinatario(String destinatario) {
		this.destinatario = destinatario;
	}


	public String getDescripcion() {
		return descripcion;
	}


	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}


	public Pedido getPedido() {
		return pedido;
	}


	public void setPedido(Pedido pedido) {
		this.pedido = pedido;
	}


	public Notificacion() {
		// TODO Auto-generated constructor stub
	}

}

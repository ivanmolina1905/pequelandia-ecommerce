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
@Getter @Setter
@Entity
public class Comprobante {
	
	@Id@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id_comprobante;
	private String descripcion;
	private LocalDateTime fecha_venta;
	private BigDecimal total_venta;
	private String tipo_comprobante;
	private int n_comprobante;
	@OneToOne
    @JoinColumn(name = "id_pedido")
	private Pedido pedido;
	
	
	public Comprobante(long id_comprobante, String descripcion, LocalDateTime fecha_venta, BigDecimal total_venta,
			String tipo_comprobante, int n_comrobante, Pedido pedido) {
		super();
		this.id_comprobante = id_comprobante;
		this.descripcion = descripcion;
		this.fecha_venta =  fecha_venta;
		this.total_venta = total_venta;
		this.tipo_comprobante = tipo_comprobante;
		this.n_comprobante = n_comrobante;
		this.pedido = pedido;
	}
	
	public Comprobante() {
		
	}
	
	public long getId_comprobante() {
		return id_comprobante;
	}
	public void setId_comprobante(long id_comprobante) {
		this.id_comprobante = id_comprobante;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public LocalDateTime getFecha_venta() {
		return  fecha_venta;
	}
	public void setFecha_venta(LocalDateTime localDate) {
		this.fecha_venta = localDate;
	}
	public BigDecimal getTotal_venta() {
		return total_venta;
	}
	public void setTotal_venta(BigDecimal total_venta) {
		this.total_venta = total_venta;
	}
	public String getTipo_comprobante() {
		return tipo_comprobante;
	}
	public void setTipo_comprobante(String tipo_comprobante) {
		this.tipo_comprobante = tipo_comprobante;
	}
	public int getN_comprobante() {
		return n_comprobante;
	}
	public void setN_comprobante(int n_comprobante) {
		this.n_comprobante = n_comprobante;
	}
	public Pedido getPedido() {
		return pedido;
	}
	public void setPedido(Pedido pedido) {
		this.pedido = pedido;
	}
	
	
	

}

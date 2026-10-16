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

@Getter @Setter
@Entity
@Table (name="carrito_de_compra")
public class Carrito_de_compra {

	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id_carrito")
    private Long idCarrito;
	private BigDecimal total;
	@ManyToOne
	@JoinColumn(name="id_usuario")
	private Usuario usuario;
	
	//nos permitira ver detalles de carritos
	@OneToMany(mappedBy = "carrito")
    private List<Detalle_carrito> detalles = new ArrayList<>();

    //nos permitira ver pedidos
    @OneToOne(mappedBy = "carrito")
    private Pedido pedido; // 1-1 con Pedido
	
	public Carrito_de_compra() {
		// TODO Auto-generated constructor stub
	}

	public Carrito_de_compra(long id_carrito, BigDecimal total, Usuario usuario) {
		super();
		this.idCarrito = id_carrito;
		this.total = total;
		this.usuario = usuario;
	}
	
	public void addDetalle(Detalle_carrito d) {
        d.setCarrito(this);
        detalles.add(d);
    }
    public void removeDetalle(Detalle_carrito d) {
        detalles.remove(d);
        d.setCarrito(null);
    }

   
	public long getId_carrito() {
		return idCarrito;
	}

	public void setId_carrito(long id_carrito) {
		this.idCarrito = id_carrito;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public List<Detalle_carrito> getDetalles() {
		return detalles;
	}

	public void setDetalles(List<Detalle_carrito> detalles) {
		this.detalles = detalles;
	}

	public Pedido getPedido() {
		return pedido;
	}

	public void setPedido(Pedido pedido) {
		this.pedido = pedido;
	}

	
}

package com.pequelandia.pequelandia.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/*clase que representa producto en la bs*/
//tabla: producto(id_producto,nombre_producto,descripcion_producto,stock,costo,precio,fecha_alta,fecha_baja,estado,id_categoria,marca_producto,modelo_producto,talle_producto)


@Getter @Setter
@Entity
@Table(name="producto")
public class Producto {
	
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id_producto")
	private long idProducto;
	private String nombre_producto;
	private String descripcion_producto;
	private int stock;
	private float costo;
	private float precio;
	private Date fecha_alta;
	private Date fecha_baja;
	private String estado;
	@ManyToMany
	@JoinTable(
	    name = "producto_categoria",
	    joinColumns = @JoinColumn(name = "id_producto"),
	    inverseJoinColumns = @JoinColumn(name = "id_categoria")
	)
	private List<Categoria> categorias;
	private String marca_producto;
	private String modelo_producto;
	private String talle_producto;
	
	  // Usado por la vista de catálogo 
    private String imagenUrl;
	
	@OneToMany(mappedBy="producto")
	private List<Detalle_carrito> detalles=new ArrayList<>();
	

    // --- Conveniencia  ---
    public void addCategoria(Categoria c) {
        if (c != null && !categorias.contains(c)) categorias.add(c);
    }
    public void removeCategoria(Categoria c) {
        categorias.remove(c);
    }
    
	


	public long getIdProducto() {
		return idProducto;
	}
	public void setIdProducto(long idProducto) {
		this.idProducto = idProducto;
	}
	public String getNombre_producto() {
		return nombre_producto;
	}


	public void setNombre_producto(String nombre_producto) {
		this.nombre_producto = nombre_producto;
	}


	public String getDescripcion_producto() {
		return descripcion_producto;
	}


	public void setDescripcion_producto(String descripcion_producto) {
		this.descripcion_producto = descripcion_producto;
	}


	public int getStock() {
		return stock;
	}


	public void setStock(int stock) {
		this.stock = stock;
	}


	public float getCosto() {
		return costo;
	}


	public void setCosto(float costo) {
		this.costo = costo;
	}


	public float getPrecio() {
		return precio;
	}


	public void setPrecio(float precio) {
		this.precio = precio;
	}


	public Date getFecha_alta() {
		return fecha_alta;
	}


	public void setFecha_alta(Date fecha_alta) {
		this.fecha_alta = fecha_alta;
	}


	public Date getFecha_baja() {
		return fecha_baja;
	}


	public void setFecha_baja(Date fecha_baja) {
		this.fecha_baja = fecha_baja;
	}


	public String getEstado() {
		return estado;
	}


	public void setEstado(String estado) {
		this.estado = estado;
	}


	

	public List<Detalle_carrito> getDetalles() {
		return detalles;
	}


	public void setDetalles(List<Detalle_carrito> detalles) {
		this.detalles = detalles;
	}


	public String getMarca_producto() {
		return marca_producto;
	}


	public void setMarca_producto(String marca_producto) {
		this.marca_producto = marca_producto;
	}


	public String getModelo_producto() {
		return modelo_producto;
	}


	public void setModelo_producto(String modelo_producto) {
		this.modelo_producto = modelo_producto;
	}


	public String getTalle_producto() {
		return talle_producto;
	}


	public void setTalle_producto(String talle_producto) {
		this.talle_producto = talle_producto;
	}


	public Producto(long id_producto, String nombre_producto, String descripcion_producto, int stock, float costo,
            float precio, Date fecha_alta, Date fecha_baja, String estado, 
            List<Categoria> categorias, String marca_producto,
            String modelo_producto, String talle_producto) {
this.idProducto = id_producto;
this.nombre_producto = nombre_producto;
this.descripcion_producto = descripcion_producto;
this.stock = stock;
this.costo = costo;
this.precio = precio;
this.fecha_alta = fecha_alta;
this.fecha_baja = fecha_baja;
this.estado = estado;
this.categorias = categorias;
this.marca_producto = marca_producto;
this.modelo_producto = modelo_producto;
this.talle_producto = talle_producto;
}



	public List<Categoria> getCategorias() {
		return categorias;
	}
	public void setCategorias(List<Categoria> categorias) {
		this.categorias = categorias;
	}
	public String getImagenUrl() {
		return imagenUrl;
	}
	public void setImagenUrl(String imagenUrl) {
		this.imagenUrl = imagenUrl;
	}
	public Producto() {
		// TODO Auto-generated constructor stub
	}

}

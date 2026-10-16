package com.pequelandia.pequelandia.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/* clase que representa a categorias en la base de datos*/
//tabla:categoria (id_categoria,nombre_categoria,descripcion_categoria)

@Getter @Setter
@Entity
@Table (name="categoria")
public class Categoria {
	
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id_categoria;
	 @Column(name = "nombre_categoria")
	    private String nombreCategoria;
	private String descripcion_categoria;
	
	@ManyToMany(mappedBy = "categorias")
	private List<Producto> productos = new ArrayList<>();
	
	public Categoria() {
		// TODO Auto-generated constructor stub
	}

	public long getId_categoria() {
		return id_categoria;
	}

	public void setId_categoria(long id_categoria) {
		this.id_categoria = id_categoria;
	}

	public String getNombre_categoria() {
		return nombreCategoria;
	}

	public void setNombre_categoria(String nombre_categoria) {
		this.nombreCategoria = nombre_categoria;
	}

	public String getDescripcion_categoria() {
		return descripcion_categoria;
	}

	public void setDescripcion_categoria(String descripcion_categoria) {
		this.descripcion_categoria = descripcion_categoria;
	}

	public List<Producto> getProductos() {
		return productos;
	}

	public void setProductos(List<Producto> productos) {
		this.productos = productos;
	}

	public Categoria(long id_categoria, String nombre_categoria, String descripcion_categoria) {
		super();
		this.id_categoria = id_categoria;
		this.nombreCategoria = nombre_categoria;
		this.descripcion_categoria = descripcion_categoria;
	}
	

}

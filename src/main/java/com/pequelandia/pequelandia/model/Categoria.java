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

/** [Doc]
 * Representa una categoría de clasificación y su relación con productos.
 */
@Getter @Setter
@Entity
@Table (name="categoria")
public class Categoria {
	
    // [Doc] Atributo id_categoria (long): id categoria.
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id_categoria;
     // [Doc] Atributo nombreCategoria (String): nombre categoria.
	 @Column(name = "nombre_categoria")
	    private String nombreCategoria;
    // [Doc] Atributo descripcion_categoria (String): descripcion categoria.
	private String descripcion_categoria;
	
    // [Doc] Atributo productos (List<Producto>): productos. Se inicializa con new ArrayList<>().
	@ManyToMany(mappedBy = "categorias")
	private List<Producto> productos = new ArrayList<>();
	
    /** [Doc]
     * Construye Categoria y conserva sus dependencias o valores iniciales.
     */
	public Categoria() {
		// TODO Auto-generated constructor stub
	}

    /** [Doc]
     * Devuelve id categoria, según el cuerpo o la implementación del contrato.
     * @return long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public long getId_categoria() {
        // [Doc] Termina el método o la función y devuelve id_categoria.
		return id_categoria;
	}

    /** [Doc]
     * Asigna id categoria con el valor recibido.
     * @param id_categoria id categoria.
     */
	public void setId_categoria(long id_categoria) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.id_categoria = id_categoria;
		this.id_categoria = id_categoria;
	}

    /** [Doc]
     * Devuelve nombre categoria, según el cuerpo o la implementación del contrato.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public String getNombre_categoria() {
        // [Doc] Termina el método o la función y devuelve nombreCategoria.
		return nombreCategoria;
	}

    /** [Doc]
     * Asigna nombre categoria con el valor recibido.
     * @param nombre_categoria nombre categoria.
     */
	public void setNombre_categoria(String nombre_categoria) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.nombreCategoria = nombre_categoria;
		this.nombreCategoria = nombre_categoria;
	}

    /** [Doc]
     * Devuelve descripcion categoria, según el cuerpo o la implementación del contrato.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public String getDescripcion_categoria() {
        // [Doc] Termina el método o la función y devuelve descripcion_categoria.
		return descripcion_categoria;
	}

    /** [Doc]
     * Asigna descripcion categoria con el valor recibido.
     * @param descripcion_categoria descripcion categoria.
     */
	public void setDescripcion_categoria(String descripcion_categoria) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.descripcion_categoria = descripcion_categoria;
		this.descripcion_categoria = descripcion_categoria;
	}

    /** [Doc]
     * Devuelve productos, según el cuerpo o la implementación del contrato.
     * @return List&lt;Producto&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public List<Producto> getProductos() {
        // [Doc] Termina el método o la función y devuelve productos.
		return productos;
	}

    /** [Doc]
     * Asigna productos con el valor recibido.
     * @param productos productos.
     */
	public void setProductos(List<Producto> productos) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.productos = productos;
		this.productos = productos;
	}

    /** [Doc]
     * Construye Categoria y conserva sus dependencias o valores iniciales.
     * @param id_categoria id categoria.
     * @param nombre_categoria nombre categoria.
     * @param descripcion_categoria descripcion categoria.
     */
	public Categoria(long id_categoria, String nombre_categoria, String descripcion_categoria) {
        // [Doc] Ejecuta la llamada o modificación super(); Sus efectos dependen del método invocado y del objeto receptor.
		super();
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.id_categoria = id_categoria;
		this.id_categoria = id_categoria;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.nombreCategoria = nombre_categoria;
		this.nombreCategoria = nombre_categoria;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.descripcion_categoria = descripcion_categoria;
		this.descripcion_categoria = descripcion_categoria;
	}
	

}

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


/** [Doc]
 * Representa el artículo comercial, sus atributos, categorías, stock y precios.
 */
@Getter @Setter
@Entity
@Table(name="producto")
public class Producto {
	
    // [Doc] Atributo idProducto (long): identificador del producto.
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id_producto")
	private long idProducto;
    // [Doc] Atributo nombre_producto (String): nombre visible del producto.
	private String nombre_producto;
    // [Doc] Atributo descripcion_producto (String): descripcion producto.
	private String descripcion_producto;
    // [Doc] Atributo stock (int): unidades disponibles del producto.
	private int stock;
    // [Doc] Atributo costo (float): costo del producto.
	private float costo;
    // [Doc] Atributo precio (float): importe unitario usado en el cálculo.
	private float precio;
    // [Doc] Atributo fecha_alta (Date): fecha de alta del producto.
	private Date fecha_alta;
    // [Doc] Atributo fecha_baja (Date): fecha de baja del producto.
	private Date fecha_baja;
    // [Doc] Atributo estado (String): estado usado por la operación.
	private String estado;
    // [Doc] Atributo categorias (List<Categoria>): categorías asociadas al producto.
	@ManyToMany
	@JoinTable(
	    name = "producto_categoria",
	    joinColumns = @JoinColumn(name = "id_producto"),
	    inverseJoinColumns = @JoinColumn(name = "id_categoria")
	)
	private List<Categoria> categorias;
    // [Doc] Atributo marca_producto (String): marca producto.
	private String marca_producto;
    // [Doc] Atributo modelo_producto (String): modelo producto.
	private String modelo_producto;
    // [Doc] Atributo talle_producto (String): talle producto.
	private String talle_producto;
	
	  // Usado por la vista de catálogo 
    // [Doc] Atributo imagenUrl (String): ubicación de la imagen mostrada en el catálogo.
    private String imagenUrl;
	
    // [Doc] Atributo detalles (List<Detalle_carrito>): líneas asociadas al carrito. Se inicializa con new ArrayList<>().
	@OneToMany(mappedBy="producto")
	private List<Detalle_carrito> detalles=new ArrayList<>();
	

    // --- Conveniencia  ---
    /** [Doc]
     * Agrega categoria en el ámbito de Producto.
     * @param c categoría del producto.
     */
    public void addCategoria(Categoria c) {
        // [Doc] Evalúa c != null && !categorias.contains(c). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (c != null && !categorias.contains(c)) categorias.add(c);
    }
    /** [Doc]
     * Quita categoria en el ámbito de Producto.
     * @param c categoría del producto.
     */
    public void removeCategoria(Categoria c) {
        // [Doc] Ejecuta la llamada o modificación categorias.remove(c); Sus efectos dependen del método invocado y del objeto receptor.
        categorias.remove(c);
    }
    
	


    /** [Doc]
     * Devuelve identificador del producto, según el cuerpo o la implementación del contrato.
     * @return long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public long getIdProducto() {
        // [Doc] Termina el método o la función y devuelve idProducto.
		return idProducto;
	}
    /** [Doc]
     * Asigna identificador del producto con el valor recibido.
     * @param idProducto identificador del producto.
     */
	public void setIdProducto(long idProducto) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.idProducto = idProducto;
		this.idProducto = idProducto;
	}
    /** [Doc]
     * Devuelve nombre visible del producto, según el cuerpo o la implementación del contrato.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public String getNombre_producto() {
        // [Doc] Termina el método o la función y devuelve nombre_producto.
		return nombre_producto;
	}


    /** [Doc]
     * Asigna nombre visible del producto con el valor recibido.
     * @param nombre_producto nombre visible del producto.
     */
	public void setNombre_producto(String nombre_producto) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.nombre_producto = nombre_producto;
		this.nombre_producto = nombre_producto;
	}


    /** [Doc]
     * Devuelve descripcion producto, según el cuerpo o la implementación del contrato.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public String getDescripcion_producto() {
        // [Doc] Termina el método o la función y devuelve descripcion_producto.
		return descripcion_producto;
	}


    /** [Doc]
     * Asigna descripcion producto con el valor recibido.
     * @param descripcion_producto descripcion producto.
     */
	public void setDescripcion_producto(String descripcion_producto) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.descripcion_producto = descripcion_producto;
		this.descripcion_producto = descripcion_producto;
	}


    /** [Doc]
     * Devuelve unidades disponibles del producto, según el cuerpo o la implementación del contrato.
     * @return int con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public int getStock() {
        // [Doc] Termina el método o la función y devuelve stock.
		return stock;
	}


    /** [Doc]
     * Asigna unidades disponibles del producto con el valor recibido.
     * @param stock unidades disponibles del producto.
     */
	public void setStock(int stock) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.stock = stock;
		this.stock = stock;
	}


    /** [Doc]
     * Devuelve costo del producto, según el cuerpo o la implementación del contrato.
     * @return float con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public float getCosto() {
        // [Doc] Termina el método o la función y devuelve costo.
		return costo;
	}


    /** [Doc]
     * Asigna costo del producto con el valor recibido.
     * @param costo costo del producto.
     */
	public void setCosto(float costo) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.costo = costo;
		this.costo = costo;
	}


    /** [Doc]
     * Devuelve importe unitario usado en el cálculo, según el cuerpo o la implementación del contrato.
     * @return float con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public float getPrecio() {
        // [Doc] Termina el método o la función y devuelve precio.
		return precio;
	}


    /** [Doc]
     * Asigna importe unitario usado en el cálculo con el valor recibido.
     * @param precio importe unitario usado en el cálculo.
     */
	public void setPrecio(float precio) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.precio = precio;
		this.precio = precio;
	}


    /** [Doc]
     * Devuelve fecha de alta del producto, según el cuerpo o la implementación del contrato.
     * @return Date con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public Date getFecha_alta() {
        // [Doc] Termina el método o la función y devuelve fecha_alta.
		return fecha_alta;
	}


    /** [Doc]
     * Asigna fecha de alta del producto con el valor recibido.
     * @param fecha_alta fecha de alta del producto.
     */
	public void setFecha_alta(Date fecha_alta) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.fecha_alta = fecha_alta;
		this.fecha_alta = fecha_alta;
	}


    /** [Doc]
     * Devuelve fecha de baja del producto, según el cuerpo o la implementación del contrato.
     * @return Date con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public Date getFecha_baja() {
        // [Doc] Termina el método o la función y devuelve fecha_baja.
		return fecha_baja;
	}


    /** [Doc]
     * Asigna fecha de baja del producto con el valor recibido.
     * @param fecha_baja fecha de baja del producto.
     */
	public void setFecha_baja(Date fecha_baja) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.fecha_baja = fecha_baja;
		this.fecha_baja = fecha_baja;
	}


    /** [Doc]
     * Devuelve estado usado por la operación, según el cuerpo o la implementación del contrato.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public String getEstado() {
        // [Doc] Termina el método o la función y devuelve estado.
		return estado;
	}


    /** [Doc]
     * Asigna estado usado por la operación con el valor recibido.
     * @param estado estado usado por la operación.
     */
	public void setEstado(String estado) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.estado = estado;
		this.estado = estado;
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
     * Devuelve marca producto, según el cuerpo o la implementación del contrato.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public String getMarca_producto() {
        // [Doc] Termina el método o la función y devuelve marca_producto.
		return marca_producto;
	}


    /** [Doc]
     * Asigna marca producto con el valor recibido.
     * @param marca_producto marca producto.
     */
	public void setMarca_producto(String marca_producto) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.marca_producto = marca_producto;
		this.marca_producto = marca_producto;
	}


    /** [Doc]
     * Devuelve modelo producto, según el cuerpo o la implementación del contrato.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public String getModelo_producto() {
        // [Doc] Termina el método o la función y devuelve modelo_producto.
		return modelo_producto;
	}


    /** [Doc]
     * Asigna modelo producto con el valor recibido.
     * @param modelo_producto modelo producto.
     */
	public void setModelo_producto(String modelo_producto) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.modelo_producto = modelo_producto;
		this.modelo_producto = modelo_producto;
	}


    /** [Doc]
     * Devuelve talle producto, según el cuerpo o la implementación del contrato.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public String getTalle_producto() {
        // [Doc] Termina el método o la función y devuelve talle_producto.
		return talle_producto;
	}


    /** [Doc]
     * Asigna talle producto con el valor recibido.
     * @param talle_producto talle producto.
     */
	public void setTalle_producto(String talle_producto) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.talle_producto = talle_producto;
		this.talle_producto = talle_producto;
	}


    /** [Doc]
     * Construye Producto y conserva sus dependencias o valores iniciales.
     * @param id_producto id producto.
     * @param nombre_producto nombre visible del producto.
     * @param descripcion_producto descripcion producto.
     * @param stock unidades disponibles del producto.
     * @param costo costo del producto.
     * @param precio importe unitario usado en el cálculo.
     * @param fecha_alta fecha de alta del producto.
     * @param fecha_baja fecha de baja del producto.
     * @param estado estado usado por la operación.
     * @param categorias categorías asociadas al producto.
     * @param marca_producto marca producto.
     * @param modelo_producto modelo producto.
     * @param talle_producto talle producto.
     */
	public Producto(long id_producto, String nombre_producto, String descripcion_producto, int stock, float costo,
            float precio, Date fecha_alta, Date fecha_baja, String estado, 
            List<Categoria> categorias, String marca_producto,
            String modelo_producto, String talle_producto) {
// [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.idProducto = id_producto;
this.idProducto = id_producto;
// [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.nombre_producto = nombre_producto;
this.nombre_producto = nombre_producto;
// [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.descripcion_producto = descripcion_producto;
this.descripcion_producto = descripcion_producto;
// [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.stock = stock;
this.stock = stock;
// [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.costo = costo;
this.costo = costo;
// [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.precio = precio;
this.precio = precio;
// [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.fecha_alta = fecha_alta;
this.fecha_alta = fecha_alta;
// [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.fecha_baja = fecha_baja;
this.fecha_baja = fecha_baja;
// [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.estado = estado;
this.estado = estado;
// [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.categorias = categorias;
this.categorias = categorias;
// [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.marca_producto = marca_producto;
this.marca_producto = marca_producto;
// [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.modelo_producto = modelo_producto;
this.modelo_producto = modelo_producto;
// [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.talle_producto = talle_producto;
this.talle_producto = talle_producto;
}



    /** [Doc]
     * Devuelve categorías asociadas al producto, según el cuerpo o la implementación del contrato.
     * @return List&lt;Categoria&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public List<Categoria> getCategorias() {
        // [Doc] Termina el método o la función y devuelve categorias.
		return categorias;
	}
    /** [Doc]
     * Asigna categorías asociadas al producto con el valor recibido.
     * @param categorias categorías asociadas al producto.
     */
	public void setCategorias(List<Categoria> categorias) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.categorias = categorias;
		this.categorias = categorias;
	}
    /** [Doc]
     * Devuelve ubicación de la imagen mostrada en el catálogo, según el cuerpo o la implementación del contrato.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public String getImagenUrl() {
        // [Doc] Termina el método o la función y devuelve imagenUrl.
		return imagenUrl;
	}
    /** [Doc]
     * Asigna ubicación de la imagen mostrada en el catálogo con el valor recibido.
     * @param imagenUrl ubicación de la imagen mostrada en el catálogo.
     */
	public void setImagenUrl(String imagenUrl) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.imagenUrl = imagenUrl;
		this.imagenUrl = imagenUrl;
	}
    /** [Doc]
     * Construye Producto y conserva sus dependencias o valores iniciales.
     */
	public Producto() {
		// TODO Auto-generated constructor stub
	}

}

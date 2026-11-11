package com.pequelandia.pequelandia.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/*Clase encargada de representar una localidad*/
//Tabla: localidad (id_localidad, nombre_localidad, codigo_postal)


/** [Doc]
 * Representa una localidad y su código postal para asociarlos a una cuenta.
 */
@Getter @Setter
@Entity
@Table (name="localidad")
public class Localidad {

    // [Doc] Atributo id_localidad (long): id localidad.
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)/*"identity" para que siga el orden de la bd*/
	private  long id_localidad;
    // [Doc] Atributo nombreLocalidad (String): nombre localidad.
	@Column(name = "nombre_localidad")
	private String nombreLocalidad;
    // [Doc] Atributo codigoPostal (int): codigo postal.
	@Column (name= "codigo_postal")
	private int codigoPostal;

    /** [Doc]
     * Construye Localidad y conserva sus dependencias o valores iniciales.
     */
	public Localidad() {
		// TODO Auto-generated constructor stub
	}

    /** [Doc]
     * Construye Localidad y conserva sus dependencias o valores iniciales.
     * @param id_localidad id localidad.
     * @param nombre_localidad nombre localidad.
     * @param codigo_postal codigo postal.
     */
	public Localidad(long id_localidad, String nombre_localidad, int codigo_postal) {
        // [Doc] Ejecuta la llamada o modificación super(); Sus efectos dependen del método invocado y del objeto receptor.
		super();
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.id_localidad = id_localidad;
		this.id_localidad = id_localidad;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.nombreLocalidad = nombre_localidad;
		this.nombreLocalidad = nombre_localidad;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.codigoPostal = codigo_postal;
		this.codigoPostal = codigo_postal;
	}

    /** [Doc]
     * Devuelve id localidad, según el cuerpo o la implementación del contrato.
     * @return long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public long getId_localidad() {
        // [Doc] Termina el método o la función y devuelve id_localidad.
		return id_localidad;
	}

    /** [Doc]
     * Asigna id localidad con el valor recibido.
     * @param id_localidad id localidad.
     */
	public void setId_localidad(long id_localidad) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.id_localidad = id_localidad;
		this.id_localidad = id_localidad;
	}

    /** [Doc]
     * Devuelve nombre localidad, según el cuerpo o la implementación del contrato.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public String getNombre_localidad() {
        // [Doc] Termina el método o la función y devuelve nombreLocalidad.
		return nombreLocalidad;
	}

    /** [Doc]
     * Asigna nombre localidad con el valor recibido.
     * @param nombre_localidad nombre localidad.
     */
	public void setNombre_localidad(String nombre_localidad) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.nombreLocalidad = nombre_localidad;
		this.nombreLocalidad = nombre_localidad;
	}

    /** [Doc]
     * Devuelve codigo postal, según el cuerpo o la implementación del contrato.
     * @return int con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public int getCodigo_postal() {
        // [Doc] Termina el método o la función y devuelve codigoPostal.
		return codigoPostal;
	}

    /** [Doc]
     * Asigna codigo postal con el valor recibido.
     * @param codigo_postal codigo postal.
     */
	public void setCodigo_postal(int codigo_postal) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.codigoPostal = codigo_postal;
		this.codigoPostal = codigo_postal;
	}
	
}

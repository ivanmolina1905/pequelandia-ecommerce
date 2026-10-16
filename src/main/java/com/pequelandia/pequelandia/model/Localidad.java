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


@Getter @Setter
@Entity
@Table (name="localidad")
public class Localidad {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)/*"identity" para que siga el orden de la bd*/
	private  long id_localidad;
	@Column(name = "nombre_localidad")
	private String nombreLocalidad;
	@Column (name= "codigo_postal")
	private int codigoPostal;

	public Localidad() {
		// TODO Auto-generated constructor stub
	}

	public Localidad(long id_localidad, String nombre_localidad, int codigo_postal) {
		super();
		this.id_localidad = id_localidad;
		this.nombreLocalidad = nombre_localidad;
		this.codigoPostal = codigo_postal;
	}

	public long getId_localidad() {
		return id_localidad;
	}

	public void setId_localidad(long id_localidad) {
		this.id_localidad = id_localidad;
	}

	public String getNombre_localidad() {
		return nombreLocalidad;
	}

	public void setNombre_localidad(String nombre_localidad) {
		this.nombreLocalidad = nombre_localidad;
	}

	public int getCodigo_postal() {
		return codigoPostal;
	}

	public void setCodigo_postal(int codigo_postal) {
		this.codigoPostal = codigo_postal;
	}
	
}

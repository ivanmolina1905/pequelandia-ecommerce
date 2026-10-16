package com.pequelandia.pequelandia.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/*clase que representa a la tabla usuario en la bs*/
//tabla: usuario(id_usuario,dni,nombre_usuario,apellido,direccion,fecha_registro,ultima_conexion,fecha_nacimiento,contraseña,telefono,email,id_localidad FK,puntos)

@Getter @Setter
@Entity
@Table (name="usuario")
public class Usuario {
	
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id_usuario")
	private Long id;
	private int dni;
	private String nombre_usuario;
	private String apellido;
	private String direccion;
	private Date fecha_registro;
	private Date ultima_conexion;
	private Date fecha_nacimiento;
	@com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
	private String contraseña;
	private String telefono;
	private String email;
	@ManyToOne
	@JoinColumn(name = "id_localidad")
	private Localidad localidad;
	@Column(nullable = false)
	private int puntos = 0;   // valor por defecto en nuevos objetos
	//esto nos permitira traer los carritos de un usuario
	@OneToMany(mappedBy = "usuario")
	private List<Carrito_de_compra> carritos=new ArrayList<>();	
	//esto nos permitira traer los pedidos de un usuario
	@OneToMany(mappedBy="usuario")
	private List<Pedido>pedidos=new ArrayList<>();
	
	public Usuario(long id_usuario, int dni, String nombre_usuario, String apellido, String direccion,
			Date fecha_registro, Date ultima_conexion, Date fecha_nacimiento, String contraseña, String telefono,
			String email, Localidad localidad, int puntos) {
		super();
		this.id = id_usuario;
		this.dni = dni;
		this.nombre_usuario = nombre_usuario;
		this.apellido = apellido;
		this.direccion = direccion;
		this.fecha_registro = fecha_registro;
		this.ultima_conexion = ultima_conexion;
		this.fecha_nacimiento = fecha_nacimiento;
		this.contraseña = contraseña;
		this.telefono = telefono;
		this.email = email;
		this.localidad = localidad;
		this.puntos = puntos;
	}


	public long getId() {
		return id;
	}


	public void setId(long id_usuario) {
		this.id = id_usuario;
	}


	public int getDni() {
		return dni;
	}


	public void setDni(int dni) {
		this.dni = dni;
	}


	public String getNombre_usuario() {
		return nombre_usuario;
	}


	public void setNombre_usuario(String nombre_usuario) {
		this.nombre_usuario = nombre_usuario;
	}


	public String getApellido() {
		return apellido;
	}


	public void setApellido(String apellido) {
		this.apellido = apellido;
	}


	public String getDireccion() {
		return direccion;
	}


	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}


	public Date getFecha_registro() {
		return fecha_registro;
	}


	public void setFecha_registro(Date fecha_registro) {
		this.fecha_registro = fecha_registro;
	}


	public Date getUltima_conexion() {
		return ultima_conexion;
	}


	public void setUltima_conexion(Date ultima_conexion) {
		this.ultima_conexion = ultima_conexion;
	}


	public Date getFecha_nacimiento() {
		return fecha_nacimiento;
	}


	public void setFecha_nacimiento(Date fecha_nacimiento) {
		this.fecha_nacimiento = fecha_nacimiento;
	}


	public String getContraseña() {
		return contraseña;
	}


	public void setContraseña(String contraseña) {
		this.contraseña = contraseña;
	}


	public String getTelefono() {
		return telefono;
	}


	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public Localidad getLocalidad() {
		return localidad;
	}


	public void setLocalidad(Localidad localidad) {
		this.localidad = localidad;
	}


	public List<Carrito_de_compra> getCarritos() {
		return carritos;
	}


	public void setCarritos(List<Carrito_de_compra> carritos) {
		this.carritos = carritos;
	}


	public List<Pedido> getPedidos() {
		return pedidos;
	}


	public void setPedidos(List<Pedido> pedidos) {
		this.pedidos = pedidos;
	}


	public int getPuntos() {
		return puntos;
	}


	public void setPuntos(int puntos) {
		this.puntos = puntos;
	}


	public Usuario() {
		// TODO Auto-generated constructor stub
	}

}


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

/** [Doc]
 * Representa la cuenta y sus datos personales, localidad, carritos, pedidos y puntos.
 */
@Getter @Setter
@Entity
@Table (name="usuario")
public class Usuario {
    public enum Rol { ADMINISTRADOR, USUARIO }
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(length = 20)
    private Rol rol;

	
    // [Doc] Atributo id (Long): identificador del registro.
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id_usuario")
	private Long id;
    // [Doc] Atributo dni (int): documento de identidad.
	private int dni;
    // [Doc] Atributo nombre_usuario (String): nombre usuario.
	private String nombre_usuario;
    // [Doc] Atributo apellido (String): apellido.
	private String apellido;
    // [Doc] Atributo direccion (String): direccion.
	private String direccion;
    // [Doc] Atributo fecha_registro (Date): fecha de creación de la cuenta.
	private Date fecha_registro;
    // [Doc] Atributo ultima_conexion (Date): fecha de última conexión almacenada.
	private Date ultima_conexion;
    // [Doc] Atributo fecha_nacimiento (Date): fecha de nacimiento de la persona.
	private Date fecha_nacimiento;
    // [Doc] Atributo contraseña (String): contraseña de la cuenta, actualmente conservada sin hash.
	@com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
	private String contraseña;
    // [Doc] Atributo telefono (String): telefono.
	private String telefono;
    // [Doc] Atributo email (String): dirección de correo de la cuenta.
	private String email;
    // [Doc] Atributo localidad (Localidad): localidad asociada a la cuenta.
	@ManyToOne
	@JoinColumn(name = "id_localidad")
	private Localidad localidad;
    // [Doc] Atributo puntos (int): puntos almacenados de la cuenta. Se inicializa con 0.
	@Column(nullable = false)
	private int puntos = 0;   // valor por defecto en nuevos objetos
	//esto nos permitira traer los carritos de un usuario
    // [Doc] Atributo carritos (List<Carrito_de_compra>): carritos. Se inicializa con new ArrayList<>().
	@OneToMany(mappedBy = "usuario")
	private List<Carrito_de_compra> carritos=new ArrayList<>();	
	//esto nos permitira traer los pedidos de un usuario
    // [Doc] Atributo pedidos (List<Pedido>): pedidos o servicio de pedidos según el tipo declarado. Se inicializa con new ArrayList<>().
	@OneToMany(mappedBy="usuario")
	private List<Pedido>pedidos=new ArrayList<>();
	
    /** [Doc]
     * Construye Usuario y conserva sus dependencias o valores iniciales.
     * @param id_usuario id usuario.
     * @param dni documento de identidad.
     * @param nombre_usuario nombre usuario.
     * @param apellido apellido.
     * @param direccion direccion.
     * @param fecha_registro fecha de creación de la cuenta.
     * @param ultima_conexion fecha de última conexión almacenada.
     * @param fecha_nacimiento fecha de nacimiento de la persona.
     * @param contraseña contraseña de la cuenta, actualmente conservada sin hash.
     * @param telefono telefono.
     * @param email dirección de correo de la cuenta.
     * @param localidad localidad asociada a la cuenta.
     * @param puntos puntos almacenados de la cuenta.
     */
	public Usuario(long id_usuario, int dni, String nombre_usuario, String apellido, String direccion,
			Date fecha_registro, Date ultima_conexion, Date fecha_nacimiento, String contraseña, String telefono,
			String email, Localidad localidad, int puntos) {
        // [Doc] Ejecuta la llamada o modificación super(); Sus efectos dependen del método invocado y del objeto receptor.
		super();
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.id = id_usuario;
		this.id = id_usuario;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.dni = dni;
		this.dni = dni;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.nombre_usuario = nombre_usuario;
		this.nombre_usuario = nombre_usuario;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.apellido = apellido;
		this.apellido = apellido;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.direccion = direccion;
		this.direccion = direccion;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.fecha_registro = fecha_registro;
		this.fecha_registro = fecha_registro;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.ultima_conexion = ultima_conexion;
		this.ultima_conexion = ultima_conexion;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.fecha_nacimiento = fecha_nacimiento;
		this.fecha_nacimiento = fecha_nacimiento;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.contraseña = contraseña;
		this.contraseña = contraseña;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.telefono = telefono;
		this.telefono = telefono;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.email = email;
		this.email = email;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.localidad = localidad;
		this.localidad = localidad;
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.puntos = puntos;
		this.puntos = puntos;
	}


    /** [Doc]
     * Devuelve identificador del registro, según el cuerpo o la implementación del contrato.
     * @return Long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public Long getId() {
        // [Doc] Termina el método o la función y devuelve id.
		return id;
	}


    /** [Doc]
     * Asigna identificador del registro con el valor recibido.
     * @param id_usuario id usuario.
     */
	public void setId(long id_usuario) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.id = id_usuario;
		this.id = id_usuario;
	}


    /** [Doc]
     * Devuelve documento de identidad, según el cuerpo o la implementación del contrato.
     * @return int con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public int getDni() {
        // [Doc] Termina el método o la función y devuelve dni.
		return dni;
	}


    /** [Doc]
     * Asigna documento de identidad con el valor recibido.
     * @param dni documento de identidad.
     */
	public void setDni(int dni) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.dni = dni;
		this.dni = dni;
	}


    /** [Doc]
     * Devuelve nombre usuario, según el cuerpo o la implementación del contrato.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public String getNombre_usuario() {
        // [Doc] Termina el método o la función y devuelve nombre_usuario.
		return nombre_usuario;
	}


    /** [Doc]
     * Asigna nombre usuario con el valor recibido.
     * @param nombre_usuario nombre usuario.
     */
	public void setNombre_usuario(String nombre_usuario) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.nombre_usuario = nombre_usuario;
		this.nombre_usuario = nombre_usuario;
	}


    /** [Doc]
     * Devuelve apellido, según el cuerpo o la implementación del contrato.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public String getApellido() {
        // [Doc] Termina el método o la función y devuelve apellido.
		return apellido;
	}


    /** [Doc]
     * Asigna apellido con el valor recibido.
     * @param apellido apellido.
     */
	public void setApellido(String apellido) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.apellido = apellido;
		this.apellido = apellido;
	}


    /** [Doc]
     * Devuelve direccion, según el cuerpo o la implementación del contrato.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public String getDireccion() {
        // [Doc] Termina el método o la función y devuelve direccion.
		return direccion;
	}


    /** [Doc]
     * Asigna direccion con el valor recibido.
     * @param direccion direccion.
     */
	public void setDireccion(String direccion) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.direccion = direccion;
		this.direccion = direccion;
	}


    /** [Doc]
     * Devuelve fecha de creación de la cuenta, según el cuerpo o la implementación del contrato.
     * @return Date con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public Date getFecha_registro() {
        // [Doc] Termina el método o la función y devuelve fecha_registro.
		return fecha_registro;
	}


    /** [Doc]
     * Asigna fecha de creación de la cuenta con el valor recibido.
     * @param fecha_registro fecha de creación de la cuenta.
     */
	public void setFecha_registro(Date fecha_registro) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.fecha_registro = fecha_registro;
		this.fecha_registro = fecha_registro;
	}


    /** [Doc]
     * Devuelve fecha de última conexión almacenada, según el cuerpo o la implementación del contrato.
     * @return Date con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public Date getUltima_conexion() {
        // [Doc] Termina el método o la función y devuelve ultima_conexion.
		return ultima_conexion;
	}


    /** [Doc]
     * Asigna fecha de última conexión almacenada con el valor recibido.
     * @param ultima_conexion fecha de última conexión almacenada.
     */
	public void setUltima_conexion(Date ultima_conexion) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.ultima_conexion = ultima_conexion;
		this.ultima_conexion = ultima_conexion;
	}


    /** [Doc]
     * Devuelve fecha de nacimiento de la persona, según el cuerpo o la implementación del contrato.
     * @return Date con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public Date getFecha_nacimiento() {
        // [Doc] Termina el método o la función y devuelve fecha_nacimiento.
		return fecha_nacimiento;
	}


    /** [Doc]
     * Asigna fecha de nacimiento de la persona con el valor recibido.
     * @param fecha_nacimiento fecha de nacimiento de la persona.
     */
	public void setFecha_nacimiento(Date fecha_nacimiento) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.fecha_nacimiento = fecha_nacimiento;
		this.fecha_nacimiento = fecha_nacimiento;
	}


    /** [Doc]
     * Devuelve contraseña de la cuenta, actualmente conservada sin hash, según el cuerpo o la implementación del
     * contrato.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public String getContraseña() {
        // [Doc] Termina el método o la función y devuelve contraseña.
		return contraseña;
	}


    /** [Doc]
     * Asigna contraseña de la cuenta, actualmente conservada sin hash con el valor recibido.
     * @param contraseña contraseña de la cuenta, actualmente conservada sin hash.
     */
	public void setContraseña(String contraseña) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.contraseña = contraseña;
		this.contraseña = contraseña;
	}


    /** [Doc]
     * Devuelve telefono, según el cuerpo o la implementación del contrato.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public String getTelefono() {
        // [Doc] Termina el método o la función y devuelve telefono.
		return telefono;
	}


    /** [Doc]
     * Asigna telefono con el valor recibido.
     * @param telefono telefono.
     */
	public void setTelefono(String telefono) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.telefono = telefono;
		this.telefono = telefono;
	}


    /** [Doc]
     * Devuelve dirección de correo de la cuenta, según el cuerpo o la implementación del contrato.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public String getEmail() {
        // [Doc] Termina el método o la función y devuelve email.
		return email;
	}


    /** [Doc]
     * Asigna dirección de correo de la cuenta con el valor recibido.
     * @param email dirección de correo de la cuenta.
     */
	public void setEmail(String email) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.email = email;
		this.email = email;
	}


    /** [Doc]
     * Devuelve localidad asociada a la cuenta, según el cuerpo o la implementación del contrato.
     * @return Localidad con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public Localidad getLocalidad() {
        // [Doc] Termina el método o la función y devuelve localidad.
		return localidad;
	}


    /** [Doc]
     * Asigna localidad asociada a la cuenta con el valor recibido.
     * @param localidad localidad asociada a la cuenta.
     */
	public void setLocalidad(Localidad localidad) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.localidad = localidad;
		this.localidad = localidad;
	}


    /** [Doc]
     * Devuelve carritos, según el cuerpo o la implementación del contrato.
     * @return List&lt;Carrito_de_compra&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public List<Carrito_de_compra> getCarritos() {
        // [Doc] Termina el método o la función y devuelve carritos.
		return carritos;
	}


    /** [Doc]
     * Asigna carritos con el valor recibido.
     * @param carritos carritos.
     */
	public void setCarritos(List<Carrito_de_compra> carritos) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.carritos = carritos;
		this.carritos = carritos;
	}


    /** [Doc]
     * Devuelve pedidos o servicio de pedidos según el tipo declarado, según el cuerpo o la implementación del
     * contrato.
     * @return List&lt;Pedido&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public List<Pedido> getPedidos() {
        // [Doc] Termina el método o la función y devuelve pedidos.
		return pedidos;
	}


    /** [Doc]
     * Asigna pedidos o servicio de pedidos según el tipo declarado con el valor recibido.
     * @param pedidos pedidos o servicio de pedidos según el tipo declarado.
     */
	public void setPedidos(List<Pedido> pedidos) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.pedidos = pedidos;
		this.pedidos = pedidos;
	}


    /** [Doc]
     * Devuelve puntos almacenados de la cuenta, según el cuerpo o la implementación del contrato.
     * @return int con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public int getPuntos() {
        // [Doc] Termina el método o la función y devuelve puntos.
		return puntos;
	}


    /** [Doc]
     * Asigna puntos almacenados de la cuenta con el valor recibido.
     * @param puntos puntos almacenados de la cuenta.
     */
	public void setPuntos(int puntos) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.puntos = puntos;
		this.puntos = puntos;
	}


    /** [Doc]
     * Construye Usuario y conserva sus dependencias o valores iniciales.
     */
	public Usuario() {
		// TODO Auto-generated constructor stub
	}

}


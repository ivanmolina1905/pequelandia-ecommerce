package com.pequelandia.pequelandia.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/** [Doc]
 * Transporta datos de registro usuario entre consultas, servicios, exportaciones y vistas sin representar una
 * entidad persistente.
 */
public class RegistroUsuarioDTO {

    // [Doc] Atributo nombre_usuario (String): nombre usuario.
    private String nombre_usuario;
    // [Doc] Atributo apellido (String): apellido.
    private String apellido;
    // [Doc] Atributo email (String): dirección de correo de la cuenta.
    private String email;
    // [Doc] Atributo dni (int): documento de identidad.
    private int dni;
    // [Doc] Atributo telefono (String): telefono.
    private String telefono;
    // [Doc] Atributo direccion (String): direccion.
    private String direccion;

    // [Doc] Atributo fecha_nacimiento (Date): fecha de nacimiento de la persona.
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date fecha_nacimiento;

    // [Doc] Atributo localidadId (Long): identificador de localidad elegido en el formulario.
    private Long localidadId;

    // [Doc] Atributo contraseña (String): contraseña de la cuenta, actualmente conservada sin hash.
    private String contraseña;
    // [Doc] Atributo confirmarContraseña (String): confirmación de contraseña del registro.
    private String confirmarContraseña;

    // [Doc] Atributo aceptarTerminos (boolean): aceptación de términos del registro.
    private boolean aceptarTerminos;

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
     * Devuelve identificador de localidad elegido en el formulario, según el cuerpo o la implementación del
     * contrato.
     * @return Long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public Long getLocalidadId() {
        // [Doc] Termina el método o la función y devuelve localidadId.
        return localidadId;
    }

    /** [Doc]
     * Asigna identificador de localidad elegido en el formulario con el valor recibido.
     * @param localidadId identificador de localidad elegido en el formulario.
     */
    public void setLocalidadId(Long localidadId) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.localidadId = localidadId;
        this.localidadId = localidadId;
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
     * Devuelve confirmación de contraseña del registro, según el cuerpo o la implementación del contrato.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public String getConfirmarContraseña() {
        // [Doc] Termina el método o la función y devuelve confirmarContraseña.
        return confirmarContraseña;
    }

    /** [Doc]
     * Asigna confirmación de contraseña del registro con el valor recibido.
     * @param confirmarContraseña confirmación de contraseña del registro.
     */
    public void setConfirmarContraseña(String confirmarContraseña) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.confirmarContraseña = confirmarContraseña;
        this.confirmarContraseña = confirmarContraseña;
    }

    /** [Doc]
     * Indica si se cumple la condición aceptar terminos.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public boolean isAceptarTerminos() {
        // [Doc] Termina el método o la función y devuelve aceptarTerminos.
        return aceptarTerminos;
    }

    /** [Doc]
     * Asigna aceptación de términos del registro con el valor recibido.
     * @param aceptarTerminos aceptación de términos del registro.
     */
    public void setAceptarTerminos(boolean aceptarTerminos) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.aceptarTerminos = aceptarTerminos;
        this.aceptarTerminos = aceptarTerminos;
    }
}
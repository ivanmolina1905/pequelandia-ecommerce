package com.pequelandia.pequelandia.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.dto.RegistroUsuarioDTO;
import com.pequelandia.pequelandia.model.Localidad;
import com.pequelandia.pequelandia.model.Usuario;
/** [Doc]
 * Define el contrato de operaciones de usuario que implementa el servicio correspondiente.
 */
public interface IUsuarioService {

	//metodo para traer a todos los usuarios
	//lectura
    /** [Doc]
     * Devuelve usuarios, según el cuerpo o la implementación del contrato.
     * @return List&lt;Usuario&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public List<Usuario> getUsuarios();
	
	//alta
    /** [Doc]
     * Guarda usuario en el ámbito de IUsuarioService.
     * @param usu usu.
     */
	public void saveUsuario(Usuario usu);
	
	//baja
    /** [Doc]
     * Elimina usuario en el ámbito de IUsuarioService.
     * @param id identificador del registro.
     */
	public void deleteUsuario(Long id);
	
	//lectura de un solo usuario
    /** [Doc]
     * Consulta persistencia según el criterio expresado por findUsuario; la consulta declarada o la convención de
     * Spring Data determina el resultado.
     * @param id identificador del registro.
     * @return Usuario con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public Usuario findUsuario(Long id);
	
	//editar usuario
    /** [Doc]
     * Actualiza usuario en el ámbito de IUsuarioService.
     * @param id identificador del registro.
     * @param usu usu.
     */
	public void editUsuario(Long id, Usuario usu);
		
    /** [Doc]
     * Consulta y devuelve los datos de la operación en el ámbito de IUsuarioService.
     * @param q q.
     * @param pageable parámetros de paginación y orden.
     * @return Page&lt;Usuario&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	Page<Usuario> listar(String q, Pageable pageable);
       
    /** [Doc]
     * Resuelve la operación login dentro de IUsuarioService; el detalle de instrucciones muestra sus pasos y
     * llamadas.
     * @param email dirección de correo de la cuenta.
     * @param contraseña contraseña de la cuenta, actualmente conservada sin hash.
     * @return Optional&lt;Usuario&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public Optional<Usuario> login(String email,String contraseña);

    /** [Doc]
     * Resuelve la operación existe email dentro de IUsuarioService; el detalle de instrucciones muestra sus pasos y
     * llamadas.
     * @param email dirección de correo de la cuenta.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	boolean existeEmail(String email);

    /** [Doc]
     * Resuelve la operación existe nombre usuario dentro de IUsuarioService; el detalle de instrucciones muestra sus
     * pasos y llamadas.
     * @param nombreUsuario nombre usuario.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	boolean existeNombreUsuario(String nombreUsuario);

    /** [Doc]
     * Resuelve la operación existe dni dentro de IUsuarioService; el detalle de instrucciones muestra sus pasos y
     * llamadas.
     * @param dni documento de identidad.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	boolean existeDni(int dni);

    /** [Doc]
     * Guarda la cuenta y genera bienvenida y aviso de alta dentro de una transacción.
     * @param dto objeto de transferencia con los datos de la operación.
     * @param localidad localidad asociada a la cuenta.
     * @return Usuario con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	Usuario registrar(RegistroUsuarioDTO dto, Localidad localidad);
	
}
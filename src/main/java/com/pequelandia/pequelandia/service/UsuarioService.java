package com.pequelandia.pequelandia.service;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.pequelandia.pequelandia.dto.RegistroUsuarioDTO;
import com.pequelandia.pequelandia.model.Localidad;
import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.repository.IUsuarioRepository;

/** [Doc]
 * Gestiona cuentas, credenciales, duplicados y registro transaccional con bienvenida y aviso administrativo.
 */
@Service
public class UsuarioService implements IUsuarioService{


	//generamos la dependenca "inyeccion de dependencia"
    // [Doc] Atributo UsuarioRepo (IUsuarioRepository): acceso de persistencia a usuario.
	@Autowired
	private IUsuarioRepository UsuarioRepo;
    // [Doc] Atributo identidad (IdentidadService): colaborador que concentra operaciones de identidad.
    @Autowired private IdentidadService identidad;
    // [Doc] Atributo notificaciones (INotificacionService): colaborador que concentra operaciones de notificacion.
    @Autowired private INotificacionService notificaciones;
    // [Doc] Atributo correos (CorreoService): colaborador que concentra operaciones de correo.
    @Autowired private CorreoService correos;

	
    /** [Doc]
     * Devuelve usuarios, según el cuerpo o la implementación del contrato.
     * @return List&lt;Usuario&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	@Override
	public List<Usuario> getUsuarios() {
		
        // [Doc] Variable local listaUsuario (List<Usuario>): lista usuario. Se inicializa con UsuarioRepo.findAll().
		List<Usuario> listaUsuario=UsuarioRepo.findAll();
        // [Doc] Termina el método o la función y devuelve listaUsuario.
		return listaUsuario;	
	}

    /** [Doc]
     * Guarda usuario en el ámbito de UsuarioService.
     * @param usu usu.
     */
	@Override
	public void saveUsuario(Usuario usu) {
        // [Doc] Ejecuta la llamada o modificación identidad.validarNombre(usu, usu.getNombre_usuario()); Sus efectos dependen del método invocado y del objeto receptor.
		identidad.validarNombre(usu, usu.getNombre_usuario());
        // [Doc] Solicita persistir la entidad; saveAndFlush también fuerza la sincronización: UsuarioRepo.save(usu);
        UsuarioRepo.save(usu);
	}

    /** [Doc]
     * Elimina usuario en el ámbito de UsuarioService.
     * @param id identificador del registro.
     */
	@Override
	public void deleteUsuario(Long id) {
        // [Doc] Ejecuta la llamada o modificación UsuarioRepo.deleteById(id); Sus efectos dependen del método invocado y del objeto receptor.
		UsuarioRepo.deleteById(id);
	}

    /** [Doc]
     * Consulta persistencia según el criterio expresado por findUsuario; la consulta declarada o la convención de
     * Spring Data determina el resultado.
     * @param id identificador del registro.
     * @return Usuario con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	@Override
	public Usuario findUsuario(Long id) {
        // [Doc] Variable local usuariobuscado (Usuario): usuariobuscado. Se inicializa con UsuarioRepo.findById(id).orElse(null).
		Usuario usuariobuscado=UsuarioRepo.findById(id).orElse(null);
        // [Doc] Termina el método o la función y devuelve usuariobuscado.
		return usuariobuscado;
	}

    /** [Doc]
     * Actualiza usuario en el ámbito de UsuarioService.
     * @param id identificador del registro.
     * @param cambios datos nuevos que se aplican a la entidad existente.
     */
	@Override
	public void editUsuario(Long id, Usuario cambios) {
		
        // [Doc] Variable local db (Usuario): db. Se inicializa con findUsuario(id).
	    Usuario db = findUsuario(id); // lanza si no existe

		 // Strings: solo si no vienen null/blank
        // [Doc] Evalúa cambios.getNombre_usuario() != null && !cambios.getNombre_usuario().isBlank(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
	    if (cambios.getNombre_usuario() != null && !cambios.getNombre_usuario().isBlank())
            // [Doc] Ejecuta la llamada o modificación identidad.validarNombre(db, cambios.getNombre_usuario()); Sus efectos dependen del método invocado y del objeto receptor.
            identidad.validarNombre(db, cambios.getNombre_usuario());
        // [Doc] Ejecuta la llamada o modificación setIfHasText(db::setNombre_usuario, cambios.getNombre_usuario()); Sus efectos dependen del método invocado y del objeto receptor.
        setIfHasText(db::setNombre_usuario, cambios.getNombre_usuario());
        // [Doc] Ejecuta la llamada o modificación setIfHasText(db::setApellido, cambios.getApellido()); Sus efectos dependen del método invocado y del objeto receptor.
	    setIfHasText(db::setApellido,        cambios.getApellido());
        // [Doc] Ejecuta la llamada o modificación setIfHasText(db::setEmail, cambios.getEmail()); Sus efectos dependen del método invocado y del objeto receptor.
	    setIfHasText(db::setEmail,           cambios.getEmail());
        // [Doc] Ejecuta la llamada o modificación setIfHasText(db::setTelefono, cambios.getTelefono()); Sus efectos dependen del método invocado y del objeto receptor.
	    setIfHasText(db::setTelefono, 		cambios.getTelefono());
        // [Doc] Ejecuta la llamada o modificación setIfHasText(db::setDireccion, cambios.getDireccion()); Sus efectos dependen del método invocado y del objeto receptor.
	    setIfHasText(db::setDireccion,       cambios.getDireccion());
        // [Doc] Evalúa cambios.getDni() != 0. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
	    if (cambios.getDni() != 0) {
            // [Doc] Actualiza una propiedad o configuración mediante la llamada: db.setDni(cambios.getDni());
	        db.setDni(cambios.getDni());
	    }

	    
	    // usá null-check (o mantenelos comentados para no pisarlos).
        // [Doc] Ejecuta la llamada o modificación setIfNotNull(db::setFecha_registro, cambios.getFecha_registro()); Sus efectos dependen del método invocado y del objeto receptor.
	    setIfNotNull(db::setFecha_registro,   cambios.getFecha_registro());
        // [Doc] Ejecuta la llamada o modificación setIfNotNull(db::setUltima_conexion, cambios.getUltima_conexion()); Sus efectos dependen del método invocado y del objeto receptor.
	    setIfNotNull(db::setUltima_conexion,  cambios.getUltima_conexion());
         // [Doc] Ejecuta la llamada o modificación setIfNotNull(db::setFecha_nacimiento, cambios.getFecha_nacimiento()); Sus efectos dependen del método invocado y del objeto receptor.
	     setIfNotNull(db::setFecha_nacimiento, cambios.getFecha_nacimiento());
        // [Doc] Ejecuta la llamada o modificación setIfHasText(db::setContraseña, cambios.getContraseña()); Sus efectos dependen del método invocado y del objeto receptor.
	    setIfHasText(db::setContraseña,       cambios.getContraseña());
         // [Doc] Ejecuta la llamada o modificación setIfNotNull(db::setLocalidad, cambios.getLocalidad()); Sus efectos dependen del método invocado y del objeto receptor.
	     setIfNotNull(db::setLocalidad,        cambios.getLocalidad());

        // [Doc] Solicita persistir la entidad; saveAndFlush también fuerza la sincronización: UsuarioRepo.saveAndFlush(db);
	    UsuarioRepo.saveAndFlush(db);
	}

	

	/** Aplica setter solo si el valor NO es null. */
    /** [Doc]
     * Ejecuta el setter recibido únicamente cuando el valor no es nulo.
     * @param setter setter.
     * @param value value.
     */
	private static <T> void setIfNotNull(Consumer<T> setter, T value) {
        // [Doc] Evalúa value != null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
	    if (value != null) setter.accept(value);
	}

	/** Aplica setter para String solo si NO es null NI está en blanco. */
    /** [Doc]
     * Ejecuta el setter recibido únicamente cuando el texto tiene contenido.
     * @param setter setter.
     * @param value value.
     */
	private static void setIfHasText(Consumer<String> setter, String value) {
        // [Doc] Evalúa value != null && !value.trim().isEmpty(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
	    if (value != null && !value.trim().isEmpty()) setter.accept(value.trim());
	}

	


    /** [Doc]
     * Consulta y devuelve los datos de la operación en el ámbito de UsuarioService.
     * @param q q.
     * @param pageable parámetros de paginación y orden.
     * @return Page&lt;Usuario&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    public Page<Usuario> listar(String q, Pageable pageable) {
         // [Doc] Variable local term (String): term. Se inicializa con (q == null ? "" : q.trim()).
    	 String term = (q == null ? "" : q.trim());

            // [Doc] Variable local safe (Pageable): safe. Se inicializa con PageRequest.of( Math.max(pageable.getPageNumber(), 0), pageable.getPageSize() > 0 ? Math.min(pageable.getPa....
    	    Pageable safe = PageRequest.of(
    	        Math.max(pageable.getPageNumber(), 0),
    	        pageable.getPageSize() > 0 ? Math.min(pageable.getPageSize(), 100) : 10,
    	        Sort.by("id").ascending()
    	    );

            // [Doc] Evalúa term.isEmpty(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
    	    if (term.isEmpty()) {
                // [Doc] Ejecuta la llamada o modificación System.out.println("[usuarios] usando findAll"); Sus efectos dependen del método invocado y del objeto receptor.
    	        System.out.println("[usuarios] usando findAll");
                // [Doc] Variable local p (Page<Usuario>): p. Se inicializa con UsuarioRepo.findAll(safe).
    	        Page<Usuario> p = UsuarioRepo.findAll(safe);
                // [Doc] Ejecuta la llamada o modificación System.out.printf("[usuarios] total=%d, elems=%d%n", p.getTotalElements(), p.getNumberOfElements()); Sus efectos dependen del método invocado y del objeto receptor.
    	        System.out.printf("[usuarios] total=%d, elems=%d%n",
    	                p.getTotalElements(), p.getNumberOfElements());
                // [Doc] Termina el método o la función y devuelve p.
    	        return p;
    	    } else {
                // [Doc] Ejecuta la llamada o modificación System.out.println("[usuarios] usando buscar q='" + term + "'"); Sus efectos dependen del método invocado y del objeto receptor.
    	        System.out.println("[usuarios] usando buscar q='" + term + "'");
                // [Doc] Variable local p (Page<Usuario>): p. Se inicializa con UsuarioRepo.buscar(term, safe).
    	        Page<Usuario> p = UsuarioRepo.buscar(term, safe);
                // [Doc] Ejecuta la llamada o modificación System.out.printf("[usuarios] total=%d, elems=%d%n", p.getTotalElements(), p.getNumberOfElements()); Sus efectos dependen del método invocado y del objeto receptor.
    	        System.out.printf("[usuarios] total=%d, elems=%d%n",
    	                p.getTotalElements(), p.getNumberOfElements());
                // [Doc] Termina el método o la función y devuelve p.
    	        return p;
    	    }
}

  
   

    /** [Doc]
     * Valida las credenciales y guarda la última conexión únicamente si el ingreso es correcto.
     * @param email dirección de correo de la cuenta.
     * @param contraseña contraseña de la cuenta, actualmente conservada sin hash.
     * @return Optional&lt;Usuario&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @org.springframework.transaction.annotation.Transactional
    public Optional<Usuario> login(String email,String contraseña) {
        return UsuarioRepo.login(email.trim(), contraseña).map(usuario -> {
            usuario.setUltima_conexion(new Date());
            return UsuarioRepo.save(usuario);
        });
    }
    
    /** [Doc]
     * Resuelve la operación existe email dentro de UsuarioService; el detalle de instrucciones muestra sus pasos y
     * llamadas.
     * @param email dirección de correo de la cuenta.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public boolean existeEmail(String email) {
        // [Doc] Termina el método o la función y devuelve UsuarioRepo.existeEmail(email.trim()).
        return UsuarioRepo.existeEmail(email.trim());
    }

    /** [Doc]
     * Resuelve la operación existe nombre usuario dentro de UsuarioService; el detalle de instrucciones muestra sus
     * pasos y llamadas.
     * @param nombreUsuario nombre usuario.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public boolean existeNombreUsuario(String nombreUsuario) {
        // [Doc] Termina el método o la función y devuelve UsuarioRepo.existeNombreUsuario(nombreUsuario.trim()).
        return UsuarioRepo.existeNombreUsuario(nombreUsuario.trim());
    }

    /** [Doc]
     * Resuelve la operación existe dni dentro de UsuarioService; el detalle de instrucciones muestra sus pasos y
     * llamadas.
     * @param dni documento de identidad.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public boolean existeDni(int dni) {
        // [Doc] Termina el método o la función y devuelve UsuarioRepo.existeDni(dni).
        return UsuarioRepo.existeDni(dni);
    }
    

    

   
    /** [Doc]
     * Guarda la cuenta y genera bienvenida y aviso de alta dentro de una transacción.
     * @param dto objeto de transferencia con los datos de la operación.
     * @param localidad localidad asociada a la cuenta.
     * @return Usuario con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @org.springframework.transaction.annotation.Transactional
    public Usuario registrar(RegistroUsuarioDTO dto, Localidad localidad) {
        // [Doc] Variable local usuario (Usuario): cuenta de usuario utilizada por la operación. Se inicializa con new Usuario().
        Usuario usuario = new Usuario();
        usuario.setRol(Usuario.Rol.USUARIO);

        // [Doc] Ejecuta la llamada o modificación identidad.validarNombre(usuario, dto.getNombre_usuario()); Sus efectos dependen del método invocado y del objeto receptor.
        identidad.validarNombre(usuario, dto.getNombre_usuario());
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: usuario.setNombre_usuario(dto.getNombre_usuario().trim());
        usuario.setNombre_usuario(dto.getNombre_usuario().trim());
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: usuario.setApellido(dto.getApellido().trim());
        usuario.setApellido(dto.getApellido().trim());
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: usuario.setEmail(dto.getEmail().trim().toLowerCase());
        usuario.setEmail(dto.getEmail().trim().toLowerCase());
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: usuario.setDni(dto.getDni());
        usuario.setDni(dto.getDni());
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: usuario.setTelefono(dto.getTelefono() == null ? "" : dto.getTelefono().trim());
        usuario.setTelefono(dto.getTelefono() == null ? "" : dto.getTelefono().trim());
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: usuario.setDireccion(dto.getDireccion().trim());
        usuario.setDireccion(dto.getDireccion().trim());
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: usuario.setFecha_nacimiento(dto.getFecha_nacimiento());
        usuario.setFecha_nacimiento(dto.getFecha_nacimiento());
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: usuario.setFecha_registro(new Date());
        usuario.setFecha_registro(new Date());
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: usuario.setLocalidad(localidad);
        usuario.setLocalidad(localidad);
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: usuario.setContraseña(dto.getContraseña());
        usuario.setContraseña(dto.getContraseña());
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: usuario.setPuntos(0);
        usuario.setPuntos(0);

        // [Doc] Variable local guardado (Usuario): guardado. Se inicializa con UsuarioRepo.save(usuario).
        Usuario guardado = UsuarioRepo.save(usuario);
        // [Doc] Ejecuta la llamada o modificación notificaciones.notificarNuevoUsuario(guardado); Sus efectos dependen del método invocado y del objeto receptor.
        notificaciones.notificarNuevoUsuario(guardado);
        // [Doc] Ejecuta la llamada o modificación correos.enviarBienvenida(guardado); Sus efectos dependen del método invocado y del objeto receptor.
        correos.enviarBienvenida(guardado);
        // [Doc] Termina el método o la función y devuelve guardado.
        return guardado;
    }

	
}
		



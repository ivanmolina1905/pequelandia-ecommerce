package com.pequelandia.pequelandia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pequelandia.pequelandia.model.Usuario;

//jparepository es una clase que tiene jpa donde estan los metodos para el crud, esta clase recibe dos parametros uno es la clase a la cual le hacemos el repository y otra el tipo de dato del id
/** [Doc]
 * Declara consultas y operaciones JPA de usuario; Spring Data crea la implementación.
 */
public interface IUsuarioRepository extends JpaRepository<Usuario,Long> {
    /** [Doc]
     * Consulta persistencia según el criterio expresado por findByIdParaRecuperacion; la consulta declarada o la
     * convención de Spring Data determina el resultado.
     * @param id identificador del registro.
     * @return Optional&lt;Usuario&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.id = :id")
    Optional<Usuario> findByIdParaRecuperacion(@Param("id") Long id);
	
    /** [Doc]
     * Busca los datos de la operación en el ámbito de IUsuarioRepository.
     * @param q q.
     * @param pageable parámetros de paginación y orden.
     * @return Page&lt;Usuario&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	@Query("""
		       SELECT u
		       FROM Usuario u
		       WHERE LOWER(COALESCE(u.nombre_usuario, '')) LIKE LOWER(CONCAT('%', :q, '%'))
		          OR LOWER(COALESCE(u.apellido, ''))      LIKE LOWER(CONCAT('%', :q, '%'))
		          OR LOWER(COALESCE(u.email, ''))         LIKE LOWER(CONCAT('%', :q, '%'))
		       """)
		Page<Usuario> buscar(@Param("q") String q, Pageable pageable);

	
	
	
    /** [Doc]
     * Resuelve la operación login dentro de IUsuarioRepository; el detalle de instrucciones muestra sus pasos y
     * llamadas.
     * @param user user.
     * @param pass pass.
     * @return Optional&lt;Usuario&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	@Query("""
		    SELECT u
		    FROM Usuario u
		    WHERE LOWER(u.email) = LOWER(:user)
		    AND u.contraseña = :pass
		""")
		Optional<Usuario> login(
		        @Param("user") String user,
		        @Param("pass") String pass
		);


	// Usuarios registrados por día
    /** [Doc]
     * Resuelve la operación usuarios registrados por dia dentro de IUsuarioRepository; el detalle de instrucciones
     * muestra sus pasos y llamadas.
     * @return List&lt;Object[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	@Query(value = """
	        SELECT DATE(u.fecha_registro) AS fecha, COUNT(*) AS total
	        FROM usuario u
	        GROUP BY DATE(u.fecha_registro)
	        ORDER BY fecha
	    """, nativeQuery = true)
	    List<Object[]> usuariosRegistradosPorDia();

    //  Usuarios registrados por mes
    /** [Doc]
     * Resuelve la operación usuarios registrados por mes dentro de IUsuarioRepository; el detalle de instrucciones
     * muestra sus pasos y llamadas.
     * @param anio año de la consulta.
     * @return List&lt;Object[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query(value = """
            SELECT EXTRACT(MONTH FROM u.fecha_registro) AS mes, COUNT(*) AS total
            FROM usuario u
            WHERE EXTRACT(YEAR FROM u.fecha_registro) = :anio
            GROUP BY EXTRACT(MONTH FROM u.fecha_registro)
            ORDER BY mes
        """, nativeQuery = true)
        List<Object[]> usuariosRegistradosPorMes(@Param("anio") int anio);

    //  Usuarios registrados por año
    /** [Doc]
     * Resuelve la operación usuarios registrados por anio dentro de IUsuarioRepository; el detalle de instrucciones
     * muestra sus pasos y llamadas.
     * @return List&lt;Object[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query(value = """
            SELECT EXTRACT(YEAR FROM u.fecha_registro) AS anio, COUNT(*) AS total
            FROM usuario u
            GROUP BY EXTRACT(YEAR FROM u.fecha_registro)
            ORDER BY anio
        """, nativeQuery = true)
        List<Object[]> usuariosRegistradosPorAnio();
        
        
        
        
        /** [Doc]
         * Resuelve la operación existe email dentro de IUsuarioRepository; el detalle de instrucciones muestra sus pasos
         * y llamadas.
         * @param email dirección de correo de la cuenta.
         * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
        @Query("""
        	    SELECT COUNT(u) > 0
        	    FROM Usuario u
        	    WHERE LOWER(u.email) = LOWER(:email)
        	""")
        	boolean existeEmail(@Param("email") String email);


            /** [Doc]
             * Resuelve la operación existe nombre usuario dentro de IUsuarioRepository; el detalle de instrucciones muestra
             * sus pasos y llamadas.
             * @param nombreUsuario nombre usuario.
             * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
             */
        	@Query("""
        	    SELECT COUNT(u) > 0
        	    FROM Usuario u
        	    WHERE LOWER(u.nombre_usuario) = LOWER(:nombreUsuario)
        	""")
        	boolean existeNombreUsuario(@Param("nombreUsuario") String nombreUsuario);


            /** [Doc]
             * Resuelve la operación existe dni dentro de IUsuarioRepository; el detalle de instrucciones muestra sus pasos y
             * llamadas.
             * @param dni documento de identidad.
             * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
             */
        	@Query("""
        	    SELECT COUNT(u) > 0
        	    FROM Usuario u
        	    WHERE u.dni = :dni
        	""")
        	boolean existeDni(@Param("dni") int dni);




			
        
        

    /** [Doc]
     * Resuelve la operación ids por nombre dentro de IUsuarioRepository; el detalle de instrucciones muestra sus
     * pasos y llamadas.
     * @param nombre nombre normalizado o visible de la entidad.
     * @return List&lt;Long&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("select u.id from Usuario u where lower(u.nombre_usuario) = lower(:nombre)")
    List<Long> idsPorNombre(@Param("nombre") String nombre);

    /** [Doc]
     * Resuelve la operación email en uso dentro de IUsuarioRepository; el detalle de instrucciones muestra sus
     * pasos y llamadas.
     * @param id identificador del registro.
     * @param email dirección de correo de la cuenta.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("select count(u)>0 from Usuario u where u.id <> :id and lower(u.email)=lower(:email)")
    boolean emailEnUso(@Param("id") Long id, @Param("email") String email);

}

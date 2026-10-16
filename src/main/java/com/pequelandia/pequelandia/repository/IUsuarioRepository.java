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
public interface IUsuarioRepository extends JpaRepository<Usuario,Long> {
	
	@Query("""
		       SELECT u
		       FROM Usuario u
		       WHERE LOWER(COALESCE(u.nombre_usuario, '')) LIKE LOWER(CONCAT('%', :q, '%'))
		          OR LOWER(COALESCE(u.apellido, ''))      LIKE LOWER(CONCAT('%', :q, '%'))
		          OR LOWER(COALESCE(u.email, ''))         LIKE LOWER(CONCAT('%', :q, '%'))
		       """)
		Page<Usuario> buscar(@Param("q") String q, Pageable pageable);

	
	
	
	@Query("""
		    SELECT u
		    FROM Usuario u
		    WHERE (
		        LOWER(u.nombre_usuario) = LOWER(:user)
		        OR LOWER(u.email) = LOWER(:user)
		    )
		    AND u.contraseña = :pass
		""")
		Optional<Usuario> login(
		        @Param("user") String user,
		        @Param("pass") String pass
		);


	// Usuarios registrados por día
	@Query(value = """
	        SELECT DATE(u.fecha_registro) AS fecha, COUNT(*) AS total
	        FROM usuario u
	        GROUP BY DATE(u.fecha_registro)
	        ORDER BY fecha
	    """, nativeQuery = true)
	    List<Object[]> usuariosRegistradosPorDia();

    //  Usuarios registrados por mes
    @Query(value = """
            SELECT EXTRACT(MONTH FROM u.fecha_registro) AS mes, COUNT(*) AS total
            FROM usuario u
            WHERE EXTRACT(YEAR FROM u.fecha_registro) = :anio
            GROUP BY EXTRACT(MONTH FROM u.fecha_registro)
            ORDER BY mes
        """, nativeQuery = true)
        List<Object[]> usuariosRegistradosPorMes(@Param("anio") int anio);

    //  Usuarios registrados por año
    @Query(value = """
            SELECT EXTRACT(YEAR FROM u.fecha_registro) AS anio, COUNT(*) AS total
            FROM usuario u
            GROUP BY EXTRACT(YEAR FROM u.fecha_registro)
            ORDER BY anio
        """, nativeQuery = true)
        List<Object[]> usuariosRegistradosPorAnio();
        
        
        
        
        @Query("""
        	    SELECT COUNT(u) > 0
        	    FROM Usuario u
        	    WHERE LOWER(u.email) = LOWER(:email)
        	""")
        	boolean existeEmail(@Param("email") String email);


        	@Query("""
        	    SELECT COUNT(u) > 0
        	    FROM Usuario u
        	    WHERE LOWER(u.nombre_usuario) = LOWER(:nombreUsuario)
        	""")
        	boolean existeNombreUsuario(@Param("nombreUsuario") String nombreUsuario);


        	@Query("""
        	    SELECT COUNT(u) > 0
        	    FROM Usuario u
        	    WHERE u.dni = :dni
        	""")
        	boolean existeDni(@Param("dni") int dni);




			
        
        
}

package com.pequelandia.pequelandia.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pequelandia.pequelandia.model.Notificacion;

/** [Doc]
 * Declara consultas y operaciones JPA de notificacion; Spring Data crea la implementación.
 */
public interface INotificacionRepository
        extends JpaRepository<Notificacion, Long> {

    /*
     * Usamos JPQL explícito porque tu atributo
     * fecha_notificacion contiene guion bajo.
     */
    /** [Doc]
     * Consulta y devuelve ultimas en el ámbito de INotificacionRepository.
     * @param destinatario identidad o dirección que recibirá el aviso o correo.
     * @param pageable parámetros de paginación y orden.
     * @return List&lt;Notificacion&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("""
        SELECT n
        FROM Notificacion n
        WHERE n.destinatario = :destinatario
        ORDER BY n.fecha_notificacion DESC
    """)
    List<Notificacion> listarUltimas(
            @Param("destinatario") String destinatario,
            Pageable pageable
    );

    /** [Doc]
     * Cuenta por destinatario en el ámbito de INotificacionRepository.
     * @param destinatario identidad o dirección que recibirá el aviso o correo.
     * @return long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("""
        SELECT COUNT(n)
        FROM Notificacion n
        WHERE n.destinatario = :destinatario
    """)
    long contarPorDestinatario(
            @Param("destinatario") String destinatario
    );
    
    /** [Doc]
     * Cuenta no leidas en el ámbito de INotificacionRepository.
     * @param destinatario identidad o dirección que recibirá el aviso o correo.
     * @return long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("""
            SELECT COUNT(n)
            FROM Notificacion n
            WHERE n.destinatario = :destinatario
              AND NOT EXISTS (
                  SELECT l.id
                  FROM NotificacionLectura l
                  WHERE l.notificacion = n
                    AND l.destinatario = :destinatario
              )
        """)
        long contarNoLeidas(
                @Param("destinatario") String destinatario
        );
    
}
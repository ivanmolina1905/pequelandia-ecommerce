package com.pequelandia.pequelandia.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pequelandia.pequelandia.model.Notificacion;

public interface INotificacionRepository
        extends JpaRepository<Notificacion, Long> {

    /*
     * Usamos JPQL explícito porque tu atributo
     * fecha_notificacion contiene guion bajo.
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

    @Query("""
        SELECT COUNT(n)
        FROM Notificacion n
        WHERE n.destinatario = :destinatario
    """)
    long contarPorDestinatario(
            @Param("destinatario") String destinatario
    );
    
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
package com.pequelandia.pequelandia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pequelandia.pequelandia.model.NotificacionLectura;

public interface INotificacionLecturaRepository
        extends JpaRepository<NotificacionLectura, Long> {

    @Query("""
        SELECT l
        FROM NotificacionLectura l
        WHERE l.notificacion.id_notificacion = :notificacionId
          AND l.destinatario = :destinatario
    """)
    Optional<NotificacionLectura> buscarLectura(
            @Param("notificacionId") Long notificacionId,
            @Param("destinatario") String destinatario
    );

    @Modifying
    @Query(value = """
        INSERT INTO notificacion_lectura
            (id_notificacion, destinatario, fecha_lectura)
        SELECT n.id_notificacion, :destinatario, CURRENT_TIMESTAMP
        FROM notificacion n
        WHERE n.destinatario = :destinatario
          AND NOT EXISTS (
              SELECT 1
              FROM notificacion_lectura l
              WHERE l.id_notificacion = n.id_notificacion
                AND l.destinatario = :destinatario
          )
    """, nativeQuery = true)
    void marcarTodasComoLeidas(
            @Param("destinatario") String destinatario
    );
}
package com.pequelandia.pequelandia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pequelandia.pequelandia.model.NotificacionLectura;

/** [Doc]
 * Declara consultas y operaciones JPA de notificacion lectura; Spring Data crea la implementación.
 */
public interface INotificacionLecturaRepository
        extends JpaRepository<NotificacionLectura, Long> {

    /** [Doc]
     * Busca lectura en el ámbito de INotificacionLecturaRepository.
     * @param notificacionId notificacion id.
     * @param destinatario identidad o dirección que recibirá el aviso o correo.
     * @return Optional&lt;NotificacionLectura&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
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

    /** [Doc]
     * Registra todas como leidas en el ámbito de INotificacionLecturaRepository.
     * @param destinatario identidad o dirección que recibirá el aviso o correo.
     */
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
        ON CONFLICT (id_notificacion, destinatario) DO NOTHING
    """, nativeQuery = true)
    void marcarTodasComoLeidas(
            @Param("destinatario") String destinatario
    );

    /** [Doc]
     * Resuelve la operación insertar lectura dentro de INotificacionLecturaRepository; el detalle de instrucciones
     * muestra sus pasos y llamadas.
     * @param id identificador del registro.
     * @param destinatario identidad o dirección que recibirá el aviso o correo.
     */
    @Modifying
    @Query(value = """
        INSERT INTO notificacion_lectura (id_notificacion, destinatario, fecha_lectura)
        VALUES (:id, :destinatario, CURRENT_TIMESTAMP)
        ON CONFLICT (id_notificacion, destinatario) DO NOTHING
        """, nativeQuery = true)
    void insertarLectura(@Param("id") Long id, @Param("destinatario") String destinatario);
}
package com.pequelandia.pequelandia.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** [Doc]
 * Representa la lectura de un aviso por un destinatario.
 */
@Entity
@Table(
    name = "notificacion_lectura",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_notificacion_lectura_destinatario",
            columnNames = {"id_notificacion", "destinatario"}
        )
    }
)
public class NotificacionLectura {

    // [Doc] Atributo id (Long): identificador del registro.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // [Doc] Atributo notificacion (Notificacion): aviso que se construye o consulta.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_notificacion", nullable = false)
    private Notificacion notificacion;

    // [Doc] Atributo destinatario (String): identidad o dirección que recibirá el aviso o correo.
    @Column(nullable = false, length = 80)
    private String destinatario;

    // [Doc] Atributo fechaLectura (LocalDateTime): fecha lectura.
    @Column(name = "fecha_lectura", nullable = false)
    private LocalDateTime fechaLectura;

    /** [Doc]
     * Construye NotificacionLectura y conserva sus dependencias o valores iniciales.
     */
    public NotificacionLectura() {
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
     * Devuelve notificacion, según el cuerpo o la implementación del contrato.
     * @return Notificacion con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public Notificacion getNotificacion() {
        // [Doc] Termina el método o la función y devuelve notificacion.
        return notificacion;
    }

    /** [Doc]
     * Asigna notificacion con el valor recibido.
     * @param notificacion aviso que se construye o consulta.
     */
    public void setNotificacion(Notificacion notificacion) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.notificacion = notificacion;
        this.notificacion = notificacion;
    }

    /** [Doc]
     * Devuelve identidad o dirección que recibirá el aviso o correo, según el cuerpo o la implementación del
     * contrato.
     * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public String getDestinatario() {
        // [Doc] Termina el método o la función y devuelve destinatario.
        return destinatario;
    }

    /** [Doc]
     * Asigna identidad o dirección que recibirá el aviso o correo con el valor recibido.
     * @param destinatario identidad o dirección que recibirá el aviso o correo.
     */
    public void setDestinatario(String destinatario) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.destinatario = destinatario;
        this.destinatario = destinatario;
    }

    /** [Doc]
     * Devuelve fecha lectura, según el cuerpo o la implementación del contrato.
     * @return LocalDateTime con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public LocalDateTime getFechaLectura() {
        // [Doc] Termina el método o la función y devuelve fechaLectura.
        return fechaLectura;
    }

    /** [Doc]
     * Asigna fecha lectura con el valor recibido.
     * @param fechaLectura fecha lectura.
     */
    public void setFechaLectura(LocalDateTime fechaLectura) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.fechaLectura = fechaLectura;
        this.fechaLectura = fechaLectura;
    }
}
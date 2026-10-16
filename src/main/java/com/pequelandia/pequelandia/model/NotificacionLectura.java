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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_notificacion", nullable = false)
    private Notificacion notificacion;

    @Column(nullable = false, length = 80)
    private String destinatario;

    @Column(name = "fecha_lectura", nullable = false)
    private LocalDateTime fechaLectura;

    public NotificacionLectura() {
    }

    public Long getId() {
        return id;
    }

    public Notificacion getNotificacion() {
        return notificacion;
    }

    public void setNotificacion(Notificacion notificacion) {
        this.notificacion = notificacion;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public LocalDateTime getFechaLectura() {
        return fechaLectura;
    }

    public void setFechaLectura(LocalDateTime fechaLectura) {
        this.fechaLectura = fechaLectura;
    }
}
package com.pequelandia.pequelandia.config;

import com.pequelandia.pequelandia.service.EntregaCorreoService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** [Doc]
 * Procesa periódicamente la cola de correos cuando la entrega está habilitada.
 */
@Component
@EnableScheduling
@ConditionalOnProperty(name = "app.mail.delivery-enabled", havingValue = "true", matchIfMissing = true)
public class CorreoScheduler {
    // [Doc] Atributo entrega (EntregaCorreoService): colaborador que concentra operaciones de entrega correo. Su referencia no se reasigna después de inicializarse.
    private final EntregaCorreoService entrega;
    /** [Doc]
     * Construye CorreoScheduler y conserva sus dependencias o valores iniciales.
     * @param entrega colaborador que concentra operaciones de entrega correo.
     */
    public CorreoScheduler(EntregaCorreoService entrega) { this.entrega = entrega; }
    /** [Doc]
     * Intenta procesar hasta diez mensajes por ejecución; termina antes cuando no hay otro mensaje disponible.
     */
    @Scheduled(fixedDelayString = "${app.mail.poll-ms:5000}")
    public void enviarPendientes() {
        // [Doc] Recorre los elementos o repite según int i = 0; i < 10 && entrega.entregarSiguiente(); i++. Cada vuelta ejecuta el cuerpo del bucle.
        for (int i = 0; i < 10 && entrega.entregarSiguiente(); i++) { /* lote limitado */ }
    }
}

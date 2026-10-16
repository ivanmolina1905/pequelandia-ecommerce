package com.pequelandia.pequelandia.config;

import com.pequelandia.pequelandia.service.EntregaCorreoService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@ConditionalOnProperty(name = "app.mail.delivery-enabled", havingValue = "true", matchIfMissing = true)
public class CorreoScheduler {
    private final EntregaCorreoService entrega;
    public CorreoScheduler(EntregaCorreoService entrega) { this.entrega = entrega; }
    @Scheduled(fixedDelayString = "${app.mail.poll-ms:5000}")
    public void enviarPendientes() {
        for (int i = 0; i < 10 && entrega.entregarSiguiente(); i++) { /* lote limitado */ }
    }
}

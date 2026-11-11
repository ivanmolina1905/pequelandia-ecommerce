package com.pequelandia.pequelandia;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** [Doc]
 * Configura el soporte compartido de las pruebas contra una base de datos descartable.
 */
@SpringBootTest(properties = {
    "spring.config.import=", "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false", "app.mail.delivery-enabled=false", "app.security.admin-id=1",
    "management.health.mail.enabled=false", "logging.level.root=WARN",
    "spring.mail.username=pruebas@example.invalid", "spring.mail.password=",
    "app.admin.email=admin@example.invalid", "spring.datasource.hikari.maximum-pool-size=6"
})
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "PEQUELANDIA_TEST_DB_URL", matches = ".+/pequelandia_verificacion")
public abstract class IntegrationSupport {
    /** [Doc]
     * Prepara o auxilia el escenario de prueba: base aislada.
     * @param registry registro de propiedades dinámicas del contexto de pruebas.
     */
    @DynamicPropertySource
    static void baseAislada(DynamicPropertyRegistry registry) {
        // [Doc] Variable local url (String): url. Se inicializa con System.getenv("PEQUELANDIA_TEST_DB_URL").
        String url = System.getenv("PEQUELANDIA_TEST_DB_URL");
        // [Doc] Evalúa url == null || !url.matches("jdbc:postgresql://(localhost|127\\.0\\.0\\.1):[0-9]+/pequelandia_verificacion"). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (url == null || !url.matches("jdbc:postgresql://(localhost|127\\.0\\.0\\.1):[0-9]+/pequelandia_verificacion"))
            // [Doc] Interrumpe el flujo lanzando new IllegalStateException("Las pruebas requieren una base local descartable pequelandia_verificacion"). El llamador o el manejador de errores decide la respuesta.
            throw new IllegalStateException("Las pruebas requieren una base local descartable pequelandia_verificacion");
        // [Doc] Ejecuta la llamada o modificación registry.add("spring.datasource.url", () -> url); Sus efectos dependen del método invocado y del objeto receptor.
        registry.add("spring.datasource.url", () -> url);
        // [Doc] Ejecuta la llamada o modificación registry.add("spring.datasource.username", () -> "pequelandia_test"); Sus efectos dependen del método invocado y del objeto receptor.
        registry.add("spring.datasource.username", () -> "pequelandia_test");
        // [Doc] Ejecuta la llamada o modificación registry.add("spring.datasource.password", () -> ""); Sus efectos dependen del método invocado y del objeto receptor.
        registry.add("spring.datasource.password", () -> "");
    }
}

package com.pequelandia.pequelandia;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

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
    @DynamicPropertySource
    static void baseAislada(DynamicPropertyRegistry registry) {
        String url = System.getenv("PEQUELANDIA_TEST_DB_URL");
        if (url == null || !url.matches("jdbc:postgresql://(localhost|127\\.0\\.0\\.1):[0-9]+/pequelandia_verificacion"))
            throw new IllegalStateException("Las pruebas requieren una base local descartable pequelandia_verificacion");
        registry.add("spring.datasource.url", () -> url);
        registry.add("spring.datasource.username", () -> "pequelandia_test");
        registry.add("spring.datasource.password", () -> "");
    }
}

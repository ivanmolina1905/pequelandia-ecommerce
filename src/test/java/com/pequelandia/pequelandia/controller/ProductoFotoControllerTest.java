package com.pequelandia.pequelandia.controller;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class ProductoFotoControllerTest {
    @TempDir Path directory;

    @Test void savesImageAndServesItAfterControllerRestart() throws Exception {
        var controller = new ProductoFotoController(directory.toString());
        var bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB), "jpg", bytes);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        try {
            var response = controller.upload(new MockMultipartFile("foto", "../../foto.jpg", "image/jpeg", bytes.toByteArray()));
            assertEquals(200, response.getStatusCode().value());
            String url = response.getBody().get("url");
            String name = url.substring(url.lastIndexOf('/') + 1);
            var stored = new ProductoFotoController(directory.toString()).image(name);
            assertEquals(200, stored.getStatusCode().value());
            assertNotNull(ImageIO.read(stored.getBody().getFile()));
            assertEquals(404, controller.image("../missing.png").getStatusCode().value());
        } finally { RequestContextHolder.resetRequestAttributes(); }
    }

    @Test void rejectsEmptyFakeAndOversizedImages() {
        var controller = new ProductoFotoController(directory.toString());
        for (byte[] content : new byte[][] { {}, "not an image".getBytes(), new byte[5 * 1024 * 1024 + 1] }) {
            assertEquals(400, controller.upload(new MockMultipartFile("foto", "foto.jpg", "image/jpeg", content)).getStatusCode().value());
        }
    }
}

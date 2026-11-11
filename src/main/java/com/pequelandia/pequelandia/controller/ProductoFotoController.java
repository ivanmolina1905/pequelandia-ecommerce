package com.pequelandia.pequelandia.controller;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
public class ProductoFotoController {
    private final Path directory;

    public ProductoFotoController(@Value("${pequelandia.upload-dir:uploads/productos}") String directory) {
        this.directory = Path.of(directory).toAbsolutePath().normalize();
    }

    @PostMapping("/admin/productos/foto")
    @ResponseBody
    public ResponseEntity<Map<String, String>> upload(@RequestParam("foto") MultipartFile file) {
        if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of("error", "La foto debe pesar hasta 5 MB."));
        }
        try (var input = ImageIO.createImageInputStream(file.getInputStream())) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) return invalidImage();
            var reader = readers.next();
            BufferedImage image;
            try {
                reader.setInput(input);
                if ((long) reader.getWidth(0) * reader.getHeight(0) > 25000000) return invalidImage();
                image = reader.read(0);
            } finally { reader.dispose(); }
            Files.createDirectories(directory);
            String name = UUID.randomUUID() + ".png";
            ImageIO.write(image, "png", directory.resolve(name).toFile());
            String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/fotos-productos/").path(name).build().toUriString();
            return ResponseEntity.ok(Map.of("url", url));
        } catch (IOException | IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "No se pudo guardar la foto. Probá de nuevo."));
        }
    }

    private ResponseEntity<Map<String, String>> invalidImage() {
        return ResponseEntity.badRequest().body(Map.of("error", "Usá una imagen JPG o PNG de hasta 25 megapíxeles."));
    }

    @GetMapping("/fotos-productos/{name:[a-f0-9\\-]+\\.png}")
    public ResponseEntity<FileSystemResource> image(@PathVariable String name) {
        Path file = directory.resolve(name).normalize();
        if (!file.startsWith(directory) || !Files.isRegularFile(file)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(new FileSystemResource(file));
    }
}

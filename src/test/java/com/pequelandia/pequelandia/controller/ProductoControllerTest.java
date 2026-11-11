package com.pequelandia.pequelandia.controller;

import com.pequelandia.pequelandia.model.Producto;
import com.pequelandia.pequelandia.repository.IProductoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductoControllerTest {
    @Test void renderizaDetalleYOcultaCompraSinStock() throws Exception {
        var resolver = new org.thymeleaf.templateresolver.ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        var engine = new org.thymeleaf.spring6.SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        var servlet = new org.springframework.mock.web.MockServletContext();
        var request = new org.springframework.mock.web.MockHttpServletRequest(servlet);
        var response = new org.springframework.mock.web.MockHttpServletResponse();
        var exchange = org.thymeleaf.web.servlet.JakartaServletWebApplication.buildApplication(servlet)
                .buildExchange(request, response);
        var context = new org.thymeleaf.context.WebContext(exchange);
        var producto = new Producto();
        producto.setNombre_producto("Remera <infantil>");
        producto.setIdProducto(7L);
        producto.setPrecio(15000);
        producto.setDescripcion_producto("Remera infantil de algodón.");
        producto.setTalle_producto("3-6m");
        producto.setStock(3);
        context.setVariable("p", producto);
        context.setVariable("csrfToken", "token-prueba");
        var html = engine.process("producto_detalle", context);
        org.junit.jupiter.api.Assertions.assertTrue(html.contains("Remera &lt;infantil&gt;"));
        org.junit.jupiter.api.Assertions.assertTrue(html.contains("/imagenes/imagendefecto.png"));
        org.junit.jupiter.api.Assertions.assertTrue(html.contains("Agregar al carrito"));
        guardarVistaParaNavegador("detalle.html", html);
        producto.setStock(0);
        html = engine.process("producto_detalle", context);
        org.junit.jupiter.api.Assertions.assertTrue(html.contains("Sin stock"));
        org.junit.jupiter.api.Assertions.assertFalse(html.contains("Agregar al carrito"));
        guardarVistaParaNavegador("sin-stock.html", html);
        producto.setStock(3);
        context.setVariable("productos", new org.springframework.data.domain.PageImpl<>(
                java.util.Collections.nCopies(12, producto)));
        context.setVariable("categorias", java.util.List.of());
        context.setVariable("ordenar", "relevancia");
        html = engine.process("verproductos", context);
        org.junit.jupiter.api.Assertions.assertTrue(html.contains("id=\"detalleProductoModal\""));
        org.junit.jupiter.api.Assertions.assertTrue(html.contains("js-ver-producto"));
        guardarVistaParaNavegador("catalogo.html", html);
    }

    private void guardarVistaParaNavegador(String nombre, String html) throws Exception {
        if (!Boolean.getBoolean("producto.modal.fixtures")) return;
        var directorio = java.nio.file.Path.of("target", "producto-modal-fixtures");
        java.nio.file.Files.createDirectories(directorio);
        java.nio.file.Files.writeString(directorio.resolve(nombre), html);
    }

    @Test void muestraDetalleInclusoSinStock() throws Exception {
        var repo = mock(IProductoRepository.class);
        var producto = new Producto();
        producto.setEstado("activo");
        producto.setStock(0);
        when(repo.findById(7L)).thenReturn(Optional.of(producto));
        MockMvcBuilders.standaloneSetup(new ProductoController(repo)).build()
                .perform(get("/producto/7"))
                .andExpect(status().isOk())
                .andExpect(view().name("producto_detalle"))
                .andExpect(model().attribute("p", producto));
    }

    @Test void rechazaProductosInexistentesEInactivos() throws Exception {
        var repo = mock(IProductoRepository.class);
        var producto = new Producto();
        producto.setEstado("INACTIVO");
        when(repo.findById(7L)).thenReturn(Optional.of(producto));
        when(repo.findById(8L)).thenReturn(Optional.empty());
        var mvc = MockMvcBuilders.standaloneSetup(new ProductoController(repo)).build();
        mvc.perform(get("/producto/7")).andExpect(status().isNotFound());
        mvc.perform(get("/producto/8")).andExpect(status().isNotFound());
    }
}

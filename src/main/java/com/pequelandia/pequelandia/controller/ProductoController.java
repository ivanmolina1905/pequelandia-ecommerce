package com.pequelandia.pequelandia.controller;

import com.pequelandia.pequelandia.repository.IProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ProductoController {
    private final IProductoRepository productos;

    public ProductoController(IProductoRepository productos) {
        this.productos = productos;
    }

    @GetMapping("/producto/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        var producto = productos.findById(id)
                .filter(p -> !"inactivo".equalsIgnoreCase(p.getEstado()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        model.addAttribute("p", producto);
        return "producto_detalle";
    }
}

package com.pequelandia.pequelandia.service;

import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.repository.IUsuarioRepository;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IdentidadServiceTest {
    @Test void compatibilidadFijaElIdYPermiteValidarUsuariosNuevos() {
        var repo = mock(IUsuarioRepository.class);
        when(repo.idsPorNombre("admin")).thenReturn(List.of(700L));
        var identidad = new IdentidadService(repo, 0L); identidad.inicializar();
        var nuevo = new Usuario();
        assertDoesNotThrow(() -> identidad.validarNombre(nuevo, "nuevo"));
        assertThrows(IllegalArgumentException.class, () -> identidad.validarNombre(nuevo, "admin"));
        nuevo.setId(701L); nuevo.setNombre_usuario("admin"); assertFalse(identidad.esAdmin(nuevo));
        nuevo.setId(700L); nuevo.setNombre_usuario("otro"); assertTrue(identidad.esAdmin(nuevo));
        assertThrows(IllegalArgumentException.class, () -> identidad.validarNombre(nuevo, "otro"));
    }
    @Test void administradorConfiguradoPuedeCambiarNombreSinPerderPermisos() {
        var identidad = new IdentidadService(mock(IUsuarioRepository.class), 700L); identidad.inicializar();
        var admin = new Usuario(); admin.setId(700L); admin.setNombre_usuario("gestor");
        assertTrue(identidad.esAdmin(admin)); assertDoesNotThrow(() -> identidad.validarNombre(admin, "gestor"));
    }
}

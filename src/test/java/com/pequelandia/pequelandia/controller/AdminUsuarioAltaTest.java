package com.pequelandia.pequelandia.controller;

import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminUsuarioAltaTest {
    @Test void guardaClaveYRolSeleccionados() {
        var servicio = mock(IUsuarioService.class);
        var controller = controller(servicio);
        var usuario = new Usuario();
        usuario.setContraseña("Clave-prueba-2026");
        usuario.setRol(Usuario.Rol.ADMINISTRADOR);
        var errores = new BeanPropertyBindingResult(usuario, "usuario");
        assertEquals("redirect:/admin/usuarios", controller.crearUsuario(usuario, errores, 1L,
                new ExtendedModelMap(), new RedirectAttributesModelMap()));
        verify(servicio).saveUsuario(usuario);
        assertEquals("Clave-prueba-2026", usuario.getContraseña());
        assertEquals(Usuario.Rol.ADMINISTRADOR, usuario.getRol());
    }
    @Test void rechazaAltaSinClave() {
        var servicio = mock(IUsuarioService.class);
        var controller = controller(servicio);
        var usuario = new Usuario();
        var errores = new BeanPropertyBindingResult(usuario, "usuario");
        assertEquals("paneladministracion", controller.crearUsuario(usuario, errores, 1L,
                new ExtendedModelMap(), new RedirectAttributesModelMap()));
        assertTrue(errores.hasFieldErrors("contraseña"));
        verify(servicio, never()).saveUsuario(any());
    }
    private AdminUsuarioController controller(IUsuarioService servicio) {
        var controller = new AdminUsuarioController();
        ReflectionTestUtils.setField(controller, "usuarioService", servicio);
        ReflectionTestUtils.setField(controller, "localidadService", mock(ILocalidadService.class));
        ReflectionTestUtils.setField(controller, "estadisticasService", mock(IEstadisticasService.class));
        return controller;
    }
}

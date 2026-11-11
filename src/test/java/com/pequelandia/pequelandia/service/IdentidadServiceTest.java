package com.pequelandia.pequelandia.service;

import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.repository.IUsuarioRepository;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** [Doc]
 * Agrupa pruebas automatizadas de identidad service.
 */
class IdentidadServiceTest {
    @Test void rolesExplicitosDeterminanAccesoInclusoParaElIdAnterior() {
        var identidad = new IdentidadService(mock(IUsuarioRepository.class), 700L);
        identidad.inicializar();
        var cuenta = new Usuario();
        cuenta.setId(701L);
        cuenta.setRol(Usuario.Rol.ADMINISTRADOR);
        assertTrue(identidad.esAdmin(cuenta));
        cuenta.setRol(Usuario.Rol.USUARIO);
        assertFalse(identidad.esAdmin(cuenta));
        cuenta.setId(700L);
        assertFalse(identidad.esAdmin(cuenta));
    }

    /** [Doc]
     * Comprueba el escenario «compatibilidad fija el id ypermite validar usuarios nuevos» mediante las aserciones
     * del cuerpo.
     */
    @Test void compatibilidadFijaElIdYPermiteValidarUsuariosNuevos() {
        // [Doc] Variable local repo (var; tipo inferido por Java): repo. Se inicializa con mock(IUsuarioRepository.class).
        var repo = mock(IUsuarioRepository.class);
        // [Doc] Ejecuta la llamada o modificación when(repo.idsPorNombre("admin")).thenReturn(List.of(700L)); Sus efectos dependen del método invocado y del objeto receptor.
        when(repo.idsPorNombre("admin")).thenReturn(List.of(700L));
        // [Doc] Variable local identidad (var; tipo inferido por Java): identidad. Se inicializa con new IdentidadService(repo, 0L).
        var identidad = new IdentidadService(repo, 0L); identidad.inicializar();
        // [Doc] Variable local nuevo (var; tipo inferido por Java): estado de destino normalizado. Se inicializa con new Usuario().
        var nuevo = new Usuario();
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertDoesNotThrow(() -> identidad.validarNombre(nuevo, "nuevo"));
        assertDoesNotThrow(() -> identidad.validarNombre(nuevo, "nuevo"));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertThrows(IllegalArgumentException.class, () -> identidad.validarNombre(nuevo, "admin"));
        assertThrows(IllegalArgumentException.class, () -> identidad.validarNombre(nuevo, "admin"));
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: nuevo.setId(701L);
        nuevo.setId(701L); nuevo.setNombre_usuario("admin"); assertFalse(identidad.esAdmin(nuevo));
        // [Doc] Actualiza una propiedad o configuración mediante la llamada: nuevo.setId(700L);
        nuevo.setId(700L); nuevo.setNombre_usuario("otro"); assertTrue(identidad.esAdmin(nuevo));
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertThrows(IllegalArgumentException.class, () -> identidad.validarNombre(nuevo, "otro"));
        assertThrows(IllegalArgumentException.class, () -> identidad.validarNombre(nuevo, "otro"));
    }
    /** [Doc]
     * Comprueba el escenario «administrador configurado puede cambiar nombre sin perder permisos» mediante las
     * aserciones del cuerpo.
     */
    @Test void administradorConfiguradoPuedeCambiarNombreSinPerderPermisos() {
        // [Doc] Variable local identidad (var; tipo inferido por Java): identidad. Se inicializa con new IdentidadService(mock(IUsuarioRepository.class), 700L).
        var identidad = new IdentidadService(mock(IUsuarioRepository.class), 700L); identidad.inicializar();
        // [Doc] Variable local admin (var; tipo inferido por Java): admin. Se inicializa con new Usuario().
        var admin = new Usuario(); admin.setId(700L); admin.setNombre_usuario("gestor");
        // [Doc] Verifica una expectativa de la prueba; una diferencia hace fallar el caso: assertTrue(identidad.esAdmin(admin));
        assertTrue(identidad.esAdmin(admin)); assertDoesNotThrow(() -> identidad.validarNombre(admin, "gestor"));
    }
}

package com.pequelandia.pequelandia.service;

import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.repository.IUsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    @Mock IUsuarioRepository usuarios;
    @InjectMocks UsuarioService servicio;

    @Test
    void ingresoCorrectoActualizaYGuardaLaUltimaConexion() {
        Usuario usuario = new Usuario();
        usuario.setUltima_conexion(new Date(0));
        when(usuarios.login("cuenta@example.invalid", "clave")).thenReturn(Optional.of(usuario));
        when(usuarios.save(usuario)).thenReturn(usuario);
        long inicio = System.currentTimeMillis();

        assertSame(usuario, servicio.login(" cuenta@example.invalid ", "clave").orElseThrow());

        assertTrue(usuario.getUltima_conexion().getTime() >= inicio);
        assertTrue(usuario.getUltima_conexion().getTime() <= System.currentTimeMillis());
        verify(usuarios).save(usuario);
    }

    @Test
    void ingresoFallidoNoGuardaNingunaConexion() {
        when(usuarios.login("cuenta@example.invalid", "incorrecta")).thenReturn(Optional.empty());

        assertTrue(servicio.login("cuenta@example.invalid", "incorrecta").isEmpty());

        verify(usuarios, never()).save(any(Usuario.class));
    }
}

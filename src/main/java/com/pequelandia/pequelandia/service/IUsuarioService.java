package com.pequelandia.pequelandia.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.dto.RegistroUsuarioDTO;
import com.pequelandia.pequelandia.model.Localidad;
import com.pequelandia.pequelandia.model.Usuario;
public interface IUsuarioService {

	//metodo para traer a todos los usuarios
	//lectura
	public List<Usuario> getUsuarios();
	
	//alta
	public void saveUsuario(Usuario usu);
	
	//baja
	public void deleteUsuario(Long id);
	
	//lectura de un solo usuario
	public Usuario findUsuario(Long id);
	
	//editar usuario
	public void editUsuario(Long id, Usuario usu);
		
	Page<Usuario> listar(String q, Pageable pageable);
       
	public Optional<Usuario> login(String nombreUsuario,String contraseña);

	boolean existeEmail(String email);

	boolean existeNombreUsuario(String nombreUsuario);

	boolean existeDni(int dni);

	Usuario registrar(RegistroUsuarioDTO dto, Localidad localidad);
	
}
package com.pequelandia.pequelandia.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.service.IUsuarioService;

@RestController
public class UsuarioController {

	@Autowired
	private IUsuarioService UsuarioServ;
	
	//probar traer todas los usuarios
	@GetMapping ("/usuarios/traer")
	public List<Usuario> getUsuarios(){
		return UsuarioServ.getUsuarios();
	}
	


}

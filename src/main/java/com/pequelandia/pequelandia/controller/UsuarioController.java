package com.pequelandia.pequelandia.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.service.IUsuarioService;

/** [Doc]
 * Expone la consulta de usuarios mediante el servicio de cuentas.
 */
@RestController
public class UsuarioController {

    // [Doc] Atributo UsuarioServ (IUsuarioService): colaborador que concentra operaciones de usuario.
	@Autowired
	private IUsuarioService UsuarioServ;
	
	//probar traer todas los usuarios
    /** [Doc]
     * Devuelve usuarios, según el cuerpo o la implementación del contrato.
     * @return List&lt;Usuario&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	@GetMapping ("/usuarios/traer")
	public List<Usuario> getUsuarios(){
        // [Doc] Termina el método o la función y devuelve UsuarioServ.getUsuarios().
		return UsuarioServ.getUsuarios();
	}
	


}

package com.pequelandia.pequelandia.service;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.pequelandia.pequelandia.dto.RegistroUsuarioDTO;
import com.pequelandia.pequelandia.model.Localidad;
import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.repository.IUsuarioRepository;

@Service
public class UsuarioService implements IUsuarioService{


	//generamos la dependenca "inyeccion de dependencia"
	@Autowired
	private IUsuarioRepository UsuarioRepo;

	
	@Override
	public List<Usuario> getUsuarios() {
		
		List<Usuario> listaUsuario=UsuarioRepo.findAll();
		return listaUsuario;	
	}

	@Override
	public void saveUsuario(Usuario usu) {
		UsuarioRepo.save(usu);
	}

	@Override
	public void deleteUsuario(Long id) {
		UsuarioRepo.deleteById(id);
	}

	@Override
	public Usuario findUsuario(Long id) {
		Usuario usuariobuscado=UsuarioRepo.findById(id).orElse(null);
		return usuariobuscado;
	}

	@Override
	public void editUsuario(Long id, Usuario cambios) {
		
	    Usuario db = findUsuario(id); // lanza si no existe

		 // Strings: solo si no vienen null/blank
	    setIfHasText(db::setNombre_usuario, cambios.getNombre_usuario());
	    setIfHasText(db::setApellido,        cambios.getApellido());
	    setIfHasText(db::setEmail,           cambios.getEmail());
	    setIfHasText(db::setTelefono, 		cambios.getTelefono());
	    setIfHasText(db::setDireccion,       cambios.getDireccion());
	    if (cambios.getDni() != 0) {
	        db.setDni(cambios.getDni());
	    }

	    
	    // usá null-check (o mantenelos comentados para no pisarlos).
	    setIfNotNull(db::setFecha_registro,   cambios.getFecha_registro());
	    setIfNotNull(db::setUltima_conexion,  cambios.getUltima_conexion());
	     setIfNotNull(db::setFecha_nacimiento, cambios.getFecha_nacimiento());
	    setIfHasText(db::setContraseña,       cambios.getContraseña());
	     setIfNotNull(db::setLocalidad,        cambios.getLocalidad());

	    UsuarioRepo.saveAndFlush(db);
	}

	

	/** Aplica setter solo si el valor NO es null. */
	private static <T> void setIfNotNull(Consumer<T> setter, T value) {
	    if (value != null) setter.accept(value);
	}

	/** Aplica setter para String solo si NO es null NI está en blanco. */
	private static void setIfHasText(Consumer<String> setter, String value) {
	    if (value != null && !value.trim().isEmpty()) setter.accept(value.trim());
	}

	


    @Override
    public Page<Usuario> listar(String q, Pageable pageable) {
    	 String term = (q == null ? "" : q.trim());

    	    Pageable safe = PageRequest.of(
    	        Math.max(pageable.getPageNumber(), 0),
    	        pageable.getPageSize() > 0 ? Math.min(pageable.getPageSize(), 100) : 10,
    	        Sort.by("id").ascending()
    	    );

    	    if (term.isEmpty()) {
    	        System.out.println("[usuarios] usando findAll");
    	        Page<Usuario> p = UsuarioRepo.findAll(safe);
    	        System.out.printf("[usuarios] total=%d, elems=%d%n",
    	                p.getTotalElements(), p.getNumberOfElements());
    	        return p;
    	    } else {
    	        System.out.println("[usuarios] usando buscar q='" + term + "'");
    	        Page<Usuario> p = UsuarioRepo.buscar(term, safe);
    	        System.out.printf("[usuarios] total=%d, elems=%d%n",
    	                p.getTotalElements(), p.getNumberOfElements());
    	        return p;
    	    }
}

  
   

    public Optional<Usuario> login(String nombreUsuario,String contraseña) {
        return UsuarioRepo.login(nombreUsuario, contraseña);
    }
    
    public boolean existeEmail(String email) {
        return UsuarioRepo.existeEmail(email.trim());
    }

    public boolean existeNombreUsuario(String nombreUsuario) {
        return UsuarioRepo.existeNombreUsuario(nombreUsuario.trim());
    }

    public boolean existeDni(int dni) {
        return UsuarioRepo.existeDni(dni);
    }
    

    

   
    public Usuario registrar(RegistroUsuarioDTO dto, Localidad localidad) {
        Usuario usuario = new Usuario();

        usuario.setNombre_usuario(dto.getNombre_usuario().trim());
        usuario.setApellido(dto.getApellido().trim());
        usuario.setEmail(dto.getEmail().trim().toLowerCase());
        usuario.setDni(dto.getDni());
        usuario.setTelefono(dto.getTelefono().trim());
        usuario.setDireccion(dto.getDireccion().trim());
        usuario.setFecha_nacimiento(dto.getFecha_nacimiento());
        usuario.setFecha_registro(new Date());
        usuario.setLocalidad(localidad);
        usuario.setContraseña(dto.getContraseña());
        usuario.setPuntos(0);

        return UsuarioRepo.save(usuario);
    }

	
}
		



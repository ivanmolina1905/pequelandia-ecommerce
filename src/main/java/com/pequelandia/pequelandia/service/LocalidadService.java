package com.pequelandia.pequelandia.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pequelandia.pequelandia.model.Localidad;
import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.repository.ILocalidadRepository;

/** [Doc]
 * Consulta las localidades usadas al registrar o editar una cuenta.
 */
@Service
public class LocalidadService implements ILocalidadService{
	
    // [Doc] Atributo localidadRepo (ILocalidadRepository): acceso de persistencia a localidad.
	@Autowired
	private ILocalidadRepository localidadRepo;
	
    /** [Doc]
     * Consulta persistencia según el criterio expresado por findLocalidad; la consulta declarada o la convención de
     * Spring Data determina el resultado.
     * @param id identificador del registro.
     * @return Localidad con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	@Override
	public Localidad findLocalidad(Long id) {
        // [Doc] Termina el método o la función y devuelve localidadRepo.findById(id) .orElseThrow(() -> new NoSuchElementException("Localidad no encontrada id=" + id)).
		return localidadRepo.findById(id) .orElseThrow(() -> new NoSuchElementException("Localidad no encontrada id=" + id));
}

    /** [Doc]
     * Devuelve todas, según el cuerpo o la implementación del contrato.
     * @return List&lt;Localidad&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	@Override
	
	public List<Localidad> getTodas() {
		
        // [Doc] Variable local listaLocalidad (List<Localidad>): lista localidad. Se inicializa con localidadRepo.findAll().
		List<Localidad> listaLocalidad=localidadRepo.findAll();
        // [Doc] Termina el método o la función y devuelve listaLocalidad.
		return listaLocalidad;
	}
	

}

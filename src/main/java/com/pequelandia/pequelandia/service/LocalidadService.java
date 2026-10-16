package com.pequelandia.pequelandia.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pequelandia.pequelandia.model.Localidad;
import com.pequelandia.pequelandia.model.Usuario;
import com.pequelandia.pequelandia.repository.ILocalidadRepository;

@Service
public class LocalidadService implements ILocalidadService{
	
	@Autowired
	private ILocalidadRepository localidadRepo;
	
	@Override
	public Localidad findLocalidad(Long id) {
		return localidadRepo.findById(id) .orElseThrow(() -> new NoSuchElementException("Localidad no encontrada id=" + id));
}

	@Override
	
	public List<Localidad> getTodas() {
		
		List<Localidad> listaLocalidad=localidadRepo.findAll();
		return listaLocalidad;
	}
	

}

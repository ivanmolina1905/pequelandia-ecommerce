package com.pequelandia.pequelandia.service;

import java.util.List;

import com.pequelandia.pequelandia.model.Localidad;
import com.pequelandia.pequelandia.model.Usuario;

public interface ILocalidadService {

	//buscar
	public Localidad findLocalidad(Long id);

	public List<Localidad> getTodas();
}

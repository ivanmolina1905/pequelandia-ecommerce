package com.pequelandia.pequelandia.service;

import java.util.List;

import com.pequelandia.pequelandia.model.Categoria;

public interface ICategoriaService {
	
	//buscar
	
	 	public List<Categoria> getCategorias();
	    public Categoria findById(Long id);
	    public List<Categoria> findAllById(List<Long> ids);
}

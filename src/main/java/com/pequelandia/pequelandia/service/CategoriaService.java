package com.pequelandia.pequelandia.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pequelandia.pequelandia.model.Categoria;
import com.pequelandia.pequelandia.repository.ICategoriaRepository;

@Service
public class CategoriaService implements ICategoriaService{

	@Autowired
	private ICategoriaRepository categoriaRepo;
	
	@Override
	public List<Categoria> getCategorias() {
		
		return categoriaRepo.findAll();	}
	@Override
	public Categoria findById(Long id) {
		return categoriaRepo.findById(id) .orElse(null);
		
	}

	@Override
    public List<Categoria> findAllById(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return categoriaRepo.findAllById(ids);
    }
    

}

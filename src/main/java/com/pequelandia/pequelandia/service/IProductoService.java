package com.pequelandia.pequelandia.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pequelandia.pequelandia.model.Producto;



public interface IProductoService {

	//metodo para traer a todos los usuarios
		//lectura
		public List<Producto> getProducto();
		
		//alta
		public void saveProducto(Producto produ);
		
		//baja
		public void deleteProducto(Long id);
		
		//lectura de un solo usuario
		public Producto findProducto(Long id);
		
		//editar usuario
		public void editProduto(Long id, Producto produ);
		
		Page<Producto> buscar(String q, Pageable pg);
		
		Page<Producto> buscarPublico(String q, Long categoriaId, Double precioMin, Double precioMax, String talle, Pageable pageable);
		List<Producto> obtenerAleatorios(int limite);

		
}

package com.pequelandia.pequelandia.service;

import java.util.List;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.pequelandia.pequelandia.model.Producto;
import com.pequelandia.pequelandia.repository.IProductoRepository;

@Service
public class ProductoService implements IProductoService{


	//generamos la dependencia
	@Autowired
	private IProductoRepository ProductoRepo;
	
	@Override
	public List<Producto> getProducto() {
		List<Producto> listaProducto=ProductoRepo.findAll();
		return listaProducto;
	}

	@Override
	public void saveProducto(Producto produ) {
		ProductoRepo.save(produ);
	}

	@Override
	public void deleteProducto(Long id) {
		ProductoRepo.deleteById(id);
	}

	@Override
	public Producto findProducto(Long id) {
		Producto productobuscado=ProductoRepo.findById(id).orElse(null);
		return productobuscado;
	}

	@Override
	public void editProduto(Long id, Producto produ) {
		   Producto db = findProducto(id);
	        if (db == null) return;

	        // Strings
	        setIfHasText(db::setNombre_producto, produ.getNombre_producto());
	        setIfHasText(db::setDescripcion_producto, produ.getDescripcion_producto());
	        setIfHasText(db::setMarca_producto, produ.getMarca_producto());
	        setIfHasText(db::setModelo_producto, produ.getModelo_producto());
	        setIfHasText(db::setTalle_producto, produ.getTalle_producto());
	        setIfHasText(db::setEstado, produ.getEstado());

	        // Números y fechas
	        setIfNotNull(db::setStock, produ.getStock());
	        setIfNotNull(db::setCosto, produ.getCosto());
	        setIfNotNull(db::setPrecio, produ.getPrecio());
	        setIfNotNull(db::setFecha_alta, produ.getFecha_alta());
	        setIfNotNull(db::setFecha_baja, produ.getFecha_baja());

	        // Categorías (solo si no viene null)
	        if (produ.getCategorias() != null) {
	            db.setCategorias(produ.getCategorias());
	        }

	        // Imagen
	        if (produ.getImagenUrl() != null && !produ.getImagenUrl().isBlank()) {
	            db.setImagenUrl(produ.getImagenUrl());
	        }

	        ProductoRepo.saveAndFlush(db);
	}
	
	 @Override
	    public Page<Producto> buscar(String q, Pageable pageable) {
	        return ProductoRepo.buscar(q, pageable);
	    }

	
	@Override
	public Page<Producto> buscarPublico(String q,
            Long categoriaId,
            Double precioMin,
            Double precioMax,
            String talle,
            Pageable pageable) {
	    String qNorm = (q == null) ? null : q.trim();
	    if (qNorm != null && qNorm.isEmpty()) qNorm = null;
	    
	    String talleNorm = (talle == null) ? null : talle.trim();
	    if (talleNorm != null && talleNorm.isEmpty()) talleNorm = null;

	    Long filtroCat = (categoriaId != null && categoriaId == 0) ? null : categoriaId;
	    
	    return ProductoRepo.buscarPublico(qNorm, filtroCat, precioMin, precioMax, talleNorm, pageable);
	}

	private static <T> void setIfNotNull(Consumer<T> setter, T value) {
	    if (value != null) setter.accept(value);
	}

	private static void setIfHasText(Consumer<String> setter, String value) {
	    if (value != null && !value.trim().isEmpty()) setter.accept(value.trim());
	}
	
	@Override
	public List<Producto> obtenerAleatorios(int limite) {
	    int lim = Math.max(1, Math.min(limite, 50));
	    return ProductoRepo.obtenerAleatorios(lim);
	}
}

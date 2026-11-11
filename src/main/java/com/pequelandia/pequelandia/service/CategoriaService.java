package com.pequelandia.pequelandia.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pequelandia.pequelandia.model.Categoria;
import com.pequelandia.pequelandia.repository.ICategoriaRepository;

/** [Doc]
 * Consulta categorías y resuelve selecciones por identificador.
 */
@Service
public class CategoriaService implements ICategoriaService{

    // [Doc] Atributo categoriaRepo (ICategoriaRepository): acceso de persistencia a categoria.
	@Autowired
	private ICategoriaRepository categoriaRepo;
	
    /** [Doc]
     * Devuelve categorías asociadas al producto, según el cuerpo o la implementación del contrato.
     * @return List&lt;Categoria&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	@Override
	public List<Categoria> getCategorias() {
		
        // [Doc] Termina el método o la función y devuelve categoriaRepo.findAll().
		return categoriaRepo.findAll();	}
    /** [Doc]
     * Consulta persistencia según el criterio expresado por findById; la consulta declarada o la convención de
     * Spring Data determina el resultado.
     * @param id identificador del registro.
     * @return Categoria con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	@Override
	public Categoria findById(Long id) {
        // [Doc] Termina el método o la función y devuelve categoriaRepo.findById(id) .orElse(null).
		return categoriaRepo.findById(id) .orElse(null);
		
	}

    /** [Doc]
     * Consulta persistencia según el criterio expresado por findAllById; la consulta declarada o la convención de
     * Spring Data determina el resultado.
     * @param ids identificadores devueltos por la búsqueda.
     * @return List&lt;Categoria&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	@Override
    public List<Categoria> findAllById(List<Long> ids) {
        // [Doc] Evalúa ids == null || ids.isEmpty(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
		if (ids == null || ids.isEmpty()) {
            // [Doc] Termina el método o la función y devuelve List.of().
            return List.of();
        }
        // [Doc] Termina el método o la función y devuelve categoriaRepo.findAllById(ids).
        return categoriaRepo.findAllById(ids);
    }
    

}

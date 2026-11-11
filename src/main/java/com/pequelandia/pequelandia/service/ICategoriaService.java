package com.pequelandia.pequelandia.service;

import java.util.List;

import com.pequelandia.pequelandia.model.Categoria;

/** [Doc]
 * Define el contrato de operaciones de categoria que implementa el servicio correspondiente.
 */
public interface ICategoriaService {
	
	//buscar
	
        /** [Doc]
         * Devuelve categorías asociadas al producto, según el cuerpo o la implementación del contrato.
         * @return List&lt;Categoria&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
	 	public List<Categoria> getCategorias();
        /** [Doc]
         * Consulta persistencia según el criterio expresado por findById; la consulta declarada o la convención de
         * Spring Data determina el resultado.
         * @param id identificador del registro.
         * @return Categoria con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
	    public Categoria findById(Long id);
        /** [Doc]
         * Consulta persistencia según el criterio expresado por findAllById; la consulta declarada o la convención de
         * Spring Data determina el resultado.
         * @param ids identificadores devueltos por la búsqueda.
         * @return List&lt;Categoria&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
	    public List<Categoria> findAllById(List<Long> ids);
}

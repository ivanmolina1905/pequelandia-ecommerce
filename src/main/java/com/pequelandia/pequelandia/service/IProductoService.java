package com.pequelandia.pequelandia.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pequelandia.pequelandia.model.Producto;



/** [Doc]
 * Define el contrato de operaciones de producto que implementa el servicio correspondiente.
 */
public interface IProductoService {

	//metodo para traer a todos los usuarios
		//lectura
        /** [Doc]
         * Devuelve producto, según el cuerpo o la implementación del contrato.
         * @return List&lt;Producto&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
		public List<Producto> getProducto();
		
		//alta
        /** [Doc]
         * Guarda producto en el ámbito de IProductoService.
         * @param produ producto procesado en este punto.
         */
		public void saveProducto(Producto produ);
		
		//baja
        /** [Doc]
         * Elimina producto en el ámbito de IProductoService.
         * @param id identificador del registro.
         */
		public void deleteProducto(Long id);
		
		//lectura de un solo usuario
        /** [Doc]
         * Consulta persistencia según el criterio expresado por findProducto; la consulta declarada o la convención de
         * Spring Data determina el resultado.
         * @param id identificador del registro.
         * @return Producto con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
		public Producto findProducto(Long id);
		
		//editar usuario
        /** [Doc]
         * Actualiza produto en el ámbito de IProductoService.
         * @param id identificador del registro.
         * @param produ producto procesado en este punto.
         */
		public void editProduto(Long id, Producto produ);
		
        /** [Doc]
         * Busca los datos de la operación en el ámbito de IProductoService.
         * @param q q.
         * @param pg pg.
         * @return Page&lt;Producto&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
		Page<Producto> buscar(String q, Pageable pg);
		
        /** [Doc]
         * Busca publico en el ámbito de IProductoService.
         * @param q q.
         * @param categoriaId categoria id.
         * @param precioMin precio min.
         * @param precioMax precio max.
         * @param talle talle.
         * @param pageable parámetros de paginación y orden.
         * @return Page&lt;Producto&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
		Page<Producto> buscarPublico(String q, Long categoriaId, Double precioMin, Double precioMax, String talle, Pageable pageable);
        /** [Doc]
         * Obtiene aleatorios en el ámbito de IProductoService.
         * @param limite limite.
         * @return List&lt;Producto&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
		List<Producto> obtenerAleatorios(int limite);

		
}

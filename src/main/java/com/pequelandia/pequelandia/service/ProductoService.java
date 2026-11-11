package com.pequelandia.pequelandia.service;

import java.util.List;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.pequelandia.pequelandia.model.Producto;
import com.pequelandia.pequelandia.repository.IProductoRepository;

/** [Doc]
 * Gestiona productos, búsquedas del catálogo, edición parcial y evaluación de alertas de stock.
 */
@Service
@org.springframework.transaction.annotation.Transactional
public class ProductoService implements IProductoService{


	//generamos la dependencia
    // [Doc] Atributo ProductoRepo (IProductoRepository): acceso de persistencia a producto.
	@Autowired
	private IProductoRepository ProductoRepo;
    // [Doc] Atributo alertaStock (AlertaStockService): colaborador que concentra operaciones de alerta stock.
    @Autowired private AlertaStockService alertaStock;
	
    /** [Doc]
     * Devuelve producto, según el cuerpo o la implementación del contrato.
     * @return List&lt;Producto&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	@Override
	public List<Producto> getProducto() {
        // [Doc] Variable local listaProducto (List<Producto>): lista producto. Se inicializa con ProductoRepo.findAll().
		List<Producto> listaProducto=ProductoRepo.findAll();
        // [Doc] Termina el método o la función y devuelve listaProducto.
		return listaProducto;
	}

    /** [Doc]
     * Guarda producto en el ámbito de ProductoService.
     * @param produ producto procesado en este punto.
     */
	@Override
	public void saveProducto(Producto produ) {
        // [Doc] Evalúa produ.getStock()<0. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
		validarImportesYStock(produ);
        // [Doc] Evalúa produ.getIdProducto()>0. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
        if (produ.getIdProducto()>0) ProductoRepo.bloquear(produ.getIdProducto()).orElseThrow();
        // [Doc] Solicita persistir la entidad; saveAndFlush también fuerza la sincronización: ProductoRepo.saveAndFlush(produ);
        ProductoRepo.saveAndFlush(produ);
        // [Doc] Ejecuta la llamada o modificación alertaStock.evaluar(produ); Sus efectos dependen del método invocado y del objeto receptor.
        alertaStock.evaluar(produ);
	}

    /** [Doc]
     * Elimina producto en el ámbito de ProductoService.
     * @param id identificador del registro.
     */
	@Override
	public void deleteProducto(Long id) {
        // [Doc] Ejecuta la llamada o modificación ProductoRepo.bloquear(id).orElseThrow(); Sus efectos dependen del método invocado y del objeto receptor.
		ProductoRepo.bloquear(id).orElseThrow();
        // [Doc] Ejecuta la llamada o modificación alertaStock.eliminar(id); Sus efectos dependen del método invocado y del objeto receptor.
        alertaStock.eliminar(id);
        // [Doc] Ejecuta la llamada o modificación ProductoRepo.deleteById(id); Sus efectos dependen del método invocado y del objeto receptor.
        ProductoRepo.deleteById(id);
	}

    /** [Doc]
     * Consulta persistencia según el criterio expresado por findProducto; la consulta declarada o la convención de
     * Spring Data determina el resultado.
     * @param id identificador del registro.
     * @return Producto con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	@Override
	public Producto findProducto(Long id) {
        // [Doc] Variable local productobuscado (Producto): producto procesado en este punto. Se inicializa con ProductoRepo.findById(id).orElse(null).
		Producto productobuscado=ProductoRepo.findById(id).orElse(null);
        // [Doc] Termina el método o la función y devuelve productobuscado.
		return productobuscado;
	}

    /** [Doc]
     * Actualiza produto en el ámbito de ProductoService.
     * @param id identificador del registro.
     * @param produ producto procesado en este punto.
     */
	@Override
	public void editProduto(Long id, Producto produ) {
           // [Doc] Variable local db (Producto): producto procesado en este punto. Se inicializa con ProductoRepo.bloquear(id).orElse(null).
		   Producto db = ProductoRepo.bloquear(id).orElse(null);
            // [Doc] Evalúa db == null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
	        if (db == null) return;

	        // Strings
            // [Doc] Ejecuta la llamada o modificación setIfHasText(db::setNombre_producto, produ.getNombre_producto()); Sus efectos dependen del método invocado y del objeto receptor.
	        setIfHasText(db::setNombre_producto, produ.getNombre_producto());
            // [Doc] Ejecuta la llamada o modificación setIfHasText(db::setDescripcion_producto, produ.getDescripcion_producto()); Sus efectos dependen del método invocado y del objeto receptor.
	        setIfHasText(db::setDescripcion_producto, produ.getDescripcion_producto());
            // [Doc] Ejecuta la llamada o modificación setIfHasText(db::setMarca_producto, produ.getMarca_producto()); Sus efectos dependen del método invocado y del objeto receptor.
	        setIfHasText(db::setMarca_producto, produ.getMarca_producto());
            // [Doc] Ejecuta la llamada o modificación setIfHasText(db::setModelo_producto, produ.getModelo_producto()); Sus efectos dependen del método invocado y del objeto receptor.
	        setIfHasText(db::setModelo_producto, produ.getModelo_producto());
            // [Doc] Ejecuta la llamada o modificación setIfHasText(db::setTalle_producto, produ.getTalle_producto()); Sus efectos dependen del método invocado y del objeto receptor.
	        setIfHasText(db::setTalle_producto, produ.getTalle_producto());
            // [Doc] Ejecuta la llamada o modificación setIfHasText(db::setEstado, produ.getEstado()); Sus efectos dependen del método invocado y del objeto receptor.
	        setIfHasText(db::setEstado, produ.getEstado());

	        // Números y fechas
            // [Doc] Ejecuta la llamada o modificación setIfNotNull(db::setStock, produ.getStock()); Sus efectos dependen del método invocado y del objeto receptor.
	        setIfNotNull(db::setStock, produ.getStock());
            // [Doc] Ejecuta la llamada o modificación setIfNotNull(db::setCosto, produ.getCosto()); Sus efectos dependen del método invocado y del objeto receptor.
	        setIfNotNull(db::setCosto, produ.getCosto());
            // [Doc] Ejecuta la llamada o modificación setIfNotNull(db::setPrecio, produ.getPrecio()); Sus efectos dependen del método invocado y del objeto receptor.
	        setIfNotNull(db::setPrecio, produ.getPrecio());
            // [Doc] Ejecuta la llamada o modificación setIfNotNull(db::setFecha_alta, produ.getFecha_alta()); Sus efectos dependen del método invocado y del objeto receptor.
	        setIfNotNull(db::setFecha_alta, produ.getFecha_alta());
            // [Doc] Ejecuta la llamada o modificación setIfNotNull(db::setFecha_baja, produ.getFecha_baja()); Sus efectos dependen del método invocado y del objeto receptor.
	        setIfNotNull(db::setFecha_baja, produ.getFecha_baja());

	        // Categorías (solo si no viene null)
            // [Doc] Evalúa produ.getCategorias() != null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
	        if (produ.getCategorias() != null) {
                // [Doc] Actualiza una propiedad o configuración mediante la llamada: db.setCategorias(produ.getCategorias());
	            db.setCategorias(produ.getCategorias());
	        }

	        // Imagen
            // [Doc] Evalúa produ.getImagenUrl() != null && !produ.getImagenUrl().isBlank(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
	        if (produ.getImagenUrl() != null && !produ.getImagenUrl().isBlank()) {
                // [Doc] Actualiza una propiedad o configuración mediante la llamada: db.setImagenUrl(produ.getImagenUrl());
	            db.setImagenUrl(produ.getImagenUrl());
	        }

            // [Doc] Evalúa db.getStock()<0. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
	        validarImportesYStock(db);
        // [Doc] Solicita persistir la entidad; saveAndFlush también fuerza la sincronización: ProductoRepo.saveAndFlush(db);
        ProductoRepo.saveAndFlush(db);
        // [Doc] Ejecuta la llamada o modificación alertaStock.evaluar(db); Sus efectos dependen del método invocado y del objeto receptor.
        alertaStock.evaluar(db);
	}
	
     /** [Doc]
      * Busca los datos de la operación en el ámbito de ProductoService.
      * @param q q.
      * @param pageable parámetros de paginación y orden.
      * @return Page&lt;Producto&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
      */
	 @Override
	    public Page<Producto> buscar(String q, Pageable pageable) {
            // [Doc] Termina el método o la función y devuelve ProductoRepo.buscar(q, pageable).
	        return ProductoRepo.buscar(q, pageable);
	    }

	
    /** [Doc]
     * Busca publico en el ámbito de ProductoService.
     * @param q q.
     * @param categoriaId categoria id.
     * @param precioMin precio min.
     * @param precioMax precio max.
     * @param talle talle.
     * @param pageable parámetros de paginación y orden.
     * @return Page&lt;Producto&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	@Override
	public Page<Producto> buscarPublico(String q,
            Long categoriaId,
            Double precioMin,
            Double precioMax,
            String talle,
            Pageable pageable) {
        // [Doc] Variable local qNorm (String): q norm. Se inicializa con (q == null) ? null : q.trim().
	    String qNorm = (q == null) ? null : q.trim();
        // [Doc] Evalúa qNorm != null && qNorm.isEmpty(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
	    if (qNorm != null && qNorm.isEmpty()) qNorm = null;
	    
        // [Doc] Variable local talleNorm (String): talle norm. Se inicializa con (talle == null) ? null : talle.trim().
	    String talleNorm = (talle == null) ? null : talle.trim();
        // [Doc] Evalúa talleNorm != null && talleNorm.isEmpty(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
	    if (talleNorm != null && talleNorm.isEmpty()) talleNorm = null;

        // [Doc] Variable local filtroCat (Long): filtro cat. Se inicializa con (categoriaId != null && categoriaId == 0) ? null : categoriaId.
	    Long filtroCat = (categoriaId != null && categoriaId == 0) ? null : categoriaId;
	    
        // [Doc] Termina el método o la función y devuelve ProductoRepo.buscarPublico(qNorm, filtroCat, precioMin, precioMax, talleNorm, pageable).
	    return ProductoRepo.buscarPublico(qNorm, filtroCat, precioMin, precioMax, talleNorm, pageable);
	}

    /** [Doc]
     * Ejecuta el setter recibido únicamente cuando el valor no es nulo.
     * @param setter setter.
     * @param value value.
     */
	private static <T> void setIfNotNull(Consumer<T> setter, T value) {
        // [Doc] Evalúa value != null. Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
	    if (value != null) setter.accept(value);
	}

    /** [Doc]
     * Ejecuta el setter recibido únicamente cuando el texto tiene contenido.
     * @param setter setter.
     * @param value value.
     */
	private static void setIfHasText(Consumer<String> setter, String value) {
        // [Doc] Evalúa value != null && !value.trim().isEmpty(). Si es verdadero, ejecuta su rama; de lo contrario continúa o ejecuta else.
	    if (value != null && !value.trim().isEmpty()) setter.accept(value.trim());
	}
	
    /** [Doc]
     * Obtiene aleatorios en el ámbito de ProductoService.
     * @param limite limite.
     * @return List&lt;Producto&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	@Override
	public List<Producto> obtenerAleatorios(int limite) {
        // [Doc] Variable local lim (int): lim. Se inicializa con Math.max(1, Math.min(limite, 50)).
	    int lim = Math.max(1, Math.min(limite, 50));
        // [Doc] Termina el método o la función y devuelve ProductoRepo.obtenerAleatorios(lim).
	    return ProductoRepo.obtenerAleatorios(lim);
	}
    private static void validarImportesYStock(Producto p) {
        if (p.getStock() < 0) throw new IllegalArgumentException("El stock no puede ser negativo");
        if (!Float.isFinite(p.getPrecio()) || p.getPrecio() < 0)
            throw new IllegalArgumentException("Precio inválido");
        if (!Float.isFinite(p.getCosto()) || p.getCosto() < 0)
            throw new IllegalArgumentException("Costo inválido");
    }
}

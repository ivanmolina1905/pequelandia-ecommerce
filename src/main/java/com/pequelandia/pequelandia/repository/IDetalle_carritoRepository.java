package com.pequelandia.pequelandia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pequelandia.pequelandia.model.Detalle_carrito;

/** [Doc]
 * Declara consultas y operaciones JPA de detalle carrito; Spring Data crea la implementación.
 */
public interface IDetalle_carritoRepository extends JpaRepository<Detalle_carrito, Long> {

	// ======================================================
    // ✔ JPQL: cargar todos los detalles con sus productos
    // ======================================================
    /** [Doc]
     * Consulta persistencia según el criterio expresado por findAllByCarrito; la consulta declarada o la convención
     * de Spring Data determina el resultado.
     * @param idCarrito identificador del carrito persistente.
     * @return List&lt;Detalle_carrito&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @EntityGraph(attributePaths = "producto")
    @Query("SELECT d FROM Detalle_carrito d WHERE d.carrito.idCarrito = :idCarrito")
    List<Detalle_carrito> findAllByCarrito(@Param("idCarrito") Long idCarrito);

    // ======================================================
    // ✔ Buscar detalle por carrito + producto (JPQL)
    // ======================================================
    /** [Doc]
     * Consulta persistencia según el criterio expresado por findByCarritoAndProducto; la consulta declarada o la
     * convención de Spring Data determina el resultado.
     * @param idDetalleCarrito id detalle carrito.
     * @param idProducto identificador del producto.
     * @return Optional&lt;Detalle_carrito&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @EntityGraph(attributePaths = "producto")
    @Query("""
        SELECT d 
        FROM Detalle_carrito d
        WHERE d.carrito.idCarrito = :idDetalleCarrito
          AND d.producto.idProducto = :idProducto
    """)
    Optional<Detalle_carrito> findByCarritoAndProducto(
            @Param("idDetalleCarrito") Long idDetalleCarrito,
            @Param("idProducto") Long idProducto
    );

    // ======================================================
    // ✔ Native: listar por carrito (rápido)
    // ======================================================
    /** [Doc]
     * Consulta persistencia según el criterio expresado por findAllByCarritoNative; la consulta declarada o la
     * convención de Spring Data determina el resultado.
     * @param idDetalleCarrito id detalle carrito.
     * @return List&lt;Detalle_carrito&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query(value = """
        SELECT d.*
        FROM detalle_carrito d
        WHERE d.id_carrito = :idDetalleCarrito
    """, nativeQuery = true)
    List<Detalle_carrito> findAllByCarritoNative(@Param("idDetalleCarrito") Long idDetalleCarrito);

    // ======================================================
    // ✔ Native: buscar detalle carrito + producto
    // ======================================================
    /** [Doc]
     * Consulta persistencia según el criterio expresado por findOneByCarritoAndProductoNative; la consulta declarada
     * o la convención de Spring Data determina el resultado.
     * @param idDetalleCarrito id detalle carrito.
     * @param idProducto identificador del producto.
     * @return Optional&lt;Detalle_carrito&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query(value = """
        SELECT d.*
        FROM detalle_carrito d
        WHERE d.id_carrito = :idDetalleCarrito
          AND d.id_producto = :idProducto
        LIMIT 1
    """, nativeQuery = true)
    Optional<Detalle_carrito> findOneByCarritoAndProductoNative(
        @Param("idDetalleCarrito") Long idDetalleCarrito,
        @Param("idProducto") Long idProducto
    );

    // ======================================================
    // ✔ Total carrito
    // ======================================================
    /** [Doc]
     * Resuelve la operación total carrito dentro de IDetalle_carritoRepository; el detalle de instrucciones muestra
     * sus pasos y llamadas.
     * @param idDetalleCarrito id detalle carrito.
     * @return Double con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("""
        SELECT SUM(d.cantidad * p.precio)
        FROM Detalle_carrito d
        JOIN d.producto p
        WHERE d.carrito.idCarrito = :idDetalleCarrito
    """)
    Double totalCarrito(@Param("idDetalleCarrito") Long idDetalleCarrito);

    // ======================================================
    // ✔ Estadística: productos más vendidos
    // ======================================================
    /** [Doc]
     * Resuelve la operación top productos vendidos dentro de IDetalle_carritoRepository; el detalle de instrucciones
     * muestra sus pasos y llamadas.
     * @return List&lt;Object[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("""
        SELECT d.producto.idProducto,
               d.producto.nombre_producto,
               SUM(d.cantidad)
        FROM Detalle_carrito d
        GROUP BY d.producto.idProducto, d.producto.nombre_producto
        ORDER BY SUM(d.cantidad) DESC
    """)
    List<Object[]> topProductosVendidos();

    // ======================================================
    // ✔ Eliminar por carrito
    // ======================================================
    /** [Doc]
     * Elimina by carrito id carrito en el ámbito de IDetalle_carritoRepository.
     * @param idCarrito identificador del carrito persistente.
     */
    void deleteByCarritoIdCarrito(Long idCarrito);
    

    /** [Doc]
     * Consulta persistencia según el criterio expresado por findByCarrito_IdCarrito; la consulta declarada o la
     * convención de Spring Data determina el resultado.
     * @param idCarrito identificador del carrito persistente.
     * @return List&lt;Detalle_carrito&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @EntityGraph(attributePaths = "producto")
    List<Detalle_carrito> findByCarrito_IdCarrito(Long idCarrito);

    /** [Doc]
     * Consulta persistencia según el criterio expresado por findByCarrito_IdCarritoAndProducto_IdProducto; la
     * consulta declarada o la convención de Spring Data determina el resultado.
     * @param idCarrito identificador del carrito persistente.
     * @param idProducto identificador del producto.
     * @return Optional&lt;Detalle_carrito&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @EntityGraph(attributePaths = "producto")
    Optional<Detalle_carrito> findByCarrito_IdCarritoAndProducto_IdProducto(Long idCarrito, Long idProducto);

    /** [Doc]
     * Resuelve la operación items carrito dentro de IDetalle_carritoRepository; el detalle de instrucciones muestra
     * sus pasos y llamadas.
     * @param idCarrito identificador del carrito persistente.
     * @return Long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("""
        SELECT COALESCE(SUM(d.cantidad), 0)
        FROM Detalle_carrito d
        WHERE d.carrito.idCarrito = :idCarrito
    """)
    Long itemsCarrito(@Param("idCarrito") Long idCarrito);
}


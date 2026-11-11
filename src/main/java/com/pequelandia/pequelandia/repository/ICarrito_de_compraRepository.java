package com.pequelandia.pequelandia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pequelandia.pequelandia.model.Carrito_de_compra;
import com.pequelandia.pequelandia.model.Detalle_carrito;
import com.pequelandia.pequelandia.model.Usuario;

/** [Doc]
 * Declara consultas y operaciones JPA de carrito de compra; Spring Data crea la implementación.
 */
public interface ICarrito_de_compraRepository extends JpaRepository<Carrito_de_compra, Long> {

	 // ------------------------ CARRITO ACTIVO ------------------------
    /** [Doc]
     * Consulta persistencia según el criterio expresado por findCarritoActivo; la consulta declarada o la convención
     * de Spring Data determina el resultado.
     * @param idUsuario identificador del usuario.
     * @return Optional&lt;Carrito_de_compra&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("""
        SELECT c
        FROM Carrito_de_compra c
        WHERE c.usuario.id = :idUsuario
          AND c.pedido IS NULL
    """)
    Optional<Carrito_de_compra> findCarritoActivo(@Param("idUsuario") Long idUsuario);


    // ------------------------ ÚLTIMO CARRITO ------------------------
    /** [Doc]
     * Consulta persistencia según el criterio expresado por findUltimoCarrito; la consulta declarada o la convención
     * de Spring Data determina el resultado.
     * @param idUsuario identificador del usuario.
     * @return Optional&lt;Carrito_de_compra&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("""
        SELECT c
        FROM Carrito_de_compra c
        WHERE c.usuario.id = :idUsuario
        ORDER BY c.idCarrito DESC
    """)
    Optional<Carrito_de_compra> findUltimoCarrito(@Param("idUsuario") Long idUsuario);


    // ------------------------ CARRITO TIENE PEDIDO ------------------------
    /** [Doc]
     * Resuelve la operación carrito tiene pedido dentro de ICarrito_de_compraRepository; el detalle de instrucciones
     * muestra sus pasos y llamadas.
     * @param idCarrito identificador del carrito persistente.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("""
        SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END
        FROM Carrito_de_compra c
        WHERE c.idCarrito = :idCarrito
          AND c.pedido IS NOT NULL
    """)
    boolean carritoTienePedido(@Param("idCarrito") Long idCarrito);


    // ------------------------ CALCULAR TOTAL ------------------------
    /** [Doc]
     * Calcula total en el ámbito de ICarrito_de_compraRepository.
     * @param idCarrito identificador del carrito persistente.
     * @return Double con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("""
        SELECT SUM(d.cantidad * p.precio)
        FROM Carrito_de_compra c
        JOIN c.detalles d
        JOIN d.producto p
        WHERE c.idCarrito = :idCarrito
    """)
    Double calcularTotal(@Param("idCarrito") Long idCarrito);


    // ------------------------ CARRITOS CON STOCK BAJO ------------------------
    /** [Doc]
     * Resuelve la operación carritos con stock bajo dentro de ICarrito_de_compraRepository; el detalle de
     * instrucciones muestra sus pasos y llamadas.
     * @param minimo umbral de unidades que activa el aviso de stock bajo.
     * @return java.util.List&lt;Long&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("""
        SELECT c.idCarrito
        FROM Carrito_de_compra c
        JOIN c.detalles d
        JOIN d.producto p
        WHERE p.stock < :minimo
    """)
    java.util.List<Long> carritosConStockBajo(@Param("minimo") int minimo);


    /** [Doc]
     * Consulta persistencia según el criterio expresado por findAllByCarritoNative; la consulta declarada o la
     * convención de Spring Data determina el resultado.
     * @param idCarrito identificador del carrito persistente.
     * @return List&lt;Detalle_carrito&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query(value = """
            SELECT d.*
            FROM detalle_carrito d
            WHERE d.id_carrito = :idCarrito
        """, nativeQuery = true)
        List<Detalle_carrito> findAllByCarritoNative(@Param("idCarrito") Long idCarrito);
        
        
    // ------------------------ CARGAR CARRITO + DETALLES ------------------------
    /** [Doc]
     * Consulta persistencia según el criterio expresado por findById; la consulta declarada o la convención de
     * Spring Data determina el resultado.
     * @param id identificador del registro.
     * @return Optional&lt;Carrito_de_compra&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Override
    @EntityGraph(attributePaths = {"detalles", "detalles.producto"})
    Optional<Carrito_de_compra> findById(Long id);
    
    /** [Doc]
     * Consulta persistencia según el criterio expresado por findByUsuario; la consulta declarada o la convención de
     * Spring Data determina el resultado.
     * @param usuario cuenta de usuario utilizada por la operación.
     * @return Optional&lt;Carrito_de_compra&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    Optional<Carrito_de_compra> findByUsuario(Usuario usuario);

    /** [Doc]
     * Resuelve la operación bloquear dentro de ICarrito_de_compraRepository; el detalle de instrucciones muestra sus
     * pasos y llamadas.
     * @param id identificador del registro.
     * @return java.util.Optional&lt;Carrito_de_compra&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Carrito_de_compra e where e.idCarrito = :id")
    java.util.Optional<Carrito_de_compra> bloquear(@Param("id") Long id);

    /** [Doc]
     * Consulta persistencia según el criterio expresado por findFirstByUsuarioAndPedidoIsNullOrderByIdCarritoDesc;
     * la consulta declarada o la convención de Spring Data determina el resultado.
     * @param usuario cuenta de usuario utilizada por la operación.
     * @return Optional&lt;Carrito_de_compra&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    Optional<Carrito_de_compra> findFirstByUsuarioAndPedidoIsNullOrderByIdCarritoDesc(Usuario usuario);
}
package com.pequelandia.pequelandia.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pequelandia.pequelandia.model.Comprobante;

/** [Doc]
 * Declara consultas y operaciones JPA de comprobante; Spring Data crea la implementación.
 */
public interface IComprobanteRepository extends JpaRepository<Comprobante, Long> {

    // ✔ Para saber si un pedido ya tiene comprobante (evita duplicados)
    /** [Doc]
     * Consulta persistencia según el criterio expresado por existsByPedidoId; la consulta declarada o la convención
     * de Spring Data determina el resultado.
     * @param id identificador del registro.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("""
        SELECT (COUNT(c) > 0)
        FROM Comprobante c
        WHERE c.pedido.id_pedido = :id
    """)
    boolean existsByPedidoId(@Param("id") Long id);

    // ✔ Traer comprobante por pedido (útil en el panel admin)
    /** [Doc]
     * Consulta persistencia según el criterio expresado por findByPedidoId; la consulta declarada o la convención de
     * Spring Data determina el resultado.
     * @param id identificador del registro.
     * @return Optional&lt;Comprobante&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("""
            SELECT c
            FROM Comprobante c
            WHERE c.pedido.id_pedido = :id
        """)
        Optional<Comprobante> findByPedidoId(@Param("id") Long id);

    // ✔ Obtener el último número de comprobante (para auto-numerar)
    /** [Doc]
     * Obtiene ultimo numero en el ámbito de IComprobanteRepository.
     * @return Integer con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("""
        SELECT MAX(c.n_comprobante)
        FROM Comprobante c
    """)
    Integer obtenerUltimoNumero();

    // ✔ Total vendido por día
    /** [Doc]
     * Resuelve la operación total por dia dentro de IComprobanteRepository; el detalle de instrucciones muestra sus
     * pasos y llamadas.
     * @param dia dia.
     * @return Double con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("""
        SELECT SUM(c.total_venta)
        FROM Comprobante c
        WHERE DATE(c.fecha_venta) = :dia
    """)
    Double totalPorDia(@Param("dia") LocalDate dia);

    // ✔ Totales por mes
    /** [Doc]
     * Resuelve la operación total por mes dentro de IComprobanteRepository; el detalle de instrucciones muestra sus
     * pasos y llamadas.
     * @param anio año de la consulta.
     * @param mes mes de la consulta.
     * @return Double con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("""
        SELECT SUM(c.total_venta)
        FROM Comprobante c
        WHERE EXTRACT(YEAR FROM c.fecha_venta) = :anio
        AND EXTRACT(MONTH FROM c.fecha_venta) = :mes
    """)
    Double totalPorMes(@Param("anio") int anio, @Param("mes") int mes);

    // ✔ Totales por año
    /** [Doc]
     * Resuelve la operación total por anio dentro de IComprobanteRepository; el detalle de instrucciones muestra sus
     * pasos y llamadas.
     * @return List&lt;Object[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("""
        SELECT EXTRACT(YEAR FROM c.fecha_venta), SUM(c.total_venta)
        FROM Comprobante c
        GROUP BY EXTRACT(YEAR FROM c.fecha_venta)
        ORDER BY EXTRACT(YEAR FROM c.fecha_venta)
    """)
    List<Object[]> totalPorAnio();
}

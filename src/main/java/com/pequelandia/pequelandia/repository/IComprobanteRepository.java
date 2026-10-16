package com.pequelandia.pequelandia.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pequelandia.pequelandia.model.Comprobante;

public interface IComprobanteRepository extends JpaRepository<Comprobante, Long> {

    // ✔ Para saber si un pedido ya tiene comprobante (evita duplicados)
    @Query("""
        SELECT (COUNT(c) > 0)
        FROM Comprobante c
        WHERE c.pedido.id_pedido = :id
    """)
    boolean existsByPedidoId(@Param("id") Long id);

    // ✔ Traer comprobante por pedido (útil en el panel admin)
    @Query("""
            SELECT c
            FROM Comprobante c
            WHERE c.pedido.id_pedido = :id
        """)
        Optional<Comprobante> findByPedidoId(@Param("id") Long id);

    // ✔ Obtener el último número de comprobante (para auto-numerar)
    @Query("""
        SELECT MAX(c.n_comprobante)
        FROM Comprobante c
    """)
    Integer obtenerUltimoNumero();

    // ✔ Total vendido por día
    @Query("""
        SELECT SUM(c.total_venta)
        FROM Comprobante c
        WHERE DATE(c.fecha_venta) = :dia
    """)
    Double totalPorDia(@Param("dia") LocalDate dia);

    // ✔ Totales por mes
    @Query("""
        SELECT SUM(c.total_venta)
        FROM Comprobante c
        WHERE EXTRACT(YEAR FROM c.fecha_venta) = :anio
        AND EXTRACT(MONTH FROM c.fecha_venta) = :mes
    """)
    Double totalPorMes(@Param("anio") int anio, @Param("mes") int mes);

    // ✔ Totales por año
    @Query("""
        SELECT EXTRACT(YEAR FROM c.fecha_venta), SUM(c.total_venta)
        FROM Comprobante c
        GROUP BY EXTRACT(YEAR FROM c.fecha_venta)
        ORDER BY EXTRACT(YEAR FROM c.fecha_venta)
    """)
    List<Object[]> totalPorAnio();
}

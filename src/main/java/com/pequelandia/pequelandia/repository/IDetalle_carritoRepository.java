package com.pequelandia.pequelandia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pequelandia.pequelandia.model.Detalle_carrito;

public interface IDetalle_carritoRepository extends JpaRepository<Detalle_carrito, Long> {

	// ======================================================
    // ✔ JPQL: cargar todos los detalles con sus productos
    // ======================================================
    @EntityGraph(attributePaths = "producto")
    @Query("SELECT d FROM Detalle_carrito d WHERE d.carrito.idCarrito = :idCarrito")
    List<Detalle_carrito> findAllByCarrito(@Param("idDetalleCarrito") Long idCarrito);

    // ======================================================
    // ✔ Buscar detalle por carrito + producto (JPQL)
    // ======================================================
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
    @Query(value = """
        SELECT d.*
        FROM detalle_carrito d
        WHERE d.id_detalle_carrito = :idDetalleCarrito
    """, nativeQuery = true)
    List<Detalle_carrito> findAllByCarritoNative(@Param("idDetalleCarrito") Long idDetalleCarrito);

    // ======================================================
    // ✔ Native: buscar detalle carrito + producto
    // ======================================================
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
    void deleteByCarritoIdCarrito(Long idCarrito);
    

    @EntityGraph(attributePaths = "producto")
    List<Detalle_carrito> findByCarrito_IdCarrito(Long idCarrito);

    @EntityGraph(attributePaths = "producto")
    Optional<Detalle_carrito> findByCarrito_IdCarritoAndProducto_IdProducto(Long idCarrito, Long idProducto);

    @Query("""
        SELECT COALESCE(SUM(d.cantidad), 0)
        FROM Detalle_carrito d
        WHERE d.carrito.idCarrito = :idCarrito
    """)
    Long itemsCarrito(@Param("idCarrito") Long idCarrito);
}


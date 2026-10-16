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

public interface ICarrito_de_compraRepository extends JpaRepository<Carrito_de_compra, Long> {

	 // ------------------------ CARRITO ACTIVO ------------------------
    @Query("""
        SELECT c
        FROM Carrito_de_compra c
        WHERE c.usuario.id = :idUsuario
          AND c.pedido IS NULL
    """)
    Optional<Carrito_de_compra> findCarritoActivo(@Param("idUsuario") Long idUsuario);


    // ------------------------ ÚLTIMO CARRITO ------------------------
    @Query("""
        SELECT c
        FROM Carrito_de_compra c
        WHERE c.usuario.id = :idUsuario
        ORDER BY c.idCarrito DESC
    """)
    Optional<Carrito_de_compra> findUltimoCarrito(@Param("idUsuario") Long idUsuario);


    // ------------------------ CARRITO TIENE PEDIDO ------------------------
    @Query("""
        SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END
        FROM Carrito_de_compra c
        WHERE c.idCarrito = :idCarrito
          AND c.pedido IS NOT NULL
    """)
    boolean carritoTienePedido(@Param("idCarrito") Long idCarrito);


    // ------------------------ CALCULAR TOTAL ------------------------
    @Query("""
        SELECT SUM(d.cantidad * p.precio)
        FROM Carrito_de_compra c
        JOIN c.detalles d
        JOIN d.producto p
        WHERE c.idCarrito = :idCarrito
    """)
    Double calcularTotal(@Param("idCarrito") Long idCarrito);


    // ------------------------ CARRITOS CON STOCK BAJO ------------------------
    @Query("""
        SELECT c.idCarrito
        FROM Carrito_de_compra c
        JOIN c.detalles d
        JOIN d.producto p
        WHERE p.stock < :minimo
    """)
    java.util.List<Long> carritosConStockBajo(@Param("minimo") int minimo);


    @Query(value = """
            SELECT d.*
            FROM detalle_carrito d
            WHERE d.id_carrito = :idCarrito
        """, nativeQuery = true)
        List<Detalle_carrito> findAllByCarritoNative(@Param("idCarrito") Long idCarrito);
        
        
    // ------------------------ CARGAR CARRITO + DETALLES ------------------------
    @Override
    @EntityGraph(attributePaths = {"detalles", "detalles.producto"})
    Optional<Carrito_de_compra> findById(Long id);
    
    Optional<Carrito_de_compra> findByUsuario(Usuario usuario);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Carrito_de_compra e where e.idCarrito = :id")
    java.util.Optional<Carrito_de_compra> bloquear(@Param("id") Long id);

    Optional<Carrito_de_compra> findFirstByUsuarioAndPedidoIsNullOrderByIdCarritoDesc(Usuario usuario);
}
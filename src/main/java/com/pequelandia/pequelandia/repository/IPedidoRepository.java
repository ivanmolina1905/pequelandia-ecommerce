package com.pequelandia.pequelandia.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pequelandia.pequelandia.model.Pedido;



public interface IPedidoRepository extends JpaRepository<Pedido,Long> {
	
	@Query("select (count(c) > 0) from Comprobante c where c.pedido.id_pedido = :id")
	boolean existsByPedidoId(@Param("id") Long id);







    @Query("SELECT COUNT(p) FROM Pedido p")
    Long contarPedidos();

 // ======================================================
    // ✔ Ventas del día (NATIVO porque usás Date)
    // ======================================================
    @Query(value = """
        SELECT COALESCE(SUM(d.cantidad * prod.precio), 0)
        FROM pedido p
        JOIN carrito_de_compra c ON c.id_carrito = p.id_carrito
        JOIN detalle_carrito d   ON d.id_carrito = c.id_carrito
        JOIN producto prod       ON prod.id_producto = d.id_producto
        WHERE DATE(p.fecha_pedido) = :fecha
    """, nativeQuery = true)
    Double ventasDelDia(@Param("fecha") LocalDate fecha);


    // ======================================================
    // ✔ Ventas del mes (NATIVO)
    // ======================================================
    @Query(value = """
        SELECT COALESCE(SUM(d.cantidad * prod.precio), 0)
        FROM pedido p
        JOIN carrito_de_compra c ON c.id_carrito = p.id_carrito
        JOIN detalle_carrito d   ON d.id_carrito = c.id_carrito
        JOIN producto prod       ON prod.id_producto = d.id_producto
        WHERE EXTRACT(YEAR FROM p.fecha_pedido)  = :anio
        AND   EXTRACT(MONTH FROM p.fecha_pedido) = :mes
    """, nativeQuery = true)
    Double ventasDelMes(@Param("anio") int anio, @Param("mes") int mes);


    // ======================================================
    // ✔ Serie diaria
    // ======================================================
    @Query(value = """
        SELECT DATE(p.fecha_pedido) AS dia,
               COALESCE(SUM(d.cantidad * prod.precio), 0) AS total
        FROM pedido p
        JOIN carrito_de_compra c ON c.id_carrito = p.id_carrito
        JOIN detalle_carrito d   ON d.id_carrito = c.id_carrito
        JOIN producto prod       ON prod.id_producto = d.id_producto
        WHERE DATE(p.fecha_pedido) BETWEEN :desde AND :hasta
        GROUP BY DATE(p.fecha_pedido)
        ORDER BY dia
    """, nativeQuery = true)
    List<Object[]> ventasPorDia(
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);


    // ======================================================
    // ✔ Serie mensual
    // ======================================================
    @Query(value = """
        SELECT EXTRACT(MONTH FROM p.fecha_pedido) AS mes,
               COALESCE(SUM(d.cantidad * prod.precio), 0) AS total
        FROM pedido p
        JOIN carrito_de_compra c ON c.id_carrito = p.id_carrito
        JOIN detalle_carrito d   ON d.id_carrito = c.id_carrito
        JOIN producto prod       ON prod.id_producto = d.id_producto
        WHERE EXTRACT(YEAR FROM p.fecha_pedido) = :anio
        GROUP BY mes
        ORDER BY mes
    """, nativeQuery = true)
    List<Object[]> ventasPorMes(@Param("anio") int anio);


    // ======================================================
    // ✔ Serie anual
    // ======================================================
    @Query(value = """
        SELECT EXTRACT(YEAR FROM p.fecha_pedido) AS anio,
               COALESCE(SUM(d.cantidad * prod.precio), 0) AS total
        FROM pedido p
        JOIN carrito_de_compra c ON c.id_carrito = p.id_carrito
        JOIN detalle_carrito d   ON d.id_carrito = c.id_carrito
        JOIN producto prod       ON prod.id_producto = d.id_producto
        WHERE EXTRACT(YEAR FROM p.fecha_pedido) BETWEEN :inicio AND :fin
        GROUP BY anio
        ORDER BY anio
    """, nativeQuery = true)
    List<Object[]> ventasPorAnio(
            @Param("inicio") int inicio,
            @Param("fin") int fin);


    // ======================================================
    // ✔ Top productos más vendidos
    // ======================================================
    @Query(value = """
        SELECT prod.id_producto, prod.nombre_producto, SUM(d.cantidad) AS total
        FROM detalle_carrito d
        JOIN producto prod ON prod.id_producto = d.id_producto
        GROUP BY prod.id_producto, prod.nombre_producto
        ORDER BY total DESC
        LIMIT 10
    """, nativeQuery = true)
    List<Object[]> productosMasVendidos();



    @Query("""
    	    SELECT COUNT(p)
    	    FROM Pedido p
    	    WHERE p.estado_pedido = :estado
    	""")


	long countByEstadoPedido(@Param("estado") String string);
    
    List<Pedido> findByUsuario_Id(Long idUsuario);
    
    @Query("""
    		  SELECT p
    		  FROM Pedido p
    		  WHERE p.usuario.id = :usuarioId
    		  ORDER BY p.fecha_pedido DESC
    		""")
    		List<Pedido> listarPorUsuarioOrdenados(@Param("usuarioId") Long usuarioId);
    
    @Query("SELECT p FROM Pedido p ORDER BY p.fecha_pedido DESC")
    List<Pedido> listarTodosOrdenados();

    @Query("""
       SELECT p
       FROM Pedido p
       WHERE UPPER(p.estado_pedido) = UPPER(:estado)
       ORDER BY p.fecha_pedido DESC
    """)
    List<Pedido> listarPorEstadoOrdenados(@Param("estado") String estado);

    
    
    
    @Query("SELECT p FROM Pedido p ORDER BY p.fecha_pedido DESC")
    Page<Pedido> listarTodosOrdenados(Pageable pageable);

    @Query("""
       SELECT p
       FROM Pedido p
       WHERE UPPER(p.estado_pedido) = UPPER(:estado)
       ORDER BY p.fecha_pedido DESC
    """)
    Page<Pedido> listarPorEstadoOrdenados(@Param("estado") String estado, Pageable pageable);

//querts para panel de usuario
 // Total gastado por usuario (usa tablas reales)
    @Query(value = """
        SELECT COALESCE(SUM(d.cantidad * prod.precio), 0)
        FROM pedido p
        JOIN carrito_de_compra c ON c.id_carrito = p.id_carrito
        JOIN detalle_carrito d   ON d.id_carrito = c.id_carrito
        JOIN producto prod       ON prod.id_producto = d.id_producto
        WHERE p.id_usuario = :usuarioId
    """, nativeQuery = true)
    Double totalGastadoPorUsuario(@Param("usuarioId") Long usuarioId);

    // Total productos comprados (sumatoria de cantidades)
    @Query(value = """
        SELECT COALESCE(SUM(d.cantidad), 0)
        FROM pedido p
        JOIN carrito_de_compra c ON c.id_carrito = p.id_carrito
        JOIN detalle_carrito d   ON d.id_carrito = c.id_carrito
        WHERE p.id_usuario = :usuarioId
    """, nativeQuery = true)
    Long totalProductosComprados(@Param("usuarioId") Long usuarioId);

    // Total pedidos del usuario
    long countByUsuario_Id(Long usuarioId);

    // Serie mensual de compras del usuario (para gráfico)
    @Query(value = """
        SELECT EXTRACT(MONTH FROM p.fecha_pedido) AS mes,
               COALESCE(SUM(d.cantidad * prod.precio), 0) AS total
        FROM pedido p
        JOIN carrito_de_compra c ON c.id_carrito = p.id_carrito
        JOIN detalle_carrito d   ON d.id_carrito = c.id_carrito
        JOIN producto prod       ON prod.id_producto = d.id_producto
        WHERE EXTRACT(YEAR FROM p.fecha_pedido) = :anio
          AND p.id_usuario = :usuarioId
        GROUP BY mes
        ORDER BY mes
    """, nativeQuery = true)
    List<Object[]> comprasPorMes(@Param("usuarioId") Long usuarioId, @Param("anio") int anio);

    // Historial paginado por usuario
    
    
    @Query("SELECT p FROM Pedido p WHERE p.usuario.id = :id ORDER BY p.fecha_pedido DESC")
    Page<Pedido> findByUsuarioId(@Param("id") Long id, Pageable pageable);
    
 // ======================================================
 // LISTADO ADMIN DE PEDIDOS CON FILTROS COMBINADOS
 // ======================================================

 @Query(
     value = """
         SELECT p.*
         FROM pedido p
         LEFT JOIN usuario u ON u.id_usuario = p.id_usuario
         WHERE (
             :estado IS NULL
             OR :estado = ''
             OR UPPER(p.estado_pedido) = UPPER(:estado)
         )
         AND (
             :desde IS NULL
             OR DATE(p.fecha_pedido) >= :desde
         )
         AND (
             :hasta IS NULL
             OR DATE(p.fecha_pedido) <= :hasta
         )
         AND (
             :usuario IS NULL
             OR :usuario = ''
             OR LOWER(COALESCE(u.nombre_usuario, '')) LIKE LOWER(CONCAT('%', :usuario, '%'))
             OR LOWER(COALESCE(u.apellido, ''))       LIKE LOWER(CONCAT('%', :usuario, '%'))
             OR LOWER(COALESCE(u.email, ''))          LIKE LOWER(CONCAT('%', :usuario, '%'))
             OR CAST(COALESCE(u.dni, 0) AS TEXT)      LIKE CONCAT('%', :usuario, '%')
         )
         ORDER BY p.fecha_pedido DESC, p.id_pedido DESC
     """,
     countQuery = """
         SELECT COUNT(*)
         FROM pedido p
         LEFT JOIN usuario u ON u.id_usuario = p.id_usuario
         WHERE (
             :estado IS NULL
             OR :estado = ''
             OR UPPER(p.estado_pedido) = UPPER(:estado)
         )
         AND (
             :desde IS NULL
             OR DATE(p.fecha_pedido) >= :desde
         )
         AND (
             :hasta IS NULL
             OR DATE(p.fecha_pedido) <= :hasta
         )
         AND (
             :usuario IS NULL
             OR :usuario = ''
             OR LOWER(COALESCE(u.nombre_usuario, '')) LIKE LOWER(CONCAT('%', :usuario, '%'))
             OR LOWER(COALESCE(u.apellido, ''))       LIKE LOWER(CONCAT('%', :usuario, '%'))
             OR LOWER(COALESCE(u.email, ''))          LIKE LOWER(CONCAT('%', :usuario, '%'))
             OR CAST(COALESCE(u.dni, 0) AS TEXT)      LIKE CONCAT('%', :usuario, '%')
         )
     """,
     nativeQuery = true
 )
 Page<Pedido> filtrarPedidosAdmin(
         @Param("estado") String estado,
         @Param("desde") LocalDate desde,
         @Param("hasta") LocalDate hasta,
         @Param("usuario") String usuario,
         Pageable pageable
 );
 
//======================================================
//RESUMEN DE PEDIDOS PARA EL PANEL ADMIN
//======================================================

@Query(value = """
  SELECT COUNT(*)
  FROM pedido p
  WHERE DATE(p.fecha_pedido) = :fecha
""", nativeQuery = true)
long contarPedidosDelDia(@Param("fecha") LocalDate fecha);


@Query("""
  SELECT COUNT(p)
  FROM Pedido p
  WHERE UPPER(p.estado_pedido) = UPPER(:estado)
""")
long contarPorEstadoSinDistinguirMayusculas(
      @Param("estado") String estado
);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Pedido e where e.id_pedido = :id")
    java.util.Optional<Pedido> bloquear(@Param("id") Long id);


    @Query("select p from Pedido p where p.carrito.idCarrito = :id")
    java.util.Optional<Pedido> buscarPorCarrito(@Param("id") Long id);

}


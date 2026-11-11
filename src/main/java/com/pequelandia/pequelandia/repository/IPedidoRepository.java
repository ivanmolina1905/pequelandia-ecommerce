package com.pequelandia.pequelandia.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pequelandia.pequelandia.model.Pedido;



/** [Doc]
 * Declara consultas y operaciones JPA de pedido; Spring Data crea la implementación.
 */
public interface IPedidoRepository extends JpaRepository<Pedido,Long> {
	
    /** [Doc]
     * Consulta persistencia según el criterio expresado por existsByPedidoId; la consulta declarada o la convención
     * de Spring Data determina el resultado.
     * @param id identificador del registro.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	@Query("select (count(c) > 0) from Comprobante c where c.pedido.id_pedido = :id")
	boolean existsByPedidoId(@Param("id") Long id);







    /** [Doc]
     * Cuenta pedidos en el ámbito de IPedidoRepository.
     * @return Long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("SELECT COUNT(p) FROM Pedido p")
    Long contarPedidos();

 // ======================================================
    // ✔ Ventas del día (NATIVO porque usás Date)
    // ======================================================
    /** [Doc]
     * Consulta importes de ventas de del dia en el ámbito de IPedidoRepository.
     * @param fecha fecha o etiqueta temporal de la operación.
     * @return Double con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query(value = """
        SELECT COALESCE(SUM(d.cantidad * d.precio), 0)
        FROM pedido p
        JOIN carrito_de_compra c ON c.id_carrito = p.id_carrito
        JOIN detalle_carrito d   ON d.id_carrito = c.id_carrito
        WHERE UPPER(COALESCE(p.estado_pedido, '')) <> 'CANCELADO' AND DATE(p.fecha_pedido) = :fecha
    """, nativeQuery = true)
    Double ventasDelDia(@Param("fecha") LocalDate fecha);


    // ======================================================
    // ✔ Ventas del mes (NATIVO)
    // ======================================================
    /** [Doc]
     * Consulta importes de ventas de del mes en el ámbito de IPedidoRepository.
     * @param anio año de la consulta.
     * @param mes mes de la consulta.
     * @return Double con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query(value = """
        SELECT COALESCE(SUM(d.cantidad * d.precio), 0)
        FROM pedido p
        JOIN carrito_de_compra c ON c.id_carrito = p.id_carrito
        JOIN detalle_carrito d   ON d.id_carrito = c.id_carrito
        WHERE UPPER(COALESCE(p.estado_pedido, '')) <> 'CANCELADO' AND EXTRACT(YEAR FROM p.fecha_pedido)  = :anio
        AND   EXTRACT(MONTH FROM p.fecha_pedido) = :mes
    """, nativeQuery = true)
    Double ventasDelMes(@Param("anio") int anio, @Param("mes") int mes);


    // ======================================================
    // ✔ Serie diaria
    // ======================================================
    /** [Doc]
     * Consulta importes de ventas de por dia en el ámbito de IPedidoRepository.
     * @param desde inicio del intervalo consultado.
     * @param hasta fin del intervalo consultado.
     * @return List&lt;Object[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query(value = """
        SELECT DATE(p.fecha_pedido) AS dia,
               COALESCE(SUM(d.cantidad * d.precio), 0) AS total
        FROM pedido p
        JOIN carrito_de_compra c ON c.id_carrito = p.id_carrito
        JOIN detalle_carrito d   ON d.id_carrito = c.id_carrito
        WHERE UPPER(COALESCE(p.estado_pedido, '')) <> 'CANCELADO' AND DATE(p.fecha_pedido) BETWEEN :desde AND :hasta
        GROUP BY DATE(p.fecha_pedido)
        ORDER BY dia
    """, nativeQuery = true)
    List<Object[]> ventasPorDia(
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);


    // ======================================================
    // ✔ Serie mensual
    // ======================================================
    /** [Doc]
     * Consulta importes de ventas de por mes en el ámbito de IPedidoRepository.
     * @param anio año de la consulta.
     * @return List&lt;Object[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query(value = """
        SELECT EXTRACT(MONTH FROM p.fecha_pedido) AS mes,
               COALESCE(SUM(d.cantidad * d.precio), 0) AS total
        FROM pedido p
        JOIN carrito_de_compra c ON c.id_carrito = p.id_carrito
        JOIN detalle_carrito d   ON d.id_carrito = c.id_carrito
        WHERE UPPER(COALESCE(p.estado_pedido, '')) <> 'CANCELADO' AND EXTRACT(YEAR FROM p.fecha_pedido) = :anio
        GROUP BY mes
        ORDER BY mes
    """, nativeQuery = true)
    List<Object[]> ventasPorMes(@Param("anio") int anio);


    // ======================================================
    // ✔ Serie anual
    // ======================================================
    /** [Doc]
     * Consulta importes de ventas de por anio en el ámbito de IPedidoRepository.
     * @param inicio límite inicial del intervalo.
     * @param fin límite final del intervalo.
     * @return List&lt;Object[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query(value = """
        SELECT EXTRACT(YEAR FROM p.fecha_pedido) AS anio,
               COALESCE(SUM(d.cantidad * d.precio), 0) AS total
        FROM pedido p
        JOIN carrito_de_compra c ON c.id_carrito = p.id_carrito
        JOIN detalle_carrito d   ON d.id_carrito = c.id_carrito
        WHERE UPPER(COALESCE(p.estado_pedido, '')) <> 'CANCELADO' AND EXTRACT(YEAR FROM p.fecha_pedido) BETWEEN :inicio AND :fin
        GROUP BY anio
        ORDER BY anio
    """, nativeQuery = true)
    List<Object[]> ventasPorAnio(
            @Param("inicio") int inicio,
            @Param("fin") int fin);


    // ======================================================
    // ✔ Top productos más vendidos
    // ======================================================
    /** [Doc]
     * Resuelve la operación productos mas vendidos dentro de IPedidoRepository; el detalle de instrucciones muestra
     * sus pasos y llamadas.
     * @return List&lt;Object[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query(value = """
        SELECT prod.id_producto, prod.nombre_producto, SUM(d.cantidad) AS total
        FROM detalle_carrito d
        JOIN producto prod ON prod.id_producto = d.id_producto
        WHERE EXISTS (SELECT 1 FROM pedido p WHERE p.id_carrito = d.id_carrito
            AND UPPER(COALESCE(p.estado_pedido, '')) <> 'CANCELADO')
        GROUP BY prod.id_producto, prod.nombre_producto
        ORDER BY total DESC
        LIMIT 10
    """, nativeQuery = true)
    List<Object[]> productosMasVendidos();



    /** [Doc]
     * Consulta persistencia según el criterio expresado por countByEstadoPedido; la consulta declarada o la
     * convención de Spring Data determina el resultado.
     * @param string string.
     * @return long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("""
    	    SELECT COUNT(p)
    	    FROM Pedido p
    	    WHERE p.estado_pedido = :estado
    	""")


	long countByEstadoPedido(@Param("estado") String string);
    
    /** [Doc]
     * Consulta persistencia según el criterio expresado por findByUsuario_Id; la consulta declarada o la convención
     * de Spring Data determina el resultado.
     * @param idUsuario identificador del usuario.
     * @return List&lt;Pedido&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    List<Pedido> findByUsuario_Id(Long idUsuario);
    
    /** [Doc]
     * Consulta y devuelve por usuario ordenados en el ámbito de IPedidoRepository.
     * @param usuarioId identificador del usuario.
     * @return List&lt;Pedido&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("""
    		  SELECT p
    		  FROM Pedido p
    		  WHERE p.usuario.id = :usuarioId
    		  ORDER BY p.fecha_pedido DESC
    		""")
    		List<Pedido> listarPorUsuarioOrdenados(@Param("usuarioId") Long usuarioId);
    
    /** [Doc]
     * Consulta y devuelve todos ordenados en el ámbito de IPedidoRepository.
     * @return List&lt;Pedido&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("SELECT p FROM Pedido p ORDER BY p.fecha_pedido DESC")
    List<Pedido> listarTodosOrdenados();

    /** [Doc]
     * Consulta y devuelve por estado ordenados en el ámbito de IPedidoRepository.
     * @param estado estado usado por la operación.
     * @return List&lt;Pedido&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("""
       SELECT p
       FROM Pedido p
       WHERE UPPER(p.estado_pedido) = UPPER(:estado)
       ORDER BY p.fecha_pedido DESC
    """)
    List<Pedido> listarPorEstadoOrdenados(@Param("estado") String estado);

    
    
    
    /** [Doc]
     * Consulta y devuelve todos ordenados en el ámbito de IPedidoRepository.
     * @param pageable parámetros de paginación y orden.
     * @return Page&lt;Pedido&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("SELECT p FROM Pedido p ORDER BY p.fecha_pedido DESC")
    Page<Pedido> listarTodosOrdenados(Pageable pageable);

    /** [Doc]
     * Consulta y devuelve por estado ordenados en el ámbito de IPedidoRepository.
     * @param estado estado usado por la operación.
     * @param pageable parámetros de paginación y orden.
     * @return Page&lt;Pedido&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("""
       SELECT p
       FROM Pedido p
       WHERE UPPER(p.estado_pedido) = UPPER(:estado)
       ORDER BY p.fecha_pedido DESC
    """)
    Page<Pedido> listarPorEstadoOrdenados(@Param("estado") String estado, Pageable pageable);

//querts para panel de usuario
 // Total gastado por usuario (usa tablas reales)
    /** [Doc]
     * Resuelve la operación total gastado por usuario dentro de IPedidoRepository; el detalle de instrucciones
     * muestra sus pasos y llamadas.
     * @param usuarioId identificador del usuario.
     * @return Double con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query(value = """
        SELECT COALESCE(SUM(d.cantidad * d.precio), 0)
        FROM pedido p
        JOIN carrito_de_compra c ON c.id_carrito = p.id_carrito
        JOIN detalle_carrito d   ON d.id_carrito = c.id_carrito
        WHERE UPPER(COALESCE(p.estado_pedido, '')) <> 'CANCELADO' AND p.id_usuario = :usuarioId
    """, nativeQuery = true)
    Double totalGastadoPorUsuario(@Param("usuarioId") Long usuarioId);

    // Total productos comprados (sumatoria de cantidades)
    /** [Doc]
     * Resuelve la operación total productos comprados dentro de IPedidoRepository; el detalle de instrucciones
     * muestra sus pasos y llamadas.
     * @param usuarioId identificador del usuario.
     * @return Long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query(value = """
        SELECT COALESCE(SUM(d.cantidad), 0)
        FROM pedido p
        JOIN carrito_de_compra c ON c.id_carrito = p.id_carrito
        JOIN detalle_carrito d   ON d.id_carrito = c.id_carrito
        WHERE UPPER(COALESCE(p.estado_pedido, '')) <> 'CANCELADO' AND p.id_usuario = :usuarioId
    """, nativeQuery = true)
    Long totalProductosComprados(@Param("usuarioId") Long usuarioId);

    // Total pedidos del usuario
    /** [Doc]
     * Consulta persistencia según el criterio expresado por countByUsuario_Id; la consulta declarada o la convención
     * de Spring Data determina el resultado.
     * @param usuarioId identificador del usuario.
     * @return long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    long countByUsuario_Id(Long usuarioId);

    // Serie mensual de compras del usuario (para gráfico)
    /** [Doc]
     * Resuelve la operación compras por mes dentro de IPedidoRepository; el detalle de instrucciones muestra sus
     * pasos y llamadas.
     * @param usuarioId identificador del usuario.
     * @param anio año de la consulta.
     * @return List&lt;Object[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query(value = """
        SELECT EXTRACT(MONTH FROM p.fecha_pedido) AS mes,
               COALESCE(SUM(d.cantidad * d.precio), 0) AS total
        FROM pedido p
        JOIN carrito_de_compra c ON c.id_carrito = p.id_carrito
        JOIN detalle_carrito d   ON d.id_carrito = c.id_carrito
        WHERE UPPER(COALESCE(p.estado_pedido, '')) <> 'CANCELADO' AND EXTRACT(YEAR FROM p.fecha_pedido) = :anio
          AND p.id_usuario = :usuarioId
        GROUP BY mes
        ORDER BY mes
    """, nativeQuery = true)
    List<Object[]> comprasPorMes(@Param("usuarioId") Long usuarioId, @Param("anio") int anio);

    // Historial paginado por usuario
    
    
    /** [Doc]
     * Consulta persistencia según el criterio expresado por findByUsuarioId; la consulta declarada o la convención
     * de Spring Data determina el resultado.
     * @param id identificador del registro.
     * @param pageable parámetros de paginación y orden.
     * @return Page&lt;Pedido&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("SELECT p FROM Pedido p WHERE p.usuario.id = :id ORDER BY p.fecha_pedido DESC")
    Page<Pedido> findByUsuarioId(@Param("id") Long id, Pageable pageable);
    
 // ======================================================
 // LISTADO ADMIN DE PEDIDOS CON FILTROS COMBINADOS
 // ======================================================

 /** [Doc]
  * Resuelve la operación filtrar pedidos admin dentro de IPedidoRepository; el detalle de instrucciones muestra
  * sus pasos y llamadas.
  * @param estado estado usado por la operación.
  * @param desde inicio del intervalo consultado.
  * @param hasta fin del intervalo consultado.
  * @param usuario cuenta de usuario utilizada por la operación.
  * @param pageable parámetros de paginación y orden.
  * @return Page&lt;Pedido&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
  */
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
             CAST(:desde AS DATE) IS NULL
             OR DATE(p.fecha_pedido) >= CAST(:desde AS DATE)
         )
         AND (
             CAST(:hasta AS DATE) IS NULL
             OR DATE(p.fecha_pedido) <= CAST(:hasta AS DATE)
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
             CAST(:desde AS DATE) IS NULL
             OR DATE(p.fecha_pedido) >= CAST(:desde AS DATE)
         )
         AND (
             CAST(:hasta AS DATE) IS NULL
             OR DATE(p.fecha_pedido) <= CAST(:hasta AS DATE)
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

/** [Doc]
 * Cuenta pedidos del dia en el ámbito de IPedidoRepository.
 * @param fecha fecha o etiqueta temporal de la operación.
 * @return long con el resultado de la operación; véase el cuerpo para las ramas de error.
 */
@Query(value = """
  SELECT COUNT(*)
  FROM pedido p
  WHERE DATE(p.fecha_pedido) = :fecha
""", nativeQuery = true)
long contarPedidosDelDia(@Param("fecha") LocalDate fecha);


/** [Doc]
 * Cuenta por estado sin distinguir mayusculas en el ámbito de IPedidoRepository.
 * @param estado estado usado por la operación.
 * @return long con el resultado de la operación; véase el cuerpo para las ramas de error.
 */
@Query("""
  SELECT COUNT(p)
  FROM Pedido p
  WHERE UPPER(p.estado_pedido) = UPPER(:estado)
""")
long contarPorEstadoSinDistinguirMayusculas(
      @Param("estado") String estado
);

    /** [Doc]
     * Resuelve la operación bloquear dentro de IPedidoRepository; el detalle de instrucciones muestra sus pasos y
     * llamadas.
     * @param id identificador del registro.
     * @return java.util.Optional&lt;Pedido&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Pedido e where e.id_pedido = :id")
    java.util.Optional<Pedido> bloquear(@Param("id") Long id);


    /** [Doc]
     * Busca por carrito en el ámbito de IPedidoRepository.
     * @param id identificador del registro.
     * @return java.util.Optional&lt;Pedido&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("select p from Pedido p where p.carrito.idCarrito = :id")
    java.util.Optional<Pedido> buscarPorCarrito(@Param("id") Long id);

}


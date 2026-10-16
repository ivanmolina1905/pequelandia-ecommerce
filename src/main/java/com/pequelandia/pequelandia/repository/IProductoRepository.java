package com.pequelandia.pequelandia.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pequelandia.pequelandia.model.Producto;

public interface IProductoRepository extends JpaRepository<Producto,Long> {

	@Query("""
	        SELECT p
	        FROM Producto p
	        WHERE (:q IS NULL OR :q = ''
	           OR LOWER(COALESCE(p.nombre_producto, ''))     LIKE LOWER(CONCAT('%', :q, '%'))
	           OR LOWER(COALESCE(p.descripcion_producto, ''))LIKE LOWER(CONCAT('%', :q, '%'))
	           OR LOWER(COALESCE(p.marca_producto, ''))      LIKE LOWER(CONCAT('%', :q, '%'))
	           OR LOWER(COALESCE(p.modelo_producto, ''))     LIKE LOWER(CONCAT('%', :q, '%'))
	           
	        )
	      
	        """)
	    Page<Producto> buscar(@Param("q") String q, Pageable pageable);
	
	
	@EntityGraph(attributePaths = "categorias")
	@Query(value = """
	    SELECT DISTINCT p
	    FROM Producto p
	    LEFT JOIN p.categorias c
	    WHERE (:q IS NULL OR :q = ''
	       OR LOWER(COALESCE(p.nombre_producto,    '')) LIKE LOWER(CONCAT('%', :q, '%'))
	       OR LOWER(COALESCE(p.descripcion_producto,'')) LIKE LOWER(CONCAT('%', :q, '%'))
	       OR LOWER(COALESCE(p.marca_producto,     '')) LIKE LOWER(CONCAT('%', :q, '%'))
	       OR LOWER(COALESCE(p.modelo_producto,    '')) LIKE LOWER(CONCAT('%', :q, '%'))
	       OR LOWER(COALESCE(p.talle_producto,     '')) LIKE LOWER(CONCAT('%', :q, '%'))
	       OR LOWER(COALESCE(c.nombreCategoria,   '')) LIKE LOWER(CONCAT('%', :q, '%'))
	    )
	    AND ( :categoriaId IS NULL OR :categoriaId = 0 OR c.id_categoria = :categoriaId )
	    AND (:precioMin IS NULL OR p.precio >= :precioMin)
    AND (:precioMax IS NULL OR p.precio <= :precioMax)
    AND (:talle IS NULL OR :talle = '' OR LOWER(COALESCE(p.talle_producto, '')) LIKE LOWER(CONCAT('%', :talle, '%')))
    AND (p.estado IS NULL OR LOWER(p.estado) <> 'inactivo')
	""",
	countQuery = """
	    SELECT COUNT(DISTINCT p.idProducto)
	    FROM Producto p
	    LEFT JOIN p.categorias c
	    WHERE (:q IS NULL OR :q = ''
	       OR LOWER(COALESCE(p.nombre_producto,    '')) LIKE LOWER(CONCAT('%', :q, '%'))
	       OR LOWER(COALESCE(p.descripcion_producto,'')) LIKE LOWER(CONCAT('%', :q, '%'))
	       OR LOWER(COALESCE(p.marca_producto,     '')) LIKE LOWER(CONCAT('%', :q, '%'))
	       OR LOWER(COALESCE(p.modelo_producto,    '')) LIKE LOWER(CONCAT('%', :q, '%'))
	       OR LOWER(COALESCE(p.talle_producto,     '')) LIKE LOWER(CONCAT('%', :q, '%'))
	       OR LOWER(COALESCE(c.nombreCategoria,   '')) LIKE LOWER(CONCAT('%', :q, '%'))
	    )
	    AND ( :categoriaId IS NULL OR :categoriaId = 0 OR c.id_categoria = :categoriaId )
	    AND (:precioMin IS NULL OR p.precio >= :precioMin)
		AND (:precioMax IS NULL OR p.precio <= :precioMax)
        AND (:talle IS NULL OR :talle = '' OR LOWER(COALESCE(p.talle_producto, '')) LIKE LOWER(CONCAT('%', :talle, '%')))
	    AND ( p.estado IS NULL OR LOWER(p.estado) <> 'inactivo' )
	""")
	Page<Producto> buscarPublico(@Param("q") String q,
            @Param("categoriaId") Long categoriaId,
            @Param("precioMin") Double precioMin,
            @Param("precioMax") Double precioMax,
            @Param("talle") String talle,
            Pageable pageable);


//=====================================================
// 📉 PRODUCTOS CON STOCK BAJO (para Dashboard)
// =====================================================
@Query(value = """
        SELECT p.*
        FROM producto p
        WHERE p.stock < :minimo
        ORDER BY p.stock ASC
        
    """, nativeQuery = true)
List<Producto> obtenerStockBajo(
        @Param("minimo") int minimo
       
);


// =====================================================
// 🏆 TOP PRODUCTOS MÁS VENDIDOS
// =====================================================
@Query(value = """
SELECT p.id_producto, p.nombre_producto, SUM(d.cantidad) AS total
FROM detalle_carrito d
JOIN producto p ON p.id_producto = d.id_producto
GROUP BY p.id_producto, p.nombre_producto
ORDER BY total DESC
LIMIT :limite
""", nativeQuery = true)
List<Object[]> productosMasVendidos(@Param("limite") int limite);


// =====================================================
// 📦 TOTAL DE PRODUCTOS ACTIVOS
// =====================================================
@Query("SELECT COUNT(p) FROM Producto p WHERE LOWER(p.estado) = 'activo'")
Long contarActivos();

@Query(value = """
SELECT p.*
FROM producto p
WHERE (p.estado IS NULL OR LOWER(p.estado) <> 'inactivo')
ORDER BY RANDOM()
LIMIT :limite
""", nativeQuery = true)
List<Producto> obtenerAleatorios(@Param("limite") int limite);


    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Producto e where e.idProducto = :id")
    java.util.Optional<Producto> bloquear(@Param("id") Long id);

}

package com.pequelandia.pequelandia.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pequelandia.pequelandia.model.Categoria;

/** [Doc]
 * Declara consultas y operaciones JPA de categoria; Spring Data crea la implementación.
 */
public interface ICategoriaRepository extends JpaRepository<Categoria, Long> {

    // ✔ Buscar categoría por nombre (evita duplicados)
    /** [Doc]
     * Consulta persistencia según el criterio expresado por findByNombreCategoriaIgnoreCase; la consulta declarada o
     * la convención de Spring Data determina el resultado.
     * @param nombre_categoria nombre categoria.
     * @return Optional&lt;Categoria&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    Optional<Categoria> findByNombreCategoriaIgnoreCase(String nombre_categoria);

    // ✔ Ver si ya existe nombre (útil cuando creás desde el panel)
    /** [Doc]
     * Consulta persistencia según el criterio expresado por existsByNombreCategoriaIgnoreCase; la consulta declarada
     * o la convención de Spring Data determina el resultado.
     * @param nombre_categoria nombre categoria.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    boolean existsByNombreCategoriaIgnoreCase(String nombre_categoria);

    // ✔ Listar ordenadas alfabéticamente (para el panel)
    /** [Doc]
     * Consulta persistencia según el criterio expresado por findAllOrdenadas; la consulta declarada o la convención
     * de Spring Data determina el resultado.
     * @return List&lt;Categoria&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("SELECT c FROM Categoria c ORDER BY c.nombreCategoria ASC")
    List<Categoria> findAllOrdenadas();
}

package com.pequelandia.pequelandia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pequelandia.pequelandia.model.Localidad;

/** [Doc]
 * Declara consultas y operaciones JPA de localidad; Spring Data crea la implementación.
 */
public interface ILocalidadRepository extends JpaRepository<Localidad, Long> {

    // 🔹 Listar todas ordenadas (para selects del panel)
    /** [Doc]
     * Consulta persistencia según el criterio expresado por findAllByOrderByNombreLocalidadAsc; la consulta
     * declarada o la convención de Spring Data determina el resultado.
     * @return List&lt;Localidad&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    List<Localidad> findAllByOrderByNombreLocalidadAsc();

    // 🔹 Buscar por nombre (para autocompletado)
    /** [Doc]
     * Consulta persistencia según el criterio expresado por findByNombreLocalidadContainingIgnoreCase; la consulta
     * declarada o la convención de Spring Data determina el resultado.
     * @param nombre nombre normalizado o visible de la entidad.
     * @return List&lt;Localidad&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    List<Localidad> findByNombreLocalidadContainingIgnoreCase(String nombre);

    // 🔹 Buscar por código postal
    /** [Doc]
     * Consulta persistencia según el criterio expresado por findByCodigoPostal; la consulta declarada o la
     * convención de Spring Data determina el resultado.
     * @param codigoPostal codigo postal.
     * @return List&lt;Localidad&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    List<Localidad> findByCodigoPostal(int codigoPostal);

    // 🔹 Evitar duplicados en carga de datos
    /** [Doc]
     * Consulta persistencia según el criterio expresado por existsByNombreLocalidadIgnoreCase; la consulta declarada
     * o la convención de Spring Data determina el resultado.
     * @param nombre nombre normalizado o visible de la entidad.
     * @return boolean con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    boolean existsByNombreLocalidadIgnoreCase(String nombre);

    // 🔹 Si querés traer solo nombres (útil para optimizar)
    /** [Doc]
     * Consulta y devuelve nombres en el ámbito de ILocalidadRepository.
     * @return List&lt;String&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    @Query("SELECT l.nombreLocalidad FROM Localidad l ORDER BY l.nombreLocalidad ASC")
    List<String> listarNombres();
}

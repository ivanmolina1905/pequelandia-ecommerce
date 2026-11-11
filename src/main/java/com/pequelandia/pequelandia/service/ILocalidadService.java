package com.pequelandia.pequelandia.service;

import java.util.List;

import com.pequelandia.pequelandia.model.Localidad;
import com.pequelandia.pequelandia.model.Usuario;

/** [Doc]
 * Define el contrato de operaciones de localidad que implementa el servicio correspondiente.
 */
public interface ILocalidadService {

	//buscar
    /** [Doc]
     * Consulta persistencia según el criterio expresado por findLocalidad; la consulta declarada o la convención de
     * Spring Data determina el resultado.
     * @param id identificador del registro.
     * @return Localidad con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public Localidad findLocalidad(Long id);

    /** [Doc]
     * Devuelve todas, según el cuerpo o la implementación del contrato.
     * @return List&lt;Localidad&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public List<Localidad> getTodas();
}

package com.pequelandia.pequelandia.service;

import com.pequelandia.pequelandia.dto.PanelUsuarioDTO;

/** [Doc]
 * Define el contrato de operaciones de panel usuario que implementa el servicio correspondiente.
 */
public interface IPanelUsuarioService {

	
    /** [Doc]
     * Calcula gasto, unidades, cantidad y promedio de pedidos, obtiene el último pedido y completa los doce meses
     * del año con sus importes.
     * @param usuarioId identificador del usuario.
     * @return PanelUsuarioDTO con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public PanelUsuarioDTO build(Long usuarioId);
}

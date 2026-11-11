package com.pequelandia.pequelandia.service;

import com.pequelandia.pequelandia.model.Comprobante;

/** [Doc]
 * Define el contrato de operaciones de comprobante que implementa el servicio correspondiente.
 */
public interface IComprobanteService {

    /** [Doc]
     * Exige un pedido con total y sin comprobante previo; calcula el número como cantidad de comprobantes más uno y
     * guarda fecha, tipo e importe.
     * @param pedidoId identificador del pedido.
     * @param descripcion texto descriptivo de la entidad u operación.
     * @param tipoComprobante tipo comprobante.
     * @return Comprobante con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	Comprobante emitir(Long pedidoId, String descripcion, String tipoComprobante);
}

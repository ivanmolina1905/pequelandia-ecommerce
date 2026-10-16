package com.pequelandia.pequelandia.service;

import com.pequelandia.pequelandia.model.Comprobante;

public interface IComprobanteService {

	Comprobante emitir(Long pedidoId, String descripcion, String tipoComprobante);
}

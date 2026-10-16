package com.pequelandia.pequelandia.service;

import java.time.LocalDate;
import java.util.List;

import com.pequelandia.pequelandia.model.Pedido;

public interface IPedidoService {

	
	  	Pedido finalizarCompra(Long usuarioId, Long carritoId); // crea el pedido con datos del sistema
	  	Pedido marcarEntregado(Long pedidoId);                  // cambia estado a ENTREGADO
	
	  	/** Cambia estado manualmente (PENDIENTE / PREPARACIÓN / ENVIADO / ENTREGADO) */
	    Pedido cambiarEstado(Long pedidoId, String nuevoEstado);
	    
		//lectura
		public List<Pedido> getPedido();
		
		//alta
		public void savePedido(Pedido pedi);
		
		//baja
		public void deletePedido(Long id);
		
		//lectura de un solo usuario
		public Pedido findPedido(Long id);
		
		//editar usuario
		public void editPedido(Long id, Pedido pedi);
		
		// ============================
	    // ✔ ESTADÍSTICAS PARA DASHBOARD 
	    // ============================

	    Long contarPedidos();

	    Double ventasDelDia(LocalDate fecha);

	    Double ventasDelMes(int anio, int mes);

	    List<Object[]> ventasPorDia(LocalDate desde, LocalDate hasta);

	    List<Object[]> ventasPorMes(int anio);

	    List<Object[]> ventasPorAnio(int inicio, int fin);

	    List<Object[]> productosMasVendidos();
		Pedido cambiarEstadoConReposicion(Long id, String estado);
		
}

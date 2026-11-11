package com.pequelandia.pequelandia.service;

import java.time.LocalDate;
import java.util.List;

import com.pequelandia.pequelandia.model.Pedido;

/** [Doc]
 * Define el contrato de operaciones de pedido que implementa el servicio correspondiente.
 */
public interface IPedidoService {

	
        /** [Doc]
         * Bloquea y refresca el carrito, exige titularidad, reutiliza un pedido ya creado o valida y descuenta stock;
         * guarda total, seguimiento y avisos.
         * @param usuarioId identificador del usuario.
         * @param carritoId identificador del carrito persistente.
         * @return Pedido con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
	  	Pedido finalizarCompra(Long usuarioId, Long carritoId); // crea el pedido con datos del sistema
        /** [Doc]
         * Registra entregado en el ámbito de IPedidoService.
         * @param pedidoId identificador del pedido.
         * @return Pedido con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
	  	Pedido marcarEntregado(Long pedidoId);                  // cambia estado a ENTREGADO
	
	  	/** Cambia estado manualmente (PENDIENTE / PREPARACIÓN / ENVIADO / ENTREGADO) */
        /** [Doc]
         * Cambia estado en el ámbito de IPedidoService.
         * @param pedidoId identificador del pedido.
         * @param nuevoEstado nuevo estado.
         * @return Pedido con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
	    Pedido cambiarEstado(Long pedidoId, String nuevoEstado);
	    
		//lectura
        /** [Doc]
         * Devuelve pedido, según el cuerpo o la implementación del contrato.
         * @return List&lt;Pedido&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
		public List<Pedido> getPedido();
		
		//alta
        /** [Doc]
         * Guarda pedido en el ámbito de IPedidoService.
         * @param pedi pedido procesado en este punto.
         */
		public void savePedido(Pedido pedi);
		
		//baja
        /** [Doc]
         * Elimina pedido en el ámbito de IPedidoService.
         * @param id identificador del registro.
         */
		public void deletePedido(Long id);
		
		//lectura de un solo usuario
        /** [Doc]
         * Consulta persistencia según el criterio expresado por findPedido; la consulta declarada o la convención de
         * Spring Data determina el resultado.
         * @param id identificador del registro.
         * @return Pedido con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
		public Pedido findPedido(Long id);
		
		//editar usuario
        /** [Doc]
         * Actualiza pedido en el ámbito de IPedidoService.
         * @param id identificador del registro.
         * @param pedi pedido procesado en este punto.
         */
		public void editPedido(Long id, Pedido pedi);
		
		// ============================
	    // ✔ ESTADÍSTICAS PARA DASHBOARD 
	    // ============================

        /** [Doc]
         * Cuenta pedidos en el ámbito de IPedidoService.
         * @return Long con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
	    Long contarPedidos();

        /** [Doc]
         * Consulta importes de ventas de del dia en el ámbito de IPedidoService.
         * @param fecha fecha o etiqueta temporal de la operación.
         * @return Double con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
	    Double ventasDelDia(LocalDate fecha);

        /** [Doc]
         * Consulta importes de ventas de del mes en el ámbito de IPedidoService.
         * @param anio año de la consulta.
         * @param mes mes de la consulta.
         * @return Double con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
	    Double ventasDelMes(int anio, int mes);

        /** [Doc]
         * Consulta importes de ventas de por dia en el ámbito de IPedidoService.
         * @param desde inicio del intervalo consultado.
         * @param hasta fin del intervalo consultado.
         * @return List&lt;Object[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
	    List<Object[]> ventasPorDia(LocalDate desde, LocalDate hasta);

        /** [Doc]
         * Consulta importes de ventas de por mes en el ámbito de IPedidoService.
         * @param anio año de la consulta.
         * @return List&lt;Object[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
	    List<Object[]> ventasPorMes(int anio);

        /** [Doc]
         * Consulta importes de ventas de por anio en el ámbito de IPedidoService.
         * @param inicio límite inicial del intervalo.
         * @param fin límite final del intervalo.
         * @return List&lt;Object[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
	    List<Object[]> ventasPorAnio(int inicio, int fin);

        /** [Doc]
         * Resuelve la operación productos mas vendidos dentro de IPedidoService; el detalle de instrucciones muestra sus
         * pasos y llamadas.
         * @return List&lt;Object[]&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
	    List<Object[]> productosMasVendidos();
        /** [Doc]
         * Cambia estado con reposicion en el ámbito de IPedidoService.
         * @param id identificador del registro.
         * @param estado estado usado por la operación.
         * @return Pedido con el resultado de la operación; véase el cuerpo para las ramas de error.
         */
		Pedido cambiarEstadoConReposicion(Long id, String estado);
		

    /** [Doc]
     * Crea un carrito persistente desde la copia de sesión, agrega sus líneas en orden de ID y delega la
     * confirmación transaccional.
     * @param usuarioId identificador del usuario.
     * @param items elementos del carrito o de la colección procesada.
     * @return Pedido con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    Pedido comprar(Long usuarioId, java.util.Map<Long, Integer> items);
    /** [Doc]
     * Solicita cancelacion en el ámbito de IPedidoService.
     * @param pedidoId identificador del pedido.
     * @param usuarioId identificador del usuario.
     * @return Pedido con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    Pedido solicitarCancelacion(Long pedidoId, Long usuarioId);
    /** [Doc]
     * Confirma cancelacion en el ámbito de IPedidoService.
     * @param pedidoId identificador del pedido.
     * @return Pedido con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    Pedido confirmarCancelacion(Long pedidoId);

}

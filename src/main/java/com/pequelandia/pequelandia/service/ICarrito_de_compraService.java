package com.pequelandia.pequelandia.service;

import java.math.BigDecimal;
import java.util.List;

import com.pequelandia.pequelandia.model.Carrito_de_compra;
import com.pequelandia.pequelandia.model.Detalle_carrito;

import jakarta.servlet.http.HttpSession;

/** [Doc]
 * Define el contrato de operaciones de carrito de compra que implementa el servicio correspondiente.
 */
public interface ICarrito_de_compraService {
	 // Carrito
    /** [Doc]
     * Consulta y devuelve los datos de la operación en el ámbito de ICarrito_de_compraService.
     * @return List&lt;Carrito_de_compra&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    List<Carrito_de_compra> listar();
    /** [Doc]
     * Obtiene los datos de la operación en el ámbito de ICarrito_de_compraService.
     * @param id identificador del registro.
     * @return Carrito_de_compra con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    Carrito_de_compra obtener(Long id);
    /** [Doc]
     * Guarda los datos de la operación en el ámbito de ICarrito_de_compraService.
     * @param carrito carrito persistente procesado.
     * @return Carrito_de_compra con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    Carrito_de_compra guardar(Carrito_de_compra carrito);            // usa total del front
    /** [Doc]
     * Elimina los datos de la operación en el ámbito de ICarrito_de_compraService.
     * @param id identificador del registro.
     */
    void eliminar(Long id);

    // Actualizar sólo el total (sin recalcular)
    /** [Doc]
     * Exige un carrito editable, sustituye un total nulo por cero y guarda el nuevo importe.
     * @param carritoId identificador del carrito persistente.
     * @param nuevoTotal nuevo total.
     * @return Carrito_de_compra con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    Carrito_de_compra actualizarTotal(Long carritoId, BigDecimal nuevoTotal);

    // Detalles (sin tocar el total)
    /** [Doc]
     * Agrega detalle en el ámbito de ICarrito_de_compraService.
     * @param carritoId identificador del carrito persistente.
     * @param productoId identificador del producto.
     * @param cantidad unidades de una línea de carrito.
     * @return Detalle_carrito con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    Detalle_carrito agregarDetalle(Long carritoId, Long productoId, Integer cantidad);
    /** [Doc]
     * Exige un carrito editable, busca la línea del producto y la elimina o informa que no existe.
     * @param carritoId identificador del carrito persistente.
     * @param productoId identificador del producto.
     */
    void quitarDetalle(Long carritoId, Long productoId);
    /** [Doc]
     * Devuelve or create carrito, según el cuerpo o la implementación del contrato.
     * @param session sesión HTTP que conserva identidad, carrito y token CSRF.
     * @return Carrito_de_compra con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	Carrito_de_compra getOrCreateCarrito(HttpSession session);
	
    /** [Doc]
     * Exige origen invitado y destino de usuario editables, acumula sus líneas, elimina el origen y recalcula el
     * total del destino.
     * @param carritoInvitadoId carrito invitado id.
     * @param carritoUsuarioId carrito usuario id.
     */
	void mergeCarritos(Long carritoInvitadoId, Long carritoUsuarioId);
    /** [Doc]
     * Recalcula total1 en el ámbito de ICarrito_de_compraService.
     * @param carritoId identificador del carrito persistente.
     * @return BigDecimal con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	BigDecimal recalcularTotal1(Long carritoId);
	
    /** [Doc]
     * Consulta la suma de unidades de las líneas del carrito persistente.
     * @param carritoId identificador del carrito persistente.
     * @return int con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	int items(Long carritoId);
	
    /** [Doc]
     * Cambia cantidad en el ámbito de ICarrito_de_compraService.
     * @param carritoId identificador del carrito persistente.
     * @param productoId identificador del producto.
     * @param cantidadNueva cantidad nueva.
     */
	void cambiarCantidad(Long carritoId, Long productoId, int cantidadNueva);
    /** [Doc]
     * Consulta el importe del carrito y lo convierte a BigDecimal con dos decimales y redondeo HALF_UP; el parámetro
     * fallback no se utiliza.
     * @param fallback fallback.
     * @param carritoId identificador del carrito persistente.
     * @return BigDecimal con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	BigDecimal total(BigDecimal fallback, Long carritoId);
	
	
    
 
}

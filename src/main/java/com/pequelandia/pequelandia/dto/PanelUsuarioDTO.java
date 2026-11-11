package com.pequelandia.pequelandia.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** [Doc]
 * Transporta datos de panel usuario entre consultas, servicios, exportaciones y vistas sin representar una
 * entidad persistente.
 */
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class PanelUsuarioDTO {

  // [Doc] Atributo totalGastado (double): total gastado.
  private double totalGastado;
  // [Doc] Atributo totalPedidos (long): total pedidos.
  private long totalPedidos;
  // [Doc] Atributo totalProductosComprados (long): total productos comprados.
  private long totalProductosComprados;

  // [Doc] Atributo ultimoPedidoId (Long): ultimo pedido id.
  private Long ultimoPedidoId;
  // [Doc] Atributo ultimoPedidoEstado (String): ultimo pedido estado.
  private String ultimoPedidoEstado;
  // [Doc] Atributo ultimoPedidoFecha (String): ultimo pedido fecha.
  private String ultimoPedidoFecha; // formateada
  // [Doc] Atributo promedioCompra (double): promedio compra.
  private double promedioCompra;

  // para gráfico
  // [Doc] Atributo meses (List<String>): meses. Se inicializa con new ArrayList<>().
  private List<String> meses = new ArrayList<>();
  // [Doc] Atributo totales (List<Double>): totales. Se inicializa con new ArrayList<>().
  private List<Double> totales = new ArrayList<>();
/** [Doc]
 * Devuelve total gastado, según el cuerpo o la implementación del contrato.
 * @return double con el resultado de la operación; véase el cuerpo para las ramas de error.
 */
public double getTotalGastado() {
    // [Doc] Termina el método o la función y devuelve totalGastado.
	return totalGastado;
}
/** [Doc]
 * Asigna total gastado con el valor recibido.
 * @param totalGastado total gastado.
 */
public void setTotalGastado(double totalGastado) {
    // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.totalGastado = totalGastado;
	this.totalGastado = totalGastado;
}
/** [Doc]
 * Devuelve total pedidos, según el cuerpo o la implementación del contrato.
 * @return long con el resultado de la operación; véase el cuerpo para las ramas de error.
 */
public long getTotalPedidos() {
    // [Doc] Termina el método o la función y devuelve totalPedidos.
	return totalPedidos;
}
/** [Doc]
 * Asigna total pedidos con el valor recibido.
 * @param totalPedidos total pedidos.
 */
public void setTotalPedidos(long totalPedidos) {
    // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.totalPedidos = totalPedidos;
	this.totalPedidos = totalPedidos;
}
/** [Doc]
 * Devuelve total productos comprados, según el cuerpo o la implementación del contrato.
 * @return long con el resultado de la operación; véase el cuerpo para las ramas de error.
 */
public long getTotalProductosComprados() {
    // [Doc] Termina el método o la función y devuelve totalProductosComprados.
	return totalProductosComprados;
}
/** [Doc]
 * Asigna total productos comprados con el valor recibido.
 * @param totalProductosComprados total productos comprados.
 */
public void setTotalProductosComprados(long totalProductosComprados) {
    // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.totalProductosComprados = totalProductosComprados;
	this.totalProductosComprados = totalProductosComprados;
}
/** [Doc]
 * Devuelve ultimo pedido id, según el cuerpo o la implementación del contrato.
 * @return Long con el resultado de la operación; véase el cuerpo para las ramas de error.
 */
public Long getUltimoPedidoId() {
    // [Doc] Termina el método o la función y devuelve ultimoPedidoId.
	return ultimoPedidoId;
}
/** [Doc]
 * Asigna ultimo pedido id con el valor recibido.
 * @param ultimoPedidoId ultimo pedido id.
 */
public void setUltimoPedidoId(Long ultimoPedidoId) {
    // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.ultimoPedidoId = ultimoPedidoId;
	this.ultimoPedidoId = ultimoPedidoId;
}
/** [Doc]
 * Devuelve ultimo pedido estado, según el cuerpo o la implementación del contrato.
 * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
 */
public String getUltimoPedidoEstado() {
    // [Doc] Termina el método o la función y devuelve ultimoPedidoEstado.
	return ultimoPedidoEstado;
}
/** [Doc]
 * Asigna ultimo pedido estado con el valor recibido.
 * @param ultimoPedidoEstado ultimo pedido estado.
 */
public void setUltimoPedidoEstado(String ultimoPedidoEstado) {
    // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.ultimoPedidoEstado = ultimoPedidoEstado;
	this.ultimoPedidoEstado = ultimoPedidoEstado;
}
/** [Doc]
 * Devuelve ultimo pedido fecha, según el cuerpo o la implementación del contrato.
 * @return String con el resultado de la operación; véase el cuerpo para las ramas de error.
 */
public String getUltimoPedidoFecha() {
    // [Doc] Termina el método o la función y devuelve ultimoPedidoFecha.
	return ultimoPedidoFecha;
}
/** [Doc]
 * Asigna ultimo pedido fecha con el valor recibido.
 * @param ultimoPedidoFecha ultimo pedido fecha.
 */
public void setUltimoPedidoFecha(String ultimoPedidoFecha) {
    // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.ultimoPedidoFecha = ultimoPedidoFecha;
	this.ultimoPedidoFecha = ultimoPedidoFecha;
}
/** [Doc]
 * Devuelve meses, según el cuerpo o la implementación del contrato.
 * @return List&lt;String&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
 */
public List<String> getMeses() {
    // [Doc] Termina el método o la función y devuelve meses.
	return meses;
}
/** [Doc]
 * Asigna meses con el valor recibido.
 * @param meses meses.
 */
public void setMeses(List<String> meses) {
    // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.meses = meses;
	this.meses = meses;
}
/** [Doc]
 * Devuelve totales, según el cuerpo o la implementación del contrato.
 * @return List&lt;Double&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
 */
public List<Double> getTotales() {
    // [Doc] Termina el método o la función y devuelve totales.
	return totales;
}
/** [Doc]
 * Devuelve promedio compra, según el cuerpo o la implementación del contrato.
 * @return double con el resultado de la operación; véase el cuerpo para las ramas de error.
 */
public double getPromedioCompra() {
    // [Doc] Termina el método o la función y devuelve promedioCompra.
    return promedioCompra;
}

/** [Doc]
 * Asigna promedio compra con el valor recibido.
 * @param promedioCompra promedio compra.
 */
public void setPromedioCompra(double promedioCompra) {
    // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.promedioCompra = promedioCompra;
    this.promedioCompra = promedioCompra;
}
/** [Doc]
 * Asigna totales con el valor recibido.
 * @param totales totales.
 */
public void setTotales(List<Double> totales) {
    // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.totales = totales;
	this.totales = totales;
}
  
  
}

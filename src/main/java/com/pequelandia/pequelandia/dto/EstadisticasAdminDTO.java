package com.pequelandia.pequelandia.dto;

import java.util.List;

import com.pequelandia.pequelandia.model.Producto;

import lombok.Data;

/** [Doc]
 * Transporta datos de estadisticas admin entre consultas, servicios, exportaciones y vistas sin representar una
 * entidad persistente.
 */
@Data
public class EstadisticasAdminDTO {

    // [Doc] Atributo totalUsuarios (long): total usuarios.
    private long totalUsuarios;
    // [Doc] Atributo totalProductos (long): total productos.
    private long totalProductos;
    // [Doc] Atributo totalPedidos (long): total pedidos.
    private long totalPedidos;

    // [Doc] Atributo ventasHoy (double): ventas hoy.
    private double ventasHoy;
    // [Doc] Atributo ventasMes (double): ventas mes.
    private double ventasMes;
    // [Doc] Atributo ventasAnio (double): ventas anio.
    private double ventasAnio;

    // [Doc] Atributo topProductos (List<ProductoMasVendidoDTO>): top productos.
    private List<ProductoMasVendidoDTO> topProductos;
    // [Doc] Atributo productosStockBajo (List<Producto>): productos stock bajo.
    private List<Producto> productosStockBajo;

    // [Doc] Atributo usuariosNuevosMes (long): usuarios nuevos mes.
    private long usuariosNuevosMes;
    
    /** [Doc]
     * Devuelve total usuarios, según el cuerpo o la implementación del contrato.
     * @return long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
    public long getTotalUsuarios() {
        // [Doc] Termina el método o la función y devuelve totalUsuarios.
		return totalUsuarios;
	}
    /** [Doc]
     * Asigna total usuarios con el valor recibido.
     * @param totalUsuarios total usuarios.
     */
	public void setTotalUsuarios(long totalUsuarios) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.totalUsuarios = totalUsuarios;
		this.totalUsuarios = totalUsuarios;
	}
    /** [Doc]
     * Devuelve total productos, según el cuerpo o la implementación del contrato.
     * @return long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public long getTotalProductos() {
        // [Doc] Termina el método o la función y devuelve totalProductos.
		return totalProductos;
	}
    /** [Doc]
     * Asigna total productos con el valor recibido.
     * @param totalProductos total productos.
     */
	public void setTotalProductos(long totalProductos) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.totalProductos = totalProductos;
		this.totalProductos = totalProductos;
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
     * Devuelve ventas hoy, según el cuerpo o la implementación del contrato.
     * @return double con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public double getVentasHoy() {
        // [Doc] Termina el método o la función y devuelve ventasHoy.
		return ventasHoy;
	}
    /** [Doc]
     * Asigna ventas hoy con el valor recibido.
     * @param ventasHoy ventas hoy.
     */
	public void setVentasHoy(double ventasHoy) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.ventasHoy = ventasHoy;
		this.ventasHoy = ventasHoy;
	}
    /** [Doc]
     * Devuelve ventas mes, según el cuerpo o la implementación del contrato.
     * @return double con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public double getVentasMes() {
        // [Doc] Termina el método o la función y devuelve ventasMes.
		return ventasMes;
	}
    /** [Doc]
     * Asigna ventas mes con el valor recibido.
     * @param ventasMes ventas mes.
     */
	public void setVentasMes(double ventasMes) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.ventasMes = ventasMes;
		this.ventasMes = ventasMes;
	}
    /** [Doc]
     * Devuelve ventas anio, según el cuerpo o la implementación del contrato.
     * @return double con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public double getVentasAnio() {
        // [Doc] Termina el método o la función y devuelve ventasAnio.
		return ventasAnio;
	}
    /** [Doc]
     * Asigna ventas anio con el valor recibido.
     * @param ventasAnio ventas anio.
     */
	public void setVentasAnio(double ventasAnio) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.ventasAnio = ventasAnio;
		this.ventasAnio = ventasAnio;
	}
    /** [Doc]
     * Devuelve top productos, según el cuerpo o la implementación del contrato.
     * @return List&lt;ProductoMasVendidoDTO&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public List<ProductoMasVendidoDTO> getTopProductos() {
        // [Doc] Termina el método o la función y devuelve topProductos.
		return topProductos;
	}
    /** [Doc]
     * Asigna top productos con el valor recibido.
     * @param topProductos top productos.
     */
	public void setTopProductos(List<ProductoMasVendidoDTO> topProductos) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.topProductos = topProductos;
		this.topProductos = topProductos;
	}
    /** [Doc]
     * Devuelve productos stock bajo, según el cuerpo o la implementación del contrato.
     * @return List&lt;Producto&gt; con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public List<Producto> getProductosStockBajo() {
        // [Doc] Termina el método o la función y devuelve productosStockBajo.
		return productosStockBajo;
	}
    /** [Doc]
     * Asigna productos stock bajo con el valor recibido.
     * @param list list.
     */
	public void setProductosStockBajo(List<Producto> list) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.productosStockBajo = list;
		this.productosStockBajo = list;
	}
    /** [Doc]
     * Devuelve usuarios nuevos mes, según el cuerpo o la implementación del contrato.
     * @return long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public long getUsuariosNuevosMes() {
        // [Doc] Termina el método o la función y devuelve usuariosNuevosMes.
		return usuariosNuevosMes;
	}
    /** [Doc]
     * Asigna usuarios nuevos mes con el valor recibido.
     * @param usuariosNuevosMes usuarios nuevos mes.
     */
	public void setUsuariosNuevosMes(long usuariosNuevosMes) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.usuariosNuevosMes = usuariosNuevosMes;
		this.usuariosNuevosMes = usuariosNuevosMes;
	}
    /** [Doc]
     * Devuelve pedidos completados, según el cuerpo o la implementación del contrato.
     * @return long con el resultado de la operación; véase el cuerpo para las ramas de error.
     */
	public long getPedidosCompletados() {
        // [Doc] Termina el método o la función y devuelve pedidosCompletados.
		return pedidosCompletados;
	}
    /** [Doc]
     * Asigna pedidos completados con el valor recibido.
     * @param pedidosCompletados pedidos completados.
     */
	public void setPedidosCompletados(long pedidosCompletados) {
        // [Doc] Conserva en el atributo de la instancia el valor o colaborador recibido: this.pedidosCompletados = pedidosCompletados;
		this.pedidosCompletados = pedidosCompletados;
	}
    // [Doc] Atributo pedidosCompletados (long): pedidos completados.
	private long pedidosCompletados;
	
}

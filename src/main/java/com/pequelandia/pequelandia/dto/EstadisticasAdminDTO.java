package com.pequelandia.pequelandia.dto;

import java.util.List;

import com.pequelandia.pequelandia.model.Producto;

import lombok.Data;

@Data
public class EstadisticasAdminDTO {

    private long totalUsuarios;
    private long totalProductos;
    private long totalPedidos;

    private double ventasHoy;
    private double ventasMes;
    private double ventasAnio;

    private List<ProductoMasVendidoDTO> topProductos;
    private List<Producto> productosStockBajo;

    private long usuariosNuevosMes;
    
    public long getTotalUsuarios() {
		return totalUsuarios;
	}
	public void setTotalUsuarios(long totalUsuarios) {
		this.totalUsuarios = totalUsuarios;
	}
	public long getTotalProductos() {
		return totalProductos;
	}
	public void setTotalProductos(long totalProductos) {
		this.totalProductos = totalProductos;
	}
	public long getTotalPedidos() {
		return totalPedidos;
	}
	public void setTotalPedidos(long totalPedidos) {
		this.totalPedidos = totalPedidos;
	}
	public double getVentasHoy() {
		return ventasHoy;
	}
	public void setVentasHoy(double ventasHoy) {
		this.ventasHoy = ventasHoy;
	}
	public double getVentasMes() {
		return ventasMes;
	}
	public void setVentasMes(double ventasMes) {
		this.ventasMes = ventasMes;
	}
	public double getVentasAnio() {
		return ventasAnio;
	}
	public void setVentasAnio(double ventasAnio) {
		this.ventasAnio = ventasAnio;
	}
	public List<ProductoMasVendidoDTO> getTopProductos() {
		return topProductos;
	}
	public void setTopProductos(List<ProductoMasVendidoDTO> topProductos) {
		this.topProductos = topProductos;
	}
	public List<Producto> getProductosStockBajo() {
		return productosStockBajo;
	}
	public void setProductosStockBajo(List<Producto> list) {
		this.productosStockBajo = list;
	}
	public long getUsuariosNuevosMes() {
		return usuariosNuevosMes;
	}
	public void setUsuariosNuevosMes(long usuariosNuevosMes) {
		this.usuariosNuevosMes = usuariosNuevosMes;
	}
	public long getPedidosCompletados() {
		return pedidosCompletados;
	}
	public void setPedidosCompletados(long pedidosCompletados) {
		this.pedidosCompletados = pedidosCompletados;
	}
	private long pedidosCompletados;
	
}

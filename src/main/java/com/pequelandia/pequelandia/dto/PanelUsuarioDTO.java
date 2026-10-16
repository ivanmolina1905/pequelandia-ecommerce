package com.pequelandia.pequelandia.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class PanelUsuarioDTO {

  private double totalGastado;
  private long totalPedidos;
  private long totalProductosComprados;

  private Long ultimoPedidoId;
  private String ultimoPedidoEstado;
  private String ultimoPedidoFecha; // formateada
  private double promedioCompra;

  // para gráfico
  private List<String> meses = new ArrayList<>();
  private List<Double> totales = new ArrayList<>();
public double getTotalGastado() {
	return totalGastado;
}
public void setTotalGastado(double totalGastado) {
	this.totalGastado = totalGastado;
}
public long getTotalPedidos() {
	return totalPedidos;
}
public void setTotalPedidos(long totalPedidos) {
	this.totalPedidos = totalPedidos;
}
public long getTotalProductosComprados() {
	return totalProductosComprados;
}
public void setTotalProductosComprados(long totalProductosComprados) {
	this.totalProductosComprados = totalProductosComprados;
}
public Long getUltimoPedidoId() {
	return ultimoPedidoId;
}
public void setUltimoPedidoId(Long ultimoPedidoId) {
	this.ultimoPedidoId = ultimoPedidoId;
}
public String getUltimoPedidoEstado() {
	return ultimoPedidoEstado;
}
public void setUltimoPedidoEstado(String ultimoPedidoEstado) {
	this.ultimoPedidoEstado = ultimoPedidoEstado;
}
public String getUltimoPedidoFecha() {
	return ultimoPedidoFecha;
}
public void setUltimoPedidoFecha(String ultimoPedidoFecha) {
	this.ultimoPedidoFecha = ultimoPedidoFecha;
}
public List<String> getMeses() {
	return meses;
}
public void setMeses(List<String> meses) {
	this.meses = meses;
}
public List<Double> getTotales() {
	return totales;
}
public double getPromedioCompra() {
    return promedioCompra;
}

public void setPromedioCompra(double promedioCompra) {
    this.promedioCompra = promedioCompra;
}
public void setTotales(List<Double> totales) {
	this.totales = totales;
}
  
  
}

package com.pequelandia.pequelandia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data

@NoArgsConstructor
public class PuntoSerieDTO {
    public PuntoSerieDTO(String string, Double total) {
		this.label = string; this.valor = total;
	}
	private String label;   // día, mes o año
    private Double valor;   // total vendido
    
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public Double getValor() {
		return valor;
	}
	public void setValor(Double valor) {
		this.valor = valor;
	}
}

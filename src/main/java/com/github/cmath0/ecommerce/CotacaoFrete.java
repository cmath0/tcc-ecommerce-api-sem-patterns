package com.github.cmath0.ecommerce;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CotacaoFrete {
	
	private double valorFrete;
	private String prazoEstimadoEntrega;
	private int tipoEntrega;
}

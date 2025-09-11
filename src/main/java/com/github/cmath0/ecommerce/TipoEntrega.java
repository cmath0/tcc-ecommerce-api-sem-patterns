package com.github.cmath0.ecommerce;

public enum TipoEntrega {

	NORMAL(1),
	EXPRESSA(2),
	TERCEIRIZADA_RAPIDA_EXPRESS(3);
	
	private final int codigo;
	
	private TipoEntrega(int codigo) {
		this.codigo = codigo;
	}
	
	public static TipoEntrega fromCodigo(int codigo) {
		for (TipoEntrega tipo : TipoEntrega.values()) {
			if (tipo.codigo == codigo) {
				return tipo;
			}
		}
		
		throw new IllegalArgumentException("Código de tipo de entrega inválido: " + codigo);
	}
	
	public int getCodigo() {
		return codigo;
	}
}

package com.github.cmath0.ecommerce;

public enum StatusPedido {
	AGUARDANDO_PAGAMENTO(1, "Aguardando pagamento"),
	PAGO(2, "Pago"),
	ENVIADO(3, "Enviado à transportadora"),
	ENTREGUE(4, "Entregue"),
	CANCELADO(5, "Cancelado");
	
	private final int codigo;
	private final String descricao;
	
	StatusPedido(int codigo, String descricao) {
		this.codigo = codigo;
		this.descricao = descricao;
	}
	
	public int getCodigo() {
		return codigo;
	}
	
	public String getDescricao() {
		return descricao;
	}
	
	public static StatusPedido fromCodigo(int codigo) {
		for (StatusPedido status : values()) {
			if (status.codigo == codigo) {
				return status;
			}
		}
		
		throw new IllegalArgumentException("Código de status inválido: " + codigo);
	}
	
	public static boolean isValid(int codigo) {
		try {
			fromCodigo(codigo);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
    }
}

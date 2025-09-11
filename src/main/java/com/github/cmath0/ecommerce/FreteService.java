package com.github.cmath0.ecommerce;

import org.springframework.stereotype.Service;

@Service
public class FreteService {

	public CotacaoFrete calcularFrete(Pedido pedido) {
		// se valor do pedido for > 200 frete gratis
		if (pedido.getValorTotal() > 200) {
			pedido.setTipoEntrega(TipoEntrega.EXPRESSA.getCodigo());
			return new CotacaoFrete(0.0, "2 a 3 dias", pedido.getTipoEntrega());
		}
		
		TipoEntrega tipoEntrega = TipoEntrega.fromCodigo(pedido.getTipoEntrega());
		
		switch (tipoEntrega) {
		case NORMAL:
			return calcularFreteNormal(pedido);
		case EXPRESSA:
			return calcularFreteExpressa(pedido);
		case TERCEIRIZADA_RAPIDA_EXPRESS:
			return calcularFreteRapidaExpress(pedido);
		default:
			throw new IllegalArgumentException("Tipo de entrega inválido: " + pedido.getTipoEntrega());
		}
	}

	private CotacaoFrete calcularFreteNormal(final Pedido pedido) {
		// entrega normal: preco base 15.00, mais 2.00 a cada 1000g, prazo 5 a 7 dias
		double valorFreteBase = 15.00;
		double valorAdicionalPorKg = 2.00;
		String prazoEstimado = "5 a 7 dias";
		
		double multiplicadorPorRegiao = getMultiplicadorPorRegiao(pedido.getCepDestino());
		double pesoEmKg = pedido.getPesoTotalEmGramas() / 1000.0;
		double valorFrete = (valorFreteBase + (valorAdicionalPorKg * pesoEmKg)) * multiplicadorPorRegiao;
		
		return new CotacaoFrete(valorFrete, prazoEstimado, pedido.getTipoEntrega());
	}

	private CotacaoFrete calcularFreteExpressa(final Pedido pedido) {
		// entrega expressa: preco base 25.00, mais 3.00 a cada 1000g, prazo 2 a 3 dias
		double valorFreteBase = 25.00;
		double valorAdicionalPorKg = 3.00;
		String prazoEstimado = "2 a 3 dias";
		
		double multiplicadorPorRegiao = getMultiplicadorPorRegiao(pedido.getCepDestino());
		double pesoEmKg = pedido.getPesoTotalEmGramas() / 1000.0;
		double valorFrete = (valorFreteBase + (valorAdicionalPorKg * pesoEmKg)) * multiplicadorPorRegiao;
		
		return new CotacaoFrete(valorFrete, prazoEstimado, pedido.getTipoEntrega());
	}
	
	private CotacaoFrete calcularFreteRapidaExpress(final Pedido pedido) {
		// preco fixo 35.00, prazo 1 dia util para sudeste, sul e centro-oeste, 2 dias uteis para nordeste e norte
		Regiao regiao = Regiao.obterRegiaoPorCep(pedido.getCepDestino());
		String prazoEstimado = "1 dia útil";
		
		if (regiao == Regiao.NORTE || regiao == Regiao.NORDESTE) {
			prazoEstimado = "2 dias úteis";
		}
		
		double valorFreteBase = 35.00;
		double multiplicadorPorRegiao = getMultiplicadorPorRegiaoRapidaExpress(pedido.getCepDestino());
		double valorFrete = valorFreteBase * multiplicadorPorRegiao;
		
		return new CotacaoFrete(valorFrete, prazoEstimado, pedido.getTipoEntrega());
	}
	
	private double getMultiplicadorPorRegiao(String cepDestino) {
		// multiplicadores por regiao: Norte 1.2, Nordeste 1.15, Centro-Oeste 1.1, Sudeste 1.0, Sul 1.1
		Regiao regiao = Regiao.obterRegiaoPorCep(cepDestino);
		
		switch (regiao) {
		case NORTE:
			return 1.2;
		case NORDESTE:
			return 1.15;
		case CENTRO_OESTE:
			return 1.1;
		case SUDESTE:
			return 1.0;
		case SUL:
			return 1.1;
		default:
			throw new IllegalArgumentException("Região inválida para o CEP: " + cepDestino);
		}
	}
	
	private double getMultiplicadorPorRegiaoRapidaExpress(String cepDestino) {
		// multiplicador diferencial por regiao: Norte 1.4, Nordeste 1.3, Centro-Oeste 1.2, Sudeste 1.0, Sul 1.1
		Regiao regiao = Regiao.obterRegiaoPorCep(cepDestino);
		
		switch (regiao) {
		case NORTE:
			return 1.4;
		case NORDESTE:
			return 1.3;
		case CENTRO_OESTE:
			return 1.2;
		case SUDESTE:
			return 1.0;
		case SUL:
			return 1.1;
		default:
			throw new IllegalArgumentException("Região inválida para o CEP: " + cepDestino);
		}
	}
}

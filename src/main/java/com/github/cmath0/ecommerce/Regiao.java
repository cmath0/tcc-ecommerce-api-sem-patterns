package com.github.cmath0.ecommerce;

public enum Regiao {
    SUDESTE, SUL, NORDESTE, CENTRO_OESTE, NORTE;
	
    public static Regiao obterRegiaoPorCep(String cep) {
    	int prefixo = Integer.parseInt(cep.substring(0, 2));
    	if (prefixo >= 0 && prefixo <= 29) return Regiao.SUDESTE;
    	if (prefixo >= 30 && prefixo <= 49) return Regiao.SUL;
    	if (prefixo >= 50 && prefixo <= 59) return Regiao.NORDESTE;
    	if (prefixo >= 60 && prefixo <= 79) return Regiao.CENTRO_OESTE;
    	return Regiao.NORTE;
    }
}
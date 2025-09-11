package com.github.cmath0.ecommerce;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fretes/cotacao")
public class FreteController {

	@Autowired private FreteService freteService;
	
	@GetMapping
	public CotacaoFrete calcularFrete(@RequestBody Pedido pedido) {
		return freteService.calcularFrete(pedido);
	}
}

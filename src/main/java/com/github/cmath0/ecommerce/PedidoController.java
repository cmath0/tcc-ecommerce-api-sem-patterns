package com.github.cmath0.ecommerce;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

	@Autowired PedidoService service;
	
	@GetMapping
	public List<Pedido> listarPedidos() {
		return service.listarPedidos();
	}
	
	@PostMapping
	public Pedido efetuarPedido(@RequestBody Pedido pedido) {
		return service.efetuarPedido(pedido);
	}
	
	@PatchMapping("/{id}/status")
	public Pedido atualizarStatusPedido(@PathVariable long id, @RequestBody Pedido pedidoBody) {
		return service.atualizarStatusPedido(id, pedidoBody);
	}
}

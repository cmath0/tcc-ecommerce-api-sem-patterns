package com.github.cmath0.ecommerce;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

	@Autowired
	private ClienteService service;
	
	@GetMapping
	public List<Cliente> listarClientes() {
		return service.listarClientes();
	}
	
	@PostMapping
	public Cliente criarCliente(@RequestBody Cliente cliente) {
		if (cliente.getNivel() < 1 || cliente.getNivel() > 4) {
			throw new IllegalArgumentException("Nível do cliente inválido. Nível: " + cliente.getNivel());
		}

		return service.criarCliente(cliente);
	}
}

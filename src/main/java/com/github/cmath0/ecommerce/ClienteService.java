package com.github.cmath0.ecommerce;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class ClienteService {

	@Autowired ClienteRepository repository;
	
	public List<Cliente> listarClientes() {
		return repository.findAll();
	}
	
	public Cliente criarCliente(@RequestBody Cliente cliente) {
		if (cliente.getNivel() < 1 || cliente.getNivel() > 4) {
			throw new IllegalArgumentException("Nível do cliente inválido. Nível: " + cliente.getNivel());
		}

		return repository.save(cliente);
	}
}

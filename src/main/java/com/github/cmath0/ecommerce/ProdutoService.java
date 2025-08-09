package com.github.cmath0.ecommerce;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class ProdutoService {

	@Autowired private ProdutoRepository repository;
	
	public List<Produto> listarProdutosDisponiveis() {
        List<Produto> produtos = repository.findAll();
        produtos.removeIf(p -> p.getQuantidadeEstoque() <= 0);
        
		return produtos;
    }
	
	public Produto criarProduto(@RequestBody Produto produto) {
    	if (produto.getNome() == null || produto.getNome().trim().isEmpty()) {
    		throw new IllegalArgumentException("Nome não pode ser vazio!");
    	}
    	
    	if (produto.getPreco() <= 0) {
    		throw new IllegalArgumentException("Preço inválido!");
    	}
    	
    	if (produto.getQuantidadeEstoque() < 0) {
    		throw new IllegalArgumentException("Quantidade em estoque inválida!");
    	}
    	
        return repository.save(produto);
    }
}

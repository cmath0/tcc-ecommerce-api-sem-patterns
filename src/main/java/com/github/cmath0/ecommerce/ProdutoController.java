package com.github.cmath0.ecommerce;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

	@Autowired private ProdutoService produtoService;

	@GetMapping
    public List<Produto> listarProdutosDisponiveis() {
		return produtoService.listarProdutosDisponiveis();
//        List<Produto> produtos = repository.findAll();
//        produtos.removeIf(p -> p.getQuantidadeEstoque() <= 0);
//        
//		return produtos;
    }

    @PostMapping
    public Produto criarProduto(@RequestBody Produto produto) {
    	return produtoService.criarProduto(produto);
//    	if (produto.getNome() == null || produto.getNome().trim().isEmpty()) {
//    		throw new IllegalArgumentException("Nome não pode ser vazio!");
//    	}
//    	
//    	if (produto.getPreco() <= 0) {
//    		throw new IllegalArgumentException("Preço inválido!");
//    	}
//    	
//    	if (produto.getQuantidadeEstoque() < 0) {
//    		throw new IllegalArgumentException("Quantidade em estoque inválida!");
//    	}
//    	
//        return repository.save(produto);
    }
}

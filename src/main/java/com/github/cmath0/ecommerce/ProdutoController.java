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
    }

    @PostMapping
    public Produto criarProduto(@RequestBody Produto produto) {
    	return produtoService.criarProduto(produto);
    }
}

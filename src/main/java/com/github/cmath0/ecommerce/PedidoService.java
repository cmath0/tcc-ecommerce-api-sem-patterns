package com.github.cmath0.ecommerce;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PedidoService {

	@Autowired PedidoRepository repository;
	@Autowired ProdutoRepository produtoRepository;
	@Autowired ClienteRepository clienteRepository;
	
	public List<Pedido> listarPedidos() {
		return repository.findAll();
	}
	
	public Pedido efetuarPedido(Pedido pedido) {
		Map<Long, Produto> produtosDoPedido = new HashMap<>();
		pedido.setValorTotal(0.0);
		pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO.getCodigo());
		
		// verificar estoque dos produtos e calcular valor total do pedido
		for (Long produtoId : pedido.getProdutosDoPedido()) {
			Produto produto = produtosDoPedido.get(produtoId);
			
			if (produto == null) {
				if (!produtoRepository.existsById(produtoId)) {
					throw new IllegalArgumentException("Produto inexistente. Id: " + produtoId);
				}
				
				produto = produtoRepository.getReferenceById(produtoId);
			}
			
			if (produto.getQuantidadeEstoque() < 1) {
				throw new IllegalArgumentException("Produto fora de estoque: " + produto.getId() + " - " + produto.getNome());
			}
			
			produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - 1);
			produtosDoPedido.put(produto.getId(), produto);
			
			pedido.setValorTotal(pedido.getValorTotal() + produto.getPreco());
		}
		
		// aplicar desconto baseado no nivel do cliente
		if (pedido.getClienteId() != null) {
			if (!clienteRepository.existsById(pedido.getClienteId())) {
				throw new IllegalArgumentException("Cliente inexistente. Id: " + pedido.getClienteId());
			}

			Cliente cliente = clienteRepository.getReferenceById(pedido.getClienteId());
			
			if (cliente.getNivel() < 1 || cliente.getNivel() > 4) {
				throw new IllegalArgumentException("Nível do cliente inválido. Nível: " + cliente.getNivel());
			}
			
			double desconto = 0.0;
			
			if (cliente.getNivel() == 1) {
				desconto = 0.0; // sem desconto
			} else if (cliente.getNivel() == 2) {
				desconto = 0.05; // 5%
			} else if (cliente.getNivel() == 3) {
				desconto = 0.10; // 10%
				
				if (pedido.getValorTotal() > 200.00) {
					desconto += 0.02; // mais 2% se valor total > 200
				}
			} else if (cliente.getNivel() == 4) {
				desconto = 0.15; // 15%
				
				if (pedido.getValorTotal() > 200.00) {
					desconto += 0.03; // mais 3% se valor total > 200
				}
			}
			
			pedido.setValorTotal(pedido.getValorTotal() * (1 - desconto));
		}
		
		return repository.save(pedido);
	}
	
	public Pedido atualizarStatusPedido(long id, Pedido pedidoBody) {
		int codigoNovoStatus = pedidoBody.getStatus();
		
		if (!repository.existsById(id)) {
			throw new IllegalArgumentException(String.format("Pedido %d não encontrado", id));
		}
		
		Pedido pedido = repository.getReferenceById(id);
		StatusPedido statusAtual = StatusPedido.fromCodigo(pedido.getStatus());
		StatusPedido novoStatus = StatusPedido.fromCodigo(codigoNovoStatus);
		
		if (statusAtual == novoStatus) {
			throw new TransicaoInvalidaStatusException(String.format("Pedido já possui o status %s.", novoStatus.getDescricao()));
		}
		
		// validar transições possíveis
		switch (statusAtual) {
			case AGUARDANDO_PAGAMENTO:
				if (novoStatus != StatusPedido.PAGO && novoStatus != StatusPedido.CANCELADO) {
					lancarExcecaoTransicaoInvalida(statusAtual, novoStatus);
				}
				
				if (novoStatus == StatusPedido.PAGO) {
					reduzirEstoqueDosProdutos(pedido);
				}
				if (novoStatus == StatusPedido.CANCELADO) {
					retornarProdutosAoEstoque(pedido);
				}
				break;
			case PAGO:
				if (novoStatus != StatusPedido.ENVIADO && novoStatus != StatusPedido.CANCELADO) {
					lancarExcecaoTransicaoInvalida(statusAtual, novoStatus);
				}
				
				if (novoStatus == StatusPedido.CANCELADO) {
					retornarProdutosAoEstoque(pedido);
					
					// efetuar reembolso
					System.out.println("Reembolso solicitado para o pedido número: " + pedido.getId());
				}
				break;
			case ENVIADO:
				if (novoStatus != StatusPedido.ENTREGUE) {
					lancarExcecaoTransicaoInvalida(statusAtual, novoStatus);
				}
				break;
			case ENTREGUE:
				throw new TransicaoInvalidaStatusException("Não é possível alterar o status de um pedido já entregue.");
			case CANCELADO:
				throw new TransicaoInvalidaStatusException("Não é possível alterar o status de um pedido cancelado.");
			default:
				throw new IllegalArgumentException("Status desconhecido: " + statusAtual);
		}
		
		pedido.setStatus(codigoNovoStatus);
		return repository.save(pedido);
	}

	private void lancarExcecaoTransicaoInvalida(StatusPedido statusAtual, StatusPedido novoStatus) {
		throw new TransicaoInvalidaStatusException("Transição inválida de status: " + statusAtual.getDescricao()
				+ " para " + novoStatus.getDescricao());
	}

	private void reduzirEstoqueDosProdutos(Pedido pedido) {
		// reduzir estoque dos produtos
		List<Produto> produtosParaAtualizarEstoque = produtoRepository.findAllById(pedido.getProdutosDoPedido());
		
		for (Produto produto : produtosParaAtualizarEstoque) {
			produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - 1);
		}
		
		produtoRepository.saveAll(produtosParaAtualizarEstoque);
	}

	private void retornarProdutosAoEstoque(Pedido pedido) {
		// retornar produtos ao estoque
		List<Produto> produtosParaAtualizarEstoque = produtoRepository.findAllById(pedido.getProdutosDoPedido());
		
		for (Produto produto : produtosParaAtualizarEstoque) {
			produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() + 1);
		}
		
		produtoRepository.saveAll(produtosParaAtualizarEstoque);
	}
}

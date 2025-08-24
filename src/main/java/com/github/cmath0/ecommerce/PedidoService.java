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
		pedido.setValorSubtotal(0.0);
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
			
			pedido.setValorSubtotal(pedido.getValorSubtotal() + produto.getPreco());
		}
		
		pedido.setValorTotal(pedido.getValorSubtotal());
		
		aplicarDescontos(pedido);
		
		return repository.save(pedido);
	}

	private void aplicarDescontos(Pedido pedido) {
		// aplicar desconto de acordo com ordem
		
		// 1 - volume (acima de 200 reais, 10% desconto)
		if (pedido.getValorSubtotal() > 200.00) {
			double desconto = pedido.getValorSubtotal() * 0.10; // 10% de desconto
			pedido.setValorDescontos(pedido.getValorDescontos() + desconto);
			pedido.setValorTotal(pedido.getValorTotal() - desconto);
		}
		
		// 2 - por tipo cliente (nível 2 ou 3, 5% ou 10% de desconto)
		if (pedido.getClienteId() != null && clienteRepository.existsById(pedido.getClienteId())) {
			Cliente cliente = clienteRepository.getReferenceById(pedido.getClienteId());
			
			if (cliente.getNivel() == 2) {
				double desconto = pedido.getValorSubtotal() * 0.05; // 5% de desconto
				pedido.setValorDescontos(pedido.getValorDescontos() + desconto);
				pedido.setValorTotal(pedido.getValorTotal() - desconto);
			} else if (cliente.getNivel() == 3) {
				double desconto = pedido.getValorSubtotal() * 0.10; // 10% de desconto
				pedido.setValorDescontos(pedido.getValorDescontos() + desconto);
				pedido.setValorTotal(pedido.getValorTotal() - desconto);
			}
		}
		
		// 3 - cupom - 50 reais off em compras acima de 500 reais
		if ("PROMO50".equals(pedido.getCupomDesconto()) && pedido.getValorSubtotal() > 500.00) {
			double desconto = 50.00; // 50 reais de desconto
			pedido.setValorDescontos(pedido.getValorDescontos() + desconto);
			pedido.setValorTotal(pedido.getValorTotal() - desconto);
		}
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
					
					System.out.println("📧 Enviando e-mail de confirmação de pagamento...");
				}
				if (novoStatus == StatusPedido.CANCELADO) {
					System.out.println("📧 Enviando e-mail de confirmação de cancelamento do pedido...");
				}
				break;
			case PAGO:
				if (novoStatus != StatusPedido.ENVIADO && novoStatus != StatusPedido.CANCELADO) {
					lancarExcecaoTransicaoInvalida(statusAtual, novoStatus);
				}
				
				if (novoStatus == StatusPedido.ENVIADO) {
					System.out.println("📧 Enviando e-mail de pedido enviado à transportadora...");
				}
				
				if (novoStatus == StatusPedido.CANCELADO) {
					retornarProdutosAoEstoque(pedido);
					
					// efetuar reembolso
					System.out.println("Reembolso solicitado para o pedido número: " + pedido.getId());
					
					System.out.println("📧 Enviando e-mail de confirmação de cancelamento do pedido...");
				}
				break;
			case ENVIADO:
				if (novoStatus != StatusPedido.ENTREGUE) {
					lancarExcecaoTransicaoInvalida(statusAtual, novoStatus);
				}
				
				System.out.println("📧 Enviando e-mail de confirmação de entrega do pedido...");
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

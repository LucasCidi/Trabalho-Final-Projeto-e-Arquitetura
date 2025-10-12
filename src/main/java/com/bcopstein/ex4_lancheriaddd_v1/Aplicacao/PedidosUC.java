package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao.Presenters.PedidosPresenter;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.EstoqueRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.PedidosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ProdutosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ReceitasRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.*;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.CalculadoraPedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PedidosUC {
    private final PedidosRepository pedidosRepository;
    private final ReceitasRepository receitasRepository;
    private final EstoqueRepository estoqueRepository;
    private final ProdutosRepository produtosRepository;
    private final CalculadoraPedidoService calculadoraService;

    @Autowired
    public PedidosUC(PedidosRepository pedidosRepository, ReceitasRepository receitasRepository,
                     EstoqueRepository estoqueRepository, ProdutosRepository produtosRepository,
                     CalculadoraPedidoService calculadoraService) {
        this.pedidosRepository = pedidosRepository;
        this.receitasRepository = receitasRepository;
        this.estoqueRepository = estoqueRepository;
        this.produtosRepository = produtosRepository;
        this.calculadoraService = calculadoraService;
    }

    public PedidosPresenter submeterPedidoFromRequest(String cpf, List<PedidosPresenter> itensRequest) {
        Cliente cliente = new Cliente(cpf, "", "", true, "", "", "");

        List<ItemPedido> itens = itensRequest.stream().map(itemRequest -> {
            Produto produto = produtosRepository.recuperaProdutoPorid(itemRequest.getProdutoId());
            if (produto == null) {
                throw new IllegalArgumentException("Produto não encontrado: " + itemRequest.getProdutoId());
            }
            return new ItemPedido(produto, itemRequest.getQuantidade());
        }).collect(Collectors.toList());

        Pedido pedido = submeterPedido(cliente, itens);
        if (pedido == null) {
            return new PedidosPresenter(null, "Pedido negado: falta de ingredientes");
        }

        return new PedidosPresenter(pedido, "Pedido aprovado com sucesso");
    }

    public Pedido submeterPedido(Cliente cliente, List<ItemPedido> itens) {
        // Verificar disponibilidade de ingredientes
        Map<Long, Integer> ingredientesNecessarios = new HashMap<>();
        for (ItemPedido item : itens) {
            Produto produto = item.getItem();
            int quantidade = item.getQuantidade();
            Receita receita = produto.getReceita();
            List<Ingrediente> ingredientes = receitasRepository.recuperaIngredientesReceita(receita.getId());
            for (Ingrediente ingrediente : ingredientes) {
                ingredientesNecessarios.merge(ingrediente.getId(), quantidade, Integer::sum);
            }
        }

        // Verificar se há estoque suficiente
        for (Map.Entry<Long, Integer> entry : ingredientesNecessarios.entrySet()) {
            ItemEstoque itemEstoque = estoqueRepository.recuperaItemEstoque(entry.getKey());
            if (itemEstoque == null || itemEstoque.getQuantidade() < entry.getValue()) {
                return null;
            }
        }

        // Calcular valores do pedido
        double valor = calculadoraService.calcularValorTotal(itens);
        double impostos = calculadoraService.calcularImpostos(valor);
        double desconto = calculadoraService.calcularDesconto(valor);
        double valorCobrado = calculadoraService.calcularValorCobrado(valor, impostos, desconto);

        // Criar e salvar o pedido com status APROVADO, sem atualizar o estoque
        long pedidoId = pedidosRepository.gerarNovoId();
        Pedido pedido = new Pedido(pedidoId, cliente, null, itens, Pedido.Status.APROVADO,
                valor, impostos, desconto, valorCobrado);

        pedidosRepository.salvarPedido(pedido);
        pedidosRepository.salvarItensPedido(pedido.getId(), itens);

        return pedido;
    }

    public void atualizarEstoqueAposPagamento(Pedido pedido) {
        Map<Long, Integer> ingredientesNecessarios = new HashMap<>();
        for (ItemPedido item : pedido.getItens()) {
            Produto produto = item.getItem();
            int quantidade = item.getQuantidade();
            Receita receita = produto.getReceita();
            List<Ingrediente> ingredientes = receitasRepository.recuperaIngredientesReceita(receita.getId());
            for (Ingrediente ingrediente : ingredientes) {
                ingredientesNecessarios.merge(ingrediente.getId(), quantidade, Integer::sum);
            }
        }

        for (Map.Entry<Long, Integer> entry : ingredientesNecessarios.entrySet()) {
            estoqueRepository.atualizarEstoque(entry.getKey(), entry.getValue());
        }
    }

    public void restaurarEstoqueAposCancelamento(Pedido pedido) {
        Map<Long, Integer> ingredientesNecessarios = new HashMap<>();
        for (ItemPedido item : pedido.getItens()) {
            Produto produto = item.getItem();
            int quantidade = item.getQuantidade();
            Receita receita = produto.getReceita();
            List<Ingrediente> ingredientes = receitasRepository.recuperaIngredientesReceita(receita.getId());
            for (Ingrediente ingrediente : ingredientes) {
                ingredientesNecessarios.merge(ingrediente.getId(), quantidade, Integer::sum);
            }
        }

        for (Map.Entry<Long, Integer> entry : ingredientesNecessarios.entrySet()) {
            estoqueRepository.restaurarEstoque(entry.getKey(), entry.getValue());
        }
    }
}
package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.EstoqueRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.PedidosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ProdutosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ReceitasRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PedidosService {
    private final PedidosRepository pedidosRepository;
    private final ProdutosRepository produtosRepository;
    private final ReceitasRepository receitasRepository;
    private final EstoqueRepository estoqueRepository;
    private final PagamentoService pagamentoService;
    private final CozinhaService cozinhaService;
    private final EntregaService entregaService;

    @Autowired
    public PedidosService(PedidosRepository pedidosRepository, ProdutosRepository produtosRepository,
                          ReceitasRepository receitasRepository, EstoqueRepository estoqueRepository,
                          PagamentoService pagamentoService, CozinhaService cozinhaService,
                          EntregaService entregaService) {
        this.pedidosRepository = pedidosRepository;
        this.produtosRepository = produtosRepository;
        this.receitasRepository = receitasRepository;
        this.estoqueRepository = estoqueRepository;
        this.pagamentoService = pagamentoService;
        this.cozinhaService = cozinhaService;
        this.entregaService = entregaService;
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
        double valor = 0.0;
        for (ItemPedido item : itens) {
            valor += item.getItem().getPreco() * item.getQuantidade() / 100.0;
        }
        double impostos = valor * 0.10; // impostos
        double desconto = 0.0;
        double valorCobrado = valor + impostos - desconto;

        // Criar o pedido
        long pedidoId = pedidosRepository.gerarNovoId();
        Pedido pedido = new Pedido(pedidoId, cliente, null, itens, Pedido.Status.APROVADO,
                valor, impostos, desconto, valorCobrado);

        // Salvar o pedido e itens
        pedidosRepository.salvarPedido(pedido);
        pedidosRepository.salvarItensPedido(pedido.getId(), itens);

        // Atualizar estoque
        for (Map.Entry<Long, Integer> entry : ingredientesNecessarios.entrySet()) {
            estoqueRepository.atualizarEstoque(entry.getKey(), entry.getValue());
        }

        return pedido;
    }

    public Pedido consultarStatusPedido(String clienteCpf, long pedidoId) {
        Pedido pedido = pedidosRepository.recuperarPedidoPorId(pedidoId);
        if (pedido == null || !pedido.getCliente().getCpf().equals(clienteCpf)) {
            return null; // Pedido não encontrado ou não pertence ao cliente
        }
        return pedido;
    }

    public Pedido cancelarPedido(String clienteCpf, long pedidoId) {
        Pedido pedido = pedidosRepository.recuperarPedidoPorId(pedidoId);
        if (pedido == null || !pedido.getCliente().getCpf().equals(clienteCpf)) {
            return null;
        }
        if (pedido.getStatus() != Pedido.Status.APROVADO) {
            return null;
        }

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

        pedidosRepository.atualizarStatusPedido(pedidoId, Pedido.Status.CANCELADO);
        pedido.setStatus(Pedido.Status.CANCELADO);
        return pedido;
    }

    public Pedido pagarPedido(String clienteCpf, long pedidoId) {
        Pedido pedido = pedidosRepository.recuperarPedidoPorId(pedidoId);
        if (pedido == null || !pedido.getCliente().getCpf().equals(clienteCpf)) {
            return null;
        }
        if (pagamentoService.processarPagamento(clienteCpf, pedidoId)) {
            cozinhaService.chegadaDePedido(pedido);
            return pedido;
        }
        return null;
    }

    public List<Pedido> recuperaPedidosPorDatas(LocalDateTime data1, LocalDateTime data2) {
        List<Pedido> pedidos = pedidosRepository.recuperaPedidosPorDatas(data1, data2);
        return pedidos;
    }

    public List<Pedido> recuperaPedidosPorClienteEDatas(LocalDateTime data1, LocalDateTime data2, String cpf) {
        List<Pedido> pedidos = pedidosRepository.recuperaPedidosPorClienteEDatas(data1, data2, cpf);
        return pedidos;
    }

}
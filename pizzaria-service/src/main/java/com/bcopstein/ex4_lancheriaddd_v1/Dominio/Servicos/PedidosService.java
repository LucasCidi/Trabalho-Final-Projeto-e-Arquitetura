package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Http.EstoqueGatewayClient;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests.AtualizarEstoqueRequest;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.ItemEstoqueResponse;
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
    private final CalculadoraPedidoService calculadoraService;
    private final EstoqueGatewayClient estoqueClient;

    @Autowired
    public PedidosService(PedidosRepository pedidosRepository,
                          ProdutosRepository produtosRepository,
                          ReceitasRepository receitasRepository,
                          EstoqueRepository estoqueRepository,
                          PagamentoService pagamentoService,
                          CozinhaService cozinhaService,
                          EntregaService entregaService,
                          CalculadoraPedidoService calculadoraService,
                          EstoqueGatewayClient estoqueClient) {
        this.pedidosRepository = pedidosRepository;
        this.produtosRepository = produtosRepository;
        this.receitasRepository = receitasRepository;
        this.estoqueRepository = estoqueRepository;
        this.pagamentoService = pagamentoService;
        this.cozinhaService = cozinhaService;
        this.entregaService = entregaService;
        this.calculadoraService = calculadoraService;
        this.estoqueClient = estoqueClient;
    }

    public Pedido submeterPedido(Cliente cliente, List<ItemPedido> itens) {
        Map<Long, Integer> ingredientesNecessarios = calcularIngredientesNecessarios(itens);
        if (!estoqueSuficiente(ingredientesNecessarios)) {
            return null;
        }

        double valor = calculadoraService.calcularValorTotal(itens);
        double impostos = calculadoraService.calcularImpostos(valor);
        double desconto = calculadoraService.calcularDesconto(valor);
        double valorCobrado = calculadoraService.calcularValorCobrado(valor, impostos, desconto);

        long pedidoId = pedidosRepository.gerarNovoId();
        Pedido pedido = new Pedido(pedidoId, cliente, null, itens, Pedido.Status.APROVADO,
                valor, impostos, desconto, valorCobrado);

        pedidosRepository.salvarPedido(pedido);
        pedidosRepository.salvarItensPedido(pedido.getId(), itens);

        return pedido;
    }

    public Pedido consultarStatusPedido(String clienteCpf, long pedidoId) {
        Pedido pedido = pedidosRepository.recuperarPedidoPorId(pedidoId);
        if (pedido == null || !pedido.getCliente().getCpf().equals(clienteCpf)) {
            return null;
        }
        return pedido;
    }

    public Pedido cancelarPedido(String clienteCpf, long pedidoId) {
        Pedido pedido = pedidosRepository.recuperarPedidoPorId(pedidoId);
        if (pedido == null || !pedido.getCliente().getCpf().equals(clienteCpf)) {
            return null;
        }
        if (pedido.getStatus() != Pedido.Status.APROVADO && pedido.getStatus() != Pedido.Status.PAGO) {
            return null;
        }

        if (pedido.getStatus() == Pedido.Status.PAGO) {
            restaurarEstoqueAposCancelamento(pedido);
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
        if (pedido.getStatus() != Pedido.Status.APROVADO) {
            return null;
        }

        if (pagamentoService.processarPagamento(clienteCpf, pedidoId)) {
            atualizarEstoqueAposPagamento(pedido);
            cozinhaService.chegadaDePedido(pedido);
            pedidosRepository.atualizarStatusPedido(pedidoId, Pedido.Status.PAGO);
            pedido.setStatus(Pedido.Status.PAGO);
            return pedido;
        }
        return null;
    }

    public List<Pedido> recuperaPedidosPorDatas(LocalDateTime data1, LocalDateTime data2) {
        return pedidosRepository.recuperaPedidosPorDatas(data1, data2);
    }

    public List<Pedido> recuperaPedidosPorClienteEDatas(LocalDateTime data1, LocalDateTime data2, String cpf) {
        return pedidosRepository.recuperaPedidosPorClienteEDatas(data1, data2, cpf);
    }

    public void atualizarEstoqueAposPagamento(Pedido pedido) {
        Map<Long, Integer> ingredientes = calcularIngredientesNecessarios(pedido.getItens());
        ingredientes.forEach((id, qtd) -> {
            estoqueClient.debitar(new AtualizarEstoqueRequest(id, qtd));
        });
    }

    public void restaurarEstoqueAposCancelamento(Pedido pedido) {
        Map<Long, Integer> ingredientes = calcularIngredientesNecessarios(pedido.getItens());
        ingredientes.forEach((id, qtd) -> {
            estoqueClient.creditar(new AtualizarEstoqueRequest(id, qtd));
        });
    }

    private Map<Long, Integer> calcularIngredientesNecessarios(List<ItemPedido> itens) {
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
        return ingredientesNecessarios;
    }

    private boolean estoqueSuficiente(Map<Long, Integer> ingredientesNecessarios) {
        return ingredientesNecessarios.entrySet().stream().allMatch(entry -> {
            ItemEstoqueResponse dto = estoqueClient.getItem(entry.getKey());
            if (dto == null) return false;
            int quantidadeDisponivel = dto.quantidade();
            return quantidadeDisponivel >= entry.getValue();
        });
    }
}
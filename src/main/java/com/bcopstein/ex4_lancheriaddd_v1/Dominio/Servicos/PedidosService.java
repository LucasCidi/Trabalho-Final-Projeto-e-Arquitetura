package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.PedidosUC;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.EstoqueRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.PedidosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ProdutosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ReceitasRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;


@Service
public class PedidosService {
    private final PedidosRepository pedidosRepository;
    private final ProdutosRepository produtosRepository;
    private final ReceitasRepository receitasRepository;
    private final EstoqueRepository estoqueRepository;
    private final PagamentoService pagamentoService;
    private final CozinhaService cozinhaService;
    private final EntregaService entregaService;
    private final PedidosUC pedidosUC;

    @Autowired
    public PedidosService(PedidosRepository pedidosRepository, ProdutosRepository produtosRepository,
                          ReceitasRepository receitasRepository, EstoqueRepository estoqueRepository,
                          PagamentoService pagamentoService, CozinhaService cozinhaService,
                          EntregaService entregaService, PedidosUC pedidosUC) {
        this.pedidosRepository = pedidosRepository;
        this.produtosRepository = produtosRepository;
        this.receitasRepository = receitasRepository;
        this.estoqueRepository = estoqueRepository;
        this.pagamentoService = pagamentoService;
        this.cozinhaService = cozinhaService;
        this.entregaService = entregaService;
        this.pedidosUC = pedidosUC;
    }

    public Pedido submeterPedido(Cliente cliente, List<ItemPedido> itens) {
        return pedidosUC.submeterPedido(cliente, itens);
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
        if (pedido.getStatus() != Pedido.Status.APROVADO) {
            return null;
        }

        // Restaure o estoque se o pedido foi pago
        if (pedido.getStatus() == Pedido.Status.PAGO) {
            pedidosUC.restaurarEstoqueAposCancelamento(pedido);
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
            pedidosUC.atualizarEstoqueAposPagamento(pedido); // Update stock after payment
            cozinhaService.chegadaDePedido(pedido);
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
}
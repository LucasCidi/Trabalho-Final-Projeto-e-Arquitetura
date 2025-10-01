package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.PedidosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PagamentoService {
    private final PedidosRepository pedidosRepository;

    @Autowired
    public PagamentoService(PedidosRepository pedidosRepository) {
        this.pedidosRepository = pedidosRepository;
    }

    public boolean processarPagamento(String clienteCpf, long pedidoId) {
        Pedido pedido = pedidosRepository.recuperarPedidoPorId(pedidoId);
        if (pedido == null || !pedido.getCliente().getCpf().equals(clienteCpf)) {
            return false;
        }
        if (pedido.getStatus() != Pedido.Status.APROVADO) {
            return false;
        }
        pedido.setStatus(Pedido.Status.PAGO);
        pedido.setDataHoraPagamento(LocalDateTime.now());
        pedidosRepository.atualizarStatusPedido(pedidoId, Pedido.Status.PAGO);
        pedidosRepository.atualizarDataHoraPagamento(pedidoId, LocalDateTime.now());
        return true;
    }
}
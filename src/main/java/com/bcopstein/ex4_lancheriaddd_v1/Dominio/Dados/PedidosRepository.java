package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.ItemPedido;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;

import java.util.List;

public interface PedidosRepository {
    void salvarPedido(Pedido pedido);
    void salvarItensPedido(long pedidoId, List<ItemPedido> itens);
    long gerarNovoId();
    Pedido recuperarPedidoPorId(long pedidoId);
    void atualizarStatusPedido(long pedidoId, Pedido.Status status);
}

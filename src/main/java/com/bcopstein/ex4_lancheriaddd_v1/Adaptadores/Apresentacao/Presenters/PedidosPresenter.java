package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao.Presenters;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;

public class PedidosPresenter {
    // Dados de entrada
    private long produtoId;
    private int quantidade;

    // Dados de saída
    private Long pedidoId;
    private String mensagem;
    private Double valorTotal;
    private String status;

    // requisição
    public PedidosPresenter() {}

    public PedidosPresenter(long produtoId, int quantidade) {
        this.produtoId = produtoId;
        this.quantidade = quantidade;
    }

    // resposta
    public PedidosPresenter(Pedido pedido, String mensagem) {
        this.pedidoId = pedido != null ? pedido.getId() : null;
        this.mensagem = mensagem;
        this.valorTotal = pedido != null ? pedido.getValorCobrado() : null;
        this.status = pedido != null ? pedido.getStatus().name() : null;
    }

    public long getProdutoId() { return produtoId; }
    public void setProdutoId(long produtoId) { this.produtoId = produtoId; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public Long getPedidoId() { return pedidoId; }
    public String getMensagem() { return mensagem; }
    public Double getValorTotal() { return valorTotal; }
    public String getStatus() { return status; }
}

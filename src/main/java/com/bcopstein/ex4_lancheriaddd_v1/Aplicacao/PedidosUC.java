package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao.Presenters.PedidosPresenter;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ProdutosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.*;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.PedidosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PedidosUC {
    private final PedidosService pedidosService;
    private final ProdutosRepository produtosRepository;

    @Autowired
    public PedidosUC(PedidosService pedidosService, ProdutosRepository produtosRepository) {
        this.pedidosService = pedidosService;
        this.produtosRepository = produtosRepository;
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

        Pedido pedido = pedidosService.submeterPedido(cliente, itens);
        if (pedido == null) {
            return new PedidosPresenter(null, "Pedido negado: falta de ingredientes");
        }

        return new PedidosPresenter(pedido, "Pedido aprovado com sucesso");
    }
}
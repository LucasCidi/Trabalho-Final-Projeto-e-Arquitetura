package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.ItemPedido;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalculadoraPedidoService {
    public double calcularValorTotal(List<ItemPedido> itens) {
        double valor = 0.0;
        for (ItemPedido item : itens) {
            valor += item.getItem().getPreco() * item.getQuantidade() / 100.0;
        }
        return valor;
    }

    public double calcularImpostos(double valor) {
        return valor * 0.10;
    }

    public double calcularDesconto(double valor) {
        return 0.0;
    }

    public double calcularValorCobrado(double valor, double impostos, double desconto) {
        return valor + impostos - desconto;
    }
}
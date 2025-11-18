package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.PedidosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CozinhaService {
    private Queue<Pedido> filaEntrada;
    private Pedido emPreparacao;
    private Queue<Pedido> filaSaida;
    private ScheduledExecutorService scheduler;
    private final PedidosRepository pedidosRepository;
    private final EntregaService entregaService;

    @Autowired
    public CozinhaService(PedidosRepository pedidosRepository, EntregaService entregaService) {
        this.pedidosRepository = pedidosRepository;
        this.entregaService = entregaService;
        filaEntrada = new LinkedBlockingQueue<>();
        emPreparacao = null;
        filaSaida = new LinkedBlockingQueue<>();
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    private synchronized void colocaEmPreparacao(Pedido pedido) {
        pedido.setStatus(Pedido.Status.PREPARACAO);
        pedidosRepository.atualizarStatusPedido(pedido.getId(), Pedido.Status.PREPARACAO);
        emPreparacao = pedido;
        System.out.println("Pedido em preparacao: " + pedido);
        scheduler.schedule(() -> pedidoPronto(), 5, TimeUnit.SECONDS);
    }

    public synchronized void chegadaDePedido(Pedido p) {
        filaEntrada.add(p);
        System.out.println("Pedido na fila de entrada: " + p);
        if (emPreparacao == null) {
            colocaEmPreparacao(filaEntrada.poll());
        }
    }

    private synchronized void pedidoPronto() {
        emPreparacao.setStatus(Pedido.Status.PRONTO);
        pedidosRepository.atualizarStatusPedido(emPreparacao.getId(), Pedido.Status.PRONTO);
        filaSaida.add(emPreparacao);
        System.out.println("Pedido na fila de saida: " + emPreparacao);
        entregaService.chegadaDePedido(emPreparacao);
        emPreparacao = null;
        if (!filaEntrada.isEmpty()) {
            Pedido prox = filaEntrada.poll();
            scheduler.schedule(() -> colocaEmPreparacao(prox), 1, TimeUnit.SECONDS);
        }
    }
}
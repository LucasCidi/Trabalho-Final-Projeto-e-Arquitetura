package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.PedidosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class EntregaService {
    private Queue<Pedido> filaEntrega;
    private Pedido emTransporte;
    private ScheduledExecutorService scheduler;
    private final PedidosRepository pedidosRepository;

    @Autowired
    public EntregaService(PedidosRepository pedidosRepository) {
        this.pedidosRepository = pedidosRepository;
        filaEntrega = new LinkedBlockingQueue<>();
        emTransporte = null;
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public synchronized void chegadaDePedido(Pedido pedido) {
        filaEntrega.add(pedido);
        System.out.println("Pedido na fila de entrega: " + pedido);
        if (emTransporte == null) {
            iniciarTransporte(filaEntrega.poll());
        }
    }

    private synchronized void iniciarTransporte(Pedido pedido) {
        pedido.setStatus(Pedido.Status.TRANSPORTE);
        pedidosRepository.atualizarStatusPedido(pedido.getId(), Pedido.Status.TRANSPORTE);
        emTransporte = pedido;
        System.out.println("Pedido em transporte: " + pedido);
        scheduler.schedule(() -> pedidoEntregue(), 5, TimeUnit.SECONDS);
    }

    private synchronized void pedidoEntregue() {
        emTransporte.setStatus(Pedido.Status.ENTREGUE);
        pedidosRepository.atualizarStatusPedido(emTransporte.getId(), Pedido.Status.ENTREGUE);
        System.out.println("Pedido entregue: " + emTransporte);
        emTransporte = null;
        if (!filaEntrega.isEmpty()) {
            Pedido prox = filaEntrega.poll();
            scheduler.schedule(() -> iniciarTransporte(prox), 1, TimeUnit.SECONDS);
        }
    }
}

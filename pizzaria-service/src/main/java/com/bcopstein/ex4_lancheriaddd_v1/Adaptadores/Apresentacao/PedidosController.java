package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao.Presenters.PedidosPresenter;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.PedidosUC;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.PedidosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pedidos")
public class PedidosController {
    private final PedidosService pedidosService;
    private final PedidosUC pedidosUC;

    @Autowired
    public PedidosController(PedidosService pedidosService, PedidosUC pedidosUC) {
        this.pedidosService = pedidosService;
        this.pedidosUC = pedidosUC;
    }

    @PostMapping("/submeter")
    public ResponseEntity<PedidosPresenter> submeterPedido(@RequestBody List<PedidosPresenter> itensRequest) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String cpf = userDetails.getUsername();
        try {
            PedidosPresenter response = pedidosUC.submeterPedidoFromRequest(cpf, itensRequest);
            return response.getPedidoId() == null ? ResponseEntity.badRequest().body(response) : ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new PedidosPresenter(null, e.getMessage()));
        }
    }

    @GetMapping("/status/{pedidoId}")
    public ResponseEntity<PedidosPresenter> consultarStatusPedido(@PathVariable long pedidoId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String cpf = userDetails.getUsername();

        Pedido pedido = pedidosService.consultarStatusPedido(cpf, pedidoId);
        if (pedido == null) {
            return ResponseEntity.badRequest().body(new PedidosPresenter(null, "Pedido não encontrado ou não pertence ao cliente"));
        }

        PedidosPresenter response = new PedidosPresenter(pedido, "Status do pedido recuperado com sucesso");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancelar/{pedidoId}")
    public ResponseEntity<PedidosPresenter> cancelarPedido(@PathVariable long pedidoId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String cpf = userDetails.getUsername();

        Pedido pedido = pedidosService.cancelarPedido(cpf, pedidoId);
        if (pedido == null) {
            return ResponseEntity.badRequest().body(new PedidosPresenter(null, "Pedido não encontrado, não pertence ao cliente ou não pode ser cancelado"));
        }

        PedidosPresenter response = new PedidosPresenter(pedido, "Pedido cancelado com sucesso");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/pagar/{pedidoId}")
    public ResponseEntity<PedidosPresenter> pagarPedido(@PathVariable long pedidoId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String cpf = userDetails.getUsername();

        Pedido pedido = pedidosService.pagarPedido(cpf, pedidoId);
        if (pedido == null) {
            return ResponseEntity.badRequest().body(new PedidosPresenter(null, "Pagamento não processado: pedido não encontrado, não pertence ao cliente ou não está aprovado"));
        }

        PedidosPresenter response = new PedidosPresenter(pedido, "Pagamento processado com sucesso");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/entregue")
    public ResponseEntity<List<PedidosPresenter>> pedidosEntregados(@RequestParam("data1") LocalDateTime data1, @RequestParam("data2") LocalDateTime data2) {
        List<Pedido> pedidos = pedidosService.recuperaPedidosPorDatas(data1, data2);

        List<PedidosPresenter> pedidosPresenter = pedidos.stream()
                .map(pedido -> new PedidosPresenter(pedido, null))
                .collect(Collectors.toList());
        return ResponseEntity.ok(pedidosPresenter);
    }

    @GetMapping("/entregueParaCliente")
    public ResponseEntity<List<PedidosPresenter>> pedidosEntreguesParaCliente(@RequestParam("data1") LocalDateTime data1,
                                                                              @RequestParam("data2") LocalDateTime data2,
                                                                              @RequestParam("cpf") String cpf) {
        List<Pedido> pedidos = pedidosService.recuperaPedidosPorClienteEDatas(data1, data2, cpf);

        List<PedidosPresenter> pedidosPresenter = pedidos.stream()
                .map(pedido -> new PedidosPresenter(pedido, null))
                .collect(Collectors.toList());

        return ResponseEntity.ok(pedidosPresenter);
    }
}
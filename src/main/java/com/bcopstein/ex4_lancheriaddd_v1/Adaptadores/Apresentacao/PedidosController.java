package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao.Presenters.PedidosPresenter;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ProdutosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.ItemPedido;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Produto;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.PedidosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pedidos")
public class PedidosController {
    private final PedidosService pedidosService;
    private final ProdutosRepository produtosRepository;

    @Autowired
    public PedidosController(PedidosService pedidosService, ProdutosRepository produtosRepository) {
        this.pedidosService = pedidosService;
        this.produtosRepository = produtosRepository;
    }

    @PostMapping("/submeter")
    public ResponseEntity<PedidosPresenter> submeterPedido(@RequestBody List<PedidosPresenter> itensRequest) {
        // Obter o cliente autenticado
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String cpf = userDetails.getUsername();
        Cliente cliente = new Cliente(cpf, "", "", true, "", "", ""); // Dados completos podem ser buscados, se necessário

        // Converter requisição para lista de ItemPedido
        List<ItemPedido> itens = itensRequest.stream().map(itemRequest -> {
            Produto produto = produtosRepository.recuperaProdutoPorid(itemRequest.getProdutoId());
            if (produto == null) {
                throw new IllegalArgumentException("Produto não encontrado: " + itemRequest.getProdutoId());
            }
            return new ItemPedido(produto, itemRequest.getQuantidade());
        }).collect(Collectors.toList());

        // Submeter o pedido
        Pedido pedido = pedidosService.submeterPedido(cliente, itens);
        if (pedido == null) {
            return ResponseEntity.badRequest().body(new PedidosPresenter(null, "Pedido negado: falta de ingredientes"));
        }

        // Retornar resposta
        PedidosPresenter response = new PedidosPresenter(pedido, "Pedido aprovado com sucesso");
        return ResponseEntity.ok(response);
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
}

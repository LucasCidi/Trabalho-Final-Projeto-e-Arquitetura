package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.PedidosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ProdutosRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.ItemPedido;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Pedido;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Produto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PedidosRepositoryJDBC implements PedidosRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ProdutosRepository produtosRepository;

    @Autowired
    public PedidosRepositoryJDBC(JdbcTemplate jdbcTemplate, ProdutosRepository produtosRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.produtosRepository = produtosRepository;
    }

    @Override
    public void salvarPedido(Pedido pedido) {
        String sql = "INSERT INTO pedidos (id, cliente_cpf, data_hora_pagamento, status, valor, impostos, desconto, valor_cobrado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                pedido.getId(),
                pedido.getCliente().getCpf(),
                pedido.getDataHoraPagamento(),
                pedido.getStatus().name(),
                pedido.getValor(),
                pedido.getImpostos(),
                pedido.getDesconto(),
                pedido.getValorCobrado()
        );
    }

    @Override
    public void salvarItensPedido(long pedidoId, List<ItemPedido> itens) {
        String sql = "INSERT INTO itens_pedido (pedido_id, produto_id, quantidade) VALUES (?, ?, ?)";
        for (ItemPedido item : itens) {
            jdbcTemplate.update(sql, pedidoId, item.getItem().getId(), item.getQuantidade());
        }
    }

    @Override
    public long gerarNovoId() {
        String sql = "SELECT COALESCE(MAX(id), 0) + 1 FROM pedidos";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    @Override
    public Pedido recuperarPedidoPorId(long pedidoId) {
        String sql = "SELECT id, cliente_cpf, data_hora_pagamento, status, valor, impostos, desconto, valor_cobrado " +
                "FROM pedidos WHERE id = ?";
        List<Pedido> pedidos = jdbcTemplate.query(sql,
                ps -> ps.setLong(1, pedidoId),
                (rs, rowNum) -> {
                    long id = rs.getLong("id");
                    String clienteCpf = rs.getString("cliente_cpf");
                    LocalDateTime dataHoraPagamento = rs.getTimestamp("data_hora_pagamento") != null
                            ? rs.getTimestamp("data_hora_pagamento").toLocalDateTime()
                            : null;
                    Pedido.Status status = Pedido.Status.valueOf(rs.getString("status"));
                    double valor = rs.getDouble("valor");
                    double impostos = rs.getDouble("impostos");
                    double desconto = rs.getDouble("desconto");
                    double valorCobrado = rs.getDouble("valor_cobrado");

                    Cliente cliente = new Cliente(clienteCpf, "", "", true, "", "", "");
                    List<ItemPedido> itens = recuperarItensPedido(id);

                    return new Pedido(id, cliente, dataHoraPagamento, itens, status, valor, impostos, desconto, valorCobrado);
                }
        );
        return pedidos.isEmpty() ? null : pedidos.getFirst();
    }

    private List<ItemPedido> recuperarItensPedido(long pedidoId) {
        String sql = "SELECT produto_id, quantidade FROM itens_pedido WHERE pedido_id = ?";
        return jdbcTemplate.query(sql,
                ps -> ps.setLong(1, pedidoId),
                (rs, rowNum) -> {
                    long produtoId = rs.getLong("produto_id");
                    int quantidade = rs.getInt("quantidade");
                    Produto produto = produtosRepository.recuperaProdutoPorid(produtoId);
                    return new ItemPedido(produto, quantidade);
                }
        );
    }

    @Override
    public void atualizarStatusPedido(long pedidoId, Pedido.Status status) {
        String sql = "UPDATE pedidos SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status.name(), pedidoId);
    }

    @Override
    public void atualizarDataHoraPagamento(long pedidoId, LocalDateTime dataHoraPagamento) {
        String sql = "UPDATE pedidos SET data_hora_pagamento = ? WHERE id = ?";
        jdbcTemplate.update(sql, dataHoraPagamento, pedidoId);
    }
}
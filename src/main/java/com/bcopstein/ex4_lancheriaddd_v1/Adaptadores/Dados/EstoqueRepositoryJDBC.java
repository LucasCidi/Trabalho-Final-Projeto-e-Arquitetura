package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.EstoqueRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Ingrediente;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.ItemEstoque;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class EstoqueRepositoryJDBC implements EstoqueRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public EstoqueRepositoryJDBC(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ItemEstoque recuperaItemEstoque(long ingredienteId) {
        String sql = "SELECT id, quantidade, ingrediente_id FROM itensEstoque WHERE ingrediente_id = ?";
        return jdbcTemplate.query(sql,
                ps -> ps.setLong(1, ingredienteId),
                (rs, rowNum) -> new ItemEstoque(
                        new Ingrediente(rs.getLong("ingrediente_id"), ""),
                        rs.getInt("quantidade")
                )
        ).stream().findFirst().orElse(null);
    }

    @Override
    public void atualizarEstoque(long ingredienteId, int quantidadeUsada) {
        String sql = "UPDATE itensEstoque SET quantidade = quantidade - ? WHERE ingrediente_id = ?";
        jdbcTemplate.update(sql, quantidadeUsada, ingredienteId);
    }

    @Override
    public void restaurarEstoque(long ingredienteId, int quantidadeUsada) {
        String sql = "UPDATE itensEstoque SET quantidade = quantidade + ? WHERE ingrediente_id = ?";
        jdbcTemplate.update(sql, quantidadeUsada, ingredienteId);
    }
}

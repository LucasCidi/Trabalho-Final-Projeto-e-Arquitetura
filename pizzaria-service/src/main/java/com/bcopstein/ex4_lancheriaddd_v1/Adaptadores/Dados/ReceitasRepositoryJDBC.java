
package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Ingrediente;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Receita;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.IngredientesRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ReceitasRepository;

@Repository
public class ReceitasRepositoryJDBC implements ReceitasRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReceitasRepositoryJDBC(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Receita recuperaReceita(long id) {
        String sql = "SELECT id, titulo FROM receitas WHERE id = ?";
        return jdbcTemplate.query(sql,
                ps -> ps.setLong(1, id),
                (rs, rowNum) -> new Receita(rs.getLong("id"), rs.getString("titulo"))
        ).stream().findFirst().orElse(null);
    }

    @Override
    public List<Ingrediente> recuperaIngredientesReceita(long receitaId) {
        String sql = "SELECT i.id, i.descricao FROM ingredientes i " +
                "JOIN receita_ingrediente ri ON i.id = ri.ingrediente_id " +
                "WHERE ri.receita_id = ?";
        return jdbcTemplate.query(sql,
                ps -> ps.setLong(1, receitaId),
                (rs, rowNum) -> new Ingrediente(rs.getLong("id"), rs.getString("descricao"))
        );
    }

}



package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Dados;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados.ClienteRepository;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ClienteRepositoryJDBC  implements ClienteRepository {
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public ClienteRepositoryJDBC(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void save(Cliente cliente) {
        String sqlCliente = "INSERT INTO clientes (cpf, nome, password, enabled, celular, endereco, email) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String encodedPassword = passwordEncoder.encode(cliente.getPassword());
        jdbcTemplate.update(sqlCliente,
                cliente.getCpf(),
                cliente.getNome(),
                encodedPassword,
                cliente.getEnabled() != null ? cliente.getEnabled() : true, // Default to true if not provided
                cliente.getCelular(),
                cliente.getEndereco(),
                cliente.getEmail()
        );

        String sqlAuthority = "INSERT INTO authorities (cpf, authority) VALUES (?, ?)";
        jdbcTemplate.update(sqlAuthority, cliente.getCpf(), "ROLE_USER");
    }

    @Override
    public Optional<Cliente> findByCpf(String cpf) {
        String sql = "SELECT * FROM clientes WHERE cpf = ?";
        try {
            Cliente cliente = jdbcTemplate.queryForObject(sql, new Object[]{cpf}, (rs, rowNum) ->
                    new Cliente(
                            rs.getString("cpf"),
                            rs.getString("nome"),
                            rs.getString("password"),
                            rs.getBoolean("enabled"),
                            rs.getString("celular"),
                            rs.getString("endereco"),
                            rs.getString("email")
                    )
            );
            return Optional.ofNullable(cliente);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}

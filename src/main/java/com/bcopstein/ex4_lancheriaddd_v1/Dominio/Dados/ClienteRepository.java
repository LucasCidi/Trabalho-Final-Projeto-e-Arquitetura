package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;

import java.util.Optional;

public interface ClienteRepository {
    void save(Cliente cliente);
    Optional<Cliente> findByCpf(String cpf);
}

package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao.Presenters;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ClientePresenter {
    public Map<String, String> present(Cliente cliente) {
        Map<String, String> response = new HashMap<>();
        response.put("cpf", cliente.getCpf());
        response.put("nome", cliente.getNome());
        response.put("email", cliente.getEmail());
        response.put("mensagem", "Cliente registrado com sucesso");
        return response;
    }

    public Map<String, String> presentError(String mensagem) {
        Map<String, String> response = new HashMap<>();
        response.put("erro", mensagem);
        return response;
    }
}


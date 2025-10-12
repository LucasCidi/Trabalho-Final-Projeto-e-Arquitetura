package com.bcopstein.ex4_lancheriaddd_v1.Aplicacao;

import com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao.Presenters.ClientePresenter;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Servicos.ClienteService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ClienteUC {
    private final ClienteService clienteService;
    private final ClientePresenter clientePresenter;

    public ClienteUC(ClienteService clienteService, ClientePresenter clientePresenter) {
        this.clienteService = clienteService;
        this.clientePresenter = clientePresenter;
    }

    public Map<String, String> registrarCliente(Cliente cliente) {
        try {
            Cliente registeredCliente = clienteService.registrarCliente(cliente);
            return clientePresenter.present(registeredCliente);
        } catch (IllegalArgumentException e) {
            return clientePresenter.presentError(e.getMessage());
        }
    }
}

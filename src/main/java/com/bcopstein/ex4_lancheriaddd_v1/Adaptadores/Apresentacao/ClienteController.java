package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Apresentacao;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.ClienteUC;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Cliente;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteUC clienteUC;

    public ClienteController(ClienteUC clienteUC) {
        this.clienteUC = clienteUC;
    }

    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrar(@RequestBody Cliente cliente) {
        Map<String, String> response = clienteUC.registrarCliente(cliente);
        if (response.containsKey("erro")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}

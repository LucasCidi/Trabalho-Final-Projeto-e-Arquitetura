package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Http;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Requests.AtualizarEstoqueRequest;
import com.bcopstein.ex4_lancheriaddd_v1.Aplicacao.Responses.ItemEstoqueResponse;

@FeignClient(
        name = "gateway-estoque",
        url = "${gateway.url}",              // http://localhost:8080
        configuration = com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Http.FeignAuthConfig.class
)
public interface EstoqueGatewayClient {

    @GetMapping("/ingredientes/{ingredienteId}")
    ItemEstoqueResponse getItem(@PathVariable("ingredienteId") long ingredienteId);

    @PostMapping("/ingredientes/debitar")
    void debitar(@RequestBody AtualizarEstoqueRequest req);

    @PostMapping("/ingredientes/creditar")
    void creditar(@RequestBody AtualizarEstoqueRequest req);
}
package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.dto.AtualizarEstoqueRequest;
import com.example.demo.dto.ItemEstoqueResponse;

@RestController
@RequestMapping("/estoque")
public class EstoqueController {

    private final EstoqueService estoqueService;

    public EstoqueController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    @GetMapping("/{ingredienteId}")
    public ResponseEntity<ItemEstoqueResponse> getItem(@PathVariable long ingredienteId) {
        ItemEstoque item = estoqueService.buscarPorIngrediente(ingredienteId);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(
                new ItemEstoqueResponse(
                        item.getIngrediente().getId(),
                        item.getQuantidade()
                )
        );
    }

    @PostMapping("/debitar")
    public ResponseEntity<Void> debitar(@RequestBody AtualizarEstoqueRequest req) {
        estoqueService.debitar(req.ingredienteId(), req.quantidade());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/creditar")
    public ResponseEntity<Void> creditar(@RequestBody AtualizarEstoqueRequest req) {
        estoqueService.creditar(req.ingredienteId(), req.quantidade());
        return ResponseEntity.ok().build();
    }
}

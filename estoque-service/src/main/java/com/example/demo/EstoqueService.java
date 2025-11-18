package com.example.demo;

import org.springframework.stereotype.Service;

@Service
public class EstoqueService {

    private final ItemEstoqueRepository itemEstoqueRepository;

    public EstoqueService(ItemEstoqueRepository itemEstoqueRepository) {
        this.itemEstoqueRepository = itemEstoqueRepository;
    }

    public ItemEstoque buscarPorIngrediente(long ingredienteId) {
        return itemEstoqueRepository
                .findByIngredienteId(ingredienteId)
                .orElse(null);
    }

    public void debitar(long ingredienteId, int quantidade) {
        ItemEstoque item = itemEstoqueRepository
                .findByIngredienteId(ingredienteId)
                .orElseThrow(() -> new RuntimeException("Ingrediente não encontrado no estoque"));

        if (item.getQuantidade() < quantidade) {
            throw new RuntimeException("Estoque insuficiente");
        }

        item.setQuantidade(item.getQuantidade() - quantidade);
        itemEstoqueRepository.save(item);
    }

    public void creditar(long ingredienteId, int quantidade) {
        ItemEstoque item = itemEstoqueRepository
                .findByIngredienteId(ingredienteId)
                .orElseThrow(() -> new RuntimeException("Ingrediente não encontrado no estoque"));

        item.setQuantidade(item.getQuantidade() + quantidade);
        itemEstoqueRepository.save(item);
    }
}
package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "itens_estoque")
public class ItemEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ingrediente_id")
    private Ingrediente ingrediente;

    private int quantidade;

    public ItemEstoque(Ingrediente ingrediente, int quantidade) {
        this.ingrediente = ingrediente;
        this.quantidade = quantidade;
    }

    public Long getId() { return id; }
    public Ingrediente getIngrediente() { return ingrediente; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
}
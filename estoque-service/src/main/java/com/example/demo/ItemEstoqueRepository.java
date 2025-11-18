package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemEstoqueRepository extends JpaRepository<ItemEstoque, Long> {
    Optional<ItemEstoque> findByIngredienteId(Long ingredienteId);
}
package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.ItemEstoque;

public interface EstoqueRepository {
    ItemEstoque recuperaItemEstoque(long ingredienteId);
    void atualizarEstoque(long ingredienteId, int quantidadeUsada);
}

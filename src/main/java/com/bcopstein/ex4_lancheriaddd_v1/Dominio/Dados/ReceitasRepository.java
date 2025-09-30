package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Dados;

import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Ingrediente;
import com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades.Receita;

import java.util.List;

public interface ReceitasRepository {
    Receita recuperaReceita(long id);
    List<Ingrediente> recuperaIngredientesReceita(long receitaId);
}
    


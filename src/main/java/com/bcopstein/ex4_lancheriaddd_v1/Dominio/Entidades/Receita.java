package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades;

import java.util.List;

public class Receita {
    private long id;
    private String titulo;

    public Receita(long id, String titulo) {
        this.id = id;
        this.titulo = titulo;
    }

    public long getId() { return id; }
    public String getTitulo() { return titulo; }
}

package com.bcopstein.ex4_lancheriaddd_v1.Dominio.Entidades;

public class Cliente {
    private String cpf;
    private String nome;
    private String password;
    private Boolean enabled;
    private String celular;
    private String endereco;
    private String email;

    public Cliente(String cpf, String nome, String password, Boolean enabled, String celular, String endereco, String email) {
        this.cpf = cpf;
        this.nome = nome;
        this.password = password;
        this.enabled = enabled;
        this.celular = celular;
        this.endereco = endereco;
        this.email = email;
    }

    public String getCpf() { return cpf; }
    public String getNome() { return nome; }
    public String getPassword() { return password; }
    public Boolean getEnabled() { return enabled; }
    public String getCelular() { return celular; }
    public String getEndereco() { return endereco; }
    public String getEmail() { return email; }

    public void setNome(String nome) {
        this.nome = nome;
    }
}

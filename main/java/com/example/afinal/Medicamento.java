package com.example.afinal;

public class Medicamento {
    private String nome;
    private String descricao;
    private String horario;
    private boolean tomado;

    public Medicamento() {
        // Construtor vazio necessário para Firebase
    }

    public Medicamento(String nome, String descricao, String horario, boolean tomado) {
        this.nome = nome;
        this.descricao = descricao;
        this.horario = horario;
        this.tomado = tomado;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public boolean isTomado() {
        return tomado;
    }

    public void setTomado(boolean tomado) {
        this.tomado = tomado;
    }
}
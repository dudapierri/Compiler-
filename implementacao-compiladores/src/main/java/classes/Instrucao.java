package classes;

import javafx.scene.input.Mnemonic;

import java.util.List;

public class Instrucao implements Comparable<Instrucao> {
    public Integer numero;
    public final Mnemonic codigo;
    public Parametro parametro;

    public Instrucao(Mnemonic codigo, Parametro parametro){
        this.numero =0;
        this.codigo = codigo;
        this.parametro = parametro;

    }

    public Instrucao(Integer numero, Mnemonic codigo, Parametro parametro) {
        this.numero = numero;
        this.codigo = codigo;
        this.parametro = parametro;
    }

    public Integer getNumero() {
        return numero;
    }

    public Mnemonic getCodigo() {
        return codigo;
    }

    public Parametro getParametro() {
        return parametro;
    }

    public void setParametro(Parametro parametro) {
        this.parametro = parametro;
    }

    public static void enumerarInstrucoes(List<Instrucao> insList) {
        for (int i = 0; i < insList.size(); i++) {
            insList.get(i).numero = i + 1;
        }
    }


    @Override
    public String toString() {
        return String.format("Instrucao %s -  %s - %s", numero, codigo, parametro);
    }

    @Override
    public int compareTo(Instrucao o) {
        return this.numero.compareTo(o.numero);
    }

    public enum Mnemonic {
        ADD,
        DIV,
        MUL,
        SUB,
        DVW,
        MOD,
        PWR,
        ALB,
        ALI,
        ALR,
        ALS,
        LDB,
        LDI,
        LDR,
        LDS,
        LDV,
        STR,
        AND,
        NOT,
        OR,
        BGE,
        BGR,
        DIF,
        EQL,
        SME,
        SMR,
        JMF,
        JMP,
        JMT,
        STP,
        REA,
        WRT,
        STC
    }




}

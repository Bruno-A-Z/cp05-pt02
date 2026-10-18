package com.java.cp04.Amazin.service;

/** Dados do cartão informados no checkout. O número completo trafega apenas na mensageria e nunca é gravado. */
public record DadosCartao(String numero, String titular, String validade) {

    public String numeroLimpo() {
        return numero == null ? "" : numero.replaceAll("\\D", "");
    }

    public String ultimos4() {
        String n = numeroLimpo();
        return n.length() >= 4 ? n.substring(n.length() - 4) : n;
    }
}

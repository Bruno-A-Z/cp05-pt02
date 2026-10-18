package com.java.cp04.Amazin.banco;

/** Resposta da API do Banco Tranquilo. status: APROVADO ou RECUSADO. */
public record PagamentoApiResponse(
        String transactionId,
        String status,
        String motivo,
        String codigoAutorizacao) {
}

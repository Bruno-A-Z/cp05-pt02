package com.java.cp04.Amazin.messaging;

/** Mensagem de resposta do Banco Tranquilo, publicada na fila de respostas. */
public record PagamentoResposta(
        String transactionId,
        Long pedidoId,
        boolean aprovado,
        String status,          // APROVADO | RECUSADO | ERRO
        String motivo,
        String codigoAutorizacao) {
}

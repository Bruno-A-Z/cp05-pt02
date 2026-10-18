package com.java.cp04.Amazin.messaging;

import java.math.BigDecimal;

/** Mensagem publicada pelo Mercado Express na fila de requisições de pagamento. */
public record PagamentoRequisicao(
        String transactionId,
        Long pedidoId,
        String clienteNome,
        String clienteEmail,
        BigDecimal valor,
        String numeroCartao,
        String nomeTitular) {

    /** Mascara o cartão para logs: **** **** **** 1234 */
    public String cartaoMascarado() {
        if (numeroCartao == null || numeroCartao.length() < 4) {
            return "****";
        }
        return "**** " + numeroCartao.substring(numeroCartao.length() - 4);
    }
}

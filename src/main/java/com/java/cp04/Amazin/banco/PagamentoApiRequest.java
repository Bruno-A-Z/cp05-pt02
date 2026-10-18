package com.java.cp04.Amazin.banco;

import java.math.BigDecimal;

/** Corpo da requisição da API do Banco Tranquilo. */
public record PagamentoApiRequest(
        String transactionId,
        BigDecimal valor,
        String numeroCartao,
        String nomeTitular) {
}

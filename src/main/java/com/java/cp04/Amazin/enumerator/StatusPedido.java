package com.java.cp04.Amazin.enumerator;

public enum StatusPedido {
    PENDENTE,   // aguardando resposta do Banco Tranquilo
    APROVADO,   // pagamento confirmado
    RECUSADO,   // pagamento negado pelo banco
    ERRO        // falha técnica (broker/banco indisponível)
}

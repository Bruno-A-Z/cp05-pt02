package com.java.cp04.Amazin.dto;

import com.java.cp04.Amazin.model.Pedido;

import java.math.BigDecimal;
import java.util.List;

public record CompraResponse(
        Long pedidoId,
        String transactionId,
        String status,
        BigDecimal total,
        String motivo,
        String codigoAutorizacao,
        List<ItemResumo> itens) {

    public record ItemResumo(String nome, int quantidade, BigDecimal precoUnitario, BigDecimal subtotal) {
    }

    public static CompraResponse de(Pedido p) {
        return new CompraResponse(
                p.getId(),
                p.getTransactionId(),
                p.getStatus().name(),
                p.getTotal(),
                p.getMotivo(),
                p.getCodigoAutorizacao(),
                p.getItens().stream()
                        .map(i -> new ItemResumo(i.getNome(), i.getQuantidade(), i.getPrecoUnitario(), i.getSubtotal()))
                        .toList());
    }
}

package com.java.cp04.Amazin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

/** Corpo do POST /api/compras (usado no Postman/Insomnia). */
public record CompraRequest(
        @NotEmpty(message = "Informe ao menos um item.") List<@Valid ItemCompra> itens,
        @NotNull(message = "Informe os dados do cartão.") @Valid Cartao cartao) {

    public record ItemCompra(
            @NotNull(message = "itemId é obrigatório.") Long itemId,
            @Min(value = 1, message = "quantidade deve ser >= 1.") int quantidade) {
    }

    public record Cartao(
            @NotBlank(message = "numero é obrigatório.") @Pattern(regexp = "^[0-9 ]{13,23}$", message = "numero inválido.") String numero,
            @NotBlank(message = "titular é obrigatório.") String titular,
            @NotBlank(message = "validade é obrigatória.") String validade) {
    }
}

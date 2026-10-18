package com.java.cp04.Amazin.exception;

/** Erro de regra de negócio na compra (carrinho vazio, estoque insuficiente, broker indisponível...). */
public class CompraException extends RuntimeException {
    public CompraException(String message) {
        super(message);
    }
}

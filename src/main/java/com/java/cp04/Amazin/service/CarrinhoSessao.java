package com.java.cp04.Amazin.service;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.LinkedHashMap;
import java.util.Map;

/** Carrinho de compras do usuário, mantido na sessão HTTP (itemId → quantidade). */
@Component
@SessionScope
public class CarrinhoSessao {

    private final Map<Long, Integer> itens = new LinkedHashMap<>();

    public void adicionar(Long itemId, int quantidade) {
        if (quantidade > 0) {
            itens.merge(itemId, quantidade, Integer::sum);
        }
    }

    public void remover(Long itemId) {
        itens.remove(itemId);
    }

    public void limpar() {
        itens.clear();
    }

    public Map<Long, Integer> getItens() {
        return Map.copyOf(itens);
    }

    public int quantidadeTotal() {
        return itens.values().stream().mapToInt(Integer::intValue).sum();
    }

    public boolean vazio() {
        return itens.isEmpty();
    }
}

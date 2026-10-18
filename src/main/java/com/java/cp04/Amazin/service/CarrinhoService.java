package com.java.cp04.Amazin.service;

import com.java.cp04.Amazin.model.Item;
import com.java.cp04.Amazin.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CarrinhoService {

    private final ItemRepository itemRepository;

    public record Linha(Item item, int quantidade, BigDecimal subtotal) {
    }

    public record Resumo(List<Linha> linhas, BigDecimal total) {
    }

    /** Monta as linhas do carrinho com preço atual do banco. Produtos excluídos são ignorados. */
    public Resumo resumir(CarrinhoSessao carrinho) {
        List<Linha> linhas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> e : carrinho.getItens().entrySet()) {
            Item item = itemRepository.findById(e.getKey()).orElse(null);
            if (item == null) {
                carrinho.remover(e.getKey());
                continue;
            }
            BigDecimal preco = BigDecimal.valueOf(item.getPrice() == null ? 0.0 : item.getPrice())
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal subtotal = preco.multiply(BigDecimal.valueOf(e.getValue()));
            linhas.add(new Linha(item, e.getValue(), subtotal));
            total = total.add(subtotal);
        }
        return new Resumo(linhas, total);
    }

    public List<LinhaCompra> paraCompra(CarrinhoSessao carrinho) {
        return carrinho.getItens().entrySet().stream()
                .map(e -> new LinhaCompra(e.getKey(), e.getValue()))
                .toList();
    }
}

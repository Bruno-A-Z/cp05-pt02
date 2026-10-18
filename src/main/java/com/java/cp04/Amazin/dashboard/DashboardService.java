package com.java.cp04.Amazin.dashboard;

import com.java.cp04.Amazin.enumerator.StatusPedido;
import com.java.cp04.Amazin.model.Item;
import com.java.cp04.Amazin.model.ItemPedido;
import com.java.cp04.Amazin.model.Pedido;
import com.java.cp04.Amazin.repository.EmailLogRepository;
import com.java.cp04.Amazin.repository.ItemRepository;
import com.java.cp04.Amazin.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int ESTOQUE_BAIXO = 10;

    private final PedidoRepository pedidoRepository;
    private final ItemRepository itemRepository;
    private final EmailLogRepository emailLogRepository;

    @Transactional(readOnly = true)
    public DashboardDados gerar() {
        List<Pedido> todos = pedidoRepository.findAllByOrderByDataCriacaoDesc();
        List<Pedido> aprovados = todos.stream().filter(p -> p.getStatus() == StatusPedido.APROVADO).toList();

        BigDecimal receita = soma(aprovados.stream().map(Pedido::getTotal));
        BigDecimal custo = soma(aprovados.stream().map(Pedido::getCustoTotal));
        BigDecimal ticket = aprovados.isEmpty()
                ? BigDecimal.ZERO
                : receita.divide(BigDecimal.valueOf(aprovados.size()), 2, RoundingMode.HALF_UP);
        long itensVendidos = aprovados.stream().flatMap(p -> p.getItens().stream())
                .mapToLong(ItemPedido::getQuantidade).sum();

        long pendentes = contar(todos, StatusPedido.PENDENTE);
        long recusados = contar(todos, StatusPedido.RECUSADO) + contar(todos, StatusPedido.ERRO);

        DashboardDados.Kpis kpis = new DashboardDados.Kpis(
                receita, custo, receita.subtract(custo),
                aprovados.size(), pendentes, recusados, ticket, itensVendidos, emailLogRepository.count());

        // Vendas e lucro por dia
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        Map<LocalDate, List<Pedido>> porDia = new TreeMap<>(aprovados.stream()
                .collect(Collectors.groupingBy(p -> p.getDataCriacao().toLocalDate())));
        List<String> dias = new ArrayList<>();
        List<BigDecimal> receitaDia = new ArrayList<>();
        List<BigDecimal> lucroDia = new ArrayList<>();
        porDia.forEach((dia, lista) -> {
            dias.add(dia.format(fmt));
            BigDecimal r = soma(lista.stream().map(Pedido::getTotal));
            receitaDia.add(r);
            lucroDia.add(r.subtract(soma(lista.stream().map(Pedido::getCustoTotal))));
        });

        // Receita por setor
        Map<String, BigDecimal> porSetor = new TreeMap<>();
        // Quantidade por produto
        Map<String, Integer> porProduto = new HashMap<>();
        for (Pedido p : aprovados) {
            for (ItemPedido ip : p.getItens()) {
                String setor = ip.getSetor() == null ? "OUTROS" : ip.getSetor().name();
                porSetor.merge(setor, ip.getSubtotal(), BigDecimal::add);
                porProduto.merge(ip.getNome(), ip.getQuantidade(), Integer::sum);
            }
        }
        List<Map.Entry<String, Integer>> top = porProduto.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5).toList();

        // Estoque
        List<Item> itens = itemRepository.findAll();
        itens.sort(Comparator.comparing(Item::getName, String.CASE_INSENSITIVE_ORDER));
        List<String> estoqueLabels = itens.stream().map(Item::getName).toList();
        List<Integer> estoqueValores = itens.stream().map(i -> i.getStock() == null ? 0 : i.getStock()).toList();
        List<String> baixo = itens.stream()
                .filter(i -> (i.getStock() == null ? 0 : i.getStock()) < ESTOQUE_BAIXO)
                .map(i -> i.getName() + " (" + (i.getStock() == null ? 0 : i.getStock()) + ")")
                .toList();

        // Status dos pedidos
        Map<String, Long> status = new LinkedHashMap<>();
        for (StatusPedido s : StatusPedido.values()) {
            status.put(s.name(), contar(todos, s));
        }

        DateTimeFormatter fmtData = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        List<DashboardDados.PedidoResumo> ultimos = todos.stream().limit(10)
                .map(p -> new DashboardDados.PedidoResumo(p.getId(), p.getUsuario().getNome(), p.getTotal(),
                        p.getStatus().name(), p.getDataCriacao().format(fmtData)))
                .toList();

        return new DashboardDados(kpis, dias, receitaDia, lucroDia,
                new ArrayList<>(porSetor.keySet()), new ArrayList<>(porSetor.values()),
                top.stream().map(Map.Entry::getKey).toList(), top.stream().map(Map.Entry::getValue).toList(),
                estoqueLabels, estoqueValores, status, ultimos, baixo);
    }

    private static long contar(List<Pedido> pedidos, StatusPedido s) {
        return pedidos.stream().filter(p -> p.getStatus() == s).count();
    }

    private static BigDecimal soma(java.util.stream.Stream<BigDecimal> valores) {
        return valores.reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

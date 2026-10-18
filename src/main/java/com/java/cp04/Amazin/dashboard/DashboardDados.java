package com.java.cp04.Amazin.dashboard;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Dados do dashboard gerencial (JSON consumido pelo Chart.js). */
public record DashboardDados(
        Kpis kpis,
        List<String> diasLabels,
        List<BigDecimal> receitaPorDia,
        List<BigDecimal> lucroPorDia,
        List<String> setorLabels,
        List<BigDecimal> receitaPorSetor,
        List<String> topLabels,
        List<Integer> topQuantidades,
        List<String> estoqueLabels,
        List<Integer> estoqueValores,
        Map<String, Long> statusPedidos,
        List<PedidoResumo> ultimosPedidos,
        List<String> estoqueBaixo) {

    public record Kpis(
            BigDecimal receita,
            BigDecimal custo,
            BigDecimal lucro,
            long pedidosAprovados,
            long pedidosPendentes,
            long pedidosRecusados,
            BigDecimal ticketMedio,
            long itensVendidos,
            long emailsRegistrados) {
    }

    public record PedidoResumo(Long id, String cliente, BigDecimal total, String status, String data) {
    }
}

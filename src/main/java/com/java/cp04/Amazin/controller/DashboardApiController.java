package com.java.cp04.Amazin.controller;

import com.java.cp04.Amazin.dashboard.DashboardDados;
import com.java.cp04.Amazin.dashboard.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Dados do dashboard gerencial em JSON (somente ADMIN). */
@RestController
@RequestMapping("/admin/dashboard/dados")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Indicadores gerenciais do Mercado Express")
public class DashboardApiController {

    private final DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "Vendas, lucro, estoque e status dos pedidos")
    public DashboardDados dados() {
        return dashboardService.gerar();
    }
}

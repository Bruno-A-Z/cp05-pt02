package com.java.cp04.Amazin.banco;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API do Banco Tranquilo. Rota pública no Spring Security, mas protegida por chave de API
 * (cabeçalho X-API-KEY), como numa integração real entre sistemas.
 */
@RestController
@RequestMapping("/banco-tranquilo/api")
@RequiredArgsConstructor
@Tag(name = "Banco Tranquilo", description = "API de autorização de pagamentos")
public class BancoTranquiloApiController {

    private final BancoTranquiloService service;

    @Value("${banco.api.key:banco-tranquilo-key}")
    private String apiKey;

    @PostMapping("/pagamentos")
    @Operation(summary = "Autorizar pagamento")
    public ResponseEntity<PagamentoApiResponse> autorizar(
            @RequestHeader(value = "X-API-KEY", required = false) String chave,
            @RequestBody PagamentoApiRequest request) {
        if (chave == null || !chave.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(service.autorizar(request));
    }
}

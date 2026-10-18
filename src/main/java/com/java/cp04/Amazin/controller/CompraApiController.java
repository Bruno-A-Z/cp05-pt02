package com.java.cp04.Amazin.controller;

import com.java.cp04.Amazin.dto.CompraRequest;
import com.java.cp04.Amazin.dto.CompraResponse;
import com.java.cp04.Amazin.exception.CompraException;
import com.java.cp04.Amazin.model.Pedido;
import com.java.cp04.Amazin.service.CompraService;
import com.java.cp04.Amazin.service.DadosCartao;
import com.java.cp04.Amazin.service.LinhaCompra;
import com.java.cp04.Amazin.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * API de compras para testes no Postman/Insomnia (HTTP Basic).
 * POST cria o pedido e dispara a requisição de pagamento ao Banco Tranquilo via RabbitMQ (resposta assíncrona).
 * GET /{id} consulta o status atual do pedido.
 */
@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
@Tag(name = "Compras", description = "Finalização de compra e integração com o Banco Tranquilo")
public class CompraApiController {

    private final CompraService compraService;
    private final PedidoService pedidoService;

    @PostMapping
    @Operation(summary = "Finalizar compra (envia pagamento ao Banco Tranquilo via RabbitMQ)")
    public ResponseEntity<CompraResponse> comprar(@Valid @RequestBody CompraRequest request, Authentication auth) {
        List<LinhaCompra> linhas = request.itens().stream()
                .map(i -> new LinhaCompra(i.itemId(), i.quantidade()))
                .toList();
        Pedido pedido = compraService.finalizar(auth.getName(), linhas,
                new DadosCartao(request.cartao().numero(), request.cartao().titular(), request.cartao().validade()));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(CompraResponse.de(pedido));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar status do pedido")
    public CompraResponse consultar(@PathVariable Long id, Authentication auth) {
        Pedido pedido = pedidoService.buscarPorId(id);
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!admin && !pedido.getUsuario().getUsername().equals(auth.getName())) {
            throw new AccessDeniedException("Pedido de outro usuário");
        }
        return CompraResponse.de(pedido);
    }

    @ExceptionHandler(CompraException.class)
    public ResponseEntity<Map<String, String>> compraInvalida(CompraException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacao(MethodArgumentNotValidException e) {
        Map<String, String> campos = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(f -> f.getField(), f -> String.valueOf(f.getDefaultMessage()), (a, b) -> a));
        return ResponseEntity.badRequest().body(Map.of("erro", "Requisição inválida.", "campos", campos));
    }
}

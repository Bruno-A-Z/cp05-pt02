package com.java.cp04.Amazin.service;

import com.java.cp04.Amazin.exception.CompraException;
import com.java.cp04.Amazin.messaging.PagamentoPublisher;
import com.java.cp04.Amazin.messaging.PagamentoRequisicao;
import com.java.cp04.Amazin.model.Pedido;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orquestra a finalização da compra:
 * 1) cria o pedido PENDENTE (e baixa o estoque) em uma transação;
 * 2) publica a requisição de pagamento no RabbitMQ (fora da transação, após o commit).
 * A resposta do Banco Tranquilo chega depois, de forma assíncrona (PagamentoRespostaListener).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompraService {

    private final PedidoService pedidoService;
    private final PagamentoPublisher publisher;

    public Pedido finalizar(String username, List<LinhaCompra> linhas, DadosCartao cartao) {
        Pedido pedido = pedidoService.criarPedido(username, linhas, cartao.ultimos4());

        PagamentoRequisicao requisicao = new PagamentoRequisicao(
                pedido.getTransactionId(),
                pedido.getId(),
                pedido.getUsuario().getNome(),
                pedido.getUsuario().getEmail(),
                pedido.getTotal(),
                cartao.numeroLimpo(),
                cartao.titular());
        try {
            publisher.publicarRequisicao(requisicao);
            log.info("Pedido {} enviado ao Banco Tranquilo (transação {}, cartão {})",
                    pedido.getId(), pedido.getTransactionId(), requisicao.cartaoMascarado());
        } catch (AmqpException e) {
            log.error("Falha ao publicar no RabbitMQ para o pedido {}", pedido.getId(), e);
            pedidoService.marcarErro(pedido.getId(), "Mensageria indisponível. Tente novamente em instantes.");
            throw new CompraException("Não foi possível enviar o pagamento ao banco (mensageria indisponível). "
                    + "O estoque foi devolvido; tente novamente.");
        }
        return pedido;
    }
}

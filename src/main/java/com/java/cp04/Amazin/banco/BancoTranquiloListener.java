package com.java.cp04.Amazin.banco;

import com.java.cp04.Amazin.config.RabbitConfig;
import com.java.cp04.Amazin.messaging.PagamentoPublisher;
import com.java.cp04.Amazin.messaging.PagamentoRequisicao;
import com.java.cp04.Amazin.messaging.PagamentoResposta;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 * Lado Banco Tranquilo: consome as requisições de pagamento, consulta a API do banco
 * e publica a resposta na fila de respostas.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BancoTranquiloListener {

    private final BancoTranquiloClient client;
    private final PagamentoPublisher publisher;

    @RabbitListener(queues = RabbitConfig.QUEUE_REQUISICAO)
    public void processar(PagamentoRequisicao req) {
        log.info("Banco Tranquilo recebeu requisição: pedido={} valor={} cartão={}",
                req.pedidoId(), req.valor(), req.cartaoMascarado());

        PagamentoResposta resposta;
        try {
            PagamentoApiResponse api = client.autorizar(new PagamentoApiRequest(
                    req.transactionId(), req.valor(), req.numeroCartao(), req.nomeTitular()));
            boolean aprovado = api != null && "APROVADO".equalsIgnoreCase(api.status());
            resposta = new PagamentoResposta(
                    req.transactionId(),
                    req.pedidoId(),
                    aprovado,
                    aprovado ? "APROVADO" : "RECUSADO",
                    api == null ? "Resposta vazia do banco." : api.motivo(),
                    api == null ? null : api.codigoAutorizacao());
        } catch (RestClientException e) {
            log.error("API do Banco Tranquilo indisponível para o pedido {}", req.pedidoId(), e);
            resposta = new PagamentoResposta(req.transactionId(), req.pedidoId(), false, "ERRO",
                    "Banco Tranquilo indisponível. Tente novamente.", null);
        }
        publisher.publicarResposta(resposta);
    }
}

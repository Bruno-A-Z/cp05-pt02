package com.java.cp04.Amazin.messaging;

import com.java.cp04.Amazin.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PagamentoPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publicarRequisicao(PagamentoRequisicao requisicao) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.RK_REQUISICAO, requisicao);
    }

    public void publicarResposta(PagamentoResposta resposta) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.RK_RESPOSTA, resposta);
    }
}

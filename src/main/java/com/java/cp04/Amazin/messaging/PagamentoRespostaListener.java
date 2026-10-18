package com.java.cp04.Amazin.messaging;

import com.java.cp04.Amazin.config.RabbitConfig;
import com.java.cp04.Amazin.service.EmailService;
import com.java.cp04.Amazin.service.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Lado Mercado Express: consome a resposta do banco, atualiza o pedido e avisa o cliente por e-mail. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PagamentoRespostaListener {

    private final PedidoService pedidoService;
    private final EmailService emailService;

    @RabbitListener(queues = RabbitConfig.QUEUE_RESPOSTA)
    public void receberResposta(PagamentoResposta resposta) {
        log.info("Resposta do Banco Tranquilo: pedido={} status={} motivo={}",
                resposta.pedidoId(), resposta.status(), resposta.motivo());

        pedidoService.processarResposta(resposta).ifPresent(pedido -> {
            if (resposta.aprovado()) {
                try {
                    emailService.enviarConfirmacaoPagamento(pedido);
                } catch (Exception e) {
                    // O pagamento já foi confirmado; falha de e-mail não pode reprocessar a mensagem.
                    log.error("Erro ao registrar/enviar e-mail do pedido {}", pedido.getId(), e);
                }
            }
        });
    }
}

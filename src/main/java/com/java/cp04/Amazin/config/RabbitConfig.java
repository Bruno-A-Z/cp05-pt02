package com.java.cp04.Amazin.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologia do RabbitMQ (ou LavinMQ) para as transações bancárias:
 *
 * <pre>
 * Mercado --(requisicao)--> [mercado.pagamentos.exchange] --> pagamento.requisicao.queue --> Banco Tranquilo
 * Mercado <--(resposta)---- [mercado.pagamentos.exchange] <-- pagamento.resposta.queue   <-- Banco Tranquilo
 * mensagens com falha ----> [mercado.pagamentos.dlx] -----> *.dlq (Dead Letter Queues)
 * </pre>
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "mercado.pagamentos.exchange";
    public static final String DLX = "mercado.pagamentos.dlx";

    public static final String QUEUE_REQUISICAO = "pagamento.requisicao.queue";
    public static final String QUEUE_RESPOSTA = "pagamento.resposta.queue";
    public static final String QUEUE_REQUISICAO_DLQ = "pagamento.requisicao.dlq";
    public static final String QUEUE_RESPOSTA_DLQ = "pagamento.resposta.dlq";

    public static final String RK_REQUISICAO = "pagamento.requisicao";
    public static final String RK_RESPOSTA = "pagamento.resposta";
    public static final String RK_REQUISICAO_DLQ = "pagamento.requisicao.dlq";
    public static final String RK_RESPOSTA_DLQ = "pagamento.resposta.dlq";

    @Bean
    public DirectExchange pagamentosExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange pagamentosDlx() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    public Queue requisicaoQueue() {
        return QueueBuilder.durable(QUEUE_REQUISICAO)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", RK_REQUISICAO_DLQ)
                .build();
    }

    @Bean
    public Queue respostaQueue() {
        return QueueBuilder.durable(QUEUE_RESPOSTA)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", RK_RESPOSTA_DLQ)
                .build();
    }

    @Bean
    public Queue requisicaoDlq() {
        return QueueBuilder.durable(QUEUE_REQUISICAO_DLQ).build();
    }

    @Bean
    public Queue respostaDlq() {
        return QueueBuilder.durable(QUEUE_RESPOSTA_DLQ).build();
    }

    @Bean
    public Binding requisicaoBinding() {
        return BindingBuilder.bind(requisicaoQueue()).to(pagamentosExchange()).with(RK_REQUISICAO);
    }

    @Bean
    public Binding respostaBinding() {
        return BindingBuilder.bind(respostaQueue()).to(pagamentosExchange()).with(RK_RESPOSTA);
    }

    @Bean
    public Binding requisicaoDlqBinding() {
        return BindingBuilder.bind(requisicaoDlq()).to(pagamentosDlx()).with(RK_REQUISICAO_DLQ);
    }

    @Bean
    public Binding respostaDlqBinding() {
        return BindingBuilder.bind(respostaDlq()).to(pagamentosDlx()).with(RK_RESPOSTA_DLQ);
    }

    /** Mensagens em JSON (legíveis no Management UI). O Spring Boot aplica este conversor ao RabbitTemplate e aos listeners. */
    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("com.java.cp04.Amazin.messaging");
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}

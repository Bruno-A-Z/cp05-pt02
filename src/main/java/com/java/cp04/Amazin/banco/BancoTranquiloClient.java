package com.java.cp04.Amazin.banco;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/** Cliente HTTP que o consumidor da fila usa para chamar a API do Banco Tranquilo (configurável em banco.api.url). */
@Component
public class BancoTranquiloClient {

    private final RestClient restClient;

    public BancoTranquiloClient(@Value("${banco.api.url}") String baseUrl,
                                @Value("${banco.api.key:banco-tranquilo-key}") String apiKey) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .defaultHeader("X-API-KEY", apiKey)
                .build();
    }

    public PagamentoApiResponse autorizar(PagamentoApiRequest request) {
        return restClient.post()
                .uri("/pagamentos")
                .body(request)
                .retrieve()
                .body(PagamentoApiResponse.class);
    }
}

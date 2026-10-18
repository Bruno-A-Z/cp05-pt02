package com.java.cp04.Amazin;

import com.java.cp04.Amazin.banco.BancoTranquiloService;
import com.java.cp04.Amazin.banco.PagamentoApiRequest;
import com.java.cp04.Amazin.banco.PagamentoApiResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BancoTranquiloServiceTest {

    private final BancoTranquiloService service = new BancoTranquiloService(new BigDecimal("5000"));

    private PagamentoApiResponse pagar(String cartao, String valor) {
        return service.autorizar(new PagamentoApiRequest("tx-1", new BigDecimal(valor), cartao, "Fulano"));
    }

    @Test
    void aprovaCartaoValido() {
        PagamentoApiResponse r = pagar("4111 1111 1111 2222", "100.00");
        assertEquals("APROVADO", r.status());
        assertNotNull(r.codigoAutorizacao());
    }

    @Test
    void recusaSaldoInsuficiente() {
        assertEquals("Saldo insuficiente.", pagar("4111111111110000", "10").motivo());
    }

    @Test
    void recusaCartaoBloqueado() {
        assertEquals("RECUSADO", pagar("4111111111111111", "10").status());
    }

    @Test
    void recusaValorAcimaDoLimite() {
        assertEquals("RECUSADO", pagar("4111111111112222", "5000.01").status());
    }

    @Test
    void recusaCartaoInvalido() {
        assertEquals("RECUSADO", pagar("123", "10").status());
    }
}

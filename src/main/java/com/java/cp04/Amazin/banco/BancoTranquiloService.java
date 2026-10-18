package com.java.cp04.Amazin.banco;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Regras de autorização do Banco Tranquilo (simulação para fins didáticos).
 * <ul>
 *   <li>cartão com número inválido (menos de 13 ou mais de 19 dígitos) → RECUSADO</li>
 *   <li>cartão terminado em 0000 → RECUSADO (saldo insuficiente)</li>
 *   <li>cartão terminado em 1111 → RECUSADO (cartão bloqueado)</li>
 *   <li>valor acima do limite por transação → RECUSADO</li>
 *   <li>qualquer outro caso → APROVADO</li>
 * </ul>
 */
@Service
public class BancoTranquiloService {

    private final BigDecimal limite;

    public BancoTranquiloService(@Value("${banco.limite-transacao:5000}") BigDecimal limite) {
        this.limite = limite;
    }

    public PagamentoApiResponse autorizar(PagamentoApiRequest req) {
        String numero = req.numeroCartao() == null ? "" : req.numeroCartao().replaceAll("\\D", "");

        if (numero.length() < 13 || numero.length() > 19) {
            return recusar(req, "Número de cartão inválido.");
        }
        if (req.valor() == null || req.valor().signum() <= 0) {
            return recusar(req, "Valor da transação inválido.");
        }
        if (numero.endsWith("0000")) {
            return recusar(req, "Saldo insuficiente.");
        }
        if (numero.endsWith("1111")) {
            return recusar(req, "Cartão bloqueado.");
        }
        if (req.valor().compareTo(limite) > 0) {
            return recusar(req, "Valor acima do limite por transação (R$ " + limite + ").");
        }
        String autorizacao = "AUT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PagamentoApiResponse(req.transactionId(), "APROVADO", "Pagamento autorizado.", autorizacao);
    }

    private PagamentoApiResponse recusar(PagamentoApiRequest req, String motivo) {
        return new PagamentoApiResponse(req.transactionId(), "RECUSADO", motivo, null);
    }
}

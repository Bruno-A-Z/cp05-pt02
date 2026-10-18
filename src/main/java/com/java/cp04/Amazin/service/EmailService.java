package com.java.cp04.Amazin.service;

import com.java.cp04.Amazin.enumerator.StatusEmail;
import com.java.cp04.Amazin.model.EmailLog;
import com.java.cp04.Amazin.model.ItemPedido;
import com.java.cp04.Amazin.model.Pedido;
import com.java.cp04.Amazin.repository.EmailLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final ObjectProvider<JavaMailSender> mailSender;
    private final EmailLogRepository emailLogRepository;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:no-reply@mercadoexpress.com}")
    private String from;

    /** Envia (ou simula) o e-mail de confirmação de pagamento e SEMPRE grava o resultado em TDS_Email_Log_Mercado. */
    public EmailLog enviarConfirmacaoPagamento(Pedido pedido) {
        String destinatario = pedido.getUsuario().getEmail();
        String assunto = "Mercado Express - Pagamento confirmado (pedido #" + pedido.getId() + ")";
        String corpo = montarCorpo(pedido);

        EmailLog registro = new EmailLog();
        registro.setPedidoId(pedido.getId());
        registro.setDestinatario(destinatario);
        registro.setAssunto(assunto);
        registro.setCorpo(corpo);
        registro.setDataEnvio(LocalDateTime.now());

        JavaMailSender sender = mailSender.getIfAvailable();
        if (!mailEnabled || sender == null) {
            registro.setStatus(StatusEmail.SIMULADO);
            log.info("[E-MAIL SIMULADO] para {} - {}", destinatario, assunto);
        } else {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom(from);
                msg.setTo(destinatario);
                msg.setSubject(assunto);
                msg.setText(corpo);
                sender.send(msg);
                registro.setStatus(StatusEmail.ENVIADO);
                log.info("E-mail de confirmação enviado para {}", destinatario);
            } catch (Exception e) {
                registro.setStatus(StatusEmail.FALHA);
                String erro = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                registro.setErro(erro.length() > 480 ? erro.substring(0, 480) : erro);
                log.error("Falha ao enviar e-mail para {}", destinatario, e);
            }
        }
        return emailLogRepository.save(registro);
    }

    private String montarCorpo(Pedido pedido) {
        Locale br = Locale.of("pt", "BR");
        StringBuilder sb = new StringBuilder();
        sb.append("Olá, ").append(pedido.getUsuario().getNome()).append("!\n\n");
        sb.append("O Banco Tranquilo confirmou o pagamento da sua compra no Mercado Express.\n\n");
        sb.append("Pedido: #").append(pedido.getId()).append('\n');
        sb.append("Data: ").append(pedido.getDataCriacao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append('\n');
        if (pedido.getCartaoFinal() != null) {
            sb.append("Cartão: final ").append(pedido.getCartaoFinal()).append('\n');
        }
        if (pedido.getCodigoAutorizacao() != null) {
            sb.append("Autorização: ").append(pedido.getCodigoAutorizacao()).append('\n');
        }
        sb.append("\nItens:\n");
        for (ItemPedido ip : pedido.getItens()) {
            sb.append(String.format(br, " - %dx %s  (R$ %.2f)%n", ip.getQuantidade(), ip.getNome(), ip.getSubtotal()));
        }
        sb.append(String.format(br, "%nTotal pago: R$ %.2f%n", pedido.getTotal()));
        sb.append("\nObrigado por comprar no Mercado Express!");
        return sb.toString();
    }
}

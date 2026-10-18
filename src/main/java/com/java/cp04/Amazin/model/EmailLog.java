package com.java.cp04.Amazin.model;

import com.java.cp04.Amazin.enumerator.StatusEmail;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Controle de todos os e-mails disparados para os clientes. */
@Entity
@Table(name = "TDS_Email_Log_Mercado")
@Getter
@Setter
@NoArgsConstructor
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "PEDIDO_ID")
    private Long pedidoId;

    @Column(name = "DESTINATARIO", nullable = false, length = 120)
    private String destinatario;

    @Column(name = "ASSUNTO", nullable = false, length = 200)
    private String assunto;

    @Column(name = "CORPO", length = 4000)
    private String corpo;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private StatusEmail status;

    @Column(name = "ERRO", length = 500)
    private String erro;

    @Column(name = "DATA_ENVIO", nullable = false)
    private LocalDateTime dataEnvio;
}

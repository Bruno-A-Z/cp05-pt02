package com.java.cp04.Amazin.model;

import com.java.cp04.Amazin.enumerator.StatusPedido;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TDS_Pedidos_Mercado")
@Getter
@Setter
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador de correlação enviado ao RabbitMQ e ao Banco Tranquilo. */
    @Column(name = "TRANSACTION_ID", nullable = false, unique = true, length = 40)
    private String transactionId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "USUARIO_ID", nullable = false)
    private Usuario usuario;

    @Column(name = "DATA_CRIACAO", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "DATA_ATUALIZACAO")
    private LocalDateTime dataAtualizacao;

    @Column(name = "TOTAL", nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "CUSTO_TOTAL", nullable = false, precision = 12, scale = 2)
    private BigDecimal custoTotal = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private StatusPedido status = StatusPedido.PENDENTE;

    @Column(name = "MOTIVO", length = 255)
    private String motivo;

    @Column(name = "CODIGO_AUTORIZACAO", length = 40)
    private String codigoAutorizacao;

    /** Somente os 4 últimos dígitos: o número completo do cartão nunca é gravado. */
    @Column(name = "CARTAO_FINAL", length = 4)
    private String cartaoFinal;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ItemPedido> itens = new ArrayList<>();

    public BigDecimal getLucro() {
        return total.subtract(custoTotal);
    }

    public void adicionarItem(ItemPedido item) {
        item.setPedido(this);
        itens.add(item);
    }
}

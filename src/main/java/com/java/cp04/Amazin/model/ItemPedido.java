package com.java.cp04.Amazin.model;

import com.java.cp04.Amazin.enumerator.Sector;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "TDS_Itens_Pedido_Mercado")
@Getter
@Setter
@NoArgsConstructor
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PEDIDO_ID", nullable = false)
    private Pedido pedido;

    /** Referência simples (sem FK) para o produto, que pode ser excluído depois sem quebrar o histórico. */
    @Column(name = "ITEM_ID")
    private Long itemId;

    @Column(name = "NOME_ITEM", nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "SETOR", length = 20)
    private Sector setor;

    @Column(name = "QUANTIDADE", nullable = false)
    private int quantidade;

    @Column(name = "PRECO_UNITARIO", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoUnitario;

    @Column(name = "CUSTO_UNITARIO", nullable = false, precision = 12, scale = 2)
    private BigDecimal custoUnitario;

    public BigDecimal getSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}

package com.java.cp04.Amazin.model;

import com.java.cp04.Amazin.enumerator.Sector;
import com.java.cp04.Amazin.enumerator.Size;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "TDS_Sec_MVC_TB_Mercado")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME_ITEM", nullable = false)
    private String name;

    @Column(name = "KIND_ITEM")
    private String kind;

    @Enumerated(EnumType.STRING)
    @Column(name = "ITEM_SECTOR")
    private Sector sector;

    @Enumerated(EnumType.STRING)
    @Column(name = "ITEM_SIZE")
    private Size size;

    @Column(name = "ITEM_PRICE")
    private Double price;

    /** Quantidade em estoque (baixa automática a cada compra; devolvida se o pagamento for recusado). */
    @Column(name = "ITEM_STOCK")
    private Integer stock = 0;

    /** Custo unitário do produto, usado para calcular o lucro no dashboard. */
    @Column(name = "ITEM_COST")
    private Double cost = 0.0;

}
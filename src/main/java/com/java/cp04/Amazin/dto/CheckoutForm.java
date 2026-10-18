package com.java.cp04.Amazin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CheckoutForm {

    @NotBlank(message = "Informe o número do cartão.")
    @Pattern(regexp = "^[0-9 ]{13,23}$", message = "Número de cartão inválido (13 a 19 dígitos).")
    private String numeroCartao;

    @NotBlank(message = "Informe o nome do titular.")
    private String nomeTitular;

    @NotBlank(message = "Informe a validade.")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$", message = "Validade no formato MM/AA.")
    private String validade;

    @NotBlank(message = "Informe o CVV.")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV inválido.")
    private String cvv;
}

package com.java.cp04.Amazin.web;

import com.java.cp04.Amazin.dto.CheckoutForm;
import com.java.cp04.Amazin.exception.CompraException;
import com.java.cp04.Amazin.model.Item;
import com.java.cp04.Amazin.model.Pedido;
import com.java.cp04.Amazin.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/carrinho")
@RequiredArgsConstructor
public class CarrinhoViewController {

    private final CarrinhoSessao carrinho;
    private final CarrinhoService carrinhoService;
    private final ItemService itemService;
    private final CompraService compraService;

    @GetMapping
    public String ver(Model model) {
        popular(model);
        if (!model.containsAttribute("checkout")) {
            model.addAttribute("checkout", new CheckoutForm());
        }
        return "carrinho";
    }

    @PostMapping("/adicionar/{id}")
    public String adicionar(@PathVariable Long id,
                            @RequestParam(defaultValue = "1") int quantidade,
                            RedirectAttributes redirect) {
        try {
            Item item = itemService.buscarPorId(id);
            int estoque = item.getStock() == null ? 0 : item.getStock();
            int jaNoCarrinho = carrinho.getItens().getOrDefault(id, 0);
            if (quantidade < 1) {
                redirect.addFlashAttribute("erro", "Quantidade inválida.");
            } else if (jaNoCarrinho + quantidade > estoque) {
                redirect.addFlashAttribute("erro", "Estoque insuficiente para \"" + item.getName()
                        + "\" (disponível: " + estoque + ").");
            } else {
                carrinho.adicionar(id, quantidade);
                redirect.addFlashAttribute("mensagem", quantidade + "x \"" + item.getName() + "\" adicionado ao carrinho.");
            }
        } catch (NoSuchElementException e) {
            redirect.addFlashAttribute("erro", "Produto não encontrado.");
        }
        return "redirect:/web/itens";
    }

    @PostMapping("/remover/{id}")
    public String remover(@PathVariable Long id) {
        carrinho.remover(id);
        return "redirect:/carrinho";
    }

    @PostMapping("/limpar")
    public String limpar() {
        carrinho.limpar();
        return "redirect:/carrinho";
    }

    @PostMapping("/finalizar")
    public String finalizar(@Valid @ModelAttribute("checkout") CheckoutForm form,
                            BindingResult result,
                            Authentication auth,
                            Model model) {
        if (carrinho.vazio()) {
            model.addAttribute("erro", "O carrinho está vazio.");
            popular(model);
            return "carrinho";
        }
        if (result.hasErrors()) {
            popular(model);
            return "carrinho";
        }
        try {
            Pedido pedido = compraService.finalizar(
                    auth.getName(),
                    carrinhoService.paraCompra(carrinho),
                    new DadosCartao(form.getNumeroCartao(), form.getNomeTitular(), form.getValidade()));
            carrinho.limpar();
            return "redirect:/pedidos/" + pedido.getId() + "?novo";
        } catch (CompraException e) {
            model.addAttribute("erro", e.getMessage());
            popular(model);
            return "carrinho";
        }
    }

    private void popular(Model model) {
        CarrinhoService.Resumo resumo = carrinhoService.resumir(carrinho);
        model.addAttribute("linhas", resumo.linhas());
        model.addAttribute("total", resumo.total());
    }
}

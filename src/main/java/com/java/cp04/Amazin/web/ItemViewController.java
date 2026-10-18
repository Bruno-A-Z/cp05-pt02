package com.java.cp04.Amazin.web;

import com.java.cp04.Amazin.enumerator.Sector;
import com.java.cp04.Amazin.enumerator.Size;
import com.java.cp04.Amazin.model.Item;
import com.java.cp04.Amazin.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/itens")
@RequiredArgsConstructor
public class ItemViewController {

    private final ItemService itemService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("itens", itemService.listarTodos());
        return "itens/lista";
    }

    @GetMapping("/{id}")
    public String detalhes(@PathVariable Long id, Model model) {
        model.addAttribute("item", itemService.buscarPorId(id));
        return "itens/detalhes";
    }

    @GetMapping("/novo")
    public String novoForm(Model model) {
        model.addAttribute("item", new Item());
        model.addAttribute("sectors", Sector.values());
        model.addAttribute("sizes", Size.values());
        model.addAttribute("modoEdicao", false);
        return "itens/form";
    }

    @PostMapping
    public String criar(@ModelAttribute Item item, RedirectAttributes redirectAttributes) {
        itemService.criar(item);
        redirectAttributes.addFlashAttribute("mensagem", "Item criado com sucesso!");
        return "redirect:/web/itens";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        model.addAttribute("item", itemService.buscarPorId(id));
        model.addAttribute("sectors", Sector.values());
        model.addAttribute("sizes", Size.values());
        model.addAttribute("modoEdicao", true);
        return "itens/form";
    }

    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable Long id, @ModelAttribute Item item,
                            RedirectAttributes redirectAttributes) {
        itemService.atualizar(id, item);
        redirectAttributes.addFlashAttribute("mensagem", "Item atualizado com sucesso!");
        return "redirect:/web/itens";
    }

    @PostMapping("/{id}/deletar")
    public String deletar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        itemService.deletar(id);
        redirectAttributes.addFlashAttribute("mensagem", "Item excluído com sucesso!");
        return "redirect:/web/itens";
    }
}
package com.java.cp04.Amazin.web;

import com.java.cp04.Amazin.dto.CadastroForm;
import com.java.cp04.Amazin.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    @GetMapping("/req/cadastro")
    public String cadastroForm(Model model) {
        model.addAttribute("form", new CadastroForm());
        return "cadastro";
    }

    @PostMapping("/req/cadastro")
    public String cadastrar(@Valid @ModelAttribute("form") CadastroForm form, BindingResult result) {
        if (form.getSenha() != null && !form.getSenha().equals(form.getConfirmarSenha())) {
            result.rejectValue("confirmarSenha", "senha.diferente", "As senhas não conferem.");
        }
        if (form.getUsername() != null && !result.hasFieldErrors("username")
                && usuarioService.usernameExiste(form.getUsername())) {
            result.rejectValue("username", "username.existe", "Este usuário já está cadastrado.");
        }
        if (form.getEmail() != null && !result.hasFieldErrors("email")
                && usuarioService.emailExiste(form.getEmail())) {
            result.rejectValue("email", "email.existe", "Este e-mail já está cadastrado.");
        }
        if (result.hasErrors()) {
            return "cadastro";
        }

        usuarioService.cadastrar(form);
        return "redirect:/req/login?cadastrado";
    }
}

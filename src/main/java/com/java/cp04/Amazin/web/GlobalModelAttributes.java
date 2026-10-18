package com.java.cp04.Amazin.web;

import com.java.cp04.Amazin.service.CarrinhoSessao;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.java.cp04.Amazin.web")
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final CarrinhoSessao carrinho;

    @ModelAttribute("carrinhoQtd")
    public int carrinhoQtd() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean logado = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
        return logado ? carrinho.quantidadeTotal() : 0;
    }

    @ModelAttribute("autenticado")
    public boolean autenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());
    }

    @ModelAttribute("admin")
    public boolean admin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @ModelAttribute("usuarioLogado")
    public String usuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return null;
    }
}

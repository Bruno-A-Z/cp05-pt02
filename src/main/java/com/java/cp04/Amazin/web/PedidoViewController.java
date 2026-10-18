package com.java.cp04.Amazin.web;

import com.java.cp04.Amazin.model.Pedido;
import com.java.cp04.Amazin.repository.EmailLogRepository;
import com.java.cp04.Amazin.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class PedidoViewController {

    private final PedidoService pedidoService;
    private final EmailLogRepository emailLogRepository;

    /** Pedidos do próprio usuário. */
    @GetMapping("/pedidos")
    public String meusPedidos(Authentication auth, Model model) {
        model.addAttribute("pedidos", pedidoService.listarDoUsuario(auth.getName()));
        model.addAttribute("titulo", "Meus pedidos");
        model.addAttribute("visaoAdmin", false);
        return "pedidos/lista";
    }

    @GetMapping("/pedidos/{id}")
    public String detalhes(@PathVariable Long id, Authentication auth, Model model) {
        Pedido pedido = pedidoService.buscarPorId(id);
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!admin && !pedido.getUsuario().getUsername().equals(auth.getName())) {
            throw new AccessDeniedException("Pedido de outro usuário");
        }
        model.addAttribute("pedido", pedido);
        model.addAttribute("emails", emailLogRepository.findByPedidoId(id));
        return "pedidos/detalhes";
    }

    /** Todos os pedidos (ADMIN). */
    @GetMapping("/admin/pedidos")
    public String todosPedidos(Model model) {
        model.addAttribute("pedidos", pedidoService.listarTodos());
        model.addAttribute("titulo", "Todos os pedidos");
        model.addAttribute("visaoAdmin", true);
        return "pedidos/lista";
    }

    /** Log de e-mails enviados aos clientes (ADMIN). */
    @GetMapping("/admin/emails")
    public String emails(Model model) {
        model.addAttribute("emails", emailLogRepository.findAllByOrderByDataEnvioDesc());
        return "admin-emails";
    }

    @GetMapping("/admin/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}

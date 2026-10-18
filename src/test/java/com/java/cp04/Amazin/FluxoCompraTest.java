package com.java.cp04.Amazin;

import com.java.cp04.Amazin.enumerator.Role;
import com.java.cp04.Amazin.enumerator.StatusEmail;
import com.java.cp04.Amazin.enumerator.StatusPedido;
import com.java.cp04.Amazin.exception.CompraException;
import com.java.cp04.Amazin.messaging.PagamentoRequisicao;
import com.java.cp04.Amazin.messaging.PagamentoResposta;
import com.java.cp04.Amazin.model.EmailLog;
import com.java.cp04.Amazin.model.Item;
import com.java.cp04.Amazin.model.Pedido;
import com.java.cp04.Amazin.model.Usuario;
import com.java.cp04.Amazin.repository.EmailLogRepository;
import com.java.cp04.Amazin.repository.ItemRepository;
import com.java.cp04.Amazin.repository.UsuarioRepository;
import com.java.cp04.Amazin.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/** Testa o fluxo de compra sem precisar de RabbitMQ: o RabbitTemplate é simulado. */
@SpringBootTest
@ActiveProfiles("test")
class FluxoCompraTest {

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Autowired private CompraService compraService;
    @Autowired private PedidoService pedidoService;
    @Autowired private EmailService emailService;
    @Autowired private ItemRepository itemRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EmailLogRepository emailLogRepository;

    private Long itemId;

    @BeforeEach
    void preparar() {
        if (!usuarioRepository.existsByUsername("cliente-teste")) {
            Usuario u = new Usuario();
            u.setNome("Cliente Teste");
            u.setUsername("cliente-teste");
            u.setEmail("cliente.teste@example.com");
            u.setSenha("x");
            u.setRole(Role.USER);
            usuarioRepository.save(u);
        }
        Item item = new Item();
        item.setName("Produto de teste");
        item.setKind("Teste");
        item.setPrice(10.0);
        item.setCost(4.0);
        item.setStock(5);
        itemId = itemRepository.save(item).getId();
    }

    private int estoque() {
        return itemRepository.findById(itemId).orElseThrow().getStock();
    }

    private Pedido comprar(int qtd) {
        return compraService.finalizar("cliente-teste", List.of(new LinhaCompra(itemId, qtd)),
                new DadosCartao("4111 1111 1111 2222", "Cliente Teste", "12/30"));
    }

    @Test
    void compraCriaPedidoPendenteBaixaEstoqueEPublicaNaFila() {
        Pedido p = comprar(2);

        assertEquals(StatusPedido.PENDENTE, p.getStatus());
        assertEquals(0, p.getTotal().compareTo(new java.math.BigDecimal("20.00")));
        assertEquals("2222", p.getCartaoFinal());
        assertEquals(3, estoque());
        verify(rabbitTemplate).convertAndSend(eq("mercado.pagamentos.exchange"), eq("pagamento.requisicao"),
                any(PagamentoRequisicao.class));
    }

    @Test
    void respostaAprovadaConfirmaPedidoEEmailFicaRegistrado() {
        Pedido p = comprar(1);

        Pedido atualizado = pedidoService.processarResposta(
                new PagamentoResposta(p.getTransactionId(), p.getId(), true, "APROVADO", "ok", "AUT-123")).orElseThrow();
        assertEquals(StatusPedido.APROVADO, atualizado.getStatus());
        assertEquals("AUT-123", atualizado.getCodigoAutorizacao());

        EmailLog log = emailService.enviarConfirmacaoPagamento(atualizado);
        assertEquals(StatusEmail.SIMULADO, log.getStatus());
        assertEquals("cliente.teste@example.com", log.getDestinatario());
        assertFalse(emailLogRepository.findByPedidoId(p.getId()).isEmpty());
    }

    @Test
    void respostaRecusadaDevolveEstoque() {
        Pedido p = comprar(3);
        assertEquals(2, estoque());

        Pedido atualizado = pedidoService.processarResposta(
                new PagamentoResposta(p.getTransactionId(), p.getId(), false, "RECUSADO", "Saldo insuficiente.", null)).orElseThrow();

        assertEquals(StatusPedido.RECUSADO, atualizado.getStatus());
        assertEquals(5, estoque());
    }

    @Test
    void respostaDuplicadaEIgnorada() {
        Pedido p = comprar(1);
        PagamentoResposta r = new PagamentoResposta(p.getTransactionId(), p.getId(), true, "APROVADO", "ok", "AUT-1");
        assertTrue(pedidoService.processarResposta(r).isPresent());
        assertTrue(pedidoService.processarResposta(r).isEmpty());
    }

    @Test
    void estoqueInsuficienteImpedeCompra() {
        assertThrows(CompraException.class, () -> comprar(99));
        assertEquals(5, estoque());
    }
}

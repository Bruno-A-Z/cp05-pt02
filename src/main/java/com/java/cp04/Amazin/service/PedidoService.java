package com.java.cp04.Amazin.service;

import com.java.cp04.Amazin.enumerator.StatusPedido;
import com.java.cp04.Amazin.exception.CompraException;
import com.java.cp04.Amazin.messaging.PagamentoResposta;
import com.java.cp04.Amazin.model.Item;
import com.java.cp04.Amazin.model.ItemPedido;
import com.java.cp04.Amazin.model.Pedido;
import com.java.cp04.Amazin.model.Usuario;
import com.java.cp04.Amazin.repository.ItemRepository;
import com.java.cp04.Amazin.repository.PedidoRepository;
import com.java.cp04.Amazin.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ItemRepository itemRepository;
    private final UsuarioRepository usuarioRepository;

    /** Cria o pedido PENDENTE e reserva (baixa) o estoque. Tudo na mesma transação. */
    @Transactional
    public Pedido criarPedido(String username, List<LinhaCompra> linhas, String cartaoFinal) {
        if (linhas == null || linhas.isEmpty()) {
            throw new CompraException("O carrinho está vazio.");
        }
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new CompraException("Usuário não encontrado: " + username));

        // Junta linhas repetidas do mesmo produto
        Map<Long, Integer> agrupadas = new LinkedHashMap<>();
        for (LinhaCompra l : linhas) {
            if (l.itemId() == null || l.quantidade() <= 0) {
                throw new CompraException("Quantidade inválida para o produto " + l.itemId() + ".");
            }
            agrupadas.merge(l.itemId(), l.quantidade(), Integer::sum);
        }

        Pedido pedido = new Pedido();
        pedido.setTransactionId(UUID.randomUUID().toString());
        pedido.setUsuario(usuario);
        pedido.setDataCriacao(LocalDateTime.now());
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setCartaoFinal(cartaoFinal);

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal custo = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> e : agrupadas.entrySet()) {
            Item item = itemRepository.findById(e.getKey())
                    .orElseThrow(() -> new CompraException("Produto não encontrado: " + e.getKey()));
            int estoque = item.getStock() == null ? 0 : item.getStock();
            if (estoque < e.getValue()) {
                throw new CompraException("Estoque insuficiente para \"" + item.getName()
                        + "\" (disponível: " + estoque + ").");
            }
            item.setStock(estoque - e.getValue());

            ItemPedido ip = new ItemPedido();
            ip.setItemId(item.getId());
            ip.setNome(item.getName());
            ip.setSetor(item.getSector());
            ip.setQuantidade(e.getValue());
            ip.setPrecoUnitario(dinheiro(item.getPrice()));
            ip.setCustoUnitario(dinheiro(item.getCost()));
            pedido.adicionarItem(ip);

            total = total.add(ip.getSubtotal());
            custo = custo.add(ip.getCustoUnitario().multiply(BigDecimal.valueOf(ip.getQuantidade())));
        }
        pedido.setTotal(total);
        pedido.setCustoTotal(custo);
        return pedidoRepository.save(pedido);
    }

    /**
     * Aplica a resposta do Banco Tranquilo. Retorna o pedido atualizado, ou vazio se o pedido não existe
     * ou já foi processado (idempotência para mensagens duplicadas).
     */
    @Transactional
    public Optional<Pedido> processarResposta(PagamentoResposta resposta) {
        Optional<Pedido> opt = pedidoRepository.findByTransactionId(resposta.transactionId());
        if (opt.isEmpty()) {
            log.warn("Resposta para transação desconhecida: {}", resposta.transactionId());
            return Optional.empty();
        }
        Pedido pedido = opt.get();
        if (pedido.getStatus() != StatusPedido.PENDENTE) {
            log.info("Pedido {} já processado ({}). Resposta ignorada.", pedido.getId(), pedido.getStatus());
            return Optional.empty();
        }

        if (resposta.aprovado()) {
            pedido.setStatus(StatusPedido.APROVADO);
            pedido.setCodigoAutorizacao(resposta.codigoAutorizacao());
            pedido.setMotivo(resposta.motivo());
        } else {
            pedido.setStatus("ERRO".equalsIgnoreCase(resposta.status()) ? StatusPedido.ERRO : StatusPedido.RECUSADO);
            pedido.setMotivo(truncar(resposta.motivo()));
            devolverEstoque(pedido);
        }
        pedido.setDataAtualizacao(LocalDateTime.now());
        return Optional.of(pedidoRepository.save(pedido));
    }

    /** Falha técnica antes de chegar ao banco (ex.: broker fora do ar): marca ERRO e devolve o estoque. */
    @Transactional
    public void marcarErro(Long pedidoId, String motivo) {
        pedidoRepository.findById(pedidoId).ifPresent(pedido -> {
            if (pedido.getStatus() == StatusPedido.PENDENTE) {
                pedido.setStatus(StatusPedido.ERRO);
                pedido.setMotivo(truncar(motivo));
                pedido.setDataAtualizacao(LocalDateTime.now());
                devolverEstoque(pedido);
                pedidoRepository.save(pedido);
            }
        });
    }

    @Transactional(readOnly = true)
    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pedido não encontrado com id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarDoUsuario(String username) {
        return pedidoRepository.findByUsuarioUsernameOrderByDataCriacaoDesc(username);
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAllByOrderByDataCriacaoDesc();
    }

    private void devolverEstoque(Pedido pedido) {
        for (ItemPedido ip : pedido.getItens()) {
            if (ip.getItemId() == null) continue;
            itemRepository.findById(ip.getItemId()).ifPresent(item -> {
                int atual = item.getStock() == null ? 0 : item.getStock();
                item.setStock(atual + ip.getQuantidade());
            });
        }
    }

    private static BigDecimal dinheiro(Double valor) {
        return BigDecimal.valueOf(valor == null ? 0.0 : valor).setScale(2, RoundingMode.HALF_UP);
    }

    private static String truncar(String texto) {
        if (texto == null) return null;
        return texto.length() > 250 ? texto.substring(0, 250) : texto;
    }
}

package com.java.cp04.Amazin.repository;

import com.java.cp04.Amazin.enumerator.StatusPedido;
import com.java.cp04.Amazin.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Optional<Pedido> findByTransactionId(String transactionId);

    List<Pedido> findByUsuarioUsernameOrderByDataCriacaoDesc(String username);

    List<Pedido> findAllByOrderByDataCriacaoDesc();

    List<Pedido> findByStatus(StatusPedido status);
}

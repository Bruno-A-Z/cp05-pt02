package com.java.cp04.Amazin.repository;

import com.java.cp04.Amazin.model.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

    List<EmailLog> findAllByOrderByDataEnvioDesc();

    List<EmailLog> findByPedidoId(Long pedidoId);
}

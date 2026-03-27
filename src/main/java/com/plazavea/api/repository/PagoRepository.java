package com.plazavea.api.repository;

import com.plazavea.api.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    List<Pago> findAllByOrderByIdPagoDesc();

    List<Pago> findByPedido_IdPedidoOrderByIdPagoDesc(Integer idPedido);
}
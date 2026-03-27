package com.plazavea.api.repository;

import com.plazavea.api.model.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Integer> {

    List<DetallePedido> findByPedido_IdPedido(Integer idPedido);
}
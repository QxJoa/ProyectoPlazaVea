package com.plazavea.api.repository;

import com.plazavea.api.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    List<Pedido> findAllByOrderByIdPedidoDesc();

    List<Pedido> findByUsuario_IdUsuarioOrderByIdPedidoDesc(Integer idUsuario);
}
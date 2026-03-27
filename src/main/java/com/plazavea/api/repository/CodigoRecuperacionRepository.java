package com.plazavea.api.repository;

import com.plazavea.api.model.CodigoRecuperacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CodigoRecuperacionRepository extends JpaRepository<CodigoRecuperacion, Integer> {

    Optional<CodigoRecuperacion> findTopByUsuario_IdUsuarioAndEstadoOrderByIdCodigoDesc(Integer idUsuario, String estado);

    List<CodigoRecuperacion> findByUsuario_IdUsuarioAndEstado(Integer idUsuario, String estado);
}
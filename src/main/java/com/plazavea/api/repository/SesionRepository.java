package com.plazavea.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.plazavea.api.model.Sesion;

@Repository
public interface SesionRepository extends JpaRepository<Sesion, Integer> {

    // 🔹 Método antiguo (ya no lo usarás, pero lo dejamos por compatibilidad)
    Optional<Sesion> findByUsuarioIdUsuarioAndFechaFinIsNull(Integer idUsuario);

    // 🔥 NUEVO: trae TODAS las sesiones activas (soluciona tu error)
    List<Sesion> findAllByUsuarioIdUsuarioAndFechaFinIsNull(Integer idUsuario);

    // 🔥 OPCIONAL PRO: obtener la última sesión activa
    Optional<Sesion> findTopByUsuarioIdUsuarioAndFechaFinIsNullOrderByFechaInicioDesc(Integer idUsuario);
}
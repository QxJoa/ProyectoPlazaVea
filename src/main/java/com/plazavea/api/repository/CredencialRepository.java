package com.plazavea.api.repository;

import com.plazavea.api.model.Credencial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredencialRepository extends JpaRepository<Credencial, Integer> {

    Optional<Credencial> findByUsername(String username);

    Optional<Credencial> findByUsernameAndEstado(String username, String estado);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCaseAndIdCredencialNot(String username, Integer idCredencial);

    Optional<Credencial> findByUsuario_IdUsuario(Integer idUsuario);
}
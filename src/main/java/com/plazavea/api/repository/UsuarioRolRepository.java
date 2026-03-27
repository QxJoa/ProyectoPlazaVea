package com.plazavea.api.repository;

import com.plazavea.api.model.UsuarioRol;
import com.plazavea.api.model.UsuarioRolId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, UsuarioRolId> {

    List<UsuarioRol> findByUsuario_IdUsuario(Integer idUsuario);

    boolean existsByUsuario_IdUsuarioAndRol_IdRol(Integer idUsuario, Integer idRol);

    boolean existsByUsuario_IdUsuarioAndRol_NombreRolIgnoreCase(Integer idUsuario, String nombreRol);

    Optional<UsuarioRol> findByUsuario_IdUsuarioAndRol_IdRol(Integer idUsuario, Integer idRol);
}
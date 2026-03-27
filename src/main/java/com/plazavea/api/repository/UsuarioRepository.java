package com.plazavea.api.repository;

import com.plazavea.api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdUsuarioNot(String email, Integer idUsuario);

    Optional<Usuario> findByEmailIgnoreCase(String email);

    Optional<Usuario> findByTelefono(String telefono);

    Optional<Usuario> findByEmailIgnoreCaseAndEstado(String email, String estado);

    Optional<Usuario> findByTelefonoAndEstado(String telefono, String estado);
}
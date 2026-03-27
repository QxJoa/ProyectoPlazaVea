package com.plazavea.api.service;

import com.plazavea.api.dto.usuario.CreateUsuarioRequest;
import com.plazavea.api.dto.usuario.UpdateUsuarioRequest;
import com.plazavea.api.dto.usuario.UsuarioResponse;
import com.plazavea.api.model.Credencial;
import com.plazavea.api.model.Usuario;
import com.plazavea.api.repository.CredencialRepository;
import com.plazavea.api.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final CredencialRepository credencialRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          CredencialRepository credencialRepository,
                          PasswordEncoder passwordEncoder,
                          AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.credencialRepository = credencialRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public UsuarioResponse crearUsuario(Integer idUsuarioSolicitante, CreateUsuarioRequest request) {
        validarCreateRequest(request);

        String email = normalizar(request.getEmail());
        String username = normalizar(request.getUsername());

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new RuntimeException("Ya existe un usuario con ese email.");
        }

        if (credencialRepository.existsByUsernameIgnoreCase(username)) {
            throw new RuntimeException("Ya existe una credencial con ese username.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombres(normalizar(request.getNombres()));
        usuario.setApellidos(normalizar(request.getApellidos()));
        usuario.setEmail(email);
        usuario.setTelefono(normalizar(request.getTelefono()));
        usuario.setTipoUsuario(normalizar(request.getTipoUsuario()));
        usuario.setEstado("ACTIVO");

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        Credencial credencial = new Credencial();
        credencial.setUsuario(usuarioGuardado);
        credencial.setUsername(username);
        credencial.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        credencial.setSalt("BCRYPT");
        credencial.setEstado("ACTIVO");
        credencial.setFechaCambio(new Date());

        Credencial credencialGuardada = credencialRepository.save(credencial);

        auditoriaService.registrar(
                idUsuarioSolicitante,
                "USUARIO",
                "INSERT",
                "USUARIOS",
                "CREAR_USUARIO",
                "Usuario creado correctamente.",
                "ID usuario: " + usuarioGuardado.getIdUsuario()
                        + ", Email: " + usuarioGuardado.getEmail()
                        + ", Username: " + credencialGuardada.getUsername(),
                null
        );

        return toResponse(usuarioGuardado, credencialGuardada);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(usuario -> {
                    Optional<Credencial> credencialOpt =
                            credencialRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
                    return toResponse(usuario, credencialOpt.orElse(null));
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerUsuarioPorId(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        Credencial credencial = credencialRepository.findByUsuario_IdUsuario(usuario.getIdUsuario())
                .orElse(null);

        return toResponse(usuario, credencial);
    }

    @Transactional
    public UsuarioResponse actualizarUsuario(Integer idUsuarioSolicitante, Integer id, UpdateUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        Credencial credencial = credencialRepository.findByUsuario_IdUsuario(usuario.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Credencial no encontrada para el usuario."));

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            String emailNormalizado = normalizar(request.getEmail());

            if (usuarioRepository.existsByEmailIgnoreCaseAndIdUsuarioNot(emailNormalizado, id)) {
                throw new RuntimeException("Ya existe otro usuario con ese email.");
            }

            usuario.setEmail(emailNormalizado);
        }

        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            String usernameNormalizado = normalizar(request.getUsername());

            if (credencialRepository.existsByUsernameIgnoreCaseAndIdCredencialNot(
                    usernameNormalizado, credencial.getIdCredencial())) {
                throw new RuntimeException("Ya existe otra credencial con ese username.");
            }

            credencial.setUsername(usernameNormalizado);
        }

        if (request.getNombres() != null) {
            usuario.setNombres(normalizar(request.getNombres()));
        }

        if (request.getApellidos() != null) {
            usuario.setApellidos(normalizar(request.getApellidos()));
        }

        if (request.getTelefono() != null) {
            usuario.setTelefono(normalizar(request.getTelefono()));
        }

        if (request.getTipoUsuario() != null) {
            usuario.setTipoUsuario(normalizar(request.getTipoUsuario()));
        }

        if (request.getEstado() != null && !request.getEstado().trim().isEmpty()) {
            String estado = request.getEstado().trim().toUpperCase();
            usuario.setEstado(estado);
            credencial.setEstado(estado);
        }

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            credencial.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            credencial.setSalt("BCRYPT");
            credencial.setFechaCambio(new Date());
        }

        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        Credencial credencialActualizada = credencialRepository.save(credencial);

        auditoriaService.registrar(
                idUsuarioSolicitante,
                "USUARIO",
                "UPDATE",
                "USUARIOS",
                "ACTUALIZAR_USUARIO",
                "Usuario actualizado correctamente.",
                "ID usuario: " + usuarioActualizado.getIdUsuario()
                        + ", Email: " + usuarioActualizado.getEmail(),
                null
        );

        return toResponse(usuarioActualizado, credencialActualizada);
    }

    @Transactional
    public void desactivarUsuario(Integer idUsuarioSolicitante, Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        Credencial credencial = credencialRepository.findByUsuario_IdUsuario(usuario.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Credencial no encontrada para el usuario."));

        usuario.setEstado("INACTIVO");
        credencial.setEstado("INACTIVO");

        usuarioRepository.save(usuario);
        credencialRepository.save(credencial);

        auditoriaService.registrar(
                idUsuarioSolicitante,
                "USUARIO",
                "UPDATE",
                "USUARIOS",
                "DESACTIVAR_USUARIO",
                "Usuario desactivado correctamente.",
                "ID usuario: " + usuario.getIdUsuario()
                        + ", Email: " + usuario.getEmail(),
                null
        );
    }

    private void validarCreateRequest(CreateUsuarioRequest request) {
        if (request == null) {
            throw new RuntimeException("El cuerpo de la solicitud es obligatorio.");
        }

        if (esVacio(request.getNombres())) {
            throw new RuntimeException("El campo nombres es obligatorio.");
        }

        if (esVacio(request.getApellidos())) {
            throw new RuntimeException("El campo apellidos es obligatorio.");
        }

        if (esVacio(request.getEmail())) {
            throw new RuntimeException("El campo email es obligatorio.");
        }

        if (esVacio(request.getTipoUsuario())) {
            throw new RuntimeException("El campo tipoUsuario es obligatorio.");
        }

        if (esVacio(request.getUsername())) {
            throw new RuntimeException("El campo username es obligatorio.");
        }

        if (esVacio(request.getPassword())) {
            throw new RuntimeException("El campo password es obligatorio.");
        }
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim();
    }

    private UsuarioResponse toResponse(Usuario usuario, Credencial credencial) {
        UsuarioResponse response = new UsuarioResponse();

        response.setIdUsuario(usuario.getIdUsuario());
        response.setNombres(usuario.getNombres());
        response.setApellidos(usuario.getApellidos());
        response.setEmail(usuario.getEmail());
        response.setTelefono(usuario.getTelefono());
        response.setTipoUsuario(usuario.getTipoUsuario());
        response.setEstado(usuario.getEstado());

        if (credencial != null) {
            response.setUsername(credencial.getUsername());
            response.setCredencialEstado(credencial.getEstado());
            response.setFechaCambioCredencial(
                    credencial.getFechaCambio() != null ? credencial.getFechaCambio().toString() : null
            );
        }

        return response;
    }
}
package com.plazavea.api.service;

import java.util.Date;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.plazavea.api.dto.ChangePasswordRequest;
import com.plazavea.api.dto.LoginRequest;
import com.plazavea.api.dto.LoginResponse;
import com.plazavea.api.model.Credencial;
import com.plazavea.api.model.Sesion;
import com.plazavea.api.model.Usuario;
import com.plazavea.api.repository.CredencialRepository;
import com.plazavea.api.repository.SesionRepository;

@Service
public class AuthService {

    private final CredencialRepository credencialRepository;
    private final SesionRepository sesionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public AuthService(CredencialRepository credencialRepository,
                       SesionRepository sesionRepository,
                       PasswordEncoder passwordEncoder,
                       AuditoriaService auditoriaService) {
        this.credencialRepository = credencialRepository;
        this.sesionRepository = sesionRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    public LoginResponse login(LoginRequest request, String ip, String dispositivo) {
        if (request == null || esVacio(request.getUsername()) || esVacio(request.getPassword())) {
            return new LoginResponse(false, "Usuario y contraseña son obligatorios.", null);
        }

        Credencial credencial = credencialRepository
                .findByUsernameAndEstado(request.getUsername(), "ACTIVO")
                .orElse(null);

        if (credencial == null) {
            return new LoginResponse(false, "Usuario no encontrado o inactivo.", null);
        }

        Usuario usuario = credencial.getUsuario();
        if (usuario == null) {
            return new LoginResponse(false, "La credencial no tiene un usuario asociado.", null);
        }

        if (!"ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
            return new LoginResponse(false, "El usuario está inactivo.", usuario.getIdUsuario());
        }

        String passwordIngresada = request.getPassword();
        String passwordGuardada = credencial.getPasswordHash();

        boolean coincide = false;

        if (passwordGuardada != null && esBCrypt(passwordGuardada)) {
            coincide = passwordEncoder.matches(passwordIngresada, passwordGuardada);
        } else if (passwordGuardada != null && passwordGuardada.equals(passwordIngresada)) {
            coincide = true;

            credencial.setPasswordHash(passwordEncoder.encode(passwordIngresada));
            credencial.setFechaCambio(new Date());
            credencialRepository.save(credencial);
        }

        if (!coincide) {
            return new LoginResponse(false, "Contraseña incorrecta.", null);
        }

        Sesion sesion = new Sesion();
        sesion.setUsuario(usuario);
        sesion.setFechaInicio(new Date());
        sesion.setIp(ip);
        sesion.setDispositivo(dispositivo);
        sesionRepository.save(sesion);

        auditoriaService.registrar(
                usuario.getIdUsuario(),
                "SESION",
                "INSERT",
                "AUTENTICACION",
                "INICIO_SESION",
                "Inicio de sesión correcto"
        );

        return new LoginResponse(true, "Login correcto.", usuario.getIdUsuario());
    }

    public LoginResponse logout(Integer idUsuario) {
        if (idUsuario == null) {
            return new LoginResponse(false, "El idUsuario es obligatorio.", null);
        }

        List<Sesion> sesiones = sesionRepository
                .findAllByUsuarioIdUsuarioAndFechaFinIsNull(idUsuario);

        if (sesiones == null || sesiones.isEmpty()) {
            return new LoginResponse(false, "No hay sesiones activas para cerrar.", idUsuario);
        }

        Date ahora = new Date();

        for (Sesion sesion : sesiones) {
            sesion.setFechaFin(ahora);
        }

        sesionRepository.saveAll(sesiones);

        auditoriaService.registrar(
                idUsuario,
                "SESION",
                "UPDATE",
                "AUTENTICACION",
                "CIERRE_SESION",
                "Cierre de todas las sesiones activas"
        );

        return new LoginResponse(true, "Sesión(es) cerrada(s) correctamente.", idUsuario);
    }

    public LoginResponse cambiarPassword(ChangePasswordRequest request) {
        if (request == null) {
            return new LoginResponse(false, "La solicitud es inválida.", null);
        }

        if (request.getIdUsuario() == null) {
            return new LoginResponse(false, "El idUsuario es obligatorio.", null);
        }

        if (esVacio(request.getPasswordActual())
                || esVacio(request.getPasswordNueva())
                || esVacio(request.getConfirmarPassword())) {
            return new LoginResponse(false, "Todos los campos son obligatorios.", request.getIdUsuario());
        }

        if (!request.getPasswordNueva().equals(request.getConfirmarPassword())) {
            return new LoginResponse(false, "La nueva contraseña y la confirmación no coinciden.", request.getIdUsuario());
        }

        if (request.getPasswordNueva().length() < 8) {
            return new LoginResponse(false, "La nueva contraseña debe tener al menos 8 caracteres.", request.getIdUsuario());
        }

        Credencial credencial = credencialRepository
                .findByUsuario_IdUsuario(request.getIdUsuario())
                .orElse(null);

        if (credencial == null) {
            return new LoginResponse(false, "No se encontró la credencial del usuario.", request.getIdUsuario());
        }

        Usuario usuario = credencial.getUsuario();
        if (usuario == null) {
            return new LoginResponse(false, "La credencial no tiene un usuario asociado.", request.getIdUsuario());
        }

        if (!"ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
            return new LoginResponse(false, "El usuario está inactivo.", request.getIdUsuario());
        }

        if (!"ACTIVO".equalsIgnoreCase(credencial.getEstado())) {
            return new LoginResponse(false, "La credencial del usuario está inactiva.", request.getIdUsuario());
        }

        String passwordActualGuardada = credencial.getPasswordHash();
        boolean passwordActualValida = false;

        if (passwordActualGuardada != null && esBCrypt(passwordActualGuardada)) {
            passwordActualValida = passwordEncoder.matches(request.getPasswordActual(), passwordActualGuardada);
        } else if (passwordActualGuardada != null && passwordActualGuardada.equals(request.getPasswordActual())) {
            passwordActualValida = true;
        }

        if (!passwordActualValida) {
            return new LoginResponse(false, "La contraseña actual es incorrecta.", request.getIdUsuario());
        }

        boolean mismaPassword = false;

        if (passwordActualGuardada != null && esBCrypt(passwordActualGuardada)) {
            mismaPassword = passwordEncoder.matches(request.getPasswordNueva(), passwordActualGuardada);
        } else if (passwordActualGuardada != null) {
            mismaPassword = passwordActualGuardada.equals(request.getPasswordNueva());
        }

        if (mismaPassword) {
            return new LoginResponse(false, "La nueva contraseña no puede ser igual a la actual.", request.getIdUsuario());
        }

        credencial.setPasswordHash(passwordEncoder.encode(request.getPasswordNueva()));
        credencial.setFechaCambio(new Date());
        credencialRepository.save(credencial);

        auditoriaService.registrar(
                request.getIdUsuario(),
                "CREDENCIAL",
                "UPDATE",
                "AUTENTICACION",
                "CAMBIO_PASSWORD",
                "Cambio de contraseña realizado por usuario logueado"
        );

        return new LoginResponse(true, "Contraseña actualizada correctamente.", request.getIdUsuario());
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private boolean esBCrypt(String hash) {
        return hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
    }
}
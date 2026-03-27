package com.plazavea.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.plazavea.api.dto.ChangePasswordRequest;
import com.plazavea.api.dto.LoginRequest;
import com.plazavea.api.dto.LoginResponse;
import com.plazavea.api.dto.LogoutRequest;
import com.plazavea.api.dto.recuperacion.ApiMessageResponse;
import com.plazavea.api.dto.recuperacion.PasswordResetConfirmRequest;
import com.plazavea.api.dto.recuperacion.PasswordResetRequest;
import com.plazavea.api.service.AuthService;
import com.plazavea.api.service.PasswordResetService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest) {

        String ip = obtenerIpCliente(httpRequest);
        String dispositivo = obtenerDispositivo(httpRequest);

        LoginResponse response = authService.login(request, ip, dispositivo);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout(@RequestBody LogoutRequest request) {
        LoginResponse response = authService.logout(request.getIdUsuario());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<LoginResponse> changePassword(@RequestBody ChangePasswordRequest request) {
        LoginResponse response = authService.cambiarPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<ApiMessageResponse> requestPasswordReset(@RequestBody PasswordResetRequest request,
                                                                  HttpServletRequest httpRequest) {
        String ip = obtenerIpCliente(httpRequest);
        ApiMessageResponse response = passwordResetService.solicitarRecuperacion(request, ip);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<ApiMessageResponse> confirmPasswordReset(@RequestBody PasswordResetConfirmRequest request,
                                                                  HttpServletRequest httpRequest) {
        String ip = obtenerIpCliente(httpRequest);
        ApiMessageResponse response = passwordResetService.confirmarRecuperacion(request, ip);
        return ResponseEntity.ok(response);
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else if (ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    private String obtenerDispositivo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");

        if (userAgent == null || userAgent.isBlank()) {
            return "DESCONOCIDO";
        }

        return userAgent;
    }
}
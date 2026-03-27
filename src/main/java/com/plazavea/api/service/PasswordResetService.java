package com.plazavea.api.service;

import com.plazavea.api.dto.recuperacion.ApiMessageResponse;
import com.plazavea.api.dto.recuperacion.PasswordResetConfirmRequest;
import com.plazavea.api.dto.recuperacion.PasswordResetRequest;
import com.plazavea.api.exception.BadRequestException;
import com.plazavea.api.model.CodigoRecuperacion;
import com.plazavea.api.model.Credencial;
import com.plazavea.api.model.Usuario;
import com.plazavea.api.repository.CodigoRecuperacionRepository;
import com.plazavea.api.repository.CredencialRepository;
import com.plazavea.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class PasswordResetService {

    private final UsuarioRepository usuarioRepository;
    private final CredencialRepository credencialRepository;
    private final CodigoRecuperacionRepository codigoRecuperacionRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailOtpService emailOtpService;
    private final AuditoriaService auditoriaService;

    @Value("${app.security.password-reset.otp-expiration-minutes:3}")
    private int otpExpirationMinutes;

    @Value("${app.security.password-reset.max-attempts:3}")
    private int maxAttempts;

    @Value("${app.security.password-reset.max-resends:3}")
    private int maxResends;

    @Value("${app.security.password-reset.generic-message:Si los datos ingresados son correctos, recibirás un código de verificación por correo electrónico.}")
    private String genericMessage;

    public PasswordResetService(UsuarioRepository usuarioRepository,
                                CredencialRepository credencialRepository,
                                CodigoRecuperacionRepository codigoRecuperacionRepository,
                                PasswordEncoder passwordEncoder,
                                EmailOtpService emailOtpService,
                                AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.credencialRepository = credencialRepository;
        this.codigoRecuperacionRepository = codigoRecuperacionRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailOtpService = emailOtpService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
public ApiMessageResponse solicitarRecuperacion(PasswordResetRequest request, String ipOrigen) {

    System.out.println("=== INICIO RESET ===");

    if (request == null || isBlank(request.getIdentificador())) {
        System.out.println("Request vacío o identificador vacío");
        return new ApiMessageResponse(true, genericMessage);
    }

    System.out.println("Identificador recibido: " + request.getIdentificador());

    String identificador = normalizarIdentificador(request.getIdentificador());

    System.out.println("Identificador normalizado: " + identificador);

    Optional<Usuario> usuarioOpt = buscarUsuarioActivoPorIdentificador(identificador);

    System.out.println("Usuario encontrado: " + usuarioOpt.isPresent());

    if (usuarioOpt.isEmpty()) {
        System.out.println("No se encontró usuario");
        return new ApiMessageResponse(true, genericMessage);
    }

    Usuario usuario = usuarioOpt.get();

    System.out.println("Email del usuario: " + usuario.getEmail());
    System.out.println("Estado del usuario: " + usuario.getEstado());

    if (isBlank(usuario.getEmail())) {
        System.out.println("Usuario sin email");
        return new ApiMessageResponse(true, genericMessage);
    }

        List<CodigoRecuperacion> pendientes =
                codigoRecuperacionRepository.findByUsuario_IdUsuarioAndEstado(usuario.getIdUsuario(), "PENDIENTE");

        for (CodigoRecuperacion item : pendientes) {
            if (item.getReenvios() != null && item.getReenvios() >= maxResends) {
                return new ApiMessageResponse(true, genericMessage);
            }
            item.setEstado("INVALIDADO");
            codigoRecuperacionRepository.save(item);
        }

        String codigoOtp = generarOtp6Digitos();
        String codigoHash = passwordEncoder.encode(codigoOtp);

        CodigoRecuperacion codigo = new CodigoRecuperacion();
        codigo.setUsuario(usuario);
        codigo.setIdentificador(identificador);
        codigo.setTelefonoDestino(usuario.getEmail());
        codigo.setCodigoHash(codigoHash);
        codigo.setEstado("PENDIENTE");
        codigo.setIntentos(0);
        codigo.setReenvios(1);
        codigo.setFechaCreacion(LocalDateTime.now());
        codigo.setFechaExpiracion(LocalDateTime.now().plusMinutes(otpExpirationMinutes));

        codigoRecuperacionRepository.save(codigo);
        System.out.println("=== ENVIANDO EMAIL ===");
        System.out.println("Destino: " + usuario.getEmail());
        System.out.println("OTP generado: " + codigoOtp);

        emailOtpService.enviarCodigo(usuario.getEmail(), codigoOtp);

        auditoriaService.registrar(
                usuario.getIdUsuario(),
                "CODIGO_RECUPERACION",
                "INSERT",
                "SEGURIDAD",
                "SOLICITUD_RESET_PASSWORD",
                "Se solicitó recuperación de contraseña por correo",
                "Identificador ingresado: " + identificador,
                ipOrigen
        );

        return new ApiMessageResponse(true, genericMessage);
    }

    @Transactional
    public ApiMessageResponse confirmarRecuperacion(PasswordResetConfirmRequest request, String ipOrigen) {
        validarRequestConfirmacion(request);

        String identificador = normalizarIdentificador(request.getIdentificador());

        Usuario usuario = buscarUsuarioActivoPorIdentificador(identificador)
                .orElseThrow(() -> new BadRequestException("No se pudo completar la recuperación."));

        CodigoRecuperacion codigo = codigoRecuperacionRepository
                .findTopByUsuario_IdUsuarioAndEstadoOrderByIdCodigoDesc(usuario.getIdUsuario(), "PENDIENTE")
                .orElseThrow(() -> new BadRequestException("No se encontró un código pendiente."));

        if (codigo.getFechaExpiracion() == null || codigo.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            codigo.setEstado("EXPIRADO");
            codigoRecuperacionRepository.save(codigo);
            throw new BadRequestException("El código ha expirado.");
        }

        if (codigo.getIntentos() != null && codigo.getIntentos() >= maxAttempts) {
            codigo.setEstado("BLOQUEADO");
            codigoRecuperacionRepository.save(codigo);
            throw new BadRequestException("Se alcanzó el máximo de intentos permitidos.");
        }

        boolean otpValido = passwordEncoder.matches(request.getCodigoOtp(), codigo.getCodigoHash());

        if (!otpValido) {
            int intentos = codigo.getIntentos() == null ? 0 : codigo.getIntentos();
            codigo.setIntentos(intentos + 1);

            if (codigo.getIntentos() >= maxAttempts) {
                codigo.setEstado("BLOQUEADO");
            }

            codigoRecuperacionRepository.save(codigo);

            auditoriaService.registrar(
                    usuario.getIdUsuario(),
                    "CODIGO_RECUPERACION",
                    "UPDATE",
                    "SEGURIDAD",
                    "OTP_INVALIDO",
                    "Intento fallido de validación OTP por correo",
                    "Intentos actuales: " + codigo.getIntentos(),
                    ipOrigen
            );

            throw new BadRequestException("El código OTP es incorrecto.");
        }

        Credencial credencial = credencialRepository.findByUsuario_IdUsuario(usuario.getIdUsuario())
                .orElseThrow(() -> new BadRequestException("No se encontró credencial asociada al usuario."));

        credencial.setPasswordHash(passwordEncoder.encode(request.getNuevaPassword()));
        credencial.setSalt("BCRYPT");
        credencial.setFechaCambio(new Date());
        credencialRepository.save(credencial);

        codigo.setEstado("USADO");
        codigo.setFechaValidacion(LocalDateTime.now());
        codigoRecuperacionRepository.save(codigo);

        auditoriaService.registrar(
                usuario.getIdUsuario(),
                "CREDENCIAL",
                "UPDATE",
                "SEGURIDAD",
                "RESET_PASSWORD_EXITOSO",
                "Se restableció la contraseña del usuario por correo",
                "Recuperación completada correctamente",
                ipOrigen
        );

        return new ApiMessageResponse(true, "Contraseña actualizada correctamente.");
    }

    private void validarRequestConfirmacion(PasswordResetConfirmRequest request) {
        if (request == null) {
            throw new BadRequestException("Solicitud inválida.");
        }

        if (isBlank(request.getIdentificador())) {
            throw new BadRequestException("El identificador es obligatorio.");
        }

        if (isBlank(request.getCodigoOtp())) {
            throw new BadRequestException("El código OTP es obligatorio.");
        }

        if (isBlank(request.getNuevaPassword())) {
            throw new BadRequestException("La nueva contraseña es obligatoria.");
        }

        if (isBlank(request.getConfirmarPassword())) {
            throw new BadRequestException("La confirmación de contraseña es obligatoria.");
        }

        if (!request.getNuevaPassword().equals(request.getConfirmarPassword())) {
            throw new BadRequestException("La nueva contraseña y la confirmación no coinciden.");
        }

        validarPasswordSegura(request.getNuevaPassword());
    }

    private void validarPasswordSegura(String password) {
        if (password.length() < 8) {
            throw new BadRequestException("La nueva contraseña debe tener al menos 8 caracteres.");
        }

        boolean tieneMayuscula = password.matches(".*[A-Z].*");
        boolean tieneMinuscula = password.matches(".*[a-z].*");
        boolean tieneNumero = password.matches(".*\\d.*");

        if (!tieneMayuscula || !tieneMinuscula || !tieneNumero) {
            throw new BadRequestException("La nueva contraseña debe incluir mayúscula, minúscula y número.");
        }
    }

    
    private Optional<Usuario> buscarUsuarioActivoPorIdentificador(String identificador) {
    System.out.println("Buscando identificador: [" + identificador + "]");

    if (identificador.contains("@")) {
        Optional<Usuario> porEmail = usuarioRepository.findByEmailIgnoreCaseAndEstado(identificador, "ACTIVO");
        System.out.println("Resultado por email: " + porEmail.isPresent());
        return porEmail;
    }

    String telefonoLimpio = limpiarTelefono(identificador);
    Optional<Usuario> porTelefono = usuarioRepository.findByTelefonoAndEstado(telefonoLimpio, "ACTIVO");
    System.out.println("Resultado por telefono: " + porTelefono.isPresent());
    return porTelefono;
}

    private String normalizarIdentificador(String valor) {
        String limpio = valor == null ? "" : valor.trim();
        if (limpio.contains("@")) {
            return limpio.toLowerCase();
        }
        return limpiarTelefono(limpio);
    }

    private String limpiarTelefono(String telefono) {
        return telefono.replaceAll("[^0-9]", "");
    }

    private String generarOtp6Digitos() {
        int numero = 100000 + new Random().nextInt(900000);
        return String.valueOf(numero);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
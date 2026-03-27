package com.plazavea.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailOtpService {

    private final JavaMailSender mailSender;

    @Value("${app.integration.email.enabled:false}")
    private boolean enabled;

    @Value("${app.integration.email.from:}")
    private String from;

    public EmailOtpService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCodigo(String destino, String codigoOtp) {
        if (!enabled) {
            System.out.println("EMAIL OTP DESHABILITADO");
            return;
        }

        try {
            System.out.println("=== ENVIANDO OTP POR CORREO ===");
            System.out.println("FROM: " + from);
            System.out.println("TO: " + destino);
            System.out.println("CODIGO: " + codigoOtp);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(destino);
            message.setSubject("Código de recuperación - Plaza Vea");
            message.setText(
                    "Hola,\n\n" +
                    "Tu código de verificación para recuperar tu contraseña es: " + codigoOtp + "\n\n" +
                    "Este código vence en 3 minutos.\n" +
                    "Si no solicitaste este cambio, ignora este correo.\n\n" +
                    "Plaza Vea"
            );

            mailSender.send(message);

            System.out.println("=== CORREO ENVIADO CORRECTAMENTE ===");

        } catch (Exception e) {
            System.out.println("=== ERROR EN ENVIO DE CORREO ===");
            e.printStackTrace();
            throw new RuntimeException("Error enviando correo OTP: " + e.getMessage(), e);
        }
    }
}
package com.plazavea.api.service;

import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwilioWhatsappService {

    @Value("${app.integration.twilio.enabled:false}")
    private boolean enabled;

    @Value("${app.integration.twilio.account-sid:}")
    private String accountSid;

    @Value("${app.integration.twilio.auth-token:}")
    private String authToken;

    @Value("${app.integration.twilio.verify-service-sid:}")
    private String verifyServiceSid;

    @Value("${app.integration.twilio.channel:whatsapp}")
    private String channel;

    @PostConstruct
    public void init() {
        if (enabled) {
            Twilio.init(accountSid, authToken);
        }
    }

    public void enviarCodigo(String telefonoE164) {
    if (!enabled) {
        return;
    }

    try {
        System.out.println("=== ENVIANDO OTP POR TWILIO ===");
        System.out.println("Telefono destino: " + telefonoE164);
        System.out.println("Verify Service SID: " + verifyServiceSid);
        System.out.println("Canal: " + channel);

        Verification.creator(
                verifyServiceSid,
                telefonoE164,
                channel
        ).create();

        System.out.println("=== OTP ENVIADO CORRECTAMENTE ===");

    } catch (Exception e) {
        System.out.println("=== ERROR EN TWILIO ===");
        e.printStackTrace();
        throw new RuntimeException("Error enviando OTP por Twilio: " + e.getMessage(), e);
    }
}

    public boolean verificarCodigo(String telefonoE164, String codigoOtp) {
        if (!enabled) {
            return false;
        }

        VerificationCheck verificationCheck = VerificationCheck.creator(verifyServiceSid)
                .setTo(telefonoE164)
                .setCode(codigoOtp)
                .create();

        return "approved".equalsIgnoreCase(verificationCheck.getStatus());
    }
}
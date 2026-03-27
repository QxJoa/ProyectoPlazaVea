package com.plazavea.api.dto.recuperacion;

public class PasswordResetRequest {

    private String identificador;

    public PasswordResetRequest() {
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }
}
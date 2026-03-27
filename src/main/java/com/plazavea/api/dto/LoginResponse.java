package com.plazavea.api.dto;

public class LoginResponse {

    private boolean success;
    private String message;
    private Integer idUsuario;

    public LoginResponse() {
    }

    public LoginResponse(boolean success, String message, Integer idUsuario) {
        this.success = success;
        this.message = message;
        this.idUsuario = idUsuario;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
}
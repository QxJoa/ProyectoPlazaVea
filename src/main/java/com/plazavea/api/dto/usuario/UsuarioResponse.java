package com.plazavea.api.dto.usuario;

public class UsuarioResponse {

    private Integer idUsuario;
    private String nombres;
    private String apellidos;
    private String email;
    private String telefono;
    private String tipoUsuario;
    private String estado;
    private String username;
    private String credencialEstado;
    private String fechaCambioCredencial;

    public UsuarioResponse() {
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCredencialEstado() {
        return credencialEstado;
    }

    public void setCredencialEstado(String credencialEstado) {
        this.credencialEstado = credencialEstado;
    }

    public String getFechaCambioCredencial() {
        return fechaCambioCredencial;
    }

    public void setFechaCambioCredencial(String fechaCambioCredencial) {
        this.fechaCambioCredencial = fechaCambioCredencial;
    }
}
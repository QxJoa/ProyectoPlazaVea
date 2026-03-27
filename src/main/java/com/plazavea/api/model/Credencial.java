package com.plazavea.api.model;

import java.util.Date;

import jakarta.persistence.*;

@Entity
@Table(name = "CREDENCIAL")
public class Credencial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CREDENCIAL")
    private Integer idCredencial;

    @OneToOne
    @JoinColumn(name = "ID_USUARIO")
    private Usuario usuario;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "PASSWORD_HASH")
    private String passwordHash;

    @Column(name = "SALT")
    private String salt;

    @Column(name = "ESTADO")
    private String estado;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_CAMBIO")
    private Date fechaCambio;

    public Credencial() {
    }

    public Integer getIdCredencial() {
        return idCredencial;
    }

    public void setIdCredencial(Integer idCredencial) {
        this.idCredencial = idCredencial;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Date getFechaCambio() {
        return fechaCambio;
    }

    public void setFechaCambio(Date fechaCambio) {
        this.fechaCambio = fechaCambio;
    }
}
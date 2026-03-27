package com.plazavea.api.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "LOG_LOGIN")
public class LogLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_LOG")
    private Integer idLog;

    @ManyToOne
    @JoinColumn(name = "ID_USUARIO")
    private Usuario usuario;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_INTENTO")
    private Date fechaIntento;

    @Column(name = "IP")
    private String ip;

    @Column(name = "RESULTADO")
    private String resultado;

    @Column(name = "MOTIVO")
    private String motivo;

    public LogLogin() {
    }

    public Integer getIdLog() {
        return idLog;
    }

    public void setIdLog(Integer idLog) {
        this.idLog = idLog;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Date getFechaIntento() {
        return fechaIntento;
    }

    public void setFechaIntento(Date fechaIntento) {
        this.fechaIntento = fechaIntento;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
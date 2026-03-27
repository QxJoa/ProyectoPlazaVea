package com.plazavea.api.dto.auditoria;

public class AuditoriaResponse {

    private Integer idAuditoria;
    private String tablaAfectada;
    private String operacion;
    private Integer idUsuarioResponsable;
    private String usernameResponsable;
    private String nombreResponsable;
    private String modulo;
    private String accion;
    private String descripcion;
    private String detalleAdicional;
    private String fechaHora;
    private String ipOrigen;

    public AuditoriaResponse() {
    }

    public Integer getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(Integer idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public String getTablaAfectada() {
        return tablaAfectada;
    }

    public void setTablaAfectada(String tablaAfectada) {
        this.tablaAfectada = tablaAfectada;
    }

    public String getOperacion() {
        return operacion;
    }

    public void setOperacion(String operacion) {
        this.operacion = operacion;
    }

    public Integer getIdUsuarioResponsable() {
        return idUsuarioResponsable;
    }

    public void setIdUsuarioResponsable(Integer idUsuarioResponsable) {
        this.idUsuarioResponsable = idUsuarioResponsable;
    }

    public String getUsernameResponsable() {
        return usernameResponsable;
    }

    public void setUsernameResponsable(String usernameResponsable) {
        this.usernameResponsable = usernameResponsable;
    }

    public String getNombreResponsable() {
        return nombreResponsable;
    }

    public void setNombreResponsable(String nombreResponsable) {
        this.nombreResponsable = nombreResponsable;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDetalleAdicional() {
        return detalleAdicional;
    }

    public void setDetalleAdicional(String detalleAdicional) {
        this.detalleAdicional = detalleAdicional;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }

    public void setIpOrigen(String ipOrigen) {
        this.ipOrigen = ipOrigen;
    }
}
package com.plazavea.api.dto.rol;

public class AsignarRolRequest {

    private Integer idUsuarioSolicitante;
    private Integer idUsuarioObjetivo;
    private Integer idRol;

    public AsignarRolRequest() {
    }

    public Integer getIdUsuarioSolicitante() {
        return idUsuarioSolicitante;
    }

    public void setIdUsuarioSolicitante(Integer idUsuarioSolicitante) {
        this.idUsuarioSolicitante = idUsuarioSolicitante;
    }

    public Integer getIdUsuarioObjetivo() {
        return idUsuarioObjetivo;
    }

    public void setIdUsuarioObjetivo(Integer idUsuarioObjetivo) {
        this.idUsuarioObjetivo = idUsuarioObjetivo;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }
}
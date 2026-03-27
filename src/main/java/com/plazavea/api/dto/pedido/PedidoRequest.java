package com.plazavea.api.dto.pedido;

import java.math.BigDecimal;

public class PedidoRequest {

    private Integer idUsuario;
    private BigDecimal total;
    private String estado;

    public PedidoRequest() {
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
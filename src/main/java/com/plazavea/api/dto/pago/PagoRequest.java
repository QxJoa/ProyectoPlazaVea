package com.plazavea.api.dto.pago;

import java.math.BigDecimal;

public class PagoRequest {

    private Integer idPedido;
    private BigDecimal monto;
    private String metodo;

    public Integer getIdPedido() { return idPedido; }
    public void setIdPedido(Integer idPedido) { this.idPedido = idPedido; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
}
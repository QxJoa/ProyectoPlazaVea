package com.plazavea.api.service;

import com.plazavea.api.dto.reporte.ReporteAuditoriaResponse;
import com.plazavea.api.dto.reporte.ReportePagoResponse;
import com.plazavea.api.dto.reporte.ReportePedidoResponse;
import com.plazavea.api.dto.reporte.ReporteProductoStockResponse;
import com.plazavea.api.model.Auditoria;
import com.plazavea.api.model.Pago;
import com.plazavea.api.model.Pedido;
import com.plazavea.api.model.Producto;
import com.plazavea.api.repository.AuditoriaRepository;
import com.plazavea.api.repository.PagoRepository;
import com.plazavea.api.repository.PedidoRepository;
import com.plazavea.api.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReporteService {

    private final PedidoRepository pedidoRepository;
    private final PagoRepository pagoRepository;
    private final ProductoRepository productoRepository;
    private final AuditoriaRepository auditoriaRepository;

    public ReporteService(PedidoRepository pedidoRepository,
                          PagoRepository pagoRepository,
                          ProductoRepository productoRepository,
                          AuditoriaRepository auditoriaRepository) {
        this.pedidoRepository = pedidoRepository;
        this.pagoRepository = pagoRepository;
        this.productoRepository = productoRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<ReportePedidoResponse> reportePedidos() {
        List<Pedido> lista = pedidoRepository.findAllByOrderByIdPedidoDesc();
        List<ReportePedidoResponse> response = new ArrayList<>();

        for (Pedido pedido : lista) {
            ReportePedidoResponse dto = new ReportePedidoResponse();
            dto.setIdPedido(pedido.getIdPedido());

            if (pedido.getUsuario() != null) {
                dto.setIdUsuario(pedido.getUsuario().getIdUsuario());
                dto.setNombresUsuario(pedido.getUsuario().getNombres());
                dto.setApellidosUsuario(pedido.getUsuario().getApellidos());
            }

            dto.setFecha(pedido.getFecha() != null ? pedido.getFecha().toString() : null);
            dto.setTotal(pedido.getTotal());
            dto.setEstado(pedido.getEstado());

            response.add(dto);
        }

        return response;
    }

    @Transactional(readOnly = true)
    public List<ReportePagoResponse> reportePagos() {
        List<Pago> lista = pagoRepository.findAllByOrderByIdPagoDesc();
        List<ReportePagoResponse> response = new ArrayList<>();

        for (Pago pago : lista) {
            ReportePagoResponse dto = new ReportePagoResponse();
            dto.setIdPago(pago.getIdPago());

            if (pago.getPedido() != null) {
                dto.setIdPedido(pago.getPedido().getIdPedido());
            }

            dto.setMonto(pago.getMonto());
            dto.setMetodo(pago.getMetodo());
            dto.setEstado(pago.getEstado());
            dto.setFecha(pago.getFecha() != null ? pago.getFecha().toString() : null);

            response.add(dto);
        }

        return response;
    }

    @Transactional(readOnly = true)
    public List<ReporteProductoStockResponse> reporteProductosConStock() {
        List<Producto> lista = productoRepository.findAll();
        List<ReporteProductoStockResponse> response = new ArrayList<>();

        for (Producto producto : lista) {
            ReporteProductoStockResponse dto = new ReporteProductoStockResponse();
            dto.setIdProducto(producto.getIdProducto());
            dto.setNombreProducto(producto.getNombre());
            dto.setCategoria(
                    producto.getCategoria() != null
                            ? producto.getCategoria().getNombre()
                            : null
            );
            dto.setPrecio(producto.getPrecio());
            dto.setStock(producto.getStock());
            dto.setEstado(producto.getEstado());

            response.add(dto);
        }

        return response;
    }

    @Transactional(readOnly = true)
    public List<ReporteAuditoriaResponse> reporteAuditoria() {
        List<Auditoria> lista = auditoriaRepository.findAllByOrderByIdAuditoriaDesc();
        List<ReporteAuditoriaResponse> response = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        for (Auditoria item : lista) {
            ReporteAuditoriaResponse dto = new ReporteAuditoriaResponse();
            dto.setIdAuditoria(item.getIdAuditoria());
            dto.setTablaAfectada(item.getTablaAfectada());
            dto.setOperacion(item.getOperacion());
            dto.setModulo(item.getModulo());
            dto.setAccion(item.getAccion());
            dto.setDescripcion(item.getDescripcion());
            dto.setDetalleAdicional(item.getDetalleAdicional());
            dto.setIpOrigen(item.getIpOrigen());

            dto.setFechaHora(
                    item.getFechaHora() != null
                            ? item.getFechaHora().format(formatter)
                            : null
            );

            if (item.getUsuarioResponsable() != null) {
                dto.setIdUsuarioResponsable(item.getUsuarioResponsable().getIdUsuario());
            }

            response.add(dto);
        }

        return response;
    }
}
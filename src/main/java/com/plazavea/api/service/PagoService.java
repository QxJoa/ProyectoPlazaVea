package com.plazavea.api.service;

import com.plazavea.api.dto.pago.PagoRequest;
import com.plazavea.api.dto.pago.PagoResponse;
import com.plazavea.api.exception.BadRequestException;
import com.plazavea.api.exception.ResourceNotFoundException;
import com.plazavea.api.model.Pago;
import com.plazavea.api.model.Pedido;
import com.plazavea.api.repository.DetallePedidoRepository;
import com.plazavea.api.repository.PagoRepository;
import com.plazavea.api.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PagoService {

    private final PagoRepository pagoRepo;
    private final PedidoRepository pedidoRepo;
    private final DetallePedidoRepository detallePedidoRepository;
    private final AuditoriaService auditoriaService;

    public PagoService(PagoRepository pagoRepo,
                       PedidoRepository pedidoRepo,
                       DetallePedidoRepository detallePedidoRepository,
                       AuditoriaService auditoriaService) {
        this.pagoRepo = pagoRepo;
        this.pedidoRepo = pedidoRepo;
        this.detallePedidoRepository = detallePedidoRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public PagoResponse registrarPago(Integer idUsuario, PagoRequest request) {

        if (request == null) {
            throw new BadRequestException("Debe enviar los datos del pago.");
        }

        if (request.getIdPedido() == null) {
            throw new BadRequestException("Debe indicar el pedido a pagar.");
        }

        if (request.getMonto() == null || request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El monto del pago es inválido.");
        }

        if (request.getMetodo() == null || request.getMetodo().trim().isEmpty()) {
            throw new BadRequestException("Debe indicar el método de pago.");
        }

        Pedido pedido = pedidoRepo.findById(request.getIdPedido())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado."));

        if ("PAGADO".equalsIgnoreCase(pedido.getEstado())) {
            throw new BadRequestException("El pedido ya fue pagado.");
        }

        if ("CANCELADO".equalsIgnoreCase(pedido.getEstado())) {
            throw new BadRequestException("No se puede pagar un pedido cancelado.");
        }

        boolean tieneDetalle = !detallePedidoRepository.findByPedido_IdPedido(pedido.getIdPedido()).isEmpty();
        if (!tieneDetalle) {
            throw new BadRequestException("No se puede pagar un pedido sin detalle.");
        }

        if (pedido.getTotal() == null || pedido.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El pedido no tiene total válido.");
        }

        if (request.getMonto().compareTo(pedido.getTotal()) != 0) {
            throw new BadRequestException("El monto del pago debe ser igual al total del pedido.");
        }

        Pago pago = new Pago();
        pago.setPedido(pedido);
        pago.setMonto(request.getMonto());
        pago.setMetodo(request.getMetodo().trim().toUpperCase());
        pago.setEstado("COMPLETADO");

        Pago guardado = pagoRepo.save(pago);

        String estadoAnterior = pedido.getEstado();
        pedido.setEstado("PAGADO");
        pedidoRepo.save(pedido);

        auditoriaService.registrar(
                idUsuario,
                "PAGO",
                "INSERT",
                "PAGOS",
                "REGISTRAR_PAGO",
                "Pago registrado correctamente",
                "Pedido: " + pedido.getIdPedido()
                        + ", Monto: " + pago.getMonto()
                        + ", Método: " + pago.getMetodo(),
                null
        );

        auditoriaService.registrar(
                idUsuario,
                "PEDIDO",
                "UPDATE",
                "PEDIDOS",
                "ACTUALIZAR_ESTADO_PEDIDO",
                "Pedido marcado como PAGADO",
                "Pedido ID: " + pedido.getIdPedido()
                        + ", Estado anterior: " + estadoAnterior
                        + ", Estado nuevo: PAGADO",
                null
        );

        return toResponse(guardado);
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> listarPagos() {
        List<Pago> pagos = pagoRepo.findAllByOrderByIdPagoDesc();
        List<PagoResponse> response = new ArrayList<>();

        for (Pago pago : pagos) {
            response.add(toResponse(pago));
        }

        return response;
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> listarPagosPorPedido(Integer idPedido) {
        pedidoRepo.findById(idPedido)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado."));

        List<Pago> pagos = pagoRepo.findByPedido_IdPedidoOrderByIdPagoDesc(idPedido);
        List<PagoResponse> response = new ArrayList<>();

        for (Pago pago : pagos) {
            response.add(toResponse(pago));
        }

        return response;
    }

    @Transactional(readOnly = true)
    public PagoResponse obtenerPagoPorId(Integer idPago) {
        Pago pago = pagoRepo.findById(idPago)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado."));

        return toResponse(pago);
    }

    private PagoResponse toResponse(Pago p) {
        PagoResponse r = new PagoResponse();
        r.setIdPago(p.getIdPago());
        r.setIdPedido(p.getPedido().getIdPedido());
        r.setMonto(p.getMonto());
        r.setMetodo(p.getMetodo());
        r.setEstado(p.getEstado());
        return r;
    }
}
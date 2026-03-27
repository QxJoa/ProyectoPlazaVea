package com.plazavea.api.service;

import com.plazavea.api.dto.detalle.DetallePedidoRequest;
import com.plazavea.api.dto.detalle.DetallePedidoResponse;
import com.plazavea.api.exception.BadRequestException;
import com.plazavea.api.exception.ResourceNotFoundException;
import com.plazavea.api.model.DetallePedido;
import com.plazavea.api.model.Pedido;
import com.plazavea.api.model.Producto;
import com.plazavea.api.repository.DetallePedidoRepository;
import com.plazavea.api.repository.PedidoRepository;
import com.plazavea.api.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class DetallePedidoService {

    private final DetallePedidoRepository detalleRepo;
    private final PedidoRepository pedidoRepo;
    private final ProductoRepository productoRepo;
    private final AuditoriaService auditoriaService;

    public DetallePedidoService(DetallePedidoRepository detalleRepo,
                                PedidoRepository pedidoRepo,
                                ProductoRepository productoRepo,
                                AuditoriaService auditoriaService) {
        this.detalleRepo = detalleRepo;
        this.pedidoRepo = pedidoRepo;
        this.productoRepo = productoRepo;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public DetallePedidoResponse agregarProducto(Integer idUsuario,
                                                 DetallePedidoRequest request) {

        if (request == null) {
            throw new BadRequestException("Debe enviar los datos del detalle.");
        }

        if (request.getIdPedido() == null) {
            throw new BadRequestException("Debe indicar el pedido.");
        }

        if (request.getIdProducto() == null) {
            throw new BadRequestException("Debe indicar el producto.");
        }

        if (request.getCantidad() == null || request.getCantidad() <= 0) {
            throw new BadRequestException("Cantidad inválida.");
        }

        Pedido pedido = pedidoRepo.findById(request.getIdPedido())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado."));

        if ("PAGADO".equalsIgnoreCase(pedido.getEstado())) {
            throw new BadRequestException("No se puede modificar un pedido ya pagado.");
        }

        if ("CANCELADO".equalsIgnoreCase(pedido.getEstado())) {
            throw new BadRequestException("No se puede modificar un pedido cancelado.");
        }

        Producto producto = productoRepo.findById(request.getIdProducto())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado."));

        if (producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("El producto no tiene un precio válido.");
        }

        if (producto.getStock() == null || producto.getStock() <= 0) {
            throw new BadRequestException("El producto no tiene stock disponible.");
        }

        if (request.getCantidad() > producto.getStock()) {
            throw new BadRequestException("Stock insuficiente. Disponible: " + producto.getStock());
        }

        BigDecimal precio = producto.getPrecio();
        BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(request.getCantidad()));

        DetallePedido detalle = new DetallePedido();
        detalle.setPedido(pedido);
        detalle.setProducto(producto);
        detalle.setCantidad(request.getCantidad());
        detalle.setPrecioUnitario(precio);
        detalle.setSubtotal(subtotal);

        detalleRepo.save(detalle);

        if (!"PAGADO".equalsIgnoreCase(pedido.getEstado())) {
            String estadoAnterior = pedido.getEstado();

            pedido.setEstado("PENDIENTE_PAGO");
            pedidoRepo.save(pedido);

            auditoriaService.registrar(
                    idUsuario,
                    "PEDIDO",
                    "UPDATE",
                    "PEDIDOS",
                    "ACTUALIZAR_ESTADO_PEDIDO",
                    "Estado del pedido actualizado",
                    "Pedido ID: " + pedido.getIdPedido()
                            + ", Estado anterior: " + estadoAnterior
                            + ", Estado nuevo: PENDIENTE_PAGO",
                    null
            );
        }

        producto.setStock(producto.getStock() - request.getCantidad());
        productoRepo.save(producto);

        recalcularTotalPedido(pedido);

        auditoriaService.registrar(
                idUsuario,
                "DETALLE_PEDIDO",
                "INSERT",
                "PEDIDOS",
                "AGREGAR_PRODUCTO",
                "Producto agregado al pedido",
                "Pedido: " + pedido.getIdPedido()
                        + ", Producto: " + producto.getNombre()
                        + ", Cantidad: " + request.getCantidad(),
                null
        );

        auditoriaService.registrar(
                idUsuario,
                "PRODUCTO",
                "UPDATE",
                "STOCK",
                "DESCUENTO",
                "Stock descontado",
                "Producto ID: " + producto.getIdProducto()
                        + ", Cantidad: " + request.getCantidad(),
                null
        );

        return toResponse(detalle);
    }

    @Transactional(readOnly = true)
    public List<DetallePedidoResponse> listarPorPedido(Integer idPedido) {
        Pedido pedido = pedidoRepo.findById(idPedido)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado."));

        List<DetallePedido> lista = detalleRepo.findByPedido_IdPedido(pedido.getIdPedido());
        List<DetallePedidoResponse> response = new ArrayList<>();

        for (DetallePedido d : lista) {
            response.add(toResponse(d));
        }

        return response;
    }

    @Transactional(readOnly = true)
    public boolean existeDetalleEnPedido(Integer idPedido) {
        return !detalleRepo.findByPedido_IdPedido(idPedido).isEmpty();
    }

    private void recalcularTotalPedido(Pedido pedido) {
        List<DetallePedido> detalles = detalleRepo.findByPedido_IdPedido(pedido.getIdPedido());

        BigDecimal total = BigDecimal.ZERO;

        for (DetallePedido d : detalles) {
            if (d.getSubtotal() != null) {
                total = total.add(d.getSubtotal());
            }
        }

        pedido.setTotal(total);
        pedidoRepo.save(pedido);
    }

    private DetallePedidoResponse toResponse(DetallePedido d) {
        DetallePedidoResponse r = new DetallePedidoResponse();
        r.setIdDetalle(d.getIdDetalle());
        r.setIdProducto(d.getProducto().getIdProducto());
        r.setNombreProducto(d.getProducto().getNombre());
        r.setCantidad(d.getCantidad());
        r.setPrecioUnitario(d.getPrecioUnitario());
        r.setSubtotal(d.getSubtotal());
        return r;
    }
}
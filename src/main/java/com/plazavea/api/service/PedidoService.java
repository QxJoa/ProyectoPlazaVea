package com.plazavea.api.service;

import com.plazavea.api.dto.pedido.PedidoEstadoRequest;
import com.plazavea.api.dto.pedido.PedidoRequest;
import com.plazavea.api.dto.pedido.PedidoResponse;
import com.plazavea.api.dto.pedido.PedidoResumenResponse;
import com.plazavea.api.exception.BadRequestException;
import com.plazavea.api.exception.ResourceNotFoundException;
import com.plazavea.api.model.DetallePedido;
import com.plazavea.api.model.Pedido;
import com.plazavea.api.model.Producto;
import com.plazavea.api.model.Usuario;
import com.plazavea.api.repository.DetallePedidoRepository;
import com.plazavea.api.repository.PedidoRepository;
import com.plazavea.api.repository.ProductoRepository;
import com.plazavea.api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final ProductoRepository productoRepository;
    private final AuditoriaService auditoriaService;

    public PedidoService(PedidoRepository pedidoRepository,
                         UsuarioRepository usuarioRepository,
                         DetallePedidoRepository detallePedidoRepository,
                         ProductoRepository productoRepository,
                         AuditoriaService auditoriaService) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.productoRepository = productoRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPedidos() {
        List<Pedido> pedidos = pedidoRepository.findAllByOrderByIdPedidoDesc();
        List<PedidoResponse> response = new ArrayList<>();

        for (Pedido pedido : pedidos) {
            response.add(toResponse(pedido));
        }

        return response;
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPedidosPorUsuario(Integer idUsuario) {
        usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        List<Pedido> pedidos = pedidoRepository.findByUsuario_IdUsuarioOrderByIdPedidoDesc(idUsuario);
        List<PedidoResponse> response = new ArrayList<>();

        for (Pedido pedido : pedidos) {
            response.add(toResponse(pedido));
        }

        return response;
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtenerPedidoPorId(Integer idPedido) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado."));

        return toResponse(pedido);
    }

    @Transactional(readOnly = true)
    public PedidoResumenResponse obtenerResumenPedido(Integer idPedido) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado."));

        List<DetallePedido> detalles = detallePedidoRepository.findByPedido_IdPedido(idPedido);

        PedidoResumenResponse response = new PedidoResumenResponse();
        response.setIdPedido(pedido.getIdPedido());
        response.setEstado(pedido.getEstado());
        response.setCantidadProductos(detalles.size());
        response.setTotal(pedido.getTotal() != null ? pedido.getTotal() : BigDecimal.ZERO);

        return response;
    }

    @Transactional
    public PedidoResponse crearPedido(Integer idUsuarioSolicitante,
                                      PedidoRequest request,
                                      String ipOrigen) {
        if (request == null) {
            throw new BadRequestException("Debe enviar los datos del pedido.");
        }

        if (request.getIdUsuario() == null) {
            throw new BadRequestException("Debe indicar el idUsuario del pedido.");
        }

        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);

        if (request.getTotal() == null) {
            pedido.setTotal(BigDecimal.ZERO);
        } else {
            if (request.getTotal().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("El total no puede ser negativo.");
            }
            pedido.setTotal(request.getTotal());
        }

        if (request.getEstado() == null || request.getEstado().trim().isEmpty()) {
            pedido.setEstado("CREADO");
        } else {
            pedido.setEstado(request.getEstado().trim().toUpperCase());
        }

        Pedido guardado = pedidoRepository.save(pedido);

        auditoriaService.registrar(
                idUsuarioSolicitante,
                "PEDIDO",
                "INSERT",
                "PEDIDOS",
                "CREAR_PEDIDO",
                "Pedido creado correctamente.",
                "ID pedido: " + guardado.getIdPedido()
                        + ", ID usuario: " + guardado.getUsuario().getIdUsuario()
                        + ", Estado: " + guardado.getEstado()
                        + ", Total: " + guardado.getTotal(),
                ipOrigen
        );

        return toResponse(guardado);
    }

    @Transactional
    public PedidoResponse actualizarEstado(Integer idUsuarioSolicitante,
                                           Integer idPedido,
                                           PedidoEstadoRequest request,
                                           String ipOrigen) {
        if (request == null || request.getEstado() == null || request.getEstado().trim().isEmpty()) {
            throw new BadRequestException("Debe indicar el nuevo estado del pedido.");
        }

        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado."));

        String estadoAnterior = pedido.getEstado();
        String nuevoEstado = request.getEstado().trim().toUpperCase();

        if ("PAGADO".equalsIgnoreCase(estadoAnterior) && !"PAGADO".equalsIgnoreCase(nuevoEstado)) {
            throw new BadRequestException("No se puede cambiar manualmente el estado de un pedido ya pagado.");
        }

        if ("CANCELADO".equalsIgnoreCase(estadoAnterior)) {
            throw new BadRequestException("No se puede modificar el estado de un pedido cancelado.");
        }

        pedido.setEstado(nuevoEstado);

        Pedido actualizado = pedidoRepository.save(pedido);

        auditoriaService.registrar(
                idUsuarioSolicitante,
                "PEDIDO",
                "UPDATE",
                "PEDIDOS",
                "ACTUALIZAR_ESTADO_PEDIDO",
                "Estado del pedido actualizado correctamente.",
                "ID pedido: " + actualizado.getIdPedido()
                        + ", Estado anterior: " + estadoAnterior
                        + ", Estado nuevo: " + actualizado.getEstado(),
                ipOrigen
        );

        return toResponse(actualizado);
    }

    @Transactional
    public PedidoResponse cancelarPedido(Integer idUsuarioSolicitante,
                                         Integer idPedido,
                                         String ipOrigen) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado."));

        String estadoActual = pedido.getEstado() == null ? "" : pedido.getEstado().trim().toUpperCase();

        if ("CANCELADO".equals(estadoActual)) {
            throw new BadRequestException("El pedido ya está cancelado.");
        }

        if ("PAGADO".equals(estadoActual)) {
            throw new BadRequestException("No se puede cancelar un pedido pagado.");
        }

        List<DetallePedido> detalles = detallePedidoRepository.findByPedido_IdPedido(pedido.getIdPedido());

        for (DetallePedido detalle : detalles) {
            if (detalle.getProducto() == null || detalle.getProducto().getIdProducto() == null) {
                continue;
            }

            Producto producto = productoRepository.findById(detalle.getProducto().getIdProducto())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado al devolver stock."));

            int stockActual = producto.getStock() == null ? 0 : producto.getStock();
            int cantidadDevuelta = detalle.getCantidad() == null ? 0 : detalle.getCantidad();

            producto.setStock(stockActual + cantidadDevuelta);
            productoRepository.save(producto);

            auditoriaService.registrar(
                    idUsuarioSolicitante,
                    "PRODUCTO",
                    "UPDATE",
                    "STOCK",
                    "DEVOLUCION_STOCK_CANCELACION",
                    "Stock devuelto por cancelación de pedido",
                    "Pedido ID: " + pedido.getIdPedido()
                            + ", Producto ID: " + producto.getIdProducto()
                            + ", Cantidad devuelta: " + cantidadDevuelta
                            + ", Stock nuevo: " + producto.getStock(),
                    ipOrigen
            );
        }

        String estadoAnterior = pedido.getEstado();
        pedido.setEstado("CANCELADO");
        Pedido cancelado = pedidoRepository.save(pedido);

        auditoriaService.registrar(
                idUsuarioSolicitante,
                "PEDIDO",
                "UPDATE",
                "PEDIDOS",
                "CANCELAR_PEDIDO",
                "Pedido cancelado correctamente",
                "Pedido ID: " + cancelado.getIdPedido()
                        + ", Estado anterior: " + estadoAnterior
                        + ", Estado nuevo: CANCELADO"
                        + ", Detalles afectados: " + detalles.size(),
                ipOrigen
        );

        return toResponse(cancelado);
    }

    private PedidoResponse toResponse(Pedido pedido) {
        PedidoResponse dto = new PedidoResponse();
        dto.setIdPedido(pedido.getIdPedido());

        if (pedido.getUsuario() != null) {
            dto.setIdUsuario(pedido.getUsuario().getIdUsuario());
            dto.setNombresUsuario(pedido.getUsuario().getNombres());
            dto.setApellidosUsuario(pedido.getUsuario().getApellidos());
        }

        dto.setFecha(pedido.getFecha() != null ? pedido.getFecha().toString() : null);
        dto.setTotal(pedido.getTotal());
        dto.setEstado(pedido.getEstado());

        return dto;
    }
}
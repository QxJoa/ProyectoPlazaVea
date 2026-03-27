package com.plazavea.api.controller;

import com.plazavea.api.dto.pedido.PedidoEstadoRequest;
import com.plazavea.api.dto.pedido.PedidoRequest;
import com.plazavea.api.dto.pedido.PedidoResponse;
import com.plazavea.api.dto.pedido.PedidoResumenResponse;
import com.plazavea.api.service.PedidoService;
import com.plazavea.api.service.PermisoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final PermisoService permisoService;

    public PedidoController(PedidoService pedidoService,
                            PermisoService permisoService) {
        this.pedidoService = pedidoService;
        this.permisoService = permisoService;
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listarPedidos(@RequestParam Integer idUsuarioSolicitante) {
        permisoService.validarPermiso(idUsuarioSolicitante, "PEDIDOS", "LISTAR");
        List<PedidoResponse> pedidos = pedidoService.listarPedidos();
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> obtenerPedidoPorId(@PathVariable Integer id,
                                                             @RequestParam Integer idUsuarioSolicitante) {
        permisoService.validarPermiso(idUsuarioSolicitante, "PEDIDOS", "CONSULTAR");
        PedidoResponse response = pedidoService.obtenerPedidoPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<PedidoResponse>> listarPedidosPorUsuario(@PathVariable Integer idUsuario,
                                                                        @RequestParam Integer idUsuarioSolicitante) {
        permisoService.validarPermiso(idUsuarioSolicitante, "PEDIDOS", "CONSULTAR");
        List<PedidoResponse> pedidos = pedidoService.listarPedidosPorUsuario(idUsuario);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/resumen/{idPedido}")
    public ResponseEntity<PedidoResumenResponse> obtenerResumen(@PathVariable Integer idPedido,
                                                                @RequestParam Integer idUsuarioSolicitante) {
        permisoService.validarPermiso(idUsuarioSolicitante, "PEDIDOS", "CONSULTAR");
        PedidoResumenResponse response = pedidoService.obtenerResumenPedido(idPedido);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> crearPedido(@RequestParam Integer idUsuarioSolicitante,
                                                           @RequestBody PedidoRequest request,
                                                           HttpServletRequest httpRequest) {
        permisoService.validarPermiso(idUsuarioSolicitante, "PEDIDOS", "CREAR");

        String ip = obtenerIpCliente(httpRequest);
        PedidoResponse response = pedidoService.crearPedido(idUsuarioSolicitante, request, ip);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mensaje", "Pedido creado correctamente.");
        body.put("data", response);

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Map<String, Object>> actualizarEstado(@PathVariable Integer id,
                                                                @RequestParam Integer idUsuarioSolicitante,
                                                                @RequestBody PedidoEstadoRequest request,
                                                                HttpServletRequest httpRequest) {
        permisoService.validarPermiso(idUsuarioSolicitante, "PEDIDOS", "ACTUALIZAR");

        String ip = obtenerIpCliente(httpRequest);
        PedidoResponse response = pedidoService.actualizarEstado(idUsuarioSolicitante, id, request, ip);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mensaje", "Estado del pedido actualizado correctamente.");
        body.put("data", response);

        return ResponseEntity.ok(body);
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Map<String, Object>> cancelarPedido(@PathVariable Integer id,
                                                              @RequestParam Integer idUsuarioSolicitante,
                                                              HttpServletRequest httpRequest) {
        permisoService.validarPermiso(idUsuarioSolicitante, "PEDIDOS", "ACTUALIZAR");

        String ip = obtenerIpCliente(httpRequest);
        PedidoResponse response = pedidoService.cancelarPedido(idUsuarioSolicitante, id, ip);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mensaje", "Pedido cancelado correctamente.");
        body.put("data", response);

        return ResponseEntity.ok(body);
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else if (ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
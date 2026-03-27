package com.plazavea.api.controller;

import com.plazavea.api.dto.detalle.DetallePedidoRequest;
import com.plazavea.api.dto.detalle.DetallePedidoResponse;
import com.plazavea.api.service.DetallePedidoService;
import com.plazavea.api.service.PermisoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/detalle-pedido")
public class DetallePedidoController {

    private final DetallePedidoService service;
    private final PermisoService permisoService;

    public DetallePedidoController(DetallePedidoService service,
                                   PermisoService permisoService) {
        this.service = service;
        this.permisoService = permisoService;
    }

    @PostMapping
    public ResponseEntity<DetallePedidoResponse> agregar(@RequestParam Integer idUsuarioSolicitante,
                                                         @RequestBody DetallePedidoRequest request) {
        permisoService.validarPermiso(idUsuarioSolicitante, "PEDIDOS", "CREAR");
        DetallePedidoResponse response = service.agregarProducto(idUsuarioSolicitante, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{idPedido}")
    public ResponseEntity<List<DetallePedidoResponse>> listarPorPedido(@PathVariable Integer idPedido,
                                                                       @RequestParam Integer idUsuarioSolicitante) {
        permisoService.validarPermiso(idUsuarioSolicitante, "PEDIDOS", "CONSULTAR");
        List<DetallePedidoResponse> response = service.listarPorPedido(idPedido);
        return ResponseEntity.ok(response);
    }
}
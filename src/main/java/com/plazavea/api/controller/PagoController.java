package com.plazavea.api.controller;

import com.plazavea.api.dto.pago.PagoRequest;
import com.plazavea.api.dto.pago.PagoResponse;
import com.plazavea.api.service.PagoService;
import com.plazavea.api.service.PermisoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService service;
    private final PermisoService permisoService;

    public PagoController(PagoService service,
                          PermisoService permisoService) {
        this.service = service;
        this.permisoService = permisoService;
    }

    @PostMapping
    public ResponseEntity<PagoResponse> pagar(@RequestParam Integer idUsuarioSolicitante,
                                              @RequestBody PagoRequest request) {
        permisoService.validarPermiso(idUsuarioSolicitante, "PAGOS", "CREAR");
        return ResponseEntity.ok(service.registrarPago(idUsuarioSolicitante, request));
    }

    @GetMapping
    public ResponseEntity<List<PagoResponse>> listarPagos(@RequestParam Integer idUsuarioSolicitante) {
        permisoService.validarPermiso(idUsuarioSolicitante, "PAGOS", "CONSULTAR");
        return ResponseEntity.ok(service.listarPagos());
    }

    @GetMapping("/{idPago}")
    public ResponseEntity<PagoResponse> obtenerPagoPorId(@PathVariable Integer idPago,
                                                         @RequestParam Integer idUsuarioSolicitante) {
        permisoService.validarPermiso(idUsuarioSolicitante, "PAGOS", "CONSULTAR");
        return ResponseEntity.ok(service.obtenerPagoPorId(idPago));
    }

    @GetMapping("/pedido/{idPedido}")
    public ResponseEntity<List<PagoResponse>> listarPagosPorPedido(@PathVariable Integer idPedido,
                                                                   @RequestParam Integer idUsuarioSolicitante) {
        permisoService.validarPermiso(idUsuarioSolicitante, "PAGOS", "CONSULTAR");
        return ResponseEntity.ok(service.listarPagosPorPedido(idPedido));
    }
}
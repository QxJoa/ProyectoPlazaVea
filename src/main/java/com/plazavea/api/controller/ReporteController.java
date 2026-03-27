package com.plazavea.api.controller;

import com.plazavea.api.dto.reporte.ReporteAuditoriaResponse;
import com.plazavea.api.dto.reporte.ReportePagoResponse;
import com.plazavea.api.dto.reporte.ReportePedidoResponse;
import com.plazavea.api.dto.reporte.ReporteProductoStockResponse;
import com.plazavea.api.service.PermisoService;
import com.plazavea.api.service.ReporteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;
    private final PermisoService permisoService;

    public ReporteController(ReporteService reporteService,
                             PermisoService permisoService) {
        this.reporteService = reporteService;
        this.permisoService = permisoService;
    }

    @GetMapping("/pedidos")
    public ResponseEntity<List<ReportePedidoResponse>> reportePedidos(@RequestParam Integer idUsuarioSolicitante) {
        permisoService.validarPermiso(idUsuarioSolicitante, "PEDIDOS", "CONSULTAR");
        return ResponseEntity.ok(reporteService.reportePedidos());
    }

    @GetMapping("/pagos")
    public ResponseEntity<List<ReportePagoResponse>> reportePagos(@RequestParam Integer idUsuarioSolicitante) {
        permisoService.validarPermiso(idUsuarioSolicitante, "PAGOS", "CONSULTAR");
        return ResponseEntity.ok(reporteService.reportePagos());
    }

    @GetMapping("/productos-stock")
    public ResponseEntity<List<ReporteProductoStockResponse>> reporteProductosConStock(@RequestParam Integer idUsuarioSolicitante) {
        permisoService.validarPermiso(idUsuarioSolicitante, "PRODUCTOS", "CONSULTAR");
        return ResponseEntity.ok(reporteService.reporteProductosConStock());
    }

    @GetMapping("/auditoria")
    public ResponseEntity<List<ReporteAuditoriaResponse>> reporteAuditoria(@RequestParam Integer idUsuarioSolicitante) {
        permisoService.validarPermiso(idUsuarioSolicitante, "AUDITORIA", "CONSULTAR");
        return ResponseEntity.ok(reporteService.reporteAuditoria());
    }
}
package com.plazavea.api.controller;

import com.plazavea.api.dto.auditoria.AuditoriaResponse;
import com.plazavea.api.service.AuditoriaService;
import com.plazavea.api.service.PermisoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;
    private final PermisoService permisoService;

    public AuditoriaController(AuditoriaService auditoriaService,
                               PermisoService permisoService) {
        this.auditoriaService = auditoriaService;
        this.permisoService = permisoService;
    }

    @GetMapping
    public ResponseEntity<?> listarAuditoria(@RequestParam Integer idUsuarioSolicitante) {
        try {
            permisoService.validarPermiso(idUsuarioSolicitante, "AUDITORIA", "CONSULTAR");

            List<AuditoriaResponse> lista = auditoriaService.listarAuditoria();
            return ResponseEntity.ok(lista);

        } catch (RuntimeException e) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
        }
    }
}
package com.plazavea.api.controller;

import com.plazavea.api.dto.rol.AsignarRolRequest;
import com.plazavea.api.dto.rol.RolResponse;
import com.plazavea.api.dto.rol.UsuarioRolResponse;
import com.plazavea.api.dto.rol.ValidacionPermisoResponse;
import com.plazavea.api.service.PermisoService;
import com.plazavea.api.service.RolService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/roles")
public class RolController {

    private final RolService rolService;
    private final PermisoService permisoService;

    public RolController(RolService rolService, PermisoService permisoService) {
        this.rolService = rolService;
        this.permisoService = permisoService;
    }

    @GetMapping
    public ResponseEntity<?> listarRoles(@RequestParam Integer idUsuarioSolicitante) {
        try {
            permisoService.validarPermiso(idUsuarioSolicitante, "ROLES", "LISTAR");
            List<RolResponse> roles = rolService.listarRoles();
            return ResponseEntity.ok(roles);
        } catch (RuntimeException e) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
        }
    }

    @PostMapping("/asignar")
    public ResponseEntity<?> asignarRol(@RequestBody AsignarRolRequest request) {
        try {
            String mensaje = rolService.asignarRol(request);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("mensaje", mensaje);
            return ResponseEntity.ok(body);
        } catch (RuntimeException e) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("mensaje", e.getMessage());
            return ResponseEntity.badRequest().body(body);
        }
    }

    @GetMapping("/usuario/{idUsuarioObjetivo}")
    public ResponseEntity<?> listarRolesDeUsuario(@PathVariable Integer idUsuarioObjetivo,
                                                  @RequestParam Integer idUsuarioSolicitante) {
        try {
            List<UsuarioRolResponse> response =
                    rolService.listarRolesDeUsuario(idUsuarioSolicitante, idUsuarioObjetivo);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
        }
    }

    @GetMapping("/validar-permiso")
    public ResponseEntity<ValidacionPermisoResponse> validarPermiso(@RequestParam Integer idUsuario,
                                                                    @RequestParam String recurso,
                                                                    @RequestParam String accion) {
        return ResponseEntity.ok(
                permisoService.validarPermisoConRespuesta(idUsuario, recurso, accion)
        );
    }
}
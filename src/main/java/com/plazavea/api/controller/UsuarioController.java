package com.plazavea.api.controller;

import com.plazavea.api.dto.usuario.CreateUsuarioRequest;
import com.plazavea.api.dto.usuario.UpdateUsuarioRequest;
import com.plazavea.api.dto.usuario.UsuarioResponse;
import com.plazavea.api.service.PermisoService;
import com.plazavea.api.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PermisoService permisoService;

    public UsuarioController(UsuarioService usuarioService, PermisoService permisoService) {
        this.usuarioService = usuarioService;
        this.permisoService = permisoService;
    }

    @PostMapping
    public ResponseEntity<?> crearUsuario(@RequestParam Integer idUsuarioSolicitante,
                                          @RequestBody CreateUsuarioRequest request) {
        try {
            permisoService.validarPermiso(idUsuarioSolicitante, "USUARIOS", "CREAR");

            UsuarioResponse response = usuarioService.crearUsuario(idUsuarioSolicitante, request);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("mensaje", "Usuario creado correctamente.");
            body.put("data", response);

            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } catch (RuntimeException e) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("mensaje", e.getMessage());

            if ("No tiene permisos para realizar esta acción.".equals(e.getMessage())
                    || "El usuario solicitante está inactivo.".equals(e.getMessage())
                    || "El usuario no tiene roles asignados.".equals(e.getMessage())
                    || "Debe indicar el idUsuarioSolicitante.".equals(e.getMessage())
                    || "El usuario solicitante no existe.".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
            }

            return ResponseEntity.badRequest().body(body);
        }
    }

    @GetMapping
    public ResponseEntity<?> listarUsuarios(@RequestParam Integer idUsuarioSolicitante) {
        try {
            permisoService.validarPermiso(idUsuarioSolicitante, "USUARIOS", "LISTAR");

            List<UsuarioResponse> usuarios = usuarioService.listarUsuarios();
            return ResponseEntity.ok(usuarios);
        } catch (RuntimeException e) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerUsuarioPorId(@PathVariable Integer id,
                                                 @RequestParam Integer idUsuarioSolicitante) {
        try {
            permisoService.validarPermiso(idUsuarioSolicitante, "USUARIOS", "CONSULTAR");

            UsuarioResponse response = usuarioService.obtenerUsuarioPorId(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("mensaje", e.getMessage());

            if ("Usuario no encontrado.".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
            }

            if ("No tiene permisos para realizar esta acción.".equals(e.getMessage())
                    || "El usuario solicitante está inactivo.".equals(e.getMessage())
                    || "El usuario no tiene roles asignados.".equals(e.getMessage())
                    || "Debe indicar el idUsuarioSolicitante.".equals(e.getMessage())
                    || "El usuario solicitante no existe.".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
            }

            return ResponseEntity.badRequest().body(body);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Integer id,
                                               @RequestParam Integer idUsuarioSolicitante,
                                               @RequestBody UpdateUsuarioRequest request) {
        try {
            permisoService.validarPermiso(idUsuarioSolicitante, "USUARIOS", "ACTUALIZAR");

            UsuarioResponse response = usuarioService.actualizarUsuario(idUsuarioSolicitante, id, request);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("mensaje", "Usuario actualizado correctamente.");
            body.put("data", response);

            return ResponseEntity.ok(body);
        } catch (RuntimeException e) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("mensaje", e.getMessage());

            if ("Usuario no encontrado.".equals(e.getMessage())
                    || "Credencial no encontrada para el usuario.".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
            }

            if ("No tiene permisos para realizar esta acción.".equals(e.getMessage())
                    || "El usuario solicitante está inactivo.".equals(e.getMessage())
                    || "El usuario no tiene roles asignados.".equals(e.getMessage())
                    || "Debe indicar el idUsuarioSolicitante.".equals(e.getMessage())
                    || "El usuario solicitante no existe.".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
            }

            return ResponseEntity.badRequest().body(body);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> desactivarUsuario(@PathVariable Integer id,
                                               @RequestParam Integer idUsuarioSolicitante) {
        try {
            permisoService.validarPermiso(idUsuarioSolicitante, "USUARIOS", "ELIMINAR");

            usuarioService.desactivarUsuario(idUsuarioSolicitante, id);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("mensaje", "Usuario desactivado correctamente.");

            return ResponseEntity.ok(body);
        } catch (RuntimeException e) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("mensaje", e.getMessage());

            if ("Usuario no encontrado.".equals(e.getMessage())
                    || "Credencial no encontrada para el usuario.".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
            }

            if ("No tiene permisos para realizar esta acción.".equals(e.getMessage())
                    || "El usuario solicitante está inactivo.".equals(e.getMessage())
                    || "El usuario no tiene roles asignados.".equals(e.getMessage())
                    || "Debe indicar el idUsuarioSolicitante.".equals(e.getMessage())
                    || "El usuario solicitante no existe.".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
            }

            return ResponseEntity.badRequest().body(body);
        }
    }
}
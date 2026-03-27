package com.plazavea.api.service;

import com.plazavea.api.dto.rol.ValidacionPermisoResponse;
import com.plazavea.api.exception.ForbiddenException;
import com.plazavea.api.model.Usuario;
import com.plazavea.api.model.UsuarioRol;
import com.plazavea.api.repository.UsuarioRepository;
import com.plazavea.api.repository.UsuarioRolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PermisoService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;

    public PermisoService(UsuarioRepository usuarioRepository,
                          UsuarioRolRepository usuarioRolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioRolRepository = usuarioRolRepository;
    }

    @Transactional(readOnly = true)
    public List<String> obtenerRoles(Integer idUsuario) {
        List<UsuarioRol> asignaciones = usuarioRolRepository.findByUsuario_IdUsuario(idUsuario);
        List<String> roles = new ArrayList<>();

        for (UsuarioRol item : asignaciones) {
            if (item.getRol() != null && item.getRol().getNombreRol() != null) {
                roles.add(item.getRol().getNombreRol().toUpperCase());
            }
        }

        return roles;
    }

    @Transactional(readOnly = true)
    public boolean tieneRol(Integer idUsuario, String nombreRol) {
        return usuarioRolRepository.existsByUsuario_IdUsuarioAndRol_NombreRolIgnoreCase(idUsuario, nombreRol);
    }

    @Transactional(readOnly = true)
    public void validarPermiso(Integer idUsuario, String recurso, String accion) {
        if (idUsuario == null) {
            throw new ForbiddenException("Debe indicar el idUsuarioSolicitante.");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ForbiddenException("El usuario solicitante no existe."));

        if (usuario.getEstado() == null || !usuario.getEstado().equalsIgnoreCase("ACTIVO")) {
            throw new ForbiddenException("El usuario solicitante está inactivo.");
        }

        List<String> roles = obtenerRoles(idUsuario);

        if (roles.isEmpty()) {
            throw new ForbiddenException("El usuario no tiene roles asignados.");
        }

        if (autorizado(roles, recurso, accion)) {
            return;
        }

        throw new ForbiddenException("No tiene permisos para realizar esta acción.");
    }

    @Transactional(readOnly = true)
    public ValidacionPermisoResponse validarPermisoConRespuesta(Integer idUsuario, String recurso, String accion) {
        ValidacionPermisoResponse response = new ValidacionPermisoResponse();
        response.setIdUsuario(idUsuario);
        response.setRecurso(recurso);
        response.setAccion(accion);

        try {
            List<String> roles = obtenerRoles(idUsuario);
            boolean autorizado = autorizado(roles, recurso, accion);

            response.setRoles(roles);
            response.setAutorizado(autorizado);
            response.setMensaje(autorizado ? "Permiso concedido." : "Permiso denegado.");
        } catch (Exception e) {
            response.setAutorizado(false);
            response.setMensaje(e.getMessage());
        }

        return response;
    }

    private boolean autorizado(List<String> roles, String recurso, String accion) {
        String recursoNormalizado = recurso == null ? "" : recurso.trim().toUpperCase();
        String accionNormalizada = accion == null ? "" : accion.trim().toUpperCase();

        for (String rol : roles) {

            if ("ADMIN".equals(rol)) {
                if (recursoNormalizado.equals("USUARIOS")) return true;
                if (recursoNormalizado.equals("PRODUCTOS")) return true;
                if (recursoNormalizado.equals("PEDIDOS")) return true;
                if (recursoNormalizado.equals("ROLES")) return true;
                if (recursoNormalizado.equals("AUDITORIA")) return true;
                if (recursoNormalizado.equals("PAGOS")) return true;
            }

            if ("SUPERVISOR".equals(rol)) {
                if (recursoNormalizado.equals("PRODUCTOS")) return true;
                if (recursoNormalizado.equals("PEDIDOS")) return true;
            }

            if ("CAJERO".equals(rol)) {
                if (recursoNormalizado.equals("PRODUCTOS")
                        && (accionNormalizada.equals("VER")
                        || accionNormalizada.equals("LISTAR")
                        || accionNormalizada.equals("CONSULTAR"))) {
                    return true;
                }

                if (recursoNormalizado.equals("PAGOS")) {
                    return true;
                }

                if (recursoNormalizado.equals("PEDIDOS")) {
                    return true;
                }
            }

            if ("ALMACEN".equals(rol)) {
                if (recursoNormalizado.equals("PRODUCTOS")) {
                    return true;
                }
            }

            if ("CLIENTE".equals(rol)) {
                if (recursoNormalizado.equals("PRODUCTOS")
                        && (accionNormalizada.equals("VER")
                        || accionNormalizada.equals("LISTAR")
                        || accionNormalizada.equals("CONSULTAR"))) {
                    return true;
                }
            }

            if ("AUDITOR".equals(rol)) {
                if (recursoNormalizado.equals("AUDITORIA")
                        && (accionNormalizada.equals("CONSULTAR")
                        || accionNormalizada.equals("LISTAR"))) {
                    return true;
                }
            }
        }

        return false;
    }
}
package com.plazavea.api.service;

import com.plazavea.api.dto.rol.AsignarRolRequest;
import com.plazavea.api.dto.rol.RolResponse;
import com.plazavea.api.dto.rol.UsuarioRolResponse;
import com.plazavea.api.model.Rol;
import com.plazavea.api.model.Usuario;
import com.plazavea.api.model.UsuarioRol;
import com.plazavea.api.model.UsuarioRolId;
import com.plazavea.api.repository.RolRepository;
import com.plazavea.api.repository.UsuarioRepository;
import com.plazavea.api.repository.UsuarioRolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RolService {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final PermisoService permisoService;
    private final AuditoriaService auditoriaService;

    public RolService(RolRepository rolRepository,
                      UsuarioRepository usuarioRepository,
                      UsuarioRolRepository usuarioRolRepository,
                      PermisoService permisoService,
                      AuditoriaService auditoriaService) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.permisoService = permisoService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<RolResponse> listarRoles() {
        List<Rol> roles = rolRepository.findAll();
        List<RolResponse> response = new ArrayList<>();

        for (Rol rol : roles) {
            RolResponse item = new RolResponse();
            item.setIdRol(rol.getIdRol());
            item.setNombreRol(rol.getNombreRol());
            item.setDescripcion(rol.getDescripcion());
            response.add(item);
        }

        return response;
    }

    @Transactional
    public String asignarRol(AsignarRolRequest request) {
        if (request == null) {
            throw new RuntimeException("La solicitud es obligatoria.");
        }

        if (request.getIdUsuarioSolicitante() == null) {
            throw new RuntimeException("Debe indicar idUsuarioSolicitante.");
        }

        if (request.getIdUsuarioObjetivo() == null) {
            throw new RuntimeException("Debe indicar idUsuarioObjetivo.");
        }

        if (request.getIdRol() == null) {
            throw new RuntimeException("Debe indicar idRol.");
        }

        permisoService.validarPermiso(request.getIdUsuarioSolicitante(), "ROLES", "ASIGNAR");

        Usuario usuarioObjetivo = usuarioRepository.findById(request.getIdUsuarioObjetivo())
                .orElseThrow(() -> new RuntimeException("El usuario objetivo no existe."));

        Rol rol = rolRepository.findById(request.getIdRol())
                .orElseThrow(() -> new RuntimeException("El rol no existe."));

        boolean yaExiste = usuarioRolRepository.existsByUsuario_IdUsuarioAndRol_IdRol(
                usuarioObjetivo.getIdUsuario(), rol.getIdRol()
        );

        if (yaExiste) {
            throw new RuntimeException("El usuario ya tiene ese rol asignado.");
        }

        UsuarioRol usuarioRol = new UsuarioRol();
        usuarioRol.setId(new UsuarioRolId(usuarioObjetivo.getIdUsuario(), rol.getIdRol()));
        usuarioRol.setUsuario(usuarioObjetivo);
        usuarioRol.setRol(rol);

        usuarioRolRepository.save(usuarioRol);

        auditoriaService.registrar(
                request.getIdUsuarioSolicitante(),
                "USUARIO_ROL",
                "INSERT",
                "ROLES",
                "ASIGNAR_ROL",
                "Se asignó el rol correctamente.",
                "Usuario objetivo ID: " + usuarioObjetivo.getIdUsuario()
                        + ", Rol ID: " + rol.getIdRol()
                        + ", Nombre rol: " + rol.getNombreRol(),
                null
        );

        return "Rol asignado correctamente.";
    }

    @Transactional(readOnly = true)
    public List<UsuarioRolResponse> listarRolesDeUsuario(Integer idUsuarioSolicitante, Integer idUsuarioObjetivo) {
        if (idUsuarioSolicitante == null) {
            throw new RuntimeException("Debe indicar idUsuarioSolicitante.");
        }

        if (idUsuarioObjetivo == null) {
            throw new RuntimeException("Debe indicar idUsuarioObjetivo.");
        }

    boolean consultaPropia = idUsuarioSolicitante.equals(idUsuarioObjetivo);

        if (!consultaPropia) {
            permisoService.validarPermiso(idUsuarioSolicitante, "ROLES", "CONSULTAR");
    }

    Usuario usuario = usuarioRepository.findById(idUsuarioObjetivo)
            .orElseThrow(() -> new RuntimeException("El usuario objetivo no existe."));

    List<UsuarioRol> asignaciones = usuarioRolRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());
    List<UsuarioRolResponse> response = new ArrayList<>();

    for (UsuarioRol item : asignaciones) {
        UsuarioRolResponse dto = new UsuarioRolResponse();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setNombresUsuario(usuario.getNombres() + " " + usuario.getApellidos());
        dto.setIdRol(item.getRol().getIdRol());
        dto.setNombreRol(item.getRol().getNombreRol());
        response.add(dto);
    }

    return response;
}
}
package com.plazavea.api.service;

import com.plazavea.api.dto.auditoria.AuditoriaResponse;
import com.plazavea.api.model.Auditoria;
import com.plazavea.api.model.Usuario;
import com.plazavea.api.model.Credencial;
import com.plazavea.api.repository.AuditoriaRepository;
import com.plazavea.api.repository.CredencialRepository;
import com.plazavea.api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CredencialRepository credencialRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository,
                            UsuarioRepository usuarioRepository,
                            CredencialRepository credencialRepository) {
        this.auditoriaRepository = auditoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.credencialRepository = credencialRepository;
    }

    @Transactional
    public void registrar(Integer idUsuarioResponsable,
                          String tablaAfectada,
                          String operacion,
                          String modulo,
                          String accion,
                          String descripcion) {
        registrar(idUsuarioResponsable, tablaAfectada, operacion, modulo, accion, descripcion, null, null);
    }

    @Transactional
    public void registrar(Integer idUsuarioResponsable,
                          String tablaAfectada,
                          String operacion,
                          String modulo,
                          String accion,
                          String descripcion,
                          String detalleAdicional,
                          String ipOrigen) {

        if (idUsuarioResponsable == null) {
            return;
        }

        Usuario usuario = usuarioRepository.findById(idUsuarioResponsable)
                .orElseThrow(() -> new RuntimeException("Usuario responsable de auditoría no encontrado."));

        Auditoria auditoria = new Auditoria();
        auditoria.setTablaAfectada(tablaAfectada);
        auditoria.setOperacion(operacion);
        auditoria.setUsuarioResponsable(usuario);
        auditoria.setModulo(modulo);
        auditoria.setAccion(accion);
        auditoria.setDescripcion(descripcion);
        auditoria.setDetalleAdicional(detalleAdicional);
        auditoria.setIpOrigen(ipOrigen);

        auditoriaRepository.save(auditoria);
    }

    @Transactional(readOnly = true)
    public List<AuditoriaResponse> listarAuditoria() {
        List<Auditoria> lista = auditoriaRepository.findAllByOrderByIdAuditoriaDesc();
        List<AuditoriaResponse> response = new ArrayList<>();

        for (Auditoria item : lista) {
            AuditoriaResponse dto = new AuditoriaResponse();
            dto.setIdAuditoria(item.getIdAuditoria());
            dto.setTablaAfectada(item.getTablaAfectada());
            dto.setOperacion(item.getOperacion());
            dto.setModulo(item.getModulo());
            dto.setAccion(item.getAccion());
            dto.setDescripcion(item.getDescripcion());
            dto.setDetalleAdicional(item.getDetalleAdicional());
            dto.setIpOrigen(item.getIpOrigen());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

                dto.setFechaHora(
                item.getFechaHora() != null
                ? item.getFechaHora().format(formatter)
                : null
);

            if (item.getUsuarioResponsable() != null) {
                Usuario usuario = item.getUsuarioResponsable();
                dto.setIdUsuarioResponsable(usuario.getIdUsuario());
                dto.setNombreResponsable(usuario.getNombres() + " " + usuario.getApellidos());

                Optional<Credencial> credencialOpt =
                        credencialRepository.findByUsuario_IdUsuario(usuario.getIdUsuario());

                dto.setUsernameResponsable(
                        credencialOpt.map(Credencial::getUsername).orElse("")
                );
            }

            response.add(dto);
        }

        return response;
    }
}
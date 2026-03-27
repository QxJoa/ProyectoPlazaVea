package com.plazavea.api.controller;

import com.plazavea.api.model.Producto;
import com.plazavea.api.repository.ProductoRepository;
import com.plazavea.api.service.AuditoriaService;
import com.plazavea.api.service.PermisoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoRepository productoRepository;
    private final PermisoService permisoService;
    private final AuditoriaService auditoriaService;

    public ProductoController(ProductoRepository productoRepository,
                              PermisoService permisoService,
                              AuditoriaService auditoriaService) {
        this.productoRepository = productoRepository;
        this.permisoService = permisoService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public ResponseEntity<?> listarProductos(@RequestParam Integer idUsuarioSolicitante) {
        try {
            permisoService.validarPermiso(idUsuarioSolicitante, "PRODUCTOS", "LISTAR");
            List<Producto> productos = productoRepository.findAll();
            return ResponseEntity.ok(productos);
        } catch (RuntimeException e) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable("id") int id,
                                         @RequestParam Integer idUsuarioSolicitante) {
        try {
            permisoService.validarPermiso(idUsuarioSolicitante, "PRODUCTOS", "CONSULTAR");

            return productoRepository.findById(id)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> {
                        Map<String, Object> body = new LinkedHashMap<>();
                        body.put("mensaje", "Producto no encontrado.");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
                    });

        } catch (RuntimeException e) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
        }
    }

    @PostMapping
    public ResponseEntity<?> guardarProducto(@RequestParam Integer idUsuarioSolicitante,
                                             @RequestBody Producto producto) {
        try {
            permisoService.validarPermiso(idUsuarioSolicitante, "PRODUCTOS", "CREAR");

            Producto productoGuardado = productoRepository.save(producto);

            auditoriaService.registrar(
                    idUsuarioSolicitante,
                    "PRODUCTO",
                    "INSERT",
                    "PRODUCTOS",
                    "CREAR_PRODUCTO",
                    "Producto creado correctamente.",
                    "ID producto: " + productoGuardado.getIdProducto()
                            + ", Nombre: " + productoGuardado.getNombre(),
                    null
            );

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("mensaje", "Producto creado correctamente.");
            body.put("data", productoGuardado);

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

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable("id") int id,
                                                @RequestParam Integer idUsuarioSolicitante,
                                                @RequestBody Producto productoActualizado) {
        try {
            permisoService.validarPermiso(idUsuarioSolicitante, "PRODUCTOS", "ACTUALIZAR");

            return productoRepository.findById(id)
                    .<ResponseEntity<?>>map(producto -> {
                        producto.setNombre(productoActualizado.getNombre());
                        producto.setPrecio(productoActualizado.getPrecio());
                        producto.setStock(productoActualizado.getStock());
                        producto.setEstado(productoActualizado.getEstado());
                        producto.setCategoria(productoActualizado.getCategoria());

                        Producto productoGuardado = productoRepository.save(producto);

                        auditoriaService.registrar(
                                idUsuarioSolicitante,
                                "PRODUCTO",
                                "UPDATE",
                                "PRODUCTOS",
                                "ACTUALIZAR_PRODUCTO",
                                "Producto actualizado correctamente.",
                                "ID producto: " + productoGuardado.getIdProducto()
                                        + ", Nombre: " + productoGuardado.getNombre(),
                                null
                        );

                        Map<String, Object> body = new LinkedHashMap<>();
                        body.put("mensaje", "Producto actualizado correctamente.");
                        body.put("data", productoGuardado);

                        return ResponseEntity.ok(body);
                    })
                    .orElseGet(() -> {
                        Map<String, Object> body = new LinkedHashMap<>();
                        body.put("mensaje", "Producto no encontrado.");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
                    });

        } catch (RuntimeException e) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProducto(@PathVariable("id") int id,
                                              @RequestParam Integer idUsuarioSolicitante) {
        try {
            permisoService.validarPermiso(idUsuarioSolicitante, "PRODUCTOS", "ELIMINAR");

            return productoRepository.findById(id)
                    .<ResponseEntity<?>>map(producto -> {
                        try {
                            Integer idProducto = producto.getIdProducto();
                            String nombreProducto = producto.getNombre();

                            productoRepository.delete(producto);

                            auditoriaService.registrar(
                                    idUsuarioSolicitante,
                                    "PRODUCTO",
                                    "DELETE",
                                    "PRODUCTOS",
                                    "ELIMINAR_PRODUCTO",
                                    "Producto eliminado correctamente.",
                                    "ID producto: " + idProducto + ", Nombre: " + nombreProducto,
                                    null
                            );

                            Map<String, Object> body = new LinkedHashMap<>();
                            body.put("mensaje", "Producto eliminado correctamente.");
                            return ResponseEntity.ok(body);

                        } catch (Exception e) {
                            Map<String, Object> body = new LinkedHashMap<>();
                            body.put("mensaje", "No se puede eliminar el producto porque está relacionado con un detalle de pedido.");
                            return ResponseEntity.badRequest().body(body);
                        }
                    })
                    .orElseGet(() -> {
                        Map<String, Object> body = new LinkedHashMap<>();
                        body.put("mensaje", "Producto no encontrado.");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
                    });

        } catch (RuntimeException e) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("mensaje", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
        }
    }
}
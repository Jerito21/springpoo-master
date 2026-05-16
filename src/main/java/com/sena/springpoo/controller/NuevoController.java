package com.sena.springpoo.controller;

import com.sena.springpoo.models.NotificacionEmail;
import com.sena.springpoo.models.Producto;
import com.sena.springpoo.models.Usuario;
import com.sena.springpoo.models.UsuarioProductoDTO;
import com.sena.springpoo.repository.NotificacionEmailRepository;
import com.sena.springpoo.repository.ProductoRepository;
import com.sena.springpoo.repository.UsuarioRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.sql.SQLException;
import java.util.*;


/**
 * ╔══════════════════════════════════════════════════════════════╗
 *  NuevoController — SENA Store
 * ╠══════════════════════════════════════════════════════════════╣
 *  GET    /nuevo/productos      @RequestParam
 *  POST   /nuevo/productos      @RequestBody
 *  PUT    /nuevo/productos/{id} @ModelAttribute
 *  DELETE /nuevo/productos/{id} @PathVariable
 * ╠══════════════════════════════════════════════════════════════╣
 *  Errores HTTP manejados:
 *  200 OK           → operación exitosa
 *  201 Created      → producto creado
 *  400 Bad Request  → datos inválidos
 *  404 Not Found    → ID no existe
 *  500 Server Error → base de datos apagada u otro error
 * ╚══════════════════════════════════════════════════════════════╝
 */
@Controller
@CrossOrigin(origins = "*")
public class NuevoController {

    // ── Logger ────────────────────────────────────────────────────
    private static final Logger log = LogManager.getLogger(NuevoController.class);

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotificacionEmailRepository notificacionRepository;

    // ── Correo fijo destinatario y URL webhook n8n ──
    private static final String CORREO_DESTINO = "j44962020@gmail.com";

    @Value("${n8n.webhook.url}")
    private String n8nWebhookUrl;

    // ──────────────────────────────────────────────────────────────
    //  Utilidad: llamar al webhook de N8N y registrar en BD
    // ──────────────────────────────────────────────────────────────
    private void notificarN8NProducto(Producto producto) {
        String asunto  = "🛍️ Nuevo producto creado: " + producto.getNombre();
        String mensaje = "Se ha creado el producto '" + producto.getNombre()
                + "' con precio $" + producto.getPrecio()
                + " en la categoría '" + producto.getCategoria() + "'.";

        // Guardar registro en BD con estado PENDIENTE
        NotificacionEmail notif = new NotificacionEmail(
                CORREO_DESTINO, asunto, mensaje,
                "PENDIENTE", producto.getId(), producto.getNombre()
        );
        notificacionRepository.save(notif);

        // Armar payload para n8n
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("correoDestino",    CORREO_DESTINO);
        payload.put("asunto",           asunto);
        payload.put("mensaje",          mensaje);
        payload.put("productoId",       producto.getId());
        payload.put("productoNombre",   producto.getNombre());
        payload.put("productoPrecio",   producto.getPrecio());
        payload.put("productoCategoria",producto.getCategoria());

        try {
            WebClient.create()
                    .post()
                    .uri(n8nWebhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                            resp -> {
                                log.info("✅ N8N notificado — producto id={} | resp={}", producto.getId(), resp);
                                notif.setEstado("ENVIADO");
                                notificacionRepository.save(notif);
                            },
                            err -> {
                                log.error("❌ Error notificando N8N — producto id={} | err={}", producto.getId(), err.getMessage());
                                notif.setEstado("FALLIDO");
                                notificacionRepository.save(notif);
                            }
                    );
        } catch (Exception e) {
            log.error("❌ Excepción llamando webhook N8N: {}", e.getMessage());
            notif.setEstado("FALLIDO");
            notificacionRepository.save(notif);
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  DTO interno
    // ──────────────────────────────────────────────────────────────
    static class ProductoRequest {
        private String nombre;
        private Double precio;
        private String categoria;
        private Long   usuarioId;   // 👈 para asignar al usuario en el INNER JOIN

        public ProductoRequest() {}
        public String getNombre()            { return nombre; }
        public void   setNombre(String n)    { this.nombre = n; }
        public Double getPrecio()            { return precio; }
        public void   setPrecio(Double p)    { this.precio = p; }
        public String getCategoria()         { return categoria; }
        public void   setCategoria(String c) { this.categoria = c; }
        public Long   getUsuarioId()         { return usuarioId; }
        public void   setUsuarioId(Long uid) { this.usuarioId = uid; }
    }

    // ── DTO para crear usuario ──
    static class UsuarioRequest {
        private String nombre;
        private String tipoDocumento;
        private String documento;
        private String telefono;

        public UsuarioRequest() {}
        public String getNombre()                          { return nombre; }
        public void   setNombre(String n)                  { this.nombre = n; }
        public String getTipoDocumento()                   { return tipoDocumento; }
        public void   setTipoDocumento(String t)           { this.tipoDocumento = t; }
        public String getDocumento()                       { return documento; }
        public void   setDocumento(String d)               { this.documento = d; }
        public String getTelefono()                        { return telefono; }
        public void   setTelefono(String tel)              { this.telefono = tel; }
    }

    // ──────────────────────────────────────────────────────────────
    //  Utilidad: idioma desde header Accept-Language
    // ──────────────────────────────────────────────────────────────
    private String idioma(String lang) {
        return (lang != null && lang.toLowerCase().startsWith("en")) ? "en" : "es";
    }
    private String msg(String es, String en, String lang) {
        return "en".equals(lang) ? en : es;
    }

    // ──────────────────────────────────────────────────────────────
    //  Utilidad: construir respuesta de error estándar
    // ──────────────────────────────────────────────────────────────
    private Map<String, Object> errorBody(HttpStatus status, String mensaje, String detalle) {
        Map<String, Object> err = new LinkedHashMap<>();
        err.put("timestamp", new Date().toString());
        err.put("status",    status.value());
        err.put("error",     status.getReasonPhrase());
        err.put("mensaje",   mensaje);
        err.put("detalle",   detalle);
        return err;
    }

    // ──────────────────────────────────────────────────────────────
    //  Utilidad: detectar si el error es de conexión a MySQL
    // ──────────────────────────────────────────────────────────────
    private boolean esErrorDeBaseDeDatos(Exception e) {
        Throwable causa = e;
        while (causa != null) {
            String msg = causa.getMessage();
            if (msg != null && (
                    msg.contains("Communications link failure") ||
                            msg.contains("Connection refused")         ||
                            msg.contains("Unable to acquire JDBC")     ||
                            msg.contains("could not prepare statement")||
                            msg.contains("Unable to open JDBC")        ||
                            msg.contains("No connection available")    ||
                            msg.contains("Connection is closed")       ||
                            causa instanceof SQLException              ||
                            causa instanceof java.net.ConnectException
            )) return true;
            causa = causa.getCause();
        }
        return e instanceof DataAccessException;
    }

    // ──────────────────────────────────────────────────────────────
    //  Utilidad: armar respuesta 500
    // ──────────────────────────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> error500(Exception e, String idioma) {
        if (esErrorDeBaseDeDatos(e)) {
            log.error("❌ Base de datos no disponible — {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorBody(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            msg("⚠ Base de datos no disponible",
                                    "⚠ Database not available", idioma),
                            msg("MySQL está apagado o no se puede conectar. " +
                                            "Inicia MySQL en XAMPP e intenta de nuevo.",
                                    "MySQL is off or unreachable. " +
                                            "Start MySQL in XAMPP and try again.", idioma)
                    ));
        }
        log.error("❌ Error interno del servidor — {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        msg("Error interno del servidor",
                                "Internal server error", idioma),
                        e.getMessage() != null ? e.getMessage() : "Unknown error"
                ));
    }

    // ══════════════════════════════════════════════════════════════
    //  VISTAS THYMELEAF
    // ══════════════════════════════════════════════════════════════

    @GetMapping("/nuevo/prueba")
    public String mostrarProductos(Model model) {
        log.info("Vista solicitada: /nuevo/prueba");
        model.addAttribute("imagenUrl",
                "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=400&q=80");
        model.addAttribute("imagenTitulo", "SENA Store");
        return "Prueba";
    }

    @GetMapping("/nuevo/formulario")
    public String mostrarFormulario() {
        log.info("Vista solicitada: /nuevo/formulario");
        return "formulario";
    }

    // ══════════════════════════════════════════════════════════════
    //  GET /nuevo/imagen
    // ══════════════════════════════════════════════════════════════
    @ResponseBody
    @GetMapping("/nuevo/imagen")
    public ResponseEntity<Map<String, Object>> obtenerImagen(
            @RequestHeader(value = "Accept-Language", defaultValue = "es") String lang) {

        log.info("GET /nuevo/imagen solicitado");
        String idioma = idioma(lang);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("imagenUrl",   "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=600&q=80");
        respuesta.put("titulo",      msg("SENA Store", "SENA Store", idioma));
        respuesta.put("descripcion", msg("Imagen enviada desde el controlador",
                "Image sent from the controller", idioma));

        log.info("GET /nuevo/imagen respondido correctamente");
        return ResponseEntity.ok(respuesta);
    }

    // ══════════════════════════════════════════════════════════════
    //  GET /nuevo/productos — @RequestParam
    //  200 OK  |  500 Base de datos apagada
    // ══════════════════════════════════════════════════════════════
    @ResponseBody
    @GetMapping("/nuevo/productos")
    public ResponseEntity<Map<String, Object>> consultar(
            @RequestParam(value = "categoria", required = false) String categoria,
            @RequestParam(value = "precioMax", required = false) Double precioMax,
            @RequestHeader(value = "Accept-Language", defaultValue = "es") String lang) {

        String idioma = idioma(lang);
        log.info("GET /nuevo/productos | categoria={} | precioMax={}", categoria, precioMax);

        try {
            List<Producto> resultado;

            if (categoria != null && precioMax != null) {
                log.debug("Filtro: categoria={} AND precioMax={}", categoria, precioMax);
                resultado = productoRepository.findByCategoriaAndPrecioLessThanEqual(categoria, precioMax);
            } else if (categoria != null) {
                log.debug("Filtro: categoria={}", categoria);
                resultado = productoRepository.findByCategoria(categoria);
            } else if (precioMax != null) {
                log.debug("Filtro: precioMax={}", precioMax);
                resultado = productoRepository.findByPrecioLessThanEqual(precioMax);
            } else {
                log.debug("Sin filtros — consultando todos los productos");
                resultado = productoRepository.findAll();
            }

            log.info("GET /nuevo/productos exitoso — {} productos encontrados", resultado.size());

            Map<String, Object> respuesta = new LinkedHashMap<>();
            respuesta.put("status",    200);
            respuesta.put("mensaje",   msg("Consulta realizada con éxito",
                    "Query executed successfully", idioma));
            respuesta.put("total",     resultado.size());
            respuesta.put("productos", resultado);

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            log.error("Error en GET /nuevo/productos | categoria={} | precioMax={}", categoria, precioMax, e);
            return error500(e, idioma);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  POST /nuevo/productos — @RequestBody
    //  201 Created  |  400 Bad Request  |  500 BD apagada
    // ══════════════════════════════════════════════════════════════
    @ResponseBody
    @PostMapping("/nuevo/productos")
    public ResponseEntity<Map<String, Object>> crear(
            @RequestBody ProductoRequest body,
            @RequestHeader(value = "Accept-Language", defaultValue = "es") String lang) {

        String idioma = idioma(lang);
        log.info("POST /nuevo/productos | nombre={} | precio={} | categoria={}",
                body.getNombre(), body.getPrecio(), body.getCategoria());

        // 400 — nombre vacío
        if (body.getNombre() == null || body.getNombre().isBlank()) {
            log.warn("POST /nuevo/productos — validación fallida: nombre vacío o nulo");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(errorBody(HttpStatus.BAD_REQUEST,
                            msg("El campo nombre es obligatorio",
                                    "Field nombre is required", idioma),
                            msg("Envía el campo 'nombre' en el JSON",
                                    "Send 'nombre' field in the JSON", idioma)));
        }

        // 400 — precio inválido
        if (body.getPrecio() == null || body.getPrecio() <= 0) {
            log.warn("POST /nuevo/productos — validación fallida: precio inválido={}", body.getPrecio());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(errorBody(HttpStatus.BAD_REQUEST,
                            msg("El precio debe ser mayor a 0",
                                    "Price must be greater than 0", idioma),
                            msg("Envía un precio válido mayor a $0",
                                    "Send a valid price greater than $0", idioma)));
        }

        try {
            Producto nuevo = new Producto(body.getNombre(), body.getPrecio(), body.getCategoria());

            // ── Asignar usuario si viene usuarioId ──
            if (body.getUsuarioId() != null) {
                usuarioRepository.findById(body.getUsuarioId()).ifPresent(nuevo::setUsuario);
            }

            Producto guardado = productoRepository.save(nuevo);

            log.info("POST /nuevo/productos exitoso — producto creado | id={} | nombre={}",
                    guardado.getId(), guardado.getNombre());

            // ── Notificar a N8N para enviar correo de confirmación ──
            notificarN8NProducto(guardado);

            Map<String, Object> respuesta = new LinkedHashMap<>();
            respuesta.put("status",   201);
            respuesta.put("mensaje",  msg("Producto guardado en base de datos",
                    "Product saved to database", idioma));
            respuesta.put("id",       guardado.getId());
            respuesta.put("producto", guardado);

            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);

        } catch (Exception e) {
            log.error("Error en POST /nuevo/productos | nombre={} | precio={}",
                    body.getNombre(), body.getPrecio(), e);
            return error500(e, idioma);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  PUT /nuevo/productos/{id} — @ModelAttribute + @PathVariable
    //  200 OK  |  404 Not Found  |  500 BD apagada
    // ══════════════════════════════════════════════════════════════
    @ResponseBody
    @PutMapping("/nuevo/productos/{id}")
    public ResponseEntity<Map<String, Object>> modificar(
            @PathVariable Long id,
            @ModelAttribute ProductoRequest cambios,
            @RequestHeader(value = "Accept-Language", defaultValue = "es") String lang) {

        String idioma = idioma(lang);
        log.info("PUT /nuevo/productos/{} | nombre={} | precio={} | categoria={}",
                id, cambios.getNombre(), cambios.getPrecio(), cambios.getCategoria());

        try {
            // 404 — ID no existe
            Optional<Producto> optional = productoRepository.findById(id);
            if (optional.isEmpty()) {
                log.warn("PUT /nuevo/productos/{} — producto no encontrado", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(errorBody(HttpStatus.NOT_FOUND,
                                msg("Producto no encontrado con ID: " + id,
                                        "Product not found with ID: "    + id, idioma),
                                msg("Verifica el ID e intenta de nuevo",
                                        "Check the ID and try again", idioma)));
            }

            Producto existente = optional.get();
            log.debug("PUT /nuevo/productos/{} — datos actuales: nombre={} | precio={} | categoria={}",
                    id, existente.getNombre(), existente.getPrecio(), existente.getCategoria());

            if (cambios.getNombre()    != null && !cambios.getNombre().isBlank())
                existente.setNombre(cambios.getNombre());
            if (cambios.getPrecio()    != null && cambios.getPrecio() > 0)
                existente.setPrecio(cambios.getPrecio());
            if (cambios.getCategoria() != null && !cambios.getCategoria().isBlank())
                existente.setCategoria(cambios.getCategoria());

            Producto actualizado = productoRepository.save(existente);

            log.info("PUT /nuevo/productos/{} exitoso — nombre={} | precio={} | categoria={}",
                    id, actualizado.getNombre(), actualizado.getPrecio(), actualizado.getCategoria());

            Map<String, Object> respuesta = new LinkedHashMap<>();
            respuesta.put("status",   200);
            respuesta.put("mensaje",  msg("Producto actualizado en base de datos",
                    "Product updated in database", idioma));
            respuesta.put("producto", actualizado);

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            log.error("Error en PUT /nuevo/productos/{}", id, e);
            return error500(e, idioma);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  DELETE /nuevo/productos/{id} — @PathVariable
    //  200 OK  |  404 Not Found  |  500 BD apagada
    // ══════════════════════════════════════════════════════════════
    @ResponseBody
    @DeleteMapping("/nuevo/productos/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "es") String lang) {

        String idioma = idioma(lang);
        log.info("DELETE /nuevo/productos/{}", id);

        try {
            // 404 — ID no existe
            if (!productoRepository.existsById(id)) {
                log.warn("DELETE /nuevo/productos/{} — producto no encontrado", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(errorBody(HttpStatus.NOT_FOUND,
                                msg("No se encontró el producto con ID: " + id,
                                        "Product not found with ID: "         + id, idioma),
                                msg("Verifica el ID e intenta de nuevo",
                                        "Check the ID and try again", idioma)));
            }

            productoRepository.deleteById(id);

            log.info("DELETE /nuevo/productos/{} exitoso — producto eliminado", id);

            Map<String, Object> respuesta = new LinkedHashMap<>();
            respuesta.put("status",  200);
            respuesta.put("mensaje", msg("Producto eliminado de la base de datos",
                    "Product deleted from database", idioma));
            respuesta.put("id",      id);

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            log.error("Error en DELETE /nuevo/productos/{}", id, e);
            return error500(e, idioma);
        }
    }
    // ══════════════════════════════════════════════════════════════
    //  GET /nuevo/usuarios — lista todos los usuarios
    // ══════════════════════════════════════════════════════════════
    @ResponseBody
    @GetMapping("/nuevo/usuarios")
    public ResponseEntity<Map<String, Object>> listarUsuarios(
            @RequestHeader(value = "Accept-Language", defaultValue = "es") String lang) {

        String idioma = idioma(lang);
        log.info("GET /nuevo/usuarios");

        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            Map<String, Object> respuesta = new LinkedHashMap<>();
            respuesta.put("status",   200);
            respuesta.put("total",    usuarios.size());
            respuesta.put("usuarios", usuarios);
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return error500(e, idioma);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  POST /nuevo/usuarios — crear usuario
    // ══════════════════════════════════════════════════════════════
    @ResponseBody
    @PostMapping("/nuevo/usuarios")
    public ResponseEntity<Map<String, Object>> crearUsuario(
            @RequestBody UsuarioRequest body,
            @RequestHeader(value = "Accept-Language", defaultValue = "es") String lang) {

        String idioma = idioma(lang);
        log.info("POST /nuevo/usuarios | nombre={}", body.getNombre());

        if (body.getNombre() == null || body.getNombre().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(errorBody(HttpStatus.BAD_REQUEST,
                            msg("El nombre es obligatorio", "Name is required", idioma),
                            msg("Envía el campo 'nombre'", "Send the 'nombre' field", idioma)));
        }

        try {
            Usuario u = new Usuario(
                    body.getNombre(),
                    body.getTipoDocumento(),
                    body.getDocumento(),
                    body.getTelefono());
            Usuario guardado = usuarioRepository.save(u);

            Map<String, Object> respuesta = new LinkedHashMap<>();
            respuesta.put("status",  201);
            respuesta.put("mensaje", msg("Usuario creado", "User created", idioma));
            respuesta.put("id",      guardado.getId());
            respuesta.put("usuario", guardado);
            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
        } catch (Exception e) {
            return error500(e, idioma);
        }
    }

    // ══════════════════════════════════════════════════════════════
//  GET /nuevo/usuarios-productos — INNER JOIN con DTO
//  200 OK  |  500 BD apagada
// ══════════════════════════════════════════════════════════════
    @ResponseBody
    @GetMapping("/nuevo/usuarios-productos")
    public ResponseEntity<Map<String, Object>> consultarUsuariosConProductos(
            @RequestParam(value = "categoria", required = false) String categoria,
            @RequestParam(value = "nombre",    required = false) String nombre,
            @RequestHeader(value = "Accept-Language", defaultValue = "es") String lang) {

        String idioma = idioma(lang);
        log.info("GET /nuevo/usuarios-productos | categoria={} | nombre={}", categoria, nombre);

        try {
            List<UsuarioProductoDTO> resultado;

            if (categoria != null) {
                log.debug("INNER JOIN filtrado por categoria={}", categoria);
                resultado = usuarioRepository.findUsuariosConProductosPorCategoria(categoria);
            } else if (nombre != null) {
                log.debug("INNER JOIN filtrado por nombre={}", nombre);
                resultado = usuarioRepository.findProductosPorNombreUsuario(nombre);
            } else {
                log.debug("INNER JOIN sin filtros");
                resultado = usuarioRepository.findUsuariosConProductos();
            }

            log.info("GET /nuevo/usuarios-productos exitoso — {} registros", resultado.size());

            Map<String, Object> respuesta = new LinkedHashMap<>();
            respuesta.put("status",  200);
            respuesta.put("mensaje", msg("Consulta con JOIN realizada con éxito",
                    "JOIN query executed successfully", idioma));
            respuesta.put("total",   resultado.size());
            respuesta.put("datos",   resultado);

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            log.error("Error en GET /nuevo/usuarios-productos", e);
            return error500(e, idioma);
        }
    }
}
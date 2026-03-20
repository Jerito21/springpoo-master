package com.sena.springpoo.controller;

import com.sena.springpoo.models.Producto;
import com.sena.springpoo.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 *  NuevoController — conectado a MySQL
 * ╠══════════════════════════════════════════════════════════════╣
 *  VISTAS  (Thymeleaf)
 *  GET /nuevo/prueba   → templates/Productos.html
 *  GET /nuevo/formulario        → templates/formulario.html
 *
 *  REST JSON
 *  GET    /nuevo/productos          @RequestParam
 *  POST   /nuevo/productos          @RequestBody
 *  PUT    /nuevo/productos/{id}     @ModelAttribute
 *  DELETE /nuevo/productos/{id}     @PathVariable
 * ╠══════════════════════════════════════════════════════════════╣
 *  Header  Accept-Language: es → español  |  en → inglés
 * ╚══════════════════════════════════════════════════════════════╝
 */
@Controller
@CrossOrigin(origins = "*")
public class NuevoController {

    @Autowired
    private ProductoRepository productoRepository;

    // ──────────────────────────────────────────────────────────────
    //  DTO interno para POST y PUT
    // ──────────────────────────────────────────────────────────────
    static class ProductoRequest {
        private String nombre;
        private Double precio;
        private String categoria;

        public ProductoRequest() {}
        public String getNombre()            { return nombre; }
        public void   setNombre(String n)    { this.nombre = n; }
        public Double getPrecio()            { return precio; }
        public void   setPrecio(Double p)    { this.precio = p; }
        public String getCategoria()         { return categoria; }
        public void   setCategoria(String c) { this.categoria = c; }
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

    // ══════════════════════════════════════════════════════════════
    //  VISTAS THYMELEAF
    // ══════════════════════════════════════════════════════════════

    /**
     * URL: localhost:8080/nuevo/prueba
     * Abre: templates/Productos.html  ← formulario CRUD completo
     */
    @GetMapping("/nuevo/prueba")
    public String mostrarProductos() {
        return "Prueba";       // nombre exacto sin .html
    }

    /**
     * URL: localhost:8080/nuevo/formulario
     * Abre: templates/formulario.html
     */
    @GetMapping("/nuevo/formulario")
    public String mostrarFormulario() {
        return "formulario";
    }

    // ══════════════════════════════════════════════════════════════
    //  GET  /nuevo/productos  —  @RequestParam
    // ══════════════════════════════════════════════════════════════
    @ResponseBody
    @GetMapping("/nuevo/productos")
    public ResponseEntity<Map<String, Object>> consultar(
            @RequestParam(value = "categoria", required = false) String categoria,
            @RequestParam(value = "precioMax", required = false) Double precioMax,
            @RequestHeader(value = "Accept-Language", defaultValue = "es") String lang) {

        String idioma = idioma(lang);
        List<Producto> resultado;

        if (categoria != null && precioMax != null) {
            resultado = productoRepository.findByCategoriaAndPrecioLessThanEqual(categoria, precioMax);
        } else if (categoria != null) {
            resultado = productoRepository.findByCategoria(categoria);
        } else if (precioMax != null) {
            resultado = productoRepository.findByPrecioLessThanEqual(precioMax);
        } else {
            resultado = productoRepository.findAll();
        }

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje",   msg("Consulta realizada con exito",
                "Query executed successfully", idioma));
        respuesta.put("total",     resultado.size());
        respuesta.put("productos", resultado);
        return ResponseEntity.ok(respuesta);
    }

    // ══════════════════════════════════════════════════════════════
    //  POST  /nuevo/productos  —  @RequestBody
    // ══════════════════════════════════════════════════════════════
    @ResponseBody
    @PostMapping("/nuevo/productos")
    public ResponseEntity<Map<String, Object>> crear(
            @RequestBody ProductoRequest body,
            @RequestHeader(value = "Accept-Language", defaultValue = "es") String lang) {

        String idioma = idioma(lang);
        Map<String, Object> respuesta = new LinkedHashMap<>();

        if (body.getNombre() == null || body.getNombre().isBlank()) {
            respuesta.put("error", msg("El campo nombre es obligatorio",
                    "Field nombre is required", idioma));
            return ResponseEntity.badRequest().body(respuesta);
        }

        Producto nuevo = new Producto(body.getNombre(), body.getPrecio(), body.getCategoria());
        Producto guardado = productoRepository.save(nuevo);

        respuesta.put("mensaje",  msg("Producto guardado en base de datos",
                "Product saved to database", idioma));
        respuesta.put("id",       guardado.getId());
        respuesta.put("producto", guardado);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // ══════════════════════════════════════════════════════════════
    //  PUT  /nuevo/productos/{id}  —  @ModelAttribute + @PathVariable
    // ══════════════════════════════════════════════════════════════
    @ResponseBody
    @PutMapping("/nuevo/productos/{id}")
    public ResponseEntity<Map<String, Object>> modificar(
            @PathVariable Long id,
            @ModelAttribute ProductoRequest cambios,
            @RequestHeader(value = "Accept-Language", defaultValue = "es") String lang) {

        String idioma = idioma(lang);
        Map<String, Object> respuesta = new LinkedHashMap<>();

        Optional<Producto> optional = productoRepository.findById(id);
        if (optional.isEmpty()) {
            respuesta.put("error", msg("Producto no encontrado con ID: " + id,
                    "Product not found with ID: "    + id, idioma));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
        }

        Producto existente = optional.get();
        if (cambios.getNombre()    != null && !cambios.getNombre().isBlank())
            existente.setNombre(cambios.getNombre());
        if (cambios.getPrecio()    != null && cambios.getPrecio() > 0)
            existente.setPrecio(cambios.getPrecio());
        if (cambios.getCategoria() != null && !cambios.getCategoria().isBlank())
            existente.setCategoria(cambios.getCategoria());

        Producto actualizado = productoRepository.save(existente);

        respuesta.put("mensaje",  msg("Producto actualizado en base de datos",
                "Product updated in database", idioma));
        respuesta.put("producto", actualizado);
        return ResponseEntity.ok(respuesta);
    }

    // ══════════════════════════════════════════════════════════════
    //  DELETE  /nuevo/productos/{id}  —  @PathVariable
    // ══════════════════════════════════════════════════════════════
    @ResponseBody
    @DeleteMapping("/nuevo/productos/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(
            @PathVariable Long id,
            @RequestHeader(value = "Accept-Language", defaultValue = "es") String lang) {

        String idioma = idioma(lang);
        Map<String, Object> respuesta = new LinkedHashMap<>();

        if (!productoRepository.existsById(id)) {
            respuesta.put("error", msg("No se encontro el producto con ID: " + id,
                    "Product not found with ID: "         + id, idioma));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
        }

        productoRepository.deleteById(id);

        respuesta.put("mensaje", msg("Producto eliminado de la base de datos",
                "Product deleted from database", idioma));
        respuesta.put("id",      id);
        return ResponseEntity.ok(respuesta);
    }
}
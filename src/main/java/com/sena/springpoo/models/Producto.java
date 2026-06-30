package com.sena.springpoo.models;

/**
 * POJO — tabla 'productos' en MySQL.
 * Sin anotaciones JPA: el mapeo se hace manualmente en ProductoRepository con JdbcTemplate.
 */
public class Producto {

    private Long   id;
    private String nombre;
    private Double precio;
    private String categoria;
    private Long   usuarioId; // FK hacia la tabla usuarios

    // ── Constructores ──
    public Producto() {}

    public Producto(String nombre, Double precio, String categoria) {
        this.nombre    = nombre;
        this.precio    = precio;
        this.categoria = categoria;
    }

    public Producto(String nombre, Double precio, String categoria, Long usuarioId) {
        this.nombre    = nombre;
        this.precio    = precio;
        this.categoria = categoria;
        this.usuarioId = usuarioId;
    }

    // ── Getters y Setters ──
    public Long   getId()                        { return id; }
    public void   setId(Long id)                 { this.id = id; }

    public String getNombre()                    { return nombre; }
    public void   setNombre(String nombre)       { this.nombre = nombre; }

    public Double getPrecio()                    { return precio; }
    public void   setPrecio(Double precio)       { this.precio = precio; }

    public String getCategoria()                 { return categoria; }
    public void   setCategoria(String categoria) { this.categoria = categoria; }

    public Long   getUsuarioId()                 { return usuarioId; }
    public void   setUsuarioId(Long usuarioId)   { this.usuarioId = usuarioId; }
}
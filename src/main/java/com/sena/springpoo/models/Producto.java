package com.sena.springpoo.models;

import jakarta.persistence.*;

/**
 * Entidad JPA — se mapea a la tabla 'productos' en MySQL.
 * Hibernate la crea automáticamente gracias a ddl-auto=update.
 */
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Double precio;

    @Column(length = 80)
    private String categoria;

    // ── RELACIÓN: muchos productos pertenecen a un usuario ──
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")  // 👈 columna que se crea en la tabla productos
    private Usuario usuario;

    // ── Constructores ──
    public Producto() {}

    // Constructor sin usuario (para cuando no se asigna)
    public Producto(String nombre, Double precio, String categoria) {
        this.nombre    = nombre;
        this.precio    = precio;
        this.categoria = categoria;
    }

    // Constructor con usuario (para el JOIN)
    public Producto(String nombre, Double precio, String categoria, Usuario usuario) {
        this.nombre    = nombre;
        this.precio    = precio;
        this.categoria = categoria;
        this.usuario   = usuario;
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

    // ── Getter y Setter del usuario ──
    public Usuario getUsuario()                  { return usuario; }
    public void    setUsuario(Usuario usuario)   { this.usuario = usuario; }
}
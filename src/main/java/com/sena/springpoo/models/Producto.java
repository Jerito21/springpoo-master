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

    // ── Constructores ──
    public Producto() {}

    public Producto(String nombre, Double precio, String categoria) {
        this.nombre    = nombre;
        this.precio    = precio;
        this.categoria = categoria;
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
}
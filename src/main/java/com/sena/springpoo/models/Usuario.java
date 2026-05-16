package com.sena.springpoo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String tipoDocumento;
    private String documento;
    private String telefono;

    // ── RELACIÓN: un usuario tiene muchos productos ──
    @JsonIgnore                              // 👈 evita el loop infinito
    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY)
    private List<Producto> productos;

    // ── Constructores ──
    public Usuario() {}

    public Usuario(String nombre, String tipoDocumento, String documento, String telefono) {
        this.nombre        = nombre;
        this.tipoDocumento = tipoDocumento;
        this.documento     = documento;
        this.telefono      = telefono;
    }

    // ── Getters y Setters existentes ──
    public Long getId() { return id; }

    public String getNombre()                          { return nombre; }
    public void   setNombre(String nombre)             { this.nombre = nombre; }

    public String getTipoDocumento()                   { return tipoDocumento; }
    public void   setTipoDocumento(String tipoDoc)     { this.tipoDocumento = tipoDoc; }

    public String getDocumento()                       { return documento; }
    public void   setDocumento(String documento)       { this.documento = documento; }

    public String getTelefono()                        { return telefono; }
    public void   setTelefono(String telefono)         { this.telefono = telefono; }

    // ── Getter y Setter nuevo ──
    public List<Producto> getProductos()               { return productos; }
    public void           setProductos(List<Producto> p) { this.productos = p; }
}
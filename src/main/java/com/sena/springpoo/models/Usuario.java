package com.sena.springpoo.models;

/**
 * POJO — tabla 'usuarios' en MySQL.
 * Sin anotaciones JPA: el mapeo se hace manualmente en UsuarioRepository con JdbcTemplate.
 */
public class Usuario {

    private Long   id;
    private String nombre;
    private String tipoDocumento;
    private String documento;
    private String telefono;

    // ── Constructores ──
    public Usuario() {}

    public Usuario(String nombre, String tipoDocumento, String documento, String telefono) {
        this.nombre        = nombre;
        this.tipoDocumento = tipoDocumento;
        this.documento     = documento;
        this.telefono      = telefono;
    }

    // ── Getters y Setters ──
    public Long   getId()                              { return id; }
    public void   setId(Long id)                       { this.id = id; }

    public String getNombre()                          { return nombre; }
    public void   setNombre(String nombre)             { this.nombre = nombre; }

    public String getTipoDocumento()                   { return tipoDocumento; }
    public void   setTipoDocumento(String tipoDoc)     { this.tipoDocumento = tipoDoc; }

    public String getDocumento()                       { return documento; }
    public void   setDocumento(String documento)       { this.documento = documento; }

    public String getTelefono()                        { return telefono; }
    public void   setTelefono(String telefono)         { this.telefono = telefono; }
}
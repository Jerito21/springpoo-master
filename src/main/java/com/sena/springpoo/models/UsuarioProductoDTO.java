package com.sena.springpoo.models;

public class UsuarioProductoDTO {

    // Datos del usuario
    private Long   usuarioId;
    private String usuarioNombre;
    private String usuarioTipoDocumento;
    private String usuarioDocumento;
    private String usuarioTelefono;

    // Datos del producto
    private Long   productoId;
    private String productoNombre;
    private Double productoPrecio;
    private String productoCategoria;

    // ── Constructor ──
    public UsuarioProductoDTO(
            Long   usuarioId,
            String usuarioNombre,
            String usuarioTipoDocumento,
            String usuarioDocumento,
            String usuarioTelefono,
            Long   productoId,
            String productoNombre,
            Double productoPrecio,
            String productoCategoria) {

        this.usuarioId            = usuarioId;
        this.usuarioNombre        = usuarioNombre;
        this.usuarioTipoDocumento = usuarioTipoDocumento;
        this.usuarioDocumento     = usuarioDocumento;
        this.usuarioTelefono      = usuarioTelefono;
        this.productoId           = productoId;
        this.productoNombre       = productoNombre;
        this.productoPrecio       = productoPrecio;
        this.productoCategoria    = productoCategoria;
    }

    // ── Getters ──
    public Long   getUsuarioId()             { return usuarioId; }
    public String getUsuarioNombre()         { return usuarioNombre; }
    public String getUsuarioTipoDocumento()  { return usuarioTipoDocumento; }
    public String getUsuarioDocumento()      { return usuarioDocumento; }
    public String getUsuarioTelefono()       { return usuarioTelefono; }
    public Long   getProductoId()            { return productoId; }
    public String getProductoNombre()        { return productoNombre; }
    public Double getProductoPrecio()        { return productoPrecio; }
    public String getProductoCategoria()     { return productoCategoria; }
}
package com.sena.springpoo.models;

import java.time.LocalDateTime;

/**
 * POJO — tabla 'notificaciones_email' en MySQL.
 * Sin anotaciones JPA: el mapeo se hace manualmente en NotificacionEmailRepository con JdbcTemplate.
 */
public class NotificacionEmail {

    private Long          id;
    private String        destinatario;
    private String        asunto;
    private String        mensaje;
    private String        estado;
    private LocalDateTime fechaEnvio;
    private Long          productoId;
    private String        productoNombre;

    public NotificacionEmail() {}

    public NotificacionEmail(String destinatario, String asunto, String mensaje,
                             String estado, Long productoId, String productoNombre) {
        this.destinatario   = destinatario;
        this.asunto         = asunto;
        this.mensaje        = mensaje;
        this.estado         = estado;
        this.fechaEnvio     = LocalDateTime.now();
        this.productoId     = productoId;
        this.productoNombre = productoNombre;
    }

    public Long          getId()                                { return id; }
    public void          setId(Long id)                         { this.id = id; }
    public String        getDestinatario()                      { return destinatario; }
    public void          setDestinatario(String d)              { this.destinatario = d; }
    public String        getAsunto()                            { return asunto; }
    public void          setAsunto(String a)                    { this.asunto = a; }
    public String        getMensaje()                           { return mensaje; }
    public void          setMensaje(String m)                   { this.mensaje = m; }
    public String        getEstado()                            { return estado; }
    public void          setEstado(String e)                    { this.estado = e; }
    public LocalDateTime getFechaEnvio()                        { return fechaEnvio; }
    public void          setFechaEnvio(LocalDateTime f)         { this.fechaEnvio = f; }
    public Long          getProductoId()                        { return productoId; }
    public void          setProductoId(Long pid)                { this.productoId = pid; }
    public String        getProductoNombre()                    { return productoNombre; }
    public void          setProductoNombre(String n)            { this.productoNombre = n; }
}
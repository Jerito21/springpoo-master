package com.sena.springpoo.repository;

import com.sena.springpoo.models.NotificacionEmail;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

/**
 * Repositorio JDBC para la tabla 'notificaciones_email'.
 * Usa JdbcTemplate con SQL manual.
 */
@Repository
public class NotificacionEmailRepository {

    private final JdbcTemplate jdbc;

    public NotificacionEmailRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── RowMapper para NotificacionEmail ──
    private final RowMapper<NotificacionEmail> mapper = (rs, rowNum) -> {
        NotificacionEmail n = new NotificacionEmail();
        n.setId(rs.getLong("id"));
        n.setDestinatario(rs.getString("destinatario"));
        n.setAsunto(rs.getString("asunto"));
        n.setMensaje(rs.getString("mensaje"));
        n.setEstado(rs.getString("estado"));
        Timestamp ts = rs.getTimestamp("fecha_envio");
        if (ts != null) n.setFechaEnvio(ts.toLocalDateTime());
        n.setProductoId(rs.getLong("producto_id"));
        n.setProductoNombre(rs.getString("producto_nombre"));
        return n;
    };

    // ── INSERT o UPDATE del estado ──
    public NotificacionEmail save(NotificacionEmail n) {
        if (n.getId() == null) {
            // INSERT — nueva notificación
            String sql = """
                INSERT INTO notificaciones_email
                    (destinatario, asunto, mensaje, estado, fecha_envio, producto_id, producto_nombre)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, n.getDestinatario());
                ps.setString(2, n.getAsunto());
                ps.setString(3, n.getMensaje());
                ps.setString(4, n.getEstado());
                ps.setTimestamp(5, n.getFechaEnvio() != null
                        ? Timestamp.valueOf(n.getFechaEnvio()) : null);
                if (n.getProductoId() != null) ps.setLong(6, n.getProductoId());
                else                            ps.setNull(6, java.sql.Types.BIGINT);
                ps.setString(7, n.getProductoNombre());
                return ps;
            }, keyHolder);
            n.setId(keyHolder.getKey().longValue());
        } else {
            // UPDATE — solo actualiza el estado (ENVIADO / FALLIDO)
            jdbc.update(
                "UPDATE notificaciones_email SET estado = ? WHERE id = ?",
                n.getEstado(), n.getId()
            );
        }
        return n;
    }

    // ── SELECT * WHERE estado = ? ──
    public List<NotificacionEmail> findByEstado(String estado) {
        return jdbc.query(
                "SELECT * FROM notificaciones_email WHERE estado = ?", mapper, estado);
    }

    // ── SELECT * WHERE producto_id = ? ──
    public List<NotificacionEmail> findByProductoId(Long productoId) {
        return jdbc.query(
                "SELECT * FROM notificaciones_email WHERE producto_id = ?", mapper, productoId);
    }
}
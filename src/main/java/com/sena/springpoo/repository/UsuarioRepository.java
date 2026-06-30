package com.sena.springpoo.repository;

import com.sena.springpoo.models.Usuario;
import com.sena.springpoo.models.UsuarioProductoDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio JDBC para la tabla 'usuarios'.
 * Usa JdbcTemplate con SQL manual en lugar de JpaRepository y JPQL.
 */
@Repository
public class UsuarioRepository {

    private final JdbcTemplate jdbc;

    public UsuarioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── RowMapper para Usuario ──
    private final RowMapper<Usuario> userMapper = (rs, rowNum) -> {
        Usuario u = new Usuario();
        u.setId(rs.getLong("id"));
        u.setNombre(rs.getString("nombre"));
        u.setTipoDocumento(rs.getString("tipo_documento"));
        u.setDocumento(rs.getString("documento"));
        u.setTelefono(rs.getString("telefono"));
        return u;
    };

    // ── RowMapper para UsuarioProductoDTO (resultado del INNER JOIN) ──
    private final RowMapper<UsuarioProductoDTO> joinMapper = (rs, rowNum) ->
        new UsuarioProductoDTO(
            rs.getLong("usuario_id"),
            rs.getString("usuario_nombre"),
            rs.getString("usuario_tipo_documento"),
            rs.getString("usuario_documento"),
            rs.getString("usuario_telefono"),
            rs.getLong("producto_id"),
            rs.getString("producto_nombre"),
            rs.getDouble("producto_precio"),
            rs.getString("producto_categoria")
        );

    // ── SQL base del INNER JOIN ──
    private static final String JOIN_SQL = """
        SELECT
            u.id           AS usuario_id,
            u.nombre       AS usuario_nombre,
            u.tipo_documento AS usuario_tipo_documento,
            u.documento    AS usuario_documento,
            u.telefono     AS usuario_telefono,
            p.id           AS producto_id,
            p.nombre       AS producto_nombre,
            p.precio       AS producto_precio,
            p.categoria    AS producto_categoria
        FROM usuarios u
        INNER JOIN productos p ON p.usuario_id = u.id
        """;

    // ── SELECT * FROM usuarios ──
    public List<Usuario> findAll() {
        return jdbc.query("SELECT * FROM usuarios", userMapper);
    }

    // ── SELECT * FROM usuarios WHERE id = ? ──
    public Optional<Usuario> findById(Long id) {
        List<Usuario> result = jdbc.query(
                "SELECT * FROM usuarios WHERE id = ?", userMapper, id);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    // ── INSERT o UPDATE ──
    public Usuario save(Usuario u) {
        if (u.getId() == null) {
            String sql = "INSERT INTO usuarios (nombre, tipo_documento, documento, telefono) VALUES (?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, u.getNombre());
                ps.setString(2, u.getTipoDocumento());
                ps.setString(3, u.getDocumento());
                ps.setString(4, u.getTelefono());
                return ps;
            }, keyHolder);
            u.setId(keyHolder.getKey().longValue());
        } else {
            jdbc.update(
                "UPDATE usuarios SET nombre = ?, tipo_documento = ?, documento = ?, telefono = ? WHERE id = ?",
                u.getNombre(), u.getTipoDocumento(), u.getDocumento(), u.getTelefono(), u.getId()
            );
        }
        return u;
    }

    // ── DELETE FROM usuarios WHERE id = ? ──
    public void deleteById(Long id) {
        jdbc.update("DELETE FROM usuarios WHERE id = ?", id);
    }

    // ── INNER JOIN — todos los usuarios con sus productos ──
    public List<UsuarioProductoDTO> findUsuariosConProductos() {
        return jdbc.query(JOIN_SQL, joinMapper);
    }

    // ── INNER JOIN filtrado por categoría de producto ──
    public List<UsuarioProductoDTO> findUsuariosConProductosPorCategoria(String categoria) {
        return jdbc.query(JOIN_SQL + " WHERE p.categoria = ?", joinMapper, categoria);
    }

    // ── INNER JOIN filtrado por nombre de usuario ──
    public List<UsuarioProductoDTO> findProductosPorNombreUsuario(String nombre) {
        return jdbc.query(JOIN_SQL + " WHERE u.nombre = ?", joinMapper, nombre);
    }
}
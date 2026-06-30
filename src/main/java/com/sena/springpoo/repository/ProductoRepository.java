package com.sena.springpoo.repository;

import com.sena.springpoo.models.Producto;
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
 * Repositorio JDBC para la tabla 'productos'.
 * Usa JdbcTemplate para ejecutar SQL manual en lugar de Hibernate/JPA.
 */
@Repository
public class ProductoRepository {

    private final JdbcTemplate jdbc;

    // Spring inyecta JdbcTemplate automáticamente gracias a la config en application.properties
    public ProductoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── RowMapper: convierte cada fila del ResultSet en un objeto Producto ──
    private final RowMapper<Producto> mapper = (rs, rowNum) -> {
        Producto p = new Producto();
        p.setId(rs.getLong("id"));
        p.setNombre(rs.getString("nombre"));
        p.setPrecio(rs.getDouble("precio"));
        p.setCategoria(rs.getString("categoria"));
        // usuario_id puede ser NULL en la BD
        long uid = rs.getLong("usuario_id");
        p.setUsuarioId(rs.wasNull() ? null : uid);
        return p;
    };

    // ── SELECT * FROM productos ──
    public List<Producto> findAll() {
        return jdbc.query("SELECT * FROM productos", mapper);
    }

    // ── SELECT * FROM productos WHERE id = ? ──
    public Optional<Producto> findById(Long id) {
        List<Producto> result = jdbc.query(
                "SELECT * FROM productos WHERE id = ?", mapper, id);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    // ── SELECT COUNT(*) FROM productos WHERE id = ? ──
    public boolean existsById(Long id) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM productos WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    // ── INSERT o UPDATE según si el producto tiene ID ──
    public Producto save(Producto p) {
        if (p.getId() == null) {
            // INSERT — producto nuevo
            String sql = "INSERT INTO productos (nombre, precio, categoria, usuario_id) VALUES (?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, p.getNombre());
                ps.setDouble(2, p.getPrecio());
                ps.setString(3, p.getCategoria());
                if (p.getUsuarioId() != null) ps.setLong(4, p.getUsuarioId());
                else                           ps.setNull(4, java.sql.Types.BIGINT);
                return ps;
            }, keyHolder);
            p.setId(keyHolder.getKey().longValue());
        } else {
            // UPDATE — producto existente
            jdbc.update(
                "UPDATE productos SET nombre = ?, precio = ?, categoria = ?, usuario_id = ? WHERE id = ?",
                p.getNombre(), p.getPrecio(), p.getCategoria(), p.getUsuarioId(), p.getId()
            );
        }
        return p;
    }

    // ── DELETE FROM productos WHERE id = ? ──
    public void deleteById(Long id) {
        jdbc.update("DELETE FROM productos WHERE id = ?", id);
    }

    // ── SELECT * FROM productos WHERE categoria = ? ──
    public List<Producto> findByCategoria(String categoria) {
        return jdbc.query(
                "SELECT * FROM productos WHERE categoria = ?", mapper, categoria);
    }

    // ── SELECT * FROM productos WHERE precio <= ? ──
    public List<Producto> findByPrecioLessThanEqual(Double precioMax) {
        return jdbc.query(
                "SELECT * FROM productos WHERE precio <= ?", mapper, precioMax);
    }

    // ── SELECT * FROM productos WHERE categoria = ? AND precio <= ? ──
    public List<Producto> findByCategoriaAndPrecioLessThanEqual(String categoria, Double precioMax) {
        return jdbc.query(
                "SELECT * FROM productos WHERE categoria = ? AND precio <= ?",
                mapper, categoria, precioMax);
    }
}
package com.sena.springpoo.repository;

import com.sena.springpoo.models.Usuario;
import com.sena.springpoo.models.UsuarioProductoDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // ── INNER JOIN — todos los usuarios con sus productos ──
    @Query("""
        SELECT new com.sena.springpoo.models.UsuarioProductoDTO(
            u.id,
            u.nombre,
            u.tipoDocumento,
            u.documento,
            u.telefono,
            p.id,
            p.nombre,
            p.precio,
            p.categoria
        )
        FROM Usuario u
        INNER JOIN u.productos p
    """)
    List<UsuarioProductoDTO> findUsuariosConProductos();

    // ── INNER JOIN filtrado por categoria ──
    @Query("""
        SELECT new com.sena.springpoo.models.UsuarioProductoDTO(
            u.id,
            u.nombre,
            u.tipoDocumento,
            u.documento,
            u.telefono,
            p.id,
            p.nombre,
            p.precio,
            p.categoria
        )
        FROM Usuario u
        INNER JOIN u.productos p
        WHERE p.categoria = :categoria
    """)
    List<UsuarioProductoDTO> findUsuariosConProductosPorCategoria(
            @Param("categoria") String categoria);

    // ── INNER JOIN filtrado por nombre de usuario ──
    @Query("""
        SELECT new com.sena.springpoo.models.UsuarioProductoDTO(
            u.id,
            u.nombre,
            u.tipoDocumento,
            u.documento,
            u.telefono,
            p.id,
            p.nombre,
            p.precio,
            p.categoria
        )
        FROM Usuario u
        INNER JOIN u.productos p
        WHERE u.nombre = :nombre
    """)
    List<UsuarioProductoDTO> findProductosPorNombreUsuario(
            @Param("nombre") String nombre);
}
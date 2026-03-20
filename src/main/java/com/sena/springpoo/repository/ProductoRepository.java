package com.sena.springpoo.repository;

import com.sena.springpoo.models.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA genera automáticamente todas las operaciones
 * CRUD — no necesitas escribir SQL.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Buscar por categoría (equivale a: SELECT * FROM productos WHERE categoria = ?)
    List<Producto> findByCategoria(String categoria);

    // Buscar por precio menor o igual (para el filtro precioMax)
    List<Producto> findByPrecioLessThanEqual(Double precioMax);

    // Buscar por categoría Y precio máximo
    List<Producto> findByCategoriaAndPrecioLessThanEqual(String categoria, Double precioMax);
}
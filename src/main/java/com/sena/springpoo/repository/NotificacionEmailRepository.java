package com.sena.springpoo.repository;

import com.sena.springpoo.models.NotificacionEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificacionEmailRepository extends JpaRepository<NotificacionEmail, Long> {
    List<NotificacionEmail> findByEstado(String estado);
    List<NotificacionEmail> findByProductoId(Long productoId);
}
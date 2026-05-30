package notificacion_service.repository;

import notificacion_service.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.estado = 'PENDIENTE' AND " +
           "(n.fechaProximoIntento IS NULL OR n.fechaProximoIntento <= :now) " +
           "ORDER BY CASE n.prioridad " +
           "  WHEN 'URGENTE' THEN 1 " +
           "  WHEN 'NORMAL' THEN 2 " +
           "  WHEN 'BAJA' THEN 3 " +
           "  ELSE 4 END ASC, n.fechaCreacion ASC")
    List<Notification> findPendingNotificationsToProcess(@Param("now") LocalDateTime now);

    List<Notification> findByUsuarioDestinatarioAndEstadoNot(Long usuarioDestinatario, String estado);
}

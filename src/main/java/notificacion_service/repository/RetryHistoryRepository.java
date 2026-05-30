package notificacion_service.repository;

import notificacion_service.model.RetryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RetryHistoryRepository extends JpaRepository<RetryHistory, Long> {
    List<RetryHistory> findByNotificationId(Long notificationId);
}

package notificacion_service.controller;

import notificacion_service.model.*;
import notificacion_service.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping
    public ResponseEntity<?> sendDirectNotification(@RequestBody NotificationDirectRequest request) {
        try {
            Notification notification = notificationService.createDirectNotification(request);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear notificación directa: " + e.getMessage());
        }
    }

    @PostMapping("/template")
    public ResponseEntity<?> sendTemplateNotification(@RequestBody NotificationTemplateRequest request) {
        try {
            Notification notification = notificationService.createTemplateNotification(request);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear notificación por plantilla: " + e.getMessage());
        }
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> sendBulkNotification(@RequestBody BulkNotificationRequest request) {
        try {
            List<Notification> notifications = notificationService.createBulkNotification(request);
            return ResponseEntity.ok("Registradas " + notifications.size() + " notificaciones en lote con éxito");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear envío masivo: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            Notification notification = notificationService.markAsRead(id);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al marcar como leída: " + e.getMessage());
        }
    }

    @GetMapping("/unread/{usuarioId}")
    public ResponseEntity<?> getUnreadNotifications(@PathVariable Long usuarioId) {
        try {
            List<Notification> unread = notificationService.getUnreadNotificationsForUser(usuarioId);
            return ResponseEntity.ok(unread);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al obtener notificaciones no leídas: " + e.getMessage());
        }
    }
}

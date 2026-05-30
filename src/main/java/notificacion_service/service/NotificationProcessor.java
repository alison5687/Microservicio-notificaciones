package notificacion_service.service;

import notificacion_service.model.Notification;
import notificacion_service.model.RetryHistory;
import notificacion_service.model.UserPreference;
import notificacion_service.repository.NotificationRepository;
import notificacion_service.repository.RetryHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class NotificationProcessor {

    private static final Logger logger = LoggerFactory.getLogger(NotificationProcessor.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private RetryHistoryRepository retryHistoryRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private AuditServiceClient auditServiceClient;

    @Autowired
    @Qualifier("EMAIL_sender")
    private NotificationSender emailSender;

    @Autowired
    @Qualifier("SMS_sender")
    private NotificationSender smsSender;

    @Autowired
    @Qualifier("PUSH_sender")
    private NotificationSender pushSender;

    @Autowired
    @Qualifier("INTERNAL_sender")
    private NotificationSender internalSender;

    private static final int BASE_BACKOFF_SECONDS = 5; // Base interval for exponential backoff

    @Scheduled(fixedDelay = 5000) // Executes every 5 seconds
    public void processPendingNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<Notification> pending = notificationRepository.findPendingNotificationsToProcess(now);

        if (pending.isEmpty()) {
            return;
        }

        logger.info("[PROCESSOR] Encontradas {} notificaciones pendientes para procesar", pending.size());

        for (Notification notification : pending) {
            try {
                processSingleNotification(notification);
            } catch (Exception e) {
                logger.error("[PROCESSOR] [CRITICAL-ERROR] Error al procesar notificación ID {}: {}", 
                        notification.getId(), e.getMessage());
            }
        }
    }

    private void processSingleNotification(Notification notification) {
        Long userId = notification.getUsuarioDestinatario();
        
        // 1. Obtener preferencias de usuario
        UserPreference prefs = userServiceClient.getUserPreferences(userId);

        // 2. Si el usuario tiene las notificaciones desactivadas a nivel general, marcamos como FALLIDA directamente
        if (!prefs.isActivo()) {
            logger.warn("[PROCESSOR] Notificación ID {} cancelada: El usuario {} tiene notificaciones inactivas", 
                    notification.getId(), userId);
            notification.setEstado("FALLIDA");
            notification.setFechaProximoIntento(null);
            notificationRepository.save(notification);
            
            auditServiceClient.sendAuditLog("ENVIAR_NOTIFICACION", 
                    "Cancelada: usuario " + userId + " tiene notificaciones inactivas", 
                    notification.getTrackingId(), "FAILURE");
            return;
        }

        // 3. Verificar Horario de No Molestar (DND) si la prioridad no es URGENTE
        if (!"URGENTE".equalsIgnoreCase(notification.getPrioridad())) {
            if (isInsideDndWindow(prefs.getHoraInicioNoMolestar(), prefs.getHoraFinNoMolestar())) {
                logger.info("[PROCESSOR] Notificación ID {} (Prioridad: {}) para usuario {} omitida temporalmente debido al Horario de No Molestar ({} - {})",
                        notification.getId(), notification.getPrioridad(), userId, 
                        prefs.getHoraInicioNoMolestar(), prefs.getHoraFinNoMolestar());
                // Sigue en PENDIENTE y se volverá a procesar en el próximo ciclo
                return;
            }
        }

        // 4. Determinar datos de contacto según el canal
        String canal = notification.getCanal();
        String targetContact = "";
        NotificationSender sender = null;

        if ("EMAIL".equalsIgnoreCase(canal)) {
            targetContact = userServiceClient.getUserEmail(userId);
            sender = emailSender;
        } else if ("SMS".equalsIgnoreCase(canal)) {
            targetContact = userServiceClient.getUserMobilePhone(userId);
            sender = smsSender;
        } else if ("PUSH".equalsIgnoreCase(canal)) {
            targetContact = userId.toString();
            sender = pushSender;
        } else if ("INTERNAL".equalsIgnoreCase(canal)) {
            targetContact = userId.toString();
            sender = internalSender;
        } else {
            logger.error("[PROCESSOR] Canal '{}' desconocido para notificación ID {}", canal, notification.getId());
            notification.setEstado("FALLIDA");
            notification.setFechaProximoIntento(null);
            notificationRepository.save(notification);
            
            auditServiceClient.sendAuditLog("ENVIAR_NOTIFICACION", 
                    "Fallo: canal desconocido " + canal, 
                    notification.getTrackingId(), "FAILURE");
            return;
        }

        // 5. Incrementar intentos realizados
        int intentoActual = notification.getIntentosRealizados() + 1;
        notification.setIntentosRealizados(intentoActual);

        // 6. Realizar el envío simulado
        try {
            sender.send(notification, targetContact);
            
            // Envío Exitoso
            notification.setEstado("ENVIADA");
            notification.setFechaEnvio(LocalDateTime.now());
            notification.setFechaProximoIntento(null);
            notificationRepository.save(notification);

            // Guardar Historial de Reintentos
            saveRetryHistory(notification, intentoActual, "EXITO", "Envío simulado completado con éxito");
            
            // Auditoría asíncrona
            auditServiceClient.sendAuditLog("ENVIAR_NOTIFICACION", 
                    "Notificación enviada con éxito a través de " + canal + " a " + targetContact, 
                    notification.getTrackingId(), "SUCCESS");

        } catch (Exception e) {
            // Envío Fallido
            logger.warn("[PROCESSOR] Error al enviar notificación ID {} en el intento {}: {}", 
                    notification.getId(), intentoActual, e.getMessage());

            saveRetryHistory(notification, intentoActual, "FALLO", e.getMessage());

            if (intentoActual >= notification.getIntentosMaximos()) {
                // Se agotaron los intentos
                notification.setEstado("FALLIDA");
                notification.setFechaProximoIntento(null);
                notificationRepository.save(notification);
                
                logger.error("[PROCESSOR] Se alcanzó el número máximo de intentos ({}) para la notificación ID {}", 
                        notification.getIntentosMaximos(), notification.getId());

                auditServiceClient.sendAuditLog("ENVIAR_NOTIFICACION", 
                        "Fallo definitivo tras " + intentoActual + " intentos: " + e.getMessage(), 
                        notification.getTrackingId(), "FAILURE");
            } else {
                // Programar reintento con backoff exponencial: base_seconds * 2^intentos
                int backoffSeconds = BASE_BACKOFF_SECONDS * (1 << intentoActual);
                notification.setFechaProximoIntento(LocalDateTime.now().plusSeconds(backoffSeconds));
                notificationRepository.save(notification);
                
                logger.info("[PROCESSOR] Reintento programado para notificación ID {} en {} segundos (fecha: {})", 
                        notification.getId(), backoffSeconds, notification.getFechaProximoIntento());

                auditServiceClient.sendAuditLog("ENVIAR_NOTIFICACION", 
                        "Fallo temporal en intento " + intentoActual + ". Programado reintento en " + backoffSeconds + "s: " + e.getMessage(), 
                        notification.getTrackingId(), "FAILURE");
            }
        }
    }

    private boolean isInsideDndWindow(LocalTime inicio, LocalTime fin) {
        if (inicio == null || fin == null) {
            return false;
        }

        LocalTime now = LocalTime.now();
        if (inicio.isBefore(fin)) {
            // Caso regular: e.g. 22:00 a 06:00 (espera, 22:00 a 06:00 cruza medianoche, así que inicio > fin)
            // Caso regular: e.g. 13:00 a 15:00
            return now.isAfter(inicio) && now.isBefore(fin);
        } else {
            // Caso cruce de medianoche: e.g. 22:00 a 06:00
            return now.isAfter(inicio) || now.isBefore(fin);
        }
    }

    private void saveRetryHistory(Notification notification, int intento, String resultado, String errorDetalle) {
        try {
            RetryHistory history = new RetryHistory();
            history.setNotification(notification);
            history.setIntento(intento);
            history.setResultado(resultado);
            history.setErrorDetalle(errorDetalle);
            retryHistoryRepository.save(history);
        } catch (Exception e) {
            logger.error("[PROCESSOR] Error guardando historial de reintentos: {}", e.getMessage());
        }
    }
}

package notificacion_service.service;

import notificacion_service.model.*;
import notificacion_service.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationTemplateService templateService;

    @Autowired
    private AuditServiceClient auditServiceClient;

    @Autowired
    private UserServiceClient userServiceClient;

    public Notification createDirectNotification(NotificationDirectRequest request) {
        Notification notification = new Notification();
        
        String canal = request.getCanal();
        
        if (request.getDestinatarioDirecto() != null && !request.getDestinatarioDirecto().isEmpty()) {
            notification.setDestinatarioDirecto(request.getDestinatarioDirecto());
            notification.setUsuarioDestinatario(null);
            if (canal == null || canal.isEmpty()) {
                canal = "EMAIL"; // Canal por defecto para envíos directos
            }
        } else {
            notification.setUsuarioDestinatario(request.getUsuarioDestinatario());
            // Si no se define el canal en el request, obtener el canal preferido del usuario
            if (canal == null || canal.isEmpty()) {
                UserPreference prefs = userServiceClient.getUserPreferences(request.getUsuarioDestinatario());
                canal = prefs.getCanalPreferido();
            }
        }
        
        notification.setCanal(canal.toUpperCase());
        notification.setAsunto(request.getAsunto());
        notification.setMensaje(request.getMensaje());
        
        String prioridad = request.getPrioridad() != null ? request.getPrioridad().toUpperCase() : "NORMAL";
        notification.setPrioridad(prioridad);
        notification.setEstado("PENDIENTE");
        
        String trackingId = request.getTrackingId() != null ? request.getTrackingId() : UUID.randomUUID().toString();
        notification.setTrackingId(trackingId);
        
        int maxAttempts = request.getIntentosMaximos() != null ? request.getIntentosMaximos() : 3;
        notification.setIntentosMaximos(maxAttempts);

        Notification saved = notificationRepository.save(notification);
        
        if (saved.getUsuarioDestinatario() != null) {
            logger.info("[SERVICE] Creada notificación directa ID {} para usuario {} (Prioridad: {}, Canal: {})", 
                    saved.getId(), saved.getUsuarioDestinatario(), saved.getPrioridad(), saved.getCanal());

            auditServiceClient.sendAuditLog("CREAR_NOTIFICACION", 
                    "Notificación directa registrada para usuario " + saved.getUsuarioDestinatario() + " por canal " + saved.getCanal(), 
                    saved.getTrackingId(), "SUCCESS");
        } else {
            logger.info("[SERVICE] Creada notificación directa ID {} para destinatario {} (Prioridad: {}, Canal: {})", 
                    saved.getId(), saved.getDestinatarioDirecto(), saved.getPrioridad(), saved.getCanal());

            auditServiceClient.sendAuditLog("CREAR_NOTIFICACION", 
                    "Notificación directa registrada para destinatario " + saved.getDestinatarioDirecto() + " por canal " + saved.getCanal(), 
                    saved.getTrackingId(), "SUCCESS");
        }

        return saved;
    }

    public Notification createTemplateNotification(NotificationTemplateRequest request) {
        NotificationTemplate template = templateService.getTemplateByNombre(request.getNombrePlantilla())
                .orElseThrow(() -> new RuntimeException("Plantilla no encontrada con nombre: " + request.getNombrePlantilla()));

        if (!template.isEstado()) {
            throw new RuntimeException("La plantilla '" + request.getNombrePlantilla() + "' está desactivada");
        }

        // Validar y reemplazar variables
        Map<String, String> variables = request.getVariables() != null ? request.getVariables() : new HashMap<>();
        validateRequiredVariables(template, variables);

        String interpolatedSubject = interpolateString(template.getPlantillaAsunto(), variables);
        String interpolatedMessage = interpolateString(template.getPlantillaMensaje(), variables);

        Notification notification = new Notification();
        notification.setUsuarioDestinatario(request.getUsuarioDestinatario());
        notification.setCanal(template.getCanal().toUpperCase());
        notification.setAsunto(interpolatedSubject);
        notification.setMensaje(interpolatedMessage);
        
        String prioridad = request.getPrioridad() != null ? request.getPrioridad().toUpperCase() : "NORMAL";
        notification.setPrioridad(prioridad);
        notification.setEstado("PENDIENTE");
        
        String trackingId = request.getTrackingId() != null ? request.getTrackingId() : UUID.randomUUID().toString();
        notification.setTrackingId(trackingId);
        
        int maxAttempts = request.getIntentosMaximos() != null ? request.getIntentosMaximos() : 3;
        notification.setIntentosMaximos(maxAttempts);

        Notification saved = notificationRepository.save(notification);
        logger.info("[SERVICE] Creada notificación con plantilla '{}' ID {} para usuario {} (Canal: {})", 
                template.getNombre(), saved.getId(), saved.getUsuarioDestinatario(), saved.getCanal());

        auditServiceClient.sendAuditLog("CREAR_NOTIFICACION", 
                "Notificación registrada usando plantilla '" + template.getNombre() + "' para usuario " + saved.getUsuarioDestinatario(), 
                saved.getTrackingId(), "SUCCESS");

        return saved;
    }

    @Transactional
    public List<Notification> createBulkNotification(BulkNotificationRequest request) {
        List<Notification> createdList = new ArrayList<>();
        String trackingId = request.getTrackingId() != null ? request.getTrackingId() : UUID.randomUUID().toString();

        logger.info("[SERVICE] Iniciando envío masivo para {} usuarios", request.getUsuariosDestinatarios().size());

        for (Long usuarioId : request.getUsuariosDestinatarios()) {
            try {
                Notification notification;
                if (request.getNombrePlantilla() != null && !request.getNombrePlantilla().isEmpty()) {
                    NotificationTemplateRequest templateReq = new NotificationTemplateRequest();
                    templateReq.setUsuarioDestinatario(usuarioId);
                    templateReq.setNombrePlantilla(request.getNombrePlantilla());
                    templateReq.setVariables(request.getVariables());
                    templateReq.setPrioridad(request.getPrioridad());
                    templateReq.setTrackingId(trackingId);
                    templateReq.setIntentosMaximos(request.getIntentosMaximos());
                    
                    notification = createTemplateNotification(templateReq);
                } else {
                    NotificationDirectRequest directReq = new NotificationDirectRequest();
                    directReq.setUsuarioDestinatario(usuarioId);
                    directReq.setCanal(request.getCanal());
                    directReq.setAsunto(request.getAsunto());
                    directReq.setMensaje(request.getMensaje());
                    directReq.setPrioridad(request.getPrioridad());
                    directReq.setTrackingId(trackingId);
                    directReq.setIntentosMaximos(request.getIntentosMaximos());

                    notification = createDirectNotification(directReq);
                }
                createdList.add(notification);
            } catch (Exception e) {
                logger.error("[SERVICE] Error al generar notificación masiva para usuario {}: {}", usuarioId, e.getMessage());
                // Continuamos con el resto de usuarios en caso de fallo individual
            }
        }

        auditServiceClient.sendAuditLog("ENVIO_MASIVO", 
                "Envío masivo registrado para " + createdList.size() + " usuarios exitosamente de un total de " + request.getUsuariosDestinatarios().size(), 
                trackingId, "SUCCESS");

        return createdList;
    }

    public Notification markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada con ID: " + id));

        notification.setEstado("LEIDA");
        notification.setFechaLectura(LocalDateTime.now());
        Notification saved = notificationRepository.save(notification);

        logger.info("[SERVICE] Notificación ID {} marcada como LEÍDA", id);

        auditServiceClient.sendAuditLog("MARCAR_LEIDA", 
                "Notificación ID " + id + " marcada como leída", 
                saved.getTrackingId(), "SUCCESS");

        return saved;
    }

    public List<Notification> getUnreadNotificationsForUser(Long usuarioId) {
        return notificationRepository.findByUsuarioDestinatarioAndEstadoNot(usuarioId, "LEIDA");
    }

    private void validateRequiredVariables(NotificationTemplate template, Map<String, String> variables) {
        if (template.getVariablesRequeridas() == null || template.getVariablesRequeridas().isEmpty()) {
            return;
        }

        String[] required = template.getVariablesRequeridas().split(",");
        List<String> missing = new ArrayList<>();
        for (String var : required) {
            String trimmed = var.trim();
            if (!trimmed.isEmpty() && (!variables.containsKey(trimmed) || variables.get(trimmed) == null)) {
                missing.add(trimmed);
            }
        }

        if (!missing.isEmpty()) {
            throw new RuntimeException("Faltan variables requeridas por la plantilla '" + template.getNombre() + "': " + missing);
        }
    }

    private String interpolateString(String templateStr, Map<String, String> variables) {
        if (templateStr == null) {
            return "";
        }
        String result = templateStr;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue() != null ? entry.getValue() : "");
        }
        return result;
    }
}

package notificacion_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuditServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(AuditServiceClient.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${services.auditoria.url:http://localhost:8081/api/v1}")
    private String auditoriaServiceUrl;

    @Async
    public void sendAuditLog(String operation, String details, String trackingId, String status) {
        logger.info("[AUDIT-LOG] [ASYNC-START] Operación: '{}', Detalles: '{}', TrackingId: '{}', Estado: '{}'", 
                operation, details, trackingId, status);
        
        try {
            String url = auditoriaServiceUrl + "/logs";
            
            Map<String, Object> body = new HashMap<>();
            body.put("timestamp", LocalDateTime.now().toString());
            body.put("operacion", operation);
            body.put("detalles", details);
            body.put("tracking_id", trackingId);
            body.put("estado", status);
            body.put("servicio_origen", "ms-notificaciones");

            // Realizar llamada REST POST
            // Nota: Dado que ms-auditoria puede no estar disponible, esto fallará en la mayoría de los casos locales
            // pero al ser asíncrono no retrasará la petición principal ni interrumpirá el flujo.
            restTemplate.postForObject(url, body, Map.class);
            logger.info("[AUDIT-LOG] [SUCCESS] Registro enviado exitosamente a ms-auditoria");
        } catch (Exception e) {
            logger.warn("[AUDIT-LOG] [SIMULADO] ms-auditoria no disponible ({}). Log registrado localmente en consola.", e.getMessage());
        }
    }
}

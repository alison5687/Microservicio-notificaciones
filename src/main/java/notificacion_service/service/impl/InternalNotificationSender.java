package notificacion_service.service.impl;

import notificacion_service.model.Notification;
import notificacion_service.service.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("INTERNAL_sender")
public class InternalNotificationSender implements NotificationSender {

    private static final Logger logger = LoggerFactory.getLogger(InternalNotificationSender.class);

    @Value("${simulation.send-failure-rate:0.0}")
    private double failureRate;

    @Override
    public void send(Notification notification, String recipientContact) throws Exception {
        logger.info("[SENDER] [INTERNAL] Registrando notificación interna para usuario '{}'", recipientContact);
        
        Thread.sleep(100);

        if (Math.random() < failureRate) {
            logger.warn("[SENDER] [INTERNAL] [ERROR] Falló el registro de notificación interna para '{}'", recipientContact);
            throw new RuntimeException("Error simulado de base de datos interna para el canal INTERNAL");
        }

        logger.info("[SENDER] [INTERNAL] [SUCCESS] Notificación interna registrada exitosamente para '{}' (Mensaje: {})", recipientContact, notification.getMensaje());
    }
}

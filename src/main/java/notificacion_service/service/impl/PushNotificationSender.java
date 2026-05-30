package notificacion_service.service.impl;

import notificacion_service.model.Notification;
import notificacion_service.service.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("PUSH_sender")
public class PushNotificationSender implements NotificationSender {

    private static final Logger logger = LoggerFactory.getLogger(PushNotificationSender.class);

    @Value("${simulation.send-failure-rate:0.0}")
    private double failureRate;

    @Override
    public void send(Notification notification, String recipientContact) throws Exception {
        logger.info("[SENDER] [PUSH] Iniciando envío de notificación Push a usuario con identificador '{}'", recipientContact);
        
        Thread.sleep(400);

        if (Math.random() < failureRate) {
            logger.warn("[SENDER] [PUSH] [ERROR] Falló la simulación de conexión APNS/FCM para '{}'", recipientContact);
            throw new RuntimeException("Error simulado de pasarela Push para el canal PUSH");
        }

        logger.info("[SENDER] [PUSH] [SUCCESS] Notificación Push enviada exitosamente a '{}' (Mensaje: {})", recipientContact, notification.getMensaje());
    }
}

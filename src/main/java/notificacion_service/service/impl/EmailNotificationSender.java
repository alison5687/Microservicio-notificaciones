package notificacion_service.service.impl;

import notificacion_service.model.Notification;
import notificacion_service.service.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("EMAIL_sender")
public class EmailNotificationSender implements NotificationSender {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationSender.class);

    @Value("${simulation.send-failure-rate:0.0}")
    private double failureRate;

    @Override
    public void send(Notification notification, String recipientContact) throws Exception {
        logger.info("[SENDER] [EMAIL] Iniciando envío de correo a '{}'. Asunto: '{}'", recipientContact, notification.getAsunto());
        
        // Simular latencia de red
        Thread.sleep(500);

        if (Math.random() < failureRate) {
            logger.warn("[SENDER] [EMAIL] [ERROR] Falló la simulación de conexión SMTP para '{}'", recipientContact);
            throw new RuntimeException("Error simulado de conexión SMTP para el canal EMAIL");
        }

        logger.info("[SENDER] [EMAIL] [SUCCESS] Correo enviado exitosamente a '{}' (Mensaje: {})", recipientContact, notification.getMensaje());
    }
}

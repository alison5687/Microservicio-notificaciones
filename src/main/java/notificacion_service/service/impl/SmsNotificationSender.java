package notificacion_service.service.impl;

import notificacion_service.model.Notification;
import notificacion_service.service.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("SMS_sender")
public class SmsNotificationSender implements NotificationSender {

    private static final Logger logger = LoggerFactory.getLogger(SmsNotificationSender.class);

    @Value("${simulation.send-failure-rate:0.0}")
    private double failureRate;

    @Override
    public void send(Notification notification, String recipientContact) throws Exception {
        logger.info("[SENDER] [SMS] Iniciando envío de mensaje de texto a '{}'", recipientContact);
        
        Thread.sleep(300);

        if (Math.random() < failureRate) {
            logger.warn("[SENDER] [SMS] [ERROR] Falló la simulación de red celular para SMS a '{}'", recipientContact);
            throw new RuntimeException("Error simulado de red celular para el canal SMS");
        }

        logger.info("[SENDER] [SMS] [SUCCESS] SMS enviado exitosamente a '{}' (Mensaje: {})", recipientContact, notification.getMensaje());
    }
}

package notificacion_service.service.impl;

import notificacion_service.model.Notification;
import notificacion_service.service.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service("EMAIL_sender")
public class EmailNotificationSender implements NotificationSender {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationSender.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${simulation.send-failure-rate:0.0}")
    private double failureRate;

    @Override
    public void send(Notification notification, String recipientContact) throws Exception {
        logger.info("[SENDER] [EMAIL] Iniciando envío de correo real a '{}'. Asunto: '{}'", recipientContact, notification.getAsunto());
        
        // Mantener simulación opcional si failureRate > 0
        if (failureRate > 0 && Math.random() < failureRate) {
            logger.warn("[SENDER] [EMAIL] [SIMULATED-ERROR] Falló la simulación de conexión SMTP para '{}'", recipientContact);
            throw new RuntimeException("Error simulado de conexión SMTP para el canal EMAIL");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientContact);
            message.setSubject(notification.getAsunto());
            message.setText(notification.getMensaje());
            
            mailSender.send(message);
            
            logger.info("[SENDER] [EMAIL] [SUCCESS] Correo enviado exitosamente a '{}'", recipientContact);
        } catch (Exception e) {
            logger.error("[SENDER] [EMAIL] [ERROR] Error al enviar correo a '{}': {}", recipientContact, e.getMessage());
            throw e; // Lanzar para activar el mecanismo de reintentos
        }
    }
}

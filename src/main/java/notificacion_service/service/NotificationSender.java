package notificacion_service.service;

import notificacion_service.model.Notification;

public interface NotificationSender {
    void send(Notification notification, String recipientContact) throws Exception;
}

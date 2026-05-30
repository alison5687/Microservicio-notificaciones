package notificacion_service;

import notificacion_service.model.*;
import notificacion_service.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationTemplateService templateService;

    @MockBean
    private UserServiceClient userServiceClient;

    @MockBean
    private AuditServiceClient auditServiceClient;

    @Test
    public void testCreateDirectNotification() {
        NotificationDirectRequest request = new NotificationDirectRequest();
        request.setUsuarioDestinatario(1L);
        request.setCanal("EMAIL");
        request.setAsunto("Direct Subject");
        request.setMensaje("Direct Msg");
        request.setPrioridad("URGENTE");

        Notification notification = notificationService.createDirectNotification(request);

        assertNotNull(notification.getId());
        assertEquals("EMAIL", notification.getCanal());
        assertEquals("Direct Subject", notification.getAsunto());
        assertEquals("Direct Msg", notification.getMensaje());
        assertEquals("PENDIENTE", notification.getEstado());
        assertEquals("URGENTE", notification.getPrioridad());
    }

    @Test
    public void testCreateTemplateNotification() {
        // Crear una plantilla
        NotificationTemplate template = new NotificationTemplate();
        template.setNombre("bienvenida_test");
        template.setCanal("EMAIL");
        template.setPlantillaAsunto("Bienvenido {{nombre}}");
        template.setPlantillaMensaje("Hola {{nombre}}, tu código es {{codigo}}");
        template.setVariablesRequeridas("nombre,codigo");
        template.setEstado(true);
        
        try {
            templateService.createTemplate(template);
        } catch (Exception e) {
            // Ya puede existir si corre varias veces en DB persistente
        }

        NotificationTemplateRequest request = new NotificationTemplateRequest();
        request.setUsuarioDestinatario(2L);
        request.setNombrePlantilla("bienvenida_test");
        request.setPrioridad("NORMAL");
        
        Map<String, String> vars = new HashMap<>();
        vars.put("nombre", "Alison");
        vars.put("codigo", "9876");
        request.setVariables(vars);

        Notification notification = notificationService.createTemplateNotification(request);

        assertNotNull(notification.getId());
        assertEquals("EMAIL", notification.getCanal());
        assertEquals("Bienvenido Alison", notification.getAsunto());
        assertEquals("Hola Alison, tu código es 9876", notification.getMensaje());
        assertEquals("PENDIENTE", notification.getEstado());
    }
}

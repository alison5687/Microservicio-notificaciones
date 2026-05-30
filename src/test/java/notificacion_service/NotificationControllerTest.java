package notificacion_service;

import notificacion_service.model.Notification;
import notificacion_service.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    public void setup() {
        notificationRepository.deleteAll();
    }

    @Test
    public void testSendDirectNotification() throws Exception {
        String requestJson = "{\n" +
                "  \"usuarioDestinatario\": 1,\n" +
                "  \"canal\": \"EMAIL\",\n" +
                "  \"asunto\": \"Prueba Directa\",\n" +
                "  \"mensaje\": \"Hola esto es un mensaje de prueba\",\n" +
                "  \"prioridad\": \"URGENTE\",\n" +
                "  \"trackingId\": \"TRK-101\"\n" +
                "}";

        mockMvc.perform(post("/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioDestinatario").value(1))
                .andExpect(jsonPath("$.canal").value("EMAIL"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.prioridad").value("URGENTE"))
                .andExpect(jsonPath("$.trackingId").value("TRK-101"));
    }

    @Test
    public void testGetUnreadNotifications() throws Exception {
        // Guardar notificación de prueba
        Notification notification = new Notification();
        notification.setUsuarioDestinatario(12L);
        notification.setCanal("SMS");
        notification.setAsunto(null);
        notification.setMensaje("Mensaje no leído");
        notification.setPrioridad("NORMAL");
        notification.setEstado("ENVIADA");
        notification.setTrackingId("TRK-102");
        notificationRepository.save(notification);

        mockMvc.perform(get("/notifications/unread/12")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuarioDestinatario").value(12))
                .andExpect(jsonPath("$[0].estado").value("ENVIADA"))
                .andExpect(jsonPath("$[0].mensaje").value("Mensaje no leído"));
    }
}

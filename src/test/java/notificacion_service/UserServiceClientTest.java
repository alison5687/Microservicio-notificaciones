package notificacion_service;

import notificacion_service.model.UserPreference;
import notificacion_service.repository.UserPreferenceRepository;
import notificacion_service.service.UserServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UserServiceClientTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private UserPreferenceRepository userPreferenceRepository;
    private UserServiceClient userServiceClient;

    @BeforeEach
    void setup() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        userPreferenceRepository = Mockito.mock(UserPreferenceRepository.class);
        userServiceClient = new UserServiceClient(restTemplate, userPreferenceRepository);
        ReflectionTestUtils.setField(userServiceClient, "usuariosServiceUrl", "http://localhost:8000/api/v1");
    }

    @Test
    void getUserEmail_returnsEmailFromMsUsuarios() {
        server.expect(requestTo("http://localhost:8000/api/v1/usuarios/1"))
                .andRespond(withSuccess(
                        "{\"request_id\":\"req-123\",\"success\":true,\"data\":{\"id\":1,\"username\":\"usuario1\",\"email\":\"usuario1@example.com\"},\"message\":\"Usuario obtenido exitosamente\"}",
                        MediaType.APPLICATION_JSON));

        String email = userServiceClient.getUserEmail(1L);

        assertThat(email).isEqualTo("usuario1@example.com");
        server.verify();
    }

    @Test
    void getUserPreferences_returnsPreferencesFromMsUsuarios() {
        server.expect(requestTo("http://localhost:8000/api/v1/preferencias/1"))
                .andRespond(withSuccess(
                        "{\"request_id\":\"req-456\",\"success\":true,\"data\":{\"id\":10,\"usuario_id\":1,\"notif_email\":true,\"notif_sms\":false,\"notif_push\":true,\"canal_preferido\":\"EMAIL\",\"horario_no_molestar_inicio\":\"22:00:00\",\"horario_no_molestar_fin\":\"06:00:00\",\"created_at\":\"2026-06-01T10:00:00\",\"updated_at\":\"2026-06-01T10:00:00\"},\"message\":\"Preferencias obtenidas exitosamente\"}",
                        MediaType.APPLICATION_JSON));

        UserPreference preferences = userServiceClient.getUserPreferences(1L);

        assertThat(preferences.getUsuario()).isEqualTo(1L);
        assertThat(preferences.getCanalPreferido()).isEqualTo("EMAIL");
        assertThat(preferences.isActivo()).isTrue();
        server.verify();
    }
}

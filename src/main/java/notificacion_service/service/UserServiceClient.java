package notificacion_service.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import notificacion_service.model.UserPreference;
import notificacion_service.repository.UserPreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Service
public class UserServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${services.usuarios.url:http://localhost:8000/api/v1}")
    private String usuariosServiceUrl;

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    public static class UserDto {
        private Long id;
        private String username;
        private String email;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class ProfileDto {
        @JsonProperty("usuario_id")
        private Long usuarioId;
        @JsonProperty("telefono_movil")
        private String telefonoMovil;

        public Long getUsuarioId() { return usuarioId; }
        public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
        public String getTelefonoMovil() { return telefonoMovil; }
        public void setTelefonoMovil(String telefonoMovil) { this.telefonoMovil = telefonoMovil; }
    }

    public static class PreferencesDto {
        @JsonProperty("notif_email")
        private boolean notifEmail;
        @JsonProperty("notif_sms")
        private boolean notifSms;
        @JsonProperty("notif_push")
        private boolean notifPush;
        @JsonProperty("canal_preferido")
        private String canalPreferido;
        @JsonProperty("horario_no_molestar_inicio")
        private String horarioNoMolestarInicio;
        @JsonProperty("horario_no_molestar_fin")
        private String horarioNoMolestarFin;

        public boolean isNotifEmail() { return notifEmail; }
        public void setNotifEmail(boolean notifEmail) { this.notifEmail = notifEmail; }
        public boolean isNotifSms() { return notifSms; }
        public void setNotifSms(boolean notifSms) { this.notifSms = notifSms; }
        public boolean isNotifPush() { return notifPush; }
        public void setNotifPush(boolean notifPush) { this.notifPush = notifPush; }
        public String getCanalPreferido() { return canalPreferido; }
        public void setCanalPreferido(String canalPreferido) { this.canalPreferido = canalPreferido; }
        public String getHorarioNoMolestarInicio() { return horarioNoMolestarInicio; }
        public void setHorarioNoMolestarInicio(String horarioNoMolestarInicio) { this.horarioNoMolestarInicio = horarioNoMolestarInicio; }
        public String getHorarioNoMolestarFin() { return horarioNoMolestarFin; }
        public void setHorarioNoMolestarFin(String horarioNoMolestarFin) { this.horarioNoMolestarFin = horarioNoMolestarFin; }
    }

    public static class StandardResponse<T> {
        @JsonProperty("request_id")
        private String requestId;
        private boolean success;
        private T data;
        private String message;
        private String timestamp;

        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public T getData() { return data; }
        public void setData(T data) { this.data = data; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }

    // Response wrappers for RestTemplate generic types erasure
    private static class UserResponse extends StandardResponse<UserDto> {}
    private static class ProfileResponse extends StandardResponse<ProfileDto> {}
    private static class PreferencesResponse extends StandardResponse<PreferencesDto> {}

    public String getUserEmail(Long usuarioId) {
        try {
            String url = usuariosServiceUrl + "/usuarios/" + usuarioId;
            UserResponse response = restTemplate.getForObject(url, UserResponse.class);
            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData().getEmail();
            }
        } catch (Exception e) {
            logger.warn("No se pudo obtener el email del usuario {} desde ms-usuarios: {}. Usando fallback.", usuarioId, e.getMessage());
        }
        return "usuario" + usuarioId + "@example.com"; // Fallback email
    }

    public String getUserMobilePhone(Long usuarioId) {
        try {
            String url = usuariosServiceUrl + "/perfiles/" + usuarioId;
            ProfileResponse response = restTemplate.getForObject(url, ProfileResponse.class);
            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData().getTelefonoMovil();
            }
        } catch (Exception e) {
            logger.warn("No se pudo obtener el móvil del usuario {} desde ms-usuarios: {}. Usando fallback.", usuarioId, e.getMessage());
        }
        return "+573000000000"; // Fallback phone
    }

    public UserPreference getUserPreferences(Long usuarioId) {
        // Intentar obtener de ms-usuarios primero
        try {
            String url = usuariosServiceUrl + "/preferencias/" + usuarioId;
            PreferencesResponse response = restTemplate.getForObject(url, PreferencesResponse.class);
            if (response != null && response.isSuccess() && response.getData() != null) {
                PreferencesDto dto = response.getData();
                UserPreference prefs = new UserPreference();
                prefs.setUsuario(usuarioId);
                prefs.setCanalPreferido(dto.getCanalPreferido() != null ? dto.getCanalPreferido() : "EMAIL");
                // Preferencia activa si al menos uno está activo o depende de la lógica.
                // En ms-usuarios, notif_email, notif_sms, notif_push indican por qué canal puede recibir.
                // Asumiremos activo = true si el canal preferido respectivo está activo.
                boolean activo = true;
                if ("EMAIL".equalsIgnoreCase(dto.getCanalPreferido())) activo = dto.isNotifEmail();
                else if ("SMS".equalsIgnoreCase(dto.getCanalPreferido())) activo = dto.isNotifSms();
                else if ("PUSH".equalsIgnoreCase(dto.getCanalPreferido())) activo = dto.isNotifPush();
                prefs.setActivo(activo);

                if (dto.getHorarioNoMolestarInicio() != null && !dto.getHorarioNoMolestarInicio().isEmpty()) {
                    prefs.setHoraInicioNoMolestar(parseLocalTime(dto.getHorarioNoMolestarInicio()));
                }
                if (dto.getHorarioNoMolestarFin() != null && !dto.getHorarioNoMolestarFin().isEmpty()) {
                    prefs.setHoraFinNoMolestar(parseLocalTime(dto.getHorarioNoMolestarFin()));
                }
                
                // Actualizar o guardar en caché local
                saveLocalCache(prefs);
                return prefs;
            }
        } catch (Exception e) {
            logger.warn("No se pudo obtener preferencias del usuario {} desde ms-usuarios: {}. Usando caché local o valores predeterminados.", usuarioId, e.getMessage());
        }

        // Si ms-usuarios falla, buscar en base de datos local
        Optional<UserPreference> localPrefs = userPreferenceRepository.findByUsuario(usuarioId);
        if (localPrefs.isPresent()) {
            return localPrefs.get();
        }

        // Si no hay local, retornar valores predeterminados
        UserPreference defaultPrefs = new UserPreference();
        defaultPrefs.setUsuario(usuarioId);
        defaultPrefs.setCanalPreferido("EMAIL");
        defaultPrefs.setActivo(true);
        defaultPrefs.setHoraInicioNoMolestar(null);
        defaultPrefs.setHoraFinNoMolestar(null);
        return defaultPrefs;
    }

    private void saveLocalCache(UserPreference prefs) {
        try {
            Optional<UserPreference> existing = userPreferenceRepository.findByUsuario(prefs.getUsuario());
            if (existing.isPresent()) {
                UserPreference dbPrefs = existing.get();
                dbPrefs.setCanalPreferido(prefs.getCanalPreferido());
                dbPrefs.setActivo(prefs.isActivo());
                dbPrefs.setHoraInicioNoMolestar(prefs.getHoraInicioNoMolestar());
                dbPrefs.setHoraFinNoMolestar(prefs.getHoraFinNoMolestar());
                userPreferenceRepository.save(dbPrefs);
            } else {
                userPreferenceRepository.save(prefs);
            }
        } catch (Exception e) {
            logger.warn("No se pudo guardar la preferencia en caché local: {}", e.getMessage());
        }
    }

    private LocalTime parseLocalTime(String timeStr) {
        try {
            // El formato puede ser HH:mm:ss o HH:mm
            if (timeStr.length() == 5) {
                return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
            } else {
                return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm:ss"));
            }
        } catch (Exception e) {
            logger.warn("Error parseando hora '{}', usando null", timeStr);
            return null;
        }
    }
}

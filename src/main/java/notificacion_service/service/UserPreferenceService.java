package notificacion_service.service;

import notificacion_service.model.UserPreference;
import notificacion_service.repository.UserPreferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserPreferenceService {

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @Autowired
    private AuditServiceClient auditServiceClient;

    public UserPreference getPreferencesByUsuario(Long usuarioId) {
        return userPreferenceRepository.findByUsuario(usuarioId)
                .orElseGet(() -> {
                    UserPreference defaultPrefs = new UserPreference();
                    defaultPrefs.setUsuario(usuarioId);
                    defaultPrefs.setCanalPreferido("EMAIL");
                    defaultPrefs.setActivo(true);
                    return defaultPrefs;
                });
    }

    public UserPreference saveOrUpdatePreferences(Long usuarioId, UserPreference details) {
        UserPreference prefs = userPreferenceRepository.findByUsuario(usuarioId)
                .orElseGet(() -> {
                    UserPreference p = new UserPreference();
                    p.setUsuario(usuarioId);
                    return p;
                });

        if (details.getCanalPreferido() != null) {
            prefs.setCanalPreferido(details.getCanalPreferido());
        }
        prefs.setActivo(details.isActivo());
        prefs.setHoraInicioNoMolestar(details.getHoraInicioNoMolestar());
        prefs.setHoraFinNoMolestar(details.getHoraFinNoMolestar());

        UserPreference saved = userPreferenceRepository.save(prefs);
        auditServiceClient.sendAuditLog("ACTUALIZAR_PREFERENCIAS", 
                "Preferencias actualizadas localmente para usuario " + usuarioId, 
                "SYS-PREF-" + usuarioId, "SUCCESS");
        return saved;
    }
}

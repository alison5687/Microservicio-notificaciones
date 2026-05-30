package notificacion_service.controller;

import notificacion_service.model.UserPreference;
import notificacion_service.service.UserPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/preferences")
public class UserPreferenceController {

    @Autowired
    private UserPreferenceService userPreferenceService;

    @GetMapping("/{usuarioId}")
    public ResponseEntity<UserPreference> getPreferences(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(userPreferenceService.getPreferencesByUsuario(usuarioId));
    }

    @PutMapping("/{usuarioId}")
    public ResponseEntity<?> updatePreferences(@PathVariable Long usuarioId, @RequestBody UserPreference details) {
        try {
            UserPreference updated = userPreferenceService.saveOrUpdatePreferences(usuarioId, details);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al guardar preferencias: " + e.getMessage());
        }
    }
}

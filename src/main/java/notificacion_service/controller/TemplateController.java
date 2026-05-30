package notificacion_service.controller;

import notificacion_service.model.NotificationTemplate;
import notificacion_service.service.NotificationTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/templates")
public class TemplateController {

    @Autowired
    private NotificationTemplateService templateService;

    @PostMapping
    public ResponseEntity<?> createTemplate(@RequestBody NotificationTemplate template) {
        try {
            NotificationTemplate created = templateService.createTemplate(template);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear la plantilla: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<NotificationTemplate>> getAllTemplates() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTemplateById(@PathVariable Long id) {
        return templateService.getTemplateById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{nombre}")
    public ResponseEntity<?> getTemplateByNombre(@PathVariable String nombre) {
        return templateService.getTemplateByNombre(nombre)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTemplate(@PathVariable Long id, @RequestBody NotificationTemplate details) {
        try {
            NotificationTemplate updated = templateService.updateTemplate(id, details);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar la plantilla: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateTemplate(@PathVariable Long id) {
        try {
            NotificationTemplate deactivated = templateService.deactivateTemplate(id);
            return ResponseEntity.ok(deactivated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al desactivar la plantilla: " + e.getMessage());
        }
    }
}

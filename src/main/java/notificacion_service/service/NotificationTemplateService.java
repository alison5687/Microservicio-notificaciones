package notificacion_service.service;

import notificacion_service.model.NotificationTemplate;
import notificacion_service.repository.NotificationTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationTemplateService {

    @Autowired
    private NotificationTemplateRepository templateRepository;

    @Autowired
    private AuditServiceClient auditServiceClient;

    public NotificationTemplate createTemplate(NotificationTemplate template) {
        if (templateRepository.findByNombre(template.getNombre()).isPresent()) {
            throw new RuntimeException("Ya existe una plantilla con el nombre: " + template.getNombre());
        }
        NotificationTemplate created = templateRepository.save(template);
        auditServiceClient.sendAuditLog("CREAR_PLANTILLA", 
                "Plantilla '" + created.getNombre() + "' creada con canal " + created.getCanal(), 
                "SYS-TEMP-" + created.getId(), "SUCCESS");
        return created;
    }

    public List<NotificationTemplate> getAllTemplates() {
        return templateRepository.findAll();
    }

    public Optional<NotificationTemplate> getTemplateById(Long id) {
        return templateRepository.findById(id);
    }

    public Optional<NotificationTemplate> getTemplateByNombre(String nombre) {
        return templateRepository.findByNombre(nombre);
    }

    public NotificationTemplate updateTemplate(Long id, NotificationTemplate details) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plantilla no encontrada con ID: " + id));

        // Verificar si se cambia el nombre y ya existe otro con ese nombre
        if (details.getNombre() != null && !details.getNombre().equals(template.getNombre())) {
            if (templateRepository.findByNombre(details.getNombre()).isPresent()) {
                throw new RuntimeException("Ya existe otra plantilla con el nombre: " + details.getNombre());
            }
            template.setNombre(details.getNombre());
        }

        if (details.getCanal() != null) {
            template.setCanal(details.getCanal());
        }
        if (details.getPlantillaAsunto() != null) {
            template.setPlantillaAsunto(details.getPlantillaAsunto());
        }
        if (details.getPlantillaMensaje() != null) {
            template.setPlantillaMensaje(details.getPlantillaMensaje());
        }
        if (details.getVariablesRequeridas() != null) {
            template.setVariablesRequeridas(details.getVariablesRequeridas());
        }
        template.setEstado(details.isEstado());

        NotificationTemplate updated = templateRepository.save(template);
        auditServiceClient.sendAuditLog("ACTUALIZAR_PLANTILLA", 
                "Plantilla '" + updated.getNombre() + "' actualizada", 
                "SYS-TEMP-" + updated.getId(), "SUCCESS");
        return updated;
    }

    public NotificationTemplate deactivateTemplate(Long id) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plantilla no encontrada con ID: " + id));
        template.setEstado(false);
        NotificationTemplate updated = templateRepository.save(template);
        auditServiceClient.sendAuditLog("DESACTIVAR_PLANTILLA", 
                "Plantilla '" + updated.getNombre() + "' desactivada", 
                "SYS-TEMP-" + updated.getId(), "SUCCESS");
        return updated;
    }
}

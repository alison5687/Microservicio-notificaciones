package notificacion_service.model;

import java.util.List;
import java.util.Map;

public class BulkNotificationRequest {
    private List<Long> usuariosDestinatarios;
    private String canal; // Usado para envío directo masivo
    private String asunto; // Usado para envío directo masivo
    private String mensaje; // Usado para envío directo masivo
    private String nombrePlantilla; // Usado si se envía con plantilla
    private Map<String, String> variables; // Usado si se envía con plantilla
    private String prioridad; // URGENTE, NORMAL, BAJA
    private String trackingId;
    private Integer intentosMaximos;

    // Getters y Setters
    public List<Long> getUsuariosDestinatarios() {
        return usuariosDestinatarios;
    }

    public void setUsuariosDestinatarios(List<Long> usuariosDestinatarios) {
        this.usuariosDestinatarios = usuariosDestinatarios;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getNombrePlantilla() {
        return nombrePlantilla;
    }

    public void setNombrePlantilla(String nombrePlantilla) {
        this.nombrePlantilla = nombrePlantilla;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public Integer getIntentosMaximos() {
        return intentosMaximos;
    }

    public void setIntentosMaximos(Integer intentosMaximos) {
        this.intentosMaximos = intentosMaximos;
    }
}

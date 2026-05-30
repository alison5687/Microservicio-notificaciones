package notificacion_service.model;

import java.util.Map;

public class NotificationTemplateRequest {
    private Long usuarioDestinatario;
    private String nombrePlantilla;
    private Map<String, String> variables;
    private String prioridad; // URGENTE, NORMAL, BAJA
    private String trackingId;
    private Integer intentosMaximos;

    // Getters y Setters
    public Long getUsuarioDestinatario() {
        return usuarioDestinatario;
    }

    public void setUsuarioDestinatario(Long usuarioDestinatario) {
        this.usuarioDestinatario = usuarioDestinatario;
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

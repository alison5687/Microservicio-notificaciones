package notificacion_service.model;

public class NotificationDirectRequest {
    private Long usuarioDestinatario;
    private String canal; // EMAIL, SMS, PUSH, INTERNAL
    private String asunto;
    private String mensaje;
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

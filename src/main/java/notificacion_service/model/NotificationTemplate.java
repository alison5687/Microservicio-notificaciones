package notificacion_service.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_templates")
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre;

    @Column(name = "canal", nullable = false)
    private String canal; // EMAIL, SMS, PUSH, INTERNAL

    @Column(name = "plantilla_asunto")
    private String plantillaAsunto;

    @Column(name = "plantilla_mensaje", nullable = false, length = 4000)
    private String plantillaMensaje;

    @Column(name = "variables_requeridas")
    private String variablesRequeridas; // Comma separated list (e.g. "nombre,fecha,monto")

    @Column(name = "estado", nullable = false)
    private boolean estado = true; // true = activo, false = desactivado

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public String getPlantillaAsunto() {
        return plantillaAsunto;
    }

    public void setPlantillaAsunto(String plantillaAsunto) {
        this.plantillaAsunto = plantillaAsunto;
    }

    public String getPlantillaMensaje() {
        return plantillaMensaje;
    }

    public void setPlantillaMensaje(String plantillaMensaje) {
        this.plantillaMensaje = plantillaMensaje;
    }

    public String getVariablesRequeridas() {
        return variablesRequeridas;
    }

    public void setVariablesRequeridas(String variablesRequeridas) {
        this.variablesRequeridas = variablesRequeridas;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}

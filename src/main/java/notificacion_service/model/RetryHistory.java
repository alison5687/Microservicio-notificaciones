package notificacion_service.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "retry_histories")
public class RetryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Column(name = "intento", nullable = false)
    private int intento;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "resultado", nullable = false)
    private String resultado; // EXITO, FALLO

    @Column(name = "error_detalle", length = 4000)
    private String errorDetalle;

    @PrePersist
    protected void onCreate() {
        fechaHora = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public int getIntento() {
        return intento;
    }

    public void setIntento(int intento) {
        this.intento = intento;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getErrorDetalle() {
        return errorDetalle;
    }

    public void setErrorDetalle(String errorDetalle) {
        this.errorDetalle = errorDetalle;
    }
}

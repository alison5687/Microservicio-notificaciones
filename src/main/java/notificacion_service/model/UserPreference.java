package notificacion_service.model;

import javax.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "user_preferences")
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario", nullable = false, unique = true)
    private Long usuario; // usuario ID

    @Column(name = "canal_preferido", nullable = false)
    private String canalPreferido = "EMAIL"; // EMAIL, SMS, PUSH, INTERNAL

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "hora_inicio_no_molestar")
    private LocalTime horaInicioNoMolestar;

    @Column(name = "hora_fin_no_molestar")
    private LocalTime horaFinNoMolestar;

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuario() {
        return usuario;
    }

    public void setUsuario(Long usuario) {
        this.usuario = usuario;
    }

    public String getCanalPreferido() {
        return canalPreferido;
    }

    public void setCanalPreferido(String canalPreferido) {
        this.canalPreferido = canalPreferido;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalTime getHoraInicioNoMolestar() {
        return horaInicioNoMolestar;
    }

    public void setHoraInicioNoMolestar(LocalTime horaInicioNoMolestar) {
        this.horaInicioNoMolestar = horaInicioNoMolestar;
    }

    public LocalTime getHoraFinNoMolestar() {
        return horaFinNoMolestar;
    }

    public void setHoraFinNoMolestar(LocalTime horaFinNoMolestar) {
        this.horaFinNoMolestar = horaFinNoMolestar;
    }
}

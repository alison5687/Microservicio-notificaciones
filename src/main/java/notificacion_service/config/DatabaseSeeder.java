package notificacion_service.config;

import notificacion_service.model.NotificationTemplate;
import notificacion_service.model.UserPreference;
import notificacion_service.repository.NotificationTemplateRepository;
import notificacion_service.repository.UserPreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

    @Autowired
    private NotificationTemplateRepository templateRepository;

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @Override
    public void run(String... args) throws Exception {
        logger.info("[SEEDER] Iniciando sembrado de datos en H2...");

        // 1. Sembrar plantillas por defecto si no existen
        if (templateRepository.count() == 0) {
            logger.info("[SEEDER] Creando plantilla de prueba 'bienvenida'...");
            NotificationTemplate bienvenida = new NotificationTemplate();
            bienvenida.setNombre("bienvenida");
            bienvenida.setPlantillaAsunto("¡Bienvenido a Runners, {{nombre}}!");
            bienvenida.setPlantillaMensaje("Hola {{nombre}},\n\nTu cuenta ha sido creada exitosamente en la plataforma Runners. Tu nombre de usuario es: {{username}}.\n\n¡Gracias por registrarte!");
            bienvenida.setCanal("EMAIL");
            bienvenida.setVariablesRequeridas("nombre,username");
            bienvenida.setEstado(true);
            templateRepository.save(bienvenida);
            
            logger.info("[SEEDER] Creando plantilla de prueba 'alerta_stock'...");
            NotificationTemplate alertaStock = new NotificationTemplate();
            alertaStock.setNombre("alerta_stock");
            alertaStock.setPlantillaAsunto("Alerta de Stock Mínimo: {{articulo}}");
            alertaStock.setPlantillaMensaje("Atención,\n\nEl artículo '{{articulo}}' ha alcanzado su stock mínimo de {{minimo}} unidades. Stock actual: {{actual}}.");
            alertaStock.setCanal("EMAIL");
            alertaStock.setVariablesRequeridas("articulo,minimo,actual");
            alertaStock.setEstado(true);
            templateRepository.save(alertaStock);
        } else {
            logger.info("[SEEDER] Las plantillas ya se encuentran cargadas en la base de datos.");
        }

        // 2. Sembrar preferencias de usuario por defecto
        if (userPreferenceRepository.count() == 0) {
            logger.info("[SEEDER] Creando preferencias por defecto para usuarios de prueba 1 y 2...");
            
            // Usuario 1: Recibe por EMAIL, activo, sin horario de no molestar
            UserPreference pref1 = new UserPreference();
            pref1.setUsuario(1L);
            pref1.setCanalPreferido("EMAIL");
            pref1.setActivo(true);
            pref1.setHoraInicioNoMolestar(null);
            pref1.setHoraFinNoMolestar(null);
            userPreferenceRepository.save(pref1);

            // Usuario 2: Recibe por SMS, activo, horario no molestar nocturno (22:00 a 06:00)
            UserPreference pref2 = new UserPreference();
            pref2.setUsuario(2L);
            pref2.setCanalPreferido("SMS");
            pref2.setActivo(true);
            pref2.setHoraInicioNoMolestar(LocalTime.of(22, 0));
            pref2.setHoraFinNoMolestar(LocalTime.of(6, 0));
            userPreferenceRepository.save(pref2);
            
            // Usuario 3: Inactivo (notificaciones apagadas)
            UserPreference pref3 = new UserPreference();
            pref3.setUsuario(3L);
            pref3.setCanalPreferido("EMAIL");
            pref3.setActivo(false);
            pref3.setHoraInicioNoMolestar(null);
            pref3.setHoraFinNoMolestar(null);
            userPreferenceRepository.save(pref3);
        } else {
            logger.info("[SEEDER] Las preferencias de usuario ya se encuentran cargadas en la base de datos.");
        }

        logger.info("[SEEDER] Sembrado de datos finalizado con éxito.");
    }
}

# Microservicio de Notificaciones

Este microservicio está construido en Spring Boot (Java 17) y proporciona una API REST sencilla para enviar notificaciones por correo electrónico utilizando el protocolo SMTP.

## Requisitos Previos

- Java 17
- Maven o usar el Maven Wrapper (`mvnw`) que viene incluido en este repositorio.
- **MailHog** (Para pruebas locales): El proyecto está configurado para conectarse a un servidor SMTP local en el puerto `1025`. Debes tener MailHog (o una alternativa similar) ejecutándose en tu máquina antes de iniciar el microservicio.

## Configuración

Antes de ejecutar el servicio, debes proporcionar tus credenciales reales enviando correos electrónicos.
Abre el archivo `src/main/resources/application.properties` y modifica las siguientes líneas:

```properties
spring.mail.username=tu_correo@gmail.com
spring.mail.password=tu_contraseña_o_clave_de_aplicacion
```

> **Importante acerca de Gmail:** Si usas tu cuenta de Gmail, Google ya no permite introducir tu contraseña normal de inicio de sesión. Necesitarás tener verificación en dos pasos activada y [generar una "Contraseña de aplicación"](https://support.google.com/accounts/answer/185833).

Adicionalmente, el puerto de la aplicación está definido en este mismo archivo:
```properties
server.port=8017
```

## Cómo ejecutar el proyecto

Abre una terminal en el directorio raíz del proyecto y corre el siguiente comando usando el wrapper de Maven incluido.

**En Windows:**
```bash
.\mvnw.cmd spring-boot:run
```

**En Linux / Mac:**
```bash
./mvnw spring-boot:run
```

Si deseas únicamente generar el ejecutable `.jar`, puedes usar `./mvnw clean install` (o `.mvnw.cmd clean install` en Windows PowerShell) y posteriormente ejecutarlo con `java -jar target/notificaciones-service-0.0.1-SNAPSHOT.jar`.

## Endpoints de la API

La aplicación cuenta con el siguiente endpoint de prueba para disparar un correo electrónico.

### `POST /notifications`
Envía un correo electrónico a un destinatario en particular.

**Headers:**
- `Content-Type: application/json`

**Payload:**

```json
{
  "to": "destinatario@ejemplo.com",
  "subject": "Título del Correo",
  "body": "Contenido o texto principal del mensaje"
}
```

**Ejemplo usando cURL:**

```bash
curl -X POST http://localhost:8017/notifications \
     -H "Content-Type: application/json" \
     -d "{\"to\": \"destinatario@ejemplo.com\", \"subject\": \"Test de integracion\", \"body\": \"Funcionando correctamente\"}"
```

## Integración con otros microservicios (Ejemplo en Python)

Dado que este microservicio expone una API REST estándar, puede ser consumido desde cualquier otro lenguaje. A continuación, un ejemplo de cómo enviar una notificación desde otro microservicio escrito en **Python** usando la librería `requests`.

```python
import requests

def enviar_notificacion(destinatario, asunto, mensaje):
    url = "http://localhost:8017/notifications"
    
    payload = {
        "to": destinatario,
        "subject": asunto,
        "body": mensaje
    }
    
    try:
        # requests.post con json=payload automáticamente agrega el Content-Type: application/json
        response = requests.post(url, json=payload)
        response.raise_for_status() # Verifica si hubo error HTTP
        print("Notificación enviada con éxito:", response.text)
    except requests.exceptions.RequestException as e:
        print("Error al enviar la notificación:", e)

# Ejemplo de uso:
enviar_notificacion("usuario@ejemplo.com", "Bienvenido", "Gracias por registrarte en nuestra app.")
```

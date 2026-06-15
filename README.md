# Microservicio de Notificaciones (ms-notificaciones)

Este microservicio transversal está construido en **Spring Boot (Java 17)** y actúa como una cola asíncrona de distribución de notificaciones via EMAIL para el ecosistema del ERP Universitario.

---

## 🚀 Requisitos Previos

- **Java 17** instalado en tu sistema.
- **Maven** (o usa el Maven Wrapper `mvnw.cmd` incluido).
- **MailHog** (para pruebas locales de correo): 
  - Ejecuta el archivo `MailHog.exe` en la raíz del proyecto para levantar el servidor SMTP local en el puerto `1025` y el panel web en `http://localhost:8025`.

---

## ⚡ Cómo Ejecutar el Proyecto

Abre una terminal en el directorio raíz del proyecto y ejecuta:

**En Windows (PowerShell):**
```powershell
.\mvnw.cmd spring-boot:run
```

**En Linux / Mac:**
```bash
./mvnw spring-boot:run
```

Al iniciar, un componente de inicialización (**DatabaseSeeder**) cargará de forma automática plantillas de prueba (ej: `bienvenida`) y preferencias en la base de datos **H2 en archivo persistente**, asegurando que el microservicio esté listo para ser probado de inmediato.

---

## 🧪 Abrir H2 Console y consultar la base persistente

1. Levanta el servicio:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
2. Abre en el navegador:
   ```text
   http://localhost:8017/h2-console
   ```
3. Completa los datos así:
   * **Driver Class:** `org.h2.Driver`
   * **JDBC URL:** `jdbc:h2:file:C:/Users/ASUS/Desktop/notificacion-service/data/notificacion`
   * **User Name:** `sa`
   * **Password:** (déjalo vacío)
4. Pulsa **Connect**.
5. Para ver las notificaciones guardadas, ejecuta:
   ```sql
   SELECT id, destinatario_directo, canal, asunto, estado, fecha_envio, fecha_proximo_intento
   FROM notification
   ORDER BY fecha_creacion DESC;
   ```



---

## 📌 Endpoints Clave para la Demostración

### 1. Chequeo de Salud (`GET /notifications/health`)
Verifica que el servicio y la base de datos están activos.
* **URL:** `http://localhost:8017/notifications/health`

---

### 2. Envío Directo por Correo (`POST /notifications`)
Envía una notificación directamente usando la dirección de correo electrónico del destinatario (sin necesidad de que esté registrado en el sistema).
* **URL:** `http://localhost:8017/notifications`
* **Cuerpo (JSON):**
```json
{
  "destinatarioDirecto": "alisonji5697@gmail.com",
  "canal": "EMAIL",
  "asunto": "Demo - Notificación Directa",
  "mensaje": "¡Hola! Este correo fue enviado directamente ingresando el email.",
  "prioridad": "NORMAL"
}
```
### 2.1 Envío Directo por correo

#### Windows PowerShell
```powershell
$json = '{"destinatarioDirecto":"alisonji5697@gmail.com","canal":"EMAIL","asunto":"Prueba desde curl","mensaje":"Hola, esta es una notificación enviada desde curl.","prioridad":"NORMAL"}'

curl.exe -X POST "http://localhost:8017/notifications" \
  -H "Content-Type: application/json; charset=UTF-8" \
  -d $json
```

#### Alternativa usando un archivo JSON
```powershell
@'
{
  "destinatarioDirecto": "alisonji5697@gmail.com",
  "canal": "EMAIL",
  "asunto": "Prueba desde curl",
  "mensaje": "Hola, esta es una notificación enviada desde curl.",
  "prioridad": "NORMAL"
}
'@ > payload.json

curl.exe -X POST "http://localhost:8017/notifications" \
  -H "Content-Type: application/json; charset=UTF-8" \
  --data-binary @payload.json
```

#### Linux / Mac / Git Bash (bash)
```bash
cat > payload.json <<'EOF'
{"destinatarioDirecto":"alisonji5697@gmail.com","canal":"EMAIL","asunto":"Prueba desde curl","mensaje":"Hola, esta es una notificación enviada desde curl.","prioridad":"NORMAL"}
EOF

curl -X POST "http://localhost:8017/notifications" \
  -H "Content-Type: application/json; charset=UTF-8" \
  --data-binary @payload.json
```

> Usar un archivo JSON es más seguro en terminales Windows/Git Bash porque evita problemas de codificación con caracteres especiales como acentos.

---

### 3. Envío por Plantilla Reutilizable (`POST /notifications/template`)
Genera un correo dinámico utilizando una plantilla pre-cargada.
* **URL:** `http://localhost:8017/notifications/template`
* **Cuerpo (JSON):**
```json
{
  "usuarioDestinatario": 1,
  "nombrePlantilla": "bienvenida",
  "variables": {
    "nombre": "Carlos Gómez",
    "username": "carlos.gomez"
  },
  "prioridad": "NORMAL"
}
```


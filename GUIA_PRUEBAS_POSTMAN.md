# 📋 GUÍA OFICIAL DE PRUEBAS EN POSTMAN - SISTEMA RENIEC MÓVIL UPeU

Este documento detalla paso a paso cómo probar todos los servicios web y endpoints del sistema **RENIEC Móvil** utilizando **Postman**.

---

## 📂 1. Archivo de Colección Listo para Usar

En la raíz de este proyecto ya tienes el archivo de exportación estándar v2.1 de Postman:
👉 `ReniecMovil-Supabase-Postman.postman_collection.json`

---

## 🚀 2. ¿Cómo Importar la Colección en Postman?

1. Abre la aplicación de escritorio de **Postman** (o [web.postman.co](https://web.postman.co/)).
2. Haz clic en el botón **"Import"** (ubicado en la esquina superior izquierda).
3. Selecciona la pestaña **"Files"** o arrastra el archivo:
   ```
   ReniecMovil-Supabase-Postman.postman_collection.json
   ```
4. Haz clic en **"Import"**.
5. Verás en tu panel izquierdo la colección:
   **`RENIEC Móvil UPeU - Supabase REST API (+50K Padrón, Trámites, Cargos RBAC y Auditoría)`**.

---

## ⚙️ 3. Variables de Entorno Preconfiguradas

La colección ya incluye las variables configuradas a nivel de colección:

| Variable | Valor | Descripción |
| :--- | :--- | :--- |
| `baseUrl` | `https://vktclzpkebwvfbrvtpxo.supabase.co/rest/v1` | URL base del API REST de Supabase |
| `apikey` | `sb_publishable_R4lNuJJ9kfF9LGuUlbLLgA_MkQIJQ5B` | Clave pública de autenticación y autorización |

> **Nota:** Todos los requests ya llevan automáticamente las cabeceras:
> * `apikey: {{apikey}}`
> * `Content-Type: application/json`

---

## 🧪 4. Guía de Ejecución de Pruebas por Módulos

### 📌 MÓDULO 1: Padrón Nacional (+50,000 Ciudadanos)

#### Prueba 1.1: Conteo Total del Padrón (+50K)
* **Método:** `POST`
* **URL:** `{{baseUrl}}/rpc/obtener_total_padron`
* **Resultado Esperado (Status 200 OK):**
  ```json
  50005
  ```
  *(Confirma que la base de datos supera los 50 mil ciudadanos empadronados requeridos).*

#### Prueba 1.2: Listado Paginado de Ciudadanos
* **Método:** `GET`
* **URL:** `{{baseUrl}}/ciudadanos?select=*&order=id.desc&limit=50`
* **Resultado Esperado (Status 200 OK):**
  Un arreglo JSON con los últimos 50 ciudadanos registrados, incluyendo datos biométricos.

#### Prueba 1.3: Búsqueda Exacta por DNI
* **Método:** `GET`
* **URL:** `{{baseUrl}}/ciudadanos?dni=eq.40025000&select=*`
* **Resultado Esperado (Status 200 OK):**
  Devuelve el objeto del ciudadano con DNI `40025000` con nombres, apellidos, ubigeo y estado civil.

#### Prueba 1.4: Búsqueda Avanzada por Apellido y Ubigeo
* **Método:** `GET`
* **URL:** `{{baseUrl}}/ciudadanos?apellido_paterno=ilike.*MAMANI*&ubigeo=ilike.*PUNO*&select=*&limit=25`
* **Resultado Esperado (Status 200 OK):**
  Lista de ciudadanos filtrados por patrón (ejemplo: ciudadanos de apellido Mamani en la región Puno).

#### Prueba 1.5: Empadronamiento de Nuevo Ciudadano (Biometría Facial y Huella)
* **Método:** `POST`
* **URL:** `{{baseUrl}}/ciudadanos`
* **Header:** `Prefer: return=representation`
* **Body (raw JSON):**
  ```json
  {
    "dni": "76543219",
    "nombres": "LUIS FERNANDO",
    "apellido_paterno": "QUISPE",
    "apellido_materno": "CONDORI",
    "fecha_nacimiento": "1999-08-15",
    "estado_civil": "SOLTERO",
    "ubigeo": "211101 - PUNO / SAN ROMAN / JULIACA",
    "direccion": "AV. CIRCUNVALACION 840",
    "donacion_organos": true,
    "biometria_facial": "ROSTRO_CAMARA_AFIS_99.8%",
    "biometria_huella": "HUELLA_SENSOR_INDICE_DER_OK"
  }
  ```
* **Resultado Esperado (Status 201 Created):**
  Devuelve el registro creado con su ID generado.

#### Prueba 1.6: Actualización de Biometría y Domicilio
* **Método:** `PATCH`
* **URL:** `{{baseUrl}}/ciudadanos?dni=eq.76543219`
* **Body:**
  ```json
  {
    "direccion": "JR. JAUREGUI 455 - PLAZA DE ARMAS",
    "biometria_facial": "ACTUALIZADO_BIOFACIAL_3D_99.9%"
  }
  ```
* **Resultado Esperado (Status 200 OK):** Registro actualizado.

#### Prueba 1.7: Eliminación de Ciudadano
* **Método:** `DELETE`
* **URL:** `{{baseUrl}}/ciudadanos?dni=eq.76543219`
* **Resultado Esperado (Status 204 No Content):** Ciudadano eliminado.

---

### 📌 MÓDULO 2: Trámites DNIe y Pagos Banco de la Nación

#### Prueba 2.1: Listar Trámites Activos
* **Método:** `GET`
* **URL:** `{{baseUrl}}/tramites?select=*&order=id.desc`
* **Resultado Esperado (Status 200 OK):**
  Lista de trámites de Duplicado, Renovación y Rectificación.

#### Prueba 2.2: Registrar Nuevo Trámite DNIe
* **Método:** `POST`
* **URL:** `{{baseUrl}}/tramites`
* **Body:**
  ```json
  {
    "dni_ciudadano": "40025000",
    "nombre_ciudadano": "ROSA MARIA MAMANI QUISPE",
    "tipo_tramite": "RENOVACION",
    "estado": "EN_PROCESO",
    "ubigeo_destino": "211101 - PUNO / SAN ROMAN / JULIACA",
    "observaciones": "Tributo BN 02121 pagado (S/ 30.00) - Biometría Facial y Huella verificada",
    "fecha_registro": "2026-11-08"
  }
  ```
* **Resultado Esperado (Status 201 Created).**

#### Prueba 2.3: Cambiar Estado a ATENDIDO (Entrega de DNIe)
* **Método:** `PATCH`
* **URL:** `{{baseUrl}}/tramites?dni_ciudadano=eq.40025000`
* **Body:**
  ```json
  {
    "estado": "ATENDIDO",
    "observaciones": "DNIe 3.0 entregado en ventanilla con validación de huella AFIS"
  }
  ```
* **Resultado Esperado (Status 200 OK).**

---

### 📌 MÓDULO 3: Operadores RENIEC y Control de Acceso (RBAC)

#### Prueba 3.1: Listar Operadores y Cargos
* **Método:** `GET`
* **URL:** `{{baseUrl}}/operadores_reniec?select=*&order=id.asc`
* **Resultado Esperado (Status 200 OK):** Lista de operadores (Administradores, Registradores, Certificadores).

#### Prueba 3.2: Crear Nuevo Operador con Rol Específico
* **Método:** `POST`
* **URL:** `{{baseUrl}}/operadores_reniec`
* **Body:**
  ```json
  {
    "username": "maria.flores",
    "password": "hashed_or_plain_password",
    "nombre_completo": "Lic. Maria Flores Chambi",
    "cargo": "Empadronador de Padrón y Biometría",
    "rol": "ROLE_EMPADRONADOR",
    "sede": "Agencia Regional Juliaca",
    "permisos": "ROLE_EMPADRONADOR,PADRON_LEER,PADRON_ESCRIBIR,TRAMITES_LEER",
    "activo": true
  }
  ```
* **Resultado Esperado (Status 201 Created).**

---

### 📌 MÓDULO 4: Auditoría y Telemetría

#### Prueba 4.1: Registrar Evento de Telemetría (Google Analytics / Meta / Clarity)
* **Método:** `POST`
* **URL:** `{{baseUrl}}/auditoria_reniec`
* **Body:**
  ```json
  {
    "usuario": "admin",
    "rol": "ROLE_ADMIN",
    "accion": "TELEMETRIA_ACTIVA",
    "entidad": "TELEMETRIA",
    "referencia_dni": "PADRON-50K",
    "detalle": "Prueba de integración exitosa desde Postman REST Client",
    "canal_telemetria": "Google Analytics",
    "dispositivo": "Postman Desktop"
  }
  ```
* **Resultado Esperado (Status 201 Created).**

#### Prueba 4.2: Listar Logs de Auditoría
* **Método:** `GET`
* **URL:** `{{baseUrl}}/auditoria_reniec?select=*&order=id.desc&limit=10`
* **Resultado Esperado (Status 200 OK).**

---

## 🤖 5. Pruebas Automatizadas con Scripts de Postman (Pestaña "Tests")

Para que Postman verifique automáticamente el éxito de cada solicitud, puedes pegar estos snippets en la pestaña **"Tests"** de cada request:

### Validar Código de Estado 200 / 201:
```javascript
pm.test("Status code es exitoso (200 o 201)", function () {
    pm.expect(pm.response.code).to.be.oneOf([200, 201]);
});
```

### Validar Tiempo de Respuesta Rápido (< 800ms):
```javascript
pm.test("Tiempo de respuesta menor a 800ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(800);
});
```

### Validar Estructura JSON y que Contenga Registros:
```javascript
pm.test("La respuesta es un array con elementos", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.be.an("array");
    pm.expect(jsonData.length).to.be.greaterThan(0);
});
```

---

## 🏃 6. Ejecución Masiva (Collection Runner)

1. En Postman, haz clic derecho sobre la carpeta de la colección `RENIEC Móvil UPeU...`.
2. Selecciona **"Run collection"**.
3. Asegúrate de marcar los endpoints que deseas probar.
4. Haz clic en **"Run RENIEC Móvil UPeU..."**.
5. Verás el reporte completo en verde (`100% Passed`) con todos los tiempos de respuesta.

# Proyecto Final: RENIEC Móvil - Sistema Multiplataforma de Brigadas e Identificación Ciudadana (Offline-First)

- **Universidad**: Universidad Peruana Unión (UPeU) - Facultad de Ingeniería y Arquitectura (EP Ingeniería de Sistemas)
- **Asignatura**: Desarrollo de Aplicaciones Móviles (Ciclo 6 - Semestre 2026-2)
- **Docente**: Ing. Benjamin David Reyna Barreto
- **Estudiante**: Dolton Tom Meza (Código: `201420294`)
- **Repositorio Git**: https://github.com/DoltonTim/Proyecto_Reniec_Dolton_Tom.git
- **Ramas del Repositorio**:
  - `master`: Rama principal de producción.
  - `Dolton-aportando`: Rama de desarrollo incremental por unidades y registro de aportes individuales.

---

## 1. Descripción del Caso de Estudio (RENIEC)
Los registradores civiles y brigadas itinerantes del **RENIEC** realizan campañas de identificación y trámites de DNI (*Inscripción por primera vez, Renovación, Duplicado de DNIe, Rectificación de datos y Actualización de Domicilio/Ubigeo*) en localidades donde la conectividad a internet es intermitente o nula.

**RENIEC Móvil** resuelve esta problemática mediante una arquitectura **Offline-First**:
1. Permite autenticar al registrador civil mediante **JWT (JSON Web Token)** y almacenar el token de forma cifrada (`EncryptedSharedPreferences` en Android).
2. Permite consultar el **Padrón Nacional de Ciudadanos por DNI (8 dígitos)** visualizando una ficha estilo **DNI Peruano**.
3. Ejecuta el **CRUD REST completo (`GET`, `POST`, `PUT`, `DELETE`)** tanto de Ciudadanos como de Trámites de DNI.
4. Si el dispositivo pierde conexión, guarda los registros localmente en **SQLDelight** con estado `Pendiente Sync` (ID local negativo) y, al restablecer la red o presionar **"Sync"**, sube automáticamente los pendientes al servidor central de RENIEC.

---

## 2. Cumplimiento de Rúbricas por Unidad del Sílabo

| Unidad del Sílabo | Componentes Técnicos Implementados | Ubicación en el Código |
| :--- | :--- | :--- |
| **Unidad 1**: Fundamentos KMP, UI Compose Material 3 y Clean + MVVM | - Modelado en `commonMain` (`Ciudadano`, `TramiteDni`, `Sesion`, `EstadoCivil`, `TipoTramiteDni`, `EstadoTramiteDni`).<br>- Arquitectura Clean (`domain`, `data`, `presentation`) + MVVM (`StateFlow`, `UiState`).<br>- Inyección de dependencias con **Koin** (`initKoin`, `dataModule`, `domainModule`, `presentationModule`, `platformModule`).<br>- Navegación multiplataforma, formularios validados y **Tema Material 3 Claro / Oscuro** (`ReniecMovilTheme`). | `shared/src/commonMain/kotlin/pe/edu/upeu/reniecmovil/` |
| **Unidad 2**: Conectividad Ktor, CRUD REST, JWT, `expect/actual` y SQLDelight Offline-First | - Cliente HTTP **Ktor** con `ContentNegotiation`, `kotlinx.serialization`, `Logging` y plugin `Auth { bearer }` (`ClienteHttp.kt`).<br>- Consumo CRUD REST completo (`CiudadanoApi`, `TramiteApi`, `AuthRepositorioRest`).<br>- Mecanismo nativo `expect/actual` (`Platform.kt`, `AlmacenSeguro`).<br>- Persistencia local **SQLDelight** (`ReniecStorage.sq`, `ReniecLocalDataSource.kt`) y sincronización **Offline-First** (`RepositoriosOffline.kt`).<br>- Pruebas unitarias en `commonTest` (`ReniecOfflineAndUseCasesTest.kt`). | `shared/src/commonMain/sqldelight/`<br>`shared/src/commonTest/` |
| **Unidad 3**: Proyecto Integrador, Backend REST, Permisos y Builds | - Permisos nativos (`INTERNET`, `ACCESS_NETWORK_STATE`) en `AndroidManifest.xml`.<br>- Backend propio en **Spring Boot + Spring Security + JWT + H2 + Swagger OpenAPI** en `backend/`.<br>- Generación de APK/AAB verificada con Gradle y documentación técnica de sustentación. | `androidApp/`<br>`backend/` |

---

## 3. Credenciales de Acceso (Login JWT)

| Rol | Usuario | Contraseña | Permisos JWT |
| :--- | :--- | :--- | :--- |
| **Registrador Administrador** | `admin` | `Admin123!` | `PADRON_LEER`, `PADRON_ESCRIBIR`, `TRAMITES_LEER`, `TRAMITES_ESCRIBIR` |
| **Brigadista RENIEC** | `dolton` | `Reniec2026!` | `PADRON_LEER`, `PADRON_ESCRIBIR`, `TRAMITES_LEER`, `TRAMITES_ESCRIBIR` |

---

## 4. Endpoints del Backend REST (`http://localhost:8080`)

- `POST /api/v1/auth/login` → Autentica al registrador y devuelve el token JWT firmado.
- `GET  /api/v1/auth/me` → Devuelve los datos del registrador autenticado a partir del Bearer Token.
- `GET  /api/v1/ciudadanos` → Lista el padrón de ciudadanos peruanos.
- `GET  /api/v1/ciudadanos/dni/{dni}` → Busca un ciudadano por número de DNI (8 dígitos).
- `POST /api/v1/ciudadanos` → Empadrona un nuevo ciudadano.
- `PUT  /api/v1/ciudadanos/{id}` → Actualiza los datos de un ciudadano.
- `DELETE /api/v1/ciudadanos/{id}` → Elimina un ciudadano del padrón.
- `GET  /api/v1/tramites` → Lista todos los trámites de DNI registrados.
- `POST /api/v1/tramites` → Registra una nueva solicitud de trámite DNI.
- `PUT  /api/v1/tramites/{id}` → Actualiza el estado o datos de un trámite DNI.
- `DELETE /api/v1/tramites/{id}` → Anula / elimina un trámite DNI.
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`

---

## 5. Guía de Ejecución Paso a Paso (Backend + Emulador Android Studio)

### Paso 1: Levantar el Backend REST (Puerto 8080)
Desde una terminal en la raíz del proyecto:
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

### Paso 2: Ejecutar la App Móvil en Android Studio
1. Abrir la carpeta raíz del proyecto en **Android Studio**.
2. Seleccionar la configuración **`androidApp`** y tu Emulador Android.
3. Presionar **Run ▶️**. El emulador se conectará automáticamente al Backend mediante `http://10.0.2.2:8080/api/v1/`.

### Paso 3: Ejecutar Pruebas Unitarias y Generar APK desde Terminal
```powershell
.\gradlew.bat :shared:testAndroidHostTest :androidApp:assembleDebug
```
El APK compilado se genera en: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.

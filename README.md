# Proyecto RENIEC Móvil - Sistema de Brigadas e Identificación Ciudadana

- **Estudiante**: Dolton Tom Meza (Código: 201420294)
- **Asignatura**: Desarrollo de Aplicaciones Móviles - UPeU (2026-2)
- **Docente**: Ing. Benjamin David Reyna Barreto
- **Repositorio**: https://github.com/DoltonTim/Proyecto_Reniec_Dolton_Tom.git

## Estructura del Proyecto
- `androidApp/`: Punto de entrada Android (Kotlin + Jetpack Compose).
- `shared/`: Módulo compartido Kotlin Multiplatform (`commonMain`, `androidMain`, `iosMain`, `commonTest`) con arquitectura Clean + MVVM, Koin, Ktor Client, Autenticación JWT y persistencia Offline-First con SQLDelight.
- `iosApp/`: Configuración multiplataforma para iOS.
- `backend/`: Servidor API REST (Spring Boot + H2 + Spring Security + JWT) para gestión de Padrón de Ciudadanos y Trámites DNI de RENIEC.
- `silabo/`: Sílabo oficial de la asignatura.

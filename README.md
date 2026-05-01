# Technology MS

Microservicio de gestión de tecnologías y capacidades tecnológicas, desarrollado con Clean Architecture y Spring Boot.

## Descripción

Technology MS permite administrar el catálogo de tecnologías de una organización, incluyendo sus capacidades tecnológicas asociadas. El servicio expone una API reactiva para operaciones CRUD y se integra mediante eventos Kafka para sincronización con otros microservicios.

## Diagrama de Entidades

```
┌─────────────────────┐       ┌───────────────────────────┐
│      Technology     │       │   TechnologyCapacity      │
├─────────────────────┤       ├───────────────────────────┤
│ Long id             │──┐    │ Long id                   │
│ String name         │  │    │ Long technologyId (FK)    │◄─┐
│ String description  │  │    │ Long capacityExternalId   │  │
│ LocalDateTime       │  └───►│                           │  │
│   createdAt         │       └───────────────────────────┘  │
│ LocalDateTime       │                                      │
│   updatedAt         │                                      │
└─────────────────────┘                                      │
                                                              │
                                                              │
┌─────────────────────┐                                      │
│        Auth         │                                      │
├─────────────────────┤                                      │
│ Long id             │                                      │
│ String email        │                                      │
│ String token        │                                      │
│ Integer expiresIn   │                                      │
│ LocalDateTime       │                                      │
│   createdAt         │                                      │
└─────────────────────┘                                      │
```

### Entidades

- **Technology**: Representa una tecnología del catálogo (nombre, descripción, timestamps)
- **TechnologyCapacity**: Vincula una tecnología con una capacidad externa (relación N:1)
- **Auth**: Almacena tokens de autenticación de usuarios (email, token, expiración)

## Tecnologías

| Componente | Tecnología                   |
|------------|------------------------------|
| Lenguaje | Java 25                      |
| Framework | Spring Boot 4.0.5            |
| Arquitectura | Clean Architecture           |
| Persistencia | R2DBC (Reactivo)             |
| Mensajeria | Apache Kafka                 |
| API | Spring WebFlux (Reactivo)    |
| Seguridad | Spring Security              |
| Build | Gradle                       |
| Testing | JUnit 5, Mockito, BlockHound |
| Calidad | SonarQube, JaCoCo, Pitest    |

## Estructura del Proyecto

```
technology-ms/
├── applications/          # Capa de aplicación (assemblers, configuración)
│   └── app-service/
├── domain/                # Capa de dominio
│   ├── model/            # Entidades y excepciones del dominio
│   └── usecase/          # Casos de uso
├── infrastructure/        # Capa de infraestructura
│   ├── driven-adapters/  # Adaptadores de persistencia y mensajería
│   │   ├── kafka-publisher/
│   │   └── r2dbc-repository/
│   └── entry-points/     # Puntos de entrada (API REST, Kafka consumers)
│       ├── reactive-web/
│       └── kafka-consumer/
├── deployment/            # Configuración de despliegue
├── docs/                  # Documentación adicional
└── README.md
```

## Clean Architecture

```
┌─────────────────────────────────────────────┐
│              Infrastructure                 │
│  ┌─────────────────┐  ┌──────────────────┐  │
│  │  Entry Points   │  │  Driven Adapters │  │
│  │  - REST API      │  │  - R2DBC         │  │
│  │  - Kafka Cons.   │  │  - Kafka Pub.    │  │
│  └─────────────────┘  └──────────────────┘  │
├─────────────────────────────────────────────┤
│                 Domain                       │
│  ┌─────────────────┐  ┌──────────────────┐  │
│  │     Model       │  │    Use Cases     │  │
│  │  - Technology   │  │  - CRUD Tech     │  │
│  │  - Auth         │  │  - Sync Capacity │  │
│  │  - Exceptions   │  │  - Auth Mgmt     │  │
│  └─────────────────┘  └──────────────────┘  │
├─────────────────────────────────────────────┤
│               Application                    │
│         (Assembler, DI, Main)                │
└─────────────────────────────────────────────┘
```

## Getting Started

```bash
./gradlew bootRun
```

## API Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | /api/v1/technologies | Listar tecnologías |
| GET | /api/v1/technologies/{id} | Obtener tecnología |
| POST | /api/v1/technologies | Crear tecnología |
| PUT | /api/v1/technologies/{id} | Actualizar tecnología |
| DELETE | /api/v1/technologies/{id} | Eliminar tecnología |

## Eventos Kafka

### Consumo
- `auth-login-events`: Eventos de login de usuarios
- `auth-logout-events`: Eventos de logout de usuarios
- `sync-technologies-capacities`: Sincronización de capacidades tecnológicas

### Publicación
- `technology-catalog-events`: Eventos de catálogo de tecnologías
# AutoAndes Backend

Examen Parcial II de Lenguaje de Programación II, versión A. Gestión de servicios de taller, vehículos y órdenes con Oracle, JWT y permisos centralizados. Frontend: https://github.com/LeonardoTA/autoandes-frontend

## Ejecutar

Requisitos: JDK 21, acceso a Oracle FREEPDB1 y variables DB_URL, DB_USERNAME, DB_PASSWORD y JWT_SECRET (clave aleatoria de al menos 32 bytes en Base64). Maven Wrapper incluido. No se necesita Maven global.

En PowerShell, desde esta carpeta:

```powershell
$env:JAVA_HOME = 'C:/ruta/al/jdk-21'
$env:DB_URL = 'jdbc:oracle:thin:@localhost:1522/FREEPDB1'
$env:DB_USERNAME = 'TU_ESQUEMA'
$env:DB_PASSWORD = 'TU_CLAVE'
$env:JWT_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(64))
.\iniciar.ps1
```

En la copia local preparada, iniciar.ps1 también reconoce ../.local-autoandes/entorno.json, situado fuera del repositorio. Este archivo privado no se entrega. No publicar claves reales ni conectarse como SYSTEM/SYS. Usar un esquema de aplicación con CREATE SESSION, CREATE TABLE, CREATE SEQUENCE y cuota suficiente en USERS, creado previamente por un administrador.

Backend: http://localhost:8080. Swagger: http://localhost:8080/swagger-ui/index.html. OpenAPI: http://localhost:8080/v3/api-docs. CORS admite http://localhost:4200. La SPA se inicia en ese origen.

## Dependencias

Java 21, Spring Boot 4.0.9; starters webmvc, data-jpa, validation, security, flyway y soporte de pruebas. Oracle ojdbc11 y flyway-database-oracle. Lombok con procesadores de anotaciones tanto al compilar fuentes como pruebas. springdoc-openapi-starter-webmvc-ui 3.0.2. JWT: JJWT 0.12.6 (api, impl y jackson); clave Base64 decodificada por JwtService. Boot administra las versiones transitivas; no fijar Hibernate/Jackson manualmente.

Fuentes oficiales: [Spring Initializr](https://start.spring.io/), [Spring Boot](https://docs.spring.io/spring-boot/4.0/), [Lombok Maven](https://projectlombok.org/setup/maven), [Spring Security](https://docs.spring.io/spring-security/reference/), [springdoc](https://springdoc.org/), [Flyway Oracle](https://documentation.red-gate.com/flyway/reference/database-driver-reference/oracle-database).

## Datos y migración

Flyway ejecuta src/main/resources/db/migration/V1__esquema_y_semilla.sql; JPA valida el esquema, no lo actualiza. Incluye las cinco tablas del anexo, las tablas de seguridad faltantes y las semillas mínimas. Las omisiones del documento y las decisiones se explican en entrega/DECISIONES.md. La configuración local usa baseline 0 porque el esquema aislado contenía una tabla de demostración ajena a AutoAndes; se preserva y V1 sí se ejecuta. No usar esta configuración para asumir propiedad de otro esquema.

| Usuario | Contraseña de prueba | Rol |
|---|---|---|
| admin | Auto2026* | ADMIN |
| asesor | Auto2026* | ASESOR |
| jefe | Auto2026* | JEFE_TALLER |
| inactivo | Auto2026* | ASESOR, acceso deshabilitado |

Estas son cuentas académicas de prueba, no credenciales de Oracle. Las contraseñas se almacenan con BCrypt. ADMIN reúne todos los permisos; ASESOR registra órdenes y gestiona vehículos; JEFE_TALLER administra servicios, anula órdenes y consulta reportes. La unión de roles se calcula en un único componente.

## Contrato HTTP

Base: http://localhost:8080/api/v1. JSON; Bearer JWT salvo login. Fechas de filtros YYYY-MM-DD. GET devuelve listas directas. POST de dominio devuelve 201; login, GET, PUT y PATCH devuelven 200; DELETE 204 sin cuerpo.

| Ruta | Operación | Permiso |
|---|---|---|
| /auth/login | POST {username,password} | Pública |
| /auth/me | GET | Autenticado |
| /clientes | GET | VEHICULO_LEER |
| /servicios, /servicios/{id} | GET | SERVICIO_LEER |
| /servicios, /servicios/{id} | POST, PUT, DELETE | SERVICIO_ESCRIBIR |
| /servicios/{id}/estado | PATCH {activo} | SERVICIO_ESCRIBIR |
| /vehiculos, /vehiculos/{id} | GET; filtro clienteId | VEHICULO_LEER |
| /vehiculos, /vehiculos/{id} | POST, PUT, DELETE | VEHICULO_ESCRIBIR |
| /ordenes | GET; placa,estado,desde,hasta | ORDEN_LEER |
| /ordenes/{id} | GET | ORDEN_LEER |
| /ordenes | POST | ORDEN_REGISTRAR |
| /ordenes/{id}/anular | PATCH {motivo} | ORDEN_ANULAR |
| /reportes/ingresos-por-servicio | GET; desde,hasta | REPORTE_VER |

Servicio: {codigo,nombre,precio,duracionMin,activo}. Vehículo: {placa,marca,modelo,anio,clienteId}. Orden: {vehiculoId,kilometraje,detalles:[{servicioId,cantidad}]}; precios, número, fecha, usuario y total se calculan en el servidor. Login responde {token,tipo,expiraEnSegundos,nombreCompleto,username,roles,permisos}. Los DTO de respuestas completas se consultan en Swagger.

Error común: {timestamp,status,error,mensaje,ruta,campos?}; campos contiene mensajes por atributo solo cuando hay errores de validación. 400 datos inválidos, 401 sesión ausente/inválida/vencida, 403 falta de permiso, 404 recurso inexistente, 409 conflicto de negocio. Un 403 conserva la sesión de la SPA.

## Arquitectura

Paquete pe.edu.upeu.autoandes: controller -> service/impl -> repository -> entity. DTO y mapper separan el contrato HTTP de JPA. config y security concentran JWT, CORS, respuestas 401/403 y permisos. exception concentra errores. Lombok genera getters, setters y constructores; las entidades y relaciones JPA siguen siendo explícitas.

El registro de órdenes y su detalle ocurre en una transacción. Los precios proceden de la base; se conserva el historial, se valida kilometraje y solo se anula una orden registrada el mismo día en America/Lima. El reporte usa una consulta JPQL agregada y excluye anuladas. La secuencia empresarial puede tener saltos después de un rollback; no representan filas parciales.

## Verificación reproducible

```powershell
.\iniciar.ps1 -Accion test
.\iniciar.ps1 -Accion package
# Con la API ejecutándose y Node instalado:
node qa/api-real.mjs entrega/qa-api.json
```

Las pruebas de Maven usan Oracle real y revierten sus transacciones cuando corresponde. La prueba HTTP registra y anula una orden propia; conserva ese historial de prueba y lo identifica en el informe. No ejecutar contra datos de producción. La prueba de vencimiento firmado requiere JWT_SECRET en el entorno del proceso de Node.

Entregables: postman/AutoAndes.postman_collection.json, entrega/Tequen_LP2_ExamenU2.pdf, informes QA y entrega/DEFENSA.md. La evidencia declara lo realmente ejecutado. Trabajo en examen-a-Tequen; integración mediante develop y main; etiqueta de entrega v1.0-examen-u2. No se alteran fechas del historial.

## Puerto8080 en este equipo
MiniTool ShadowMaker puede ocupar8080. Durante la verificación se detuvo temporalmente MTAgentService con autorización del usuario y confirmación de administrador. Para una demostración local debe liberarse ese puerto; al terminar, detener AutoAndes y restaurar MiniTool. No cambiar el contrato de puertos de la entrega.


## Ejecutar colección en Postman
Importar postman/AutoAndes.postman_collection.json. Seleccionar Run y ejecutar todas las carpetas en orden: Login, CRUD, Órdenes/reporte y Errores. Las variables token se cargan al iniciar sesión; los códigos y placas libres se eligen automáticamente desde la API. No exportar valores de tokens. Los informes brutos deben guardarse fuera del repositorio y entregar solo el resumen sin credenciales. No ejecutar la colección y el ensayo de navegador a la vez porque ambos escriben datos de prueba en el mismo esquema.


## Resultado comprobado
13 pruebas Maven aprobadas,37 pruebas del frontend,64 casos HTTP,20 comprobaciones de navegador y colección Postman ejecutada con Newman6.2.3:34 solicitudes (30principales+4auxiliares),50 aserciones,0fallos. Ver entrega/qa-compilacion.json,qa-api.json,qa-navegador.json yqa-postman.json. La colección se importa y ejecuta también desde Postman Desktop; instrucciones oficiales: https://learning.postman.com/docs/collections/using-newman-cli/installing-running-newman/.


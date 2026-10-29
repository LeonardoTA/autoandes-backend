# AutoAndes: guía breve de defensa

Usar ambos repositorios abiertos y el sistema ejecutándose. El docente elige dos preguntas del banco; explicar el código mostrado, no memorizar una frase. Las rutas de backend parten de src/main/java/pe/edu/upeu/autoandes; las del frontend, de src/app.

1. **Recorrido de una orden.** Frontend features/ordenes/pages/orden-form/orden-form.ts reúne cabecera y FormArray; core/services/orden.service.ts envía HTTP; core/interceptors/auth.interceptor.ts agrega Bearer. En backend config/JwtAuthenticationFilter.java valida la sesión y SecurityConfig.java exige ORDEN_REGISTRAR. controller/OrdenController.java valida OrdenRequestDTO y llama service/impl/OrdenServiceImpl.java. Este consulta repositorios, construye OrdenServicio y DetalleOrden, guarda la transacción y mapper/OrdenMapper.java devuelve el DTO. Oracle recibe orden_servicio y detalle_orden. Mostrar el número devuelto en la pantalla.
2. **Total confiable.** OrdenServiceImpl.registrar obtiene el precio de Servicio en Oracle. Calcula cantidad × precioUnitario con BigDecimal, suma subtotales y guarda total. El formulario solo muestra una estimación y envía IDs/cantidades. Mostrar el cuerpo de POST /ordenes; nunca incluye precio, total ni usuarioRegistro.
3. **Error en el tercer detalle.** Ejecutar OrdenOracleTest.tercerDetalleInactivoNoDejaFilas o el caso de Postman con servicios 1,2,8. El tercero está inactivo: responde409. Comparar conteo de orden_servicio y detalle_orden antes/después. @Transactional está en el servicio; una excepción de negocio revierte la unidad de trabajo. Aquí se validan las líneas antes de guardar, evitando además escrituras innecesarias. Los huecos en una secuencia no son filas guardadas.
4. **Reporte agregado.** Abrir repository/DetalleOrdenRepository.java: JPQL agrupa por código/nombre, suma cantidad/subtotal y filtra REGISTRADA y fechas. Devuelve IngresoServicioDTO ordenado por importe. Oracle agrega los datos; findAll más bucles cargaría detalles innecesarios y mezclaría persistencia con cálculo de reporte. Anular la orden de prueba y observar que sale del reporte.
5. **DTO frente a entidad.** Mostrar Vehiculo, VehiculoResponseDTO y CatalogoMapper. La respuesta incluye clienteId/clienteNombre, no todo Cliente ni relaciones JPA. Evita ciclos de serialización, cargas perezosas fuera de transacción, exponer hashes o acoplar el contrato al esquema.
6. **JWT.** JwtService firma con HS256 y la clave secreta del servidor, configurada por entorno. El token transporta iss, sub (username), iat y exp. Decodificar Base64URL permite leerlo, no demuestra que su firma sea válida. El filtro valida firma/expiración y consulta usuario/permisos vigentes. Al vencer devuelve401; la SPA borra la sesión y pide login. La clave real nunca se muestra en la defensa ni se entrega en Git.
7. **Permisos backend.** Mostrar security/Permiso.java, PermisosPorRol.java y SecurityConfig.java. Los permisos de varios roles se unen; endpoints usan autoridades, no comparaciones de nombre de rol en controllers. Probar ASESOR -> reporte o anulación:403. JEFE_TALLER -> registrar orden:403. Ocultar botones no sustituye ese control.
8. **401 frente a403.** Abrir core/interceptors/error.interceptor.ts.401 fuera del login significa sesión no válida: logout y returnUrl.403 significa usuario autenticado sin permiso: conserva sesión y deja el error visible. Mostrar el guard de /ordenes/nueva con jefe y comprobar que la sesión permanece.
9. **Un solo punto para Authorization.** Abrir core/interceptors/auth.interceptor.ts y app.config.ts. El interceptor clona las peticiones dirigidas a la API y añade Bearer. Repetirlo en cada servicio duplicaría lógica y facilitaría omisiones o filtraciones a otros destinos.
10. **sessionStorage.** AuthService guarda sesión/vencimiento por pestaña y la restaura si sigue vigente. Se usa para reducir persistencia en equipos compartidos; cerrar sesión borra la entrada. No protege contra JavaScript malicioso: la autorización real sigue en backend.
11. **URL escrita a mano.** Iniciar como jefe y escribir /ordenes/nueva. authGuard comprueba sesión; permisoGuard lee el permiso de la ruta, detecta que falta ORDEN_REGISTRAR y redirige a acceso-denegado. Mostrar features/ordenes/ordenes.routes.ts y core/guards/permiso.guard.ts. La sesión no se borra.
12. **Menú reactivo.** AuthService mantiene la sesión en signal y permisos derivados. El menú y SiPermiso dependen de esas señales. Login/logout cambian la señal y Angular actualiza lo visible sin recargar toda la página. Mostrar core/config/menu.ts, layout/sidebar y shared/directives/si-permiso.directive.ts.

## Demostración de mayor valor en pocos minutos

1. Arrancar API y SPA con los comandos del README; mostrar Flyway válido y Swagger.
2. Admin: crear y editar servicio/vehículo con cliente real; provocar validación400 y duplicado409.
3. Asesor: registrar orden con dos detalles; mostrar número, total calculado y usuario del servidor.
4. Provocar tercer detalle inactivo y verificar ausencia de filas parciales.
5. Jefe: consultar reporte, anular orden del día y observar exclusión del reporte.
6. Mostrar403 del backend y acceso denegado escribiendo URL; alterar token y mostrar vuelta al login.
7. Enseñar los dos repositorios, commits por pregunta y etiqueta v1.0-examen-u2.

El anexo SQL tiene omisiones reales: no afirmar que los CREATE de seguridad y los INSERT fueron proporcionados por el profesor. Ver DECISIONES.md. No se exige video; sí PDF, Postman y README por repositorio.

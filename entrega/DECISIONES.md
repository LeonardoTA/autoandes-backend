# Decisiones y aclaraciones pendientes del enunciado

El documento entregado tiene 16 páginas y un plazo de 180 minutos. Se contrastó el anexo de la página 16 con el PDF, no solo con su extracción Markdown.

1. El anexo entrega cinco CREATE completos, pero remite usuarios/roles a la sesión 10 y enumera semillas sin INSERT. El usuario confirmó que no recibió otro SQL. V1 conserva esos cinco CREATE y marca como COMPLEMENTO el DDL de seguridad, las semillas y la secuencia de números. Pregunta al docente: ¿se acepta completar estas omisiones o entregará el script de la sesión 10? No se atribuye ese complemento al profesor.
2. La estructura de la sección 5 usa V1__esquema_y_semilla.sql; el anexo presenta un nombre con espacios. Se usa el nombre válido de Flyway con doble guion bajo.
3. Se interpretó el bloqueo de eliminación de vehículos como preservación de cualquier historial, incluidas órdenes anuladas, por las referencias del esquema. Pregunta: ¿debe permitirse borrar vehículos que solo tengan anuladas? Eso exigiría aclarar el tratamiento de sus claves foráneas e historial.
4. Los filtros son combinables: placa parcial sin distinguir mayúsculas, fechas inclusivas y fecha descendente. Fechas invertidas o con formato inválido responden 400. Estos detalles no estaban ejemplificados mediante JSON completo; se fijaron en el contrato común antes de implementar.
5. La secuencia Oracle genera OS-000001 y evita colisiones. Los huecos tras rollback son normales; la atomicidad se mide por filas de cabecera y detalle, no por continuidad de números.
6. El esquema aislado autorizado tenía una tabla de demostración anterior. Flyway baseline 0 preserva esa tabla y ejecuta V1. Nunca se usan esquemas de prácticas ni usuarios administradores para la aplicación.
7. El documento no anuncia una modificación sorpresa de código durante el examen: anuncia una defensa con dos preguntas elegidas de un banco de doce, mostrando código y ejecución. Una aclaración posterior del docente prevalece y deberá registrarse con su impacto.
8. Se permite IA si el estudiante explica cada línea. No poder explicar una pregunta implica cero en esa pregunta. El backend debe arrancar: de lo contrario la nota máxima es ocho. El historial se crea con avances reales y fechas reales; esta preparación no se presenta como si hubiera empezado en otra fecha.

Las preguntas anteriores no impidieron desarrollar las partes independientes. Si el docente aclara una regla, actualizar código, contrato, pruebas y evidencias afectados antes de volver a etiquetar una entrega nueva.

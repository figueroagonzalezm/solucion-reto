# Solución — Examen Práctico

## Qué se implementó
Se implementó inicialmente lo solicitado en la prueba implementando Swagger, corrigiendo el formulario en Angular e implementando el endpoint DELETE /clientes/{id} con los tipos de respuestas especificados.

## Cómo se implementó
-Usé Claude Code para la imlementación, primero le pedí a Claude que analizara los cambios necesarios para cumplir con los requerimientos iniciales del documento. -Luego revisé el plan y fui ejecutando uno a uno los pasos.
-Adicionalmente implementé algunas mejoras que consideré necesarias: Centralicé el manejo de errores usando la clase GlobalExceptionHandler, implementé una clase para dar formato a la respuesta y también una excepción personalizada llamada ClientNotFoundException.
-Removí las lineas duplicadas de CROSS Origin y cree una configuración centralizada en la clase WebConfig.
-Implementé el uso de JPA en vez de jdbcTemplate, eso permite que JPA pueda realizar validaciones cuando la aplicación se carga y validar que todo está OK.
-Realicé validaciones en el backend adicional de en el frontend.
-Implementé tests unitarios y de integración que faltaban tanto para frontend como para backend.
## Cambios en base de datos / configuración
-Implementé una DB en PostgreSQL que se ejecuta en un contenedor de Docker. La aplicación en desarrollo apunta a esa DB.
-Además implementé el uso de H2 para las pruebas unitarias y de integración. Así podrá realizarse una validación rápida sin tener que subir la DB.
## Dificultades encontradas
...
## Mejoras que podrían aplicarse
-Implementación de autenticación y autorización

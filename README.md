# BibliotecaEscolar

## Autor

- **Nombre del estudiante:** Antonio Jesús Gaspar Rengifo
- **Sección:** DESARROLLO ORIENTADO A OBJETOS II_001A
- **Carrera:** Analista Programador Computacional
- **Sede:** Online
- **Fecha de entrega:** 11-10-2026

## Descripción

Aplicación educativa de escritorio para la Evaluación Final Transversal de Semana 9 de Desarrollo Orientado a Objetos II. Gestiona una biblioteca escolar con Java, Swing y persistencia MySQL mediante JDBC, aplicando POO, MVC, DAO, Singleton y concurrencia.

## Funcionalidades

- Autenticación por correo y contraseña; roles bibliotecario y estudiante.
- CRUD de `Usuario`, `Estudiante`, `Libro`, `Categoria` y `Prestamo`, con restricciones de seguridad.
- Registro y actualización atómicos del estudiante y su cuenta asociada. Las cuentas de estudiante se mantienen desde Estudiantes; no se permite convertirlas en bibliotecarios.
- Préstamos con vencimiento automático, disminución de stock e historial en una transacción; devolución que repone un ejemplar solo una vez.
- Cambio del vencimiento de préstamos pendientes; eliminación únicamente de préstamos devueltos. Eliminar un préstamo también elimina esa entrada del historial y de los conteos.
- Reportes de libros más prestados, historial por estudiante y libros actualmente en préstamo, con días de atraso y fecha real de devolución.
- Estudiantes limitados a consultar libros y operar sus propios préstamos e historial. Los permisos se verifican en los servicios, no solo en los botones.
- Trabajo JDBC fuera del EDT mediante `SwingWorker`, con controles deshabilitados durante las operaciones y protección frente a respuestas de sesiones cerradas.

### Base de datos

Se crearon los scripts `bd/Script_crea_tablas_biblioteca.sql` y `bd/Script_poblado_tablas_biblioteca.sql`

## Estructura del proyecto

```text
pom.xml
src/
|-- Main.java
|-- modelo/
|-- vista/
|-- controlador/
|-- dao/
|   `-- impl/
`-- util/
bd/
|-- Script_crea_tablas_biblioteca.sql
`-- Script_poblado_tablas_biblioteca.sql
config/local.properties.example
```

## Clases principales

- `Persona`: clase abstracta con identidad compartida; `Usuario` y `Estudiante` redefinen `description()`.
- `Libro`, `Categoria` y `Prestamo`: entidades del inventario y la circulación; `Prestamo.lateDays` calcula el atraso actual o histórico.
- `Rol`, `BibliotecarioRol` y `EstudianteRol`: autorización polimórfica; `EstudianteRol` implementa `AccessPolicy` y `LoanScope`.
- `LibraryService`: reglas de negocio, permisos, cuentas asociadas y operaciones de circulación.
- `DatabaseConnection`: Singleton con `getInstance()` y conexión bajo demanda mediante `DriverManager.getConnection`.
- `CrudDAO` y los cinco DAO de entidad con sus implementaciones JDBC: acceso persistente mediante sentencias preparadas y cierre de recursos.
- `StaffDeletionGuard`: protección transaccional de eliminación de bibliotecarios, compartida por la aplicación y su prueba de dos conexiones.
- `LoginFrame`, `MainFrame` y las vistas de cada módulo: ventanas y paneles; `LibraryController` gestiona los eventos y `BackgroundTasks` las tareas de fondo.
- `Main`: inicia Swing y muestra el ingreso al sistema.

## Conceptos de POO implementados

- **Herencia y reutilización:** `Usuario` y `Estudiante` extienden `Persona`; los roles concretos extienden `Rol`.
- **Encapsulación:** identidad privada, modelos inmutables y acceso a datos separado mediante DAO.
- **Abstracción:** métodos abstractos en `Persona` y `Rol` que definen contratos para las clases derivadas.
- **Polimorfismo y sobrescritura:** `@Override` en descripciones y permisos; los servicios despachan según el rol concreto.
- **Sobrecarga:** `LibraryService.registerLoan(session, bookId)` deriva la identidad del estudiante; `registerLoan(session, studentId, bookId)` admite el flujo del bibliotecario. El controlador llama a ambas versiones.
- **Interfaces múltiples:** `EstudianteRol implements AccessPolicy, LoanScope` separa permisos administrativos y ámbito de préstamos.
- **Concurrencia y consistencia:** `SwingWorker`, métodos `synchronized`, transacciones y bloqueos MySQL; una conexión compartida prioriza consistencia sobre paralelismo de consultas.

## Compilación y ejecución en IntelliJ IDEA

### Requisitos

1. Abrir `pom.xml` en IntelliJ IDEA y configurar JDK 17.
2. Comprobar la conexión local en MySQL Workbench e importar primero `bd/Script_crea_tablas_biblioteca.sql` y después `bd/Script_poblado_tablas_biblioteca.sql` con una cuenta propia autorizada para DDL cuando sea necesario.
3. Configurar las credenciales con variables de entorno o copiar `config/local.properties.example` a `config/local.properties`.
4. Desde la raíz del proyecto, ejecutar:

```powershell
$env:LIBRARY_DB_URL = 'jdbc:mysql://localhost:3306/biblioteca_escolar?connectTimeout=5000&socketTimeout=15000&connectionTimeZone=LOCAL'
$env:LIBRARY_DB_USER = 'DATABASE_USER'
$env:LIBRARY_DB_PASSWORD = 'DATABASE_PASSWORD'
$env:LIBRARY_LOAN_DAYS = '14'
mvn clean package
java -jar target/school-library-1.0.0.jar
```

También se puede ejecutar `Main` desde IntelliJ, con la raíz del proyecto como directorio de trabajo.

### Cuentas de demostración

| Rol | Correo | Contraseña |
|---|---|---|
| Bibliotecario | `librarian@library.example` | `BibliotecaDemo2026!` |
| Estudiante | `student@library.example` | `BibliotecaDemo2026!` |

## Repositorio

[Repositorio GitHub utilizado para la entrega del proyecto final de desarrollo orientado a objetos 2.](https://github.com/angaspar-duocuc/desarrollo-orientado-a-objetos-2-final.git)

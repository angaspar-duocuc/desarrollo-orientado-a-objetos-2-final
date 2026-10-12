-- demo password: BibliotecaDemo2026!
USE biblioteca_escolar;
START TRANSACTION;
INSERT INTO categorias(nombre) VALUES ('Literatura'),('Ciencias') ON DUPLICATE KEY UPDATE id=id;
INSERT INTO estudiantes(nombre,rut,curso,correo) VALUES ('Estudiante de demostración','DEMO-STUDENT-01','8 A','student@library.example') ON DUPLICATE KEY UPDATE id=id;
INSERT INTO usuarios(nombre,rut,correo,`contraseña`,rol,id_estudiante)
SELECT 'Bibliotecario de demostración','DEMO-STAFF-01','librarian@library.example','pbkdf2-sha256$210000$1JDrrDY3TOLyM7gxMcKrlA==$HRlSMEmJEXIHmUf72olDo/Uh2mEnVWlMSEhDlkggMN4=','bibliotecario',NULL
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE correo='librarian@library.example');
INSERT INTO usuarios(nombre,rut,correo,`contraseña`,rol,id_estudiante)
SELECT nombre,rut,correo,'pbkdf2-sha256$210000$iukp7Mtf9juO6kRaBCSYXg==$NnJQo8NHWTS45Rv+AXf7/RZCVuvNI6ZfNlErlk3uluk=','estudiante',id FROM estudiantes
WHERE correo='student@library.example' AND NOT EXISTS (SELECT 1 FROM usuarios WHERE correo='student@library.example');
INSERT INTO libros(`título`,autor,isbn,editorial,stock,id_categoria)
SELECT 'El principito','Antoine de Saint-Exupéry','DEMO-ISBN-001','Edición educativa',3,id FROM categorias WHERE nombre='Literatura'
AND NOT EXISTS (SELECT 1 FROM libros WHERE isbn='DEMO-ISBN-001');
INSERT INTO libros(`título`,autor,isbn,editorial,stock,id_categoria)
SELECT 'Introducción a las ciencias','Equipo docente','DEMO-ISBN-002','Edición educativa',2,id FROM categorias WHERE nombre='Ciencias'
AND NOT EXISTS (SELECT 1 FROM libros WHERE isbn='DEMO-ISBN-002');
-- Ejemplo histórico devuelto; el stock disponible anterior ya incluye el ejemplar devuelto.
INSERT INTO prestamos(id_estudiante,id_libro,fecha_prestamo,fecha_devolucion,devuelto,fecha_devolucion_real)
SELECT e.id,l.id,'2026-01-01','2026-01-15',TRUE,'2026-01-17' FROM estudiantes e JOIN libros l ON l.isbn='DEMO-ISBN-001'
WHERE e.correo='student@library.example' AND NOT EXISTS
(SELECT 1 FROM prestamos p WHERE p.id_estudiante=e.id AND p.id_libro=l.id AND p.fecha_prestamo='2026-01-01');
COMMIT;

CREATE DATABASE IF NOT EXISTS biblioteca_escolar CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE biblioteca_escolar;
CREATE TABLE IF NOT EXISTS categorias (
 id INT PRIMARY KEY AUTO_INCREMENT,
 nombre VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS estudiantes (
 id INT PRIMARY KEY AUTO_INCREMENT,
 nombre VARCHAR(150) NOT NULL,
 rut VARCHAR(25) NOT NULL UNIQUE,
 curso VARCHAR(80) NOT NULL,
 correo VARCHAR(180) NOT NULL UNIQUE
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS usuarios (
 id INT PRIMARY KEY AUTO_INCREMENT,
 nombre VARCHAR(150) NOT NULL,
 rut VARCHAR(25) NOT NULL UNIQUE,
 correo VARCHAR(180) NOT NULL UNIQUE,
 `contraseña` VARCHAR(255) NOT NULL,
 rol VARCHAR(20) NOT NULL,
 id_estudiante INT NULL UNIQUE,
 CONSTRAINT fk_usuario_estudiante FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id) ON DELETE RESTRICT,
 CONSTRAINT ck_usuario_rol CHECK ((rol='bibliotecario' AND id_estudiante IS NULL) OR (rol='estudiante' AND id_estudiante IS NOT NULL))
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS libros (
 id INT PRIMARY KEY AUTO_INCREMENT,
 `título` VARCHAR(200) NOT NULL,
 autor VARCHAR(150) NOT NULL,
 isbn VARCHAR(30) NOT NULL UNIQUE,
 editorial VARCHAR(150) NOT NULL,
 stock INT NOT NULL,
 id_categoria INT NOT NULL,
 CONSTRAINT ck_libro_stock CHECK (stock >= 0),
 CONSTRAINT fk_libro_categoria FOREIGN KEY (id_categoria) REFERENCES categorias(id) ON DELETE RESTRICT
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS prestamos (
 id INT PRIMARY KEY AUTO_INCREMENT,
 id_estudiante INT NOT NULL,
 id_libro INT NOT NULL,
 fecha_prestamo DATE NOT NULL,
 fecha_devolucion DATE NOT NULL,
 devuelto BOOLEAN NOT NULL DEFAULT FALSE,
 fecha_devolucion_real DATE NULL,
 CONSTRAINT fk_prestamo_estudiante FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id) ON DELETE RESTRICT,
 CONSTRAINT fk_prestamo_libro FOREIGN KEY (id_libro) REFERENCES libros(id) ON DELETE RESTRICT,
 CONSTRAINT ck_prestamo_fechas CHECK (fecha_devolucion >= fecha_prestamo),
 CONSTRAINT ck_prestamo_devolucion CHECK ((devuelto=FALSE AND fecha_devolucion_real IS NULL) OR (devuelto=TRUE AND fecha_devolucion_real IS NOT NULL AND fecha_devolucion_real >= fecha_prestamo)),
 INDEX idx_historial (id_estudiante,fecha_prestamo),
 INDEX idx_pendientes (devuelto,fecha_devolucion)
) ENGINE=InnoDB;

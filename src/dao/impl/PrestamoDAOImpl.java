package dao.impl;

import dao.PrestamoDAO;
import modelo.Prestamo;
import util.AppException;
import java.time.LocalDate;
import java.util.*;

public final class PrestamoDAOImpl extends JdbcDAO<Prestamo> implements PrestamoDAO {
    private Prestamo map(java.sql.ResultSet r) throws java.sql.SQLException {
        java.sql.Date actual = r.getDate("fecha_devolucion_real");
        return new Prestamo(r.getInt("id"), r.getInt("id_estudiante"), r.getInt("id_libro"),
                r.getDate("fecha_prestamo").toLocalDate(), r.getDate("fecha_devolucion").toLocalDate(),
                r.getBoolean("devuelto"), actual == null ? null : actual.toLocalDate());
    }
    @Override public int create(Prestamo v) {
        return db.transaction(c -> createWithinTransaction(c, v));
    }
    static int createWithinTransaction(java.sql.Connection c, Prestamo v) throws java.sql.SQLException {
        requireTransaction(c);
        if (v.returned() || v.actualReturnDate() != null || v.dueDate().isBefore(v.loanDate()))
            throw new AppException("El préstamo nuevo debe estar pendiente y tener fechas válidas.");
            try (var s = c.prepareStatement("SELECT stock FROM libros WHERE id=? FOR UPDATE")) {
                s.setInt(1, v.bookId());
                try (var r = s.executeQuery()) {
                    if (!r.next() || r.getInt(1) < 1) throw new AppException("El libro no tiene ejemplares disponibles.");
                }
            }
            changeWithinTransaction(c, "UPDATE libros SET stock=stock-1 WHERE id=? AND stock>0", v.bookId());
            try (var s = c.prepareStatement("INSERT INTO prestamos(id_estudiante,id_libro,fecha_prestamo,fecha_devolucion,devuelto) VALUES (?,?,?,?,false)", java.sql.Statement.RETURN_GENERATED_KEYS)) {
                bind(s, v.studentId(), v.bookId(), v.loanDate(), v.dueDate());
                s.executeUpdate();
                try (var keys = s.getGeneratedKeys()) {
                    if (!keys.next()) throw new java.sql.SQLException("Missing generated key");
                    return keys.getInt(1);
                }
            }
    }
    @Override public Optional<Prestamo> findById(int id) { return query("SELECT * FROM prestamos WHERE id=?", this::map, id).stream().findFirst(); }
    public Optional<Prestamo> findLocked(int id) { return query("SELECT * FROM prestamos WHERE id=? FOR UPDATE", this::map, id).stream().findFirst(); }
    @Override public List<Prestamo> findAll() { return query("SELECT * FROM prestamos ORDER BY id DESC", this::map); }
    @Override public List<Prestamo> findByStudent(int id) { return query("SELECT * FROM prestamos WHERE id_estudiante=? ORDER BY id DESC", this::map, id); }
    @Override public void update(Prestamo v) {
        db.transaction(c -> {
            Prestamo old = findLocked(v.id()).orElseThrow(() -> new AppException("Préstamo no encontrado."));
            if (old.returned() || old.studentId() != v.studentId() || old.bookId() != v.bookId()
                    || !old.loanDate().equals(v.loanDate()) || v.returned() || v.actualReturnDate() != null)
                throw new AppException("Solo se puede modificar el vencimiento de un préstamo pendiente.");
            if (v.dueDate().isBefore(old.loanDate())) throw new AppException("El vencimiento precede al préstamo.");
            change("UPDATE prestamos SET fecha_devolucion=? WHERE id=? AND devuelto=false", v.dueDate(), v.id());
            return null;
        });
    }
    @Override public void returnLoan(int id, LocalDate date) {
        db.transaction(c -> {
            returnWithinTransaction(c, id, date);
            return null;
        });
    }
    static void returnWithinTransaction(java.sql.Connection c, int id, LocalDate date) throws java.sql.SQLException {
        requireTransaction(c);
        int bookId;
        try (var s = c.prepareStatement("SELECT id_libro,fecha_prestamo,devuelto FROM prestamos WHERE id=? FOR UPDATE")) {
            s.setInt(1, id);
            try (var r = s.executeQuery()) {
                if (!r.next()) throw new AppException("Préstamo no encontrado.");
                if (r.getBoolean("devuelto")) throw new AppException("El préstamo ya fue devuelto; no se modificó el stock.");
                if (date.isBefore(r.getDate("fecha_prestamo").toLocalDate())) throw new AppException("La devolución precede al préstamo.");
                bookId = r.getInt("id_libro");
            }
        }
        changeWithinTransaction(c, "UPDATE prestamos SET devuelto=true,fecha_devolucion_real=? WHERE id=? AND devuelto=false", date, id);
        changeWithinTransaction(c, "UPDATE libros SET stock=stock+1 WHERE id=?", bookId);
    }
    private static void requireTransaction(java.sql.Connection c) throws java.sql.SQLException {
        if (c.getAutoCommit()) throw new AppException("La circulación requiere una transacción activa.");
    }
    private static void changeWithinTransaction(java.sql.Connection c, String sql, Object... values) throws java.sql.SQLException {
        try (var s = c.prepareStatement(sql)) {
            bind(s, values);
            if (s.executeUpdate() != 1)
                throw new AppException("El registro cambió o ya no existe. Recargue antes de continuar.");
        }
    }
    @Override public void delete(int id) {
        db.transaction(c -> {
            Prestamo old = findLocked(id).orElseThrow(() -> new AppException("Préstamo no encontrado."));
            if (!old.returned()) throw new AppException("Devuelva el préstamo antes de eliminarlo.");
            change("DELETE FROM prestamos WHERE id=? AND devuelto=true", id);
            return null;
        });
    }
}

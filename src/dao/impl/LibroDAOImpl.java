package dao.impl;

import dao.LibroDAO;
import modelo.Libro;
import java.util.*;

public final class LibroDAOImpl extends JdbcDAO<Libro> implements LibroDAO {
    private Libro map(java.sql.ResultSet r) throws java.sql.SQLException {
        return new Libro(r.getInt("id"), r.getString("título"), r.getString("autor"),
                r.getString("isbn"), r.getString("editorial"), r.getInt("stock"), r.getInt("id_categoria"));
    }
    @Override public int create(Libro v) {
        return insert("INSERT INTO libros(`título`,autor,isbn,editorial,stock,id_categoria) VALUES (?,?,?,?,?,?)",
                v.titulo(), v.autor(), v.isbn(), v.editorial(), v.stock(), v.categoryId());
    }
    @Override public Optional<Libro> findById(int id) { return query("SELECT * FROM libros WHERE id=?", this::map, id).stream().findFirst(); }
    @Override public List<Libro> findAll() { return query("SELECT * FROM libros ORDER BY id ASC", this::map); }
    @Override public void update(Libro v) {
        change("UPDATE libros SET `título`=?,autor=?,isbn=?,editorial=?,stock=?,id_categoria=? WHERE id=? AND stock=?",
                v.titulo(), v.autor(), v.isbn(), v.editorial(), v.stock(), v.categoryId(), v.id(), v.expectedStock());
    }
    @Override public void delete(int id) { change("DELETE FROM libros WHERE id=?", id); }
}

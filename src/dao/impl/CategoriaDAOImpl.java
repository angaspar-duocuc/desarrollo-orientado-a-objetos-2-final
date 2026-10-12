package dao.impl;

import dao.CategoriaDAO;
import modelo.Categoria;
import java.util.*;

public final class CategoriaDAOImpl extends JdbcDAO<Categoria> implements CategoriaDAO {
    private Categoria map(java.sql.ResultSet r) throws java.sql.SQLException {
        return new Categoria(r.getInt("id"), r.getString("nombre"));
    }
    @Override public int create(Categoria v) { return insert("INSERT INTO categorias(nombre) VALUES (?)", v.nombre()); }
    @Override public Optional<Categoria> findById(int id) { return query("SELECT * FROM categorias WHERE id=?", this::map, id).stream().findFirst(); }
    @Override public List<Categoria> findAll() { return query("SELECT * FROM categorias ORDER BY nombre", this::map); }
    @Override public void update(Categoria v) { change("UPDATE categorias SET nombre=? WHERE id=?", v.nombre(), v.id()); }
    @Override public void delete(int id) { change("DELETE FROM categorias WHERE id=?", id); }
}
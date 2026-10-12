package dao.impl;

import dao.EstudianteDAO;
import modelo.Estudiante;
import java.util.*;

public final class EstudianteDAOImpl extends JdbcDAO<Estudiante> implements EstudianteDAO {
    private Estudiante map(java.sql.ResultSet r) throws java.sql.SQLException {
        return new Estudiante(r.getInt("id"), r.getString("nombre"), r.getString("rut"), r.getString("curso"), r.getString("correo"));
    }
    @Override public int create(Estudiante v) { return insert("INSERT INTO estudiantes(nombre,rut,curso,correo) VALUES (?,?,?,?)", v.getNombre(), v.getRut(), v.getCurso(), v.getCorreo()); }
    @Override public Optional<Estudiante> findById(int id) { return query("SELECT * FROM estudiantes WHERE id=?", this::map, id).stream().findFirst(); }
    @Override public List<Estudiante> findAll() { return query("SELECT * FROM estudiantes ORDER BY nombre", this::map); }
    @Override public void update(Estudiante v) { change("UPDATE estudiantes SET nombre=?,rut=?,curso=?,correo=? WHERE id=?", v.getNombre(), v.getRut(), v.getCurso(), v.getCorreo(), v.getId()); }
    @Override public void delete(int id) { change("DELETE FROM estudiantes WHERE id=?", id); }
}
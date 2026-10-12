package dao.impl;

import dao.UsuarioDAO;
import modelo.*;
import java.util.*;

public final class UsuarioDAOImpl extends JdbcDAO<Usuario> implements UsuarioDAO {
    private Usuario map(java.sql.ResultSet r) throws java.sql.SQLException {
        Integer student = (Integer) r.getObject("id_estudiante");
        return new Usuario(r.getInt("id"), r.getString("nombre"), r.getString("rut"), r.getString("correo"),
                r.getString("contraseña"), Rol.fromDatabase(r.getString("rol")), student);
    }
    @Override public int create(Usuario v) { return insert("INSERT INTO usuarios(nombre,rut,correo,`contraseña`,rol,id_estudiante) VALUES (?,?,?,?,?,?)", v.getNombre(), v.getRut(), v.getCorreo(), v.getPasswordHash(), v.getRol().databaseValue(), v.getStudentId()); }
    @Override public Optional<Usuario> findById(int id) { return query("SELECT * FROM usuarios WHERE id=?", this::map, id).stream().findFirst(); }
    @Override public Optional<Usuario> findByEmail(String email) { return query("SELECT * FROM usuarios WHERE correo=?", this::map, email).stream().findFirst(); }
    @Override public List<Usuario> findAll() { return query("SELECT * FROM usuarios ORDER BY nombre", this::map); }
    @Override public void update(Usuario v) { change("UPDATE usuarios SET nombre=?,rut=?,correo=?,`contraseña`=?,rol=?,id_estudiante=? WHERE id=?", v.getNombre(), v.getRut(), v.getCorreo(), v.getPasswordHash(), v.getRol().databaseValue(), v.getStudentId(), v.getId()); }
    @Override public void delete(int id) {
        change("DELETE FROM usuarios WHERE id=? AND rol='estudiante' AND id_estudiante IS NOT NULL", id);
    }
    @Override public void deleteStaff(Session session, int id) {
        db.transaction(c -> { StaffDeletionGuard.delete(c, session, id); return null; });
    }
}
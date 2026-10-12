package dao;

import modelo.Usuario;

public interface UsuarioDAO extends CrudDAO<Usuario> {
    java.util.Optional<Usuario> findByEmail(String email);
    void deleteStaff(modelo.Session session, int id);
}

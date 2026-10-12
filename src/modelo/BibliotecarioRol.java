package modelo;

public final class BibliotecarioRol extends Rol {
    @Override public boolean canManage() { return true; }
    @Override public boolean canAccessStudent(Integer own, int requested) { return requested > 0; }
    @Override public String databaseValue() { return "bibliotecario"; }
}
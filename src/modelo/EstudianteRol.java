package modelo;

public final class EstudianteRol extends Rol implements AccessPolicy, LoanScope {
    @Override public boolean canManage() { return false; }
    @Override public boolean canAccessStudent(Integer own, int requested) {
        return own != null && own == requested && requested > 0;
    }
    @Override public String databaseValue() { return "estudiante"; }
}
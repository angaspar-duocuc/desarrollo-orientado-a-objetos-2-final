package modelo;

public abstract class Rol implements AccessPolicy, LoanScope {
    public abstract String databaseValue();
    public static Rol fromDatabase(String value) {
        return switch (value) {
            case "bibliotecario" -> new BibliotecarioRol();
            case "estudiante" -> new EstudianteRol();
            default -> throw new IllegalArgumentException("Rol no reconocido.");
        };
    }
}
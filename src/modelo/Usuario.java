package modelo;

public final class Usuario extends Persona {
    private final String passwordHash;
    private final Rol rol;
    private final Integer studentId;

    public Usuario(int id, String nombre, String rut, String correo, String passwordHash,
                   Rol rol, Integer studentId) {
        super(id, nombre, rut, correo);
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.studentId = studentId;
    }
    public String getPasswordHash() { return passwordHash; }
    public Rol getRol() { return rol; }
    public Integer getStudentId() { return studentId; }
    @Override public String description() { return getNombre() + " (" + rol.databaseValue() + ")"; }
}
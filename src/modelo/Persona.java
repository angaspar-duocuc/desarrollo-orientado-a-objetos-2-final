package modelo;

public abstract class Persona {
    private final int id;
    private final String nombre;
    private final String rut;
    private final String correo;

    protected Persona(int id, String nombre, String rut, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.rut = rut;
        this.correo = correo;
    }
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getRut() { return rut; }
    public String getCorreo() { return correo; }
    public abstract String description();
}
package vista;

public final class UsuarioView extends CrudPanel {
    public UsuarioView() {
        super(new String[]{"ID","Nombre","RUT","Correo","Rol","Estudiante ID"},
                new String[][]{{"name","Nombre"},{"rut","RUT"},{"email","Correo"},{"role","Rol (cuentas de estudiante: ver Estudiantes)"}}, true, false);
        fixedField("role", "bibliotecario");
        clear.addActionListener(e -> fixedField("role", "bibliotecario"));
    }
}
package vista;

public final class EstudianteView extends CrudPanel {
    public EstudianteView() {
        super(new String[]{"ID","Nombre","RUT","Curso","Correo"},
                new String[][]{{"name","Nombre"},{"rut","RUT"},{"course","Curso"},{"email","Correo"}}, true, false);
    }
}
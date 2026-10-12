package vista;

public final class PrestamoView extends CrudPanel {
    public PrestamoView() {
        super(new String[]{"ID","Estudiante ID","Libro ID","Fecha préstamo","Vencimiento","Estado","Devolución real","Días atraso"},
                new String[][]{{"studentId","Estudiante ID (ver Estudiantes)"},{"bookId","Libro ID (ver Libros)"},
                    {"due","Nuevo vencimiento al editar (AAAA-MM-DD; creación: plazo automático)"}}, false, true);
        create.setText("Registrar préstamo");
        update.setText("Cambiar vencimiento");
        delete.setText("Eliminar préstamo devuelto");
    }
}
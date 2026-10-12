package vista;

public final class CategoriaView extends CrudPanel {
    public CategoriaView() {
        super(new String[]{"ID","Categoría"}, new String[][]{{"name","Nombre de categoría"}}, false, false);
    }
}
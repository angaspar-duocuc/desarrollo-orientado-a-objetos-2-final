package vista;

public final class LibroView extends CrudPanel {
    public LibroView() {
        super(new String[]{"ID","Título","Autor","ISBN","Editorial","Disponibles","Categoría ID"},
                new String[][]{{"title","Título"},{"author","Autor"},{"isbn","ISBN"},{"publisher","Editorial"},
                    {"stock","Ejemplares disponibles (0 permitido)"},{"categoryId","Categoría (crear en Categorías si no hay opciones)"}}, false, false,
                java.util.Set.of("categoryId"));
    }
}

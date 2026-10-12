package modelo;

public record Libro(int id, String titulo, String autor, String isbn, String editorial,
                    int stock, int categoryId, int expectedStock) {
    public Libro(int id, String titulo, String autor, String isbn, String editorial,
                 int stock, int categoryId) {
        this(id, titulo, autor, isbn, editorial, stock, categoryId, stock);
    }
}
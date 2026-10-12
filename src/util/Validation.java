package util;

public final class Validation {
    private Validation() { }
    public static String text(String value, String label, int max) {
        if (value == null || value.isBlank() || value.strip().length() > max)
            throw new AppException(label + ": ingrese entre 1 y " + max + " caracteres.");
        return value.strip();
    }
    public static String email(String value) {
        String clean = text(value, "Correo", 180).toLowerCase(java.util.Locale.ROOT);
        if (!clean.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) throw new AppException("Correo inválido.");
        return clean;
    }
    public static int positive(int value, String label) {
        if (value <= 0) throw new AppException(label + " debe ser un ID positivo.");
        return value;
    }
    public static int stock(int value) {
        if (value < 0 || value > 1_000_000) throw new AppException("El stock debe estar entre 0 y 1000000.");
        return value;
    }
}
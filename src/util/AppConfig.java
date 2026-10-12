package util;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class AppConfig {
    private final Properties local = new Properties();
    public AppConfig() {
        Path file = Path.of("config", "local.properties");
        if (Files.exists(file)) {
            try (InputStream in = Files.newInputStream(file)) { local.load(in); }
            catch (Exception ex) { throw new AppException("No se pudo leer la configuración local.", ex); }
        }
    }
    private String value(String env, String key) {
        String value = System.getenv(env);
        return value != null ? value : local.getProperty(key);
    }
    public String required(String env, String key) {
        String result = value(env, key);
        if (result == null || result.isBlank()) throw new AppException("Falta configuración: " + env + ".");
        return result;
    }
    public String dbPassword() {
        String result = value("LIBRARY_DB_PASSWORD", "db.password");
        if (result == null) throw new AppException("Falta configuración: LIBRARY_DB_PASSWORD.");
        return result;
    }
    public int loanDays() {
        return parseLoanDays(value("LIBRARY_LOAN_DAYS", "loan.days"));
    }
    public static int parseLoanDays(String raw) {
        try {
            int days = raw == null ? 14 : Integer.parseInt(raw);
            if (days < 1 || days > 365) throw new NumberFormatException();
            return days;
        } catch (NumberFormatException ex) { throw new AppException("El plazo debe estar entre 1 y 365 días."); }
    }
}

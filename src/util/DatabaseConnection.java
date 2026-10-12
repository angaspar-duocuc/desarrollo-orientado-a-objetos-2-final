package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.SwingUtilities;

public final class DatabaseConnection {
    private static final DatabaseConnection INSTANCE = new DatabaseConnection();
    private final Object lock = new Object();
    private Connection connection;
    private boolean inTransaction;

    private DatabaseConnection() { }
    public static DatabaseConnection getInstance() { return INSTANCE; }

    @FunctionalInterface public interface SqlWork<T> { T run(Connection connection) throws SQLException; }

    private Connection liveConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            AppConfig config = new AppConfig();
            connection = DriverManager.getConnection(config.required("LIBRARY_DB_URL", "db.url"),
                    config.required("LIBRARY_DB_USER", "db.user"), config.dbPassword());
        }
        return connection;
    }
    private static void requireBackground() {
        if (SwingUtilities.isEventDispatchThread())
            throw new AppException("La operación de base de datos requiere un hilo de trabajo.");
    }
    public <T> T execute(SqlWork<T> work) {
        requireBackground();
        synchronized (lock) {
            try { return work.run(liveConnection()); }
            catch (SQLException ex) { throw translate(ex); }
        }
    }
    public <T> T transaction(SqlWork<T> work) {
        requireBackground();
        synchronized (lock) {
            if (inTransaction) return execute(work);
            Connection c = null;
            try {
                c = liveConnection();
                c.setAutoCommit(false);
                inTransaction = true;
                T result = work.run(c);
                c.commit();
                return result;
            } catch (SQLException | RuntimeException ex) {
                if (c != null) {
                    try { c.rollback(); }
                    catch (SQLException rollbackFailure) { ex.addSuppressed(rollbackFailure); discard(); }
                }
                if (ex instanceof SQLException sql) throw translate(sql);
                throw (RuntimeException) ex;
            } finally {
                inTransaction = false;
                if (c != null) {
                    try { c.setAutoCommit(true); }
                    catch (SQLException ex) { discard(); }
                }
            }
        }
    }
    private void discard() {
        if (connection != null) try { connection.close(); } catch (SQLException ignored) { }
        connection = null;
    }
    public void close() {
        requireBackground();
        synchronized (lock) { discard(); }
    }
    private static AppException translate(SQLException ex) {
        String state = ex.getSQLState();
        String message;
        if (ex.getErrorCode() == 1451) message = "No se puede eliminar: existen registros relacionados.";
        else if (ex.getErrorCode() == 1452) message = "El registro relacionado no existe.";
        else if (ex.getErrorCode() == 1062) message = "Ya existe un registro con ese correo, RUT, ISBN o nombre.";
        else if (state != null && state.startsWith("23")) message = "Los datos incumplen una restricción de integridad.";
        else if (state != null && state.startsWith("08")) message = "No se pudo conectar con MySQL. Revise la configuración y el servicio.";
        else message = "No se pudo completar la operación de base de datos. Revise la configuración y el esquema.";
        return new AppException(message, ex);
    }
}
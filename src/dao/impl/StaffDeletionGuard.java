package dao.impl;

import modelo.Session;
import util.AppException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class StaffDeletionGuard {
    private StaffDeletionGuard() { }

    public static void delete(Connection connection, Session session, int targetId) throws SQLException {
        if (connection.getAutoCommit())
            throw new AppException("La eliminación de bibliotecarios requiere una transacción.");
        session.requireActive();
        List<Integer> latestStaff = new ArrayList<>();
        // La lectura con bloqueo ve las filas confirmadas actuales, no una copia previa de REPEATABLE READ.
        // Bloquear cada bibliotecario en orden de clave primaria ANTES de autorizar al actor o contar.
        try (var statement = connection.prepareStatement(
                "SELECT id FROM usuarios WHERE rol='bibliotecario' ORDER BY id FOR UPDATE")) {
            try (var rows = statement.executeQuery()) {
                while (rows.next()) latestStaff.add(rows.getInt(1));
            }
        }
        session.requireActive();
        if (!latestStaff.contains(session.userId()))
            throw new AppException("La cuenta activa ya no existe o no es bibliotecario.");
        if (targetId == session.userId())
            throw new AppException("No puede eliminar su cuenta activa.");
        if (!latestStaff.contains(targetId))
            throw new AppException("Bibliotecario no encontrado. Gestione las cuentas de estudiante desde Estudiantes.");
        if (latestStaff.size() <= 1)
            throw new AppException("Debe conservar al menos un bibliotecario.");
        try (var statement = connection.prepareStatement(
                "DELETE FROM usuarios WHERE id=? AND rol='bibliotecario' AND id_estudiante IS NULL")) {
            statement.setInt(1, targetId);
            if (statement.executeUpdate() != 1)
                throw new AppException("El bibliotecario cambió o ya no existe. Recargue los datos.");
        }
    }
}
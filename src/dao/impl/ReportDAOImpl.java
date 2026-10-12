package dao.impl;

import dao.ReportDAO;
import modelo.ReportRow;
import java.util.List;

public final class ReportDAOImpl extends JdbcDAO<ReportRow> implements ReportDAO {
    @Override public List<ReportRow> mostBorrowed() {
        return query("SELECT l.id,l.`título`,COUNT(p.id) total FROM libros l LEFT JOIN prestamos p ON p.id_libro=l.id GROUP BY l.id,l.`título` ORDER BY total DESC,l.id",
                r -> new ReportRow(new String[]{r.getString(1),r.getString(2),r.getString(3)}));
    }
    @Override public List<ReportRow> history(int id) {
        return query("SELECT p.id,l.`título`,p.fecha_prestamo,p.fecha_devolucion,p.fecha_devolucion_real,p.devuelto,GREATEST(0,DATEDIFF(COALESCE(p.fecha_devolucion_real,CURRENT_DATE),p.fecha_devolucion)) atraso FROM prestamos p JOIN libros l ON l.id=p.id_libro WHERE p.id_estudiante=? ORDER BY p.id DESC",
                r -> new ReportRow(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5),r.getBoolean(6)?"Devuelto":"Pendiente",r.getString(7)}), id);
    }
    @Override public List<ReportRow> currentlyLoaned(Integer studentId) {
        String filter = studentId == null ? "" : " AND p.id_estudiante=?";
        Object[] args = studentId == null ? new Object[0] : new Object[]{studentId};
        return query("SELECT p.id,e.nombre,l.`título`,p.fecha_devolucion,GREATEST(0,DATEDIFF(CURRENT_DATE,p.fecha_devolucion)) atraso FROM prestamos p JOIN libros l ON l.id=p.id_libro JOIN estudiantes e ON e.id=p.id_estudiante WHERE p.devuelto=false" + filter + " ORDER BY p.fecha_devolucion,p.id",
                r -> new ReportRow(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4),r.getString(5)}), args);
    }
}
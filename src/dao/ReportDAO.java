package dao;

import modelo.ReportRow;
import java.util.List;

public interface ReportDAO {
    List<ReportRow> mostBorrowed();
    List<ReportRow> history(int studentId);
    List<ReportRow> currentlyLoaned(Integer studentId);
}
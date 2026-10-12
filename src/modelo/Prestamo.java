package modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record Prestamo(int id, int studentId, int bookId, LocalDate loanDate,
                       LocalDate dueDate, boolean returned, LocalDate actualReturnDate) {
    public long lateDays(LocalDate today) {
        LocalDate end = returned ? actualReturnDate : today;
        return end == null ? 0 : Math.max(0, ChronoUnit.DAYS.between(dueDate, end));
    }
}
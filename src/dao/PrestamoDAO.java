package dao;

import modelo.Prestamo;

public interface PrestamoDAO extends CrudDAO<Prestamo> {
    java.util.List<Prestamo> findByStudent(int studentId);
    void returnLoan(int id, java.time.LocalDate date);
}

package modelo;

public interface LoanScope {
    boolean canAccessStudent(Integer ownStudentId, int requestedStudentId);
}
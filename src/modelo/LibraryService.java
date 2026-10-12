package modelo;

import dao.*;
import dao.impl.*;
import util.*;
import java.time.LocalDate;
import java.util.*;

public final class LibraryService {
    private final DatabaseConnection db = DatabaseConnection.getInstance();
    private final UsuarioDAO users = new UsuarioDAOImpl();
    private final EstudianteDAO students = new EstudianteDAOImpl();
    private final LibroDAO books = new LibroDAOImpl();
    private final CategoriaDAO categories = new CategoriaDAOImpl();
    private final PrestamoDAO loans = new PrestamoDAOImpl();
    private final ReportDAO reports = new ReportDAOImpl();

    public Usuario login(String email, char[] password) {
        try {
            Usuario user = users.findByEmail(Validation.email(email)).orElse(null);
            if (user == null || !PasswordHasher.verify(password, user.getPasswordHash()))
                throw new AppException("Correo o contraseña incorrectos.");
            if (!user.getRol().canManage() && user.getStudentId() == null)
                throw new AppException("La cuenta no tiene un estudiante asociado.");
            return user;
        } finally { PasswordHasher.clear(password); }
    }
    private Usuario current(Session session) {
        session.requireActive();
        Usuario user = users.findById(session.userId()).orElseThrow(() -> new AppException("La cuenta ya no existe."));
        session.requireActive();
        return user;
    }
    private Usuario staff(Session session) {
        Usuario user = current(session);
        if (!user.getRol().canManage()) throw new AppException("Esta operación requiere un bibliotecario.");
        return user;
    }
    private void scope(Usuario user, int studentId) {
        if (!user.getRol().canAccessStudent(user.getStudentId(), studentId))
            throw new AppException("Solo puede consultar y operar sus propios préstamos.");
    }
    public Usuario profile(Session session) { return db.transaction(c -> current(session)); }
    public List<Libro> books(Session session) { return db.transaction(c -> { current(session); return books.findAll(); }); }
    public List<Categoria> categories(Session session) { return db.transaction(c -> { current(session); return categories.findAll(); }); }
    public List<Estudiante> students(Session session) { return db.transaction(c -> { staff(session); return students.findAll(); }); }
    public List<Usuario> users(Session session) { return db.transaction(c -> { staff(session); return users.findAll(); }); }
    public List<Prestamo> loans(Session session) {
        return db.transaction(c -> {
            Usuario user = current(session);
            return user.getRol().canManage() ? loans.findAll() : loans.findByStudent(ownId(user));
        });
    }
    private int ownId(Usuario user) {
        if (user.getStudentId() == null) throw new AppException("Cuenta sin estudiante asociado.");
        return user.getStudentId();
    }
    private Libro validate(Libro v) {
        return new Libro(v.id(), Validation.text(v.titulo(), "Título", 200), Validation.text(v.autor(), "Autor", 150),
                Validation.text(v.isbn(), "ISBN", 30), Validation.text(v.editorial(), "Editorial", 150),
                Validation.stock(v.stock()), Validation.positive(v.categoryId(), "Categoría"), v.expectedStock());
    }
    public synchronized void saveBook(Session session, Libro value) {
        Libro v = validate(value);
        db.transaction(c -> { staff(session); if (v.id() == 0) books.create(v); else books.update(v); return null; });
    }
    public synchronized void deleteBook(Session session, int id) {
        db.transaction(c -> { staff(session); books.delete(id); return null; });
    }
    public void saveCategory(Session session, Categoria value) {
        Categoria v = new Categoria(value.id(), Validation.text(value.nombre(), "Categoría", 100));
        db.transaction(c -> { staff(session); if (v.id() == 0) categories.create(v); else categories.update(v); return null; });
    }
    public void deleteCategory(Session session, int id) {
        db.transaction(c -> { staff(session); categories.delete(id); return null; });
    }
    public void saveStudent(Session session, Estudiante value, char[] password) {
        try {
            Estudiante v = new Estudiante(value.getId(), Validation.text(value.getNombre(), "Nombre", 150),
                    Validation.text(value.getRut(), "RUT", 25), Validation.text(value.getCurso(), "Curso", 80), Validation.email(value.getCorreo()));
            String newHash = password.length == 0 && v.getId() != 0 ? null : PasswordHasher.hash(password);
            db.transaction(c -> {
                staff(session);
                if (v.getId() == 0) {
                    int id = students.create(v);
                    users.create(new Usuario(0, v.getNombre(), v.getRut(), v.getCorreo(), newHash, new EstudianteRol(), id));
                } else {
                    students.update(v);
                    Usuario account = users.findAll().stream().filter(u -> Objects.equals(u.getStudentId(), v.getId())).findFirst()
                            .orElseThrow(() -> new AppException("Estudiante sin cuenta asociada; operación cancelada."));
                    users.update(new Usuario(account.getId(), v.getNombre(), v.getRut(), v.getCorreo(),
                            newHash == null ? account.getPasswordHash() : newHash, new EstudianteRol(), v.getId()));
                }
                return null;
            });
        } finally { PasswordHasher.clear(password); }
    }
    public void deleteStudent(Session session, int id) {
        db.transaction(c -> {
            staff(session);
            Usuario account = users.findAll().stream().filter(u -> Objects.equals(u.getStudentId(), id)).findFirst()
                    .orElseThrow(() -> new AppException("Estudiante sin cuenta asociada."));
            users.delete(account.getId());
            students.delete(id);
            return null;
        });
    }
    public void saveUser(Session session, Usuario value, char[] password) {
        try {
            String name = Validation.text(value.getNombre(), "Nombre", 150);
            String rut = Validation.text(value.getRut(), "RUT", 25);
            String email = Validation.email(value.getCorreo());
            String newHash = password.length == 0 && value.getId() != 0 ? null : PasswordHasher.hash(password);
            db.transaction(c -> {
                staff(session);
                if (!value.getRol().canManage() || value.getStudentId() != null)
                    throw new AppException("Gestione las cuentas de estudiante en Estudiantes; no se permite cambiar el rol.");
                Usuario old = value.getId() == 0 ? null : users.findById(value.getId())
                        .orElseThrow(() -> new AppException("Usuario no encontrado."));
                if (old != null && (!old.getRol().canManage() || old.getStudentId() != null))
                    throw new AppException("No se permite convertir una cuenta de estudiante en bibliotecario.");
                Usuario v = new Usuario(value.getId(), name, rut, email, newHash == null ? old.getPasswordHash() : newHash,
                        new BibliotecarioRol(), null);
                if (old == null) users.create(v); else users.update(v);
                return null;
            });
        } finally { PasswordHasher.clear(password); }
    }
    public void deleteUser(Session session, int id) {
        users.deleteStaff(session, id);
    }
    public synchronized int registerLoan(Session session, int bookId) {
        return db.transaction(c -> registerLoan(session, ownId(current(session)), bookId));
    }
    public synchronized int registerLoan(Session session, int studentId, int bookId) {
        return db.transaction(c -> {
            Usuario user = current(session);
            scope(user, studentId);
            students.findById(studentId).orElseThrow(() -> new AppException("Estudiante no encontrado."));
            LocalDate today = LocalDate.now();
            return loans.create(new Prestamo(0, studentId, Validation.positive(bookId, "Libro"), today,
                    today.plusDays(new AppConfig().loanDays()), false, null));
        });
    }
    public synchronized void returnLoan(Session session, int loanId) {
        db.transaction(c -> {
            Usuario user = current(session);
            Prestamo old = loans.findById(loanId).orElseThrow(() -> new AppException("Préstamo no encontrado."));
            scope(user, old.studentId());
            loans.returnLoan(loanId, LocalDate.now());
            return null;
        });
    }
    public void updateLoanDueDate(Session session, int id, LocalDate due) {
        db.transaction(c -> {
            staff(session);
            Prestamo old = loans.findById(id).orElseThrow(() -> new AppException("Préstamo no encontrado."));
            loans.update(new Prestamo(id, old.studentId(), old.bookId(), old.loanDate(), due, old.returned(), old.actualReturnDate()));
            return null;
        });
    }
    public synchronized void deleteLoan(Session session, int id) {
        db.transaction(c -> { staff(session); loans.delete(id); return null; });
    }
    public List<ReportRow> mostBorrowed(Session session) {
        return db.transaction(c -> { staff(session); return reports.mostBorrowed(); });
    }
    public List<ReportRow> history(Session session, int studentId) {
        return db.transaction(c -> { scope(current(session), studentId); return reports.history(studentId); });
    }
    public List<ReportRow> currentlyLoaned(Session session) {
        return db.transaction(c -> {
            Usuario u = current(session);
            return reports.currentlyLoaned(u.getRol().canManage() ? null : ownId(u));
        });
    }
}
package controlador;

import modelo.*;
import vista.*;
import util.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.function.*;

public final class LibraryController {
    private final LibraryService service;
    private final Session session;
    private final boolean staff;
    private final MainFrame view;
    private final BackgroundTasks jobs;
    private Snapshot snapshot;
    private record Snapshot(List<Libro> books, List<Estudiante> students, List<Categoria> categories,
                            List<Usuario> users, List<Prestamo> loans) { }

    public LibraryController(LibraryService service, Session session, Usuario profile, Runnable login) {
        Edt.require();
        this.service = service; this.session = session; this.staff = profile.getRol().canManage();
        view = new MainFrame(profile);
        jobs = new BackgroundTasks(() -> session.active() && view.isDisplayable(), view::busy, view::error);
        view.logout.addActionListener(e -> { session.revoke(); view.dispose(); login.run(); });
        view.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                session.revoke();
                new Thread(() -> DatabaseConnection.getInstance().close(), "library-db-close").start();
            }
            @Override public void windowClosed(WindowEvent e) { session.revoke(); }
        });
        bindBooks(); bindLoans(); bindReports();
        if (staff) { bindStudents(); bindCategories(); bindUsers(); }
        view.refreshAll.addActionListener(e -> reload());
    }
    public void show() { Edt.require(); view.setVisible(true); reload(); }
    private Snapshot load() {
        session.requireActive();
        return new Snapshot(service.books(session), staff ? service.students(session) : List.of(),
                staff ? service.categories(session) : List.of(), staff ? service.users(session) : List.of(), service.loans(session));
    }
    private void reload() { jobs.submit(this::load, this::render); }
    private void render(Snapshot data) {
        Edt.require(); snapshot = data;
        view.books.choices("categoryId", data.categories().stream()
                .map(v -> new CrudPanel.Choice(String.valueOf(v.id()), v.nombre())).toList());
        view.books.showRows(data.books().stream().map(v -> new Object[]{v.id(),v.titulo(),v.autor(),v.isbn(),v.editorial(),v.stock(),v.categoryId()}).toList());
        view.loans.showRows(data.loans().stream().map(v -> new Object[]{v.id(),v.studentId(),v.bookId(),v.loanDate(),v.dueDate(),v.returned()?"Devuelto":"Pendiente",v.actualReturnDate(),v.lateDays(LocalDate.now())}).toList());
        if (staff) {
            view.students.showRows(data.students().stream().map(v -> new Object[]{v.getId(),v.getNombre(),v.getRut(),v.getCurso(),v.getCorreo()}).toList());
            view.categories.showRows(data.categories().stream().map(v -> new Object[]{v.id(),v.nombre()}).toList());
            view.users.showRows(data.users().stream().map(v -> new Object[]{v.getId(),v.getNombre(),v.getRut(),v.getCorreo(),v.getRol().databaseValue(),v.getStudentId()}).toList());
            view.users.fixedField("role", "bibliotecario");
        }
    }
    private void mutate(Runnable operation) {
        mutate(operation, () -> {});
    }
    private void mutate(Runnable operation, Runnable cleanup) {
        jobs.submit(() -> {
            session.requireActive();
            operation.run();
            try { return load(); }
            catch (AppException ex) {
                throw new AppException("La operación fue guardada, pero no se pudieron recargar las listas. Actualice antes de repetirla.", ex);
            }
        }, data -> {
            render(data); view.message("Operación guardada en MySQL; listas actualizadas.");
        }, cleanup);
    }
    private void formAction(CrudPanel panel, boolean create, Consumer<FormData> operation) {
        Edt.require();
        FormData data = panel.capture();
        if (!create && data.selectedId() <= 0) {
            PasswordHasher.clear(data.password()); view.error("Seleccione un registro en la tabla."); return;
        }
        mutate(() -> operation.accept(data), () -> PasswordHasher.clear(data.password()));
    }
    private void deleteAction(CrudPanel panel, IntConsumer operation) {
        FormData data = panel.capture();
        PasswordHasher.clear(data.password());
        if (data.selectedId() <= 0) { view.error("Seleccione un registro en la tabla."); return; }
        if (view.confirmDelete()) mutate(() -> operation.accept(data.selectedId()));
    }
    private static int integer(String value, String label) {
        try { return Integer.parseInt(value); }
        catch (NumberFormatException ex) { throw new AppException(label + " debe ser un número entero."); }
    }
    private static LocalDate date(String value) {
        try { return LocalDate.parse(value); }
        catch (DateTimeParseException ex) { throw new AppException("Ingrese la fecha con formato AAAA-MM-DD."); }
    }
    private void bindBooks() {
        LibroView panel = view.books;
        panel.refresh.addActionListener(e -> reload());
        panel.onSelection(i -> {
            if (snapshot == null || i >= snapshot.books().size()) return;
            Libro v = snapshot.books().get(i);
            panel.select(v.id(), Map.of("title",v.titulo(),"author",v.autor(),"isbn",v.isbn(),"publisher",v.editorial(),
                    "stock",String.valueOf(v.stock()),"categoryId",String.valueOf(v.categoryId())),v.stock());
        });
        Consumer<FormData> create = d -> service.saveBook(session, book(d, 0));
        panel.create.addActionListener(e -> formAction(panel, true, create));
        panel.update.addActionListener(e -> formAction(panel, false, d -> service.saveBook(session, book(d,d.selectedId()))));
        panel.delete.addActionListener(e -> deleteAction(panel, id -> service.deleteBook(session,id)));
    }
    private Libro book(FormData d, int id) {
        if (d.field("categoryId").isEmpty())
            throw new AppException("Seleccione una categoría. Si no hay opciones, cree una en Categorías y actualice la lista.");
        return new Libro(id,d.field("title"),d.field("author"),d.field("isbn"),d.field("publisher"),
                integer(d.field("stock"),"Stock"),integer(d.field("categoryId"),"Categoría ID"),d.expectedStock());
    }
    private void bindStudents() {
        EstudianteView p = view.students;
        p.refresh.addActionListener(e -> reload());
        p.onSelection(i -> {
            if (snapshot == null || i >= snapshot.students().size()) return;
            Estudiante v = snapshot.students().get(i);
            p.select(v.getId(),Map.of("name",v.getNombre(),"rut",v.getRut(),"course",v.getCurso(),"email",v.getCorreo()),0);
        });
        p.create.addActionListener(e -> formAction(p,true,d -> service.saveStudent(session,student(d,0),d.password())));
        p.update.addActionListener(e -> formAction(p,false,d -> service.saveStudent(session,student(d,d.selectedId()),d.password())));
        p.delete.addActionListener(e -> deleteAction(p,id -> service.deleteStudent(session,id)));
    }
    private Estudiante student(FormData d,int id) {
        return new Estudiante(id,d.field("name"),d.field("rut"),d.field("course"),d.field("email"));
    }
    private void bindCategories() {
        CategoriaView p = view.categories;
        p.refresh.addActionListener(e -> reload());
        p.onSelection(i -> {
            if (snapshot == null || i >= snapshot.categories().size()) return;
            Categoria v = snapshot.categories().get(i); p.select(v.id(),Map.of("name",v.nombre()),0);
        });
        p.create.addActionListener(e -> formAction(p,true,d -> service.saveCategory(session,new Categoria(0,d.field("name")))));
        p.update.addActionListener(e -> formAction(p,false,d -> service.saveCategory(session,new Categoria(d.selectedId(),d.field("name")))));
        p.delete.addActionListener(e -> deleteAction(p,id -> service.deleteCategory(session,id)));
    }
    private void bindUsers() {
        UsuarioView p = view.users;
        p.refresh.addActionListener(e -> reload());
        p.onSelection(i -> {
            if (snapshot == null || i >= snapshot.users().size()) return;
            Usuario v = snapshot.users().get(i);
            p.select(v.getId(),Map.of("name",v.getNombre(),"rut",v.getRut(),"email",v.getCorreo(),"role",v.getRol().databaseValue()),0);
        });
        p.create.addActionListener(e -> formAction(p,true,d -> service.saveUser(session,user(d,0),d.password())));
        p.update.addActionListener(e -> formAction(p,false,d -> service.saveUser(session,user(d,d.selectedId()),d.password())));
        p.delete.addActionListener(e -> deleteAction(p,id -> service.deleteUser(session,id)));
    }
    private Usuario user(FormData d,int id) {
        return new Usuario(id,d.field("name"),d.field("rut"),d.field("email"),"",new BibliotecarioRol(),null);
    }
    private void bindLoans() {
        PrestamoView p = view.loans;
        p.refresh.addActionListener(e -> reload());
        p.onSelection(i -> {
            if (snapshot == null || i >= snapshot.loans().size()) return;
            Prestamo v = snapshot.loans().get(i);
            p.select(v.id(),Map.of("studentId",String.valueOf(v.studentId()),"bookId",String.valueOf(v.bookId()),"due",v.dueDate().toString()),0);
        });
        p.create.addActionListener(e -> formAction(p,true,d -> {
            int bookId = integer(d.field("bookId"),"Libro ID");
            if (staff) service.registerLoan(session,integer(d.field("studentId"),"Estudiante ID"),bookId);
            else service.registerLoan(session,bookId);
        }));
        p.update.addActionListener(e -> formAction(p,false,d -> service.updateLoanDueDate(session,d.selectedId(),date(d.field("due")))));
        p.returnLoan.addActionListener(e -> formAction(p,false,d -> service.returnLoan(session,d.selectedId())));
        p.delete.addActionListener(e -> deleteAction(p,id -> service.deleteLoan(session,id)));
    }
    private void bindReports() {
        ReportView p = view.reports;
        p.mostBorrowed.addActionListener(e -> jobs.submit(() -> service.mostBorrowed(session),
                rows -> p.show(new String[]{"Libro ID","Título","Total préstamos"},rows)));
        p.currentlyLoaned.addActionListener(e -> jobs.submit(() -> service.currentlyLoaned(session),
                rows -> p.show(new String[]{"Préstamo ID","Estudiante","Título","Vencimiento","Días atraso"},rows)));
        p.history.addActionListener(e -> {
            String captured = p.captureStudentId();
            jobs.submit(() -> service.history(session,integer(captured,"Estudiante ID")),
                    rows -> p.show(new String[]{"Préstamo ID","Título","Fecha préstamo","Vencimiento","Devolución real","Estado","Días atraso"},rows));
        });
    }
}

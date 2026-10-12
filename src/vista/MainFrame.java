package vista;

import modelo.Usuario;
import util.Edt;
import javax.swing.*;
import java.awt.*;

public final class MainFrame extends JFrame {
    public final LibroView books = new LibroView();
    public final EstudianteView students;
    public final CategoriaView categories;
    public final UsuarioView users;
    public final PrestamoView loans = new PrestamoView();
    public final ReportView reports;
    public final JButton logout = new JButton("Cerrar sesión");
    public final JButton refreshAll = new JButton("Actualizar todas las listas");
    private final JTabbedPane navigation = new JTabbedPane();
    private final JLabel status = new JLabel("Listo");

    public MainFrame(Usuario user) {
        super("Biblioteca escolar — " + user.description());
        Edt.require();
        boolean staff = user.getRol().canManage();
        students = staff ? new EstudianteView() : null;
        categories = staff ? new CategoriaView() : null;
        users = staff ? new UsuarioView() : null;
        reports = new ReportView(staff, user.getStudentId());
        navigation.addTab("Libros", books);
        if (staff) {
            navigation.addTab("Estudiantes", students); navigation.addTab("Categorías", categories);
            navigation.addTab("Usuarios", users);
        } else { books.readOnly(); loans.studentLoans(user.getStudentId()); }
        navigation.addTab("Préstamos y devoluciones", loans);
        navigation.addTab("Reportes", reports);
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.add(new JLabel(user.description())); header.add(refreshAll); header.add(logout);
        add(header, BorderLayout.NORTH); add(navigation, BorderLayout.CENTER); add(status, BorderLayout.SOUTH);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1200, 780); setMinimumSize(new Dimension(900,620)); setLocationRelativeTo(null);
    }
    public void busy(boolean busy) {
        Edt.require();
        setChildrenEnabled(navigation, !busy);
        if (!busy) books.restoreSelectorState();
        refreshAll.setEnabled(!busy);
        status.setText(busy ? "Procesando en segundo plano…" : "Listo");
    }
    private void setChildrenEnabled(Container parent, boolean enabled) {
        for (Component component : parent.getComponents()) {
            component.setEnabled(enabled);
            if (component instanceof Container child) setChildrenEnabled(child, enabled);
        }
    }
    public boolean confirmDelete() {
        Edt.require();
        return JOptionPane.showConfirmDialog(this, "¿Desea eliminar el registro seleccionado? Esta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
    public void message(String message) { Edt.require(); status.setText(message); }
    public void error(String message) {
        Edt.require(); JOptionPane.showMessageDialog(this, message, "Biblioteca escolar", JOptionPane.WARNING_MESSAGE);
    }
}

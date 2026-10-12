package vista;

import util.Edt;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public final class ReportView extends JPanel {
    public final JButton mostBorrowed = new JButton("Libros más prestados");
    public final JButton history = new JButton("Historial del estudiante");
    public final JButton currentlyLoaned = new JButton("Libros actualmente en préstamo");
    private final JTextField studentId = new JTextField(8);
    private final DefaultTableModel model = new DefaultTableModel() {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    public ReportView(boolean staff, Integer ownId) {
        super(new BorderLayout(8, 8));
        Edt.require();
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Estudiante ID:")); controls.add(studentId); controls.add(history);
        if (staff) controls.add(mostBorrowed);
        controls.add(currentlyLoaned);
        if (!staff) { studentId.setText(String.valueOf(ownId)); studentId.setEditable(false); }
        add(controls, BorderLayout.NORTH);
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }
    public String captureStudentId() { Edt.require(); return studentId.getText().strip(); }
    public void show(String[] columns, List<modelo.ReportRow> rows) {
        Edt.require();
        model.setDataVector(rows.stream().map(row -> (Object[]) row.values()).toArray(Object[][]::new), columns);
    }
}
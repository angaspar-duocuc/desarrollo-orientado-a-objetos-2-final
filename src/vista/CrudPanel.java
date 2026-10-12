package vista;

import util.Edt;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class CrudPanel extends JPanel {
    public final JButton refresh = new JButton("Actualizar lista");
    public final JButton create = new JButton("Crear");
    public final JButton update = new JButton("Guardar cambios");
    public final JButton delete = new JButton("Eliminar");
    public final JButton clear = new JButton("Limpiar formulario");
    public final JButton returnLoan = new JButton("Devolver préstamo");
    private final Map<String, JTextField> fields = new LinkedHashMap<>();
    private final Map<String, JComboBox<Choice>> selectors = new LinkedHashMap<>();
    private boolean readOnly;
    public record Choice(String id, String label) {
        @Override public String toString() { return label; }
    }
    private final JPasswordField password;
    private final JTable table;
    private final DefaultTableModel model;
    private int selectedId;
    private int expectedStock;

    public CrudPanel(String[] columns, String[][] formFields, boolean withPassword, boolean withReturn) {
        this(columns, formFields, withPassword, withReturn, Set.of());
    }
    protected CrudPanel(String[] columns, String[][] formFields, boolean withPassword, boolean withReturn,
                        Set<String> selectorKeys) {
        super(new BorderLayout(10, 10));
        Edt.require();
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 5));
        for (String[] field : formFields) {
            form.add(new JLabel(field[1]));
            if (selectorKeys.contains(field[0])) {
                JComboBox<Choice> input = new JComboBox<>();
                input.setName(field[0]);
                input.setEditable(false);
                input.setEnabled(false);
                selectors.put(field[0], input);
                form.add(input);
                continue;
            }
            JTextField input = new JTextField(22);
            input.setName(field[0]);
            fields.put(field[0], input);
            form.add(input);
        }
        password = withPassword ? new JPasswordField(22) : null;
        if (password != null) {
            form.add(new JLabel("Contraseña (vacía: conservar al editar)"));
            form.add(password);
        }
        JPanel bottom = new JPanel(new BorderLayout(5, 8));
        bottom.add(form, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        for (JButton button : List.of(refresh, create, update, delete, clear)) buttons.add(button);
        if (withReturn) buttons.add(returnLoan);
        bottom.add(buttons, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);
        clear.addActionListener(e -> clearForm());
    }
    public void onSelection(java.util.function.IntConsumer selected) {
        Edt.require();
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0)
                selected.accept(table.convertRowIndexToModel(table.getSelectedRow()));
        });
    }
    public void showRows(List<Object[]> rows) {
        Edt.require();
        model.setRowCount(0);
        rows.forEach(model::addRow);
        clearForm();
    }
    public void select(int id, Map<String, String> values, int stock) {
        Edt.require();
        selectedId = id;
        expectedStock = stock;
        values.forEach((key, value) -> { if (fields.containsKey(key)) fields.get(key).setText(value); });
        values.forEach((key, value) -> {
            JComboBox<Choice> input = selectors.get(key);
            if (input != null) selectChoice(input, value);
        });
        if (password != null) password.setText("");
    }
    public FormData capture() {
        Edt.require();
        Map<String, String> values = new LinkedHashMap<>();
        fields.forEach((key, input) -> values.put(key, input.getText()));
        selectors.forEach((key, input) -> {
            Choice choice = (Choice) input.getSelectedItem();
            values.put(key, choice == null ? "" : choice.id());
        });
        return new FormData(selectedId, Map.copyOf(values), password == null ? new char[0] : password.getPassword(), expectedStock);
    }
    public void clearForm() {
        Edt.require();
        selectedId = 0;
        expectedStock = 0;
        fields.values().forEach(input -> { if (input.isEditable()) input.setText(""); });
        selectors.values().forEach(input -> input.setSelectedIndex(-1));
        if (password != null) password.setText("");
        table.clearSelection();
    }
    public void fixedField(String key, String value) {
        Edt.require();
        JTextField input = fields.get(key);
        if (input != null) { input.setText(value); input.setEditable(false); }
    }
    public void readOnly() {
        Edt.require();
        readOnly = true;
        create.setVisible(false); update.setVisible(false); delete.setVisible(false);
        fields.values().forEach(input -> input.setEditable(false));
        restoreSelectorState();
    }
    public void choices(String key, List<Choice> choices) {
        Edt.require();
        JComboBox<Choice> input = selectors.get(key);
        Choice previous = (Choice) input.getSelectedItem();
        input.removeAllItems();
        choices.forEach(input::addItem);
        selectChoice(input, previous == null ? "" : previous.id());
        restoreSelectorState();
    }
    private void selectChoice(JComboBox<Choice> input, String id) {
        input.setSelectedIndex(-1);
        for (int i = 0; i < input.getItemCount(); i++) {
            if (input.getItemAt(i).id().equals(id)) { input.setSelectedIndex(i); break; }
        }
    }
    public void restoreSelectorState() {
        Edt.require();
        selectors.values().forEach(input -> input.setEnabled(!readOnly && input.getItemCount() > 0));
    }
    public void studentLoans(int studentId) {
        fixedField("studentId", String.valueOf(studentId));
        update.setVisible(false);
        delete.setVisible(false);
        fields.get("due").setEditable(false);
    }
}

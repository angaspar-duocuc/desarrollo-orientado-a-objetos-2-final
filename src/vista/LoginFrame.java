package vista;

import util.Edt;
import javax.swing.*;
import java.awt.*;

public final class LoginFrame extends JFrame {
    public final JButton signIn = new JButton("Ingresar");
    private final JTextField email = new JTextField(26);
    private final JPasswordField password = new JPasswordField(26);
    private final JLabel status = new JLabel("Ingrese su correo y contraseña.");
    public LoginFrame() {
        super("Biblioteca escolar — Ingreso");
        Edt.require();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
        form.add(new JLabel("Biblioteca escolar"));
        form.add(new JLabel("Correo")); form.add(email);
        form.add(new JLabel("Contraseña")); form.add(password);
        form.add(signIn); form.add(status);
        setContentPane(form);
        getRootPane().setDefaultButton(signIn);
        pack(); setLocationRelativeTo(null);
    }
    public String captureEmail() { Edt.require(); return email.getText(); }
    public char[] capturePassword() { Edt.require(); return password.getPassword(); }
    public void busy(boolean busy) {
        Edt.require(); signIn.setEnabled(!busy); email.setEnabled(!busy); password.setEnabled(!busy);
        status.setText(busy ? "Validando en MySQL…" : "Ingrese su correo y contraseña.");
        password.setText("");
    }
    public void error(String message) { Edt.require(); JOptionPane.showMessageDialog(this, message, "Ingreso", JOptionPane.WARNING_MESSAGE); }
}
package controlador;

import modelo.*;
import vista.LoginFrame;
import util.DatabaseConnection;
import util.PasswordHasher;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicLong;

public final class AuthController {
    private static final AtomicLong GENERATIONS = new AtomicLong();
    private final LibraryService service;
    public AuthController(LibraryService service) { this.service = service; }
    public void showLogin() {
        LoginFrame view = new LoginFrame();
        BackgroundTasks jobs = new BackgroundTasks(view::isDisplayable, view::busy, view::error);
        view.signIn.addActionListener(e -> {
            String email = view.captureEmail();
            char[] password = view.capturePassword();
            jobs.submit(() -> service.login(email, password), user -> {
                view.dispose();
                new LibraryController(service, new Session(user.getId(), GENERATIONS.incrementAndGet()), user,
                        () -> showLogin()).show();
            }, () -> PasswordHasher.clear(password));
        });
        view.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                new Thread(() -> DatabaseConnection.getInstance().close(), "library-db-close").start();
            }
        });
        view.setVisible(true);
    }
}

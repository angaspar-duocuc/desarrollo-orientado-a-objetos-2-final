package controlador;

import util.AppException;
import util.Edt;
import javax.swing.SwingWorker;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.*;

public final class BackgroundTasks {
    private boolean busy;
    private final BooleanSupplier live;
    private final Consumer<Boolean> setBusy;
    private final Consumer<String> error;
    public BackgroundTasks(BooleanSupplier live, Consumer<Boolean> setBusy, Consumer<String> error) {
        this.live = live; this.setBusy = setBusy; this.error = error;
    }
    public <T> void submit(Callable<T> background, Consumer<T> success) {
        submit(background, success, () -> {});
    }
    public <T> void submit(Callable<T> background, Consumer<T> success, Runnable cleanup) {
        Edt.require();
        if (busy || !live.getAsBoolean()) { cleanup.run(); return; }
        busy = true;
        try {
            setBusy.accept(true);
            new SwingWorker<T, Void>() {
                @Override protected T doInBackground() throws Exception {
                    try { return background.call(); }
                    finally { cleanup.run(); }
                }
                @Override protected void done() {
                    Edt.require();
                    busy = false;
                    if (!live.getAsBoolean()) return;
                    setBusy.accept(false);
                    try { success.accept(get()); }
                    catch (InterruptedException ex) { Thread.currentThread().interrupt(); error.accept("La operación fue interrumpida."); }
                    catch (ExecutionException ex) {
                        Throwable cause = ex.getCause();
                        error.accept(cause instanceof AppException ? cause.getMessage() : "No se pudo completar la operación.");
                    } catch (RuntimeException ex) { error.accept("No se pudo actualizar la vista. Recargue los datos."); }
                }
            }.execute();
        } catch (RuntimeException ex) {
            busy = false;
            cleanup.run();
            throw ex;
        }
    }
}

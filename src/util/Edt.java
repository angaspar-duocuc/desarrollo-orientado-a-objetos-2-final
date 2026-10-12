package util;

import javax.swing.SwingUtilities;

public final class Edt {
    private Edt() { }
    public static void require() {
        if (!SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("Swing access must occur on the EDT");
    }
}
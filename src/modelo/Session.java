package modelo;

import util.AppException;

public final class Session {
    private final int userId;
    private final long generation;
    private volatile boolean active = true;
    public Session(int userId, long generation) { this.userId = userId; this.generation = generation; }
    public int userId() { return userId; }
    public long generation() { return generation; }
    public boolean active() { return active; }
    public void revoke() { active = false; }
    public void requireActive() { if (!active) throw new AppException("La sesión ha finalizado."); }
}
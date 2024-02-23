package cat.michal.catbase.server.procedure;

public interface Procedure<T, D> {
    T proceed(D arg);
}

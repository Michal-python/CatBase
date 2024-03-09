package cat.michal.catbase.server.procedure;

public interface BiProcedure<T, D, N> {
    T proceed(D arg, N arg1);
}

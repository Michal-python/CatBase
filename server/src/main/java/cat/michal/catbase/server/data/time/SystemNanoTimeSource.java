package cat.michal.catbase.server.data.time;

public class SystemNanoTimeSource implements TimeSource{
    @Override
    public long nanos() {
        return System.nanoTime();
    }

    @Override
    public long millis() {
        return System.nanoTime() / 1_000_000;
    }
}

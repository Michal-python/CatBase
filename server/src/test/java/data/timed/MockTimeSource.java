package data.timed;

import cat.michal.catbase.server.data.time.TimeSource;

public class MockTimeSource implements TimeSource {

    private long time;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public long nanos() {
        return time;
    }

    @Override
    public long millis() {
        return time / 1_000_000;
    }
}
